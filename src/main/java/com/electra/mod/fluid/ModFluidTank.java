package com.electra.mod.fluid;

import net.minecraft.core.HolderLookup;
import net.minecraft.nbt.CompoundTag;
import net.neoforged.neoforge.fluids.FluidStack;
import net.neoforged.neoforge.fluids.capability.templates.FluidTank;

import java.util.function.Predicate;

/** Cuve avec rappel à chaque modification (pour marquer le bloc comme modifié et le synchroniser). */
public class ModFluidTank extends FluidTank {

    private final Runnable onChange;

    public ModFluidTank(int capacity, Predicate<FluidStack> validator, Runnable onChange) {
        super(capacity, validator);
        this.onChange = onChange;
    }

    @Override
    protected void onContentsChanged() {
        onChange.run();
    }

    public void save(CompoundTag tag, HolderLookup.Provider registries) {
        FluidHelper.save(tag, "fluid", fluid, registries);
    }

    /** Au chargement : ne déclenche pas le rappel. */
    public void load(CompoundTag tag, HolderLookup.Provider registries) {
        setFluid(FluidHelper.load(tag, "fluid", registries));
    }
}
