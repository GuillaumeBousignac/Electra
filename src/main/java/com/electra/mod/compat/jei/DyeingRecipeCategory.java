package com.electra.mod.compat.jei;

import com.electra.mod.Electra;
import com.electra.mod.config.ElectraConfig;
import com.electra.mod.recipe.DyeingRecipe;
import com.electra.mod.setup.ModBlocks;
import mezz.jei.api.gui.builder.IRecipeLayoutBuilder;
import mezz.jei.api.gui.drawable.IDrawable;
import mezz.jei.api.gui.drawable.IDrawableAnimated;
import mezz.jei.api.gui.ingredient.IRecipeSlotsView;
import mezz.jei.api.helpers.IGuiHelper;
import mezz.jei.api.recipe.IFocusGroup;
import mezz.jei.api.recipe.RecipeIngredientRole;
import mezz.jei.api.recipe.RecipeType;
import mezz.jei.api.recipe.category.IRecipeCategory;
import net.minecraft.client.Minecraft;
import net.minecraft.core.component.DataComponents;
import net.minecraft.world.item.Items;
import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.network.chat.Component;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.material.Fluids;
import org.jetbrains.annotations.NotNull;

/** Catégorie JEI « Teinture » : objet + colorant + eau → objet teint, avec le coût en FE. */
public class DyeingRecipeCategory implements IRecipeCategory<DyeingRecipe> {

    public static final RecipeType<DyeingRecipe> TYPE =
            RecipeType.create(Electra.MODID, "dyeing", DyeingRecipe.class);

    private static final int WIDTH  = 116;
    private static final int HEIGHT = 66;

    private final IDrawable icon;
    private final IDrawable slot;
    private final IDrawableAnimated arrow;

    public DyeingRecipeCategory(IGuiHelper guiHelper) {
        this.icon  = guiHelper.createDrawableItemStack(new ItemStack(ModBlocks.DYER_ITEM.get()));
        this.slot  = guiHelper.getSlotDrawable();
        int time = ElectraConfig.SPEC.isLoaded() ? ElectraConfig.DYER_TIME.get() : 60;
        this.arrow = guiHelper.createAnimatedRecipeArrow(time);
    }

    @Override public @NotNull RecipeType<DyeingRecipe> getRecipeType() { return TYPE; }
    @Override public @NotNull Component getTitle() { return Component.translatable("jei.electra.dyeing"); }
    @Override public IDrawable getIcon() { return icon; }
    @Override public int getWidth()  { return WIDTH; }
    @Override public int getHeight() { return HEIGHT; }

    @Override
    public void setRecipe(IRecipeLayoutBuilder builder, DyeingRecipe recipe, @NotNull IFocusGroup focuses) {
        // Même disposition que l'interface de la machine : eau, objet, colonne de 3 colorants, flèche, sortie
        builder.addSlot(RecipeIngredientRole.INPUT, 1, 19)
                .setBackground(slot, -1, -1)
                .addFluidStack(Fluids.WATER, recipe.getFluidAmount());

        builder.addSlot(RecipeIngredientRole.INPUT, 19, 19)
                .setBackground(slot, -1, -1)
                .addIngredients(recipe.getInput());

        ItemStack result = recipe.getResultItem(null);
        var dye1 = builder.addSlot(RecipeIngredientRole.INPUT, 37, 1).setBackground(slot, -1, -1);
        var dye2 = builder.addSlot(RecipeIngredientRole.INPUT, 37, 19).setBackground(slot, -1, -1);
        builder.addSlot(RecipeIngredientRole.INPUT, 37, 37).setBackground(slot, -1, -1);
        if (result.has(DataComponents.DYED_COLOR)) {
            // Exemple de mélange pour les armures en cuir : rouge + jaune (jusqu'à 3 colorants)
            dye1.addItemStack(new ItemStack(Items.RED_DYE));
            dye2.addItemStack(new ItemStack(Items.YELLOW_DYE));
        } else {
            dye2.addIngredients(recipe.getDye());
        }

        builder.addSlot(RecipeIngredientRole.OUTPUT, 95, 19)
                .setBackground(slot, -1, -1)
                .addItemStack(result);
    }

    @Override
    public void draw(@NotNull DyeingRecipe recipe, @NotNull IRecipeSlotsView slots,
                     @NotNull GuiGraphics graphics, double mouseX, double mouseY) {
        arrow.draw(graphics, 60, 19);

        Component cost = Component.translatable("jei.electra.energy", recipe.getEnergyCost());
        graphics.drawString(Minecraft.getInstance().font, cost, 0, HEIGHT - 9, 0xFF808080, false);
    }
}
