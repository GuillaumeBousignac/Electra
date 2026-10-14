package com.electra.mod.datagen;

import net.minecraft.core.registries.Registries;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.tags.TagKey;
import net.minecraft.world.item.Item;
import net.minecraft.world.level.block.Block;

/** Étiquettes communes (espace « c ») propres aux matériaux d'Electra. */
public final class ModTags {

    private ModTags() {}

    public static final class Blocks {
        public static final TagKey<Block> STORAGE_BLOCKS_ROSE_GOLD = common("storage_blocks/rose_gold");
        public static final TagKey<Block> STORAGE_BLOCKS_RED_IRON  = common("storage_blocks/red_iron");

        private static TagKey<Block> common(String path) {
            return TagKey.create(Registries.BLOCK, ResourceLocation.fromNamespaceAndPath("c", path));
        }
    }

    public static final class Items {
        public static final TagKey<Item> INGOTS_ROSE_GOLD         = common("ingots/rose_gold");
        public static final TagKey<Item> INGOTS_RED_IRON          = common("ingots/red_iron");
        public static final TagKey<Item> NUGGETS_ROSE_GOLD        = common("nuggets/rose_gold");
        public static final TagKey<Item> STORAGE_BLOCKS_ROSE_GOLD = common("storage_blocks/rose_gold");
        public static final TagKey<Item> STORAGE_BLOCKS_RED_IRON  = common("storage_blocks/red_iron");

        private static TagKey<Item> common(String path) {
            return TagKey.create(Registries.ITEM, ResourceLocation.fromNamespaceAndPath("c", path));
        }
    }
}
