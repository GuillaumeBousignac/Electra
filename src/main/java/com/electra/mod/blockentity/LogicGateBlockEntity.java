package com.electra.mod.blockentity;

import com.electra.mod.block.GateType;
import com.electra.mod.block.LogicGateBlock;
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
import org.jetbrains.annotations.Nullable;

import java.util.Arrays;

/**
 * Logique d'une porte (ou d'un interrupteur) au FE.
 *
 * À chaque tick :
 * 1. lit ses entrées : une entrée est active si du courant y arrive (EnergySignals#isPowered) ;
 * 2. calcule la sortie (GateType#evaluate) ;
 * 3. tant que la sortie est active, laisse le réseau aval puiser dans sa petite réserve.
 *
 * La réserve se remplit par les faces d'alimentation : les entrées pour AND / OR,
 * l'arrière pour NOT et l'interrupteur. Chaque porte ajoute un tick de délai, comme un répéteur.
 */
public class LogicGateBlockEntity extends BlockEntity {

    private enum Role { NONE, OUTPUT, SIGNAL, POWER }

    private final ModEnergyStorage buffer = new ModEnergyStorage(
            ElectraConfig.GATE_BUFFER.get(), 0, 0, this::setChanged);

    /** FE reçus ce tick par face (index Direction#get3DDataValue), pour plafonner le débit. */
    private final int[] receivedThisTick = new int[6];
    private int extractedThisTick = 0;
    private boolean outputActive = false;

    // Horloge : phase en cours et ticks écoulés dans cette phase
    private boolean clockPhaseOn = true;
    private int clockTimer = 0;

    private final IEnergyStorage[] sideStorages = new IEnergyStorage[6];

    public LogicGateBlockEntity(BlockPos pos, BlockState state) {
        super(ModBlockEntities.LOGIC_GATE_BE.get(), pos, state);
        for (Direction side : Direction.values()) sideStorages[side.get3DDataValue()] = new SideStorage(side);
    }

    // ── Rôle des faces ─────────────────────────────────────────────────────

    private GateType type() {
        return ((LogicGateBlock) getBlockState().getBlock()).getType();
    }

    private Role roleOf(Direction side) {
        Direction facing = getBlockState().getValue(LogicGateBlock.FACING);
        GateType type = type();
        if (side == facing) return Role.OUTPUT;
        if (type.hasInputs() && (side == LogicGateBlock.inputA(facing) || side == LogicGateBlock.inputB(facing))) {
            return type.inputsCarryPower() ? Role.POWER : Role.SIGNAL;
        }
        if (type.hasPowerSide() && side == LogicGateBlock.powerSide(facing)) return Role.POWER;
        return Role.NONE;
    }

    /**
     * Capacité FE exposée sur une face : null sur les faces non raccordables (les câbles ne s'y
     * connectent pas). Sans face (Jade, mesureur) : lecture seule de la réserve.
     */
    @Nullable
    public IEnergyStorage getEnergyStorage(@Nullable Direction side) {
        if (side == null) return new ReadOnly();
        return roleOf(side) == Role.NONE ? null : sideStorages[side.get3DDataValue()];
    }

    // ── Tick ───────────────────────────────────────────────────────────────

    public void tick() {
        if (level == null || level.isClientSide()) return;

        Arrays.fill(receivedThisTick, 0);
        extractedThisTick = 0;

        BlockState state = getBlockState();
        Direction facing = state.getValue(LogicGateBlock.FACING);
        GateType type = type();
        int rate = ElectraConfig.GATE_TRANSFER_RATE.get();

        // Faces d'alimentation raccordées directement à une machine (sans câble) : on puise nous-mêmes
        for (Direction side : Direction.Plane.HORIZONTAL) {
            if (roleOf(side) != Role.POWER) continue;
            int room = buffer.getMaxEnergyStored() - buffer.getEnergyStored();
            int pulled = EnergySignals.pullDirect(level, worldPosition, side, Math.min(rate, room));
            if (pulled > 0) {
                buffer.insertInternal(pulled);
                receivedThisTick[side.get3DDataValue()] += pulled;
            }
        }

        boolean a = type.hasInputs() && EnergySignals.isPowered(level, worldPosition, LogicGateBlock.inputA(facing));
        boolean b = type.hasInputs() && EnergySignals.isPowered(level, worldPosition, LogicGateBlock.inputB(facing));
        boolean logic;
        if (type == GateType.SWITCH) logic = type.evaluate(state.getValue(LogicGateBlock.ON), false);
        else if (type == GateType.CLOCK) {
            if (++clockTimer >= LogicGateBlock.clockPhaseTicks(state.getValue(LogicGateBlock.DELAY))) {
                clockTimer = 0;
                clockPhaseOn = !clockPhaseOn;
                setChanged();
            }
            logic = type.evaluate(clockPhaseOn, false);
        }
        else if (type == GateType.OR && state.getValue(LogicGateBlock.EXCLUSIVE)) logic = a ^ b;   // XOR
        else logic = type.evaluate(a, b);
        outputActive = logic;
        boolean outputLit = logic && buffer.getEnergyStored() > 0;

        // Voyants (client seulement : pas de mise à jour des voisins)
        BlockState shown = state.setValue(LogicGateBlock.INPUT_A, a)
                .setValue(LogicGateBlock.INPUT_B, b)
                .setValue(LogicGateBlock.OUTPUT, outputLit);
        if (shown != state) level.setBlock(worldPosition, shown, Block.UPDATE_CLIENTS);
    }

