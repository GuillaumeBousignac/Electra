package com.electra.mod.blockentity;

import com.electra.mod.block.RedstoneConverterBlock;
import com.electra.mod.energy.EnergySignals;
import com.electra.mod.setup.ModBlockEntities;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraft.world.level.block.state.BlockState;

/**
 * Met le bloc à l'état POWERED tant qu'un câble adjacent appartient à un réseau alimenté.
 * L'état du bloc porte l'information : signal redstone, texture allumée et lumière.
 */
public class RedstoneConverterBlockEntity extends BlockEntity {

    public RedstoneConverterBlockEntity(BlockPos pos, BlockState state) {
        super(ModBlockEntities.REDSTONE_CONVERTER_BE.get(), pos, state);
    }

    public void tick() {
        if (level == null || level.isClientSide()) return;

        // Allumé tant qu'un câble voisin reçoit du courant d'une source (et non plus tant qu'il en stocke) :
        // le signal redstone suit ainsi les portes logiques sans attendre que les câbles se vident.
        boolean hasEnergy = false;
        for (Direction dir : Direction.values()) {
            if (level.getBlockEntity(worldPosition.relative(dir)) instanceof WireBlockEntity
                    && EnergySignals.isPowered(level, worldPosition, dir)) {
                hasEnergy = true;
                break;
            }
        }

        BlockState state = getBlockState();
        if (state.getValue(RedstoneConverterBlock.POWERED) != hasEnergy) {
            // UPDATE_ALL prévient les voisins (redstone) et le client (texture)
            level.setBlock(worldPosition, state.setValue(RedstoneConverterBlock.POWERED, hasEnergy), Block.UPDATE_ALL);
        }
    }

    public int getRedstoneSignal() {
        return getBlockState().getValue(RedstoneConverterBlock.POWERED) ? 15 : 0;
    }
}