package com.electra.mod.network;

import com.electra.mod.blockentity.BareWireBlockEntity;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.entity.BlockEntity;

import java.util.*;

public class EnergyNetworkManager {

    private static final EnergyNetworkManager INSTANCE = new EnergyNetworkManager();
    public static EnergyNetworkManager get() { return INSTANCE; }

    private final Map<BlockPos, EnergyNetwork> wireToNetwork = new HashMap<>();
    private final Set<EnergyNetwork> networks = new HashSet<>();

    private boolean active = true;

    public void saveWireEnergy(BareWireBlockEntity wire) {
        EnergyNetwork net = wireToNetwork.get(wire.getBlockPos());
        if (net == null || net.getWires().isEmpty()) return;
        int share = net.getEnergyStored() / net.getWires().size();
        wire.setDisplayEnergy(share);
    }

    public void onWirePlaced(BareWireBlockEntity wire, Level level) {
        Set<EnergyNetwork> adjacentNetworks = new HashSet<>();

        for (Direction dir : Direction.values()) {
            BlockEntity neighbor = level.getBlockEntity(wire.getBlockPos().relative(dir));
            if (neighbor instanceof BareWireBlockEntity adjacentWire) {
                EnergyNetwork net = wireToNetwork.get(adjacentWire.getBlockPos());
                if (net != null) adjacentNetworks.add(net);
            }
        }

        if (adjacentNetworks.isEmpty()) {
            EnergyNetwork newNet = new EnergyNetwork(List.of(wire));
            newNet.addEnergy(wire.getPersistedEnergy());
            networks.add(newNet);
            wireToNetwork.put(wire.getBlockPos(), newNet);
        } else if (adjacentNetworks.size() == 1) {
            EnergyNetwork net = adjacentNetworks.iterator().next();
            net.addEnergy(wire.getPersistedEnergy());
            net.getWires().add(wire);
            wireToNetwork.put(wire.getBlockPos(), net);
        } else {
            mergeNetworks(adjacentNetworks, wire);
        }
    }

    public void onWireRemoved(BlockPos pos, Level level) {
        EnergyNetwork oldNet = wireToNetwork.remove(pos);
        if (oldNet == null) return;

        int totalEnergyBefore = oldNet.getEnergyStored();
        int totalWiresBefore  = oldNet.getWires().size();

        networks.remove(oldNet);
        oldNet.getWires().removeIf(w -> w.getBlockPos().equals(pos));

        Set<BareWireBlockEntity> remaining = new HashSet<>(oldNet.getWires());
        Set<BareWireBlockEntity> visited   = new HashSet<>();
        List<EnergyNetwork> newNetworks    = new ArrayList<>();

        for (BareWireBlockEntity startWire : remaining) {
            if (visited.contains(startWire)) continue;
            List<BareWireBlockEntity> subNetwork = new ArrayList<>();
            floodFill(startWire, level, subNetwork, visited);
            if (!subNetwork.isEmpty()) {
                EnergyNetwork newNet = new EnergyNetwork(subNetwork);
                newNetworks.add(newNet);
                networks.add(newNet);
                for (BareWireBlockEntity w : subNetwork) {
                    wireToNetwork.put(w.getBlockPos(), newNet);
                }
            }
        }

        if (totalWiresBefore > 0 && !newNetworks.isEmpty()) {
            for (EnergyNetwork newNet : newNetworks) {
                int proportion = (totalEnergyBefore * newNet.getWires().size()) / totalWiresBefore;
                newNet.addEnergy(proportion);
            }
        }
    }

    private void mergeNetworks(Set<EnergyNetwork> toMerge, BareWireBlockEntity newWire) {
        List<BareWireBlockEntity> allWires = new ArrayList<>();
        int totalEnergy = 0;

        for (EnergyNetwork net : toMerge) {
            allWires.addAll(net.getWires());
            totalEnergy += net.getEnergyStored();
            networks.remove(net);
            for (BareWireBlockEntity w : net.getWires()) {
                wireToNetwork.remove(w.getBlockPos());
            }
        }
        allWires.add(newWire);
        totalEnergy += newWire.getPersistedEnergy();

        EnergyNetwork merged = new EnergyNetwork(allWires);
        merged.addEnergy(totalEnergy);
        networks.add(merged);
        for (BareWireBlockEntity w : allWires) {
            wireToNetwork.put(w.getBlockPos(), merged);
        }
    }

    private void floodFill(BareWireBlockEntity wire, Level level,
                           List<BareWireBlockEntity> result, Set<BareWireBlockEntity> visited) {
        if (visited.contains(wire)) return;
        visited.add(wire);
        result.add(wire);

        for (Direction dir : Direction.values()) {
            BlockEntity neighbor = level.getBlockEntity(wire.getBlockPos().relative(dir));
            if (neighbor instanceof BareWireBlockEntity adj) {
                floodFill(adj, level, result, visited);
            }
        }
    }

    public EnergyNetwork getNetwork(BlockPos pos) { return wireToNetwork.get(pos); }
    public Set<EnergyNetwork> getAllNetworks()     { return networks; }

    public void clear() {
        active = false;
        wireToNetwork.clear();
        networks.clear();
    }

    public void activate() {
        active = true;
    }

    public boolean isActive() {
        return active;
    }

    public void tickAll(Level level) {
        if (!active || networks.isEmpty()) return;

        Set<EnergyNetwork> toRemove = new HashSet<>();
        for (EnergyNetwork net : networks) {
            net.getWires().removeIf(w -> w.isRemoved() || w.getLevel() == null);
            if (net.getWires().isEmpty()) {
                toRemove.add(net);
            } else {
                net.tick(level);
            }
        }
        networks.removeAll(toRemove);
        toRemove.forEach(net ->
                wireToNetwork.entrySet().removeIf(e -> e.getValue() == net)
        );
    }
}