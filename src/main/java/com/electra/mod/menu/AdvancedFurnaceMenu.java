package com.electra.mod.menu;

import com.electra.mod.blockentity.AdvancedFurnaceBlockEntity;
import com.electra.mod.setup.ModMenuTypes;
import net.minecraft.network.FriendlyByteBuf;
import net.minecraft.world.entity.player.Inventory;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.inventory.*;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.block.entity.BlockEntity;
import org.jetbrains.annotations.NotNull;

public class AdvancedFurnaceMenu extends AbstractContainerMenu {

    private final AdvancedFurnaceBlockEntity blockEntity;
    private final ContainerData data;

    public AdvancedFurnaceMenu(int windowId, Inventory playerInventory, AdvancedFurnaceBlockEntity blockEntity) {
        super(ModMenuTypes.ADVANCED_FURNACE_MENU.get(), windowId);
        this.blockEntity = blockEntity;
        this.data = new SimpleContainerData(3);

        addSlot(new Slot(blockEntity, 0, 56, 22));
        addSlot(new Slot(blockEntity, 1, 56, 47));
        addSlot(new Slot(blockEntity, 2, 116, 35) {
            @Override
            public boolean mayPlace(@NotNull ItemStack stack) { return false; }
        });

        // Inventaire joueur (3 rangées)
        for (int row = 0; row < 3; row++) {
            for (int col = 0; col < 9; col++) {
                addSlot(new Slot(playerInventory, col + row * 9 + 9, 8 + col * 18, 84 + row * 18));
            }
        }

        // Hotbar
        for (int col = 0; col < 9; col++) {
            addSlot(new Slot(playerInventory, col, 8 + col * 18, 142));
        }

        addDataSlots(data);
    }

    public AdvancedFurnaceMenu(int windowId, Inventory playerInventory, FriendlyByteBuf buf) {
        this(windowId, playerInventory, getTileEntity(playerInventory, buf));
    }

    private static AdvancedFurnaceBlockEntity getTileEntity(Inventory playerInventory, FriendlyByteBuf buf) {
        BlockEntity be = playerInventory.player.level().getBlockEntity(buf.readBlockPos());
        if (be instanceof AdvancedFurnaceBlockEntity furnace) return furnace;
        throw new IllegalStateException("BlockEntity incorrect à la position donnée");
    }

    @Override
    public void broadcastChanges() {
        data.set(0, blockEntity.getCookTime());
        data.set(1, blockEntity.getCookTimeTotal());
        data.set(2, blockEntity.getEnergyStorage().getEnergyStored());
        super.broadcastChanges();
    }

    public int getCookTime()      { return data.get(0); }
    public int getCookTimeTotal() { return data.get(1); }
    public int getEnergy()        { return data.get(2); }
    public int getCapacity()      { return AdvancedFurnaceBlockEntity.CAPACITY; }

    @Override
    public boolean stillValid(@NotNull Player player) {
        return blockEntity.stillValid(player);
    }

    @Override
    public @NotNull ItemStack quickMoveStack(@NotNull Player player, int index) {
        ItemStack itemstack = ItemStack.EMPTY;
        Slot slot = slots.get(index);

        if (slot.hasItem()) {
            ItemStack stack = slot.getItem();
            itemstack = stack.copy();

            if (index == 2) {
                if (!moveItemStackTo(stack, 3, 39, true)) return ItemStack.EMPTY;
                slot.onQuickCraft(stack, itemstack);
            } else if (index >= 3) {
                if (!moveItemStackTo(stack, 0, 2, false)) return ItemStack.EMPTY;
            } else {
                if (!moveItemStackTo(stack, 3, 39, false)) return ItemStack.EMPTY;
            }

            if (stack.isEmpty()) slot.set(ItemStack.EMPTY);
            else slot.setChanged();

            if (stack.getCount() == itemstack.getCount()) return ItemStack.EMPTY;
            slot.onTake(player, stack);
        }

        return itemstack;
    }
}