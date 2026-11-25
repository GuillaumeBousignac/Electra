package com.electra.mod.block;

import com.electra.mod.config.ElectraConfig;
import net.minecraft.core.BlockPos;
import net.minecraft.sounds.SoundEvent;
import net.minecraft.sounds.SoundEvents;
import net.minecraft.sounds.SoundSource;
import net.minecraft.util.RandomSource;
import net.minecraft.world.level.Level;

/**
 * Bruits des machines en marche (appeles depuis animateTick, donc cote client uniquement, sans paquet reseau).
 * Minecraft n'anime qu'un echantillon de blocs : un appel tombe en moyenne toutes les 2 a 3 secondes
 * pour une machine proche, ce qui donne un bruit de fond discret et irregulier.
 */
public final class MachineSounds {

    /** Probabilite de jouer le son a chaque appel (le reste du temps, la machine reste silencieuse). */
    private static final float CHANCE = 0.6f;

    private MachineSounds() {}

    public static void furnace(Level level, BlockPos pos, RandomSource random) {
        play(level, pos, random, SoundEvents.FURNACE_FIRE_CRACKLE, 0.8f, 0.9f);
    }

    public static void grinder(Level level, BlockPos pos, RandomSource random) {
        play(level, pos, random, SoundEvents.GRINDSTONE_USE, 0.4f, 1.0f);
    }

    public static void debarker(Level level, BlockPos pos, RandomSource random) {
        play(level, pos, random, SoundEvents.AXE_STRIP, 0.4f, 1.0f);
    }

    public static void freezer(Level level, BlockPos pos, RandomSource random) {
        play(level, pos, random, SoundEvents.SNOW_BREAK, 0.5f, 1.2f);
    }

    public static void concreteMixer(Level level, BlockPos pos, RandomSource random) {
        play(level, pos, random, SoundEvents.SAND_BREAK, 0.5f, 0.8f);
    }

    public static void dyer(Level level, BlockPos pos, RandomSource random) {
        play(level, pos, random, SoundEvents.DYE_USE, 0.5f, 1.0f);
    }

    public static void oxidation(Level level, BlockPos pos, RandomSource random) {
        play(level, pos, random, SoundEvents.AXE_SCRAPE, 0.4f, 1.0f);
    }

    private static void play(Level level, BlockPos pos, RandomSource random, SoundEvent sound,
                             float volume, float basePitch) {
        if (!ElectraConfig.MACHINE_SOUNDS_ENABLED.get() || random.nextFloat() >= CHANCE) return;
        // Hauteur legerement variable pour eviter l'effet de repetition
        float pitch = basePitch + (random.nextFloat() - 0.5f) * 0.2f;
        level.playLocalSound(pos.getX() + 0.5, pos.getY() + 0.5, pos.getZ() + 0.5,
                sound, SoundSource.BLOCKS, volume, pitch, false);
    }
}
