package com.electra.mod.setup;

import com.electra.mod.Electra;
import com.electra.mod.blockentity.AdvancedFurnaceBlockEntity;
import com.electra.mod.blockentity.ConcreteMixerBlockEntity;
import com.electra.mod.blockentity.DebarkerBlockEntity;
import com.electra.mod.blockentity.DyerBlockEntity;
import com.electra.mod.blockentity.OxidationStationBlockEntity;
import com.electra.mod.blockentity.FreezerBlockEntity;
import com.electra.mod.blockentity.GrinderBlockEntity;
import com.electra.mod.blockentity.ElectricLampBlockEntity;
import com.electra.mod.blockentity.FluidPumpBlockEntity;
import com.electra.mod.blockentity.FluidReservoirBlockEntity;
import com.electra.mod.blockentity.IronPipeBlockEntity;
import com.electra.mod.blockentity.LightningCollectorBlockEntity;
import com.electra.mod.blockentity.LogicGateBlockEntity;
import com.electra.mod.blockentity.RedstoneConverterBlockEntity;
import com.electra.mod.blockentity.RedstoneDynamoBlockEntity;
import com.electra.mod.blockentity.SprinklerBlockEntity;
import com.electra.mod.blockentity.WireBlockEntity;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.entity.BlockEntityType;
import net.neoforged.neoforge.registries.DeferredHolder;
import net.neoforged.neoforge.registries.DeferredRegister;

import java.util.ArrayList;
import java.util.List;

public class ModBlockEntities {

    public static final DeferredRegister<BlockEntityType<?>> BLOCK_ENTITIES =
            DeferredRegister.create(BuiltInRegistries.BLOCK_ENTITY_TYPE, Electra.MODID);

    public static final DeferredHolder<BlockEntityType<?>, BlockEntityType<LightningCollectorBlockEntity>> LIGHTNING_COLLECTOR_BE =
            BLOCK_ENTITIES.register("lightning_collector_be", () -> BlockEntityType.Builder
                    .of(LightningCollectorBlockEntity::new, ModBlocks.LIGHTNING_COLLECTOR.get())
                    .build(null));

    // Identifiant « bare_wire_be » conservé pour ne pas casser les mondes de test existants.
    // Un seul type pour tous les câbles : nu + 16 isolés.
    public static final DeferredHolder<BlockEntityType<?>, BlockEntityType<WireBlockEntity>> WIRE_BE =
            BLOCK_ENTITIES.register("bare_wire_be", () -> {
                List<Block> blocks = new ArrayList<>();
                blocks.add(ModBlocks.BARE_WIRE.get());
                ModBlocks.INSULATED_WIRES.values().forEach(holder -> blocks.add(holder.get()));
                return BlockEntityType.Builder.of(WireBlockEntity::new, blocks.toArray(Block[]::new)).build(null);
            });

    public static final DeferredHolder<BlockEntityType<?>, BlockEntityType<RedstoneConverterBlockEntity>> REDSTONE_CONVERTER_BE =
            BLOCK_ENTITIES.register("redstone_converter_be", () -> BlockEntityType.Builder
                    .of(RedstoneConverterBlockEntity::new, ModBlocks.REDSTONE_CONVERTER.get())
                    .build(null));

    public static final DeferredHolder<BlockEntityType<?>, BlockEntityType<AdvancedFurnaceBlockEntity>> ADVANCED_FURNACE_BE =
            BLOCK_ENTITIES.register("advanced_furnace_be", () -> BlockEntityType.Builder
                    .of(AdvancedFurnaceBlockEntity::new, ModBlocks.ADVANCED_FURNACE.get())
                    .build(null));

    public static final DeferredHolder<BlockEntityType<?>, BlockEntityType<GrinderBlockEntity>> GRINDER_BE =
            BLOCK_ENTITIES.register("grinder_be", () -> BlockEntityType.Builder
                    .of(GrinderBlockEntity::new, ModBlocks.GRINDER.get())
                    .build(null));

    public static final DeferredHolder<BlockEntityType<?>, BlockEntityType<FreezerBlockEntity>> FREEZER_BE =
            BLOCK_ENTITIES.register("freezer_be", () -> BlockEntityType.Builder
                    .of(FreezerBlockEntity::new, ModBlocks.FREEZER.get())
                    .build(null));

