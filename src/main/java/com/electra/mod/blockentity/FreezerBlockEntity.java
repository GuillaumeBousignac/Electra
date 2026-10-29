package com.electra.mod.blockentity;

import com.electra.mod.block.FreezerBlock;
import com.electra.mod.config.ElectraConfig;
import com.electra.mod.energy.ModEnergyStorage;
import com.electra.mod.fluid.FluidPort;
import com.electra.mod.fluid.FluidPorts;
import com.electra.mod.fluid.ModFluidTank;
import com.electra.mod.menu.FreezerMenu;
import com.electra.mod.recipe.FreezingRecipe;
import com.electra.mod.recipe.FreezingRecipeInput;
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
import net.minecraft.world.item.crafting.RecipeHolder;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.material.Fluids;
import net.neoforged.neoforge.fluids.capability.IFluidHandler;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

/**
 * Congélateur électrique : une cuve d'eau (alimentée par les tuyaux ou au seau), une case d'entrée facultative,
 * une case de sortie. Eau → glace, et glace → glace compacte → glace bleue en y ajoutant de l'eau.
 */
public class FreezerBlockEntity extends BlockEntity implements WorldlyContainer, MenuProvider {

    public static final int SLOT_INPUT = 0;
    public static final int SLOT_OUTPUT = 1;

    private static final int[] SLOTS_INPUT  = {SLOT_INPUT};
    private static final int[] SLOTS_OUTPUT = {SLOT_OUTPUT};

    private final NonNullList<ItemStack> items = NonNullList.withSize(2, ItemStack.EMPTY);

    private final ModEnergyStorage energyStorage = new ModEnergyStorage(
            ElectraConfig.FREEZER_CAPACITY.get(), ElectraConfig.FREEZER_MAX_RECEIVE.get(), 0, this::onChanged);

    /** Eau seulement ; les tuyaux et les seaux peuvent la remplir, rien ne peut la vider de l'extérieur. */
    private final ModFluidTank tank = new ModFluidTank(ElectraConfig.FREEZER_TANK.get(),
            stack -> stack.getFluid().isSame(Fluids.WATER), this::onChanged);
    private final FluidPort port = FluidPorts.sink(tank);

    private int progress = 0;
    private boolean needsSync = false;

    private final ContainerData data = new ContainerData() {
        @Override
        public int get(int index) {
            int energy = energyStorage.getEnergyStored();
            return switch (index) {
                case 0 -> progress;
                case 1 -> ElectraConfig.FREEZER_TIME.get();
                case 2 -> energy & 0xFFFF;
                case 3 -> (energy >>> 16) & 0xFFFF;
                case 4 -> tank.getFluidAmount();
                default -> 0;
            };
        }

        @Override public void set(int index, int value) {}
        @Override public int getCount() { return 5; }
    };

    public FreezerBlockEntity(BlockPos pos, BlockState state) {
        super(ModBlockEntities.FREEZER_BE.get(), pos, state);
    }

    private void onChanged() {
        setChanged();
        needsSync = true;
    }

    // ── Logique ────────────────────────────────────────────────────────────

    public void tick() {
        if (level == null || level.isClientSide()) return;

        FreezingRecipe recipe = findRecipe(items.get(SLOT_INPUT));
        boolean canRun = recipe != null
                && energyStorage.getEnergyStored() >= recipe.getEnergyCost()
                && canInsertOutput(items.get(SLOT_OUTPUT), recipe.getResultItem(level.registryAccess()));

        if (!canRun) {
            if (progress != 0) {
                progress = 0;
                setChanged();
            }
            FreezerBlock.setLit(level, worldPosition, getBlockState(), false);
        } else {
            FreezerBlock.setLit(level, worldPosition, getBlockState(), true);
            if (++progress >= ElectraConfig.FREEZER_TIME.get()) {
                progress = 0;
                craft(recipe);
            }
        }

        if (needsSync && level.getGameTime() % 10 == 0) {
            needsSync = false;
            if (level instanceof ServerLevel serverLevel) {
                serverLevel.sendBlockUpdated(worldPosition, getBlockState(), getBlockState(), Block.UPDATE_CLIENTS);
            }
        }
    }

