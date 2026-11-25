package com.electra.mod.guide;

import java.util.Arrays;
import java.util.List;

/**
 * Contenu du livre-guide (en anglais). Source unique : l'ecran du livre lit les textes par leurs
 * cles de traduction, et ModLanguageProvider les ecrit dans en_us.json.
 * Pour ajouter un chapitre : l'ajouter ici, puis relancer la generation des donnees.
 */
public final class GuideContent {

    public enum Kind { HEADING, TEXT, IMAGE, RECIPE }

    /**
     * Un bloc du livre. text : le texte, ou la legende pour une image ou une recette.
     * data : nom du fichier pour une image (dans textures/gui/guide/), resultat pour une recette
     * ("namespace:objet" ou "namespace:objet*quantite"). slots : les 9 cases de la grille, "" si vide.
     */
    public record Entry(Kind kind, String text, String data, List<String> slots) {}

    public record Chapter(String id, String title, List<Entry> entries) {
        public String titleKey() {
            return "guide.electra." + id + ".title";
        }

        /** Cle de traduction de l'entree numero i. */
        public String entryKey(int i) {
            return "guide.electra." + id + ".e" + i;
        }
    }

    private static Entry heading(String text) {
        return new Entry(Kind.HEADING, text, "", List.of());
    }

    private static Entry text(String text) {
        return new Entry(Kind.TEXT, text, "", List.of());
    }

    /** Image : fichier dans src/main/resources/assets/electra/textures/gui/guide/. */
    private static Entry image(String caption, String file) {
        return new Entry(Kind.IMAGE, caption, file, List.of());
    }

    /** Recette : 9 cases (ligne par ligne, haut gauche en premier), "" pour une case vide. */
    private static Entry recipe(String caption, String result, String... slots) {
        if (slots.length != 9) throw new IllegalArgumentException("A recipe needs 9 slots: " + caption);
        return new Entry(Kind.RECIPE, caption, result, Arrays.asList(slots));
    }

