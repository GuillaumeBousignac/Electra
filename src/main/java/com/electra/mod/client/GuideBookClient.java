package com.electra.mod.client;

import com.electra.mod.config.ElectraConfig;
import net.minecraft.client.Minecraft;
import net.minecraft.client.resources.sounds.SimpleSoundInstance;
import net.minecraft.sounds.SoundEvent;
import net.minecraft.sounds.SoundEvents;

/** Point d'entree client du livre-guide : evite de charger les classes d'ecran sur un serveur dedie. */
public final class GuideBookClient {

    public static void open() {
        playSound(SoundEvents.BOOK_PUT, 1.0f);
        Minecraft.getInstance().setScreen(new GuideBookScreen());
    }

    /** Bruit d'interface du livre (ouverture, pages), coupe par le reglage guideSounds. */
    public static void playSound(SoundEvent sound, float pitch) {
        if (!ElectraConfig.GUIDE_SOUNDS_ENABLED.get()) return;
        Minecraft.getInstance().getSoundManager().play(SimpleSoundInstance.forUI(sound, pitch));
    }

    private GuideBookClient() {}
}
