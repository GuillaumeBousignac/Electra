package com.electra.mod.recipe;

import com.mojang.serialization.Codec;
import com.mojang.serialization.MapCodec;
import com.mojang.serialization.codecs.RecordCodecBuilder;
import net.minecraft.network.RegistryFriendlyByteBuf;
import net.minecraft.network.codec.StreamCodec;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.crafting.Ingredient;
import net.minecraft.world.item.crafting.RecipeSerializer;
import org.jetbrains.annotations.NotNull;

public class AlloyingRecipeSerializer implements RecipeSerializer<AlloyingRecipe> {

    public static final MapCodec<AlloyingRecipe> CODEC = RecordCodecBuilder.mapCodec(instance ->
            instance.group(
                    Ingredient.CODEC.fieldOf("input1").forGetter(AlloyingRecipe::getInput1),
                    Ingredient.CODEC.fieldOf("input2").forGetter(AlloyingRecipe::getInput2),
                    ItemStack.CODEC.fieldOf("result").forGetter(r -> r.getResultItem(null)),
                    Codec.INT.optionalFieldOf("energy_cost", 100).forGetter(AlloyingRecipe::getEnergyCost)
            ).apply(instance, AlloyingRecipe::new)
    );

    public static final StreamCodec<RegistryFriendlyByteBuf, AlloyingRecipe> STREAM_CODEC =
            StreamCodec.composite(
                    Ingredient.CONTENTS_STREAM_CODEC, AlloyingRecipe::getInput1,
                    Ingredient.CONTENTS_STREAM_CODEC, AlloyingRecipe::getInput2,
                    ItemStack.STREAM_CODEC,           r -> r.getResultItem(null),
                    net.minecraft.network.codec.ByteBufCodecs.INT, AlloyingRecipe::getEnergyCost,
                    AlloyingRecipe::new
            );

    @Override
    public @NotNull MapCodec<AlloyingRecipe> codec() { return CODEC; }

    @Override
    public @NotNull StreamCodec<RegistryFriendlyByteBuf, AlloyingRecipe> streamCodec() { return STREAM_CODEC; }
}