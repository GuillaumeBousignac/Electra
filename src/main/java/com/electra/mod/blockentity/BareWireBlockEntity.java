package com.electra.mod.blockentity;

import com.electra.mod.network.EnergyNetworkManager;
import com.electra.mod.setup.ModBlockEntities;
import net.minecraft.core.BlockPos;
import net.minecraft.core.HolderLookup;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.network.protocol.Packet;
import net.minecraft.network.protocol.game.ClientGamePacketListener;
import net.minecraft.network.protocol.game.ClientboundBlockEntityDataPacket;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraft.world.level.block.state.BlockState;
import net.neoforged.neoforge.energy.EnergyStorage;
import org.jetbrains.annotations.NotNull;

public class BareWireBlockEntity extends BlockEntity {

    public static final int CAPACITY = 10_000;

    private int displayEnergy = 0;
    private int persistedEnergy = 0; // énergie persistée sur disque

    private final EnergyStorage energyStorage = new EnergyStorage(CAPACITY) {
        @Override
        public int getEnergyStored() { return displayEnergy; }
    };

    public BareWireBlockEntity(BlockPos pos, BlockState state) {
        super(ModBlockEntities.BARE_WIRE_BE.get(), pos, state);
    }

    @Override
    public void onLoad() {
        super.onLoad();
        if (level != null && !level.isClientSide()) {
            EnergyNetworkManager.get().onWirePlaced(this, level);
        }
    }

    @Override
    public void onChunkUnloaded() {
        super.onChunkUnloaded();
        if (level != null && !level.isClientSide() && EnergyNetworkManager.get().isActive()) {
            EnergyNetworkManager.get().saveWireEnergy(this);
            EnergyNetworkManager.get().onWireRemoved(worldPosition, level);
        }
    }

    @Override
    public void setRemoved() {
        if (level != null && !level.isClientSide() && EnergyNetworkManager.get().isActive()) {
            EnergyNetworkManager.get().onWireRemoved(worldPosition, level);
        }
        super.setRemoved();
    }

    public void setDisplayEnergy(int energy) {
        this.displayEnergy = energy;
        this.persistedEnergy = energy; // toujours synchro
    }

    public int getPersistedEnergy() { return persistedEnergy; }
    public EnergyStorage getEnergyStorage() { return energyStorage; }

    @Override
    public @NotNull CompoundTag getUpdateTag(HolderLookup.@NotNull Provider registries) {
        CompoundTag tag = new CompoundTag();
        tag.putInt("energy", displayEnergy);
        return tag;
    }

    @Override
    public void handleUpdateTag(@NotNull CompoundTag tag, HolderLookup.@NotNull Provider registries) {
        displayEnergy = tag.getInt("energy");
    }

    @Override
    public Packet<ClientGamePacketListener> getUpdatePacket() {
        return ClientboundBlockEntityDataPacket.create(this);
    }

    @Override
    protected void saveAdditional(@NotNull CompoundTag tag, HolderLookup.@NotNull Provider registries) {
        super.saveAdditional(tag, registries);
        tag.putInt("energy", persistedEnergy);
    }

    @Override
    protected void loadAdditional(@NotNull CompoundTag tag, HolderLookup.@NotNull Provider registries) {
        super.loadAdditional(tag, registries);
        persistedEnergy = tag.getInt("energy");
        displayEnergy = persistedEnergy;
    }
}