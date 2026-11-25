package com.electra.mod.client;

import com.electra.mod.Electra;
import com.electra.mod.guide.GuideContent;
import com.mojang.blaze3d.platform.InputConstants;
import com.mojang.blaze3d.platform.NativeImage;
import net.minecraft.ChatFormatting;
import net.minecraft.Util;
import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.client.gui.components.Button;
import net.minecraft.client.gui.components.Tooltip;
import net.minecraft.client.gui.screens.ConfirmLinkScreen;
import net.minecraft.client.gui.screens.Screen;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.network.chat.Component;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.sounds.SoundEvents;
import net.minecraft.util.FormattedCharSequence;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.Items;
import org.jetbrains.annotations.NotNull;

import java.io.InputStream;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * Ecran du livre-guide, entierement dessine par le code :
 * liste des chapitres a gauche, contenu pagine automatiquement a droite (texte, images, recettes).
 */
public class GuideBookScreen extends Screen {

    // Derniere position lue, conservee d'une ouverture a l'autre
    private static int lastChapter = 0;
    private static int lastPage = 0;

    // Palette : cuivre (cadre), parchemin (fond), bleu FE (filet)
    private static final int COPPER = 0xFFB87333;
    private static final int COPPER_DARK = 0xFF5A3418;
    private static final int PARCHMENT = 0xFFF3E7C9;
    private static final int SIDEBAR = 0xFFE5D3AA;
    private static final int SELECTED = 0xFFD2B77F;
    private static final int TEXT = 0xFF3B2A1A;
    private static final int HEADING = 0xFF8A4B14;
    private static final int FE_BLUE = 0xFF3A8DDE;

    private static final int MAX_WIDTH = 340;
    private static final int MAX_HEIGHT = 220;
    private static final int SIDE_WIDTH = 96;
    private static final int LINE_HEIGHT = 10;
    private static final int ROW_HEIGHT = 14;
    private static final int SLOT = 18;

    private static final String IMAGE_FOLDER = "textures/gui/guide/";

    private static final String DISCORD_URL = "https://discord.gg/bqTMwC2GzR";
    // Remplacable par n'importe quel PNG carre (reduit a 14 x 14 a l'affichage)
    private static final ResourceLocation DISCORD_ICON =
            ResourceLocation.fromNamespaceAndPath(Electra.MODID, IMAGE_FOLDER + "discord_icon.png");

    /** Dessine un bloc a la position donnee ; la souris sert a reperer l'objet survole. */
    private interface Drawer {
        void draw(GuiGraphics graphics, int x, int y, int mouseX, int mouseY);
    }

    /** Un bloc de page (ligne de texte, image, recette) avec sa hauteur en pixels. */
    private record Block(int height, Drawer drawer) {}

    private record PlacedBlock(Block block, int y) {}

    private int left, top, bookW, bookH;
    private int contentX, contentY, contentWidth, pageHeight;
    private int chapter, page;
    private final List<List<PlacedBlock>> pages = new ArrayList<>();
    private final Map<ResourceLocation, int[]> imageSizes = new HashMap<>();
    private Button prevButton, nextButton, discordButton;
    private ItemStack hoveredStack = ItemStack.EMPTY;

    public GuideBookScreen() {
        super(Component.translatable("item.electra.guide_book"));
        this.chapter = Math.min(lastChapter, GuideContent.CHAPTERS.size() - 1);
        this.page = lastPage;
    }

    @Override
    protected void init() {
        bookW = Math.min(MAX_WIDTH, this.width - 20);
        bookH = Math.min(MAX_HEIGHT, this.height - 20);
        left = (this.width - bookW) / 2;
        top = (this.height - bookH) / 2;

        contentX = left + SIDE_WIDTH + 12;
        contentWidth = bookW - SIDE_WIDTH - 24;
        contentY = top + 30;
        pageHeight = Math.max(4 * LINE_HEIGHT, bookH - 30 - 30);

        paginate();
        page = Math.max(0, Math.min(page, pages.size() - 1));

        int buttonY = top + bookH - 24;
        prevButton = addRenderableWidget(Button.builder(Component.literal("<"), b -> turnPage(-1))
                .bounds(contentX, buttonY, 20, 18).build());
        nextButton = addRenderableWidget(Button.builder(Component.literal(">"), b -> turnPage(1))
                .bounds(left + bookW - 32, buttonY, 20, 18).build());
        updateButtons();

        // Lien vers le Discord, en bas de la colonne des chapitres (icone et texte dessines dans render)
        discordButton = addRenderableWidget(Button.builder(Component.empty(), b -> openDiscord())
                .bounds(left + 6, buttonY, SIDE_WIDTH - 12, 18)
                .tooltip(Tooltip.create(Component.translatable("gui.electra.guide.discord_tooltip")))
                .build());
    }

