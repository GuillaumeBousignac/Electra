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

public class DebarkingRecipeSerializer implements RecipeSerializer<DebarkingRecipe> {

    public static final MapCodec<DebarkingRecipe> CODEC = RecordCodecBuilder.mapCodec(instance ->
            instance.group(
                    Ingredient.CODEC.fieldOf("input").forGetter(DebarkingRecipe::getInput),
                    ItemStack.CODEC.fieldOf("result").forGetter(r -> r.getResultItem(null)),
                    Codec.INT.optionalFieldOf("energy_cost", 40).forGetter(DebarkingRecipe::getEnergyCost)
            ).apply(instance, DebarkingRecipe::new)
    );

    public static final StreamCodec<RegistryFriendlyByteBuf, DebarkingRecipe> STREAM_CODEC =
            StreamCodec.composite(
                    Ingredient.CONTENTS_STREAM_CODEC, DebarkingRecipe::getInput,
                    ItemStack.STREAM_CODEC,           r -> r.getResultItem(null),
                    ByteBufCodecs.INT,                DebarkingRecipe::getEnergyCost,
                    DebarkingRecipe::new
            );

    @Override
    public @NotNull MapCodec<DebarkingRecipe> codec() { return CODEC; }

    @Override
    public @NotNull StreamCodec<RegistryFriendlyByteBuf, DebarkingRecipe> streamCodec() { return STREAM_CODEC; }
}
