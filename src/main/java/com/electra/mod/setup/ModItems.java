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

    /** Autocollant rendu quand on retire un motif de cette famille (cisaille, lampe cassée). */
    public static ItemStack stickerFor(LampSymbol.Family family) {
        return switch (family) {
            case ALPHABETIC -> new ItemStack(ALPHABETIC_STICKER.get());
            case NONE -> ItemStack.EMPTY;
        };
    }
}