    /** Ouvre le Discord apres la confirmation habituelle de Minecraft pour les liens externes. */
    private void openDiscord() {
        if (minecraft == null) return;
        minecraft.setScreen(new ConfirmLinkScreen(confirmed -> {
            if (confirmed) Util.getPlatform().openUri(DISCORD_URL);
            minecraft.setScreen(this);
        }, DISCORD_URL, true));
    }

    // ── Mise en page ───────────────────────────────────────────────────────

    /** Repartit les blocs du chapitre courant sur des pages de hauteur fixe. */
    private void paginate() {
        pages.clear();
        GuideContent.Chapter current = GuideContent.CHAPTERS.get(chapter);
        List<PlacedBlock> cur = new ArrayList<>();
        int y = 0;

        for (int i = 0; i < current.entries().size(); i++) {
            GuideContent.Entry entry = current.entries().get(i);
            Component text = Component.translatable(current.entryKey(i));

            switch (entry.kind()) {
                case HEADING, TEXT -> {
                    boolean heading = entry.kind() == GuideContent.Kind.HEADING;
                    if (heading) text = text.copy().withStyle(ChatFormatting.BOLD);
                    List<FormattedCharSequence> wrapped = font.split(text, contentWidth);
                    if (heading && y > 0) {
                        y += 3;
                        // Un sous-titre ne reste jamais seul en bas de page
                        if (y + (wrapped.size() + 2) * LINE_HEIGHT > pageHeight) {
                            pages.add(cur);
                            cur = new ArrayList<>();
                            y = 0;
                        }
                    }
                    int color = heading ? HEADING : TEXT;
                    for (FormattedCharSequence seq : wrapped) {
                        if (y + LINE_HEIGHT > pageHeight) {
                            pages.add(cur);
                            cur = new ArrayList<>();
                            y = 0;
                        }
                        cur.add(new PlacedBlock(new Block(LINE_HEIGHT,
                                (g, x, by, mx, my) -> g.drawString(font, seq, x, by, color, false)), y));
                        y += LINE_HEIGHT;
                    }
                    y += heading ? 2 : 5;
                }
                case IMAGE, RECIPE -> {
                    Block block = entry.kind() == GuideContent.Kind.IMAGE
                            ? imageBlock(entry, text) : recipeBlock(entry, text);
                    if (y > 0 && y + block.height() > pageHeight) {
                        pages.add(cur);
                        cur = new ArrayList<>();
                        y = 0;
                    }
                    cur.add(new PlacedBlock(block, y));
                    y += block.height() + 6;
                }
            }
        }
        if (!cur.isEmpty()) pages.add(cur);
        if (pages.isEmpty()) pages.add(List.of());
    }

    /** Image centree, jamais agrandie, reduite si besoin pour tenir dans la page avec sa legende. */
    private Block imageBlock(GuideContent.Entry entry, Component caption) {
        ResourceLocation location = ResourceLocation.fromNamespaceAndPath(
                Electra.MODID, IMAGE_FOLDER + entry.data());
        int[] size = imageSize(location);
        List<FormattedCharSequence> captionLines = font.split(caption, contentWidth);
        int captionHeight = captionLines.size() * LINE_HEIGHT;

        int texW = size != null ? size[0] : contentWidth;
        int texH = size != null ? size[1] : 40;
        int maxH = pageHeight - captionHeight - 4;
        int drawW = Math.min(contentWidth, texW);
        int drawH = texH * drawW / texW;
        if (drawH > maxH) {
            drawH = maxH;
            drawW = texW * drawH / texH;
        }
        final int w = drawW, h = drawH;

        return new Block(h + 4 + captionHeight, (g, x, y, mx, my) -> {
            int ix = x + (contentWidth - w) / 2;
            g.fill(ix - 1, y - 1, ix + w + 1, y + h + 1, COPPER_DARK);
            if (size != null) {
                g.blit(location, ix, y, w, h, 0f, 0f, texW, texH, texW, texH);
            } else {
                g.fill(ix, y, ix + w, y + h, 0xFFC9B88C);
                g.drawCenteredString(font, "Missing image: " + entry.data(), ix + w / 2, y + h / 2 - 4, TEXT);
            }
            drawCaption(g, captionLines, x, y + h + 4);
        });
    }

