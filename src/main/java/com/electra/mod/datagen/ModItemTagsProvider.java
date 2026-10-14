package com.electra.mod.datagen;

import com.electra.mod.Electra;
import com.electra.mod.setup.ModItems;
import net.minecraft.core.HolderLookup;
import net.minecraft.data.PackOutput;
import net.minecraft.data.tags.ItemTagsProvider;
import net.minecraft.data.tags.TagsProvider;
import net.minecraft.world.level.block.Block;
import net.neoforged.neoforge.common.Tags;
import net.neoforged.neoforge.common.data.ExistingFileHelper;
import org.jetbrains.annotations.NotNull;

import java.util.concurrent.CompletableFuture;

public class ModItemTagsProvider extends ItemTagsProvider {

    public ModItemTagsProvider(PackOutput output, CompletableFuture<HolderLookup.Provider> lookup,
                               CompletableFuture<TagsProvider.TagLookup<Block>> blockTags, ExistingFileHelper existingFiles) {
        super(output, lookup, blockTags, Electra.MODID, existingFiles);
    }

    @Override
    protected void addTags(HolderLookup.@NotNull Provider provider) {
        // Lingots
        tag(ModTags.Items.INGOTS_ROSE_GOLD).add(ModItems.ROSE_GOLD_INGOT.get());
        tag(ModTags.Items.INGOTS_RED_IRON).add(ModItems.RED_IRON_INGOT.get());
        tag(Tags.Items.INGOTS)
                .addTag(ModTags.Items.INGOTS_ROSE_GOLD)
                .addTag(ModTags.Items.INGOTS_RED_IRON);

        // Pépites
        tag(ModTags.Items.NUGGETS_ROSE_GOLD).add(ModItems.ROSE_GOLD_NUGGET.get());
        tag(Tags.Items.NUGGETS).addTag(ModTags.Items.NUGGETS_ROSE_GOLD);

        // Blocs de stockage : recopiés depuis les étiquettes de blocs
        copy(ModTags.Blocks.STORAGE_BLOCKS_ROSE_GOLD, ModTags.Items.STORAGE_BLOCKS_ROSE_GOLD);
        copy(ModTags.Blocks.STORAGE_BLOCKS_RED_IRON, ModTags.Items.STORAGE_BLOCKS_RED_IRON);
        tag(Tags.Items.STORAGE_BLOCKS)
                .addTag(ModTags.Items.STORAGE_BLOCKS_ROSE_GOLD)
                .addTag(ModTags.Items.STORAGE_BLOCKS_RED_IRON);
    }
}
