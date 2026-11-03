package com.electra.mod.recipe;

import com.electra.mod.setup.ModRecipes;
import net.minecraft.core.HolderLookup;
import net.minecraft.core.NonNullList;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.crafting.*;
import net.minecraft.world.level.Level;
import org.jetbrains.annotations.NotNull;

/** Écorçage : un tronc → le même tronc écorcé, coût en FE défini par la recette. */
public class DebarkingRecipe implements Recipe<SingleRecipeInput> {

    private final Ingredient input;
    private final ItemStack result;
    private final int energyCost;

    public DebarkingRecipe(Ingredient input, ItemStack result, int energyCost) {
        this.input = input;
        this.result = result;
        this.energyCost = energyCost;
    }

    @Override
    public boolean matches(SingleRecipeInput recipeInput, @NotNull Level level) {
        return input.test(recipeInput.item());
    }

    @Override
    public @NotNull ItemStack assemble(@NotNull SingleRecipeInput recipeInput, HolderLookup.@NotNull Provider registries) {
        return result.copy();
    }

    @Override
    public boolean canCraftInDimensions(int width, int height) { return true; }

    @Override
    public @NotNull ItemStack getResultItem(HolderLookup.Provider registries) { return result.copy(); }

    @Override
    public @NotNull NonNullList<Ingredient> getIngredients() {
        NonNullList<Ingredient> list = NonNullList.create();
        list.add(input);
        return list;
    }

    @Override
    public @NotNull RecipeSerializer<?> getSerializer() { return ModRecipes.DEBARKING_SERIALIZER.get(); }

    @Override
    public @NotNull RecipeType<?> getType() { return ModRecipes.DEBARKING_TYPE.get(); }

    public Ingredient getInput() { return input; }
    public int getEnergyCost()   { return energyCost; }
}
