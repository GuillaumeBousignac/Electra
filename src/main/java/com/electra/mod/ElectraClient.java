package com.electra.mod;

import com.electra.mod.block.ElectricLampBlock;
import com.electra.mod.client.ElectricLampRenderer;
import com.electra.mod.client.LightningCollectorRenderer;
import com.electra.mod.client.RedstoneDynamoRenderer;
import com.electra.mod.client.SilhouetteSpriteSource;
import com.electra.mod.screen.AdvancedFurnaceScreen;
import com.electra.mod.setup.ModBlockEntities;
import com.electra.mod.setup.ModBlocks;
import com.electra.mod.setup.ModMenuTypes;
import net.neoforged.api.distmarker.Dist;
import net.neoforged.bus.api.SubscribeEvent;
import net.neoforged.fml.common.EventBusSubscriber;
import net.neoforged.fml.common.Mod;
import net.neoforged.neoforge.client.event.EntityRenderersEvent;
import net.neoforged.neoforge.client.event.ModelEvent;
import net.neoforged.neoforge.client.event.RegisterColorHandlersEvent;
import net.neoforged.neoforge.client.event.RegisterMenuScreensEvent;
import net.neoforged.neoforge.client.event.RegisterSpriteSourceTypesEvent;

// Cette classe n'est jamais chargée sur un serveur dédié.
@Mod(value = Electra.MODID, dist = Dist.CLIENT)
@EventBusSubscriber(modid = Electra.MODID, value = Dist.CLIENT)
public class ElectraClient {

    public ElectraClient() {}

    @SubscribeEvent
    public static void onRegisterScreens(RegisterMenuScreensEvent event) {
        event.register(ModMenuTypes.ADVANCED_FURNACE_MENU.get(), AdvancedFurnaceScreen::new);
    }

    /** Lampe électrique : le verre (tintindex 0) prend la couleur de la teinture appliquée. */
    @SubscribeEvent
    public static void onRegisterBlockColors(RegisterColorHandlersEvent.Block event) {
        event.register((state, level, pos, tintIndex) -> tintIndex == 0
                        ? state.getValue(ElectricLampBlock.COLOR).getTextureDiffuseColor()
                        : 0xFFFFFFFF,
                ModBlocks.ELECTRIC_LAMP.get());
    }

    /** Silhouettes des outils de Minecraft pour les motifs de lampe (voir assets/minecraft/atlases/blocks.json). */
    @SubscribeEvent
    public static void onRegisterSpriteSources(RegisterSpriteSourceTypesEvent event) {
        event.register(SilhouetteSpriteSource.ID, SilhouetteSpriteSource.TYPE);
    }

    /** Modèles dessinés par le code, sans bloc associé : le bras de la Redstone Dynamo. */
    @SubscribeEvent
    public static void onRegisterAdditionalModels(ModelEvent.RegisterAdditional event) {
        event.register(RedstoneDynamoRenderer.ARM_MODEL);
    }

    @SubscribeEvent
    public static void onRegisterRenderers(EntityRenderersEvent.RegisterRenderers event) {
        event.registerBlockEntityRenderer(ModBlockEntities.LIGHTNING_COLLECTOR_BE.get(), LightningCollectorRenderer::new);
        event.registerBlockEntityRenderer(ModBlockEntities.REDSTONE_DYNAMO_BE.get(), RedstoneDynamoRenderer::new);
        event.registerBlockEntityRenderer(ModBlockEntities.ELECTRIC_LAMP_BE.get(), ElectricLampRenderer::new);
    }
}
