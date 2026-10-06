package com.electra.mod.block;

import net.minecraft.world.item.DyeColor;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.state.BlockBehaviour;
import net.minecraft.world.level.material.MapColor;

public class InsulatedWireBlock extends Block {

    private final DyeColor color;

    public InsulatedWireBlock(DyeColor color) {
        super(BlockBehaviour.Properties.of()
                .mapColor(MapColor.WOOL)
                .strength(1.0f, 2.0f)
                .noOcclusion());
        this.color = color;
    }

    public DyeColor getColor() {
        return color;
    }
}