package com.electra.mod.client;

import com.electra.mod.Electra;
import com.mojang.blaze3d.platform.NativeImage;
import com.mojang.logging.LogUtils;
import com.mojang.serialization.Codec;
import com.mojang.serialization.MapCodec;
import com.mojang.serialization.codecs.RecordCodecBuilder;
import net.minecraft.client.renderer.texture.atlas.SpriteSource;
import net.minecraft.client.renderer.texture.atlas.SpriteSourceType;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.server.packs.resources.Resource;
import net.minecraft.server.packs.resources.ResourceManager;
import org.jetbrains.annotations.NotNull;
import org.slf4j.Logger;

import java.io.ByteArrayInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.util.List;
import java.util.Optional;

/**
 * Source d'atlas : prend une texture du jeu (par exemple minecraft:item/iron_pickaxe) et en fait un
 * motif de lampe, blanc ou gris, que le rendu des lampes peut teinter.
 *
 * La conversion a lieu au chargement des textures, sur la machine du joueur, à partir des fichiers
 * de son propre jeu : le mod ne contient aucune texture de Minecraft. Avec un pack de ressources,
 * le motif suit les textures du pack.
 *
 * Déclarée dans assets/minecraft/atlases/blocks.json, par exemple :
 *   { "type": "electra:silhouette", "resource": "minecraft:item/iron_pickaxe",
 *     "sprite": "electra:lamp_symbol/pickaxe" }
 *   { "type": "electra:silhouette", "resource": "minecraft:entity/creeper/creeper",
 *     "sprite": "electra:lamp_symbol/creeper", "mode": "grayscale",
 *     "region": [8, 8, 8, 8], "reference_width": 64 }
 *
 * mode            : "silhouette" (tout pixel visible devient blanc) ou "grayscale" (on garde les
 *                   nuances, en gris, pour que les détails d'un visage restent visibles).
 * region          : [x, y, largeur, hauteur] à découper, en pixels d'une image large de
 *                   reference_width (recalculé si un pack de ressources a une autre résolution).
 *                   Le morceau est centré dans une image carrée.
 * overlays        : morceaux ajoutés par-dessus, pour les reliefs que le modèle dessine à part
 *                   (nez du villageois, groin du cochon) : [{ "region": [x, y, l, h], "at": [x, y] }],
 *                   « at » étant la position dans le morceau principal, mêmes unités que region.
 *                   Un morceau peut dépasser du principal : l'image s'agrandit pour le contenir.
 */
