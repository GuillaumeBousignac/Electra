package com.electra.mod.setup;

import com.electra.mod.Electra;
import com.electra.mod.recipe.AlloyingRecipe;
import com.electra.mod.recipe.AlloyingRecipeSerializer;
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
}