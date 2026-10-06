package com.electra.mod.blockentity;

import com.electra.mod.network.EnergyNetwork;
import com.electra.mod.network.EnergyNetworkManager;
import com.electra.mod.setup.ModBlockEntities;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.core.HolderLookup;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.network.protocol.Packet;
import net.minecraft.network.protocol.game.ClientGamePacketListener;
import net.minecraft.network.protocol.game.ClientboundBlockEntityDataPacket;
import net.minecraft.world.level.block.Blocks;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.block.state.properties.BlockStateProperties;
import net.neoforged.neoforge.energy.EnergyStorage;
import org.jetbrains.annotations.NotNull;
import org.slf4j.Logger;
import com.mojang.logging.LogUtils;

public class LightningCollectorBlockEntity extends BlockEntity {

    public static final int MAX_ENERGY = 1_000_000;

    public static final int ENERGY_PER_STRIKE = 5_000;

    private final EnergyStorage energyStorage = new EnergyStorage(MAX_ENERGY);

    public LightningCollectorBlockEntity(BlockPos pos, BlockState state) {
        super(ModBlockEntities.LIGHTNING_COLLECTOR_BE.get(), pos, state);
    }

    public EnergyStorage getEnergyStorage() {
        return energyStorage;
    }

    public void addEnergy(int amount) {
        energyStorage.receiveEnergy(amount, false);
        setChanged();
        // Envoie le paquet de sync immédiatement
        if (level instanceof net.minecraft.server.level.ServerLevel serverLevel) {
            serverLevel.sendBlockUpdated(worldPosition, getBlockState(), getBlockState(), 3);
        }
    }

    public int getEnergy() {
        return energyStorage.getEnergyStored();
    }

    private boolean wasRodPowered = false;

    private static final Logger LOGGER = LogUtils.getLogger();

    public boolean wasRodPowered() { return wasRodPowered; }
    public void setRodPowered(boolean powered) { this.wasRodPowered = powered; }

    public void tick() {
        if (level == null || level.isClientSide()) return;
        if (!level.isThundering()) return;

        boolean rodPowered = false;
        for (int i = 1; i <= 5; i++) {
            BlockPos above = worldPosition.above(i);
            BlockState state = level.getBlockState(above);
            if (state.is(Blocks.LIGHTNING_ROD)) {
                rodPowered = state.getValue(BlockStateProperties.POWERED);
                break;
            }
            if (state.isSolidRender(level, above)) break;
        }

        if (rodPowered && !wasRodPowered) {
            addEnergy(ENERGY_PER_STRIKE);
            LOGGER.info("Eclair naturel capté ! Energie : {} FE", getEnergy());
        }
        wasRodPowered = rodPowered;
    }

    @Override
    public void setRemoved() {
        super.setRemoved();
    }

    @Override
    public @NotNull CompoundTag getUpdateTag(HolderLookup.@NotNull Provider registries) {
        CompoundTag tag = new CompoundTag();
        tag.putInt("energy", energyStorage.getEnergyStored());
        return tag;
    }

    @Override
    public Packet<ClientGamePacketListener> getUpdatePacket() {
        return ClientboundBlockEntityDataPacket.create(this);
    }

    @Override
    protected void saveAdditional(@NotNull CompoundTag tag, HolderLookup.@NotNull Provider registries) {
        super.saveAdditional(tag, registries);
        tag.putInt("energy", energyStorage.getEnergyStored());
    }

    @Override
    protected void loadAdditional(@NotNull CompoundTag tag, HolderLookup.@NotNull Provider registries) {
        super.loadAdditional(tag, registries);
        int stored = tag.getInt("energy");
        energyStorage.extractEnergy(energyStorage.getEnergyStored(), false);
        energyStorage.receiveEnergy(stored, false);
    }
}