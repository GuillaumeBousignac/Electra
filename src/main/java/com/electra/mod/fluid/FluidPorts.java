package com.electra.mod.fluid;

import net.neoforged.neoforge.fluids.FluidStack;
import net.neoforged.neoforge.fluids.capability.IFluidHandler;
import org.jetbrains.annotations.NotNull;

/** Fabrique de raccords : enveloppe une cuve pour lui donner un rôle et restreindre le sens du flux. */
public final class FluidPorts {

    private FluidPorts() {}

    /** Sortie seule : le réseau peut y puiser, personne ne peut y verser. */
    public static FluidPort source(IFluidHandler tank) {
        return new Port(tank, FluidPort.Role.SOURCE, false, true);
    }

    /** Entrée et sortie : stock tampon. */
    public static FluidPort buffer(IFluidHandler tank) {
        return new Port(tank, FluidPort.Role.BUFFER, true, true);
    }

    /** Entrée seule : le fluide ne peut pas en être retiré. */
    public static FluidPort sink(IFluidHandler tank) {
        return new Port(tank, FluidPort.Role.SINK, true, false);
    }

    private static final class Port implements FluidPort {

        private final IFluidHandler tank;
        private final Role role;
        private final boolean canFill;
        private final boolean canDrain;

        private Port(IFluidHandler tank, Role role, boolean canFill, boolean canDrain) {
            this.tank = tank;
            this.role = role;
            this.canFill = canFill;
            this.canDrain = canDrain;
        }

        @Override public Role role() { return role; }

        @Override public int getTanks() { return tank.getTanks(); }

        @Override public @NotNull FluidStack getFluidInTank(int index) { return tank.getFluidInTank(index); }

        @Override public int getTankCapacity(int index) { return tank.getTankCapacity(index); }

        @Override
        public boolean isFluidValid(int index, @NotNull FluidStack stack) {
            return canFill && tank.isFluidValid(index, stack);
        }

        @Override
        public int fill(@NotNull FluidStack resource, @NotNull FluidAction action) {
            return canFill ? tank.fill(resource, action) : 0;
        }

        @Override
        public @NotNull FluidStack drain(@NotNull FluidStack resource, @NotNull FluidAction action) {
            return canDrain ? tank.drain(resource, action) : FluidStack.EMPTY;
        }

        @Override
        public @NotNull FluidStack drain(int maxDrain, @NotNull FluidAction action) {
            return canDrain ? tank.drain(maxDrain, action) : FluidStack.EMPTY;
        }
    }
}
