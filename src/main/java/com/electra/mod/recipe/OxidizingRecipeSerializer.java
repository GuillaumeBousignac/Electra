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

public class OxidizingRecipeSerializer implements RecipeSerializer<OxidizingRecipe> {

    public static final MapCodec<OxidizingRecipe> CODEC = RecordCodecBuilder.mapCodec(instance ->
            instance.group(
                    Ingredient.CODEC.fieldOf("input").forGetter(OxidizingRecipe::getInput),
                    Codec.INT.optionalFieldOf("input_count", 1).forGetter(OxidizingRecipe::getInputCount),
                    Codec.INT.fieldOf("fluid_amount").forGetter(OxidizingRecipe::getFluidAmount),
                    ItemStack.CODEC.fieldOf("result").forGetter(r -> r.getResultItem(null)),
                    Codec.INT.optionalFieldOf("energy_cost", 60).forGetter(OxidizingRecipe::getEnergyCost)
            ).apply(instance, OxidizingRecipe::new)
    );

    public static final StreamCodec<RegistryFriendlyByteBuf, OxidizingRecipe> STREAM_CODEC =
            StreamCodec.composite(
                    Ingredient.CONTENTS_STREAM_CODEC, OxidizingRecipe::getInput,
                    ByteBufCodecs.INT,                                       OxidizingRecipe::getInputCount,
                    ByteBufCodecs.INT,                                       OxidizingRecipe::getFluidAmount,
                    ItemStack.STREAM_CODEC,                                  r -> r.getResultItem(null),
                    ByteBufCodecs.INT,                                       OxidizingRecipe::getEnergyCost,
                    OxidizingRecipe::new
            );

    @Override
    public @NotNull MapCodec<OxidizingRecipe> codec() { return CODEC; }

    @Override
    public @NotNull StreamCodec<RegistryFriendlyByteBuf, OxidizingRecipe> streamCodec() { return STREAM_CODEC; }
}
