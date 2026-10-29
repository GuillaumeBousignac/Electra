package com.electra.mod.client;

import com.electra.mod.block.FluidReservoirBlock;
import com.electra.mod.blockentity.FluidReservoirBlockEntity;
import com.mojang.blaze3d.vertex.PoseStack;
import com.mojang.blaze3d.vertex.VertexConsumer;
import net.minecraft.client.renderer.MultiBufferSource;
import net.minecraft.client.renderer.RenderType;
import net.minecraft.client.renderer.blockentity.BlockEntityRenderer;
import net.minecraft.client.renderer.blockentity.BlockEntityRendererProvider;
import net.minecraft.client.renderer.texture.TextureAtlas;
import net.minecraft.util.Mth;
import net.minecraft.world.level.block.state.BlockState;
import net.neoforged.neoforge.fluids.FluidStack;
import org.jetbrains.annotations.NotNull;

/** Liquide visible derrière les vitres du réservoir ; le niveau suit le remplissage. */
public class FluidReservoirRenderer implements BlockEntityRenderer<FluidReservoirBlockEntity> {

    // Intérieur de la cuve en pixels : 2 → 14 sur chaque axe
    private static final float MIN = 2f / 16f;
    private static final float MAX = 14f / 16f;

    public FluidReservoirRenderer(BlockEntityRendererProvider.Context context) {}

    @Override
    public void render(@NotNull FluidReservoirBlockEntity reservoir, float partialTick, @NotNull PoseStack poseStack,
                       @NotNull MultiBufferSource buffers, int packedLight, int packedOverlay) {
        FluidStack fluid = reservoir.getTank().getFluid();
        if (fluid.isEmpty() || reservoir.getLevel() == null) return;

        BlockState state = reservoir.getBlockState();
        boolean north = state.getValue(FluidReservoirBlock.NORTH), south = state.getValue(FluidReservoirBlock.SOUTH);
        boolean east = state.getValue(FluidReservoirBlock.EAST), west = state.getValue(FluidReservoirBlock.WEST);
        boolean up = state.getValue(FluidReservoirBlock.UP), down = state.getValue(FluidReservoirBlock.DOWN);

        // Hauteur du liquide : sur toute la hauteur du bloc quand il est relié à un voisin au-dessus ou en dessous
        float ratio = Mth.clamp((float) fluid.getAmount() / reservoir.getTank().getCapacity(), 0f, 1f);
        float bottom = down ? 0f : MIN;
        float usable = (up ? 1f : MAX) - bottom;
        float top = bottom + usable * Math.max(ratio, 0.04f);
        boolean full = ratio >= 0.999f && up;
        if (full) top = 1f;

        // Le liquide traverse les parois communes : plus de cadre, il remplit jusqu'au bord et ne dessine pas ces faces
        float x0 = west ? 0f : MIN, x1 = east ? 1f : MAX;
        float z0 = north ? 0f : MIN, z1 = south ? 1f : MAX;
        int skip = 0;
        if (west) skip |= FluidRenderHelper.WEST;
        if (east) skip |= FluidRenderHelper.EAST;
        if (north) skip |= FluidRenderHelper.NORTH;
        if (south) skip |= FluidRenderHelper.SOUTH;
        if (down) skip |= FluidRenderHelper.DOWN;
        if (full) skip |= FluidRenderHelper.UP;

        FluidRenderHelper.Style style = FluidRenderHelper.styleOf(fluid, reservoir.getLevel(), reservoir.getBlockPos());
        VertexConsumer consumer = buffers.getBuffer(RenderType.entityTranslucentCull(TextureAtlas.LOCATION_BLOCKS));
        FluidRenderHelper.drawBox(consumer, poseStack.last(), style.sprite(), style.argb(),
                FluidRenderHelper.light(packedLight, style), x0, bottom, z0, x1, top, z1, skip);
    }
}
