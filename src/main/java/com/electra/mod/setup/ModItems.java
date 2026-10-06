package com.electra.mod.setup;

import com.electra.mod.Electra;
import com.electra.mod.item.EnergyMeterItem;
import net.minecraft.world.item.Item;
import net.neoforged.neoforge.registries.DeferredItem;
import net.neoforged.neoforge.registries.DeferredRegister;

public class ModItems {

    public static final DeferredRegister.Items ITEMS =
            DeferredRegister.createItems(Electra.MODID);

    public static final DeferredItem<Item> ROSE_GOLD_INGOT =
            ITEMS.register("rose_gold_ingot",
                    () -> new Item(new Item.Properties()));

    public static final DeferredItem<EnergyMeterItem> ENERGY_METER =
            ITEMS.register("energy_meter",
                    () -> new EnergyMeterItem(new Item.Properties().stacksTo(1)));
}