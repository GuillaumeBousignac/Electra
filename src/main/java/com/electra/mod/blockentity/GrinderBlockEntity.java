package com.electra.mod.blockentity;

import com.electra.mod.block.GrinderBlock;
import com.electra.mod.config.ElectraConfig;
import com.electra.mod.energy.ModEnergyStorage;
import com.electra.mod.menu.GrinderMenu;
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
import net.minecraft.world.item.crafting.SingleRecipeInput;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraft.world.level.block.state.BlockState;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

/** Broyeur électrique : un objet en entrée, un objet (en plus grande quantité) en sortie. */
public class GrinderBlockEntity extends BlockEntity implements WorldlyContainer, MenuProvider {

    public static final int SLOT_INPUT = 0;
    public static final int SLOT_OUTPUT = 1;

    private static final int[] SLOTS_INPUT  = {SLOT_INPUT};
    private static final int[] SLOTS_OUTPUT = {SLOT_OUTPUT};

    private final NonNullList<ItemStack> items = NonNullList.withSize(2, ItemStack.EMPTY);

    /** Consommateur pur : ne se laisse pas vider par les câbles. */
    private final ModEnergyStorage energyStorage = new ModEnergyStorage(
            ElectraConfig.GRINDER_CAPACITY.get(), ElectraConfig.GRINDER_MAX_RECEIVE.get(), 0, this::onEnergyChanged);

    private int progress = 0;
    private boolean needsSync = false;

    /** Même découpage que le four avancé : l'énergie passe en deux moitiés de 16 bits. */
    private final ContainerData data = new ContainerData() {
        @Override
        public int get(int index) {
            int energy = energyStorage.getEnergyStored();
            return switch (index) {
                case 0 -> progress;
                case 1 -> ElectraConfig.GRINDER_TIME.get();
                case 2 -> energy & 0xFFFF;
                case 3 -> (energy >>> 16) & 0xFFFF;
                default -> 0;
            };
        }

        @Override public void set(int index, int value) {}
        @Override public int getCount() { return 4; }
    };

    private record Match(ItemStack result, int energyCost) {}

    public GrinderBlockEntity(BlockPos pos, BlockState state) {
        super(ModBlockEntities.GRINDER_BE.get(), pos, state);
    }

    private void onEnergyChanged() {
        setChanged();
        needsSync = true;
    }

    // ── Logique ────────────────────────────────────────────────────────────

    public void tick() {
        if (level == null || level.isClientSide()) return;

        Match match = findMatch(items.get(SLOT_INPUT));
        boolean canRun = match != null
                && energyStorage.getEnergyStored() >= match.energyCost()
                && canInsertOutput(items.get(SLOT_OUTPUT), match.result());

        if (!canRun) {
            if (progress != 0) {
                progress = 0;
                setChanged();
            }
            GrinderBlock.setLit(level, worldPosition, getBlockState(), false);
        } else {
            GrinderBlock.setLit(level, worldPosition, getBlockState(), true);
            if (++progress >= ElectraConfig.GRINDER_TIME.get()) {
                progress = 0;
                craft(match);
            }
        }

        if (needsSync && level.getGameTime() % 10 == 0) {
            needsSync = false;
            if (level instanceof ServerLevel serverLevel) {
                serverLevel.sendBlockUpdated(worldPosition, getBlockState(), getBlockState(), Block.UPDATE_CLIENTS);
            }
        }
    }

    private void craft(Match match) {
        energyStorage.extractInternal(match.energyCost());
        items.get(SLOT_INPUT).shrink(1);

        ItemStack output = items.get(SLOT_OUTPUT);
        if (output.isEmpty()) items.set(SLOT_OUTPUT, match.result().copy());
        else output.grow(match.result().getCount());

        setChanged();
        needsSync = true;
    }

    @Nullable
    private Match findMatch(ItemStack input) {
        if (level == null || input.isEmpty()) return null;
        return level.getRecipeManager()
                .getRecipeFor(ModRecipes.GRINDING_TYPE.get(), new SingleRecipeInput(input), level)
                .map(holder -> new Match(holder.value().getResultItem(level.registryAccess()),
                        holder.value().getEnergyCost()))
                .orElse(null);
    }

    private static boolean canInsertOutput(ItemStack existing, ItemStack result) {
        if (result.isEmpty()) return false;
        if (existing.isEmpty()) return true;
        if (!ItemStack.isSameItemSameComponents(existing, result)) return false;
        return existing.getCount() + result.getCount() <= existing.getMaxStackSize();
    }

    // ── Accesseurs ─────────────────────────────────────────────────────────

    public ModEnergyStorage getEnergyStorage() { return energyStorage; }
    public ContainerData getContainerData()    { return data; }

    // ── MenuProvider ───────────────────────────────────────────────────────

    @Override
    public @NotNull Component getDisplayName() {
        return Component.translatable("block.electra.grinder");
    }

    @Nullable
    @Override
    public AbstractContainerMenu createMenu(int windowId, @NotNull Inventory playerInventory, @NotNull Player player) {
        return new GrinderMenu(windowId, playerInventory, this, data);
    }

    // ── Inventaire (entonnoirs et tuyaux) ──────────────────────────────────

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

    /** Seuls les objets qui ont une recette de broyage entrent : pas de bouchon dans la case d'entrée. */
    @Override
    public boolean canPlaceItem(int slot, @NotNull ItemStack stack) {
        return slot == SLOT_INPUT && findMatch(stack) != null;
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
        tag.putInt("progress", progress);
    }

    @Override
    protected void loadAdditional(@NotNull CompoundTag tag, HolderLookup.@NotNull Provider registries) {
        super.loadAdditional(tag, registries);
        items.clear();
        ContainerHelper.loadAllItems(tag, items, registries);
        energyStorage.setEnergy(tag.getInt("energy"));
        progress = tag.getInt("progress");
    }
}
