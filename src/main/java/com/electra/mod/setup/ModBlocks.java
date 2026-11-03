package com.electra.mod.setup;

import com.electra.mod.Electra;
import com.electra.mod.block.AdvancedFurnaceBlock;
import com.electra.mod.block.ConcreteMixerBlock;
import com.electra.mod.block.OxidationStationBlock;
import com.electra.mod.block.DebarkerBlock;
import com.electra.mod.block.DyerBlock;
import com.electra.mod.block.FreezerBlock;
import com.electra.mod.block.GrinderBlock;
import com.electra.mod.block.BareWireBlock;
import com.electra.mod.block.ElectricLampBlock;
import com.electra.mod.block.FluidPumpBlock;
import com.electra.mod.block.FluidReservoirBlock;
import com.electra.mod.block.GateType;
import com.electra.mod.block.InsulatedWireBlock;
import com.electra.mod.block.IronPipeBlock;
import com.electra.mod.block.LightningCollectorBlock;
import com.electra.mod.block.LogicGateBlock;
import com.electra.mod.block.RedstoneConverterBlock;
import com.electra.mod.block.RedstoneDynamoBlock;
import com.electra.mod.block.SprinklerBlock;
import net.minecraft.world.item.BlockItem;
import net.minecraft.world.item.DyeColor;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.SoundType;
import net.minecraft.world.level.block.state.BlockBehaviour;
import net.minecraft.world.level.material.MapColor;
import net.neoforged.neoforge.registries.DeferredBlock;
import net.neoforged.neoforge.registries.DeferredItem;
import net.neoforged.neoforge.registries.DeferredRegister;

import java.util.EnumMap;
import java.util.Map;

public class ModBlocks {

    public static final DeferredRegister.Blocks BLOCKS = DeferredRegister.createBlocks(Electra.MODID);
    public static final DeferredRegister.Items ITEMS = DeferredRegister.createItems(Electra.MODID);

    public static final DeferredBlock<LightningCollectorBlock> LIGHTNING_COLLECTOR =
            BLOCKS.registerBlock("lightning_collector", LightningCollectorBlock::new,
                    BlockBehaviour.Properties.of()
                            .mapColor(MapColor.COLOR_ORANGE)
                            .sound(SoundType.COPPER)
                            .noOcclusion()
                            .isViewBlocking((state, level, pos) -> false)
                            .strength(3.0f, 6.0f)
                            .lightLevel(state -> state.getValue(LightningCollectorBlock.CHARGED) ? 10 : 0)
                            .requiresCorrectToolForDrops());
    public static final DeferredItem<BlockItem> LIGHTNING_COLLECTOR_ITEM =
            ITEMS.registerSimpleBlockItem("lightning_collector", LIGHTNING_COLLECTOR);

    // Câble nu
    public static final DeferredBlock<BareWireBlock> BARE_WIRE =
            BLOCKS.registerBlock("bare_wire", BareWireBlock::new,
                    BlockBehaviour.Properties.of()
                            .mapColor(MapColor.COLOR_ORANGE)
                            .sound(SoundType.COPPER)
                            .strength(1.0f, 2.0f)
                            .noOcclusion());
    public static final DeferredItem<BlockItem> BARE_WIRE_ITEM =
            ITEMS.registerSimpleBlockItem("bare_wire", BARE_WIRE);

    // Câbles isolés - 16 couleurs
    public static final Map<DyeColor, DeferredBlock<InsulatedWireBlock>> INSULATED_WIRES = new EnumMap<>(DyeColor.class);
    public static final Map<DyeColor, DeferredItem<BlockItem>> INSULATED_WIRE_ITEMS = new EnumMap<>(DyeColor.class);

    static {
        for (DyeColor color : DyeColor.values()) {
            String name = color.getName() + "_insulated_wire";
            DeferredBlock<InsulatedWireBlock> block = BLOCKS.registerBlock(name,
                    properties -> new InsulatedWireBlock(color, properties),
                    BlockBehaviour.Properties.of()
                            .mapColor(color.getMapColor())
                            .sound(SoundType.WOOL)
                            .strength(1.0f, 2.0f)
                            .noOcclusion());
            INSULATED_WIRES.put(color, block);
            INSULATED_WIRE_ITEMS.put(color, ITEMS.registerSimpleBlockItem(name, block));
        }
    }

