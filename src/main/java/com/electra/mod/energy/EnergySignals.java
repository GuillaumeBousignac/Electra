package com.electra.mod.energy;

import com.electra.mod.blockentity.WireBlockEntity;
import com.electra.mod.network.EnergyNetwork;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.world.level.Level;
import net.neoforged.neoforge.capabilities.Capabilities;
import net.neoforged.neoforge.energy.IEnergyStorage;

/**
 * Lecture et prélèvement du courant sur une face, pour les composants logiques (portes, lampes).
 *
 * Une face est « alimentée » quand du courant est disponible :
 * - câble voisin : son réseau contient de l'énergie ou reçoit celle d'une source ce tick.
 *   Un réseau privé de source se décharge en moins d'une seconde (EnergyNetwork) : l'entrée s'éteint vite ;
 * - machine voisine sans câble (ex. sortie d'une autre porte) : elle peut fournir du FE maintenant.
 */
public final class EnergySignals {

    private EnergySignals() {}

    public static boolean isPowered(Level level, BlockPos pos, Direction side) {
        BlockPos neighborPos = pos.relative(side);
        if (!level.isLoaded(neighborPos)) return false;

        if (level.getBlockEntity(neighborPos) instanceof WireBlockEntity wire) {
            EnergyNetwork network = wire.getNetwork();
            return network != null && (network.isFed() || network.getEnergyStored() > 0);
        }
        IEnergyStorage source = directSource(level, pos, side);
        return source != null && source.extractEnergy(1, true) > 0;
    }

    /**
     * Prélève jusqu'à {@code max} FE sur une machine voisine raccordée directement (sans câble).
     * Les câbles, eux, poussent l'énergie d'eux-mêmes : rien à faire dans ce cas.
     */
    public static int pullDirect(Level level, BlockPos pos, Direction side, int max) {
        if (max <= 0) return 0;
        IEnergyStorage source = directSource(level, pos, side);
        return source == null ? 0 : source.extractEnergy(max, false);
    }

    private static IEnergyStorage directSource(Level level, BlockPos pos, Direction side) {
        BlockPos neighborPos = pos.relative(side);
        if (!level.isLoaded(neighborPos) || level.getBlockEntity(neighborPos) instanceof WireBlockEntity) return null;
        IEnergyStorage storage = level.getCapability(Capabilities.EnergyStorage.BLOCK, neighborPos, side.getOpposite());
        return storage != null && storage.canExtract() ? storage : null;
    }
}
