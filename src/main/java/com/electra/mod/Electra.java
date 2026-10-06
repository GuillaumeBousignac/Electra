package com.electra.mod;

import com.electra.mod.event.ServerTickEvents;
import com.electra.mod.item.ModCreativeModeTabs;
import com.electra.mod.setup.*;
import com.mojang.logging.LogUtils;
import net.neoforged.bus.api.IEventBus;
import net.neoforged.fml.ModContainer;
import net.neoforged.fml.common.Mod;
import net.neoforged.neoforge.common.NeoForge;
import net.neoforged.neoforge.event.tick.ServerTickEvent;
import org.slf4j.Logger;

@Mod("electra")
public class Electra {

    public static final String MODID = "electra";
    public static final Logger LOGGER = LogUtils.getLogger();

    public Electra(IEventBus modEventBus, ModContainer modContainer) {
        ModBlocks.BLOCKS.register(modEventBus);
        ModBlocks.ITEMS.register(modEventBus);
        ModBlockEntities.BLOCK_ENTITIES.register(modEventBus);

        ModItems.ITEMS.register(modEventBus);

        ModMenuTypes.MENU_TYPES.register(modEventBus);

        NeoForge.EVENT_BUS.register(new ServerTickEvents());

        ModRecipes.SERIALIZERS.register(modEventBus);
        ModRecipes.TYPES.register(modEventBus);

        ModCreativeModeTabs.register(modEventBus);
    }
}