    public static final DeferredBlock<Block> ROSE_GOLD_BLOCK =
            BLOCKS.registerSimpleBlock("rose_gold_block",
                    BlockBehaviour.Properties.of()
                            .mapColor(MapColor.COLOR_PINK)
                            .strength(4.0f, 6.0f)
                            .sound(SoundType.METAL)
                            .requiresCorrectToolForDrops());
    public static final DeferredItem<BlockItem> ROSE_GOLD_BLOCK_ITEM =
            ITEMS.registerSimpleBlockItem("rose_gold_block", ROSE_GOLD_BLOCK);

    public static final DeferredBlock<Block> RED_IRON_BLOCK =
            BLOCKS.registerSimpleBlock("red_iron_block",
                    BlockBehaviour.Properties.of()
                            .mapColor(MapColor.COLOR_RED)
                            .strength(5.0f, 6.0f)
                            .sound(SoundType.METAL)
                            .requiresCorrectToolForDrops());
    public static final DeferredItem<BlockItem> RED_IRON_BLOCK_ITEM =
            ITEMS.registerSimpleBlockItem("red_iron_block", RED_IRON_BLOCK);

    /** Fer rouge oxydé : un seul stade, pensé pour l'usure future de la Redstone Dynamo. */
    public static final DeferredBlock<Block> RUSTED_RED_IRON_BLOCK =
            BLOCKS.registerSimpleBlock("rusted_red_iron_block",
                    BlockBehaviour.Properties.of()
                            .mapColor(MapColor.TERRACOTTA_BROWN)
                            .strength(5.0f, 6.0f)
                            .sound(SoundType.METAL)
                            .requiresCorrectToolForDrops());
    public static final DeferredItem<BlockItem> RUSTED_RED_IRON_BLOCK_ITEM =
            ITEMS.registerSimpleBlockItem("rusted_red_iron_block", RUSTED_RED_IRON_BLOCK);

    public static final DeferredBlock<RedstoneConverterBlock> REDSTONE_CONVERTER =
            BLOCKS.registerBlock("redstone_converter", RedstoneConverterBlock::new,
                    BlockBehaviour.Properties.of()
                            .mapColor(MapColor.COLOR_ORANGE)
                            .sound(SoundType.COPPER)
                            .strength(2.0f, 6.0f)
                            .lightLevel(state -> state.getValue(RedstoneConverterBlock.POWERED) ? 7 : 0)
                            .requiresCorrectToolForDrops());
    public static final DeferredItem<BlockItem> REDSTONE_CONVERTER_ITEM =
            ITEMS.registerSimpleBlockItem("redstone_converter", REDSTONE_CONVERTER);

    public static final DeferredBlock<AdvancedFurnaceBlock> ADVANCED_FURNACE =
            BLOCKS.registerBlock("advanced_furnace", AdvancedFurnaceBlock::new,
                    BlockBehaviour.Properties.of()
                            .mapColor(MapColor.COLOR_ORANGE)
                            .strength(3.5f)
                            .sound(SoundType.METAL)
                            .lightLevel(state -> state.getValue(AdvancedFurnaceBlock.LIT) ? 13 : 0)
                            .requiresCorrectToolForDrops());
    public static final DeferredItem<BlockItem> ADVANCED_FURNACE_ITEM =
            ITEMS.registerSimpleBlockItem("advanced_furnace", ADVANCED_FURNACE);

    public static final DeferredBlock<GrinderBlock> GRINDER =
            BLOCKS.registerBlock("grinder", GrinderBlock::new,
                    BlockBehaviour.Properties.of()
                            .mapColor(MapColor.COLOR_ORANGE)
                            .strength(3.5f)
                            .sound(SoundType.METAL)
                            .lightLevel(state -> state.getValue(GrinderBlock.LIT) ? 6 : 0)
                            .requiresCorrectToolForDrops());
    public static final DeferredItem<BlockItem> GRINDER_ITEM =
            ITEMS.registerSimpleBlockItem("grinder", GRINDER);

