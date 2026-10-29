package com.electra.mod.client;

import com.electra.mod.Electra;
import com.electra.mod.blockentity.FluidPumpBlockEntity;
import com.mojang.blaze3d.vertex.PoseStack;
import com.mojang.blaze3d.vertex.VertexConsumer;
import net.minecraft.client.renderer.MultiBufferSource;
import net.minecraft.client.renderer.RenderType;
import net.minecraft.client.renderer.blockentity.BlockEntityRenderer;
import net.minecraft.client.renderer.blockentity.BlockEntityRendererProvider;
import net.minecraft.client.renderer.texture.TextureAtlas;
import net.minecraft.client.renderer.texture.TextureAtlasSprite;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.phys.AABB;
import org.jetbrains.annotations.NotNull;

/** Tube d'aspiration en fer, de la pompe jusqu'à la source visée. */
public class FluidPumpRenderer implements BlockEntityRenderer<FluidPumpBlockEntity> {

    private static final ResourceLocation TUBE_TEXTURE =
            ResourceLocation.fromNamespaceAndPath(Electra.MODID, "block/fluid_pipe");

    // Tube de 3 px de large, centré
    private static final float LO = 6.5f / 16f;
    private static final float HI = 9.5f / 16f;

    public FluidPumpRenderer(BlockEntityRendererProvider.Context context) {}

    @Override
    public void render(@NotNull FluidPumpBlockEntity pump, float partialTick, @NotNull PoseStack poseStack,
                       @NotNull MultiBufferSource buffers, int packedLight, int packedOverlay) {
        int depth = pump.getSuctionDepth();
        if (depth <= 0) return;

        TextureAtlasSprite sprite = FluidRenderHelper.blockSprite(TUBE_TEXTURE);
        VertexConsumer consumer = buffers.getBuffer(RenderType.entityCutout(TextureAtlas.LOCATION_BLOCKS));
        // Du dessous de la pompe jusqu'au milieu du bloc de la source
        FluidRenderHelper.drawBox(consumer, poseStack.last(), sprite, 0xFFFFFFFF, packedLight,
                LO, 0.5f - depth, LO, HI, 0f, HI);
    }

    /** Le tube dépasse sous le bloc : sans cela il disparaîtrait dès que la pompe sort du champ de vision. */
    @Override
    public @NotNull AABB getRenderBoundingBox(@NotNull FluidPumpBlockEntity pump) {
        return new AABB(pump.getBlockPos()).expandTowards(0, -pump.getSuctionDepth(), 0);
    }
}
