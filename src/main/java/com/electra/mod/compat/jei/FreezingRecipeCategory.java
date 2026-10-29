package com.electra.mod.compat.jei;

import com.electra.mod.Electra;
import com.electra.mod.config.ElectraConfig;
import com.electra.mod.recipe.FreezingRecipe;
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
import net.minecraft.world.level.material.Fluids;
import org.jetbrains.annotations.NotNull;

/** Catégorie JEI « Congélation » : eau (+ objet facultatif) → objet, avec le coût en FE. */
public class FreezingRecipeCategory implements IRecipeCategory<FreezingRecipe> {

    public static final RecipeType<FreezingRecipe> TYPE =
            RecipeType.create(Electra.MODID, "freezing", FreezingRecipe.class);

    private static final int WIDTH  = 98;
    private static final int HEIGHT = 44;

    private final IDrawable icon;
    private final IDrawable slot;
    private final IDrawableAnimated arrow;

    public FreezingRecipeCategory(IGuiHelper guiHelper) {
        this.icon  = guiHelper.createDrawableItemStack(new ItemStack(ModBlocks.FREEZER_ITEM.get()));
        this.slot  = guiHelper.getSlotDrawable();
        int time = ElectraConfig.SPEC.isLoaded() ? ElectraConfig.FREEZER_TIME.get() : 60;
        this.arrow = guiHelper.createAnimatedRecipeArrow(time);
    }

    @Override public @NotNull RecipeType<FreezingRecipe> getRecipeType() { return TYPE; }
    @Override public @NotNull Component getTitle() { return Component.translatable("jei.electra.freezing"); }
    @Override public IDrawable getIcon() { return icon; }
    @Override public int getWidth()  { return WIDTH; }
    @Override public int getHeight() { return HEIGHT; }

    @Override
    public void setRecipe(IRecipeLayoutBuilder builder, FreezingRecipe recipe, @NotNull IFocusGroup focuses) {
        builder.addSlot(RecipeIngredientRole.INPUT, 1, 1)
                .setBackground(slot, -1, -1)
                .addFluidStack(Fluids.WATER, recipe.getFluidAmount());

        if (recipe.hasInput()) {
            builder.addSlot(RecipeIngredientRole.INPUT, 19, 1)
                    .setBackground(slot, -1, -1)
                    .addItemStacks(java.util.Arrays.stream(recipe.getInput().get().getItems())
                            .map(stack -> stack.copyWithCount(recipe.getInputCount()))
                            .toList());
        }

        builder.addSlot(RecipeIngredientRole.OUTPUT, 77, 9)
                .setBackground(slot, -1, -1)
                .addItemStack(recipe.getResultItem(null));
    }

    @Override
    public void draw(@NotNull FreezingRecipe recipe, @NotNull IRecipeSlotsView slots,
                     @NotNull GuiGraphics graphics, double mouseX, double mouseY) {
        arrow.draw(graphics, 42, 9);

        Component cost = Component.translatable("jei.electra.energy", recipe.getEnergyCost());
        graphics.drawString(Minecraft.getInstance().font, cost, 0, HEIGHT - 9, 0xFF808080, false);
    }
}
