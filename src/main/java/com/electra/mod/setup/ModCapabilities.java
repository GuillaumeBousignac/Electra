package com.electra.mod.setup;

import net.neoforged.neoforge.capabilities.Capabilities;
import net.neoforged.neoforge.capabilities.RegisterCapabilitiesEvent;
import net.neoforged.neoforge.items.wrapper.InvWrapper;
import net.neoforged.neoforge.items.wrapper.SidedInvWrapper;

/**
 * Expose l'énergie (FE) et les inventaires via le système standard de NeoForge.
 * C'est ce qui rend Electra compatible avec les autres mods (Mekanism, Create, etc.).
 */
public final class ModCapabilities {

    private ModCapabilities() {}

    public static void register(RegisterCapabilitiesEvent event) {
        event.registerBlockEntity(Capabilities.EnergyStorage.BLOCK,
                ModBlockEntities.WIRE_BE.get(), (be, side) -> be.getExternalStorage());

        event.registerBlockEntity(Capabilities.EnergyStorage.BLOCK,
                ModBlockEntities.LIGHTNING_COLLECTOR_BE.get(), (be, side) -> be.getEnergyStorage());

        event.registerBlockEntity(Capabilities.EnergyStorage.BLOCK,
                ModBlockEntities.ADVANCED_FURNACE_BE.get(), (be, side) -> be.getEnergyStorage());

        event.registerBlockEntity(Capabilities.ItemHandler.BLOCK,
                ModBlockEntities.ADVANCED_FURNACE_BE.get(),
                (be, side) -> side == null ? new InvWrapper(be) : new SidedInvWrapper(be, side));
    }
}
