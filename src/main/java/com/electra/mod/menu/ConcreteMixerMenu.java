package com.electra.mod.menu;

import com.electra.mod.blockentity.ConcreteMixerBlockEntity;
import com.electra.mod.config.ElectraConfig;
import com.electra.mod.setup.ModMenuTypes;
import net.minecraft.network.FriendlyByteBuf;
import net.minecraft.world.Container;
import net.minecraft.world.entity.player.Inventory;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.inventory.AbstractContainerMenu;
import net.minecraft.world.inventory.ContainerData;
import net.minecraft.world.inventory.SimpleContainerData;
import net.minecraft.world.inventory.Slot;
import net.minecraft.world.item.ItemStack;
import org.jetbrains.annotations.NotNull;

public class ConcreteMixerMenu extends AbstractContainerMenu {

    private static final int MACHINE_SLOTS = 2;
    private static final int PLAYER_END = MACHINE_SLOTS + 36;

    private final ConcreteMixerBlockEntity blockEntity;
    private final ContainerData data;

    /** Côté serveur. */
    public ConcreteMixerMenu(int windowId, Inventory playerInventory,
                               ConcreteMixerBlockEntity blockEntity, ContainerData data) {
        super(ModMenuTypes.CONCRETE_MIXER_MENU.get(), windowId);
        checkContainerDataCount(data, 5);
        this.blockEntity = blockEntity;
        this.data = data;

        addSlot(new FilteredSlot(blockEntity, ConcreteMixerBlockEntity.SLOT_INPUT, 56, 35));
        addSlot(new FilteredSlot(blockEntity, ConcreteMixerBlockEntity.SLOT_OUTPUT, 116, 35));

        for (int row = 0; row < 3; row++) {
            for (int col = 0; col < 9; col++) {
                addSlot(new Slot(playerInventory, col + row * 9 + 9, 8 + col * 18, 84 + row * 18));
            }
        }
        for (int col = 0; col < 9; col++) {
            addSlot(new Slot(playerInventory, col, 8 + col * 18, 142));
        }

        addDataSlots(data);
    }

    /** Côté client. */
    public ConcreteMixerMenu(int windowId, Inventory playerInventory, FriendlyByteBuf buf) {
        this(windowId, playerInventory, getBlockEntity(playerInventory, buf), new SimpleContainerData(5));
    }

    private static ConcreteMixerBlockEntity getBlockEntity(Inventory playerInventory, FriendlyByteBuf buf) {
        if (playerInventory.player.level().getBlockEntity(buf.readBlockPos()) instanceof ConcreteMixerBlockEntity concrete_mixer) {
            return concrete_mixer;
        }
        throw new IllegalStateException("Entité de bloc incorrecte à la position donnée");
    }

    public int getCookTime()      { return data.get(0); }
    public int getCookTimeTotal() { return data.get(1); }
    public int getEnergy()        { return (data.get(2) & 0xFFFF) | ((data.get(3) & 0xFFFF) << 16); }
    public int getWater()         { return data.get(4); }
    public int getTankCapacity()  { return ElectraConfig.CONCRETE_MIXER_TANK.get(); }
    public int getCapacity()      { return ElectraConfig.CONCRETE_MIXER_CAPACITY.get(); }

    @Override
    public boolean stillValid(@NotNull Player player) {
        return blockEntity.stillValid(player);
    }

    @Override
    public @NotNull ItemStack quickMoveStack(@NotNull Player player, int index) {
        ItemStack copy = ItemStack.EMPTY;
        Slot slot = slots.get(index);
        if (!slot.hasItem()) return copy;

        ItemStack stack = slot.getItem();
        copy = stack.copy();

        if (index < MACHINE_SLOTS) {
            if (!moveItemStackTo(stack, MACHINE_SLOTS, PLAYER_END, true)) return ItemStack.EMPTY;
            slot.onQuickCraft(stack, copy);
        } else if (!moveItemStackTo(stack, ConcreteMixerBlockEntity.SLOT_INPUT, ConcreteMixerBlockEntity.SLOT_OUTPUT, false)) {
            return ItemStack.EMPTY;
        }

        if (stack.isEmpty()) slot.set(ItemStack.EMPTY);
        else slot.setChanged();

        if (stack.getCount() == copy.getCount()) return ItemStack.EMPTY;
        slot.onTake(player, stack);
        return copy;
    }

    /** Case qui respecte les règles de la bétonnière (sortie interdite, seulement les objets malaxables). */
    private static class FilteredSlot extends Slot {
        FilteredSlot(Container container, int index, int x, int y) {
            super(container, index, x, y);
        }

        @Override
        public boolean mayPlace(@NotNull ItemStack stack) {
            return container.canPlaceItem(getContainerSlot(), stack);
        }
    }
}
