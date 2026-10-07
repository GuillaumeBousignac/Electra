package com.electra.mod.blockentity;

import com.electra.mod.network.EnergyNetwork;
import com.electra.mod.network.EnergyNetworkManager;
import com.electra.mod.setup.ModBlockEntities;
import net.minecraft.core.BlockPos;
import net.minecraft.core.HolderLookup;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.network.protocol.Packet;
import net.minecraft.network.protocol.game.ClientGamePacketListener;
import net.minecraft.network.protocol.game.ClientboundBlockEntityDataPacket;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraft.world.level.block.state.BlockState;
import net.neoforged.neoforge.energy.IEnergyStorage;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

/**
 * Entité commune à tous les câbles. L'énergie réelle est stockée dans l'EnergyNetwork ;
 * chaque câble ne garde que sa part, pour la sauvegarde et l'affichage côté client.
 */
public class WireBlockEntity extends BlockEntity {

    public static final int CAPACITY = 10_000;

    private int energyShare = 0;
    private boolean unloading = false;

    /** Ce que voient les autres mods : on peut injecter du FE dans le câble, pas en retirer. */
    private final IEnergyStorage externalStorage = new IEnergyStorage() {
        @Override
        public int receiveEnergy(int maxReceive, boolean simulate) {
            EnergyNetwork net = getNetwork();
            return net == null ? 0 : net.receive(maxReceive, simulate);
        }

        @Override public int extractEnergy(int maxExtract, boolean simulate) { return 0; }

        @Override
        public int getEnergyStored() {
            EnergyNetwork net = getNetwork();
            return net == null ? energyShare : net.getShare();
        }

        @Override public int getMaxEnergyStored() { return CAPACITY; }
        @Override public boolean canExtract()     { return false; }
        @Override public boolean canReceive()     { return true; }
    };

    public WireBlockEntity(BlockPos pos, BlockState state) {
        super(ModBlockEntities.WIRE_BE.get(), pos, state);
    }

    // ── Réseau ─────────────────────────────────────────────────────────────

    @Nullable
    public EnergyNetwork getNetwork() {
        if (level == null || level.isClientSide() || !EnergyNetworkManager.isActive()) return null;
        return EnergyNetworkManager.get(level).getNetwork(worldPosition);
    }

    /** Énergie totale du réseau (serveur), ou part locale (client). */
    public int getNetworkEnergy() {
        EnergyNetwork net = getNetwork();
        return net == null ? energyShare : net.getEnergyStored();
    }

    public IEnergyStorage getExternalStorage() { return externalStorage; }
    public int getEnergyShare()                { return energyShare; }

    /** Appelé par le réseau une fois par seconde, et à l'arrêt du serveur. */
    public void updateShare(int share) {
        if (share == energyShare) return;
        energyShare = share;
        setChanged();
        if (level instanceof ServerLevel serverLevel) {
            serverLevel.sendBlockUpdated(worldPosition, getBlockState(), getBlockState(), Block.UPDATE_CLIENTS);
        }
    }

    @Override
    public void onLoad() {
        super.onLoad();
        if (level != null && !level.isClientSide() && EnergyNetworkManager.isActive()) {
            EnergyNetworkManager.get(level).onWirePlaced(this);
        }
    }

    @Override
    public void onChunkUnloaded() {
        super.onChunkUnloaded();
        unloading = true;
        if (level != null && !level.isClientSide() && EnergyNetworkManager.isActive()) {
            EnergyNetworkManager.get(level).onWireRemoved(this);
        }
    }

    @Override
    public void setRemoved() {
        // Déchargement du tronçon : déjà traité dans onChunkUnloaded
        if (!unloading && level != null && !level.isClientSide() && EnergyNetworkManager.isActive()) {
            EnergyNetworkManager.get(level).onWireRemoved(this);
        }
        super.setRemoved();
    }

    // ── Synchronisation et sauvegarde ──────────────────────────────────────

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
        tag.putInt("energy", energyShare);
    }

    @Override
    protected void loadAdditional(@NotNull CompoundTag tag, HolderLookup.@NotNull Provider registries) {
        super.loadAdditional(tag, registries);
        energyShare = tag.getInt("energy");
    }
}
