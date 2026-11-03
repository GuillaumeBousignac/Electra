package com.electra.mod.recipe;

import com.electra.mod.setup.ModRecipes;
import net.minecraft.core.HolderLookup;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.crafting.*;
import net.minecraft.world.level.Level;
import org.jetbrains.annotations.NotNull;

/**
 * Oxydation : un objet (en quantité donnée) et de l'eau (mB) donnent un objet.
 * Cuivre → cuivre exposé → patiné → oxydé, pierre → pierre moussue, fer → pigment rouge, etc.
 */
public class OxidizingRecipe implements Recipe<OxidizingRecipeInput> {

    private final Ingredient input;
    private final int inputCount;
    private final int fluidAmount;
    private final ItemStack result;
    private final int energyCost;

    public OxidizingRecipe(Ingredient input, int inputCount, int fluidAmount, ItemStack result, int energyCost) {
        this.input = input;
        this.inputCount = inputCount;
        this.fluidAmount = fluidAmount;
        this.result = result;
        this.energyCost = energyCost;
    }

    @Override
    public boolean matches(OxidizingRecipeInput in, @NotNull Level level) {
        if (in.waterAmount() < fluidAmount) return false;
        return input.test(in.item()) && in.item().getCount() >= inputCount;
    }

    @Override
    public @NotNull ItemStack assemble(@NotNull OxidizingRecipeInput in, HolderLookup.@NotNull Provider registries) {
        return result.copy();
    }

    @Override
    public boolean canCraftInDimensions(int width, int height) { return true; }

    @Override
    public @NotNull ItemStack getResultItem(HolderLookup.Provider registries) { return result.copy(); }

    @Override
    public @NotNull RecipeSerializer<?> getSerializer() { return ModRecipes.OXIDIZING_SERIALIZER.get(); }

    @Override
    public @NotNull RecipeType<?> getType() { return ModRecipes.OXIDIZING_TYPE.get(); }

    public Ingredient getInput()           { return input; }
    public int getInputCount()             { return inputCount; }
    public int getFluidAmount()            { return fluidAmount; }
    public int getEnergyCost()             { return energyCost; }
}
