package com.electra.mod.datagen;

import com.electra.mod.Electra;
import com.electra.mod.block.GateType;
import com.electra.mod.recipe.AlloyingRecipe;
import com.electra.mod.recipe.FreezingRecipe;
import com.electra.mod.recipe.GrindingRecipe;
import com.electra.mod.setup.ModBlocks;
import com.electra.mod.setup.ModItems;
import net.minecraft.core.HolderLookup;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.data.PackOutput;
import net.minecraft.data.recipes.RecipeCategory;
import net.minecraft.data.recipes.RecipeOutput;
import net.minecraft.data.recipes.RecipeProvider;
import net.minecraft.data.recipes.ShapedRecipeBuilder;
import net.minecraft.data.recipes.ShapelessRecipeBuilder;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.tags.ItemTags;
import net.minecraft.tags.TagKey;
import net.minecraft.world.item.DyeColor;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.Items;
import net.minecraft.world.item.crafting.Ingredient;
import net.minecraft.world.level.ItemLike;
import net.neoforged.neoforge.common.Tags;
import org.jetbrains.annotations.NotNull;

import java.util.Optional;
import java.util.concurrent.CompletableFuture;

public class ModRecipeProvider extends RecipeProvider {

    public ModRecipeProvider(PackOutput output, CompletableFuture<HolderLookup.Provider> lookup) {
        super(output, lookup);
    }

