package com.electra.mod.datagen;

import com.electra.mod.Electra;
import com.electra.mod.block.AdvancedFurnaceBlock;
import com.electra.mod.block.ElectricLampBlock;
import com.electra.mod.block.GateType;
import com.electra.mod.block.LogicGateBlock;
import com.electra.mod.block.RedstoneConverterBlock;
import com.electra.mod.block.RedstoneDynamoBlock;
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
        ModelFile furnaceOn = models().orientableWithBottom("advanced_furnace_on",
                modLoc("block/advanced_furnace_side"),
                modLoc("block/advanced_furnace_front_on"),
                modLoc("block/advanced_furnace_bottom"),
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

        // ── Redstone Dynamo ────────────────────────────────────────────────
        // Modèle écrit à la main (socle, bobine, chapeau) ; le bras est dessiné par RedstoneDynamoRenderer.
        // Orientation selon l'axe, comme une bûche.
        ModelFile dynamo = models().getExistingFile(modLoc("block/redstone_dynamo"));
        getVariantBuilder(ModBlocks.REDSTONE_DYNAMO.get()).forAllStates(state -> {
            Direction.Axis axis = state.getValue(RedstoneDynamoBlock.AXIS);
            return ConfiguredModel.builder()
                    .modelFile(dynamo)
                    .rotationX(axis == Direction.Axis.Y ? 0 : 90)
                    .rotationY(axis == Direction.Axis.X ? 90 : 0)
                    .build();
        });
        simpleBlockItem(ModBlocks.REDSTONE_DYNAMO.get(), dynamo);

        // ── Portes logiques et interrupteur ─────────────────────────────────
        // Modèles écrits à la main (boîtier, embouts, voyants, manette). Multipart : le boîtier selon
        // l'orientation, plus chaque voyant allumé et la manette selon l'état.
        ModelFile ledOutput = models().getExistingFile(modLoc("block/logic_gate_led_output"));
        ModelFile ledA = models().getExistingFile(modLoc("block/logic_gate_led_a"));
        ModelFile ledB = models().getExistingFile(modLoc("block/logic_gate_led_b"));
        ModelFile leverOn = models().getExistingFile(modLoc("block/energy_switch_lever_on"));
        ModelFile leverOff = models().getExistingFile(modLoc("block/energy_switch_lever_off"));
        for (GateType type : GateType.values()) {
            LogicGateBlock gate = ModBlocks.LOGIC_GATES.get(type).get();
            ModelFile gateModel = models().getExistingFile(modLoc("block/" + type.id()));
            ModelFile xorModel = models().getExistingFile(modLoc("block/xor_gate"));
            var builder = getMultipartBuilder(gate);
            for (Direction facing : Direction.Plane.HORIZONTAL) {
                int rotation = ((int) facing.toYRot() + 180) % 360;
                if (type == GateType.OR) {
                    // Même boîtier ; seul le symbole du dessus change en mode XOR
                    builder.part().modelFile(gateModel).rotationY(rotation).addModel()
                            .condition(LogicGateBlock.FACING, facing).condition(LogicGateBlock.EXCLUSIVE, false).end();
                    builder.part().modelFile(xorModel).rotationY(rotation).addModel()
                            .condition(LogicGateBlock.FACING, facing).condition(LogicGateBlock.EXCLUSIVE, true).end();
                } else {
                    builder.part().modelFile(gateModel).rotationY(rotation).addModel()
                            .condition(LogicGateBlock.FACING, facing).end();
                }
                builder.part().modelFile(ledOutput).rotationY(rotation).addModel()
                        .condition(LogicGateBlock.FACING, facing).condition(LogicGateBlock.OUTPUT, true).end();
                if (type.hasInputs()) {
                    builder.part().modelFile(ledA).rotationY(rotation).addModel()
                            .condition(LogicGateBlock.FACING, facing).condition(LogicGateBlock.INPUT_A, true).end();
                    builder.part().modelFile(ledB).rotationY(rotation).addModel()
                            .condition(LogicGateBlock.FACING, facing).condition(LogicGateBlock.INPUT_B, true).end();
                }
                if (type == GateType.SWITCH) {
                    builder.part().modelFile(leverOn).rotationY(rotation).addModel()
                            .condition(LogicGateBlock.FACING, facing).condition(LogicGateBlock.ON, true).end();
                    builder.part().modelFile(leverOff).rotationY(rotation).addModel()
                            .condition(LogicGateBlock.FACING, facing).condition(LogicGateBlock.ON, false).end();
                }
            }
            simpleBlockItem(gate, gateModel);
        }

        // ── Lampe électrique : allumée / éteinte ; la couleur est appliquée par teinte (ElectraClient)
        ModelFile lampOff = models().getExistingFile(modLoc("block/electric_lamp"));
        ModelFile lampOn = models().getExistingFile(modLoc("block/electric_lamp_on"));
        getVariantBuilder(ModBlocks.ELECTRIC_LAMP.get()).forAllStates(state -> ConfiguredModel.builder()
                .modelFile(state.getValue(ElectricLampBlock.LIT) ? lampOn : lampOff).build());
        simpleBlockItem(ModBlocks.ELECTRIC_LAMP.get(), lampOff);

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
