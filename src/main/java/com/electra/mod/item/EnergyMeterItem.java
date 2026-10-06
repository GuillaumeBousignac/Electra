package com.electra.mod.item;

import com.electra.mod.blockentity.AdvancedFurnaceBlockEntity;
import com.electra.mod.blockentity.BareWireBlockEntity;
import com.electra.mod.blockentity.LightningCollectorBlockEntity;
import com.electra.mod.blockentity.RedstoneConverterBlockEntity;
import net.minecraft.core.BlockPos;
import net.minecraft.network.chat.Component;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraft.world.phys.BlockHitResult;
import net.minecraft.world.phys.HitResult;
import net.neoforged.neoforge.energy.EnergyStorage;
import org.jetbrains.annotations.NotNull;

public class EnergyMeterItem extends Item {

    public EnergyMeterItem(Properties properties) {
        super(properties);
    }

    @Override
    public void inventoryTick(@NotNull ItemStack stack, @NotNull Level level,
                              net.minecraft.world.entity.@NotNull Entity entity,
                              int slot, boolean selected) {
        if (!selected || !(entity instanceof Player player)) return;
        if (!isInMainHand(player, stack)) return;

        // Lit côté CLIENT uniquement (les données sont synchro via getUpdatePacket)
        if (level.isClientSide()) {
            displayEnergyInfoClient(player, level);
        }
    }

    private void displayEnergyInfoClient(Player player, Level level) {
        var hitResult = player.pick(20.0, 1.0f, false);
        if (hitResult.getType() != HitResult.Type.BLOCK) return;

        BlockPos pos = ((BlockHitResult) hitResult).getBlockPos();
        BlockEntity be = level.getBlockEntity(pos);
        if (be == null) return;

        EnergyStorage storage = getEnergyStorage(be);
        if (storage == null) return;

        int stored   = storage.getEnergyStored();
        int capacity = storage.getMaxEnergyStored();
        int percent  = capacity > 0 ? (stored * 100 / capacity) : 0;

        player.displayClientMessage(
                Component.literal("§e⚡ " + be.getBlockState().getBlock().getName().getString()
                        + " §7: §a" + stored + " §7/ §a" + capacity + " FE §7(" + percent + "%)"),
                true
        );
    }

    private EnergyStorage getEnergyStorage(BlockEntity be) {
        if (be instanceof LightningCollectorBlockEntity e) return e.getEnergyStorage();
        if (be instanceof BareWireBlockEntity e)          return e.getEnergyStorage();
        if (be instanceof AdvancedFurnaceBlockEntity e)   return e.getEnergyStorage();
        if (be instanceof RedstoneConverterBlockEntity e) return e.getEnergyStorage();
        return null;
    }

    private boolean isInMainHand(Player player, ItemStack stack) {
        return player.getMainHandItem() == stack;
    }
}