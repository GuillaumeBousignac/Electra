package com.electra.mod.block;

import com.electra.mod.blockentity.WireBlockEntity;
import com.electra.mod.setup.ModDamageTypes;
import com.mojang.serialization.MapCodec;
import net.minecraft.core.BlockPos;
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
    public static final float SHOCK_DAMAGE = 4.0f;

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
        if (level.getBlockEntity(pos) instanceof WireBlockEntity wire && wire.getNetworkEnergy() > 0) {
            living.hurt(ModDamageTypes.electrocution(level), SHOCK_DAMAGE);
        }
    }
}
