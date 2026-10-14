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
import net.neoforged.neoforge.client.model.generators.BlockModelBuilder;
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
                if (type == GateType.CLOCK) {
                    // Curseur de réglage : 4 positions, comme la torche mobile d'un répéteur
                    for (int delay = 1; delay <= 4; delay++) {
                        builder.part().modelFile(models().getExistingFile(modLoc("block/energy_clock_marker_" + delay)))
                                .rotationY(rotation).addModel()
                                .condition(LogicGateBlock.FACING, facing).condition(LogicGateBlock.DELAY, delay).end();
                    }
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

        // ── Lampe électrique : texture connectée (voir electricLamp)
        electricLamp();

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

    // ── Lampe électrique ───────────────────────────────────────────────────
    //
    // Chaque face est découpée en 9 morceaux plats : un centre, 4 bords (2 px) et 4 coins (2×2 px).
    // Un bord montre la bordure en cuivre, sauf si une lampe de même couleur touche ce côté :
    // il montre alors le verre et la bordure disparaît. Un coin reste en cuivre tant que l'un de
    // ses deux côtés n'est pas connecté. Le verre (tintindex 0) prend la couleur de la lampe.

    private static final String LAMP = "block/electric_lamp/";

    private void electricLamp() {
        var builder = getMultipartBuilder(ModBlocks.ELECTRIC_LAMP.get());
        BlockModelBuilder inventory = models().withExistingParent("block/electric_lamp_inventory", mcLoc("block/block"))
                .texture("frame", modLoc("block/electric_lamp_frame"))
                .texture("glass", modLoc("block/electric_lamp_glass"))
                .texture("particle", modLoc("block/electric_lamp_frame"));

        for (boolean lit : new boolean[]{false, true}) {
            String glass = lit ? "on" : "off";
            // Centres des 6 faces
            BlockModelBuilder center = lampModel(LAMP + "center_" + glass, lit);
            for (Direction face : Direction.values()) lampQuad(center, face, 2, 14, 2, 14, "#glass", true);
            builder.part().modelFile(center).addModel().condition(ElectricLampBlock.LIT, lit).end();
        }
        for (Direction face : Direction.values()) {
            lampQuad(inventory, face, 2, 14, 2, 14, "#glass", false);
            Direction[] sides = inPlane(face);   // [a-, a+, b-, b+]
            for (Direction side : sides) {
                int[] a = range(side, sides);
                String name = face.getName() + "_" + side.getName();
                // Bord en cuivre (côté non connecté)
                ModelFile copper = lampPiece(LAMP + "edge_" + name, face, a, "#frame", false);
                builder.part().modelFile(copper).addModel()
                        .condition(ElectricLampBlock.CONNECTIONS.get(side), false).end();
                lampQuad(inventory, face, a[0], a[1], a[2], a[3], "#frame", false);
                // Bord en verre (côté connecté)
                for (boolean lit : new boolean[]{false, true}) {
                    ModelFile glass = lampPiece(LAMP + "edge_" + name + (lit ? "_on" : "_off"), face, a, "#glass", lit);
                    builder.part().modelFile(glass).addModel()
                            .condition(ElectricLampBlock.CONNECTIONS.get(side), true)
                            .condition(ElectricLampBlock.LIT, lit).end();
                }
            }
            // Coins : un côté de chaque axe du plan de la face
            for (Direction sa : new Direction[]{sides[0], sides[1]}) {
                for (Direction sb : new Direction[]{sides[2], sides[3]}) {
                    int[] ra = range(sa, sides), rb = range(sb, sides);
                    int[] c = {ra[0], ra[1], rb[2], rb[3]};
                    String name = face.getName() + "_" + sa.getName() + "_" + sb.getName();
                    ModelFile copper = lampPiece(LAMP + "corner_" + name, face, c, "#frame", false);
                    builder.part().modelFile(copper).addModel()
                            .condition(ElectricLampBlock.CONNECTIONS.get(sa), false).end();
                    builder.part().modelFile(copper).addModel()
                            .condition(ElectricLampBlock.CONNECTIONS.get(sa), true)
                            .condition(ElectricLampBlock.CONNECTIONS.get(sb), false).end();
                    lampQuad(inventory, face, c[0], c[1], c[2], c[3], "#frame", false);
                    for (boolean lit : new boolean[]{false, true}) {
                        ModelFile glass = lampPiece(LAMP + "corner_" + name + (lit ? "_on" : "_off"), face, c, "#glass", lit);
                        builder.part().modelFile(glass).addModel()
                                .condition(ElectricLampBlock.CONNECTIONS.get(sa), true)
                                .condition(ElectricLampBlock.CONNECTIONS.get(sb), true)
                                .condition(ElectricLampBlock.LIT, lit).end();
                    }
                }
            }
        }
        simpleBlockItem(ModBlocks.ELECTRIC_LAMP.get(), inventory);
    }

    private BlockModelBuilder lampModel(String path, boolean lit) {
        return models().getBuilder(path)
                .texture("frame", modLoc("block/electric_lamp_frame"))
                .texture("glass", modLoc("block/electric_lamp_glass" + (lit ? "_on" : "")))
                .texture("particle", modLoc("block/electric_lamp_frame"));
    }

    /** Modèle d'un seul morceau de face (bord ou coin), masqué si une lampe recouvre cette face. */
    private ModelFile lampPiece(String path, Direction face, int[] r, String texture, boolean lit) {
        BlockModelBuilder model = lampModel(path, lit);
        lampQuad(model, face, r[0], r[1], r[2], r[3], texture, true);
        return model;
    }

    /**
     * Les deux axes du plan d'une face, dans l'ordre [a-, a+, b-, b+] où a et b sont,
     * dans cet ordre, les axes X, Y, Z privés de l'axe de la face.
     */
    private static Direction[] inPlane(Direction face) {
        Direction.Axis[] axes = switch (face.getAxis()) {
            case X -> new Direction.Axis[]{Direction.Axis.Y, Direction.Axis.Z};
            case Y -> new Direction.Axis[]{Direction.Axis.X, Direction.Axis.Z};
            case Z -> new Direction.Axis[]{Direction.Axis.X, Direction.Axis.Y};
        };
        return new Direction[]{
                Direction.fromAxisAndDirection(axes[0], Direction.AxisDirection.NEGATIVE),
                Direction.fromAxisAndDirection(axes[0], Direction.AxisDirection.POSITIVE),
                Direction.fromAxisAndDirection(axes[1], Direction.AxisDirection.NEGATIVE),
                Direction.fromAxisAndDirection(axes[1], Direction.AxisDirection.POSITIVE)};
    }

    /** Bornes {a0, a1, b0, b1} du bord tourné vers {@code side}, dans le plan de la face. */
    private static int[] range(Direction side, Direction[] sides) {
        boolean onA = side.getAxis() == sides[0].getAxis();
        boolean positive = side.getAxisDirection() == Direction.AxisDirection.POSITIVE;
        int lo = positive ? 14 : 0, hi = positive ? 16 : 2;
        return onA ? new int[]{lo, hi, 2, 14} : new int[]{2, 14, lo, hi};
    }

    /**
     * Ajoute un morceau plat (épaisseur nulle) sur une face du cube : a et b sont les coordonnées dans
     * le plan de la face (voir inPlane). Les UV suivent la position (même règle que les UV automatiques
     * de Minecraft), ce qui fait se raccorder les textures d'une lampe à l'autre.
     */
    private static void lampQuad(BlockModelBuilder model, Direction face, float a0, float a1, float b0, float b1,
                                 String texture, boolean cull) {
        float[] from = new float[3], to = new float[3];
        Direction.Axis[] axes = switch (face.getAxis()) {
            case X -> new Direction.Axis[]{Direction.Axis.Y, Direction.Axis.Z};
            case Y -> new Direction.Axis[]{Direction.Axis.X, Direction.Axis.Z};
            case Z -> new Direction.Axis[]{Direction.Axis.X, Direction.Axis.Y};
        };
        int ia = axes[0].ordinal(), ib = axes[1].ordinal(), in = face.getAxis().ordinal();
        from[ia] = a0; to[ia] = a1; from[ib] = b0; to[ib] = b1;
        from[in] = to[in] = face.getAxisDirection() == Direction.AxisDirection.POSITIVE ? 16 : 0;

        float x0 = from[0], x1 = to[0], y0 = from[1], y1 = to[1], z0 = from[2], z1 = to[2];
        float[] uv = switch (face) {
            case NORTH -> new float[]{16 - x1, 16 - y1, 16 - x0, 16 - y0};
            case SOUTH -> new float[]{x0, 16 - y1, x1, 16 - y0};
            case EAST -> new float[]{16 - z1, 16 - y1, 16 - z0, 16 - y0};
            case WEST -> new float[]{z0, 16 - y1, z1, 16 - y0};
            case UP -> new float[]{x0, z0, x1, z1};
            case DOWN -> new float[]{x0, 16 - z1, x1, 16 - z0};
        };
        var faceBuilder = model.element().from(from[0], from[1], from[2]).to(to[0], to[1], to[2])
                .face(face).uvs(uv[0], uv[1], uv[2], uv[3]).texture(texture);
        if (cull) faceBuilder.cullface(face);
        if ("#glass".equals(texture)) faceBuilder.tintindex(0);
        faceBuilder.end().end();
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
