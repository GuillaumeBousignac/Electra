package com.electra.mod.client;

import net.minecraft.client.Minecraft;

/** Point d'entree client du livre-guide : evite de charger les classes d'ecran sur un serveur dedie. */
public final class GuideBookClient {

    public static void open() {
        Minecraft.getInstance().setScreen(new GuideBookScreen());
    }

    private GuideBookClient() {}
}
