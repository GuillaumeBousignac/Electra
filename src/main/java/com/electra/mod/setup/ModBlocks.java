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
import net.neoforged.neoforge.registries.DeferredBlock;
import net.neoforged.neoforge.registries.DeferredItem;
import net.neoforged.neoforge.registries.DeferredRegister;

import java.util.Map;

public class ModBlocks {

    public static final DeferredRegister.Blocks BLOCKS =
            DeferredRegister.createBlocks(Electra.MODID);
    public static final DeferredRegister.Items ITEMS =
            DeferredRegister.createItems(Electra.MODID);

    public static final DeferredBlock<LightningCollectorBlock> LIGHTNING_COLLECTOR =
            BLOCKS.register("lightning_collector", LightningCollectorBlock::new);
    public static final DeferredItem<BlockItem> LIGHTNING_COLLECTOR_ITEM =
            ITEMS.registerSimpleBlockItem("lightning_collector", LIGHTNING_COLLECTOR);

    // Câble nu
    public static final DeferredBlock<BareWireBlock> BARE_WIRE =
            BLOCKS.register("bare_wire", BareWireBlock::new);
    public static final DeferredItem<BlockItem> BARE_WIRE_ITEM =
            ITEMS.registerSimpleBlockItem("bare_wire", BARE_WIRE);

    // Câbles habillés - 16 couleurs
    public static final Map<DyeColor, DeferredBlock<InsulatedWireBlock>> INSULATED_WIRES =
            new java.util.EnumMap<>(DyeColor.class);
    public static final Map<DyeColor, DeferredItem<BlockItem>> INSULATED_WIRE_ITEMS =
            new java.util.EnumMap<>(DyeColor.class);

    static {
        for (DyeColor color : DyeColor.values()) {
            DeferredBlock<InsulatedWireBlock> block = BLOCKS.register(
                    color.getName() + "_insulated_wire",
                    () -> new InsulatedWireBlock(color));
            INSULATED_WIRES.put(color, block);

            DeferredItem<BlockItem> item = ITEMS.registerSimpleBlockItem(
                    color.getName() + "_insulated_wire", block);
            INSULATED_WIRE_ITEMS.put(color, item);
        }
    }

    public static final DeferredBlock<Block> ROSE_GOLD_BLOCK =
            BLOCKS.register("rose_gold_block",
                    () -> new Block(BlockBehaviour.Properties.of()
                            .strength(4f).sound(SoundType.METAL)));
    public static final DeferredItem<BlockItem> ROSE_GOLD_BLOCK_ITEM =
            ITEMS.registerSimpleBlockItem("rose_gold_block", ROSE_GOLD_BLOCK);

    public static final DeferredBlock<RedstoneConverterBlock> REDSTONE_CONVERTER =
            BLOCKS.register("redstone_converter", RedstoneConverterBlock::new);
    public static final DeferredItem<BlockItem> REDSTONE_CONVERTER_ITEM =
            ITEMS.registerSimpleBlockItem("redstone_converter", REDSTONE_CONVERTER);

    public static final DeferredBlock<AdvancedFurnaceBlock> ADVANCED_FURNACE =
            BLOCKS.register("advanced_furnace",
                    () -> new AdvancedFurnaceBlock(BlockBehaviour.Properties.of()
                            .strength(3.5f)
                            .sound(SoundType.METAL)
                            .requiresCorrectToolForDrops()));
    public static final DeferredItem<BlockItem> ADVANCED_FURNACE_ITEM =
            ITEMS.registerSimpleBlockItem("advanced_furnace", ADVANCED_FURNACE);
}