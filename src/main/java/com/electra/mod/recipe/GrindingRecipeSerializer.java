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

public class GrindingRecipeSerializer implements RecipeSerializer<GrindingRecipe> {

    public static final MapCodec<GrindingRecipe> CODEC = RecordCodecBuilder.mapCodec(instance ->
            instance.group(
                    Ingredient.CODEC.fieldOf("input").forGetter(GrindingRecipe::getInput),
                    ItemStack.CODEC.fieldOf("result").forGetter(r -> r.getResultItem(null)),
                    Codec.INT.optionalFieldOf("energy_cost", 80).forGetter(GrindingRecipe::getEnergyCost)
            ).apply(instance, GrindingRecipe::new)
    );

    public static final StreamCodec<RegistryFriendlyByteBuf, GrindingRecipe> STREAM_CODEC =
            StreamCodec.composite(
                    Ingredient.CONTENTS_STREAM_CODEC, GrindingRecipe::getInput,
                    ItemStack.STREAM_CODEC,           r -> r.getResultItem(null),
                    ByteBufCodecs.INT,                GrindingRecipe::getEnergyCost,
                    GrindingRecipe::new
            );

    @Override
    public @NotNull MapCodec<GrindingRecipe> codec() { return CODEC; }

    @Override
    public @NotNull StreamCodec<RegistryFriendlyByteBuf, GrindingRecipe> streamCodec() { return STREAM_CODEC; }
}
