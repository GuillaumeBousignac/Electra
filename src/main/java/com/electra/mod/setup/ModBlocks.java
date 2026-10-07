package com.electra.mod.setup;

import com.electra.mod.Electra;
import com.electra.mod.block.AdvancedFurnaceBlock;
import com.electra.mod.block.BareWireBlock;
import com.electra.mod.block.InsulatedWireBlock;
import com.electra.mod.block.LightningCollectorBlock;
import com.electra.mod.block.RedstoneConverterBlock;
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
}
