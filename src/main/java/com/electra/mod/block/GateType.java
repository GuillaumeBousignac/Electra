package com.electra.mod.block;

import com.mojang.serialization.Codec;
import net.minecraft.util.StringRepresentable;
import org.jetbrains.annotations.NotNull;

/**
 * Composants logiques au FE. Toutes les faces sont relatives à la sortie (FACING) :
 *
 *            sortie (avant)
 *                 ▲
 *   entrée A ◄─ [porte] ─► entrée B        (gauche / droite)
 *                 ▼
 *          alimentation (arrière)
 *
 * - AND / OR : l'énergie de sortie est puisée dans les entrées ; pas d'alimentation à l'arrière.
 *   La porte OU peut passer en OU exclusif (XOR) : accroupi + clic droit (LogicGateBlock#EXCLUSIVE).
 * - NOT : les entrées ne servent que de signal ; l'énergie vient de l'arrière.
 * - SWITCH (interrupteur) : pas d'entrées ; laisse passer le courant de l'arrière vers l'avant
 *   quand il est enclenché (clic droit).
 * - CLOCK (horloge) : pas d'entrées ; coupe et rétablit le courant de l'arrière vers l'avant en boucle.
 *   Clic droit : durée de chaque phase (4 réglages, comme un répéteur, voir LogicGateBlock#DELAY).
 */
public enum GateType implements StringRepresentable {
    AND("and_gate", true, false),
    OR("or_gate", true, false),
    NOT("not_gate", true, true),
    SWITCH("energy_switch", false, true),
    CLOCK("energy_clock", false, true);

    public static final Codec<GateType> CODEC = StringRepresentable.fromEnum(GateType::values);

    private final String id;
    private final boolean hasInputs;
    private final boolean hasPowerSide;

    GateType(String id, boolean hasInputs, boolean hasPowerSide) {
        this.id = id;
        this.hasInputs = hasInputs;
        this.hasPowerSide = hasPowerSide;
    }

    /** Identifiant du bloc (and_gate, or_gate, not_gate, energy_switch, energy_clock). */
    public String id()               { return id; }
    /** Faces gauche et droite raccordables (entrées A et B). */
    public boolean hasInputs()       { return hasInputs; }
    /** Face arrière raccordable (alimentation). */
    public boolean hasPowerSide()    { return hasPowerSide; }
    /** Les entrées fournissent aussi l'énergie de sortie (AND / OR). */
    public boolean inputsCarryPower() { return hasInputs && !hasPowerSide; }

    /** Résultat logique. Interrupteur : {@code a} = enclenché ; horloge : {@code a} = phase active. */
    public boolean evaluate(boolean a, boolean b) {
        return switch (this) {
            case AND -> a && b;
            case OR -> a || b;
            case NOT -> !(a || b);
            case SWITCH, CLOCK -> a;
        };
    }

    @Override
    public @NotNull String getSerializedName() {
        return id;
    }
}
