package com.electra.mod.datagen;

import com.electra.mod.Electra;
import com.electra.mod.block.GateType;
import com.electra.mod.recipe.AlloyingRecipe;
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
import net.minecraft.tags.TagKey;
import net.minecraft.world.item.DyeColor;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.Items;
import net.minecraft.world.item.crafting.Ingredient;
import net.minecraft.world.level.ItemLike;
import net.neoforged.neoforge.common.Tags;
import org.jetbrains.annotations.NotNull;

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

        ShapedRecipeBuilder.shaped(RecipeCategory.MISC, ModBlocks.REDSTONE_DYNAMO_ITEM.get())
                .pattern("CCC")
                .pattern("RWR")
                .pattern("CCC")
                .define('C', Tags.Items.INGOTS_COPPER)
                .define('R', ModTags.Items.INGOTS_RED_IRON)
                .define('W', bareWire)
                .unlockedBy("has_red_iron_ingot", has(ModTags.Items.INGOTS_RED_IRON))
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
}
