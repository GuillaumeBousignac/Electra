package com.electra.mod.setup;

import com.electra.mod.Electra;
import com.electra.mod.blockentity.AdvancedFurnaceBlockEntity;
import com.electra.mod.blockentity.LightningCollectorBlockEntity;
import com.electra.mod.blockentity.RedstoneConverterBlockEntity;
import com.electra.mod.blockentity.WireBlockEntity;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.entity.BlockEntityType;
import net.neoforged.neoforge.registries.DeferredHolder;
import net.neoforged.neoforge.registries.DeferredRegister;

import java.util.ArrayList;
import java.util.List;

public class ModBlockEntities {

    public static final DeferredRegister<BlockEntityType<?>> BLOCK_ENTITIES =
            DeferredRegister.create(BuiltInRegistries.BLOCK_ENTITY_TYPE, Electra.MODID);

    public static final DeferredHolder<BlockEntityType<?>, BlockEntityType<LightningCollectorBlockEntity>> LIGHTNING_COLLECTOR_BE =
            BLOCK_ENTITIES.register("lightning_collector_be", () -> BlockEntityType.Builder
                    .of(LightningCollectorBlockEntity::new, ModBlocks.LIGHTNING_COLLECTOR.get())
                    .build(null));

    // Identifiant « bare_wire_be » conservé pour ne pas casser les mondes de test existants.
    // Un seul type pour tous les câbles : nu + 16 isolés.
    public static final DeferredHolder<BlockEntityType<?>, BlockEntityType<WireBlockEntity>> WIRE_BE =
            BLOCK_ENTITIES.register("bare_wire_be", () -> {
                List<Block> blocks = new ArrayList<>();
                blocks.add(ModBlocks.BARE_WIRE.get());
                ModBlocks.INSULATED_WIRES.values().forEach(holder -> blocks.add(holder.get()));
                return BlockEntityType.Builder.of(WireBlockEntity::new, blocks.toArray(Block[]::new)).build(null);
            });

    public static final DeferredHolder<BlockEntityType<?>, BlockEntityType<RedstoneConverterBlockEntity>> REDSTONE_CONVERTER_BE =
            BLOCK_ENTITIES.register("redstone_converter_be", () -> BlockEntityType.Builder
                    .of(RedstoneConverterBlockEntity::new, ModBlocks.REDSTONE_CONVERTER.get())
                    .build(null));

    public static final DeferredHolder<BlockEntityType<?>, BlockEntityType<AdvancedFurnaceBlockEntity>> ADVANCED_FURNACE_BE =
            BLOCK_ENTITIES.register("advanced_furnace_be", () -> BlockEntityType.Builder
                    .of(AdvancedFurnaceBlockEntity::new, ModBlocks.ADVANCED_FURNACE.get())
                    .build(null));
}
