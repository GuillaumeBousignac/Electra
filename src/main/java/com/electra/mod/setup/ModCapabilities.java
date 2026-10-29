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

        event.registerBlockEntity(Capabilities.EnergyStorage.BLOCK,
                ModBlockEntities.REDSTONE_DYNAMO_BE.get(), (be, side) -> be.getEnergyStorage());

        // Portes : capacité différente selon la face (entrée, sortie, alimentation, ou rien)
        event.registerBlockEntity(Capabilities.EnergyStorage.BLOCK,
                ModBlockEntities.LOGIC_GATE_BE.get(), (be, side) -> be.getEnergyStorage(side));

        event.registerBlockEntity(Capabilities.EnergyStorage.BLOCK,
                ModBlockEntities.ELECTRIC_LAMP_BE.get(), (be, side) -> be.getEnergyStorage());

        // ── Fluides : tuyau (verser seulement), pompe (puiser seulement), réservoir (les deux), arroseur (verser seulement)
        event.registerBlockEntity(Capabilities.FluidHandler.BLOCK,
                ModBlockEntities.IRON_PIPE_BE.get(), (be, side) -> be.getExternalHandler());

        event.registerBlockEntity(Capabilities.FluidHandler.BLOCK,
                ModBlockEntities.FLUID_PUMP_BE.get(), (be, side) -> be.getFluidPort());

        event.registerBlockEntity(Capabilities.FluidHandler.BLOCK,
                ModBlockEntities.FLUID_RESERVOIR_BE.get(), (be, side) -> be.getFluidPort());

        event.registerBlockEntity(Capabilities.FluidHandler.BLOCK,
                ModBlockEntities.SPRINKLER_BE.get(), (be, side) -> be.getFluidPort());

        // La pompe se branche aussi sur le réseau électrique
        event.registerBlockEntity(Capabilities.EnergyStorage.BLOCK,
                ModBlockEntities.FLUID_PUMP_BE.get(), (be, side) -> be.getEnergyStorage());

        event.registerBlockEntity(Capabilities.ItemHandler.BLOCK,
                ModBlockEntities.ADVANCED_FURNACE_BE.get(),
                (be, side) -> side == null ? new InvWrapper(be) : new SidedInvWrapper(be, side));

        // Congélateur : énergie, objets et eau (les tuyaux ne font que verser)
        event.registerBlockEntity(Capabilities.EnergyStorage.BLOCK,
                ModBlockEntities.FREEZER_BE.get(), (be, side) -> be.getEnergyStorage());
        event.registerBlockEntity(Capabilities.FluidHandler.BLOCK,
                ModBlockEntities.FREEZER_BE.get(), (be, side) -> be.getFluidPort());
        event.registerBlockEntity(Capabilities.ItemHandler.BLOCK,
                ModBlockEntities.FREEZER_BE.get(),
                (be, side) -> side == null ? new InvWrapper(be) : new SidedInvWrapper(be, side));

        // Broyeur : énergie et objets, comme le four avancé
        event.registerBlockEntity(Capabilities.EnergyStorage.BLOCK,
                ModBlockEntities.GRINDER_BE.get(), (be, side) -> be.getEnergyStorage());
        event.registerBlockEntity(Capabilities.ItemHandler.BLOCK,
                ModBlockEntities.GRINDER_BE.get(),
                (be, side) -> side == null ? new InvWrapper(be) : new SidedInvWrapper(be, side));
    }
}
