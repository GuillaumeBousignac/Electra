package com.electra.mod.blockentity;

import com.electra.mod.block.AdvancedFurnaceBlock;
import com.electra.mod.menu.AdvancedFurnaceMenu;
import com.electra.mod.recipe.AlloyingRecipe;
import com.electra.mod.recipe.AlloyingRecipeInput;
import com.electra.mod.setup.ModBlockEntities;
import com.electra.mod.setup.ModRecipes;
import net.minecraft.core.BlockPos;
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
import net.minecraft.world.entity.player.Inventory;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.inventory.AbstractContainerMenu;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.crafting.*;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraft.world.level.block.state.BlockState;
import net.neoforged.neoforge.energy.EnergyStorage;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.util.Optional;

public class AdvancedFurnaceBlockEntity extends BlockEntity implements Container, MenuProvider {

    public static final int CAPACITY        = 50_000;
    public static final int MAX_RECEIVE     = 1_000;
    public static final int ENERGY_PER_ITEM = 100;
    private static final int COOK_TIME_TOTAL = 25;

    private static final int SLOT_INPUT1 = 0;
    private static final int SLOT_INPUT2 = 1;
    private static final int SLOT_OUTPUT  = 2;

    private final NonNullList<ItemStack> items = NonNullList.withSize(3, ItemStack.EMPTY);
    private final EnergyStorage energyStorage = new EnergyStorage(CAPACITY, MAX_RECEIVE, MAX_RECEIVE);

    private int cookTime = 0;
    private final int cookTimeTotal = COOK_TIME_TOTAL;

    public AdvancedFurnaceBlockEntity(BlockPos pos, BlockState state) {
        super(ModBlockEntities.ADVANCED_FURNACE_BE.get(), pos, state);
    }

    public void tick() {
        if (level == null || level.isClientSide()) return;

        BlockState currentState = getBlockState();
        ItemStack input1 = items.get(SLOT_INPUT1);
        ItemStack input2 = items.get(SLOT_INPUT2);
        ItemStack result = getRecipeResult(input1, input2);

        if (result.isEmpty() || energyStorage.getEnergyStored() < ENERGY_PER_ITEM
                || !canInsertOutput(items.get(SLOT_OUTPUT), result)) {
            if (cookTime != 0) {
                cookTime = 0;
                AdvancedFurnaceBlock.setLit(level, worldPosition, currentState, false);
                syncToClient();
            }
            return;
        }

        // Active le lit au premier tick de craft
        if (cookTime == 0) {
            AdvancedFurnaceBlock.setLit(level, worldPosition, currentState, true);
        }

        cookTime++;

        if (cookTime >= cookTimeTotal) {
            cookTime = 0;

            int energyCost = ENERGY_PER_ITEM;
            ItemStack currentInput2 = items.get(SLOT_INPUT2);
            if (!currentInput2.isEmpty()) {
                AlloyingRecipeInput alloyInput = new AlloyingRecipeInput(input1, currentInput2);
                Optional<RecipeHolder<AlloyingRecipe>> alloying =
                        level.getRecipeManager().getRecipeFor(ModRecipes.ALLOYING_TYPE.get(), alloyInput, level);
                if (alloying.isPresent()) energyCost = alloying.get().value().getEnergyCost();
            }

            energyStorage.extractEnergy(energyCost, false);
            input1.shrink(1);
            if (!items.get(SLOT_INPUT2).isEmpty()) items.get(SLOT_INPUT2).shrink(1);

            ItemStack output = items.get(SLOT_OUTPUT);
            if (output.isEmpty()) {
                items.set(SLOT_OUTPUT, result.copy());
            } else {
                output.grow(result.getCount());
            }

            setChanged();
            syncToClient();
        }
    }

    private ItemStack getRecipeResult(ItemStack input1, ItemStack input2) {
        if (input1.isEmpty()) return ItemStack.EMPTY;
        if (level == null) return ItemStack.EMPTY;

        // Recettes d'alliage (2 inputs)
        if (!input2.isEmpty()) {
            AlloyingRecipeInput alloyInput = new AlloyingRecipeInput(input1, input2);
            Optional<RecipeHolder<AlloyingRecipe>> alloying =
                    level.getRecipeManager().getRecipeFor(ModRecipes.ALLOYING_TYPE.get(), alloyInput, level);
            if (alloying.isPresent()) return alloying.get().value().getResultItem(level.registryAccess());
            return ItemStack.EMPTY;
        }

        // Recettes vanilla (1 input)
        SingleRecipeInput recipeInput = new SingleRecipeInput(input1);

        Optional<RecipeHolder<SmeltingRecipe>> smelting =
                level.getRecipeManager().getRecipeFor(RecipeType.SMELTING, recipeInput, level);
        if (smelting.isPresent()) return smelting.get().value().getResultItem(level.registryAccess());

        Optional<RecipeHolder<BlastingRecipe>> blasting =
                level.getRecipeManager().getRecipeFor(RecipeType.BLASTING, recipeInput, level);
        if (blasting.isPresent()) return blasting.get().value().getResultItem(level.registryAccess());

        Optional<RecipeHolder<SmokingRecipe>> smoking =
                level.getRecipeManager().getRecipeFor(RecipeType.SMOKING, recipeInput, level);
        return smoking.map(r -> r.value().getResultItem(level.registryAccess())).orElse(ItemStack.EMPTY);
    }

