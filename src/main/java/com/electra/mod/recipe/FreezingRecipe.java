package com.electra.mod.recipe;

import com.electra.mod.setup.ModRecipes;
import net.minecraft.core.HolderLookup;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.crafting.*;
import net.minecraft.world.level.Level;
import org.jetbrains.annotations.NotNull;

import java.util.Optional;

/**
 * Congélation : de l'eau (mB), plus éventuellement un objet, donne un objet.
 * Sans objet d'entrée, l'eau seule suffit (eau → glace). Avec un objet, il est consommé (glace → glace compacte).
 */
public class FreezingRecipe implements Recipe<FreezingRecipeInput> {

    private final Optional<Ingredient> input;
    private final int inputCount;
    private final int fluidAmount;
    private final ItemStack result;
    private final int energyCost;

    public FreezingRecipe(Optional<Ingredient> input, int inputCount, int fluidAmount, ItemStack result, int energyCost) {
        this.input = input;
        this.inputCount = inputCount;
        this.fluidAmount = fluidAmount;
        this.result = result;
        this.energyCost = energyCost;
    }

    @Override
    public boolean matches(FreezingRecipeInput in, @NotNull Level level) {
        if (in.waterAmount() < fluidAmount) return false;
        return input.map(ingredient -> ingredient.test(in.item()) && in.item().getCount() >= inputCount).orElse(true);
    }

    @Override
    public @NotNull ItemStack assemble(@NotNull FreezingRecipeInput in, HolderLookup.@NotNull Provider registries) {
        return result.copy();
    }

    @Override
    public boolean canCraftInDimensions(int width, int height) { return true; }

    @Override
    public @NotNull ItemStack getResultItem(HolderLookup.Provider registries) { return result.copy(); }

    @Override
    public @NotNull RecipeSerializer<?> getSerializer() { return ModRecipes.FREEZING_SERIALIZER.get(); }

    @Override
    public @NotNull RecipeType<?> getType() { return ModRecipes.FREEZING_TYPE.get(); }

    public Optional<Ingredient> getInput() { return input; }
    public boolean hasInput()              { return input.isPresent(); }
    public int getInputCount()             { return inputCount; }
    public int getFluidAmount()            { return fluidAmount; }
    public int getEnergyCost()             { return energyCost; }
}
