package com.electra.mod.fluid;

import net.neoforged.neoforge.fluids.capability.IFluidHandler;

/**
 * Raccord de fluide d'une machine Electra, vu par le réseau de tuyaux.
 *
 * Le réseau ne vide jamais les cuves des autres mods : seules les machines Electra qui se
 * déclarent ici peuvent être pompées. Les autres raccords FE/fluide sont simplement remplis.
 */
public interface FluidPort extends IFluidHandler {

    enum Role {
        /** Fournit du fluide au réseau en permanence (pompe). Ne reçoit rien. */
        SOURCE,
        /** Stock : reçoit le surplus et ne donne qu'à la demande (réservoir). */
        BUFFER,
        /** Consommateur : reçoit seulement (arroseur). */
        SINK
    }

    Role role();
}
