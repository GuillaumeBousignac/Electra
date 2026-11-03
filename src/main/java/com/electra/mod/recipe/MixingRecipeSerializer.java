package com.electra.mod.recipe;

import com.mojang.serialization.Codec;
import com.mojang.serialization.MapCodec;
import com.mojang.serialization.codecs.RecordCodecBuilder;
import net.minecraft.network.RegistryFriendlyByteBuf;
import net.minecraft.network.codec.ByteBufCodecs;
import net.minecraft.network.codec.StreamCodec;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.crafting.Ingredient;
import net.minecraft.world.item.crafting.RecipeSerializer;
import org.jetbrains.annotations.NotNull;

public class MixingRecipeSerializer implements RecipeSerializer<MixingRecipe> {

    public static final MapCodec<MixingRecipe> CODEC = RecordCodecBuilder.mapCodec(instance ->
            instance.group(
                    Ingredient.CODEC.fieldOf("input").forGetter(MixingRecipe::getInput),
                    Codec.INT.optionalFieldOf("input_count", 1).forGetter(MixingRecipe::getInputCount),
                    Codec.INT.fieldOf("fluid_amount").forGetter(MixingRecipe::getFluidAmount),
                    ItemStack.CODEC.fieldOf("result").forGetter(r -> r.getResultItem(null)),
                    Codec.INT.optionalFieldOf("energy_cost", 100).forGetter(MixingRecipe::getEnergyCost)
            ).apply(instance, MixingRecipe::new)
    );

    public static final StreamCodec<RegistryFriendlyByteBuf, MixingRecipe> STREAM_CODEC =
            StreamCodec.composite(
                    Ingredient.CONTENTS_STREAM_CODEC, MixingRecipe::getInput,
                    ByteBufCodecs.INT,                                       MixingRecipe::getInputCount,
                    ByteBufCodecs.INT,                                       MixingRecipe::getFluidAmount,
                    ItemStack.STREAM_CODEC,                                  r -> r.getResultItem(null),
                    ByteBufCodecs.INT,                                       MixingRecipe::getEnergyCost,
                    MixingRecipe::new
            );

    @Override
    public @NotNull MapCodec<MixingRecipe> codec() { return CODEC; }

    @Override
    public @NotNull StreamCodec<RegistryFriendlyByteBuf, MixingRecipe> streamCodec() { return STREAM_CODEC; }
}
