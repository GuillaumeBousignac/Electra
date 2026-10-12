package com.electra.mod.compat.jei;

import com.electra.mod.Electra;
import com.electra.mod.recipe.AlloyingRecipe;
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
import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.network.chat.Component;
import net.minecraft.world.item.ItemStack;
import org.jetbrains.annotations.NotNull;

/**
 * Catégorie JEI « Alliage » : deux entrées → une sortie, avec le coût en FE.
 * Disposition calquée sur l'interface du four avancé (entrées empilées, flèche, sortie).
 */
public class AlloyingRecipeCategory implements IRecipeCategory<AlloyingRecipe> {

    public static final RecipeType<AlloyingRecipe> TYPE =
            RecipeType.create(Electra.MODID, "alloying", AlloyingRecipe.class);

    private static final int WIDTH  = 82;
    private static final int HEIGHT = 54;

    private final IDrawable icon;
    private final IDrawable slot;
    private final IDrawableAnimated arrow;

    public AlloyingRecipeCategory(IGuiHelper guiHelper) {
        this.icon  = guiHelper.createDrawableItemStack(new ItemStack(ModBlocks.ADVANCED_FURNACE_ITEM.get()));
        this.slot  = guiHelper.getSlotDrawable();
        // Même durée que la cuisson réelle (AdvancedFurnaceBlockEntity.COOK_TIME_TOTAL = 25 ticks)
        this.arrow = guiHelper.createAnimatedRecipeArrow(25);
    }

    @Override public @NotNull RecipeType<AlloyingRecipe> getRecipeType() { return TYPE; }
    @Override public @NotNull Component getTitle() { return Component.translatable("jei.electra.alloying"); }
    @Override public IDrawable getIcon() { return icon; }
    @Override public int getWidth()  { return WIDTH; }
    @Override public int getHeight() { return HEIGHT; }

    @Override
    public void setRecipe(IRecipeLayoutBuilder builder, AlloyingRecipe recipe, @NotNull IFocusGroup focuses) {
        builder.addSlot(RecipeIngredientRole.INPUT, 1, 1)
                .setBackground(slot, -1, -1)
                .addIngredients(recipe.getInput1());

        builder.addSlot(RecipeIngredientRole.INPUT, 1, 26)
                .setBackground(slot, -1, -1)
                .addIngredients(recipe.getInput2());

        builder.addSlot(RecipeIngredientRole.OUTPUT, 61, 14)
                .setBackground(slot, -1, -1)
                .addItemStack(recipe.getResultItem(null));
    }

    @Override
    public void draw(@NotNull AlloyingRecipe recipe, @NotNull IRecipeSlotsView slots,
                     @NotNull GuiGraphics graphics, double mouseX, double mouseY) {
        arrow.draw(graphics, 26, 14);

        Component cost = Component.translatable("jei.electra.energy", recipe.getEnergyCost());
        graphics.drawString(Minecraft.getInstance().font, cost, 0, HEIGHT - 9, 0xFF808080, false);
    }
}
