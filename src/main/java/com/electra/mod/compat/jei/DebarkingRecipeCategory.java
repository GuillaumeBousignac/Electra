package com.electra.mod.compat.jei;

import com.electra.mod.Electra;
import com.electra.mod.config.ElectraConfig;
import com.electra.mod.recipe.DebarkingRecipe;
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

/** Catégorie JEI « Écorçage » : une entrée → une sortie, avec le coût en FE. */
public class DebarkingRecipeCategory implements IRecipeCategory<DebarkingRecipe> {

    public static final RecipeType<DebarkingRecipe> TYPE =
            RecipeType.create(Electra.MODID, "debarking", DebarkingRecipe.class);

    private static final int WIDTH  = 82;
    private static final int HEIGHT = 44;

    private final IDrawable icon;
    private final IDrawable slot;
    private final IDrawableAnimated arrow;

    public DebarkingRecipeCategory(IGuiHelper guiHelper) {
        this.icon  = guiHelper.createDrawableItemStack(new ItemStack(ModBlocks.DEBARKER_ITEM.get()));
        this.slot  = guiHelper.getSlotDrawable();
        int time = ElectraConfig.SPEC.isLoaded() ? ElectraConfig.DEBARKER_TIME.get() : 30;
        this.arrow = guiHelper.createAnimatedRecipeArrow(time);
    }

    @Override public @NotNull RecipeType<DebarkingRecipe> getRecipeType() { return TYPE; }
    @Override public @NotNull Component getTitle() { return Component.translatable("jei.electra.debarking"); }
    @Override public IDrawable getIcon() { return icon; }
    @Override public int getWidth()  { return WIDTH; }
    @Override public int getHeight() { return HEIGHT; }

    @Override
    public void setRecipe(IRecipeLayoutBuilder builder, DebarkingRecipe recipe, @NotNull IFocusGroup focuses) {
        builder.addSlot(RecipeIngredientRole.INPUT, 1, 9)
                .setBackground(slot, -1, -1)
                .addIngredients(recipe.getInput());

        builder.addSlot(RecipeIngredientRole.OUTPUT, 61, 9)
                .setBackground(slot, -1, -1)
                .addItemStack(recipe.getResultItem(null));
    }

    @Override
    public void draw(@NotNull DebarkingRecipe recipe, @NotNull IRecipeSlotsView slots,
                     @NotNull GuiGraphics graphics, double mouseX, double mouseY) {
        arrow.draw(graphics, 26, 9);

        Component cost = Component.translatable("jei.electra.energy", recipe.getEnergyCost());
        graphics.drawString(Minecraft.getInstance().font, cost, 0, HEIGHT - 9, 0xFF808080, false);
    }
}