    private void craft(FreezingRecipe recipe) {
        energyStorage.extractInternal(recipe.getEnergyCost());
        tank.drain(recipe.getFluidAmount(), IFluidHandler.FluidAction.EXECUTE);
        if (recipe.hasInput()) items.get(SLOT_INPUT).shrink(recipe.getInputCount());

        ItemStack result = recipe.getResultItem(level.registryAccess());
        ItemStack output = items.get(SLOT_OUTPUT);
        if (output.isEmpty()) items.set(SLOT_OUTPUT, result);
        else output.grow(result.getCount());

        setChanged();
        needsSync = true;
    }

    /** Une recette qui utilise l'objet présent l'emporte ; à défaut, une recette à l'eau seule. */
    @Nullable
    private FreezingRecipe findRecipe(ItemStack input) {
        if (level == null || tank.isEmpty()) return null;
        FreezingRecipeInput recipeInput = new FreezingRecipeInput(input, tank.getFluidAmount());
        FreezingRecipe waterOnly = null;
        for (RecipeHolder<FreezingRecipe> holder : level.getRecipeManager().getAllRecipesFor(ModRecipes.FREEZING_TYPE.get())) {
            FreezingRecipe recipe = holder.value();
            if (!recipe.matches(recipeInput, level)) continue;
            if (recipe.hasInput()) {
                if (!input.isEmpty()) return recipe;
            } else if (waterOnly == null) {
                waterOnly = recipe;
            }
        }
        return waterOnly;
    }

    private boolean isFreezable(ItemStack stack) {
        if (level == null) return false;
        for (RecipeHolder<FreezingRecipe> holder : level.getRecipeManager().getAllRecipesFor(ModRecipes.FREEZING_TYPE.get())) {
            if (holder.value().getInput().map(i -> i.test(stack)).orElse(false)) return true;
        }
        return false;
    }

    private static boolean canInsertOutput(ItemStack existing, ItemStack result) {
        if (result.isEmpty()) return false;
        if (existing.isEmpty()) return true;
        if (!ItemStack.isSameItemSameComponents(existing, result)) return false;
        return existing.getCount() + result.getCount() <= existing.getMaxStackSize();
    }

    // ── Accesseurs ─────────────────────────────────────────────────────────

    public ModEnergyStorage getEnergyStorage() { return energyStorage; }
    public FluidPort getFluidPort()            { return port; }
    public ModFluidTank getTank()              { return tank; }
    public ContainerData getContainerData()    { return data; }

    // ── MenuProvider ───────────────────────────────────────────────────────

    @Override
    public @NotNull Component getDisplayName() {
        return Component.translatable("block.electra.freezer");
    }

    @Nullable
    @Override
    public AbstractContainerMenu createMenu(int windowId, @NotNull Inventory playerInventory, @NotNull Player player) {
        return new FreezerMenu(windowId, playerInventory, this, data);
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

    /** Seuls les objets qu'une recette de congélation transforme entrent dans la case d'entrée. */
    @Override
    public boolean canPlaceItem(int slot, @NotNull ItemStack stack) {
        return slot == SLOT_INPUT && isFreezable(stack);
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
        tank.save(tag, registries);
    }

    @Override
    protected void loadAdditional(@NotNull CompoundTag tag, HolderLookup.@NotNull Provider registries) {
        super.loadAdditional(tag, registries);
        items.clear();
        ContainerHelper.loadAllItems(tag, items, registries);
        energyStorage.setEnergy(tag.getInt("energy"));
        progress = tag.getInt("progress");
        tank.load(tag, registries);
    }
}
