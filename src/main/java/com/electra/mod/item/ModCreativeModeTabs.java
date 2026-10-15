package com.electra.mod.item;

import com.electra.mod.Electra;
import com.electra.mod.setup.ModBlocks;
import com.electra.mod.setup.ModItems;
import net.minecraft.core.registries.Registries;
import net.minecraft.network.chat.Component;
import net.minecraft.world.item.CreativeModeTab;
import net.minecraft.world.item.ItemStack;
import net.neoforged.bus.api.IEventBus;
import net.neoforged.neoforge.registries.DeferredRegister;

import java.util.function.Supplier;

public class ModCreativeModeTabs {

    public static final DeferredRegister<CreativeModeTab> CREATIVE_MODE_TAB =
            DeferredRegister.create(Registries.CREATIVE_MODE_TAB, Electra.MODID);

    public static final Supplier<CreativeModeTab> ELECTRA_BLOCKS_TAB = CREATIVE_MODE_TAB.register("electra_blocks_tab",
            () -> CreativeModeTab.builder()
                    .icon(() -> new ItemStack(ModBlocks.LIGHTNING_COLLECTOR.get()))
                    .title(Component.translatable("creativetab.electra.electra_blocks"))
                    .displayItems((parameters, output) -> {
                        output.accept(ModBlocks.LIGHTNING_COLLECTOR.get());
                        output.accept(ModBlocks.REDSTONE_DYNAMO.get());
                        output.accept(ModBlocks.ADVANCED_FURNACE.get());
                        output.accept(ModBlocks.REDSTONE_CONVERTER.get());
                        ModBlocks.LOGIC_GATES.values().forEach(gate -> output.accept(gate.get()));
                        output.accept(ModBlocks.ELECTRIC_LAMP.get());
                        output.accept(ModBlocks.BARE_WIRE.get());
                        ModBlocks.INSULATED_WIRES.values().forEach(wire -> output.accept(wire.get()));
                        output.accept(ModBlocks.ROSE_GOLD_BLOCK.get());
                        output.accept(ModBlocks.RED_IRON_BLOCK.get());
                    }).build());

    public static final Supplier<CreativeModeTab> ELECTRA_ITEMS_TAB = CREATIVE_MODE_TAB.register("electra_items_tab",
            () -> CreativeModeTab.builder()
                    .icon(() -> new ItemStack(ModItems.ROSE_GOLD_INGOT.get()))
                    .title(Component.translatable("creativetab.electra.electra_items"))
                    .displayItems((parameters, output) -> {
                        output.accept(ModItems.ROSE_GOLD_INGOT.get());
                        output.accept(ModItems.ROSE_GOLD_NUGGET.get());
                        output.accept(ModItems.RED_IRON_INGOT.get());
                        output.accept(ModItems.ENERGY_METER.get());
                        output.accept(ModItems.ALPHABETIC_STICKER.get());
                        output.accept(ModItems.TOOL_STICKER.get());
                        output.accept(ModItems.ITEM_STICKER.get());
                        output.accept(ModItems.NUMBER_STICKER.get());
                        output.accept(ModItems.SYMBOL_STICKER.get());
                        output.accept(ModItems.MOB_STICKER.get());
                    }).build());

    public static void register(IEventBus eventBus) {
        CREATIVE_MODE_TAB.register(eventBus);
    }
}
