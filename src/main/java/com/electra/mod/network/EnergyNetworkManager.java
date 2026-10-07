package com.electra.mod.network;

import com.electra.mod.block.WireBlock;
import com.electra.mod.blockentity.WireBlockEntity;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.resources.ResourceKey;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.world.level.Level;
import org.jetbrains.annotations.Nullable;

import java.util.*;

/**
 * Un gestionnaire par dimension. Ne lit jamais le monde pour trouver les câbles :
 * il ne connaît que ceux qui se sont déclarés (onLoad), ce qui évite tout chargement
 * de tronçon involontaire.
 */
public final class EnergyNetworkManager {

    private static final Map<ResourceKey<Level>, EnergyNetworkManager> MANAGERS = new HashMap<>();
    private static boolean active = true;

    private final Map<BlockPos, WireBlockEntity> wires = new HashMap<>();
    private final Map<BlockPos, EnergyNetwork> byPos = new HashMap<>();
    private final Set<EnergyNetwork> networks = new LinkedHashSet<>();

    private EnergyNetworkManager() {}

    // ── Cycle de vie ───────────────────────────────────────────────────────

    public static EnergyNetworkManager get(Level level) {
        return MANAGERS.computeIfAbsent(level.dimension(), key -> new EnergyNetworkManager());
    }

    public static boolean isActive() { return active; }

    /** Au démarrage du serveur (y compris en réouvrant un monde solo). */
    public static void start() {
        MANAGERS.clear();
        active = true;
    }

    /** À l'arrêt : recopie l'énergie dans les câbles AVANT la sauvegarde finale. */
    public static void shutdown() {
        for (EnergyNetworkManager manager : MANAGERS.values()) {
            for (EnergyNetwork network : manager.networks) network.flushToWires();
        }
        MANAGERS.clear();
        active = false;
    }

    public void tick(ServerLevel level) {
        for (EnergyNetwork network : List.copyOf(networks)) network.tick(level);
    }

    @Nullable
    public EnergyNetwork getNetwork(BlockPos pos) {
        return byPos.get(pos);
    }

    // ── Ajout ──────────────────────────────────────────────────────────────

    public void onWirePlaced(WireBlockEntity wire) {
        BlockPos pos = wire.getBlockPos();
        if (wires.containsKey(pos)) return;
        wires.put(pos, wire);

        Set<EnergyNetwork> adjacent = new LinkedHashSet<>();
        for (WireBlockEntity neighbor : connectedNeighbors(wire)) {
            EnergyNetwork net = byPos.get(neighbor.getBlockPos());
            if (net != null) adjacent.add(net);
        }

        EnergyNetwork target;
        int energyToAdd = wire.getEnergyShare();

        if (adjacent.size() == 1) {
            target = adjacent.iterator().next();
        } else {
            target = new EnergyNetwork();
            networks.add(target);
            // Fusion de plusieurs réseaux
            for (EnergyNetwork old : adjacent) {
                energyToAdd += old.getEnergyStored();
                networks.remove(old);
                for (WireBlockEntity w : old.getWires()) {
                    target.addWire(w);
                    byPos.put(w.getBlockPos(), target);
                }
            }
        }

        target.addWire(wire);
        byPos.put(pos, target);
        target.addEnergy(energyToAdd);
    }

    // ── Retrait ────────────────────────────────────────────────────────────

    public void onWireRemoved(WireBlockEntity wire) {
        BlockPos pos = wire.getBlockPos();
        if (wires.get(pos) != wire) return;
        wires.remove(pos);

        EnergyNetwork old = byPos.remove(pos);
        if (old == null) return;

        int oldSize = old.getWires().size();
        int oldEnergy = old.getEnergyStored();
        List<WireBlockEntity> neighbors = connectedNeighbors(wire).stream()
                .filter(n -> byPos.get(n.getBlockPos()) == old)
                .toList();
        old.removeWire(wire);

        if (old.getWires().isEmpty()) {
            networks.remove(old);
            return;
        }

        // Cas courant (bout de ligne, déchargement) : pas de découpe possible
        if (neighbors.size() <= 1) {
            old.setEnergy((int) ((long) oldEnergy * (oldSize - 1) / oldSize));
            return;
        }

        // Découpe éventuelle en plusieurs réseaux, énergie répartie au prorata
        networks.remove(old);
        Set<WireBlockEntity> visited = new HashSet<>();
        for (WireBlockEntity start : neighbors) {
            if (!visited.add(start)) continue;
            EnergyNetwork part = new EnergyNetwork();
            Deque<WireBlockEntity> queue = new ArrayDeque<>();
            queue.add(start);
            while (!queue.isEmpty()) {
                WireBlockEntity current = queue.poll();
                part.addWire(current);
                byPos.put(current.getBlockPos(), part);
                for (WireBlockEntity next : connectedNeighbors(current)) {
                    if (byPos.get(next.getBlockPos()) == old && visited.add(next)) queue.add(next);
                }
            }
            networks.add(part);
            part.setEnergy((int) ((long) oldEnergy * part.getWires().size() / oldSize));
        }
    }

    // ── Utilitaires ────────────────────────────────────────────────────────

    /** Câbles voisins déjà déclarés et compatibles (règle des couleurs). */
    private List<WireBlockEntity> connectedNeighbors(WireBlockEntity wire) {
        List<WireBlockEntity> result = new ArrayList<>(6);
        if (!(wire.getBlockState().getBlock() instanceof WireBlock self)) return result;
        for (Direction dir : Direction.values()) {
            WireBlockEntity other = wires.get(wire.getBlockPos().relative(dir));
            if (other != null && other.getBlockState().getBlock() instanceof WireBlock otherBlock
                    && self.connectsToWire(otherBlock)) {
                result.add(other);
            }
        }
        return result;
    }
}
