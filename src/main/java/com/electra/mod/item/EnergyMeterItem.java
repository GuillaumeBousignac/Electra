package com.electra.mod.item;

import net.minecraft.core.BlockPos;
import net.minecraft.network.chat.Component;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.Level;
import net.minecraft.world.phys.BlockHitResult;
import net.minecraft.world.phys.HitResult;
import net.neoforged.neoforge.capabilities.Capabilities;
import net.neoforged.neoforge.energy.IEnergyStorage;
import org.jetbrains.annotations.NotNull;

/** Affiche l'énergie de tout bloc compatible FE visé, Electra ou autre mod. */
public class EnergyMeterItem extends Item {

    public EnergyMeterItem(Properties properties) {
        super(properties);
    }

    @Override
    public void inventoryTick(@NotNull ItemStack stack, @NotNull Level level, @NotNull Entity entity,
                              int slot, boolean selected) {
        if (!level.isClientSide() || !(entity instanceof Player player)) return;
        if (player.getMainHandItem() != stack) return;

        HitResult hit = player.pick(20.0, 1.0f, false);
        if (hit.getType() != HitResult.Type.BLOCK) return;

        BlockHitResult blockHit = (BlockHitResult) hit;
        BlockPos pos = blockHit.getBlockPos();
        IEnergyStorage storage = level.getCapability(Capabilities.EnergyStorage.BLOCK, pos, blockHit.getDirection());
        if (storage == null) storage = level.getCapability(Capabilities.EnergyStorage.BLOCK, pos, null);
        if (storage == null) return;

        int stored = storage.getEnergyStored();
        int capacity = storage.getMaxEnergyStored();
        int percent = capacity > 0 ? (int) ((long) stored * 100 / capacity) : 0;

        player.displayClientMessage(Component.translatable("message.electra.energy_meter",
                level.getBlockState(pos).getBlock().getName(), stored, capacity, percent), true);
    }
}