    private boolean isItem(ItemStack stack, String id) {
        return net.minecraft.core.registries.BuiltInRegistries.ITEM
                .getKey(stack.getItem()).toString().equals(id);
    }

    private boolean canInsertOutput(ItemStack existing, ItemStack result) {
        if (existing.isEmpty()) return true;
        if (!ItemStack.isSameItemSameComponents(existing, result)) return false;
        return existing.getCount() + result.getCount() <= existing.getMaxStackSize();
    }

    private void syncToClient() {
        if (level instanceof ServerLevel serverLevel) {
            serverLevel.sendBlockUpdated(worldPosition, getBlockState(), getBlockState(), 3);
        }
    }

    // ── MenuProvider ───────────────────────────────────────────────────────

    @Override
    public @NotNull Component getDisplayName() {
        return Component.translatable("block.electra.advanced_furnace");
    }

    @Nullable
    @Override
    public AbstractContainerMenu createMenu(int windowId, @NotNull Inventory playerInventory, @NotNull Player player) {
        return new AdvancedFurnaceMenu(windowId, playerInventory, this);
    }

    // ── Getters ────────────────────────────────────────────────────────────

    public EnergyStorage getEnergyStorage() { return energyStorage; }
    public int getCookTime()                { return cookTime; }
    public int getCookTimeTotal()           { return cookTimeTotal; }

    // ── Container ──────────────────────────────────────────────────────────

    @Override public int getContainerSize()                              { return items.size(); }
    @Override public boolean isEmpty()                                   { return items.stream().allMatch(ItemStack::isEmpty); }
    @Override public @NotNull ItemStack getItem(int slot)                { return items.get(slot); }
    @Override public @NotNull ItemStack removeItem(int slot, int amount) { return ContainerHelper.removeItem(items, slot, amount); }
    @Override public @NotNull ItemStack removeItemNoUpdate(int slot)     { return ContainerHelper.takeItem(items, slot); }

    @Override
    public void setItem(int slot, @NotNull ItemStack stack) {
        if (slot == SLOT_INPUT1 && !stack.isEmpty() && !items.get(SLOT_INPUT2).isEmpty()) {
            if (ItemStack.isSameItemSameComponents(stack, items.get(SLOT_INPUT2))) return;
        }
        if (slot == SLOT_INPUT2 && !stack.isEmpty() && !items.get(SLOT_INPUT1).isEmpty()) {
            if (ItemStack.isSameItemSameComponents(stack, items.get(SLOT_INPUT1))) return;
        }
        items.set(slot, stack);
        setChanged();
    }

    @Override public boolean stillValid(@NotNull Player player) { return true; }
    @Override public void clearContent()                        { items.clear(); }

    // ── NBT ────────────────────────────────────────────────────────────────

    @Override
    public @NotNull CompoundTag getUpdateTag(HolderLookup.@NotNull Provider registries) {
        CompoundTag tag = new CompoundTag();
        ContainerHelper.saveAllItems(tag, items, registries);
        tag.putInt("energy",   energyStorage.getEnergyStored());
        tag.putInt("cookTime", cookTime);
        return tag;
    }

    @Override
    public Packet<ClientGamePacketListener> getUpdatePacket() {
        return ClientboundBlockEntityDataPacket.create(this);
    }

    @Override
    protected void saveAdditional(@NotNull CompoundTag tag, HolderLookup.@NotNull Provider registries) {
        super.saveAdditional(tag, registries);
        ContainerHelper.saveAllItems(tag, items, registries);
        tag.putInt("energy",   energyStorage.getEnergyStored());
        tag.putInt("cookTime", cookTime);
    }

    @Override
    protected void loadAdditional(@NotNull CompoundTag tag, HolderLookup.@NotNull Provider registries) {
        super.loadAdditional(tag, registries);
        ContainerHelper.loadAllItems(tag, items, registries);
        int stored = tag.getInt("energy");
        energyStorage.extractEnergy(energyStorage.getEnergyStored(), false);
        energyStorage.receiveEnergy(stored, false);
        cookTime = tag.getInt("cookTime");
    }
}