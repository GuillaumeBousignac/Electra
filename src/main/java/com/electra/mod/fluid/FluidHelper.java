package com.electra.mod.fluid;

import net.minecraft.core.HolderLookup;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.nbt.Tag;
import net.neoforged.neoforge.fluids.FluidStack;

/** Petits utilitaires sur FluidStack (jamais de modification de FluidStack.EMPTY). */
public final class FluidHelper {

    private FluidHelper() {}

    /** Copie du fluide avec une autre quantité ; EMPTY si la quantité est nulle. */
    public static FluidStack withAmount(FluidStack stack, int amount) {
        if (stack.isEmpty() || amount <= 0) return FluidStack.EMPTY;
        FluidStack copy = stack.copy();
        copy.setAmount(amount);
        return copy;
    }

    public static FluidStack copyOf(FluidStack stack) {
        return stack.isEmpty() ? FluidStack.EMPTY : stack.copy();
    }

    public static void save(CompoundTag tag, String key, FluidStack stack, HolderLookup.Provider registries) {
        tag.put(key, stack.saveOptional(registries));
    }

    public static FluidStack load(CompoundTag tag, String key, HolderLookup.Provider registries) {
        if (!tag.contains(key, Tag.TAG_COMPOUND)) return FluidStack.EMPTY;
        return FluidStack.parseOptional(registries, tag.getCompound(key));
    }
}
