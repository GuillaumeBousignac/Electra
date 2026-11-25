package com.electra.mod.block;

import com.electra.mod.blockentity.WireBlockEntity;
import com.electra.mod.config.ElectraConfig;
import com.electra.mod.setup.ModDamageTypes;
import com.mojang.serialization.MapCodec;
import net.minecraft.core.BlockPos;
import net.minecraft.core.particles.ParticleTypes;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.sounds.SoundEvents;
import net.minecraft.sounds.SoundSource;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.item.DyeColor;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.BaseEntityBlock;
import net.minecraft.world.level.block.state.BlockState;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

/** Câble nu : conduit, se connecte à tout, électrocute s'il est sous tension. */
public class BareWireBlock extends WireBlock {

    public static final MapCodec<BareWireBlock> CODEC = simpleCodec(BareWireBlock::new);

    public BareWireBlock(Properties properties) {
        super(properties, 4);
    }

    @Override
    protected @NotNull MapCodec<? extends BaseEntityBlock> codec() {
        return CODEC;
    }

    @Override
    public @Nullable DyeColor getColor() {
        return null;
    }

    @Override
    protected void entityInside(@NotNull BlockState state, @NotNull Level level,
                                @NotNull BlockPos pos, @NotNull Entity entity) {
        if (level.isClientSide() || !(entity instanceof LivingEntity living)) return;
        // Dégâts uniquement si le réseau contient de l'énergie
        float damage = ElectraConfig.BARE_WIRE_SHOCK_DAMAGE.get().floatValue();
        if (damage > 0 && level.getBlockEntity(pos) instanceof WireBlockEntity wire && wire.getNetworkEnergy() > 0) {
            // Etincelles seulement quand le choc passe vraiment (pas pendant l'invulnerabilite apres un coup)
            if (living.hurt(ModDamageTypes.electrocution(level), damage) && level instanceof ServerLevel serverLevel) {
                spawnShockEffects(serverLevel, pos, living);
            }
        }
    }

    /** Gerbe d'etincelles bleues autour de la victime, et quelques-unes au point de contact avec le cable. */
    private static void spawnShockEffects(ServerLevel level, BlockPos pos, LivingEntity victim) {
        if (ElectraConfig.SHOCK_SOUND_ENABLED.get()) {
            // Un claquement sec et aigu, suivi d'un sifflement : le grésillement de l'arc électrique
            level.playSound(null, victim.getX(), victim.getY(), victim.getZ(),
                    SoundEvents.LIGHTNING_BOLT_IMPACT, SoundSource.BLOCKS, 0.3f, 2.0f);
            level.playSound(null, victim.getX(), victim.getY(), victim.getZ(),
                    SoundEvents.FIRE_EXTINGUISH, SoundSource.BLOCKS, 0.4f, 1.8f);
        }
        if (!ElectraConfig.PARTICLES_ENABLED.get()) return;
        double width = victim.getBbWidth();
        double height = victim.getBbHeight();
        level.sendParticles(ParticleTypes.ELECTRIC_SPARK,
                victim.getX(), victim.getY() + height * 0.5, victim.getZ(),
                14, width * 0.5, height * 0.4, width * 0.5, 0.25);
        level.sendParticles(ParticleTypes.ELECTRIC_SPARK,
                pos.getX() + 0.5, pos.getY() + 0.5, pos.getZ() + 0.5,
                6, 0.2, 0.2, 0.2, 0.15);
    }
}
