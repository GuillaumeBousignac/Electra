package com.electra.mod.network;

import com.electra.mod.blockentity.AdvancedFurnaceBlockEntity;
import com.electra.mod.blockentity.BareWireBlockEntity;
import com.electra.mod.blockentity.LightningCollectorBlockEntity;
import com.electra.mod.blockentity.RedstoneConverterBlockEntity;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.entity.BlockEntity;

import java.util.*;

public class EnergyNetwork {

    public static final int TRANSFER_RATE = 1_000;

    private int energyStored = 0;
    private final int capacity;
    private int syncTimer = 0;

    private final Set<BareWireBlockEntity> wires = new LinkedHashSet<>();

    public EnergyNetwork(Collection<BareWireBlockEntity> wires) {
        this.wires.addAll(wires);
        this.capacity = wires.size() * BareWireBlockEntity.CAPACITY;
    }

    public void tick(Level level) {
        if (level.isClientSide()) return;

        // 1. Aspire depuis les LightningCollectors
        for (BareWireBlockEntity wire : new ArrayList<>(wires)) {
            for (Direction dir : Direction.values()) {
                BlockEntity neighbor = level.getBlockEntity(wire.getBlockPos().relative(dir));
                if (neighbor instanceof LightningCollectorBlockEntity collector) {
                    int canReceive = Math.min(TRANSFER_RATE, capacity - energyStored);
                    if (canReceive > 0) {
                        int extracted = collector.getEnergyStorage().extractEnergy(canReceive, false);
                        if (extracted > 0) {
                            energyStored += extracted;
                            // Sync LC uniquement si énergie a changé
                            if (level instanceof ServerLevel serverLevel) {
                                serverLevel.sendBlockUpdated(
                                        collector.getBlockPos(),
                                        collector.getBlockState(),
                                        collector.getBlockState(), 3);
                            }
                        }
                    }
                }
            }
        }

        // 2. Notifie les RedstoneConverters (ils lisent l'énergie du câble eux-mêmes)
        for (BareWireBlockEntity wire : new ArrayList<>(wires)) {
            for (Direction dir : Direction.values()) {
                BlockEntity neighbor = level.getBlockEntity(wire.getBlockPos().relative(dir));
                if (neighbor instanceof RedstoneConverterBlockEntity converter) {
                    converter.tick();
                }
            }
        }

        // 3. Pousse vers les AdvancedFurnace adjacents
        for (BareWireBlockEntity wire : new ArrayList<>(wires)) {
            for (Direction dir : Direction.values()) {
                BlockEntity neighbor = level.getBlockEntity(wire.getBlockPos().relative(dir));
                if (neighbor instanceof AdvancedFurnaceBlockEntity furnace) {
                    int toSend = Math.min(TRANSFER_RATE, energyStored);
                    if (toSend > 0) {
                        int accepted = furnace.getEnergyStorage().receiveEnergy(toSend, false);
                        energyStored -= accepted;
                    }
                }
            }
        }

        // 3. Sync câbles une fois par seconde
        syncTimer++;
        if (syncTimer >= 20) {
            syncTimer = 0;
            int share = wires.isEmpty() ? 0 : energyStored / wires.size();
            for (BareWireBlockEntity wire : wires) {
                wire.setDisplayEnergy(share);
            }
        }
    }

    public int getEnergyStored() { return energyStored; }
    public int getCapacity()     { return capacity; }
    public Set<BareWireBlockEntity> getWires() { return wires; }

    public void addEnergy(int amount) {
        energyStored = Math.min(energyStored + amount, capacity);
    }
}