package com.electra.mod.blockentity;

import com.electra.mod.block.ElectricLampBlock;
import com.electra.mod.config.ElectraConfig;
import com.electra.mod.energy.EnergySignals;
import com.electra.mod.energy.ModEnergyStorage;
import com.electra.mod.setup.ModBlockEntities;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.core.HolderLookup;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraft.world.level.block.state.BlockState;
import net.neoforged.neoforge.energy.IEnergyStorage;
import org.jetbrains.annotations.NotNull;

/**
 * Lampe : allumée tant que du courant arrive sur une face (EnergySignals#isPowered)
 * ET que sa petite réserve couvre la consommation du tick.
 * Elle se recharge par toutes ses faces : poussée des câbles, ou prélèvement direct sur une machine voisine.
 */
public class ElectricLampBlockEntity extends BlockEntity {

    private static final int BUFFER = 200;
    private static final int MAX_RECEIVE = 20;

    private final ModEnergyStorage buffer = new ModEnergyStorage(BUFFER, MAX_RECEIVE, 0, this::setChanged);

    public ElectricLampBlockEntity(BlockPos pos, BlockState state) {
        super(ModBlockEntities.ELECTRIC_LAMP_BE.get(), pos, state);
    }

    /** Récepteur pur sur toutes les faces : les câbles s'y raccordent et le rechargent. */
    public IEnergyStorage getEnergyStorage() { return buffer; }

    public void tick() {
        if (level == null || level.isClientSide()) return;

        boolean powered = false;
        for (Direction side : Direction.values()) {
            if (!EnergySignals.isPowered(level, worldPosition, side)) continue;
            powered = true;
            int room = buffer.getMaxEnergyStored() - buffer.getEnergyStored();
            int pulled = EnergySignals.pullDirect(level, worldPosition, side, Math.min(MAX_RECEIVE, room));
            if (pulled > 0) buffer.insertInternal(pulled);
        }

        int cost = ElectraConfig.LAMP_CONSUMPTION.get();
        boolean lit = powered && buffer.getEnergyStored() >= cost;
        if (lit) buffer.extractInternal(cost);

        BlockState state = getBlockState();
        if (state.getValue(ElectricLampBlock.LIT) != lit) {
            level.setBlock(worldPosition, state.setValue(ElectricLampBlock.LIT, lit), Block.UPDATE_CLIENTS);
        }
    }

    @Override
    protected void saveAdditional(@NotNull CompoundTag tag, HolderLookup.@NotNull Provider registries) {
        super.saveAdditional(tag, registries);
        tag.putInt("energy", buffer.getEnergyStored());
    }

    @Override
    protected void loadAdditional(@NotNull CompoundTag tag, HolderLookup.@NotNull Provider registries) {
        super.loadAdditional(tag, registries);
        buffer.setEnergy(tag.getInt("energy"));
    }
}
