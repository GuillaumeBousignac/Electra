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

public class FreezingRecipeSerializer implements RecipeSerializer<FreezingRecipe> {

    public static final MapCodec<FreezingRecipe> CODEC = RecordCodecBuilder.mapCodec(instance ->
            instance.group(
                    Ingredient.CODEC.optionalFieldOf("input").forGetter(FreezingRecipe::getInput),
                    Codec.INT.optionalFieldOf("input_count", 1).forGetter(FreezingRecipe::getInputCount),
                    Codec.INT.fieldOf("fluid_amount").forGetter(FreezingRecipe::getFluidAmount),
                    ItemStack.CODEC.fieldOf("result").forGetter(r -> r.getResultItem(null)),
                    Codec.INT.optionalFieldOf("energy_cost", 200).forGetter(FreezingRecipe::getEnergyCost)
            ).apply(instance, FreezingRecipe::new)
    );

    public static final StreamCodec<RegistryFriendlyByteBuf, FreezingRecipe> STREAM_CODEC =
            StreamCodec.composite(
                    ByteBufCodecs.optional(Ingredient.CONTENTS_STREAM_CODEC), FreezingRecipe::getInput,
                    ByteBufCodecs.INT,                                       FreezingRecipe::getInputCount,
                    ByteBufCodecs.INT,                                       FreezingRecipe::getFluidAmount,
                    ItemStack.STREAM_CODEC,                                  r -> r.getResultItem(null),
                    ByteBufCodecs.INT,                                       FreezingRecipe::getEnergyCost,
                    FreezingRecipe::new
            );

    @Override
    public @NotNull MapCodec<FreezingRecipe> codec() { return CODEC; }

    @Override
    public @NotNull StreamCodec<RegistryFriendlyByteBuf, FreezingRecipe> streamCodec() { return STREAM_CODEC; }
}
