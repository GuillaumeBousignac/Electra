package com.electra.mod.datagen;

import com.electra.mod.Electra;
import com.electra.mod.setup.ModItems;
import net.minecraft.data.PackOutput;
import net.minecraft.world.item.DyeColor;
import net.neoforged.neoforge.client.model.generators.ItemModelProvider;
import net.neoforged.neoforge.common.data.ExistingFileHelper;

/**
 * Modèles d'objets. Les objets-blocs simples sont générés par ModBlockStateProvider ;
 * les modèles d'inventaire faits dans Blockbench (câble nu, Lightning Collector,
 * gabarit insulated_wire_inventory) restent écrits à la main.
 */
public class ModItemModelProvider extends ItemModelProvider {

    public ModItemModelProvider(PackOutput output, ExistingFileHelper existingFiles) {
        super(output, Electra.MODID, existingFiles);
    }

    @Override
    protected void registerModels() {
        basicItem(ModItems.ROSE_GOLD_INGOT.get());
        basicItem(ModItems.ROSE_GOLD_NUGGET.get());
        basicItem(ModItems.ALPHABETIC_STICKER.get());
        basicItem(ModItems.TOOL_STICKER.get());
        basicItem(ModItems.ITEM_STICKER.get());
        basicItem(ModItems.SYMBOL_STICKER.get());
        basicItem(ModItems.NUMBER_STICKER.get());
        basicItem(ModItems.MOB_STICKER.get());
        basicItem(ModItems.RED_IRON_INGOT.get());

        // Texture provisoire : bâton vanilla, tenu comme un outil
        withExistingParent("energy_meter", mcLoc("item/handheld"))
                .texture("layer0", mcLoc("item/stick"));

        for (DyeColor color : DyeColor.values()) {
            String name = color.getName() + "_insulated_wire";
            withExistingParent(name, modLoc("item/insulated_wire_inventory"))
                    .texture("wire", modLoc("block/" + name));
        }
    }
}