    public static final DeferredBlock<FreezerBlock> FREEZER =
            BLOCKS.registerBlock("freezer", FreezerBlock::new,
                    BlockBehaviour.Properties.of()
                            .mapColor(MapColor.ICE)
                            .strength(3.5f)
                            .sound(SoundType.METAL)
                            .requiresCorrectToolForDrops());
    public static final DeferredItem<BlockItem> FREEZER_ITEM =
            ITEMS.registerSimpleBlockItem("freezer", FREEZER);

    public static final DeferredBlock<ConcreteMixerBlock> CONCRETE_MIXER =
            BLOCKS.registerBlock("concrete_mixer", ConcreteMixerBlock::new,
                    BlockBehaviour.Properties.of()
                            .mapColor(MapColor.COLOR_ORANGE)
                            .strength(3.5f)
                            .sound(SoundType.METAL)
                            .requiresCorrectToolForDrops());
    public static final DeferredItem<BlockItem> CONCRETE_MIXER_ITEM =
            ITEMS.registerSimpleBlockItem("concrete_mixer", CONCRETE_MIXER);

    public static final DeferredBlock<OxidationStationBlock> OXIDATION_STATION =
            BLOCKS.registerBlock("oxidation_station", OxidationStationBlock::new,
                    BlockBehaviour.Properties.of()
                            .mapColor(MapColor.COLOR_ORANGE)
                            .strength(3.5f)
                            .sound(SoundType.METAL)
                            .requiresCorrectToolForDrops());
    public static final DeferredItem<BlockItem> OXIDATION_STATION_ITEM =
            ITEMS.registerSimpleBlockItem("oxidation_station", OXIDATION_STATION);

    public static final DeferredBlock<DyerBlock> DYER =
            BLOCKS.registerBlock("dyer", DyerBlock::new,
                    BlockBehaviour.Properties.of()
                            .mapColor(MapColor.COLOR_ORANGE)
                            .strength(3.5f)
                            .sound(SoundType.METAL)
                            .requiresCorrectToolForDrops());
    public static final DeferredItem<BlockItem> DYER_ITEM =
            ITEMS.registerSimpleBlockItem("dyer", DYER);

    public static final DeferredBlock<DebarkerBlock> DEBARKER =
            BLOCKS.registerBlock("debarker", DebarkerBlock::new,
                    BlockBehaviour.Properties.of()
                            .mapColor(MapColor.COLOR_ORANGE)
                            .strength(3.5f)
                            .sound(SoundType.METAL)
                            .lightLevel(state -> state.getValue(DebarkerBlock.LIT) ? 6 : 0)
                            .requiresCorrectToolForDrops());
    public static final DeferredItem<BlockItem> DEBARKER_ITEM =
            ITEMS.registerSimpleBlockItem("debarker", DEBARKER);

    // Redstone Dynamo : cœur du multibloc
    public static final DeferredBlock<RedstoneDynamoBlock> REDSTONE_DYNAMO =
            BLOCKS.registerBlock("redstone_dynamo", RedstoneDynamoBlock::new,
                    BlockBehaviour.Properties.of()
                            .mapColor(MapColor.COLOR_ORANGE)
                            .strength(3.5f, 6.0f)
                            .sound(SoundType.COPPER)
                            .noOcclusion()   // modèle ajouré : socle, bobine, chapeau
                            .requiresCorrectToolForDrops());
    public static final DeferredItem<BlockItem> REDSTONE_DYNAMO_ITEM =
            ITEMS.registerSimpleBlockItem("redstone_dynamo", REDSTONE_DYNAMO);

    // Portes logiques au FE et interrupteur : un bloc par type
    public static final Map<GateType, DeferredBlock<LogicGateBlock>> LOGIC_GATES = new EnumMap<>(GateType.class);
    public static final Map<GateType, DeferredItem<BlockItem>> LOGIC_GATE_ITEMS = new EnumMap<>(GateType.class);

