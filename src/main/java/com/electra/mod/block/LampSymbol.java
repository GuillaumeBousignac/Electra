package com.electra.mod.block;

import com.electra.mod.Electra;
import net.minecraft.network.chat.Component;
import net.minecraft.resources.ResourceLocation;
import org.jetbrains.annotations.Nullable;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Locale;

/**
 * Motif affiché sur les faces d'une lampe électrique.
 *
 * Les motifs sont rangés par famille, une famille par autocollant (LampStickerItem) : coller
 * l'autocollant pose le premier motif de la famille, puis accroupi + clic droit fait défiler
 * la famille (A → B → … → Z → A). D'autres familles pourront s'ajouter ici avec leur autocollant,
 * sans toucher au reste.
 *
 * Deux origines de texture, toujours en blanc ou en gris pour que le rendu la teinte :
 *   MOD  : dessinée pour Electra, textures/block/lamp_symbol/<id>.png, déjà à la taille du verre ;
 *   GAME : fabriquée au chargement à partir d'une texture de Minecraft (SilhouetteSpriteSource,
 *          déclarée dans assets/minecraft/atlases/blocks.json), réduite à la taille du verre.
 *
 * Sauvegarde par identifiant texte : ajouter des motifs ne décale pas ceux des mondes existants.
 */
public final class LampSymbol {

    public enum Family { NONE, ALPHABETIC, NUMBERS, TOOLS, ITEMS, SYMBOLS, MOBS }

    private enum Source { MOD, GAME }

    public static final LampSymbol NONE = new LampSymbol("none", Family.NONE, Source.MOD, null);

    private static final List<LampSymbol> ALL;
    static {
        List<LampSymbol> all = new ArrayList<>();
        all.add(NONE);
        for (char c = 'a'; c <= 'z'; c++) {
            all.add(new LampSymbol(String.valueOf(c), Family.ALPHABETIC, Source.MOD, String.valueOf(c).toUpperCase(Locale.ROOT)));
        }
        for (char c = '0'; c <= '9'; c++) {
            all.add(new LampSymbol(String.valueOf(c), Family.NUMBERS, Source.MOD, String.valueOf(c)));
        }
        String[][] maths = {{"plus", "+"}, {"minus", "−"}, {"times", "×"}, {"divide", "÷"}, {"equals", "="},
                            {"percent", "%"}, {"less", "<"}, {"greater", ">"}};
        for (String[] math : maths) all.add(new LampSymbol(math[0], Family.NUMBERS, Source.MOD, math[1]));
        for (String tool : new String[]{"pickaxe", "shovel", "sword", "axe", "hoe", "bow", "crossbow",
                                        "trident", "mace", "shears", "bucket"}) {
            all.add(new LampSymbol(tool, Family.TOOLS, Source.GAME, null));
        }
        // Objets du jeu : silhouettes, comme les outils
        for (String item : new String[]{"ingot", "stick", "diamond", "dust", "emerald", "amethyst", "coal", "gunpowder", "ender_pearl", "nether_star", "apple", "bread", "bone", "string", "feather", "book"}) {
            all.add(new LampSymbol(item, Family.ITEMS, Source.GAME, null));
        }
        for (String sign : new String[]{"warning", "exclamation", "question", "lightning", "heart", "star",
                                        "check", "cross", "arrow_up", "arrow_right", "arrow_down", "arrow_left",
                                        "power"}) {
            all.add(new LampSymbol(sign, Family.SYMBOLS, Source.MOD, null));
        }
        // Visages des créatures : pris dans les textures du jeu
        for (String mob : new String[]{"creeper", "skeleton", "zombie", "spider", "villager", "pig", "cow"}) {
            all.add(new LampSymbol(mob, Family.MOBS, Source.GAME, null));
        }
        ALL = Collections.unmodifiableList(all);
    }

    private final String id;
    private final Family family;
    private final ResourceLocation texture;
    private final float scale;
    /** Texte affiché tel quel (lettre, chiffre, signe) ; null : nom traduit symbol.electra.<id>. */
    @Nullable private final String literal;

    private LampSymbol(String id, Family family, Source source, @Nullable String literal) {
        this.id = id;
        this.family = family;
        this.literal = literal;
        this.texture = ResourceLocation.fromNamespaceAndPath(Electra.MODID,
                (source == Source.GAME ? "lamp_symbol/" : "block/lamp_symbol/") + id);
        // Les textures du jeu occupent toute leur image : réduites au verre (12 px sur 16)
        // pour ne pas recouvrir la bordure en cuivre
        this.scale = source == Source.GAME ? 12f / 16f : 1f;
    }

    public String id() { return id; }
    public Family family() { return family; }
    public boolean isNone() { return this == NONE; }

    /** Texture dans l'atlas des blocs. */
    public ResourceLocation texture() { return texture; }

    /** Taille du motif sur la face (1 = toute la face). */
    public float scale() { return scale; }

    /** Nom affiché : la lettre, le chiffre ou le signe tel quel, sinon le nom traduit (« aucun » compris). */
    public Component displayName() {
        if (literal != null) return Component.literal(literal);
        return Component.translatable("symbol.electra." + id);
    }

    /** Motif suivant de la même famille ; après le dernier, on revient au premier. « Aucun » reste « aucun ». */
    public LampSymbol next() {
        if (isNone()) return NONE;
        int index = ALL.indexOf(this) + 1;
        if (index < ALL.size() && ALL.get(index).family == family) return ALL.get(index);
        return firstOf(family);
    }

    /** Premier motif d'une famille : celui que pose un autocollant neuf. */
    public static LampSymbol firstOf(Family family) {
        for (LampSymbol symbol : ALL) if (symbol.family == family) return symbol;
        return NONE;
    }

    /** Motif inconnu (monde ouvert avec une version plus ancienne du mod) : aucun motif. */
    public static LampSymbol byId(String id) {
        for (LampSymbol symbol : ALL) if (symbol.id.equals(id)) return symbol;
        return NONE;
    }
}
