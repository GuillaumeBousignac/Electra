package com.electra.mod.recipe;

import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.crafting.RecipeInput;
import org.jetbrains.annotations.NotNull;

public record AlloyingRecipeInput(ItemStack item1, ItemStack item2) implements RecipeInput {

    @Override
    public @NotNull ItemStack getItem(int index) {
        return switch (index) {
            case 0 -> item1;
            case 1 -> item2;
            default -> ItemStack.EMPTY;
        };
    }

    @Override
    public int size() { return 2; }
}