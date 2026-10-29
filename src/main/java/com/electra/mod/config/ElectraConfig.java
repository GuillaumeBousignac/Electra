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

    // ── Grinder ────────────────────────────────────────────────────────────
    public static final ModConfigSpec.IntValue GRINDER_CAPACITY;
    public static final ModConfigSpec.IntValue GRINDER_MAX_RECEIVE;
    public static final ModConfigSpec.IntValue GRINDER_TIME;

    // ── Freezer ────────────────────────────────────────────────────────────
    public static final ModConfigSpec.IntValue FREEZER_CAPACITY;
    public static final ModConfigSpec.IntValue FREEZER_MAX_RECEIVE;
    public static final ModConfigSpec.IntValue FREEZER_TANK;
    public static final ModConfigSpec.IntValue FREEZER_TIME;

    // ── Redstone Dynamo ────────────────────────────────────────────────────
    public static final ModConfigSpec.IntValue DYNAMO_GENERATION;
    public static final ModConfigSpec.IntValue DYNAMO_CAPACITY;
    public static final ModConfigSpec.IntValue DYNAMO_MAX_EXTRACT;
    public static final ModConfigSpec.IntValue DYNAMO_SPIN_UP_TICKS;
    public static final ModConfigSpec.IntValue DYNAMO_SPIN_DOWN_TICKS;

    // ── Logic gates and lamps ─────────────────────────────────────────────
    public static final ModConfigSpec.IntValue GATE_TRANSFER_RATE;
    public static final ModConfigSpec.IntValue GATE_BUFFER;
    public static final ModConfigSpec.IntValue LAMP_CONSUMPTION;

    // ── Wires ──────────────────────────────────────────────────────────────
    public static final ModConfigSpec.IntValue WIRE_CAPACITY;
    public static final ModConfigSpec.IntValue WIRE_DISCHARGE_TICKS;
    public static final ModConfigSpec.DoubleValue BARE_WIRE_SHOCK_DAMAGE;

    // ── Fluids: pipes, pump, reservoir, sprinkler ──────────────────────────
    public static final ModConfigSpec.IntValue PIPE_CAPACITY;
    public static final ModConfigSpec.IntValue PIPE_TRANSFER_RATE;
    public static final ModConfigSpec.IntValue PUMP_RANGE;
    public static final ModConfigSpec.IntValue PUMP_INTERVAL_TICKS;
    public static final ModConfigSpec.IntValue PUMP_ENERGY_PER_BUCKET;
    public static final ModConfigSpec.IntValue PUMP_ENERGY_CAPACITY;
    public static final ModConfigSpec.IntValue PUMP_MAX_RECEIVE;
    public static final ModConfigSpec.IntValue PUMP_TANK_CAPACITY;
    public static final ModConfigSpec.IntValue RESERVOIR_CAPACITY;
    public static final ModConfigSpec.IntValue SPRINKLER_TANK;
    public static final ModConfigSpec.IntValue SPRINKLER_RADIUS;
    public static final ModConfigSpec.IntValue SPRINKLER_DEPTH;
    public static final ModConfigSpec.IntValue SPRINKLER_MB_PER_SECOND;
    public static final ModConfigSpec.DoubleValue SPRINKLER_GROWTH_CHANCE;
    public static final ModConfigSpec.DoubleValue SPRINKLER_LAVA_DAMAGE;

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

        builder.comment("Grinder: electric crushing. Energy costs are defined in each recipe (datapack), not here.")
                .push("grinder");
        GRINDER_CAPACITY = builder
                .comment("Maximum energy stored (FE).")
                .defineInRange("capacity", 50_000, 1_000, 10_000_000);
        GRINDER_MAX_RECEIVE = builder
                .comment("Maximum input rate (FE/tick).")
                .defineInRange("maxReceive", 1_000, 1, 1_000_000);
        GRINDER_TIME = builder
                .comment("Duration of one grinding operation (ticks, 20 = 1 second).")
                .defineInRange("grindTime", 40, 1, 1_200);
        builder.pop();

        builder.comment("Freezer: turns water (pipes or bucket) into ice, packed ice, blue ice and snow.",
                        "Water amounts and energy costs are defined in each recipe (datapack), not here.")
                .push("freezer");
        FREEZER_CAPACITY = builder
                .comment("Maximum energy stored (FE).")
                .defineInRange("capacity", 50_000, 1_000, 10_000_000);
        FREEZER_MAX_RECEIVE = builder
                .comment("Maximum input rate (FE/tick).")
                .defineInRange("maxReceive", 1_000, 1, 1_000_000);
        FREEZER_TANK = builder
                .comment("Water tank capacity (mB).")
                .defineInRange("tankCapacity", 4_000, 1_000, 1_000_000);
        FREEZER_TIME = builder
                .comment("Duration of one freezing operation (ticks, 20 = 1 second).")
                .defineInRange("freezeTime", 60, 1, 1_200);
        builder.pop();

        builder.comment("Redstone Dynamo: multiblock generator. A Red Iron block held by the Dynamo's arm orbits",
                        "around it, inside a 5x5 ring of redstone blocks (corners optional), while the Dynamo receives",
                        "a redstone signal.")
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

        builder.comment("Logic gates (AND, OR, NOT), Energy Switch and Electric Lamp.",
                        "An input counts as active while its cable holds energy or is being fed;",
                        "cables no longer fed by any source discharge quickly (see wires.dischargeTicks).")
                .push("logic");
        GATE_TRANSFER_RATE = builder
                .comment("Maximum energy a gate or switch can pass through, per face (FE/tick).")
                .defineInRange("gateTransferRate", 200, 1, 100_000);
        GATE_BUFFER = builder
                .comment("Internal buffer of a gate or switch (FE).")
                .defineInRange("gateBuffer", 1_000, 10, 1_000_000);
        LAMP_CONSUMPTION = builder
                .comment("Energy used by a lit Electric Lamp (FE/tick). 0 = free.")
                .defineInRange("lampConsumption", 1, 0, 20);
        builder.pop();

        builder.comment("Wires: energy is shared across each connected network.").push("wires");
        WIRE_CAPACITY = builder
                .comment("Energy stored per wire block; a network holds this value times its number of wires (FE).")
                .defineInRange("capacityPerWire", 1_000, 100, 1_000_000);
        WIRE_DISCHARGE_TICKS = builder
                .comment("Time for a network that no source feeds anymore to fully discharge (ticks, 20 = 1 second).",
                         "Keeps logic gates and lamps responsive. 0 = cables never discharge.")
                .defineInRange("dischargeTicks", 20, 0, 72_000);
        BARE_WIRE_SHOCK_DAMAGE = builder
                .comment("Damage dealt by a powered bare wire to living entities inside it (2 = one heart). 0 disables it.")
                .defineInRange("bareWireShockDamage", 4.0, 0.0, 100.0);
        builder.pop();

        builder.comment("Iron pipes: fluids (in mB, 1000 mB = one bucket) are shared across each connected network.",
                        "A network holds one fluid at a time.")
                .push("pipes");
        PIPE_CAPACITY = builder
                .comment("Fluid stored per pipe block; a network holds this value times its number of pipes (mB).")
                .defineInRange("capacityPerPipe", 500, 50, 1_000_000);
        PIPE_TRANSFER_RATE = builder
                .comment("Maximum flow per connected machine (mB/tick).")
                .defineInRange("transferRate", 200, 1, 100_000);
        builder.pop();

        builder.comment("Fluid Pump: powered by FE, sucks the first fluid source found below it.")
                .push("fluidPump");
        PUMP_RANGE = builder
                .comment("Vertical range below the pump (blocks).")
                .defineInRange("range", 6, 1, 32);
        PUMP_INTERVAL_TICKS = builder
                .comment("Time to pump one source block, i.e. 1000 mB (ticks). 20 = 1000 mB per second.")
                .defineInRange("intervalTicks", 20, 1, 1_200);
        PUMP_ENERGY_PER_BUCKET = builder
                .comment("Energy used per source block pumped (FE). 800 FE per second = 40 FE/tick, one Redstone Dynamo.")
                .defineInRange("energyPerBucket", 800, 0, 1_000_000);
        PUMP_ENERGY_CAPACITY = builder
                .comment("Maximum energy stored (FE).")
                .defineInRange("energyCapacity", 16_000, 1_000, 10_000_000);
        PUMP_MAX_RECEIVE = builder
                .comment("Maximum input rate (FE/tick).")
                .defineInRange("maxReceive", 1_000, 1, 1_000_000);
        PUMP_TANK_CAPACITY = builder
                .comment("Internal tank (mB).")
                .defineInRange("tankCapacity", 4_000, 1_000, 1_000_000);
        builder.pop();

        builder.comment("Fluid Reservoir: glass-fronted tank.").push("fluidReservoir");
        RESERVOIR_CAPACITY = builder
                .comment("Maximum fluid stored (mB).")
                .defineInRange("capacity", 16_000, 1_000, 100_000_000);
        builder.pop();

        builder.comment("Sprinkler: sprays the area below it with water (crops, farmland) or lava (nether wart).",
                        "Water: farmland stays fully hydrated; crops, stems, saplings and sweet berry bushes get extra random ticks.",
                        "Lava: nether wart gets extra random ticks; living creatures below are set on fire (blocks never are).")
                .push("sprinkler");
        SPRINKLER_TANK = builder
                .comment("Internal tank (mB).")
                .defineInRange("tankCapacity", 1_000, 50, 100_000);
        SPRINKLER_RADIUS = builder
                .comment("Horizontal radius of the sprayed area (blocks): 4 = a 9x9 square.")
                .defineInRange("radius", 4, 1, 16);
        SPRINKLER_DEPTH = builder
                .comment("How many blocks below the sprinkler are sprayed.")
                .defineInRange("depth", 4, 1, 16);
        SPRINKLER_MB_PER_SECOND = builder
                .comment("Fluid used per second while spraying (mB).")
                .defineInRange("mbPerSecond", 20, 1, 10_000);
        SPRINKLER_GROWTH_CHANCE = builder
                .comment("Chance per second that each plant in the area gets an extra random tick (vanilla growth rules still apply).")
                .defineInRange("growthChance", 0.2, 0.0, 1.0);
        SPRINKLER_LAVA_DAMAGE = builder
                .comment("Damage dealt each second by lava drops to creatures below, on top of setting them on fire. 0 = fire only.")
                .defineInRange("lavaDamage", 2.0, 0.0, 100.0);
        builder.pop();

        SPEC = builder.build();
    }
}
