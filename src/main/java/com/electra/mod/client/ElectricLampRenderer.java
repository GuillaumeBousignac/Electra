package com.electra.mod.client;

import com.electra.mod.block.ElectricLampBlock;
import com.electra.mod.block.LampSymbol;
import com.electra.mod.blockentity.ElectricLampBlockEntity;
import com.mojang.blaze3d.vertex.PoseStack;
import com.mojang.blaze3d.vertex.VertexConsumer;
import net.minecraft.client.Minecraft;
import net.minecraft.client.renderer.LevelRenderer;
import net.minecraft.client.renderer.LightTexture;
import net.minecraft.client.renderer.MultiBufferSource;
import net.minecraft.client.renderer.RenderType;
import net.minecraft.client.renderer.blockentity.BlockEntityRenderer;
import net.minecraft.client.renderer.blockentity.BlockEntityRendererProvider;
import net.minecraft.client.renderer.texture.TextureAtlasSprite;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.world.inventory.InventoryMenu;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.state.BlockState;
import org.jetbrains.annotations.NotNull;

/**
 * Dessine le motif d'une lampe (lettre…) sur chacune de ses faces visibles, juste devant le verre.
 *
 * La texture du motif est blanche : elle prend la couleur choisie. Allumée, la lampe affiche son motif
 * en pleine lumière ; éteinte, le motif reçoit la lumière ambiante comme le reste du bloc.
 * Les faces collées à un bloc plein (une autre lampe comprise) ne sont pas dessinées.
 *
 * Sens de lecture : debout sur les 4 côtés ; sur le dessus, le haut de la lettre est vers le nord ;
 * en dessous, vers le sud.
 */
public class ElectricLampRenderer implements BlockEntityRenderer<ElectricLampBlockEntity> {

    /** Décalage devant la face, pour ne pas se confondre avec le verre. */
    private static final float OFFSET = 0.002f;

    public ElectricLampRenderer(BlockEntityRendererProvider.Context context) {}

    @Override
    public void render(@NotNull ElectricLampBlockEntity lamp, float partialTick, @NotNull PoseStack poseStack,
                       @NotNull MultiBufferSource buffers, int packedLight, int packedOverlay) {
        LampSymbol symbol = lamp.getSymbol();
        Level level = lamp.getLevel();
        if (symbol.isNone() || level == null) return;

        BlockPos pos = lamp.getBlockPos();
        BlockState state = lamp.getBlockState();
        boolean lit = state.hasProperty(ElectricLampBlock.LIT) && state.getValue(ElectricLampBlock.LIT);

        TextureAtlasSprite sprite = Minecraft.getInstance().getTextureAtlas(InventoryMenu.BLOCK_ATLAS).apply(symbol.texture());
        int color = lamp.getSymbolColor().getTextureDiffuseColor();
        int red = (color >> 16) & 0xFF, green = (color >> 8) & 0xFF, blue = color & 0xFF;

        VertexConsumer consumer = buffers.getBuffer(RenderType.text(InventoryMenu.BLOCK_ATLAS));
        PoseStack.Pose pose = poseStack.last();

        for (Direction face : Direction.values()) {
            BlockPos neighborPos = pos.relative(face);
            if (level.getBlockState(neighborPos).isSolidRender(level, neighborPos)) continue;

            int light = lit ? LightTexture.FULL_BRIGHT : LevelRenderer.getLightColor(level, neighborPos);
            float shade = level.getShade(face, true);   // même ombrage que les faces du bloc
            quad(consumer, pose, face, sprite, symbol.scale(),
                    (int) (red * shade), (int) (green * shade), (int) (blue * shade), light);
        }
    }

    /**
     * Un carré couvrant la face. R (droite) et U (haut) sont vus de l'extérieur ; R × U donne la normale
     * sortante, donc les sommets bas-gauche, bas-droite, haut-droite, haut-gauche tournent dans le sens
     * attendu par Minecraft (face visible de l'extérieur).
     */
    private static void quad(VertexConsumer consumer, PoseStack.Pose pose, Direction face, TextureAtlasSprite sprite,
                             float scale, int red, int green, int blue, int light) {
        float[] n = {face.getStepX(), face.getStepY(), face.getStepZ()};
        float[] r, u;
        switch (face) {
            case SOUTH -> { r = new float[]{1, 0, 0};  u = new float[]{0, 1, 0}; }
            case NORTH -> { r = new float[]{-1, 0, 0}; u = new float[]{0, 1, 0}; }
            case EAST  -> { r = new float[]{0, 0, -1}; u = new float[]{0, 1, 0}; }
            case WEST  -> { r = new float[]{0, 0, 1};  u = new float[]{0, 1, 0}; }
            case UP    -> { r = new float[]{1, 0, 0};  u = new float[]{0, 0, -1}; }
            default    -> { r = new float[]{1, 0, 0};  u = new float[]{0, 0, 1}; }   // DOWN
        }
        float depth = 0.5f + OFFSET;
        // (s, t) : 0 = gauche / bas, 1 = droite / haut
        float[][] corners = {{0, 0}, {1, 0}, {1, 1}, {0, 1}};
        for (float[] c : corners) {
            float s = (c[0] - 0.5f) * scale, t = (c[1] - 0.5f) * scale;
            float x = 0.5f + n[0] * depth + r[0] * s + u[0] * t;
            float y = 0.5f + n[1] * depth + r[1] * s + u[1] * t;
            float z = 0.5f + n[2] * depth + r[2] * s + u[2] * t;
            float texU = sprite.getU0() + (sprite.getU1() - sprite.getU0()) * c[0];
            float texV = sprite.getV0() + (sprite.getV1() - sprite.getV0()) * (1 - c[1]);
            consumer.addVertex(pose, x, y, z)
                    .setColor(red, green, blue, 255)
                    .setUv(texU, texV)
                    .setLight(light);
        }
    }
}