    @Override
    protected void buildRecipes(@NotNull RecipeOutput output) {
        Item bareWire = ModBlocks.BARE_WIRE_ITEM.get();

        // ── Câbles ─────────────────────────────────────────────────────────
        ShapedRecipeBuilder.shaped(RecipeCategory.REDSTONE, bareWire, 8)
                .pattern("CCC")
                .define('C', Tags.Items.INGOTS_COPPER)
                .unlockedBy("has_copper_ingot", has(Tags.Items.INGOTS_COPPER))
                .save(output);

        for (DyeColor color : DyeColor.values()) {
            Item wool = BuiltInRegistries.ITEM.get(ResourceLocation.withDefaultNamespace(color.getName() + "_wool"));
            ShapedRecipeBuilder.shaped(RecipeCategory.REDSTONE, ModBlocks.INSULATED_WIRE_ITEMS.get(color).get(), 8)
                    .pattern("WWW")
                    .pattern("WLW")
                    .pattern("WWW")
                    .define('W', bareWire)
                    .define('L', wool)
                    .unlockedBy(getHasName(bareWire), has(bareWire))
                    .save(output);
        }

        // ── Machines ───────────────────────────────────────────────────────
        ShapedRecipeBuilder.shaped(RecipeCategory.MISC, ModBlocks.LIGHTNING_COLLECTOR_ITEM.get())
                .pattern("LRL")
                .pattern("L L")
                .pattern("CCC")
                .define('L', Tags.Items.INGOTS_COPPER)
                .define('C', Tags.Items.STORAGE_BLOCKS_COPPER)
                .define('R', Items.LIGHTNING_ROD)
                .unlockedBy(getHasName(Items.LIGHTNING_ROD), has(Items.LIGHTNING_ROD))
                .save(output);

        ShapedRecipeBuilder.shaped(RecipeCategory.MISC, ModBlocks.ADVANCED_FURNACE_ITEM.get())
                .pattern("CCC")
                .pattern("CFC")
                .pattern("CWC")
                .define('C', Tags.Items.INGOTS_COPPER)
                .define('F', Items.FURNACE)
                .define('W', bareWire)
                .unlockedBy(getHasName(bareWire), has(bareWire))
                .save(output);

        ShapedRecipeBuilder.shaped(RecipeCategory.MISC, ModBlocks.FREEZER_ITEM.get())
                .pattern("CCC")
                .pattern("RPR")
                .pattern("CWC")
                .define('C', Tags.Items.INGOTS_COPPER)
                .define('R', ModTags.Items.INGOTS_ROSE_GOLD)
                .define('P', Items.PACKED_ICE)
                .define('W', bareWire)
                .unlockedBy(getHasName(bareWire), has(bareWire))
                .save(output);

        ShapedRecipeBuilder.shaped(RecipeCategory.MISC, ModBlocks.GRINDER_ITEM.get())
                .pattern("CCC")
                .pattern("CGC")
                .pattern("CWC")
                .define('C', Tags.Items.INGOTS_COPPER)
                .define('G', Items.GRINDSTONE)
                .define('W', bareWire)
                .unlockedBy(getHasName(bareWire), has(bareWire))
                .save(output);

        ShapedRecipeBuilder.shaped(RecipeCategory.MISC, ModBlocks.REDSTONE_DYNAMO_ITEM.get())
                .pattern("CCC")
                .pattern("RWR")
                .pattern("CCC")
                .define('C', Tags.Items.INGOTS_COPPER)
                .define('R', ModTags.Items.INGOTS_RED_IRON)
                .define('W', bareWire)
                .unlockedBy("has_red_iron_ingot", has(ModTags.Items.INGOTS_RED_IRON))
                .save(output);

        // ── Fluides ────────────────────────────────────────────────────────
        // I = lingot de fer, G = verre, C = lingot de cuivre, W = câble nu, B = seau, P = tuyau, R = barreaux de fer
        Item ironPipe = ModBlocks.IRON_PIPE_ITEM.get();

        ShapedRecipeBuilder.shaped(RecipeCategory.REDSTONE, ironPipe, 8)
                .pattern("III")
                .pattern("GGG")
                .pattern("III")
                .define('I', Tags.Items.INGOTS_IRON)
                .define('G', Tags.Items.GLASS_BLOCKS)
                .unlockedBy("has_iron_ingot", has(Tags.Items.INGOTS_IRON))
                .save(output);

        ShapedRecipeBuilder.shaped(RecipeCategory.MISC, ModBlocks.FLUID_PUMP_ITEM.get())
                .pattern("CWC")
                .pattern("IBI")
                .pattern("IPI")
                .define('C', Tags.Items.INGOTS_COPPER)
                .define('W', bareWire)
                .define('I', Tags.Items.INGOTS_IRON)
                .define('B', Items.BUCKET)
                .define('P', ironPipe)
                .unlockedBy(getHasName(ironPipe), has(ironPipe))
                .save(output);

        ShapedRecipeBuilder.shaped(RecipeCategory.MISC, ModBlocks.FLUID_RESERVOIR_ITEM.get())
                .pattern("IGI")
                .pattern("GBG")
                .pattern("IGI")
                .define('I', Tags.Items.INGOTS_IRON)
                .define('G', Tags.Items.GLASS_BLOCKS)
                .define('B', Items.BUCKET)
                .unlockedBy(getHasName(ironPipe), has(ironPipe))
                .save(output);

        ShapedRecipeBuilder.shaped(RecipeCategory.MISC, ModBlocks.SPRINKLER_ITEM.get())
                .pattern("IPI")
                .pattern("RRR")
                .define('I', Tags.Items.INGOTS_IRON)
                .define('P', ironPipe)
                .define('R', Items.IRON_BARS)
                .unlockedBy(getHasName(ironPipe), has(ironPipe))
                .save(output);

        // ── Logique ────────────────────────────────────────────────────────
        // T = torche de redstone, G = lingot d'or rose (bon conducteur), W = câble nu
        gate(output, GateType.NOT, new String[]{" T ", "WGW"});
        gate(output, GateType.AND, new String[]{"T T", "WGW"});
        gate(output, GateType.OR, new String[]{" T ", "WGW", " T "});
        ShapedRecipeBuilder.shaped(RecipeCategory.REDSTONE, ModBlocks.LOGIC_GATE_ITEMS.get(GateType.SWITCH).get(), 2)
                .pattern(" L ")
                .pattern("WGW")
                .define('L', Items.LEVER)
                .define('G', ModTags.Items.INGOTS_ROSE_GOLD)
                .define('W', bareWire)
                .unlockedBy("has_rose_gold_ingot", has(ModTags.Items.INGOTS_ROSE_GOLD))
                .save(output);

        ShapedRecipeBuilder.shaped(RecipeCategory.REDSTONE, ModBlocks.LOGIC_GATE_ITEMS.get(GateType.CLOCK).get())
                .pattern(" K ")
                .pattern("WGW")
                .define('K', Items.CLOCK)
                .define('G', ModTags.Items.INGOTS_ROSE_GOLD)
                .define('W', bareWire)
                .unlockedBy("has_rose_gold_ingot", has(ModTags.Items.INGOTS_ROSE_GOLD))
                .save(output);

        ShapedRecipeBuilder.shaped(RecipeCategory.REDSTONE, ModBlocks.ELECTRIC_LAMP_ITEM.get(), 4)
                .pattern("GGG")
                .pattern("GDG")
                .pattern("CWC")
                .define('G', Tags.Items.GLASS_BLOCKS_COLORLESS)
                .define('D', Tags.Items.DUSTS_GLOWSTONE)
                .define('C', Tags.Items.INGOTS_COPPER)
                .define('W', bareWire)
                .unlockedBy(getHasName(bareWire), has(bareWire))
                .save(output);

        ShapedRecipeBuilder.shaped(RecipeCategory.REDSTONE, ModBlocks.REDSTONE_CONVERTER_ITEM.get())
                .pattern("CRC")
                .pattern("RWR")
                .pattern("CRC")
                .define('C', Tags.Items.INGOTS_COPPER)
                .define('R', Tags.Items.DUSTS_REDSTONE)
                .define('W', bareWire)
                .unlockedBy(getHasName(bareWire), has(bareWire))
                .save(output);

        ShapedRecipeBuilder.shaped(RecipeCategory.TOOLS, ModItems.ENERGY_METER.get())
                .pattern(" G ")
                .pattern("CRC")
                .pattern(" C ")
                .define('G', Tags.Items.GLASS_PANES)
                .define('C', Tags.Items.INGOTS_COPPER)
                .define('R', Tags.Items.DUSTS_REDSTONE)
                .unlockedBy("has_copper_ingot", has(Tags.Items.INGOTS_COPPER))
                .save(output);

        // Autocollant alphabétique : papier + colle (slime) + encre pour les lettres
        ShapelessRecipeBuilder.shapeless(RecipeCategory.REDSTONE, ModItems.ALPHABETIC_STICKER.get(), 8)
                .requires(Items.PAPER)
                .requires(Tags.Items.SLIME_BALLS)
                .requires(Items.INK_SAC)
                .unlockedBy(getHasName(ModBlocks.ELECTRIC_LAMP_ITEM.get()), has(ModBlocks.ELECTRIC_LAMP_ITEM.get()))
                .save(output);

        // Autocollant d'outils : papier + colle (slime) + pépite de fer pour les outils
        ShapelessRecipeBuilder.shapeless(RecipeCategory.REDSTONE, ModItems.TOOL_STICKER.get(), 8)
                .requires(Items.PAPER)
                .requires(Tags.Items.SLIME_BALLS)
                .requires(Tags.Items.NUGGETS_IRON)
                .unlockedBy(getHasName(ModBlocks.ELECTRIC_LAMP_ITEM.get()), has(ModBlocks.ELECTRIC_LAMP_ITEM.get()))
                .save(output);

        // Autocollant de symboles : papier + colle (slime) + poche d'encre luisante, comme pour les panneaux
        ShapelessRecipeBuilder.shapeless(RecipeCategory.REDSTONE, ModItems.SYMBOL_STICKER.get(), 8)
                .requires(Items.PAPER)
                .requires(Tags.Items.SLIME_BALLS)
                .requires(Items.GLOW_INK_SAC)
                .unlockedBy(getHasName(ModBlocks.ELECTRIC_LAMP_ITEM.get()), has(ModBlocks.ELECTRIC_LAMP_ITEM.get()))
                .save(output);

        // Autocollant de chiffres : papier + colle (slime) + redstone, pour les calculs
        ShapelessRecipeBuilder.shapeless(RecipeCategory.REDSTONE, ModItems.NUMBER_STICKER.get(), 8)
                .requires(Items.PAPER)
                .requires(Tags.Items.SLIME_BALLS)
                .requires(Tags.Items.DUSTS_REDSTONE)
                .unlockedBy(getHasName(ModBlocks.ELECTRIC_LAMP_ITEM.get()), has(ModBlocks.ELECTRIC_LAMP_ITEM.get()))
                .save(output);

        // Autocollant de créatures : papier + colle (slime) + os
        ShapelessRecipeBuilder.shapeless(RecipeCategory.REDSTONE, ModItems.MOB_STICKER.get(), 8)
                .requires(Items.PAPER)
                .requires(Tags.Items.SLIME_BALLS)
                .requires(Tags.Items.BONES)
                .unlockedBy(getHasName(ModBlocks.ELECTRIC_LAMP_ITEM.get()), has(ModBlocks.ELECTRIC_LAMP_ITEM.get()))
                .save(output);

        // Autocollant d'objets : papier + colle (slime) + cadre, qui sert à exposer les objets
        ShapelessRecipeBuilder.shapeless(RecipeCategory.REDSTONE, ModItems.ITEM_STICKER.get(), 8)
                .requires(Items.PAPER)
                .requires(Tags.Items.SLIME_BALLS)
                .requires(Items.ITEM_FRAME)
                .unlockedBy(getHasName(ModBlocks.ELECTRIC_LAMP_ITEM.get()), has(ModBlocks.ELECTRIC_LAMP_ITEM.get()))
                .save(output);

        // ── Blocs de stockage (9 lingots ⇄ 1 bloc) ─────────────────────────
        storageBlock(output, ModItems.ROSE_GOLD_INGOT.get(), ModTags.Items.INGOTS_ROSE_GOLD,
                ModBlocks.ROSE_GOLD_BLOCK_ITEM.get(), "rose_gold_ingot_from_block");
        storageBlock(output, ModItems.RED_IRON_INGOT.get(), ModTags.Items.INGOTS_RED_IRON,
                ModBlocks.RED_IRON_BLOCK_ITEM.get(), "red_iron_ingot_from_block");

        // ── Pépites (9 pépites ⇄ 1 lingot) ─────────────────────────────────
        ShapelessRecipeBuilder.shapeless(RecipeCategory.MISC, ModItems.ROSE_GOLD_NUGGET.get(), 9)
                .requires(ModTags.Items.INGOTS_ROSE_GOLD)
                .unlockedBy("has_rose_gold_ingot", has(ModTags.Items.INGOTS_ROSE_GOLD))
                .save(output);
        ShapedRecipeBuilder.shaped(RecipeCategory.MISC, ModItems.ROSE_GOLD_INGOT.get())
                .pattern("###")
                .pattern("###")
                .pattern("###")
                .define('#', ModTags.Items.NUGGETS_ROSE_GOLD)
                .unlockedBy("has_rose_gold_nugget", has(ModTags.Items.NUGGETS_ROSE_GOLD))
                .save(output, ResourceLocation.fromNamespaceAndPath(Electra.MODID, "rose_gold_ingot_from_nuggets"));

        // ── Alliages (four avancé) ─────────────────────────────────────────
        alloying(output, "rose_gold_ingot",
                Ingredient.of(Tags.Items.INGOTS_COPPER), Ingredient.of(Tags.Items.INGOTS_GOLD),
                ModItems.ROSE_GOLD_INGOT.get(), 100);
        alloying(output, "red_iron_ingot",
                Ingredient.of(Tags.Items.INGOTS_IRON), Ingredient.of(Tags.Items.DUSTS_REDSTONE),
                ModItems.RED_IRON_INGOT.get(), 150);
        alloying(output, "netherite_ingot",
                Ingredient.of(Tags.Items.INGOTS_GOLD), Ingredient.of(Items.NETHERITE_SCRAP),
                Items.NETHERITE_INGOT, 200);

        // ── Congélation (congélateur) ──────────────────────────────────────
        freezing(output, "ice", Optional.empty(), 1, 1000, Items.ICE, 200);
        freezing(output, "packed_ice", Optional.of(Ingredient.of(Items.ICE)), 1, 1000, Items.PACKED_ICE, 300);
        freezing(output, "blue_ice", Optional.of(Ingredient.of(Items.PACKED_ICE)), 1, 1000, Items.BLUE_ICE, 400);
        freezing(output, "powder_snow_bucket", Optional.of(Ingredient.of(Items.BUCKET)), 1, 1000, Items.POWDER_SNOW_BUCKET, 250);
        freezing(output, "obsidian", Optional.of(Ingredient.of(Items.MAGMA_BLOCK)), 1, 1000, Items.OBSIDIAN, 500);
        freezing(output, "leather", Optional.of(Ingredient.of(Items.ROTTEN_FLESH)), 3, 500, Items.LEATHER, 150);

        // ── Broyage (broyeur) ──────────────────────────────────────────────
        grinding(output, "stone_to_cobblestone", Ingredient.of(Items.STONE), Items.COBBLESTONE, 1, 80);
        grinding(output, "cobblestone_to_gravel", Ingredient.of(Items.COBBLESTONE), Items.GRAVEL, 1, 80);
        grinding(output, "gravel_to_sand", Ingredient.of(Items.GRAVEL), Items.SAND, 1, 80);
        grinding(output, "deepslate_to_cobbled_deepslate", Ingredient.of(Items.DEEPSLATE), Items.COBBLED_DEEPSLATE, 1, 80);
        grinding(output, "cobbled_deepslate_to_gravel", Ingredient.of(Items.COBBLED_DEEPSLATE), Items.GRAVEL, 1, 80);
        grinding(output, "sandstone_to_sand", Ingredient.of(Items.SANDSTONE), Items.SAND, 4, 80);
        grinding(output, "red_sandstone_to_red_sand", Ingredient.of(Items.RED_SANDSTONE), Items.RED_SAND, 4, 80);
        grinding(output, "bricks_to_brick", Ingredient.of(Items.BRICKS), Items.BRICK, 4, 80);
        grinding(output, "nether_bricks_to_nether_brick", Ingredient.of(Items.NETHER_BRICKS), Items.NETHER_BRICK, 4, 80);
        grinding(output, "clay_to_clay_ball", Ingredient.of(Items.CLAY), Items.CLAY_BALL, 4, 80);
        grinding(output, "snow_block_to_snowball", Ingredient.of(Items.SNOW_BLOCK), Items.SNOWBALL, 4, 80);
        grinding(output, "bone_to_bone_meal", Ingredient.of(Items.BONE), Items.BONE_MEAL, 6, 80);
        grinding(output, "blaze_rod_to_blaze_powder", Ingredient.of(Items.BLAZE_ROD), Items.BLAZE_POWDER, 5, 80);
        grinding(output, "wool_to_string", Ingredient.of(ItemTags.WOOL), Items.STRING, 4, 80);
        grinding(output, "iron_ore", Ingredient.of(Items.IRON_ORE), Items.RAW_IRON, 2, 120);
        grinding(output, "deepslate_iron_ore", Ingredient.of(Items.DEEPSLATE_IRON_ORE), Items.RAW_IRON, 2, 120);
        grinding(output, "copper_ore", Ingredient.of(Items.COPPER_ORE), Items.RAW_COPPER, 6, 120);
        grinding(output, "deepslate_copper_ore", Ingredient.of(Items.DEEPSLATE_COPPER_ORE), Items.RAW_COPPER, 6, 120);
        grinding(output, "gold_ore", Ingredient.of(Items.GOLD_ORE), Items.RAW_GOLD, 2, 120);
        grinding(output, "deepslate_gold_ore", Ingredient.of(Items.DEEPSLATE_GOLD_ORE), Items.RAW_GOLD, 2, 120);
        grinding(output, "coal_ore", Ingredient.of(Items.COAL_ORE), Items.COAL, 2, 120);
        grinding(output, "deepslate_coal_ore", Ingredient.of(Items.DEEPSLATE_COAL_ORE), Items.COAL, 2, 120);
        grinding(output, "redstone_ore", Ingredient.of(Items.REDSTONE_ORE), Items.REDSTONE, 8, 120);
        grinding(output, "deepslate_redstone_ore", Ingredient.of(Items.DEEPSLATE_REDSTONE_ORE), Items.REDSTONE, 8, 120);
        grinding(output, "lapis_ore", Ingredient.of(Items.LAPIS_ORE), Items.LAPIS_LAZULI, 12, 120);
        grinding(output, "deepslate_lapis_ore", Ingredient.of(Items.DEEPSLATE_LAPIS_ORE), Items.LAPIS_LAZULI, 12, 120);
        grinding(output, "diamond_ore", Ingredient.of(Items.DIAMOND_ORE), Items.DIAMOND, 2, 120);
        grinding(output, "deepslate_diamond_ore", Ingredient.of(Items.DEEPSLATE_DIAMOND_ORE), Items.DIAMOND, 2, 120);
        grinding(output, "emerald_ore", Ingredient.of(Items.EMERALD_ORE), Items.EMERALD, 2, 120);
        grinding(output, "deepslate_emerald_ore", Ingredient.of(Items.DEEPSLATE_EMERALD_ORE), Items.EMERALD, 2, 120);
        grinding(output, "nether_quartz_ore", Ingredient.of(Items.NETHER_QUARTZ_ORE), Items.QUARTZ, 2, 120);
        grinding(output, "nether_gold_ore", Ingredient.of(Items.NETHER_GOLD_ORE), Items.GOLD_NUGGET, 8, 120);
        grinding(output, "dandelion_to_yellow_dye", Ingredient.of(Items.DANDELION), Items.YELLOW_DYE, 2, 60);
        grinding(output, "poppy_to_red_dye", Ingredient.of(Items.POPPY), Items.RED_DYE, 2, 60);
        grinding(output, "blue_orchid_to_light_blue_dye", Ingredient.of(Items.BLUE_ORCHID), Items.LIGHT_BLUE_DYE, 2, 60);
        grinding(output, "allium_to_magenta_dye", Ingredient.of(Items.ALLIUM), Items.MAGENTA_DYE, 2, 60);
        grinding(output, "azure_bluet_to_light_gray_dye", Ingredient.of(Items.AZURE_BLUET), Items.LIGHT_GRAY_DYE, 2, 60);
        grinding(output, "red_tulip_to_red_dye", Ingredient.of(Items.RED_TULIP), Items.RED_DYE, 2, 60);
        grinding(output, "orange_tulip_to_orange_dye", Ingredient.of(Items.ORANGE_TULIP), Items.ORANGE_DYE, 2, 60);
        grinding(output, "white_tulip_to_light_gray_dye", Ingredient.of(Items.WHITE_TULIP), Items.LIGHT_GRAY_DYE, 2, 60);
        grinding(output, "pink_tulip_to_pink_dye", Ingredient.of(Items.PINK_TULIP), Items.PINK_DYE, 2, 60);
        grinding(output, "oxeye_daisy_to_light_gray_dye", Ingredient.of(Items.OXEYE_DAISY), Items.LIGHT_GRAY_DYE, 2, 60);
        grinding(output, "cornflower_to_blue_dye", Ingredient.of(Items.CORNFLOWER), Items.BLUE_DYE, 2, 60);
        grinding(output, "lily_of_the_valley_to_white_dye", Ingredient.of(Items.LILY_OF_THE_VALLEY), Items.WHITE_DYE, 2, 60);
        grinding(output, "wither_rose_to_black_dye", Ingredient.of(Items.WITHER_ROSE), Items.BLACK_DYE, 2, 60);
        grinding(output, "torchflower_to_orange_dye", Ingredient.of(Items.TORCHFLOWER), Items.ORANGE_DYE, 2, 60);
        grinding(output, "sunflower_to_yellow_dye", Ingredient.of(Items.SUNFLOWER), Items.YELLOW_DYE, 4, 60);
        grinding(output, "lilac_to_magenta_dye", Ingredient.of(Items.LILAC), Items.MAGENTA_DYE, 4, 60);
        grinding(output, "rose_bush_to_red_dye", Ingredient.of(Items.ROSE_BUSH), Items.RED_DYE, 4, 60);
        grinding(output, "peony_to_pink_dye", Ingredient.of(Items.PEONY), Items.PINK_DYE, 4, 60);
    }

