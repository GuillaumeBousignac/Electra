package com.electra.mod.block;

import com.electra.mod.config.ElectraConfig;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.core.particles.BlockParticleOption;
import net.minecraft.core.particles.ParticleOptions;
import net.minecraft.core.particles.ParticleTypes;
import net.minecraft.util.RandomSource;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.Blocks;

/**
 * Particules des machines en marche (appelees depuis animateTick, donc cote client uniquement).
 * Chaque appel est rare (Minecraft n'anime qu'un petit echantillon de blocs par tick) :
 * on fait donc apparaitre quelques particules d'un coup pour que l'effet reste visible.
 */
public final class MachineParticles {

    private MachineParticles() {}

    /** Flammes et fumee devant la porte, comme un four vanilla (four avance). */
    public static void furnace(Level level, BlockPos pos, Direction facing, RandomSource random) {
        if (!ElectraConfig.PARTICLES_ENABLED.get()) return;
        for (int i = 0; i < 3; i++) {
            double spread = random.nextDouble() * 0.6 - 0.3;
            double x = pos.getX() + 0.5 + (facing.getAxis() == Direction.Axis.X ? facing.getStepX() * 0.52 : spread);
            double y = pos.getY() + random.nextDouble() * 6.0 / 16.0;
            double z = pos.getZ() + 0.5 + (facing.getAxis() == Direction.Axis.Z ? facing.getStepZ() * 0.52 : spread);
            level.addParticle(ParticleTypes.SMOKE, x, y, z, 0.0, 0.0, 0.0);
            level.addParticle(ParticleTypes.FLAME, x, y, z, 0.0, 0.0, 0.0);
        }
    }

    /** Eclats du bloc donne, projetes au-dessus de la machine (broyeur, ecorceuse, station d'oxydation). */
    public static void chips(Level level, BlockPos pos, RandomSource random,
                             net.minecraft.world.level.block.Block block) {
        top(level, pos, random, new BlockParticleOption(ParticleTypes.BLOCK, block.defaultBlockState()), 5, 0.05);
    }

    public static void grinder(Level level, BlockPos pos, RandomSource random) {
        chips(level, pos, random, Blocks.COBBLESTONE);
    }

    public static void debarker(Level level, BlockPos pos, RandomSource random) {
        chips(level, pos, random, Blocks.OAK_PLANKS);
    }

    /** Patine verte du cuivre oxyde. */
    public static void oxidation(Level level, BlockPos pos, RandomSource random) {
        chips(level, pos, random, Blocks.OXIDIZED_COPPER);
    }

    /** Flocons de neige qui flottent au-dessus du congelateur. */
    public static void freezer(Level level, BlockPos pos, RandomSource random) {
        top(level, pos, random, ParticleTypes.SNOWFLAKE, 4, 0.03);
    }

    /** Poussiere grise qui retombe sur la betonniere. */
    public static void concreteMixer(Level level, BlockPos pos, RandomSource random) {
        top(level, pos, random,
                new BlockParticleOption(ParticleTypes.FALLING_DUST, Blocks.GRAY_CONCRETE_POWDER.defaultBlockState()),
                4, 0.0);
    }

    /** Gouttes d'eau du teinturier. */
    public static void dyer(Level level, BlockPos pos, RandomSource random) {
        top(level, pos, random, ParticleTypes.FALLING_WATER, 3, 0.0);
    }

    /** Fait apparaitre count particules sur la face superieure du bloc. */
    private static void top(Level level, BlockPos pos, RandomSource random, ParticleOptions particle,
                            int count, double upwardSpeed) {
        if (!ElectraConfig.PARTICLES_ENABLED.get()) return;
        for (int i = 0; i < count; i++) {
            double x = pos.getX() + 0.2 + random.nextDouble() * 0.6;
            double y = pos.getY() + 1.0;
            double z = pos.getZ() + 0.2 + random.nextDouble() * 0.6;
            level.addParticle(particle, x, y, z,
                    (random.nextDouble() - 0.5) * 0.02, upwardSpeed, (random.nextDouble() - 0.5) * 0.02);
        }
    }
}
