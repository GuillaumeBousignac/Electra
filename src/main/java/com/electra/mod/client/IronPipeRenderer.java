package com.electra.mod.client;

import com.electra.mod.blockentity.IronPipeBlockEntity;
import com.mojang.blaze3d.vertex.PoseStack;
import com.mojang.blaze3d.vertex.VertexConsumer;
import net.minecraft.client.renderer.MultiBufferSource;
import net.minecraft.client.renderer.RenderType;
import net.minecraft.client.renderer.blockentity.BlockEntityRenderer;
import net.minecraft.client.renderer.blockentity.BlockEntityRendererProvider;
import net.minecraft.client.renderer.texture.TextureAtlas;
import net.minecraft.util.Mth;
import net.minecraft.world.level.block.PipeBlock;
import net.minecraft.world.level.block.state.BlockState;
import net.neoforged.neoforge.fluids.FluidStack;
import org.jetbrains.annotations.NotNull;

/**
 * Fluide visible à travers les vitres du tuyau : un cœur et un bras par côté connecté.
 * Le niveau monte avec la part de fluide du tuyau (voir FluidNetwork#flushToPipes).
 */
public class IronPipeRenderer implements BlockEntityRenderer<IronPipeBlockEntity> {

    // Intérieur du tuyau en pixels : 6 → 10 sur chaque axe
    private static final float LO = 6f / 16f;
    private static final float HI = 10f / 16f;

    public IronPipeRenderer(BlockEntityRendererProvider.Context context) {}

    @Override
    public void render(@NotNull IronPipeBlockEntity pipe, float partialTick, @NotNull PoseStack poseStack,
                       @NotNull MultiBufferSource buffers, int packedLight, int packedOverlay) {
        FluidStack fluid = pipe.getShare();
        if (fluid.isEmpty() || pipe.getLevel() == null) return;

        float ratio = Mth.clamp((float) fluid.getAmount() / IronPipeBlockEntity.capacity(), 0f, 1f);
        float top = LO + (HI - LO) * Math.max(ratio, 0.2f);   // toujours un peu visible

        FluidRenderHelper.Style style = FluidRenderHelper.styleOf(fluid, pipe.getLevel(), pipe.getBlockPos());
        int light = FluidRenderHelper.light(packedLight, style);
        VertexConsumer consumer = buffers.getBuffer(RenderType.entityTranslucentCull(TextureAtlas.LOCATION_BLOCKS));
        PoseStack.Pose pose = poseStack.last();
        BlockState state = pipe.getBlockState();

        // Cœur
        box(consumer, pose, style, light, LO, LO, LO, HI, top, HI);

        // Bras : le bas toujours rempli, les côtés au niveau du fluide, le haut seulement si le tuyau est bien plein
        if (state.getValue(PipeBlock.NORTH)) box(consumer, pose, style, light, LO, LO, 0, HI, top, LO);
        if (state.getValue(PipeBlock.SOUTH)) box(consumer, pose, style, light, LO, LO, HI, HI, top, 1);
        if (state.getValue(PipeBlock.WEST))  box(consumer, pose, style, light, 0, LO, LO, LO, top, HI);
        if (state.getValue(PipeBlock.EAST))  box(consumer, pose, style, light, HI, LO, LO, 1, top, HI);
        if (state.getValue(PipeBlock.DOWN))  box(consumer, pose, style, light, LO, 0, LO, HI, LO, HI);
        if (state.getValue(PipeBlock.UP) && ratio > 0.6f) box(consumer, pose, style, light, LO, HI, LO, HI, 1, HI);
    }

    private static void box(VertexConsumer consumer, PoseStack.Pose pose, FluidRenderHelper.Style style, int light,
                            float x0, float y0, float z0, float x1, float y1, float z1) {
        FluidRenderHelper.drawBox(consumer, pose, style.sprite(), style.argb(), light, x0, y0, z0, x1, y1, z1);
    }
}
