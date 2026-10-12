package com.electra.mod;

import com.electra.mod.client.LightningCollectorRenderer;
import com.electra.mod.client.RedstoneDynamoRenderer;
import com.electra.mod.screen.AdvancedFurnaceScreen;
import com.electra.mod.setup.ModBlockEntities;
import com.electra.mod.setup.ModMenuTypes;
import net.neoforged.api.distmarker.Dist;
import net.neoforged.bus.api.SubscribeEvent;
import net.neoforged.fml.common.EventBusSubscriber;
import net.neoforged.fml.common.Mod;
import net.neoforged.neoforge.client.event.EntityRenderersEvent;
import net.neoforged.neoforge.client.event.ModelEvent;
import net.neoforged.neoforge.client.event.RegisterMenuScreensEvent;

// Cette classe n'est jamais chargée sur un serveur dédié.
@Mod(value = Electra.MODID, dist = Dist.CLIENT)
@EventBusSubscriber(modid = Electra.MODID, value = Dist.CLIENT)
public class ElectraClient {

    public ElectraClient() {}

    @SubscribeEvent
    public static void onRegisterScreens(RegisterMenuScreensEvent event) {
        event.register(ModMenuTypes.ADVANCED_FURNACE_MENU.get(), AdvancedFurnaceScreen::new);
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
    }
}