    /** Grille 3x3, fleche et resultat, avec les vrais objets et leurs infobulles. */
    private Block recipeBlock(GuideContent.Entry entry, Component caption) {
        List<FormattedCharSequence> captionLines = font.split(caption, contentWidth);
        List<ItemStack> grid = new ArrayList<>();
        for (String slot : entry.slots()) grid.add(parseStack(slot));
        ItemStack result = parseStack(entry.data());

        int gridSize = 3 * SLOT;
        int arrowW = 18;
        int total = gridSize + 8 + arrowW + 8 + SLOT;
        int height = gridSize + 4 + captionLines.size() * LINE_HEIGHT;

        return new Block(height, (g, x, y, mx, my) -> {
            int ox = x + (contentWidth - total) / 2;
            for (int i = 0; i < 9; i++) {
                drawSlot(g, ox + (i % 3) * SLOT, y + (i / 3) * SLOT, grid.get(i), mx, my);
            }
            // Fleche pleine
            int ax = ox + gridSize + 8;
            int ay = y + SLOT + SLOT / 2;
            g.fill(ax, ay - 1, ax + 14, ay + 1, COPPER_DARK);
            g.fill(ax + 14, ay - 3, ax + 15, ay + 3, COPPER_DARK);
            g.fill(ax + 15, ay - 2, ax + 16, ay + 2, COPPER_DARK);
            g.fill(ax + 16, ay - 1, ax + 17, ay + 1, COPPER_DARK);

            drawSlot(g, ax + arrowW + 8, y + SLOT, result, mx, my);
            drawCaption(g, captionLines, x, y + gridSize + 4);
        });
    }

    private void drawSlot(GuiGraphics g, int sx, int sy, ItemStack stack, int mouseX, int mouseY) {
        g.fill(sx, sy, sx + SLOT, sy + SLOT, 0xFF373737);
        g.fill(sx + 1, sy + 1, sx + SLOT, sy + SLOT, 0xFFFFFFFF);
        g.fill(sx + 1, sy + 1, sx + SLOT - 1, sy + SLOT - 1, 0xFF8B8B8B);
        if (stack.isEmpty()) return;
        g.renderItem(stack, sx + 1, sy + 1);
        g.renderItemDecorations(font, stack, sx + 1, sy + 1);
        if (mouseX >= sx && mouseX < sx + SLOT && mouseY >= sy && mouseY < sy + SLOT) {
            hoveredStack = stack;
        }
    }

    private void drawCaption(GuiGraphics g, List<FormattedCharSequence> lines, int x, int y) {
        for (int i = 0; i < lines.size(); i++) {
            FormattedCharSequence line = lines.get(i);
            g.drawString(font, line, x + (contentWidth - font.width(line)) / 2, y + i * LINE_HEIGHT, TEXT, false);
        }
    }

    /** "namespace:objet" ou "namespace:objet*quantite" ; "" donne une case vide. */
    private static ItemStack parseStack(String spec) {
        if (spec.isEmpty()) return ItemStack.EMPTY;
        String id = spec;
        int count = 1;
        int star = spec.indexOf('*');
        if (star >= 0) {
            id = spec.substring(0, star);
            count = Integer.parseInt(spec.substring(star + 1));
        }
        var item = BuiltInRegistries.ITEM.get(ResourceLocation.parse(id));
        // Identifiant inconnu : une barriere, bien visible, plutot qu'une case vide
        return new ItemStack(item == Items.AIR ? Items.BARRIER : item, count);
    }

    /** Taille reelle du fichier image, ou null s'il n'existe pas. */
    private int[] imageSize(ResourceLocation location) {
        if (imageSizes.containsKey(location)) return imageSizes.get(location);
        int[] size = null;
        if (minecraft != null) {
            try (InputStream in = minecraft.getResourceManager().getResourceOrThrow(location).open();
                 NativeImage image = NativeImage.read(in)) {
                size = new int[]{image.getWidth(), image.getHeight()};
            } catch (Exception e) {
                Electra.LOGGER.warn("Guide book: cannot read image {}", location);
            }
        }
        imageSizes.put(location, size);
        return size;
    }

    // ── Navigation ─────────────────────────────────────────────────────────

    private void turnPage(int delta) {
        int target = page + delta;
        if (target < 0 || target >= pages.size()) return;
        page = target;
        lastPage = page;
        GuideBookClient.playSound(SoundEvents.BOOK_PAGE_TURN, 1.0f);
        updateButtons();
    }

    private void selectChapter(int index) {
        if (index == chapter) return;
        chapter = index;
        page = 0;
        lastChapter = chapter;
        lastPage = 0;
        GuideBookClient.playSound(SoundEvents.BOOK_PAGE_TURN, 0.8f);
        paginate();
        updateButtons();
    }

    private void updateButtons() {
        if (prevButton == null) return;
        prevButton.active = page > 0;
        nextButton.active = page < pages.size() - 1;
    }

    // ── Rendu ──────────────────────────────────────────────────────────────

