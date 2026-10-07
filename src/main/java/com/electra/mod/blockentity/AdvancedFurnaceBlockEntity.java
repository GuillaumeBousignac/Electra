package com.electra.mod.blockentity;

import com.electra.mod.block.AdvancedFurnaceBlock;
import com.electra.mod.energy.ModEnergyStorage;
import com.electra.mod.menu.AdvancedFurnaceMenu;
import com.electra.mod.recipe.AlloyingRecipeInput;
import com.electra.mod.setup.ModBlockEntities;
import com.electra.mod.setup.ModRecipes;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.core.HolderLookup;
import net.minecraft.core.NonNullList;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.network.chat.Component;
import net.minecraft.network.protocol.Packet;
import net.minecraft.network.protocol.game.ClientGamePacketListener;
import net.minecraft.network.protocol.game.ClientboundBlockEntityDataPacket;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.world.Container;
import net.minecraft.world.ContainerHelper;
import net.minecraft.world.MenuProvider;
import net.minecraft.world.WorldlyContainer;
import net.minecraft.world.entity.player.Inventory;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.inventory.AbstractContainerMenu;
import net.minecraft.world.inventory.ContainerData;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.crafting.AbstractCookingRecipe;
import net.minecraft.world.item.crafting.RecipeType;
import net.minecraft.world.item.crafting.SingleRecipeInput;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraft.world.level.block.state.BlockState;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.util.Optional;

public class AdvancedFurnaceBlockEntity extends BlockEntity implements WorldlyContainer, MenuProvider {

    public static final int CAPACITY        = 50_000;
    public static final int MAX_RECEIVE     = 1_000;
    public static final int ENERGY_PER_ITEM = 100;
    public static final int COOK_TIME_TOTAL = 25;

    public static final int SLOT_INPUT1 = 0;
    public static final int SLOT_INPUT2 = 1;
    public static final int SLOT_OUTPUT = 2;

    private static final int[] SLOTS_INPUT  = {SLOT_INPUT1, SLOT_INPUT2};
    private static final int[] SLOTS_OUTPUT = {SLOT_OUTPUT};

    private final NonNullList<ItemStack> items = NonNullList.withSize(3, ItemStack.EMPTY);

    /** Consommateur pur : reçoit 1 000 FE/tick, ne se laisse pas vider par les câbles. */
    private final ModEnergyStorage energyStorage =
            new ModEnergyStorage(CAPACITY, MAX_RECEIVE, 0, this::onEnergyChanged);

    private int cookTime = 0;
    private boolean needsSync = false;

    /**
     * Données envoyées à l'interface. Les paquets de données de menu ne transportent que 16 bits :
     * l'énergie est donc découpée en deux moitiés.
     */
    private final ContainerData data = new ContainerData() {
        @Override
        public int get(int index) {
            int energy = energyStorage.getEnergyStored();
            return switch (index) {
                case 0 -> cookTime;
                case 1 -> COOK_TIME_TOTAL;
                case 2 -> energy & 0xFFFF;
                case 3 -> (energy >>> 16) & 0xFFFF;
                default -> 0;
            };
        }

        @Override public void set(int index, int value) {}
        @Override public int getCount() { return 4; }
    };

    private record Match(ItemStack result, int energyCost, boolean alloy) {}

    public AdvancedFurnaceBlockEntity(BlockPos pos, BlockState state) {
        super(ModBlockEntities.ADVANCED_FURNACE_BE.get(), pos, state);
    }

    private void onEnergyChanged() {
        setChanged();
        needsSync = true;
    }

    // ── Logique ────────────────────────────────────────────────────────────

    public void tick() {
        if (level == null || level.isClientSide()) return;

        Match match = findMatch();
        boolean canRun = match != null
                && energyStorage.getEnergyStored() >= match.energyCost()
                && canInsertOutput(items.get(SLOT_OUTPUT), match.result());

        if (!canRun) {
            if (cookTime != 0) {
                cookTime = 0;
                setChanged();
            }
            AdvancedFurnaceBlock.setLit(level, worldPosition, getBlockState(), false);
        } else {
            AdvancedFurnaceBlock.setLit(level, worldPosition, getBlockState(), true);
            if (++cookTime >= COOK_TIME_TOTAL) {
                cookTime = 0;
                craft(match);
            }
        }

        if (needsSync && level.getGameTime() % 10 == 0) {
            needsSync = false;
            syncToClient();
        }
    }

    private void craft(Match match) {
        energyStorage.extractInternal(match.energyCost());

        if (match.alloy()) {
            items.get(SLOT_INPUT1).shrink(1);
            items.get(SLOT_INPUT2).shrink(1);
        } else if (!items.get(SLOT_INPUT1).isEmpty()) {
            items.get(SLOT_INPUT1).shrink(1);
        } else {
            items.get(SLOT_INPUT2).shrink(1);
        }

        ItemStack output = items.get(SLOT_OUTPUT);
        if (output.isEmpty()) items.set(SLOT_OUTPUT, match.result().copy());
        else output.grow(match.result().getCount());

        setChanged();
        needsSync = true;
    }

