package com.electra.mod.blockentity;

import com.electra.mod.setup.ModBlockEntities;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.core.HolderLookup;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.network.protocol.Packet;
import net.minecraft.network.protocol.game.ClientGamePacketListener;
import net.minecraft.network.protocol.game.ClientboundBlockEntityDataPacket;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraft.world.level.block.state.BlockState;
import net.neoforged.neoforge.energy.EnergyStorage;
import org.jetbrains.annotations.NotNull;

public class RedstoneConverterBlockEntity extends BlockEntity {

    private boolean powered = false;

    public RedstoneConverterBlockEntity(BlockPos pos, BlockState state) {
        super(ModBlockEntities.REDSTONE_CONVERTER_BE.get(), pos, state);
    }

    public void tick() {
        if (level == null || level.isClientSide()) return;

        // Vérifie si un câble adjacent a de l'énergie
        boolean hasEnergy = false;
        for (Direction dir : Direction.values()) {
            BlockEntity neighbor = level.getBlockEntity(worldPosition.relative(dir));
            if (neighbor instanceof BareWireBlockEntity wire) {
                if (wire.getEnergyStorage().getEnergyStored() > 0) {
                    hasEnergy = true;
                    break;
                }
            }
        }

        // Met à jour le signal seulement si ça change
        if (hasEnergy != powered) {
            powered = hasEnergy;
            setChanged();
            level.updateNeighborsAt(worldPosition, getBlockState().getBlock());
            if (level instanceof ServerLevel serverLevel) {
                serverLevel.sendBlockUpdated(worldPosition, getBlockState(), getBlockState(), 3);
            }
        }
    }

    public int getRedstoneSignal() {
        return powered ? 15 : 0;
    }

    // Gardé pour compatibilité avec EnergyMeterItem
    public EnergyStorage getEnergyStorage() {
        return new EnergyStorage(0);
    }

    @Override
    public @NotNull CompoundTag getUpdateTag(HolderLookup.@NotNull Provider registries) {
        CompoundTag tag = new CompoundTag();
        tag.putBoolean("powered", powered);
        return tag;
    }

    @Override
    public void handleUpdateTag(@NotNull CompoundTag tag, HolderLookup.@NotNull Provider registries) {
        powered = tag.getBoolean("powered");
    }

    @Override
    public Packet<ClientGamePacketListener> getUpdatePacket() {
        return ClientboundBlockEntityDataPacket.create(this);
    }

    @Override
    protected void saveAdditional(@NotNull CompoundTag tag, HolderLookup.@NotNull Provider registries) {
        super.saveAdditional(tag, registries);
        tag.putBoolean("powered", powered);
    }

    @Override
    protected void loadAdditional(@NotNull CompoundTag tag, HolderLookup.@NotNull Provider registries) {
        super.loadAdditional(tag, registries);
        powered = tag.getBoolean("powered");
    }
}