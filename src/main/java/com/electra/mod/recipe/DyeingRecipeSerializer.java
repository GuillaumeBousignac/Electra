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

public class DyeingRecipeSerializer implements RecipeSerializer<DyeingRecipe> {

    public static final MapCodec<DyeingRecipe> CODEC = RecordCodecBuilder.mapCodec(instance ->
            instance.group(
                    Ingredient.CODEC.fieldOf("input").forGetter(DyeingRecipe::getInput),
                    Ingredient.CODEC.fieldOf("dye").forGetter(DyeingRecipe::getDye),
                    Codec.INT.fieldOf("fluid_amount").forGetter(DyeingRecipe::getFluidAmount),
                    ItemStack.CODEC.fieldOf("result").forGetter(r -> r.getResultItem(null)),
                    Codec.INT.optionalFieldOf("energy_cost", 40).forGetter(DyeingRecipe::getEnergyCost),
                    Codec.BOOL.optionalFieldOf("keep_components", false).forGetter(DyeingRecipe::keepsComponents)
            ).apply(instance, DyeingRecipe::new)
    );

    public static final StreamCodec<RegistryFriendlyByteBuf, DyeingRecipe> STREAM_CODEC =
            StreamCodec.composite(
                    Ingredient.CONTENTS_STREAM_CODEC, DyeingRecipe::getInput,
                    Ingredient.CONTENTS_STREAM_CODEC, DyeingRecipe::getDye,
                    ByteBufCodecs.INT,                DyeingRecipe::getFluidAmount,
                    ItemStack.STREAM_CODEC,           r -> r.getResultItem(null),
                    ByteBufCodecs.INT,                DyeingRecipe::getEnergyCost,
                    ByteBufCodecs.BOOL,               DyeingRecipe::keepsComponents,
                    DyeingRecipe::new
            );

    @Override
    public @NotNull MapCodec<DyeingRecipe> codec() { return CODEC; }

    @Override
    public @NotNull StreamCodec<RegistryFriendlyByteBuf, DyeingRecipe> streamCodec() { return STREAM_CODEC; }
}