public record SilhouetteSpriteSource(ResourceLocation resource, ResourceLocation sprite, String mode,
                                     Optional<List<Integer>> region, int referenceWidth,
                                     List<Overlay> overlays) implements SpriteSource {

    /** Morceau collé par-dessus le morceau principal. */
    public record Overlay(List<Integer> region, List<Integer> at) {
        public static final Codec<Overlay> CODEC = RecordCodecBuilder.create(instance -> instance.group(
                Codec.INT.listOf().fieldOf("region").forGetter(Overlay::region),
                Codec.INT.listOf().fieldOf("at").forGetter(Overlay::at)
        ).apply(instance, Overlay::new));
    }

    private static final Logger LOGGER = LogUtils.getLogger();

    public static final ResourceLocation ID = ResourceLocation.fromNamespaceAndPath(Electra.MODID, "silhouette");

    public static final MapCodec<SilhouetteSpriteSource> CODEC = RecordCodecBuilder.mapCodec(instance ->
            instance.group(
                    ResourceLocation.CODEC.fieldOf("resource").forGetter(SilhouetteSpriteSource::resource),
                    ResourceLocation.CODEC.fieldOf("sprite").forGetter(SilhouetteSpriteSource::sprite),
                    Codec.STRING.optionalFieldOf("mode", "silhouette").forGetter(SilhouetteSpriteSource::mode),
                    Codec.INT.listOf().optionalFieldOf("region").forGetter(SilhouetteSpriteSource::region),
                    Codec.INT.optionalFieldOf("reference_width", 16).forGetter(SilhouetteSpriteSource::referenceWidth),
                    Overlay.CODEC.listOf().optionalFieldOf("overlays", List.of()).forGetter(SilhouetteSpriteSource::overlays)
            ).apply(instance, SilhouetteSpriteSource::new));

    public static final SpriteSourceType TYPE = new SpriteSourceType(CODEC);

    @Override
    public void run(@NotNull ResourceManager resourceManager, @NotNull SpriteSource.Output output) {
        ResourceLocation file = TEXTURE_ID_CONVERTER.idToFile(resource);
        Optional<Resource> original = resourceManager.getResource(file);
        if (original.isEmpty()) {
            LOGGER.warn("Electra: texture {} introuvable, motif {} absent", file, sprite);
            return;
        }
        Resource source = original.get();
        // Même fichier, mais lu à travers la conversion
        output.add(sprite, new Resource(source.source(), () -> convert(source)));
    }

    private InputStream convert(Resource source) throws IOException {
        try (InputStream in = source.open(); NativeImage original = NativeImage.read(in);
             NativeImage image = crop(original)) {
            if ("grayscale".equals(mode)) toGrayscale(image);
            else toSilhouette(image);
            return new ByteArrayInputStream(image.asByteArray());
        }
    }

    /**
     * Découpe la région demandée (ou toute l'image), colle les morceaux ajoutés par-dessus,
     * puis centre le tout dans une image carrée.
     */
    private NativeImage crop(NativeImage original) {
        float factor = (float) original.getWidth() / referenceWidth;
        int[] main = region.isPresent() && region.get().size() == 4
                ? scaled(region.get(), factor)
                : new int[]{0, 0, original.getWidth(), original.getHeight()};

        // Emprise totale, en coordonnées du morceau principal (les ajouts peuvent dépasser)
        int minX = 0, minY = 0, maxX = main[2], maxY = main[3];
        for (Overlay overlay : overlays) {
            if (overlay.region().size() != 4 || overlay.at().size() != 2) continue;
            int[] r = scaled(overlay.region(), factor);
            int ax = Math.round(overlay.at().get(0) * factor), ay = Math.round(overlay.at().get(1) * factor);
            minX = Math.min(minX, ax); minY = Math.min(minY, ay);
            maxX = Math.max(maxX, ax + r[2]); maxY = Math.max(maxY, ay + r[3]);
        }
        int width = maxX - minX, height = maxY - minY, side = Math.max(width, height);
        int originX = (side - width) / 2 - minX, originY = (side - height) / 2 - minY;

        NativeImage out = new NativeImage(side, side, true);   // rempli de transparent
        copy(original, main, out, originX, originY);
        for (Overlay overlay : overlays) {
            if (overlay.region().size() != 4 || overlay.at().size() != 2) continue;
            int[] r = scaled(overlay.region(), factor);
            copy(original, r, out, originX + Math.round(overlay.at().get(0) * factor),
                    originY + Math.round(overlay.at().get(1) * factor));
        }
        return out;
    }

    private static int[] scaled(List<Integer> r, float factor) {
        return new int[]{Math.round(r.get(0) * factor), Math.round(r.get(1) * factor),
                Math.max(1, Math.round(r.get(2) * factor)), Math.max(1, Math.round(r.get(3) * factor))};
    }

    /** Copie le rectangle {x, y, l, h} de la source à la position (toX, toY) ; ignore les pixels transparents. */
    private static void copy(NativeImage from, int[] rect, NativeImage to, int toX, int toY) {
        for (int y = 0; y < rect[3]; y++) {
            for (int x = 0; x < rect[2]; x++) {
                int sx = rect[0] + x, sy = rect[1] + y, dx = toX + x, dy = toY + y;
                if (sx >= from.getWidth() || sy >= from.getHeight()) continue;
                if (dx < 0 || dy < 0 || dx >= to.getWidth() || dy >= to.getHeight()) continue;
                int pixel = from.getPixelRGBA(sx, sy);
                if ((pixel >>> 24) != 0) to.setPixelRGBA(dx, dy, pixel);
            }
        }
    }

    /** Chaque pixel visible devient blanc ; la transparence d'origine est conservée. */
    private static void toSilhouette(NativeImage image) {
        for (int y = 0; y < image.getHeight(); y++) {
            for (int x = 0; x < image.getWidth(); x++) {
                int alpha = image.getPixelRGBA(x, y) >>> 24;
                image.setPixelRGBA(x, y, alpha == 0 ? 0 : (alpha << 24) | 0x00FFFFFF);
            }
        }
    }

    /**
     * Nuances de gris, étirées pour que le pixel le plus clair soit blanc et le plus sombre gris foncé :
     * une fois teinté, le visage garde ses yeux et sa bouche.
     */
    private static void toGrayscale(NativeImage image) {
        int width = image.getWidth(), height = image.getHeight();
        int[] luminance = new int[width * height];
        int min = 255, max = 0;
        for (int y = 0; y < height; y++) {
            for (int x = 0; x < width; x++) {
                int pixel = image.getPixelRGBA(x, y);              // ABGR
                int red = pixel & 0xFF, green = (pixel >> 8) & 0xFF, blue = (pixel >> 16) & 0xFF;
                int l = (299 * red + 587 * green + 114 * blue) / 1000;
                luminance[y * width + x] = l;
                if ((pixel >>> 24) != 0) { min = Math.min(min, l); max = Math.max(max, l); }
            }
        }
        int range = Math.max(1, max - min);
        for (int y = 0; y < height; y++) {
            for (int x = 0; x < width; x++) {
                int alpha = image.getPixelRGBA(x, y) >>> 24;
                if (alpha == 0) { image.setPixelRGBA(x, y, 0); continue; }
                int grey = 40 + (luminance[y * width + x] - min) * 215 / range;
                image.setPixelRGBA(x, y, (alpha << 24) | (grey << 16) | (grey << 8) | grey);
            }
        }
    }

    @Override
    public @NotNull SpriteSourceType type() {
        return TYPE;
    }
}
