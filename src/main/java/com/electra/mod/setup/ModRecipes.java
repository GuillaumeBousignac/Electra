package com.electra.mod.setup;

import com.electra.mod.Electra;
import com.electra.mod.recipe.AlloyingRecipe;
import com.electra.mod.recipe.AlloyingRecipeSerializer;
import com.electra.mod.recipe.MixingRecipe;
import com.electra.mod.recipe.MixingRecipeSerializer;
import com.electra.mod.recipe.OxidizingRecipe;
import com.electra.mod.recipe.OxidizingRecipeSerializer;
import com.electra.mod.recipe.DyeingRecipe;
import com.electra.mod.recipe.DyeingRecipeSerializer;
import com.electra.mod.recipe.DebarkingRecipe;
import com.electra.mod.recipe.DebarkingRecipeSerializer;
import com.electra.mod.recipe.FreezingRecipe;
import com.electra.mod.recipe.FreezingRecipeSerializer;
import com.electra.mod.recipe.GrindingRecipe;
import com.electra.mod.recipe.GrindingRecipeSerializer;
import net.minecraft.core.registries.Registries;
import net.minecraft.world.item.crafting.RecipeSerializer;
import net.minecraft.world.item.crafting.RecipeType;
import net.neoforged.neoforge.registries.DeferredHolder;
import net.neoforged.neoforge.registries.DeferredRegister;

public class ModRecipes {

    public static final DeferredRegister<RecipeSerializer<?>> SERIALIZERS =
            DeferredRegister.create(Registries.RECIPE_SERIALIZER, Electra.MODID);

    public static final DeferredRegister<RecipeType<?>> TYPES =
            DeferredRegister.create(Registries.RECIPE_TYPE, Electra.MODID);

    public static final DeferredHolder<RecipeType<?>, RecipeType<AlloyingRecipe>> ALLOYING_TYPE =
            TYPES.register("alloying", () -> RecipeType.simple(
                    net.minecraft.resources.ResourceLocation.fromNamespaceAndPath(Electra.MODID, "alloying")));

    public static final DeferredHolder<RecipeSerializer<?>, AlloyingRecipeSerializer> ALLOYING_SERIALIZER =
            SERIALIZERS.register("alloying", AlloyingRecipeSerializer::new);

    public static final DeferredHolder<RecipeType<?>, RecipeType<GrindingRecipe>> GRINDING_TYPE =
            TYPES.register("grinding", () -> RecipeType.simple(
                    net.minecraft.resources.ResourceLocation.fromNamespaceAndPath(Electra.MODID, "grinding")));

    public static final DeferredHolder<RecipeSerializer<?>, GrindingRecipeSerializer> GRINDING_SERIALIZER =
            SERIALIZERS.register("grinding", GrindingRecipeSerializer::new);

    public static final DeferredHolder<RecipeType<?>, RecipeType<FreezingRecipe>> FREEZING_TYPE =
            TYPES.register("freezing", () -> RecipeType.simple(
                    net.minecraft.resources.ResourceLocation.fromNamespaceAndPath(Electra.MODID, "freezing")));

    public static final DeferredHolder<RecipeSerializer<?>, FreezingRecipeSerializer> FREEZING_SERIALIZER =
            SERIALIZERS.register("freezing", FreezingRecipeSerializer::new);

    public static final DeferredHolder<RecipeType<?>, RecipeType<DebarkingRecipe>> DEBARKING_TYPE =
            TYPES.register("debarking", () -> RecipeType.simple(
                    net.minecraft.resources.ResourceLocation.fromNamespaceAndPath(Electra.MODID, "debarking")));

    public static final DeferredHolder<RecipeSerializer<?>, DebarkingRecipeSerializer> DEBARKING_SERIALIZER =
            SERIALIZERS.register("debarking", DebarkingRecipeSerializer::new);

    public static final DeferredHolder<RecipeType<?>, RecipeType<MixingRecipe>> MIXING_TYPE =
            TYPES.register("mixing", () -> RecipeType.simple(
                    net.minecraft.resources.ResourceLocation.fromNamespaceAndPath(Electra.MODID, "mixing")));

    public static final DeferredHolder<RecipeSerializer<?>, MixingRecipeSerializer> MIXING_SERIALIZER =
            SERIALIZERS.register("mixing", MixingRecipeSerializer::new);

    public static final DeferredHolder<RecipeType<?>, RecipeType<OxidizingRecipe>> OXIDIZING_TYPE =
            TYPES.register("oxidizing", () -> RecipeType.simple(
                    net.minecraft.resources.ResourceLocation.fromNamespaceAndPath(Electra.MODID, "oxidizing")));

    public static final DeferredHolder<RecipeSerializer<?>, OxidizingRecipeSerializer> OXIDIZING_SERIALIZER =
            SERIALIZERS.register("oxidizing", OxidizingRecipeSerializer::new);

    public static final DeferredHolder<RecipeType<?>, RecipeType<DyeingRecipe>> DYEING_TYPE =
            TYPES.register("dyeing", () -> RecipeType.simple(
                    net.minecraft.resources.ResourceLocation.fromNamespaceAndPath(Electra.MODID, "dyeing")));

    public static final DeferredHolder<RecipeSerializer<?>, DyeingRecipeSerializer> DYEING_SERIALIZER =
            SERIALIZERS.register("dyeing", DyeingRecipeSerializer::new);
}
