package com.electra.mod.screen;

import com.electra.mod.Electra;
import com.electra.mod.menu.AdvancedFurnaceMenu;
import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.client.gui.screens.inventory.AbstractContainerScreen;
import net.minecraft.network.chat.Component;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.util.Mth;
import net.minecraft.world.entity.player.Inventory;
import org.jetbrains.annotations.NotNull;

public class AdvancedFurnaceScreen extends AbstractContainerScreen<AdvancedFurnaceMenu> {

    private static final ResourceLocation TEXTURE =
            ResourceLocation.fromNamespaceAndPath(Electra.MODID, "textures/gui/advanced_furnace.png");

    // Flèche pleine du four vanilla (même image, même position, même animation)
    private static final ResourceLocation BURN_PROGRESS_SPRITE =
            ResourceLocation.withDefaultNamespace("container/furnace/burn_progress");
    // Jauge d'énergie dessinée par code dans la zone vide à gauche
    private static final int BAR_X = 12, BAR_Y = 17, BAR_W = 10, BAR_H = 52;

    public AdvancedFurnaceScreen(AdvancedFurnaceMenu menu, Inventory playerInventory, Component title) {
        super(menu, playerInventory, title);
        this.imageWidth = 176;
        this.imageHeight = 166;
        this.inventoryLabelY = 72;
    }

    @Override
    protected void renderBg(@NotNull GuiGraphics graphics, float partialTick, int mouseX, int mouseY) {
        graphics.blit(TEXTURE, leftPos, topPos, 0, 0, imageWidth, imageHeight, 176, 166);

        // Progression : flèche blanche identique au four vanilla
        int total = menu.getCookTimeTotal();
        int cook = menu.getCookTime();
        if (total > 0 && cook > 0) {
            int width = Mth.ceil(cook * 24.0f / total);
            graphics.blitSprite(BURN_PROGRESS_SPRITE, 24, 16, 0, 0, leftPos + 79, topPos + 34, width, 16);
        }

        // Jauge FE bleu/blanc (direction artistique)
        int x = leftPos + BAR_X;
        int y = topPos + BAR_Y;
        graphics.fill(x - 1, y - 1, x + BAR_W + 1, y + BAR_H + 1, 0xFF373737);
        graphics.fill(x, y, x + BAR_W, y + BAR_H, 0xFF1A1A1A);
        int height = (int) ((long) menu.getEnergy() * BAR_H / menu.getCapacity());
        if (height > 0) {
            graphics.fillGradient(x, y + BAR_H - height, x + BAR_W, y + BAR_H, 0xFFE8F6FF, 0xFF3A8DDE);
        }
    }

    @Override
    public void render(@NotNull GuiGraphics graphics, int mouseX, int mouseY, float delta) {
        super.render(graphics, mouseX, mouseY, delta);
        if (isHovering(BAR_X, BAR_Y, BAR_W, BAR_H, mouseX, mouseY)) {
            graphics.renderTooltip(font,
                    Component.translatable("gui.electra.energy", menu.getEnergy(), menu.getCapacity()),
                    mouseX, mouseY);
        } else {
            renderTooltip(graphics, mouseX, mouseY);
        }
    }
}
