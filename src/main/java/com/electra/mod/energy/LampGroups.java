package com.electra.mod.energy;

import com.electra.mod.block.ElectricLampBlock;
import com.electra.mod.blockentity.ElectricLampBlockEntity;
import com.electra.mod.config.ElectraConfig;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.world.level.Level;

import java.util.*;

/**
 * Groupes de lampes : toutes les lampes qui se touchent (quelle que soit leur couleur) partagent le courant.
 *
 * Une fois par tick et par groupe, la première lampe qui se met à jour parcourt tout le groupe :
 * - le groupe est alimenté si au moins une de ses lampes reçoit du courant par une face qui ne touche
 *   pas une autre lampe (câble, sortie de porte, machine) ;
 * - la consommation (lampConsumption × nombre de lampes) est prélevée dans les réserves de l'ensemble
 *   du groupe, rechargées par les lampes raccordées ;
 * - le résultat (allumé ou non) est mémorisé pour ce tick : les autres lampes du groupe le lisent sans
 *   refaire le parcours. Coût total : un parcours par groupe et par tick.
 */
public final class LampGroups {

    /** Taille maximale parcourue : au-delà, les lampes forment un autre groupe. */
    private static final int MAX_GROUP_SIZE = 4096;

    private static final Map<Level, TickCache> CACHE = new WeakHashMap<>();

    private record TickCache(long gameTime, Map<BlockPos, Boolean> lit) {}

    private LampGroups() {}

    /** Vrai si la lampe en {@code pos} doit être allumée ce tick. */
    public static boolean isLit(Level level, BlockPos pos) {
        long now = level.getGameTime();
        TickCache cache = CACHE.get(level);
        if (cache == null || cache.gameTime() != now) {
            cache = new TickCache(now, new HashMap<>());
            CACHE.put(level, cache);
        }
        Boolean known = cache.lit().get(pos);
        if (known != null) return known;

        List<ElectricLampBlockEntity> members = collect(level, pos);
        boolean lit = update(level, members);
        for (ElectricLampBlockEntity lamp : members) cache.lit().put(lamp.getBlockPos(), lit);
        return lit;
    }

    /** Parcours en largeur des lampes adjacentes chargées. */
    private static List<ElectricLampBlockEntity> collect(Level level, BlockPos start) {
        List<ElectricLampBlockEntity> members = new ArrayList<>();
        Set<BlockPos> visited = new HashSet<>();
        Deque<BlockPos> queue = new ArrayDeque<>();
        queue.add(start);
        visited.add(start);
        while (!queue.isEmpty() && members.size() < MAX_GROUP_SIZE) {
            BlockPos pos = queue.poll();
            if (!(level.getBlockEntity(pos) instanceof ElectricLampBlockEntity lamp)) continue;
            members.add(lamp);
            for (Direction dir : Direction.values()) {
                BlockPos next = pos.relative(dir);
                if (visited.contains(next) || !level.isLoaded(next)) continue;
                if (level.getBlockState(next).getBlock() instanceof ElectricLampBlock) {
                    visited.add(next);
                    queue.add(next);
                }
            }
        }
        return members;
    }

    /** Alimentation et consommation du groupe ; renvoie l'état allumé commun. */
    private static boolean update(Level level, List<ElectricLampBlockEntity> members) {
        boolean powered = false;
        for (ElectricLampBlockEntity lamp : members) {
            BlockPos pos = lamp.getBlockPos();
            ModEnergyStorage buffer = lamp.getEnergyStorage();
            for (Direction dir : Direction.values()) {
                if (level.getBlockState(pos.relative(dir)).getBlock() instanceof ElectricLampBlock) continue;
                if (!EnergySignals.isPowered(level, pos, dir)) continue;
                powered = true;
                int room = buffer.getMaxEnergyStored() - buffer.getEnergyStored();
                int pulled = EnergySignals.pullDirect(level, pos, dir, room);
                if (pulled > 0) buffer.insertInternal(pulled);
            }
        }
        if (!powered) return false;

        long needed = (long) ElectraConfig.LAMP_CONSUMPTION.get() * members.size();
        if (needed == 0) return true;
        long available = 0;
        for (ElectricLampBlockEntity lamp : members) available += lamp.getEnergyStorage().getEnergyStored();
        if (available < needed) return false;

        // Prélèvement réparti sur les réserves du groupe
        for (ElectricLampBlockEntity lamp : members) {
            if (needed <= 0) break;
            needed -= lamp.getEnergyStorage().extractInternal((int) Math.min(needed, Integer.MAX_VALUE));
        }
        return true;
    }
}
