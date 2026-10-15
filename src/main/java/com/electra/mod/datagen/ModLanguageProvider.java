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
        add(ModBlocks.LOGIC_GATES.get(GateType.CLOCK).get(), "Energy Clock");
        add(ModBlocks.ELECTRIC_LAMP.get(), "Electric Lamp");

        for (DyeColor color : DyeColor.values()) {
            add(ModBlocks.INSULATED_WIRES.get(color).get(), toTitle(color.getName()) + " Insulated Wire");
        }

        // ── Objets ─────────────────────────────────────────────────────────
        add(ModItems.ENERGY_METER.get(), "Energy Meter");
        add(ModItems.ROSE_GOLD_INGOT.get(), "Rose Gold Ingot");
        add(ModItems.ROSE_GOLD_NUGGET.get(), "Rose Gold Nugget");
        add(ModItems.ALPHABETIC_STICKER.get(), "Alphabetic Sticker");
        add(ModItems.TOOL_STICKER.get(), "Tool Sticker");
        add(ModItems.ITEM_STICKER.get(), "Item Sticker");
        add(ModItems.SYMBOL_STICKER.get(), "Symbol Sticker");
        add(ModItems.NUMBER_STICKER.get(), "Number Sticker");
        add(ModItems.MOB_STICKER.get(), "Mob Sticker");
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
        add("tooltip.electra.energy_clock", "Turns the power from the back on and off in a loop. Right-click: change the phase length");
        add("message.electra.clock_phase", "Clock: %1$s s on, then %1$s s off");
        add("tooltip.electra.electric_lamp", "Lights up while powered; touching lamps share power. Right-click with a dye to change its color");
        add("tooltip.electra.electric_lamp_symbol", "With a sticker: sneak + right-click for the next symbol, sneak + right-click with a dye for its color, shears to remove it");
        add("tooltip.electra.lamp_sticker", "Right-click an Electric Lamp to stick a symbol on it");
        add("symbol.electra.pickaxe", "Pickaxe");
        add("symbol.electra.shovel", "Shovel");
        add("symbol.electra.sword", "Sword");
        add("symbol.electra.axe", "Axe");
        add("symbol.electra.hoe", "Hoe");
        add("symbol.electra.bow", "Bow");
        add("symbol.electra.crossbow", "Crossbow");
        add("symbol.electra.trident", "Trident");
        add("symbol.electra.mace", "Mace");
        add("symbol.electra.shears", "Shears");
        add("symbol.electra.bucket", "Bucket");
        add("symbol.electra.ingot", "Ingot");
        add("symbol.electra.stick", "Stick");
        add("symbol.electra.diamond", "Diamond");
        add("symbol.electra.dust", "Redstone Dust");
        add("symbol.electra.emerald", "Emerald");
        add("symbol.electra.amethyst", "Amethyst Shard");
        add("symbol.electra.coal", "Coal");
        add("symbol.electra.gunpowder", "Gunpowder");
        add("symbol.electra.ender_pearl", "Ender Pearl");
        add("symbol.electra.nether_star", "Nether Star");
        add("symbol.electra.apple", "Apple");
        add("symbol.electra.bread", "Bread");
        add("symbol.electra.bone", "Bone");
        add("symbol.electra.string", "String");
        add("symbol.electra.feather", "Feather");
        add("symbol.electra.book", "Book");
        add("symbol.electra.warning", "Warning");
        add("symbol.electra.exclamation", "Exclamation Mark");
        add("symbol.electra.question", "Question Mark");
        add("symbol.electra.lightning", "Lightning");
        add("symbol.electra.heart", "Heart");
        add("symbol.electra.star", "Star");
        add("symbol.electra.check", "Check Mark");
        add("symbol.electra.cross", "Cross");
        add("symbol.electra.arrow_up", "Up Arrow");
        add("symbol.electra.arrow_right", "Right Arrow");
        add("symbol.electra.arrow_down", "Down Arrow");
        add("symbol.electra.arrow_left", "Left Arrow");
        add("symbol.electra.power", "Power");
        add("symbol.electra.creeper", "Creeper");
        add("symbol.electra.skeleton", "Skeleton");
        add("symbol.electra.zombie", "Zombie");
        add("symbol.electra.villager", "Villager");
        add("symbol.electra.spider", "Spider");
        add("symbol.electra.pig", "Pig");
        add("symbol.electra.cow", "Cow");
        add("message.electra.lamp_has_sticker", "This lamp already has a sticker: remove it with shears");
        add("message.electra.lamp_symbol", "Symbol: %s");
        add("symbol.electra.none", "none");

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