    @Nullable
    private Match findMatch() {
        ItemStack in1 = items.get(SLOT_INPUT1);
        ItemStack in2 = items.get(SLOT_INPUT2);
        if (in1.isEmpty() && in2.isEmpty()) return null;

        // Deux entrées : recette d'alliage uniquement
        if (!in1.isEmpty() && !in2.isEmpty()) {
            return level.getRecipeManager()
                    .getRecipeFor(ModRecipes.ALLOYING_TYPE.get(), new AlloyingRecipeInput(in1, in2), level)
                    .map(holder -> new Match(holder.value().getResultItem(level.registryAccess()),
                            holder.value().getEnergyCost(), true))
                    .orElse(null);
        }

        // Une seule entrée : cuisson vanilla (four, haut fourneau, fumoir)
        SingleRecipeInput input = new SingleRecipeInput(in1.isEmpty() ? in2 : in1);
        return cook(RecipeType.SMELTING, input)
                .or(() -> cook(RecipeType.BLASTING, input))
                .or(() -> cook(RecipeType.SMOKING, input))
                .map(result -> new Match(result, ENERGY_PER_ITEM, false))
                .orElse(null);
    }

    private <T extends AbstractCookingRecipe> Optional<ItemStack> cook(RecipeType<T> type, SingleRecipeInput input) {
        return level.getRecipeManager().getRecipeFor(type, input, level)
                .map(holder -> holder.value().getResultItem(level.registryAccess()));
    }

    private static boolean canInsertOutput(ItemStack existing, ItemStack result) {
        if (result.isEmpty()) return false;
        if (existing.isEmpty()) return true;
        if (!ItemStack.isSameItemSameComponents(existing, result)) return false;
        return existing.getCount() + result.getCount() <= existing.getMaxStackSize();
    }

    private void syncToClient() {
        if (level instanceof ServerLevel serverLevel) {
            serverLevel.sendBlockUpdated(worldPosition, getBlockState(), getBlockState(), Block.UPDATE_CLIENTS);
        }
    }

    // ── Accesseurs ─────────────────────────────────────────────────────────

    public ModEnergyStorage getEnergyStorage() { return energyStorage; }
    public ContainerData getContainerData()    { return data; }

    // ── MenuProvider ───────────────────────────────────────────────────────

    @Override
    public @NotNull Component getDisplayName() {
        return Component.translatable("block.electra.advanced_furnace");
    }

    @Nullable
    @Override
    public AbstractContainerMenu createMenu(int windowId, @NotNull Inventory playerInventory, @NotNull Player player) {
        return new AdvancedFurnaceMenu(windowId, playerInventory, this, data);
    }

    // ── Inventaire (WorldlyContainer : entonnoirs et tuyaux) ───────────────

    @Override
    public int[] getSlotsForFace(@NotNull Direction side) {
        return side == Direction.DOWN ? SLOTS_OUTPUT : SLOTS_INPUT;
    }

    @Override
    public boolean canPlaceItemThroughFace(int slot, @NotNull ItemStack stack, @Nullable Direction direction) {
        return canPlaceItem(slot, stack);
    }

    @Override
    public boolean canTakeItemThroughFace(int slot, @NotNull ItemStack stack, @NotNull Direction direction) {
        return slot == SLOT_OUTPUT;
    }

    /** Refuse la case de sortie, et le même objet dans les deux cases d'entrée. */
    @Override
    public boolean canPlaceItem(int slot, @NotNull ItemStack stack) {
        if (slot == SLOT_OUTPUT) return false;
        ItemStack other = items.get(slot == SLOT_INPUT1 ? SLOT_INPUT2 : SLOT_INPUT1);
        return other.isEmpty() || !ItemStack.isSameItemSameComponents(other, stack);
    }

    @Override public int getContainerSize()                              { return items.size(); }
    @Override public boolean isEmpty()                                   { return items.stream().allMatch(ItemStack::isEmpty); }
    @Override public @NotNull ItemStack getItem(int slot)                { return items.get(slot); }
    @Override public @NotNull ItemStack removeItem(int slot, int amount) {
        ItemStack removed = ContainerHelper.removeItem(items, slot, amount);
        if (!removed.isEmpty()) setChanged();
        return removed;
    }
    @Override public @NotNull ItemStack removeItemNoUpdate(int slot)     { return ContainerHelper.takeItem(items, slot); }

    @Override
    public void setItem(int slot, @NotNull ItemStack stack) {
        items.set(slot, stack);
        stack.limitSize(getMaxStackSize(stack));
        setChanged();
    }

    @Override
    public boolean stillValid(@NotNull Player player) {
        return Container.stillValidBlockEntity(this, player);
    }

    @Override
    public void clearContent() {
        items.clear();
    }

    // ── Sauvegarde et synchronisation ──────────────────────────────────────

    @Override
    public @NotNull CompoundTag getUpdateTag(HolderLookup.@NotNull Provider registries) {
        return saveWithoutMetadata(registries);
    }

    @Override
    public Packet<ClientGamePacketListener> getUpdatePacket() {
        return ClientboundBlockEntityDataPacket.create(this);
    }

    @Override
    protected void saveAdditional(@NotNull CompoundTag tag, HolderLookup.@NotNull Provider registries) {
        super.saveAdditional(tag, registries);
        ContainerHelper.saveAllItems(tag, items, registries);
        tag.putInt("energy", energyStorage.getEnergyStored());
        tag.putInt("cookTime", cookTime);
    }

    @Override
    protected void loadAdditional(@NotNull CompoundTag tag, HolderLookup.@NotNull Provider registries) {
        super.loadAdditional(tag, registries);
        items.clear(); // sinon une case vidée côté serveur reste pleine côté client
        ContainerHelper.loadAllItems(tag, items, registries);
        energyStorage.setEnergy(tag.getInt("energy"));
        cookTime = tag.getInt("cookTime");
    }
}
