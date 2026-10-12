package com.electra.mod.client;

import com.electra.mod.Electra;
import com.electra.mod.block.RedstoneDynamoBlock;
import com.electra.mod.blockentity.RedstoneDynamoBlockEntity;
import com.mojang.blaze3d.vertex.PoseStack;
import com.mojang.math.Axis;
import net.minecraft.client.Minecraft;
import net.minecraft.client.renderer.MultiBufferSource;
import net.minecraft.client.renderer.Sheets;
import net.minecraft.client.renderer.block.ModelBlockRenderer;
import net.minecraft.client.renderer.blockentity.BlockEntityRenderer;
import net.minecraft.client.renderer.blockentity.BlockEntityRendererProvider;
import net.minecraft.client.resources.model.BakedModel;
import net.minecraft.client.resources.model.ModelResourceLocation;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.phys.AABB;
import org.jetbrains.annotations.NotNull;

/**
 * Dessine le bras de la Redstone Dynamo (modèle block/dynamo_arm.json) : collier autour de la bobine,
 * poutre en cuivre, platine et quatre doigts qui enserrent le bloc de fer rouge.
 *
 * Le modèle est dessiné autour de l'axe Y, bras pointant vers +X. Pour une Dynamo verticale (axe X ou Z),
 * on aligne d'abord ce Y sur l'axe de la Dynamo ; RedstoneDynamoBlockEntity#angleOf utilise la même convention.
 */
public class RedstoneDynamoRenderer implements BlockEntityRenderer<RedstoneDynamoBlockEntity> {

    /** Modèle chargé à part (voir ElectraClient : ModelEvent.RegisterAdditional). */
    public static final ModelResourceLocation ARM_MODEL = ModelResourceLocation.standalone(
            ResourceLocation.fromNamespaceAndPath(Electra.MODID, "block/dynamo_arm"));

    private final ModelBlockRenderer modelRenderer;

    public RedstoneDynamoRenderer(BlockEntityRendererProvider.Context context) {
        this.modelRenderer = context.getBlockRenderDispatcher().getModelRenderer();
    }

    @Override
    public void render(@NotNull RedstoneDynamoBlockEntity dynamo, float partialTick, @NotNull PoseStack poseStack,
                       @NotNull MultiBufferSource buffers, int packedLight, int packedOverlay) {
        if (dynamo.getLevel() == null || !dynamo.hasRotor()) return;

        BakedModel arm = Minecraft.getInstance().getModelManager().getModel(ARM_MODEL);

        poseStack.pushPose();
        poseStack.translate(0.5, 0.5, 0.5);

        // Alignement du modèle sur l'axe de la Dynamo
        switch (dynamo.getBlockState().getValue(RedstoneDynamoBlock.AXIS)) {
            case X -> poseStack.mulPose(Axis.ZP.rotationDegrees(-90.0f));
            case Z -> poseStack.mulPose(Axis.XP.rotationDegrees(90.0f));
            default -> { }
        }
        // Rotation du bras ; angle négatif = sens horaire (vu de dessus pour une Dynamo horizontale)
        poseStack.mulPose(Axis.YP.rotationDegrees(-dynamo.getRenderAngle(partialTick)));
        poseStack.translate(-0.5, -0.5, -0.5);

        modelRenderer.renderModel(poseStack.last(), buffers.getBuffer(Sheets.cutoutBlockSheet()), null, arm,
                1.0f, 1.0f, 1.0f, packedLight, packedOverlay);

        poseStack.popPose();
    }

    /** Le bras sort du bloc de la Dynamo : la zone de visibilité couvre tout le couloir. */
    public AABB getRenderBoundingBox(RedstoneDynamoBlockEntity dynamo) {
        return new AABB(dynamo.getBlockPos()).inflate(1.0);
    }
}