    /** Le bloc accepte les lingots de l'étiquette commune (lingots équivalents d'autres mods compris). */
    private static void gate(RecipeOutput output, GateType type, String[] pattern) {
        ShapedRecipeBuilder builder = ShapedRecipeBuilder.shaped(RecipeCategory.REDSTONE,
                ModBlocks.LOGIC_GATE_ITEMS.get(type).get(), 2);
        for (String row : pattern) builder.pattern(row);
        builder.define('T', Items.REDSTONE_TORCH)
                .define('G', ModTags.Items.INGOTS_ROSE_GOLD)
                .define('W', ModBlocks.BARE_WIRE_ITEM.get())
                .unlockedBy("has_rose_gold_ingot", has(ModTags.Items.INGOTS_ROSE_GOLD))
                .save(output);
    }

    private static void storageBlock(RecipeOutput output, Item ingot, TagKey<Item> ingotTag, Item block,
                                     String unpackId) {
        ShapedRecipeBuilder.shaped(RecipeCategory.BUILDING_BLOCKS, block)
                .pattern("###")
                .pattern("###")
                .pattern("###")
                .define('#', ingotTag)
                .unlockedBy(getHasName(ingot), has(ingot))
                .save(output);

        ShapelessRecipeBuilder.shapeless(RecipeCategory.MISC, ingot, 9)
                .requires(block)
                .unlockedBy(getHasName(block), has(block))
                .save(output, ResourceLocation.fromNamespaceAndPath(Electra.MODID, unpackId));
    }

