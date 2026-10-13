package com.electra.mod.blockentity;

import com.electra.mod.block.LightningCollectorBlock;
import com.electra.mod.config.ElectraConfig;
import com.electra.mod.energy.ModEnergyStorage;
import com.electra.mod.energy.OnDemandEnergyStorage;
import com.electra.mod.setup.ModBlockEntities;
import com.mojang.logging.LogUtils;
import net.minecraft.core.BlockPos;
import net.minecraft.core.HolderLookup;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.network.protocol.Packet;
import net.minecraft.network.protocol.game.ClientGamePacketListener;
import net.minecraft.network.protocol.game.ClientboundBlockEntityDataPacket;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.Blocks;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.block.state.properties.BlockStateProperties;
import org.jetbrains.annotations.NotNull;
import org.slf4j.Logger;

public class LightningCollectorBlockEntity extends BlockEntity {

    private static final Logger LOGGER = LogUtils.getLogger();

    private static final int ROD_SEARCH_HEIGHT = 5;

    /** Source pure : n'accepte pas d'énergie de l'extérieur. Valeurs lues dans electra-server.toml. */
    private final ModEnergyStorage energyStorage = new OnDemandEnergyStorage(
            ElectraConfig.COLLECTOR_CAPACITY.get(), 0, ElectraConfig.COLLECTOR_MAX_EXTRACT.get(), this::onEnergyChanged);

    private boolean wasRodPowered = false;
    private boolean needsSync = false;

    public LightningCollectorBlockEntity(BlockPos pos, BlockState state) {
        super(ModBlockEntities.LIGHTNING_COLLECTOR_BE.get(), pos, state);
    }

    public ModEnergyStorage getEnergyStorage() { return energyStorage; }

    private void onEnergyChanged() {
        setChanged();
        needsSync = true;
    }

    public void tick() {
        if (level == null || level.isClientSide()) return;

        // Front montant de l'état POWERED du paratonnerre = un éclair vient de frapper
        boolean rodPowered = isRodPowered();
        if (rodPowered && !wasRodPowered) {
            energyStorage.insertInternal(ElectraConfig.COLLECTOR_ENERGY_PER_STRIKE.get());
            LOGGER.debug("Éclair capté en {} : {} FE", worldPosition, energyStorage.getEnergyStored());
        }
        wasRodPowered = rodPowered;

        // Passage chargé ↔ déchargé : change l'état du bloc (cœur visible + lumière)
        boolean charged = energyStorage.getEnergyStored() > 0;
        BlockState state = getBlockState();
        if (state.getValue(LightningCollectorBlock.CHARGED) != charged) {
            level.setBlock(worldPosition, state.setValue(LightningCollectorBlock.CHARGED, charged), Block.UPDATE_ALL);
        }

        // Synchronisation client limitée à deux fois par seconde
        if (needsSync && level.getGameTime() % 10 == 0 && level instanceof ServerLevel serverLevel) {
            needsSync = false;
            serverLevel.sendBlockUpdated(worldPosition, getBlockState(), getBlockState(), Block.UPDATE_CLIENTS);
        }
    }

    private boolean isRodPowered() {
        for (int i = 1; i <= ROD_SEARCH_HEIGHT; i++) {
            BlockPos above = worldPosition.above(i);
            BlockState state = level.getBlockState(above);
            if (state.is(Blocks.LIGHTNING_ROD)) return state.getValue(BlockStateProperties.POWERED);
            if (state.isSolidRender(level, above)) return false;
        }
        return false;
    }

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
        tag.putInt("energy", energyStorage.getEnergyStored());
    }

    @Override
    protected void loadAdditional(@NotNull CompoundTag tag, HolderLookup.@NotNull Provider registries) {
        super.loadAdditional(tag, registries);
        energyStorage.setEnergy(tag.getInt("energy"));
    }
}
