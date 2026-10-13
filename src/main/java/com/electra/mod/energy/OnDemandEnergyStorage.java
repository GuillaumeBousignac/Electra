package com.electra.mod.energy;

/**
 * Réserve d'une source « à la demande » : le réseau de câbles n'y puise que ce que les machines
 * raccordées consomment pendant le tick, au lieu de remplir ses câbles.
 *
 * Utilisée par le Lightning Collector : l'énergie d'un éclair reste dans le collecteur tant que
 * personne n'en a besoin, et n'est donc pas perdue par la décharge des câbles.
 */
public class OnDemandEnergyStorage extends ModEnergyStorage {

    public OnDemandEnergyStorage(int capacity, int maxReceive, int maxExtract, Runnable onChange) {
        super(capacity, maxReceive, maxExtract, onChange);
    }
}