    /** Recette d'alliage : deux entrées (ordre indifférent) → une sortie, coût en FE. */
    private static void alloying(RecipeOutput output, String id, Ingredient input1, Ingredient input2,
                                 ItemLike result, int energyCost) {
        output.accept(ResourceLocation.fromNamespaceAndPath(Electra.MODID, id),
                new AlloyingRecipe(input1, input2, new ItemStack(result), energyCost),
                null);
    }

    /** Recette de broyage : un objet → une quantité d'un autre, coût en FE. */
    private static void grinding(RecipeOutput output, String id, Ingredient input, ItemLike result,
                                 int count, int energyCost) {
        output.accept(ResourceLocation.fromNamespaceAndPath(Electra.MODID, "grinding_" + id),
                new GrindingRecipe(input, new ItemStack(result, count), energyCost),
                null);
    }

    /** Recette de congélation : eau (mB) et objet facultatif → objet, coût en FE. */
    private static void freezing(RecipeOutput output, String id, Optional<Ingredient> input, int inputCount, int fluidAmount,
                                 ItemLike result, int energyCost) {
        output.accept(ResourceLocation.fromNamespaceAndPath(Electra.MODID, "freezing_" + id),
                new FreezingRecipe(input, inputCount, fluidAmount, new ItemStack(result), energyCost),
                null);
    }
}
