package com.electra.mod.datagen;

import com.electra.mod.Electra;
import com.electra.mod.block.GateType;
import com.electra.mod.setup.ModBlocks;
import com.electra.mod.setup.ModItems;
import net.minecraft.data.PackOutput;
import net.minecraft.world.item.DyeColor;
import net.neoforged.neoforge.common.data.LanguageProvider;

/**
 * Traductions anglaises (en_us), générées.
 * Le français (fr_fr.json) reste écrit à la main dans src/main/resources.
 */
public class ModLanguageProvider extends LanguageProvider {

    public ModLanguageProvider(PackOutput output) {
        super(output, Electra.MODID, "en_us");
    }

    @Override
    protected void addTranslations() {
        // ── Blocs ──────────────────────────────────────────────────────────
        add(ModBlocks.LIGHTNING_COLLECTOR.get(), "Lightning Collector");
        add(ModBlocks.BARE_WIRE.get(), "Bare Wire");
        add(ModBlocks.REDSTONE_CONVERTER.get(), "Redstone Converter");
        add(ModBlocks.ROSE_GOLD_BLOCK.get(), "Rose Gold Block");
        add(ModBlocks.RED_IRON_BLOCK.get(), "Red Iron Block");
        add(ModBlocks.ADVANCED_FURNACE.get(), "Advanced Furnace");
        add(ModBlocks.REDSTONE_DYNAMO.get(), "Redstone Dynamo");
        add(ModBlocks.LOGIC_GATES.get(GateType.AND).get(), "AND Gate");
        add(ModBlocks.LOGIC_GATES.get(GateType.OR).get(), "OR Gate");
        add(ModBlocks.LOGIC_GATES.get(GateType.NOT).get(), "NOT Gate");
        add(ModBlocks.LOGIC_GATES.get(GateType.SWITCH).get(), "Energy Switch");
        add(ModBlocks.ELECTRIC_LAMP.get(), "Electric Lamp");

        for (DyeColor color : DyeColor.values()) {
            add(ModBlocks.INSULATED_WIRES.get(color).get(), toTitle(color.getName()) + " Insulated Wire");
        }

        // ── Objets ─────────────────────────────────────────────────────────
        add(ModItems.ENERGY_METER.get(), "Energy Meter");
        add(ModItems.ROSE_GOLD_INGOT.get(), "Rose Gold Ingot");
        add(ModItems.RED_IRON_INGOT.get(), "Red Iron Ingot");

        // ── Onglets créatifs ───────────────────────────────────────────────
        add("creativetab.electra.electra_blocks", "Electra Blocks");
        add("creativetab.electra.electra_items", "Electra Items");

        // ── Messages et interfaces ─────────────────────────────────────────
        add("death.attack.electra.electrocution", "%1$s was electrocuted");
        add("death.attack.electra.electrocution.player", "%1$s was electrocuted while fighting %2$s");
        add("gui.electra.energy", "%s / %s FE");
        add("message.electra.energy_meter", "⚡ %s : %s / %s FE (%s%%)");

        // ── Infobulles ─────────────────────────────────────────────────────
        add("tooltip.electra.and_gate", "Output powered when both side inputs are powered");
        add("tooltip.electra.or_gate", "Output powered when at least one side input is powered. Sneak + right-click: switch to XOR (exactly one input)");
        add("message.electra.gate_mode_or", "Mode: OR");
        add("message.electra.gate_mode_xor", "Mode: XOR (exclusive OR)");
        add("tooltip.electra.not_gate", "Output powered from the back while no side input is powered");
        add("tooltip.electra.energy_switch", "Right-click to let power flow from the back to the front");
        add("tooltip.electra.electric_lamp", "Lights up while powered. Right-click with a dye to change its color");

        // ── JEI ────────────────────────────────────────────────────────────
        add("jei.electra.alloying", "Alloying");
        add("jei.electra.energy", "%s FE");
    }

    /** "light_blue" → "Light Blue" */
    private static String toTitle(String id) {
        StringBuilder sb = new StringBuilder();
        for (String word : id.split("_")) {
            if (!sb.isEmpty()) sb.append(' ');
            sb.append(Character.toUpperCase(word.charAt(0))).append(word.substring(1));
        }
        return sb.toString();
    }
}
