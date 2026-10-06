package com.electra.mod.recipe;

import com.electra.mod.setup.ModRecipes;
import net.minecraft.core.HolderLookup;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.crafting.*;
import net.minecraft.world.level.Level;
import org.jetbrains.annotations.NotNull;

public class AlloyingRecipe implements Recipe<AlloyingRecipeInput> {

    private final Ingredient input1;
    private final Ingredient input2;
    private final ItemStack result;
    private final int energyCost;

    public AlloyingRecipe(Ingredient input1, Ingredient input2, ItemStack result, int energyCost) {
        this.input1 = input1;
        this.input2 = input2;
        this.result = result;
        this.energyCost = energyCost;
    }

    @Override
    public boolean matches(AlloyingRecipeInput input, @NotNull Level level) {
        return (input1.test(input.item1()) && input2.test(input.item2()))
                || (input1.test(input.item2()) && input2.test(input.item1()));
    }

    @Override
    public @NotNull ItemStack assemble(@NotNull AlloyingRecipeInput input, HolderLookup.@NotNull Provider registries) {
        return result.copy();
    }

    @Override
    public boolean canCraftInDimensions(int width, int height) { return true; }

    @Override
    public @NotNull ItemStack getResultItem(HolderLookup. Provider registries) { return result.copy(); }

    @Override
    public @NotNull RecipeSerializer<?> getSerializer() { return ModRecipes.ALLOYING_SERIALIZER.get(); }

    @Override
    public @NotNull RecipeType<?> getType() { return ModRecipes.ALLOYING_TYPE.get(); }

    public Ingredient getInput1() { return input1; }
    public Ingredient getInput2() { return input2; }
    public int getEnergyCost()    { return energyCost; }
}