package com.electra.mod.config;

import net.neoforged.neoforge.common.ModConfigSpec;

/**
 * Configuration serveur d'Electra : un fichier par monde,
 * dans saves/<monde>/serverconfig/electra-server.toml.
 * Synchronisée automatiquement avec les clients en multijoueur.
 *
 * Les machines lisent leur capacité à leur chargement : après modification
 * des capacités, quitter et relancer le monde.
 */
public final class ElectraConfig {

    private ElectraConfig() {}

    public static final ModConfigSpec SPEC;

    // ── Lightning Collector ────────────────────────────────────────────────
    public static final ModConfigSpec.IntValue COLLECTOR_CAPACITY;
    public static final ModConfigSpec.IntValue COLLECTOR_ENERGY_PER_STRIKE;
    public static final ModConfigSpec.IntValue COLLECTOR_MAX_EXTRACT;

    // ── Advanced Furnace ───────────────────────────────────────────────────
    public static final ModConfigSpec.IntValue FURNACE_CAPACITY;
    public static final ModConfigSpec.IntValue FURNACE_MAX_RECEIVE;
    public static final ModConfigSpec.IntValue FURNACE_ENERGY_PER_SMELT;
    public static final ModConfigSpec.IntValue FURNACE_COOK_TIME;

    // ── Redstone Dynamo ────────────────────────────────────────────────────
    public static final ModConfigSpec.IntValue DYNAMO_GENERATION;
    public static final ModConfigSpec.IntValue DYNAMO_CAPACITY;
    public static final ModConfigSpec.IntValue DYNAMO_MAX_EXTRACT;
    public static final ModConfigSpec.IntValue DYNAMO_SPIN_UP_TICKS;
    public static final ModConfigSpec.IntValue DYNAMO_SPIN_DOWN_TICKS;

    // ── Wires ──────────────────────────────────────────────────────────────
    public static final ModConfigSpec.IntValue WIRE_CAPACITY;
    public static final ModConfigSpec.DoubleValue BARE_WIRE_SHOCK_DAMAGE;

    static {
        ModConfigSpec.Builder builder = new ModConfigSpec.Builder();

        builder.comment("Lightning Collector: captures lightning striking a lightning rod placed above it.")
                .push("lightningCollector");
        COLLECTOR_CAPACITY = builder
                .comment("Maximum energy stored (FE).")
                .defineInRange("capacity", 1_000_000, 1_000, 100_000_000);
        COLLECTOR_ENERGY_PER_STRIKE = builder
                .comment("Energy gained per lightning strike (FE).")
                .defineInRange("energyPerStrike", 5_000, 0, 100_000_000);
        COLLECTOR_MAX_EXTRACT = builder
                .comment("Maximum output rate (FE/tick).")
                .defineInRange("maxExtract", 1_000, 1, 1_000_000);
        builder.pop();

        builder.comment("Advanced Furnace: electric smelting and dual-input alloying.",
                        "Alloying energy costs are defined in each recipe (datapack), not here.")
                .push("advancedFurnace");
        FURNACE_CAPACITY = builder
                .comment("Maximum energy stored (FE).")
                .defineInRange("capacity", 50_000, 1_000, 10_000_000);
        FURNACE_MAX_RECEIVE = builder
                .comment("Maximum input rate (FE/tick).")
                .defineInRange("maxReceive", 1_000, 1, 1_000_000);
        FURNACE_ENERGY_PER_SMELT = builder
                .comment("Energy cost of a regular smelting operation (FE).")
                .defineInRange("energyPerSmelt", 100, 0, 1_000_000);
        FURNACE_COOK_TIME = builder
                .comment("Duration of one operation, smelting or alloying (ticks, 20 = 1 second).")
                .defineInRange("cookTime", 25, 1, 1_200);
        builder.pop();

        builder.comment("Redstone Dynamo: multiblock generator. A Red Iron block spins above the Dynamo,",
                        "inside a 3x3 ring of redstone blocks, while the Red Iron block receives a redstone signal.")
                .push("redstoneDynamo");
        DYNAMO_GENERATION = builder
                .comment("Energy produced at full speed (FE/tick).")
                .defineInRange("generation", 40, 1, 100_000);
        DYNAMO_CAPACITY = builder
                .comment("Maximum energy stored (FE).")
                .defineInRange("capacity", 20_000, 1_000, 10_000_000);
        DYNAMO_MAX_EXTRACT = builder
                .comment("Maximum output rate (FE/tick).")
                .defineInRange("maxExtract", 1_000, 1, 1_000_000);
        DYNAMO_SPIN_UP_TICKS = builder
                .comment("Time to reach full speed once powered (ticks, 20 = 1 second).")
                .defineInRange("spinUpTicks", 200, 1, 12_000);
        DYNAMO_SPIN_DOWN_TICKS = builder
                .comment("Time to come to a stop once the signal is removed (ticks). It keeps producing while slowing down.")
                .defineInRange("spinDownTicks", 100, 1, 12_000);
        builder.pop();

        builder.comment("Wires: energy is shared across each connected network.").push("wires");
        WIRE_CAPACITY = builder
                .comment("Energy stored per wire block; a network holds this value times its number of wires (FE).")
                .defineInRange("capacityPerWire", 10_000, 100, 1_000_000);
        BARE_WIRE_SHOCK_DAMAGE = builder
                .comment("Damage dealt by a powered bare wire to living entities inside it (2 = one heart). 0 disables it.")
                .defineInRange("bareWireShockDamage", 4.0, 0.0, 100.0);
        builder.pop();

        SPEC = builder.build();
    }
}