    public static final List<Chapter> CHAPTERS = List.of(
            new Chapter("welcome", "Welcome", List.of(
                    text("Electra adds physical electricity to Minecraft. Power travels through wires you can see, machines are plugged in by you, and nothing hides in an invisible network."),
                    text("This book explains everything the mod adds. Pick a chapter in the list on the left, then turn pages with the arrow buttons, the arrow keys or the mouse wheel."),
                    heading("Your first steps"),
                    text("1. Smelt copper and craft Bare Wire: a row of three copper ingots makes eight wires."),
                    recipe("Bare Wire", "electra:bare_wire*8",
                    "", "", "", "minecraft:copper_ingot", "minecraft:copper_ingot", "minecraft:copper_ingot", "", "", ""),
                    text("2. Build a Lightning Collector and place a lightning rod right on top of it."),
                    recipe("Lightning Collector", "electra:lightning_collector",
                    "minecraft:copper_ingot", "minecraft:lightning_rod", "minecraft:copper_ingot", "minecraft:copper_ingot", "", "minecraft:copper_ingot", "minecraft:copper_block", "minecraft:copper_block", "minecraft:copper_block"),
                    text("3. Craft an Advanced Furnace and link it to the collector with wires."),
                    recipe("Advanced Furnace", "electra:advanced_furnace",
                    "minecraft:copper_ingot", "minecraft:copper_ingot", "minecraft:copper_ingot", "minecraft:copper_ingot", "minecraft:furnace", "minecraft:copper_ingot", "minecraft:copper_ingot", "electra:bare_wire", "minecraft:copper_ingot"),
                    text("4. Wait for a thunderstorm. Each strike on the rod fills the collector with energy."),
                    text("Every recipe can also be looked up in JEI, which has a page for each Electra machine."))),
            new Chapter("power", "Power", List.of(
                    heading("Energy (FE)"),
                    text("Electra runs on FE, the energy unit shared by most mods, so its blocks can work together with machines from other mods."),
                    heading("Lightning Collector"),
                    text("A block with a lightning rod placed directly on top of it. When lightning strikes the rod, the collector stores the energy: 5000 FE per strike by default, up to 1,000,000 FE in total."),
                    text("It also acts as a battery. It hands its energy to the wires on demand, at up to 1000 FE per tick, so nothing is wasted between two storms."),
                    image("A Lightning Collector with a lightning rod placed on top of it.", "lightning_collector.png"),
                    heading("Redstone Dynamo"),
                    text("A steady generator built as a multiblock. Surround the Dynamo with a square ring of redstone blocks, five blocks wide, and leave a free corridor between the Dynamo and the ring. The corner blocks are optional."),
                    image("Top view. The Dynamo sits in the middle of the ring of redstone blocks (the corners are optional), with the Red Iron Block against it in the corridor.", "dynamo_layout.png"),
                    text("Place a Red Iron Block against one side of the Dynamo, on a side facing the ring. The Dynamo arm carries the block around it, clockwise. The magnetic pull between red iron and redstone is what makes the power."),
                    text("Give it a redstone signal to start it. It speeds up for about ten seconds, then produces 40 FE per tick. It needs no fuel, and the spinning block never hurts anyone."),
                    recipe("Redstone Dynamo", "electra:redstone_dynamo",
                    "minecraft:copper_ingot", "minecraft:copper_ingot", "minecraft:copper_ingot", "electra:red_iron_ingot", "electra:bare_wire", "electra:red_iron_ingot", "minecraft:copper_ingot", "minecraft:copper_ingot", "minecraft:copper_ingot"),
                    heading("Red Iron"),
                    text("Red Iron is an alloy of iron and redstone, made in the Advanced Furnace."))),
            new Chapter("wires", "Wires", List.of(
                    text("Wires carry FE between generators and machines. A wire connects by itself to any neighbouring block that handles FE, and connected wires form a network."),
                    heading("Bare Wire"),
                    text("Made of copper: three copper ingots in a row give eight wires. Touching a bare wire that carries energy shocks players and mobs. It is also the neutral wire: it connects to wires of every colour."),
                    heading("Insulated Wire"),
                    text("Surround a wool block with eight Bare Wires to get eight insulated wires of the wool colour. They never shock. Wires of different colours do not connect to each other, so you can run several separate networks side by side."),
                    recipe("Insulated Wire (any wool colour)", "electra:white_insulated_wire*8",
                    "electra:bare_wire", "electra:bare_wire", "electra:bare_wire", "electra:bare_wire", "minecraft:white_wool", "electra:bare_wire", "electra:bare_wire", "electra:bare_wire", "electra:bare_wire"),
                    heading("Losing the source"),
                    text("A network that is no longer fed empties itself in about one second. Wires are not batteries, so keep a source connected. The Lightning Collector is the exception: it stores its energy and gives it on demand."))),
            new Chapter("machines", "Machines", List.of(
                    heading("Advanced Furnace"),
                    text("Smelts like a furnace, and also alloys two ingredients together. Rose Gold comes from a copper ingot and a gold ingot. Red Iron comes from iron and redstone."),
                    recipe("Advanced Furnace", "electra:advanced_furnace",
                    "minecraft:copper_ingot", "minecraft:copper_ingot", "minecraft:copper_ingot", "minecraft:copper_ingot", "minecraft:furnace", "minecraft:copper_ingot", "minecraft:copper_ingot", "electra:bare_wire", "minecraft:copper_ingot"),
                    heading("Grinder"),
                    text("Crushes blocks and items with about fifty vanilla recipes: cobblestone becomes gravel, then sand, ores are doubled and flowers become dyes."),
                    recipe("Grinder", "electra:grinder",
                    "minecraft:copper_ingot", "minecraft:copper_ingot", "minecraft:copper_ingot", "minecraft:copper_ingot", "minecraft:grindstone", "minecraft:copper_ingot", "minecraft:copper_ingot", "electra:bare_wire", "minecraft:copper_ingot"),
                    heading("Freezer"),
                    text("Turns water into ice, packed ice, blue ice and snow. It can also turn a magma block into obsidian and rotten flesh into leather. It needs to be alimented by a pipe filled with water to produce ice."),
                    recipe("Freezer", "electra:freezer",
                    "minecraft:copper_ingot", "minecraft:copper_ingot", "minecraft:copper_ingot", "electra:rose_gold_ingot", "minecraft:packed_ice", "electra:rose_gold_ingot", "minecraft:copper_ingot", "electra:bare_wire", "minecraft:copper_ingot"),
                    heading("Debarker"),
                    text("Strips logs, wood, stems, hyphae and bamboo automatically."),
                    text("It can be very useful when you don't want to bark logs by hands."),
                    recipe("Debarker", "electra:debarker",
                    "minecraft:copper_ingot", "minecraft:copper_ingot", "minecraft:copper_ingot", "minecraft:copper_ingot", "minecraft:iron_axe", "minecraft:copper_ingot", "minecraft:copper_ingot", "electra:bare_wire", "minecraft:copper_ingot"),
                    heading("Concrete Mixer"),
                    text("Turns concrete powder of all sixteen colours into concrete. It also makes mud from dirt, clay from sand and mud bricks from mud."),
                    recipe("Concrete Mixer", "electra:concrete_mixer",
                    "minecraft:copper_ingot", "minecraft:copper_ingot", "minecraft:copper_ingot", "minecraft:copper_ingot", "minecraft:cauldron", "minecraft:copper_ingot", "minecraft:copper_ingot", "electra:bare_wire", "minecraft:copper_ingot"),
                    heading("Oxidation Station"),
                    text("Ages copper through all its different stages, adds moss to stone, turns iron and copper into dyes and rusts Red Iron."),
                    text("Way better than wax."),
                    recipe("Oxidation Station", "electra:oxidation_station",
                    "minecraft:copper_ingot", "minecraft:copper_ingot", "minecraft:copper_ingot", "minecraft:copper_ingot", "minecraft:water_bucket", "minecraft:copper_ingot", "minecraft:copper_ingot", "electra:bare_wire", "minecraft:copper_ingot"),
                    heading("Dyer"),
                    text("Dyes anything dyeable, from any colour to any colour, with one item and one dye per operation. Leather armor accepts a mix of up to three dyes, like a crafting table. Shulker boxes keep their contents and banners keep their patterns."),
                    recipe("Dyer (any dye works)", "electra:dyer",
                    "minecraft:copper_ingot", "minecraft:copper_ingot", "minecraft:copper_ingot", "minecraft:copper_ingot", "minecraft:white_dye", "minecraft:copper_ingot", "minecraft:copper_ingot", "electra:bare_wire", "minecraft:copper_ingot"))),
            new Chapter("fluids", "Fluids", List.of(
                    text("Fluids travel through Iron Pipes. The liquid is visible through the glass, and a pipe network carries one fluid at a time."),
                    heading("Iron Pipe"),
                    text("A row of iron ingots, a row of glass and another row of iron give eight pipes. Pipes plug into any block that holds fluids."),
                    recipe("Iron Pipe", "electra:iron_pipe*8",
                    "minecraft:iron_ingot", "minecraft:iron_ingot", "minecraft:iron_ingot", "minecraft:glass", "minecraft:glass", "minecraft:glass", "minecraft:iron_ingot", "minecraft:iron_ingot", "minecraft:iron_ingot"),
                    heading("Fluid Pump"),
                    text("Powered by FE. Place it above water or lava: it sucks up the first source within six blocks below it, at 1000 mB per second (one bucket every second), and offers it to the pipes and reservoirs touching it."),
                    recipe("Fluid Pump", "electra:fluid_pump",
                    "minecraft:copper_ingot", "electra:bare_wire", "minecraft:copper_ingot", "minecraft:iron_ingot", "minecraft:bucket", "minecraft:iron_ingot", "minecraft:iron_ingot", "electra:iron_pipe", "minecraft:iron_ingot"),
                    heading("Fluid Reservoir"),
                    text("Stores fluids, with the level visible through the glass. Right-click it with a bucket to fill or empty it. Reservoirs placed side by side merge into one giant tank."),
                    recipe("Fluid Reservoir", "electra:fluid_reservoir",
                    "minecraft:iron_ingot", "minecraft:glass", "minecraft:iron_ingot", "minecraft:glass", "minecraft:bucket", "minecraft:glass", "minecraft:iron_ingot", "minecraft:glass", "minecraft:iron_ingot"),
                    heading("Sprinkler"),
                    text("Fed with water, it irrigates farmland and speeds up the crops below it. Fed with lava, it speeds up nether wart and burns creatures below it, but never burns blocks."),
                    recipe("Sprinkler", "electra:sprinkler",
                    "minecraft:iron_ingot", "electra:iron_pipe", "minecraft:iron_ingot", "minecraft:iron_bars", "minecraft:iron_bars", "minecraft:iron_bars", "", "", ""))),
            new Chapter("logic", "Logic", List.of(
                    text("Logic components work on energy itself: a signal is either on (energy present) or off. No redstone is involved, so circuits are built with wires."),
                    text("Every component has a front, which is its output and points where you looked when placing it, two sides used as inputs A and B, and a back."),
                    heading("AND Gate"),
                    text("Output on when both inputs are on. It takes the output energy from its inputs."),
                    heading("OR Gate"),
                    text("Output on when at least one input is on. Sneak and right-click it to turn it into an XOR gate, which is on only when exactly one input is on."),
                    heading("NOT Gate"),
                    text("The inputs are only signals: the energy comes from the back. Output on while no input is on."),
                    heading("Energy Switch"),
                    text("Lets energy flow from the back to the front once switched on with a right-click."),
                    heading("Energy Clock"),
                    text("Cuts and restores the energy from the back to the front, in a loop. Right-click to change the length of each phase."),
                    heading("Redstone Converter"),
                    text("Gives a redstone signal whenever a wire next to it belongs to a powered network. Use it to connect your circuits to doors, pistons and anything else that reads redstone."))),
            new Chapter("lamps", "Lamps", List.of(
                    text("The Electric Lamp lights up while powered. Lamps that touch each other share their power, so a large sign needs only one connection. Right-click a lamp with a dye to colour its glass."),
                    heading("Stickers"),
                    text("A sticker adds a symbol to a lamp: letters, numbers, tools, items, signs or mob faces. Right-click a lamp with a sticker to apply it. Then sneak and right-click the lamp to cycle through the symbols, or sneak and right-click with a dye to colour the symbol."),
                    text("Shears remove a sticker and give it back. Stickers are made from paper and slime balls with one extra ingredient: JEI lists each recipe."),
                    text("Together, lamps and stickers make readable control panels for your circuits."))),
            new Chapter("tips", "Tips", List.of(
                    heading("Energy Meter"),
                    text("Made of a glass pane, three copper ingots and redstone dust. Hold it and aim at any block that stores energy, from up to 20 blocks away: its charge appears above your hotbar. It also works on blocks from other mods."),
                    recipe("Energy Meter", "electra:energy_meter",
                    "", "minecraft:glass_pane", "", "minecraft:copper_ingot", "minecraft:redstone", "minecraft:copper_ingot", "", "minecraft:copper_ingot", ""),
                    heading("JEI and Jade"),
                    text("Every Electra machine has its own JEI category. Jade shows the energy stored in Electra blocks when you look at them."),
                    heading("Settings"),
                    text("Each world has a file named electra-server.toml in its serverconfig folder. It sets machine capacities and speeds, generator output and more."),
                    heading("Safety"),
                    text("Bare wires shock, lightning strikes where it wants and lava sprinklers burn creatures. Insulate the wires you walk near."))));

    private GuideContent() {}
}
