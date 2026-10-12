package com.electra.mod.datagen;

import com.electra.mod.Electra;
import com.electra.mod.setup.ModBlocks;
import net.minecraft.core.HolderLookup;
import net.minecraft.data.PackOutput;
import net.minecraft.tags.BlockTags;
import net.neoforged.neoforge.common.Tags;
import net.neoforged.neoforge.common.data.BlockTagsProvider;
import net.neoforged.neoforge.common.data.ExistingFileHelper;
import org.jetbrains.annotations.NotNull;

import java.util.concurrent.CompletableFuture;

public class ModBlockTagsProvider extends BlockTagsProvider {

    public ModBlockTagsProvider(PackOutput output, CompletableFuture<HolderLookup.Provider> lookup,
                                ExistingFileHelper existingFiles) {
        super(output, lookup, Electra.MODID, existingFiles);
    }

    @Override
    protected void addTags(HolderLookup.@NotNull Provider provider) {
        // Outil requis
        tag(BlockTags.MINEABLE_WITH_PICKAXE).add(
                ModBlocks.LIGHTNING_COLLECTOR.get(),
                ModBlocks.ADVANCED_FURNACE.get(),
                ModBlocks.REDSTONE_CONVERTER.get(),
                ModBlocks.ROSE_GOLD_BLOCK.get(),
                ModBlocks.RED_IRON_BLOCK.get());

        tag(BlockTags.NEEDS_STONE_TOOL).add(ModBlocks.RED_IRON_BLOCK.get());

        // Blocs de stockage (compatibilité avec les autres mods)
        tag(ModTags.Blocks.STORAGE_BLOCKS_ROSE_GOLD).add(ModBlocks.ROSE_GOLD_BLOCK.get());
        tag(ModTags.Blocks.STORAGE_BLOCKS_RED_IRON).add(ModBlocks.RED_IRON_BLOCK.get());
        tag(Tags.Blocks.STORAGE_BLOCKS)
                .addTag(ModTags.Blocks.STORAGE_BLOCKS_ROSE_GOLD)
                .addTag(ModTags.Blocks.STORAGE_BLOCKS_RED_IRON);
    }
}
