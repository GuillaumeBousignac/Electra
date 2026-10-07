package com.electra.mod;

import com.electra.mod.screen.AdvancedFurnaceScreen;
import com.electra.mod.setup.ModMenuTypes;
import net.neoforged.api.distmarker.Dist;
import net.neoforged.bus.api.SubscribeEvent;
import net.neoforged.fml.common.EventBusSubscriber;
import net.neoforged.fml.common.Mod;
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
}
