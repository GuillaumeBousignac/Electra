package com.electra.mod.recipe;

import com.electra.mod.setup.ModRecipes;
import net.minecraft.core.HolderLookup;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.crafting.*;
import net.minecraft.world.level.Level;
import org.jetbrains.annotations.NotNull;

/**
 * Teinture : un objet (n'importe quelle couleur de sa famille), un colorant et de l'eau (mB) donnent l'objet
 * de la couleur du colorant. Un objet déjà de la bonne couleur n'est pas retraité.
 */
public class DyeingRecipe implements Recipe<DyeingRecipeInput> {

    private final Ingredient input;
    private final Ingredient dye;
    private final int fluidAmount;
    private final ItemStack result;
    private final int energyCost;
    private final boolean keepComponents;

    public DyeingRecipe(Ingredient input, Ingredient dye, int fluidAmount, ItemStack result, int energyCost,
                        boolean keepComponents) {
        this.input = input;
        this.dye = dye;
        this.fluidAmount = fluidAmount;
        this.result = result;
        this.energyCost = energyCost;
        this.keepComponents = keepComponents;
    }

    @Override
    public boolean matches(DyeingRecipeInput in, @NotNull Level level) {
        return in.waterAmount() >= fluidAmount
                && input.test(in.item())
                && dye.test(in.dye())
                && !in.item().is(result.getItem());
    }

    @Override
    public @NotNull ItemStack assemble(@NotNull DyeingRecipeInput in, HolderLookup.@NotNull Provider registries) {
        ItemStack out = result.copy();
        // Boîtes de shulker et bannières : le contenu et les motifs suivent l'objet teint
        if (keepComponents) out.applyComponents(in.item().getComponentsPatch());
        return out;
    }

    @Override
    public boolean canCraftInDimensions(int width, int height) { return true; }

    @Override
    public @NotNull ItemStack getResultItem(HolderLookup.Provider registries) { return result.copy(); }

    @Override
    public @NotNull RecipeSerializer<?> getSerializer() { return ModRecipes.DYEING_SERIALIZER.get(); }

    @Override
    public @NotNull RecipeType<?> getType() { return ModRecipes.DYEING_TYPE.get(); }

    public Ingredient getInput() { return input; }
    public Ingredient getDye()   { return dye; }
    public int getFluidAmount()  { return fluidAmount; }
    public int getEnergyCost()   { return energyCost; }
    public boolean keepsComponents() { return keepComponents; }
}
