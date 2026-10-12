package com.electra.mod.compat.jei;

import com.electra.mod.Electra;
import com.electra.mod.recipe.AlloyingRecipe;
import com.electra.mod.screen.AdvancedFurnaceScreen;
import com.electra.mod.setup.ModBlocks;
import com.electra.mod.setup.ModRecipes;
import mezz.jei.api.IModPlugin;
import mezz.jei.api.JeiPlugin;
import mezz.jei.api.constants.RecipeTypes;
import mezz.jei.api.registration.IGuiHandlerRegistration;
import mezz.jei.api.registration.IRecipeCatalystRegistration;
import mezz.jei.api.registration.IRecipeCategoryRegistration;
import mezz.jei.api.registration.IRecipeRegistration;
import net.minecraft.client.Minecraft;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.crafting.RecipeHolder;
import org.jetbrains.annotations.NotNull;

import java.util.List;

/**
 * Point d'entrée JEI. Détecté automatiquement par l'annotation @JeiPlugin :
 * cette classe n'est chargée que si JEI est installé (dépendance optionnelle).
 */
@JeiPlugin
public class ElectraJeiPlugin implements IModPlugin {

    private static final ResourceLocation ID = ResourceLocation.fromNamespaceAndPath(Electra.MODID, "jei_plugin");

    @Override
    public @NotNull ResourceLocation getPluginUid() { return ID; }

    @Override
    public void registerCategories(IRecipeCategoryRegistration registration) {
        registration.addRecipeCategories(new AlloyingRecipeCategory(registration.getJeiHelpers().getGuiHelper()));
    }

    @Override
    public void registerRecipes(@NotNull IRecipeRegistration registration) {
        var level = Minecraft.getInstance().level;
        if (level == null) return;

        // Lit les recettes réellement chargées : celles des datapacks et de KubeJS apparaissent aussi.
        List<AlloyingRecipe> recipes = level.getRecipeManager()
                .getAllRecipesFor(ModRecipes.ALLOYING_TYPE.get())
                .stream()
                .map(RecipeHolder::value)
                .toList();

        registration.addRecipes(AlloyingRecipeCategory.TYPE, recipes);
    }

    @Override
    public void registerRecipeCatalysts(@NotNull IRecipeCatalystRegistration registration) {
        // Le four avancé fait aussi la cuisson vanilla (four, haut fourneau, fumoir)
        registration.addRecipeCatalyst(new ItemStack(ModBlocks.ADVANCED_FURNACE_ITEM.get()),
                AlloyingRecipeCategory.TYPE, RecipeTypes.SMELTING, RecipeTypes.BLASTING, RecipeTypes.SMOKING);
    }

    @Override
    public void registerGuiHandlers(@NotNull IGuiHandlerRegistration registration) {
        // Clic sur la flèche de l'interface → ouvre les recettes du four
        registration.addRecipeClickArea(AdvancedFurnaceScreen.class, 79, 34, 24, 16,
                AlloyingRecipeCategory.TYPE, RecipeTypes.SMELTING);
    }
}
