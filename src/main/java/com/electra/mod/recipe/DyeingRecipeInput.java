package com.electra.mod.recipe;

import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.crafting.RecipeInput;
import org.jetbrains.annotations.NotNull;

/** Contenu de la teinturière : l'objet à teindre, le colorant et la quantité d'eau (mB) dans la cuve. */
public record DyeingRecipeInput(ItemStack item, ItemStack dye, int waterAmount) implements RecipeInput {

    @Override
    public @NotNull ItemStack getItem(int index) {
        return switch (index) {
            case 0 -> item;
            case 1 -> dye;
            default -> ItemStack.EMPTY;
        };
    }

    @Override
    public int size() { return 2; }
}
