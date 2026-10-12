package com.electra.mod.datagen;

import com.electra.mod.setup.ModBlocks;
import net.minecraft.core.HolderLookup;
import net.minecraft.data.loot.BlockLootSubProvider;
import net.minecraft.world.flag.FeatureFlags;
import net.minecraft.world.level.block.Block;
import org.jetbrains.annotations.NotNull;

import java.util.Set;

/** Butin des blocs : chaque bloc d'Electra se lâche lui-même. */
public class ModBlockLootTables extends BlockLootSubProvider {

    public ModBlockLootTables(HolderLookup.Provider registries) {
        super(Set.of(), FeatureFlags.REGISTRY.allFlags(), registries);
    }

    @Override
    protected void generate() {
        // Tout bloc enregistré se lâche lui-même. Si un futur bloc doit lâcher autre chose,
        // le traiter ici avant cette boucle et l'exclure de celle-ci.
        for (Block block : getKnownBlocks()) {
            dropSelf(block);
        }
    }

    /** Doit couvrir tous les blocs d'Electra : sinon la génération échoue (sécurité anti-oubli). */
    @Override
    protected @NotNull Iterable<Block> getKnownBlocks() {
        return ModBlocks.BLOCKS.getEntries().stream()
                .map(holder -> (Block) holder.value())
                .toList();
    }
}
