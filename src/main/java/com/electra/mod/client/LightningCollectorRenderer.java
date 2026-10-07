package com.electra.mod.client;

import com.electra.mod.Electra;
import com.electra.mod.block.LightningCollectorBlock;
import com.electra.mod.blockentity.LightningCollectorBlockEntity;
import com.mojang.blaze3d.vertex.PoseStack;
import com.mojang.blaze3d.vertex.VertexConsumer;
import com.mojang.math.Axis;
import net.minecraft.client.renderer.LightTexture;
import net.minecraft.client.renderer.MultiBufferSource;
import net.minecraft.client.renderer.RenderType;
import net.minecraft.client.renderer.blockentity.BlockEntityRenderer;
import net.minecraft.client.renderer.blockentity.BlockEntityRendererProvider;
import net.minecraft.client.renderer.texture.OverlayTexture;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.util.Mth;
import org.jetbrains.annotations.NotNull;

/**
 * Boule d'énergie au centre de la cage quand le collecteur est chargé.
 * Deux couches qui tournent en sens inverse : un cœur brillant et une enveloppe plus grande.
 * Chaque face porte un disque lumineux (coins transparents), ce qui donne un rendu arrondi.
 * La texture contient 8 images empilées, animées ici (les textures hors atlas ignorent les .mcmeta).
 */
public class LightningCollectorRenderer implements BlockEntityRenderer<LightningCollectorBlockEntity> {

    private static final ResourceLocation TEXTURE =
            ResourceLocation.fromNamespaceAndPath(Electra.MODID, "textures/block/energy_core.png");

    private static final int   FRAMES        = 8;
    private static final int   TICKS_PER_FRAME = 2;

    private static final float SHELL_SIZE    = 3.5f / 16.0f; // demi-côté de l'enveloppe
    private static final float CORE_SIZE     = 1.75f / 16.0f; // demi-côté du cœur
    private static final float SPIN_SPEED    = 2.0f;          // degrés par tick
    private static final float BOB_AMPLITUDE = 0.03f;         // en blocs
    private static final float BOB_SPEED     = 0.08f;

    // Faces : normale, puis a et b tels que a × b = normale (sommets anti-horaires vus de l'extérieur)
    private static final float[][][] FACES = {
            {{0, 1, 0}, {0, 0, 1}, {1, 0, 0}},
            {{0, -1, 0}, {1, 0, 0}, {0, 0, 1}},
            {{1, 0, 0}, {0, 1, 0}, {0, 0, 1}},
            {{-1, 0, 0}, {0, 0, 1}, {0, 1, 0}},
            {{0, 0, 1}, {1, 0, 0}, {0, 1, 0}},
            {{0, 0, -1}, {0, 1, 0}, {1, 0, 0}},
    };

    public LightningCollectorRenderer(BlockEntityRendererProvider.Context context) {}

    @Override
    public void render(@NotNull LightningCollectorBlockEntity collector, float partialTick, @NotNull PoseStack poseStack,
                       @NotNull MultiBufferSource buffers, int packedLight, int packedOverlay) {
        if (collector.getLevel() == null) return;
        if (!collector.getBlockState().getValue(LightningCollectorBlock.CHARGED)) return;

        float time = collector.getLevel().getGameTime() + partialTick;
        int frame = (int) (time / TICKS_PER_FRAME) % FRAMES;
        float v0 = (float) frame / FRAMES;
        float v1 = (float) (frame + 1) / FRAMES;
        float pulse = Mth.sin(time * 0.15f);

        VertexConsumer consumer = buffers.getBuffer(RenderType.entityTranslucentCull(TEXTURE));

        poseStack.pushPose();
        poseStack.translate(0.5, 0.5 + Mth.sin(time * BOB_SPEED) * BOB_AMPLITUDE, 0.5);

        // Cœur : petit, opaque, tourne dans un sens
        poseStack.pushPose();
        poseStack.mulPose(Axis.YP.rotationDegrees(-time * SPIN_SPEED * 1.5f));
        poseStack.mulPose(Axis.XP.rotationDegrees(45.0f));
        poseStack.mulPose(Axis.ZP.rotationDegrees(35.264f));
        drawCube(consumer, poseStack.last(), CORE_SIZE, v0, v1, 255);
        poseStack.popPose();

        // Enveloppe : plus grande, semi-transparente, tourne dans l'autre sens et respire
        poseStack.pushPose();
        float scale = 1.0f + pulse * 0.06f;
        poseStack.scale(scale, scale, scale);
        poseStack.mulPose(Axis.YP.rotationDegrees(time * SPIN_SPEED));
        poseStack.mulPose(Axis.XP.rotationDegrees(45.0f));
        poseStack.mulPose(Axis.ZP.rotationDegrees(35.264f));
        drawCube(consumer, poseStack.last(), SHELL_SIZE, v0, v1, (int) (190 + 40 * pulse));
        poseStack.popPose();

        poseStack.popPose();
    }

    private static void drawCube(VertexConsumer consumer, PoseStack.Pose pose, float h,
                                 float v0, float v1, int alpha) {
        for (float[][] face : FACES) {
            float[] n = face[0], a = face[1], b = face[2];
            float[][] corners = {{-1, -1}, {1, -1}, {1, 1}, {-1, 1}};
            float[][] uvs = {{0, v0}, {0, v1}, {1, v1}, {1, v0}};
            for (int i = 0; i < 4; i++) {
                float sa = corners[i][0] * h, sb = corners[i][1] * h;
                consumer.addVertex(pose,
                                n[0] * h + a[0] * sa + b[0] * sb,
                                n[1] * h + a[1] * sa + b[1] * sb,
                                n[2] * h + a[2] * sa + b[2] * sb)
                        .setColor(255, 255, 255, alpha)
                        .setUv(uvs[i][0], uvs[i][1])
                        .setOverlay(OverlayTexture.NO_OVERLAY)
                        .setLight(LightTexture.FULL_BRIGHT)
                        .setNormal(pose, n[0], n[1], n[2]);
            }
        }
    }
}