    public static final DeferredHolder<BlockEntityType<?>, BlockEntityType<ConcreteMixerBlockEntity>> CONCRETE_MIXER_BE =
            BLOCK_ENTITIES.register("concrete_mixer_be", () -> BlockEntityType.Builder
                    .of(ConcreteMixerBlockEntity::new, ModBlocks.CONCRETE_MIXER.get())
                    .build(null));

    public static final DeferredHolder<BlockEntityType<?>, BlockEntityType<OxidationStationBlockEntity>> OXIDATION_STATION_BE =
            BLOCK_ENTITIES.register("oxidation_station_be", () -> BlockEntityType.Builder
                    .of(OxidationStationBlockEntity::new, ModBlocks.OXIDATION_STATION.get())
                    .build(null));

    public static final DeferredHolder<BlockEntityType<?>, BlockEntityType<DyerBlockEntity>> DYER_BE =
            BLOCK_ENTITIES.register("dyer_be", () -> BlockEntityType.Builder
                    .of(DyerBlockEntity::new, ModBlocks.DYER.get())
                    .build(null));

    public static final DeferredHolder<BlockEntityType<?>, BlockEntityType<DebarkerBlockEntity>> DEBARKER_BE =
            BLOCK_ENTITIES.register("debarker_be", () -> BlockEntityType.Builder
                    .of(DebarkerBlockEntity::new, ModBlocks.DEBARKER.get())
                    .build(null));

    public static final DeferredHolder<BlockEntityType<?>, BlockEntityType<RedstoneDynamoBlockEntity>> REDSTONE_DYNAMO_BE =
            BLOCK_ENTITIES.register("redstone_dynamo_be", () -> BlockEntityType.Builder
                    .of(RedstoneDynamoBlockEntity::new, ModBlocks.REDSTONE_DYNAMO.get())
                    .build(null));

    // Un seul type pour les 4 composants logiques (AND, OR, NOT, interrupteur)
    public static final DeferredHolder<BlockEntityType<?>, BlockEntityType<LogicGateBlockEntity>> LOGIC_GATE_BE =
            BLOCK_ENTITIES.register("logic_gate_be", () -> BlockEntityType.Builder
                    .of(LogicGateBlockEntity::new, ModBlocks.LOGIC_GATES.values().stream()
                            .map(holder -> (Block) holder.get()).toArray(Block[]::new))
                    .build(null));

    // ── Fluides (0.3) ─────────────────────────────────────────────────────
    public static final DeferredHolder<BlockEntityType<?>, BlockEntityType<IronPipeBlockEntity>> IRON_PIPE_BE =
            BLOCK_ENTITIES.register("iron_pipe_be", () -> BlockEntityType.Builder
                    .of(IronPipeBlockEntity::new, ModBlocks.IRON_PIPE.get())
                    .build(null));

    public static final DeferredHolder<BlockEntityType<?>, BlockEntityType<FluidPumpBlockEntity>> FLUID_PUMP_BE =
            BLOCK_ENTITIES.register("fluid_pump_be", () -> BlockEntityType.Builder
                    .of(FluidPumpBlockEntity::new, ModBlocks.FLUID_PUMP.get())
                    .build(null));

    public static final DeferredHolder<BlockEntityType<?>, BlockEntityType<FluidReservoirBlockEntity>> FLUID_RESERVOIR_BE =
            BLOCK_ENTITIES.register("fluid_reservoir_be", () -> BlockEntityType.Builder
                    .of(FluidReservoirBlockEntity::new, ModBlocks.FLUID_RESERVOIR.get())
                    .build(null));

    public static final DeferredHolder<BlockEntityType<?>, BlockEntityType<SprinklerBlockEntity>> SPRINKLER_BE =
            BLOCK_ENTITIES.register("sprinkler_be", () -> BlockEntityType.Builder
                    .of(SprinklerBlockEntity::new, ModBlocks.SPRINKLER.get())
                    .build(null));

    public static final DeferredHolder<BlockEntityType<?>, BlockEntityType<ElectricLampBlockEntity>> ELECTRIC_LAMP_BE =
            BLOCK_ENTITIES.register("electric_lamp_be", () -> BlockEntityType.Builder
                    .of(ElectricLampBlockEntity::new, ModBlocks.ELECTRIC_LAMP.get())
                    .build(null));
}
