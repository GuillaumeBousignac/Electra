package com.electra.mod.setup;

import com.electra.mod.Electra;
import com.electra.mod.menu.AdvancedFurnaceMenu;
import com.electra.mod.menu.ConcreteMixerMenu;
import com.electra.mod.menu.DebarkerMenu;
import com.electra.mod.menu.DyerMenu;
import com.electra.mod.menu.OxidationStationMenu;
import com.electra.mod.menu.FreezerMenu;
import com.electra.mod.menu.GrinderMenu;
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

    public static final DeferredHolder<MenuType<?>, MenuType<GrinderMenu>> GRINDER_MENU =
            MENU_TYPES.register("grinder_menu",
                    () -> IMenuTypeExtension.create(GrinderMenu::new));

    public static final DeferredHolder<MenuType<?>, MenuType<FreezerMenu>> FREEZER_MENU =
            MENU_TYPES.register("freezer_menu",
                    () -> IMenuTypeExtension.create(FreezerMenu::new));

    public static final DeferredHolder<MenuType<?>, MenuType<DebarkerMenu>> DEBARKER_MENU =
            MENU_TYPES.register("debarker_menu",
                    () -> IMenuTypeExtension.create(DebarkerMenu::new));

    public static final DeferredHolder<MenuType<?>, MenuType<ConcreteMixerMenu>> CONCRETE_MIXER_MENU =
            MENU_TYPES.register("concrete_mixer_menu",
                    () -> IMenuTypeExtension.create(ConcreteMixerMenu::new));

    public static final DeferredHolder<MenuType<?>, MenuType<OxidationStationMenu>> OXIDATION_STATION_MENU =
            MENU_TYPES.register("oxidation_station_menu",
                    () -> IMenuTypeExtension.create(OxidationStationMenu::new));

    public static final DeferredHolder<MenuType<?>, MenuType<DyerMenu>> DYER_MENU =
            MENU_TYPES.register("dyer_menu",
                    () -> IMenuTypeExtension.create(DyerMenu::new));
}
