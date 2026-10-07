package com.electra.mod.energy;

import net.neoforged.neoforge.energy.EnergyStorage;

/**
 * Stockage FE avec rappel à chaque modification et méthodes internes
 * qui ignorent les limites d'entrée/sortie (pour la machine elle-même).
 */
public class ModEnergyStorage extends EnergyStorage {

    private final Runnable onChange;

    public ModEnergyStorage(int capacity, int maxReceive, int maxExtract, Runnable onChange) {
        super(capacity, maxReceive, maxExtract, 0);
        this.onChange = onChange;
    }

    @Override
    public int receiveEnergy(int toReceive, boolean simulate) {
        int received = super.receiveEnergy(toReceive, simulate);
        if (received > 0 && !simulate) onChange.run();
        return received;
    }

    @Override
    public int extractEnergy(int toExtract, boolean simulate) {
        int extracted = super.extractEnergy(toExtract, simulate);
        if (extracted > 0 && !simulate) onChange.run();
        return extracted;
    }

    /** Ajout interne, sans limite d'entrée (ex. : éclair capté). */
    public int insertInternal(int amount) {
        int inserted = Math.min(capacity - energy, Math.max(0, amount));
        if (inserted > 0) {
            energy += inserted;
            onChange.run();
        }
        return inserted;
    }

    /** Retrait interne, sans limite de sortie (ex. : consommation d'une recette). */
    public int extractInternal(int amount) {
        int extracted = Math.min(energy, Math.max(0, amount));
        if (extracted > 0) {
            energy -= extracted;
            onChange.run();
        }
        return extracted;
    }

    /** Utilisé au chargement : ne déclenche pas le rappel. */
    public void setEnergy(int value) {
        energy = Math.max(0, Math.min(capacity, value));
    }
}