    // ── Capacités par face ─────────────────────────────────────────────────

    private final class SideStorage implements IEnergyStorage {
        private final Direction side;

        SideStorage(Direction side) { this.side = side; }

        @Override
        public int receiveEnergy(int maxReceive, boolean simulate) {
            if (roleOf(side) != Role.POWER) return 0;      // les entrées de signal ne consomment rien
            int index = side.get3DDataValue();
            int room = buffer.getMaxEnergyStored() - buffer.getEnergyStored();
            int accepted = Math.min(maxReceive, Math.min(room, ElectraConfig.GATE_TRANSFER_RATE.get() - receivedThisTick[index]));
            if (accepted <= 0) return 0;
            if (!simulate) {
                buffer.insertInternal(accepted);
                receivedThisTick[index] += accepted;
            }
            return accepted;
        }

        @Override
        public int extractEnergy(int maxExtract, boolean simulate) {
            if (roleOf(side) != Role.OUTPUT || !outputActive) return 0;
            int available = Math.min(buffer.getEnergyStored(), ElectraConfig.GATE_TRANSFER_RATE.get() - extractedThisTick);
            int extracted = Math.min(maxExtract, available);
            if (extracted <= 0) return 0;
            if (!simulate) {
                buffer.extractInternal(extracted);
                extractedThisTick += extracted;
            }
            return extracted;
        }

        @Override public int getEnergyStored()    { return buffer.getEnergyStored(); }
        @Override public int getMaxEnergyStored() { return buffer.getMaxEnergyStored(); }
        @Override public boolean canExtract()     { return roleOf(side) == Role.OUTPUT; }
        @Override public boolean canReceive()     { Role r = roleOf(side); return r == Role.POWER || r == Role.SIGNAL; }
    }

    private final class ReadOnly implements IEnergyStorage {
        @Override public int receiveEnergy(int maxReceive, boolean simulate) { return 0; }
        @Override public int extractEnergy(int maxExtract, boolean simulate) { return 0; }
        @Override public int getEnergyStored()    { return buffer.getEnergyStored(); }
        @Override public int getMaxEnergyStored() { return buffer.getMaxEnergyStored(); }
        @Override public boolean canExtract()     { return false; }
        @Override public boolean canReceive()     { return false; }
    }

    // ── Sauvegarde ─────────────────────────────────────────────────────────

    @Override
    protected void saveAdditional(@NotNull CompoundTag tag, HolderLookup.@NotNull Provider registries) {
        super.saveAdditional(tag, registries);
        tag.putInt("energy", buffer.getEnergyStored());
        tag.putBoolean("output", outputActive);
        tag.putBoolean("clockPhaseOn", clockPhaseOn);
        tag.putInt("clockTimer", clockTimer);
    }

    @Override
    protected void loadAdditional(@NotNull CompoundTag tag, HolderLookup.@NotNull Provider registries) {
        super.loadAdditional(tag, registries);
        buffer.setEnergy(tag.getInt("energy"));
        outputActive = tag.getBoolean("output");
        clockPhaseOn = !tag.contains("clockPhaseOn") || tag.getBoolean("clockPhaseOn");
        clockTimer = tag.getInt("clockTimer");
    }
}
