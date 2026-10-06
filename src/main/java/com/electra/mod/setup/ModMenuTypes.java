package com.electra.mod.setup;

import com.electra.mod.Electra;
import com.electra.mod.menu.AdvancedFurnaceMenu;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.world.inventory.MenuType;
import net.neoforged.neoforge.common.extensions.IMenuTypeExtension;
import net.neoforged.neoforge.registries.DeferredHolder;
import net.neoforged.neoforge.registries.DeferredRegister;

public class ModMenuTypes {

    public static final DeferredRegister<MenuType<?>> MENU_TYPES =
            DeferredRegister.create(BuiltInRegistries.MENU, Electra.MODID);

    public static final DeferredHolder<MenuType<?>, MenuType<AdvancedFurnaceMenu>> ADVANCED_FURNACE_MENU =
            MENU_TYPES.register("advanced_furnace_menu",
                    () -> IMenuTypeExtension.create(AdvancedFurnaceMenu::new));
}