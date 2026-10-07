package com.electra.mod.network;

import com.electra.mod.block.WireBlock;
import com.electra.mod.blockentity.WireBlockEntity;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.server.level.ServerLevel;
import net.neoforged.neoforge.capabilities.BlockCapabilityCache;
import net.neoforged.neoforge.capabilities.Capabilities;
import net.neoforged.neoforge.energy.IEnergyStorage;

import java.util.*;

/**
 * Un ensemble de câbles connectés, partageant une seule réserve d'énergie.
 * Les points de raccordement (machines voisines) sont trouvés via les capacités FE standard,
 * donc n'importe quelle machine compatible FE fonctionne, Electra ou non.
 */
public class EnergyNetwork {

    public static final int TRANSFER_RATE = 1_000; // FE/tick par machine raccordée
    private static final int SYNC_INTERVAL = 20;   // part de chaque câble mise à jour chaque seconde

    private final Set<WireBlockEntity> wires = new LinkedHashSet<>();
    private final List<BlockCapabilityCache<IEnergyStorage, Direction>> endpoints = new ArrayList<>();
    private boolean endpointsDirty = true;

    private int energy = 0;
    private int syncTimer = 0;
    private int roundRobin = 0;

    // ── Composition ────────────────────────────────────────────────────────

    void addWire(WireBlockEntity wire) {
        wires.add(wire);
        endpointsDirty = true;
    }

    void removeWire(WireBlockEntity wire) {
        wires.remove(wire);
        endpointsDirty = true;
        energy = Math.min(energy, getCapacity());
    }

    public Set<WireBlockEntity> getWires() { return wires; }

    // ── Énergie ────────────────────────────────────────────────────────────

    public int getCapacity()     { return wires.size() * WireBlockEntity.CAPACITY; }
    public int getEnergyStored() { return energy; }
    public int getShare()        { return wires.isEmpty() ? 0 : energy / wires.size(); }

    void setEnergy(int value) {
        energy = Math.max(0, Math.min(getCapacity(), value));
    }

    void addEnergy(int amount) {
        setEnergy(energy + Math.max(0, amount));
    }

    /** Injection depuis l'extérieur (autres mods qui poussent dans un câble). */
    public int receive(int maxReceive, boolean simulate) {
        int accepted = Math.min(maxReceive, getCapacity() - energy);
        if (accepted <= 0) return 0;
        if (!simulate) energy += accepted;
        return accepted;
    }

    /** Recopie la part de chaque câble pour la sauvegarde et l'affichage. */
    void flushToWires() {
        int share = getShare();
        for (WireBlockEntity wire : wires) wire.updateShare(share);
    }

    // ── Tick ───────────────────────────────────────────────────────────────

    void tick(ServerLevel level) {
        if (wires.isEmpty()) return;
        if (endpointsDirty) rebuildEndpoints(level);

        int capacity = getCapacity();
        Set<IEnergyStorage> seen = Collections.newSetFromMap(new IdentityHashMap<>());
        List<IEnergyStorage> receivers = new ArrayList<>();

        for (BlockCapabilityCache<IEnergyStorage, Direction> cache : endpoints) {
            BlockPos pos = cache.pos();
            if (!level.isLoaded(pos)) continue;                              // ne jamais charger de tronçon
            if (level.getBlockState(pos).getBlock() instanceof WireBlock) continue; // autre réseau (couleur différente)

            IEnergyStorage storage = cache.getCapability();
            if (storage == null || !seen.add(storage)) continue;             // une machine touchée deux fois = une seule fois

            if (storage.canReceive()) {
                receivers.add(storage);
            } else if (storage.canExtract() && energy < capacity) {
                // Source pure (générateur) : on aspire
                energy += storage.extractEnergy(Math.min(TRANSFER_RATE, capacity - energy), false);
            }
        }

        // Distribution équitable, avec point de départ tournant
        if (energy > 0 && !receivers.isEmpty()) {
            int n = receivers.size();
            int start = Math.floorMod(roundRobin++, n);
            for (int i = 0; i < n && energy > 0; i++) {
                energy -= receivers.get((start + i) % n).receiveEnergy(Math.min(TRANSFER_RATE, energy), false);
            }
        }

        if (++syncTimer >= SYNC_INTERVAL) {
            syncTimer = 0;
            flushToWires();
        }
    }

    private void rebuildEndpoints(ServerLevel level) {
        endpoints.clear();
        Set<BlockPos> members = new HashSet<>();
        for (WireBlockEntity wire : wires) members.add(wire.getBlockPos());

        for (WireBlockEntity wire : wires) {
            for (Direction dir : Direction.values()) {
                BlockPos neighbor = wire.getBlockPos().relative(dir);
                if (members.contains(neighbor)) continue;
                endpoints.add(BlockCapabilityCache.create(
                        Capabilities.EnergyStorage.BLOCK, level, neighbor, dir.getOpposite()));
            }
        }
        endpointsDirty = false;
    }
}
