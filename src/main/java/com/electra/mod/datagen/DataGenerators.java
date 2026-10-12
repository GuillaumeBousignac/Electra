package com.electra.mod.datagen;

import com.electra.mod.Electra;
import net.minecraft.core.HolderLookup;
import net.minecraft.data.DataGenerator;
import net.minecraft.data.PackOutput;
import net.minecraft.data.loot.LootTableProvider;
import net.minecraft.world.level.storage.loot.parameters.LootContextParamSets;
import net.neoforged.bus.api.SubscribeEvent;
import net.neoforged.fml.common.EventBusSubscriber;
import net.neoforged.neoforge.common.data.BlockTagsProvider;
import net.neoforged.neoforge.common.data.ExistingFileHelper;
import net.neoforged.neoforge.data.event.GatherDataEvent;

import java.util.List;
import java.util.Set;
import java.util.concurrent.CompletableFuture;

/**
 * Point d'entrée de la génération des données. Lancé par : ./gradlew runData
 * Les fichiers sont écrits dans src/generated/resources (déjà inclus dans le build).
 */
@EventBusSubscriber(modid = Electra.MODID, bus = EventBusSubscriber.Bus.MOD)
public class DataGenerators {

    @SubscribeEvent
    public static void gatherData(GatherDataEvent event) {
        DataGenerator generator = event.getGenerator();
        PackOutput output = generator.getPackOutput();
        ExistingFileHelper existingFiles = event.getExistingFileHelper();
        CompletableFuture<HolderLookup.Provider> lookup = event.getLookupProvider();

        // ── Ressources client : états de blocs, modèles, traductions ───────
        generator.addProvider(event.includeClient(), new ModBlockStateProvider(output, existingFiles));
        generator.addProvider(event.includeClient(), new ModItemModelProvider(output, existingFiles));
        generator.addProvider(event.includeClient(), new ModLanguageProvider(output));

        // ── Données serveur : butin, étiquettes, recettes ──────────────────
        generator.addProvider(event.includeServer(), new LootTableProvider(output, Set.of(),
                List.of(new LootTableProvider.SubProviderEntry(ModBlockLootTables::new, LootContextParamSets.BLOCK)),
                lookup));

        BlockTagsProvider blockTags = generator.addProvider(event.includeServer(),
                new ModBlockTagsProvider(output, lookup, existingFiles));
        generator.addProvider(event.includeServer(),
                new ModItemTagsProvider(output, lookup, blockTags.contentsGetter(), existingFiles));

        generator.addProvider(event.includeServer(), new ModRecipeProvider(output, lookup));
    }
}