    static {
        for (GateType type : GateType.values()) {
            DeferredBlock<LogicGateBlock> block = BLOCKS.registerBlock(type.id(),
                    properties -> new LogicGateBlock(type, properties),
                    BlockBehaviour.Properties.of()
                            .mapColor(MapColor.COLOR_ORANGE)
                            .strength(1.5f, 3.0f)
                            .sound(SoundType.COPPER)
                            .noOcclusion());
            LOGIC_GATES.put(type, block);
            LOGIC_GATE_ITEMS.put(type, ITEMS.registerSimpleBlockItem(type.id(), block));
        }
    }

    // ── Fluides (0.3) ─────────────────────────────────────────────────────
    // Tuyau en fer à vitres : transporte un fluide, visible à travers le verre
    public static final DeferredBlock<IronPipeBlock> IRON_PIPE =
            BLOCKS.registerBlock("iron_pipe", IronPipeBlock::new,
                    BlockBehaviour.Properties.of()
                            .mapColor(MapColor.METAL)
                            .sound(SoundType.METAL)
                            .strength(1.5f, 4.0f)
                            .noOcclusion()
                            .requiresCorrectToolForDrops());
    public static final DeferredItem<BlockItem> IRON_PIPE_ITEM =
            ITEMS.registerSimpleBlockItem("iron_pipe", IRON_PIPE);

    // Pompe : aspire eau, lave... sous elle, alimentée en FE
    public static final DeferredBlock<FluidPumpBlock> FLUID_PUMP =
            BLOCKS.registerBlock("fluid_pump", FluidPumpBlock::new,
                    BlockBehaviour.Properties.of()
                            .mapColor(MapColor.METAL)
                            .sound(SoundType.METAL)
                            .strength(3.5f, 6.0f)
                            .requiresCorrectToolForDrops());
    public static final DeferredItem<BlockItem> FLUID_PUMP_ITEM =
            ITEMS.registerSimpleBlockItem("fluid_pump", FLUID_PUMP);

    // Réservoir : cuve à vitres, le niveau se voit de l'extérieur
    public static final DeferredBlock<FluidReservoirBlock> FLUID_RESERVOIR =
            BLOCKS.registerBlock("fluid_reservoir", FluidReservoirBlock::new,
                    BlockBehaviour.Properties.of()
                            .mapColor(MapColor.METAL)
                            .sound(SoundType.METAL)
                            .strength(3.0f, 6.0f)
                            .noOcclusion()
                            .isViewBlocking((state, level, pos) -> false)
                            .requiresCorrectToolForDrops());
    public static final DeferredItem<BlockItem> FLUID_RESERVOIR_ITEM =
            ITEMS.registerSimpleBlockItem("fluid_reservoir", FLUID_RESERVOIR);

    // Arroseur : eau pour les cultures, lave pour les verrues du Nether
    public static final DeferredBlock<SprinklerBlock> SPRINKLER =
            BLOCKS.registerBlock("sprinkler", SprinklerBlock::new,
                    BlockBehaviour.Properties.of()
                            .mapColor(MapColor.METAL)
                            .sound(SoundType.METAL)
                            .strength(2.0f, 4.0f)
                            .noOcclusion()
                            .lightLevel(state -> state.getValue(SprinklerBlock.MODE) == SprinklerBlock.Mode.LAVA ? 8 : 0)
                            .requiresCorrectToolForDrops());
    public static final DeferredItem<BlockItem> SPRINKLER_ITEM =
            ITEMS.registerSimpleBlockItem("sprinkler", SPRINKLER);

    // Lampe électrique : couleur au choix (teinture)
    public static final DeferredBlock<ElectricLampBlock> ELECTRIC_LAMP =
            BLOCKS.registerBlock("electric_lamp", ElectricLampBlock::new,
                    BlockBehaviour.Properties.of()
                            .mapColor(MapColor.COLOR_ORANGE)
                            .strength(1.5f, 3.0f)
                            .sound(SoundType.COPPER)
                            .lightLevel(state -> state.getValue(ElectricLampBlock.LIT) ? 15 : 0));
    public static final DeferredItem<BlockItem> ELECTRIC_LAMP_ITEM =
            ITEMS.registerSimpleBlockItem("electric_lamp", ELECTRIC_LAMP);
}
