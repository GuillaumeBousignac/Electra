package com.electra.mod.compat.jei;

import com.electra.mod.Electra;
import com.electra.mod.recipe.AlloyingRecipe;
import com.electra.mod.recipe.MixingRecipe;
import com.electra.mod.recipe.OxidizingRecipe;
import com.electra.mod.recipe.DyeingRecipe;
import com.electra.mod.recipe.DebarkingRecipe;
import com.electra.mod.recipe.FreezingRecipe;
import com.electra.mod.recipe.GrindingRecipe;
import com.electra.mod.screen.AdvancedFurnaceScreen;
import com.electra.mod.screen.ConcreteMixerScreen;
import com.electra.mod.screen.OxidationStationScreen;
import com.electra.mod.screen.DyerScreen;
import com.electra.mod.screen.DebarkerScreen;
import com.electra.mod.screen.FreezerScreen;
import com.electra.mod.screen.GrinderScreen;
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
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.tags.ItemTags;
import net.minecraft.world.item.DyeColor;
import net.minecraft.world.item.DyeItem;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.component.DyedItemColor;
import net.minecraft.world.item.crafting.Ingredient;
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
        registration.addRecipeCategories(
                new AlloyingRecipeCategory(registration.getJeiHelpers().getGuiHelper()),
                new GrindingRecipeCategory(registration.getJeiHelpers().getGuiHelper()),
                new FreezingRecipeCategory(registration.getJeiHelpers().getGuiHelper()),
                new DebarkingRecipeCategory(registration.getJeiHelpers().getGuiHelper()),
                new MixingRecipeCategory(registration.getJeiHelpers().getGuiHelper()),
                new OxidizingRecipeCategory(registration.getJeiHelpers().getGuiHelper()),
                new DyeingRecipeCategory(registration.getJeiHelpers().getGuiHelper()));
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

        List<GrindingRecipe> grindings = level.getRecipeManager()
                .getAllRecipesFor(ModRecipes.GRINDING_TYPE.get())
                .stream()
                .map(RecipeHolder::value)
                .toList();
        registration.addRecipes(GrindingRecipeCategory.TYPE, grindings);

        List<FreezingRecipe> freezings = level.getRecipeManager()
                .getAllRecipesFor(ModRecipes.FREEZING_TYPE.get())
                .stream()
                .map(RecipeHolder::value)
                .toList();
        registration.addRecipes(FreezingRecipeCategory.TYPE, freezings);

        List<DebarkingRecipe> debarkings = level.getRecipeManager()
                .getAllRecipesFor(ModRecipes.DEBARKING_TYPE.get())
                .stream()
                .map(RecipeHolder::value)
                .toList();
        registration.addRecipes(DebarkingRecipeCategory.TYPE, debarkings);

        List<MixingRecipe> mixings = level.getRecipeManager()
                .getAllRecipesFor(ModRecipes.MIXING_TYPE.get())
                .stream()
                .map(RecipeHolder::value)
                .toList();
        registration.addRecipes(MixingRecipeCategory.TYPE, mixings);

        List<OxidizingRecipe> oxidizings = level.getRecipeManager()
                .getAllRecipesFor(ModRecipes.OXIDIZING_TYPE.get())
                .stream()
                .map(RecipeHolder::value)
                .toList();
        registration.addRecipes(OxidizingRecipeCategory.TYPE, oxidizings);

        List<DyeingRecipe> dyeings = new java.util.ArrayList<>(level.getRecipeManager()
                .getAllRecipesFor(ModRecipes.DYEING_TYPE.get())
                .stream()
                .map(RecipeHolder::value)
                .toList());

        // Armures en cuir et autres objets « dyeable » : exemple de mélange rouge + jaune (1 à 3 colorants possibles)
        DyeItem[] allDyes = java.util.Arrays.stream(DyeColor.values()).map(DyeItem::byColor).toArray(DyeItem[]::new);
        List<DyeItem> sample = List.of(DyeItem.byColor(DyeColor.RED), DyeItem.byColor(DyeColor.YELLOW));
        for (net.minecraft.world.item.Item item : BuiltInRegistries.ITEM) {
            ItemStack base = item.getDefaultInstance();
            if (base.is(ItemTags.DYEABLE)) {
                dyeings.add(new DyeingRecipe(Ingredient.of(item), Ingredient.of(allDyes), 100,
                        DyedItemColor.applyDyes(base, sample), 40, false));
            }
        }
        registration.addRecipes(DyeingRecipeCategory.TYPE, dyeings);
    }

    @Override
    public void registerRecipeCatalysts(@NotNull IRecipeCatalystRegistration registration) {
        // Le four avancé fait aussi la cuisson vanilla (four, haut fourneau, fumoir)
        registration.addRecipeCatalyst(new ItemStack(ModBlocks.ADVANCED_FURNACE_ITEM.get()),
                AlloyingRecipeCategory.TYPE, RecipeTypes.SMELTING, RecipeTypes.BLASTING, RecipeTypes.SMOKING);
        registration.addRecipeCatalyst(new ItemStack(ModBlocks.GRINDER_ITEM.get()), GrindingRecipeCategory.TYPE);
        registration.addRecipeCatalyst(new ItemStack(ModBlocks.FREEZER_ITEM.get()), FreezingRecipeCategory.TYPE);
        registration.addRecipeCatalyst(new ItemStack(ModBlocks.DEBARKER_ITEM.get()), DebarkingRecipeCategory.TYPE);
        registration.addRecipeCatalyst(new ItemStack(ModBlocks.CONCRETE_MIXER_ITEM.get()), MixingRecipeCategory.TYPE);
        registration.addRecipeCatalyst(new ItemStack(ModBlocks.OXIDATION_STATION_ITEM.get()), OxidizingRecipeCategory.TYPE);
        registration.addRecipeCatalyst(new ItemStack(ModBlocks.DYER_ITEM.get()), DyeingRecipeCategory.TYPE);
    }

    @Override
    public void registerGuiHandlers(@NotNull IGuiHandlerRegistration registration) {
        // Clic sur la flèche de l'interface → ouvre les recettes du four
        registration.addRecipeClickArea(AdvancedFurnaceScreen.class, 79, 34, 24, 16,
                AlloyingRecipeCategory.TYPE, RecipeTypes.SMELTING);
        registration.addRecipeClickArea(GrinderScreen.class, 79, 34, 24, 16, GrindingRecipeCategory.TYPE);
        registration.addRecipeClickArea(FreezerScreen.class, 79, 34, 24, 16, FreezingRecipeCategory.TYPE);
        registration.addRecipeClickArea(DebarkerScreen.class, 79, 34, 24, 16, DebarkingRecipeCategory.TYPE);
        registration.addRecipeClickArea(ConcreteMixerScreen.class, 79, 34, 24, 16, MixingRecipeCategory.TYPE);
        registration.addRecipeClickArea(OxidationStationScreen.class, 79, 34, 24, 16, OxidizingRecipeCategory.TYPE);
        registration.addRecipeClickArea(DyerScreen.class, 79, 34, 24, 16, DyeingRecipeCategory.TYPE);
    }
}
