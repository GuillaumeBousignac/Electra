package com.electra.mod.client;

import com.mojang.blaze3d.vertex.PoseStack;
import com.mojang.blaze3d.vertex.VertexConsumer;
import net.minecraft.client.Minecraft;
import net.minecraft.client.renderer.LightTexture;
import net.minecraft.client.renderer.texture.OverlayTexture;
import net.minecraft.client.renderer.texture.TextureAtlas;
import net.minecraft.client.renderer.texture.TextureAtlasSprite;
import net.minecraft.core.BlockPos;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.level.Level;
import net.neoforged.neoforge.client.extensions.common.IClientFluidTypeExtensions;
import net.neoforged.neoforge.fluids.FluidStack;
import org.jetbrains.annotations.Nullable;

/** Dessine un fluide comme un pavé texturé (tuyaux, réservoir) et un tube plein (pompe). */
public final class FluidRenderHelper {

    private FluidRenderHelper() {}

    /** Aspect d'un fluide : texture, couleur ARGB et lumière propre (lave). */
    public record Style(TextureAtlasSprite sprite, int argb, int fluidLight) {}

    // Faces : normale, puis a et b tels que a × b = normale (sommets anti-horaires vus de l'extérieur)
    private static final float[][][] FACES = {
            {{0, 1, 0}, {0, 0, 1}, {1, 0, 0}},
            {{0, -1, 0}, {1, 0, 0}, {0, 0, 1}},
            {{1, 0, 0}, {0, 1, 0}, {0, 0, 1}},
            {{-1, 0, 0}, {0, 0, 1}, {0, 1, 0}},
            {{0, 0, 1}, {1, 0, 0}, {0, 1, 0}},
            {{0, 0, -1}, {0, 1, 0}, {1, 0, 0}},
    };

    public static TextureAtlasSprite blockSprite(ResourceLocation id) {
        return Minecraft.getInstance().getTextureAtlas(TextureAtlas.LOCATION_BLOCKS).apply(id);
    }

    public static Style styleOf(FluidStack stack, @Nullable Level level, BlockPos pos) {
        IClientFluidTypeExtensions ext = IClientFluidTypeExtensions.of(stack.getFluid());
        TextureAtlasSprite sprite = blockSprite(ext.getStillTexture(stack));

        // Teinte selon le biome pour l'eau ; blanc pour les autres fluides
        int tint = level != null
                ? ext.getTintColor(stack.getFluid().defaultFluidState(), level, pos)
                : ext.getTintColor(stack);
        int alpha = Math.min(0xE0, tint >>> 24);   // un peu de transparence, comme l'eau vanilla
        int argb = (alpha << 24) | (tint & 0x00FFFFFF);

        return new Style(sprite, argb, stack.getFluidType().getLightLevel(stack));
    }

    /** Lumière finale : la lave éclaire sa propre cuve. */
    public static int light(int packedLight, Style style) {
        if (style.fluidLight() <= 0) return packedLight;
        return LightTexture.pack(
                Math.max(LightTexture.block(packedLight), style.fluidLight()),
                LightTexture.sky(packedLight));
    }

    /** Bits de faces à ne pas dessiner (collées à un voisin) : haut, bas, est (+x), ouest (-x), sud (+z), nord (-z). */
    public static final int UP = 1, DOWN = 2, EAST = 4, WEST = 8, SOUTH = 16, NORTH = 32;

    /** Pavé aux coordonnées en blocs (0 → 1 dans le bloc). */
    public static void drawBox(VertexConsumer consumer, PoseStack.Pose pose, TextureAtlasSprite sprite,
                               int argb, int light,
                               float x0, float y0, float z0, float x1, float y1, float z1) {
        drawBox(consumer, pose, sprite, argb, light, x0, y0, z0, x1, y1, z1, 0);
    }

    public static void drawBox(VertexConsumer consumer, PoseStack.Pose pose, TextureAtlasSprite sprite,
                               int argb, int light,
                               float x0, float y0, float z0, float x1, float y1, float z1, int skipFaces) {
        int a = argb >>> 24 & 0xFF, r = argb >>> 16 & 0xFF, g = argb >>> 8 & 0xFF, b = argb & 0xFF;
        float[] center = {(x0 + x1) / 2, (y0 + y1) / 2, (z0 + z1) / 2};
        float[] half = {(x1 - x0) / 2, (y1 - y0) / 2, (z1 - z0) / 2};

        float[][] corners = {{-1, -1}, {1, -1}, {1, 1}, {-1, 1}};
        for (int faceIndex = 0; faceIndex < FACES.length; faceIndex++) {
            if ((skipFaces & (1 << faceIndex)) != 0) continue;
            float[][] face = FACES[faceIndex];
            float[] n = face[0], fa = face[1], fb = face[2];
            // Étendue de la face sur ses deux axes, pour ne pas étirer la texture
            float extentA = Math.min(1f, 2 * half[axis(fa)]);
            float extentB = Math.min(1f, 2 * half[axis(fb)]);
            float u0 = sprite.getU0(), v0 = sprite.getV0();
            float du = (sprite.getU1() - u0) * extentA, dv = (sprite.getV1() - v0) * extentB;

            for (float[] corner : corners) {
                float sa = corner[0], sb = corner[1];
                float x = center[0] + half[0] * (n[0] + fa[0] * sa + fb[0] * sb);
                float y = center[1] + half[1] * (n[1] + fa[1] * sa + fb[1] * sb);
                float z = center[2] + half[2] * (n[2] + fa[2] * sa + fb[2] * sb);
                consumer.addVertex(pose, x, y, z)
                        .setColor(r, g, b, a)
                        .setUv(u0 + (sa > 0 ? du : 0), v0 + (sb > 0 ? dv : 0))
                        .setOverlay(OverlayTexture.NO_OVERLAY)
                        .setLight(light)
                        .setNormal(pose, n[0], n[1], n[2]);
            }
        }
    }

    private static int axis(float[] v) {
        return v[0] != 0 ? 0 : v[1] != 0 ? 1 : 2;
    }
}
