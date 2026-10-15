package com.electra.mod.setup;

import com.electra.mod.Electra;
import com.electra.mod.block.LampSymbol;
import com.electra.mod.item.EnergyMeterItem;
import com.electra.mod.item.LampStickerItem;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.neoforged.neoforge.registries.DeferredItem;
import net.neoforged.neoforge.registries.DeferredRegister;

public class ModItems {

    public static final DeferredRegister.Items ITEMS =
            DeferredRegister.createItems(Electra.MODID);

    public static final DeferredItem<Item> ROSE_GOLD_INGOT =
            ITEMS.register("rose_gold_ingot",
                    () -> new Item(new Item.Properties()));

    public static final DeferredItem<Item> ROSE_GOLD_NUGGET =
            ITEMS.register("rose_gold_nugget",
                    () -> new Item(new Item.Properties()));

    /** Alliage fer + redstone : propriétés magnétiques, utilisé par la Redstone Dynamo. */
    public static final DeferredItem<Item> RED_IRON_INGOT =
            ITEMS.register("red_iron_ingot",
                    () -> new Item(new Item.Properties()));

    public static final DeferredItem<EnergyMeterItem> ENERGY_METER =
            ITEMS.register("energy_meter",
                    () -> new EnergyMeterItem(new Item.Properties().stacksTo(1)));

    /** Autocollant de lettres pour les lampes électriques. */
    public static final DeferredItem<LampStickerItem> ALPHABETIC_STICKER =
            ITEMS.register("alphabetic_sticker",
                    () -> new LampStickerItem(LampSymbol.Family.ALPHABETIC, new Item.Properties()));

    /** Autocollant d'outils (pioche, pelle, épée…) pour les lampes électriques. */
    public static final DeferredItem<LampStickerItem> TOOL_STICKER =
            ITEMS.register("tool_sticker",
                    () -> new LampStickerItem(LampSymbol.Family.TOOLS, new Item.Properties()));

    /** Autocollant de symboles de signalisation (attention, flèches, éclair…) pour les lampes électriques. */
    public static final DeferredItem<LampStickerItem> SYMBOL_STICKER =
            ITEMS.register("symbol_sticker",
                    () -> new LampStickerItem(LampSymbol.Family.SYMBOLS, new Item.Properties()));

    /** Autocollant de chiffres et de signes mathématiques pour les lampes électriques. */
    public static final DeferredItem<LampStickerItem> NUMBER_STICKER =
            ITEMS.register("number_sticker",
                    () -> new LampStickerItem(LampSymbol.Family.NUMBERS, new Item.Properties()));

    /** Autocollant de visages de créatures pour les lampes électriques. */
    public static final DeferredItem<LampStickerItem> MOB_STICKER =
            ITEMS.register("mob_sticker",
                    () -> new LampStickerItem(LampSymbol.Family.MOBS, new Item.Properties()));

    /** Autocollant d'objets (lingot, diamant, poudre…) pour les lampes électriques. */
    public static final DeferredItem<LampStickerItem> ITEM_STICKER =
            ITEMS.register("item_sticker",
                    () -> new LampStickerItem(LampSymbol.Family.ITEMS, new Item.Properties()));

    /** Autocollant rendu quand on retire un motif de cette famille (cisaille, lampe cassée). */
    public static ItemStack stickerFor(LampSymbol.Family family) {
        return switch (family) {
            case ALPHABETIC -> new ItemStack(ALPHABETIC_STICKER.get());
            case TOOLS -> new ItemStack(TOOL_STICKER.get());
            case ITEMS -> new ItemStack(ITEM_STICKER.get());
            case SYMBOLS -> new ItemStack(SYMBOL_STICKER.get());
            case NUMBERS -> new ItemStack(NUMBER_STICKER.get());
            case MOBS -> new ItemStack(MOB_STICKER.get());
            case NONE -> ItemStack.EMPTY;
        };
    }
}
