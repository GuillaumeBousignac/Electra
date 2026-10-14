package com.electra.mod.block;

import com.electra.mod.Electra;
import net.minecraft.network.chat.Component;
import net.minecraft.resources.ResourceLocation;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Locale;

/**
 * Motif affiché sur les faces d'une lampe électrique.
 *
 * Les motifs sont rangés par famille : accroupi + clic droit fait défiler la famille courante
 * (aucun → A → B → … → Z → aucun). D'autres familles (flèches, chiffres…) pourront s'ajouter
 * ici sans toucher au reste : chaque motif a une texture block/lamp_symbol/<id>.png, dessinée en
 * blanc, que le rendu teinte de la couleur choisie.
 *
 * Sauvegarde par identifiant texte : ajouter des motifs ne décale pas ceux des mondes existants.
 */
public final class LampSymbol {

    public enum Family { NONE, ALPHABETIC }

    public static final LampSymbol NONE = new LampSymbol("none", Family.NONE);

    private static final List<LampSymbol> ALL;
    static {
        List<LampSymbol> all = new ArrayList<>();
        all.add(NONE);
        for (char c = 'a'; c <= 'z'; c++) all.add(new LampSymbol(String.valueOf(c), Family.ALPHABETIC));
        ALL = Collections.unmodifiableList(all);
    }

    private final String id;
    private final Family family;
    private final ResourceLocation texture;

    private LampSymbol(String id, Family family) {
        this.id = id;
        this.family = family;
        this.texture = ResourceLocation.fromNamespaceAndPath(Electra.MODID, "block/lamp_symbol/" + id);
    }

    public String id() { return id; }
    public Family family() { return family; }
    public boolean isNone() { return this == NONE; }

    /** Texture dans l'atlas des blocs (tout ce qui est sous textures/block/ y est assemblé). */
    public ResourceLocation texture() { return texture; }

    /** Nom affiché : la lettre en majuscule, ou « aucun ». */
    public Component displayName() {
        return switch (family) {
            case NONE -> Component.translatable("symbol.electra.none");
            case ALPHABETIC -> Component.literal(id.toUpperCase(Locale.ROOT));
        };
    }

    /**
     * Motif suivant : sans motif, on commence par la première lettre ;
     * après le dernier motif d'une famille, on revient à « aucun ».
     */
    public LampSymbol next() {
        if (isNone()) return firstOf(Family.ALPHABETIC);
        int index = ALL.indexOf(this) + 1;
        if (index < ALL.size() && ALL.get(index).family == family) return ALL.get(index);
        return NONE;
    }

    private static LampSymbol firstOf(Family family) {
        for (LampSymbol symbol : ALL) if (symbol.family == family) return symbol;
        return NONE;
    }

    /** Motif inconnu (monde ouvert avec une version plus ancienne du mod) : aucun motif. */
    public static LampSymbol byId(String id) {
        for (LampSymbol symbol : ALL) if (symbol.id.equals(id)) return symbol;
        return NONE;
    }
}
