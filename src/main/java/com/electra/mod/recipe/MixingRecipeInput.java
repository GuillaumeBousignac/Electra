package com.electra.mod.recipe;

import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.crafting.RecipeInput;
import org.jetbrains.annotations.NotNull;

/** Contenu de la bétonnière : l'objet de la case d'entrée et la quantité d'eau (mB) dans la cuve. */
public record MixingRecipeInput(ItemStack item, int waterAmount) implements RecipeInput {

    @Override
    public @NotNull ItemStack getItem(int index) { return index == 0 ? item : ItemStack.EMPTY; }

    @Override
    public int size() { return 1; }
}