    @Override
    public void renderBackground(@NotNull GuiGraphics graphics, int mouseX, int mouseY, float partialTick) {
        super.renderBackground(graphics, mouseX, mouseY, partialTick);
        hoveredStack = ItemStack.EMPTY;

        // Cadre cuivre, fond parchemin, colonne des chapitres
        graphics.fill(left - 3, top - 3, left + bookW + 3, top + bookH + 3, COPPER_DARK);
        graphics.fill(left - 2, top - 2, left + bookW + 2, top + bookH + 2, COPPER);
        graphics.fill(left, top, left + bookW, top + bookH, PARCHMENT);
        graphics.fill(left, top, left + SIDE_WIDTH, top + bookH, SIDEBAR);
        graphics.fill(left + SIDE_WIDTH, top, left + SIDE_WIDTH + 1, top + bookH, COPPER);

        graphics.drawString(font, Component.translatable("gui.electra.guide.contents")
                .withStyle(ChatFormatting.BOLD), left + 8, top + 8, HEADING, false);
        for (int i = 0; i < GuideContent.CHAPTERS.size(); i++) {
            int rowY = rowY(i);
            boolean selected = i == chapter;
            boolean hovered = mouseX >= left && mouseX < left + SIDE_WIDTH
                    && mouseY >= rowY && mouseY < rowY + ROW_HEIGHT;
            if (selected || hovered) {
                graphics.fill(left + 2, rowY, left + SIDE_WIDTH - 2, rowY + ROW_HEIGHT, selected ? SELECTED : 0x33B87333);
            }
            Component title = Component.translatable(GuideContent.CHAPTERS.get(i).titleKey());
            graphics.drawString(font, font.plainSubstrByWidth(title.getString(), SIDE_WIDTH - 14),
                    left + 7, rowY + 3, selected ? HEADING : TEXT, false);
        }

        // Titre du chapitre, filet bleu FE, puis les blocs de la page
        Component chapterTitle = Component.translatable(GuideContent.CHAPTERS.get(chapter).titleKey())
                .withStyle(ChatFormatting.BOLD);
        graphics.drawString(font, chapterTitle, contentX, top + 10, HEADING, false);
        graphics.fill(contentX, top + 22, contentX + contentWidth, top + 23, FE_BLUE);

        for (PlacedBlock placed : pages.get(page)) {
            placed.block().drawer().draw(graphics, contentX, contentY + placed.y(), mouseX, mouseY);
        }

        Component pageLabel = Component.translatable("gui.electra.guide.page", page + 1, pages.size());
        graphics.drawString(font, pageLabel, contentX + contentWidth / 2 - font.width(pageLabel) / 2, top + bookH - 19, TEXT, false);
    }

    @Override
    public void render(@NotNull GuiGraphics graphics, int mouseX, int mouseY, float partialTick) {
        super.render(graphics, mouseX, mouseY, partialTick);

        // Contenu du bouton Discord : icone puis texte
        int[] iconSize = imageSize(DISCORD_ICON);
        if (iconSize != null) {
            graphics.blit(DISCORD_ICON, discordButton.getX() + 5, discordButton.getY() + 2, 14, 14,
                    0f, 0f, iconSize[0], iconSize[1], iconSize[0], iconSize[1]);
        }
        graphics.drawString(font, "Discord", discordButton.getX() + 24, discordButton.getY() + 5, 0xFFFFFFFF);

        // L'infobulle passe au-dessus de tout, boutons compris
        if (!hoveredStack.isEmpty()) {
            graphics.renderTooltip(font, hoveredStack, mouseX, mouseY);
        }
    }

    private int rowY(int index) {
        return top + 24 + index * ROW_HEIGHT;
    }

    @Override
    public boolean mouseClicked(double mouseX, double mouseY, int button) {
        if (button == 0 && mouseX >= left && mouseX < left + SIDE_WIDTH) {
            for (int i = 0; i < GuideContent.CHAPTERS.size(); i++) {
                int rowY = rowY(i);
                if (mouseY >= rowY && mouseY < rowY + ROW_HEIGHT) {
                    selectChapter(i);
                    return true;
                }
            }
        }
        return super.mouseClicked(mouseX, mouseY, button);
    }

    @Override
    public boolean mouseScrolled(double mouseX, double mouseY, double scrollX, double scrollY) {
        if (scrollY != 0) {
            turnPage(scrollY < 0 ? 1 : -1);
            return true;
        }
        return super.mouseScrolled(mouseX, mouseY, scrollX, scrollY);
    }

    @Override
    public boolean keyPressed(int keyCode, int scanCode, int modifiers) {
        if (keyCode == InputConstants.KEY_RIGHT) {
            turnPage(1);
            return true;
        }
        if (keyCode == InputConstants.KEY_LEFT) {
            turnPage(-1);
            return true;
        }
        // La touche d'inventaire ferme le livre, comme les autres ecrans
        if (minecraft != null && minecraft.options.keyInventory.matches(keyCode, scanCode)) {
            onClose();
            return true;
        }
        return super.keyPressed(keyCode, scanCode, modifiers);
    }

    @Override
    public boolean isPauseScreen() {
        return false;
    }
}
