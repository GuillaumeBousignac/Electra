package com.electra.mod.datagen;

import com.electra.mod.Electra;
import com.electra.mod.block.AdvancedFurnaceBlock;
import com.electra.mod.block.RedstoneConverterBlock;
import com.electra.mod.block.WireBlock;
import com.electra.mod.setup.ModBlocks;
import net.minecraft.core.Direction;
import net.minecraft.data.PackOutput;
import net.minecraft.world.item.DyeColor;
import net.minecraft.world.level.block.Block;
import net.neoforged.neoforge.client.model.generators.BlockStateProvider;
import net.neoforged.neoforge.client.model.generators.ConfiguredModel;
import net.neoforged.neoforge.client.model.generators.ModelFile;
import net.neoforged.neoforge.common.data.ExistingFileHelper;

/**
 * États de blocs et modèles simples.
 *
 * Les modèles faits dans Blockbench (Lightning Collector, câble nu, gabarits des câbles isolés)
 * restent écrits à la main dans src/main/resources : ils sont seulement référencés ici.
 */
public class ModBlockStateProvider extends BlockStateProvider {

    public ModBlockStateProvider(PackOutput output, ExistingFileHelper existingFiles) {
        super(output, Electra.MODID, existingFiles);
    }

    @Override
    protected void registerStatesAndModels() {
        // ── Blocs de stockage ──────────────────────────────────────────────
        simpleBlockWithItem(ModBlocks.ROSE_GOLD_BLOCK.get(), cubeAll(ModBlocks.ROSE_GOLD_BLOCK.get()));
        simpleBlockWithItem(ModBlocks.RED_IRON_BLOCK.get(), cubeAll(ModBlocks.RED_IRON_BLOCK.get()));

        // ── Lightning Collector : modèle Blockbench, même modèle chargé ou non
        //    (le cube d'énergie est dessiné par LightningCollectorRenderer)
        ModelFile collector = models().getExistingFile(modLoc("block/lightning_collector"));
        getVariantBuilder(ModBlocks.LIGHTNING_COLLECTOR.get())
                .forAllStates(state -> ConfiguredModel.builder().modelFile(collector).build());

        // ── Convertisseur redstone ─────────────────────────────────────────
        ModelFile converterOff = models().cubeBottomTop("redstone_converter",
                modLoc("block/redstone_converter_side"),
                modLoc("block/redstone_converter_bottom"),
                modLoc("block/redstone_converter_top"));
        ModelFile converterOn = models().cubeBottomTop("redstone_converter_on",
                modLoc("block/redstone_converter_side_on"),
                modLoc("block/redstone_converter_bottom"),
                modLoc("block/redstone_converter_top_on"));
        getVariantBuilder(ModBlocks.REDSTONE_CONVERTER.get())
                .partialState().with(RedstoneConverterBlock.POWERED, false)
                .modelForState().modelFile(converterOff).addModel()
                .partialState().with(RedstoneConverterBlock.POWERED, true)
                .modelForState().modelFile(converterOn).addModel();
        simpleBlockItem(ModBlocks.REDSTONE_CONVERTER.get(), converterOff);

        // ── Four avancé ────────────────────────────────────────────────────
        ModelFile furnaceOff = models().orientableWithBottom("advanced_furnace",
                modLoc("block/advanced_furnace_side"),
                modLoc("block/advanced_furnace_front"),
                modLoc("block/advanced_furnace_bottom"),
                modLoc("block/advanced_furnace_top"));
        // Identique à l'ancien fichier : allumé, la face du dessous reprend la texture du dessus
        ModelFile furnaceOn = models().orientable("advanced_furnace_on",
                modLoc("block/advanced_furnace_side"),
                modLoc("block/advanced_furnace_front_on"),
                modLoc("block/advanced_furnace_top"));
        getVariantBuilder(ModBlocks.ADVANCED_FURNACE.get()).forAllStates(state -> {
            Direction facing = state.getValue(AdvancedFurnaceBlock.FACING);
            boolean lit = state.getValue(AdvancedFurnaceBlock.LIT);
            return ConfiguredModel.builder()
                    .modelFile(lit ? furnaceOn : furnaceOff)
                    .rotationY(((int) facing.toYRot() + 180) % 360)
                    .build();
        });
        simpleBlockItem(ModBlocks.ADVANCED_FURNACE.get(), furnaceOff);

        // ── Câbles ─────────────────────────────────────────────────────────
        wire(ModBlocks.BARE_WIRE.get(),
                models().getExistingFile(modLoc("block/bare_wire_center")),
                models().getExistingFile(modLoc("block/bare_wire_side")));

        for (DyeColor color : DyeColor.values()) {
            String name = color.getName() + "_insulated_wire";
            ModelFile center = models().withExistingParent(name + "_center", modLoc("block/insulated_wire_center"))
                    .texture("wire", modLoc("block/" + name));
            ModelFile side = models().withExistingParent(name + "_side", modLoc("block/insulated_wire_side"))
                    .texture("wire", modLoc("block/" + name));
            wire(ModBlocks.INSULATED_WIRES.get(color).get(), center, side);
        }
    }

    /** Câble : un centre toujours présent + un bras par face connectée. */
    private void wire(Block block, ModelFile center, ModelFile side) {
        getMultipartBuilder(block)
                .part().modelFile(center).addModel().end()
                .part().modelFile(side).addModel()
                    .condition(WireBlock.NORTH, true).end()
                .part().modelFile(side).rotationY(180).addModel()
                    .condition(WireBlock.SOUTH, true).end()
                .part().modelFile(side).rotationY(90).addModel()
                    .condition(WireBlock.EAST, true).end()
                .part().modelFile(side).rotationY(270).addModel()
                    .condition(WireBlock.WEST, true).end()
                .part().modelFile(side).rotationX(270).addModel()
                    .condition(WireBlock.UP, true).end()
                .part().modelFile(side).rotationX(90).addModel()
                    .condition(WireBlock.DOWN, true).end();
    }
}
