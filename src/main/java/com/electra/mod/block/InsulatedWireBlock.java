package com.electra.mod.block;

import com.mojang.serialization.MapCodec;
import com.mojang.serialization.codecs.RecordCodecBuilder;
import net.minecraft.world.item.DyeColor;
import net.minecraft.world.level.block.BaseEntityBlock;
import org.jetbrains.annotations.NotNull;

/** Câble isolé à la laine : conduit sans blesser, 16 couleurs séparant les réseaux. */
public class InsulatedWireBlock extends WireBlock {

    public static final MapCodec<InsulatedWireBlock> CODEC = RecordCodecBuilder.mapCodec(instance ->
            instance.group(
                    DyeColor.CODEC.fieldOf("color").forGetter(InsulatedWireBlock::getColor),
                    propertiesCodec()
            ).apply(instance, InsulatedWireBlock::new));

    private final DyeColor color;

    public InsulatedWireBlock(DyeColor color, Properties properties) {
        super(properties, 6);
        this.color = color;
    }

    @Override
    protected @NotNull MapCodec<? extends BaseEntityBlock> codec() {
        return CODEC;
    }

    @Override
    public @NotNull DyeColor getColor() {
        return color;
    }
}
