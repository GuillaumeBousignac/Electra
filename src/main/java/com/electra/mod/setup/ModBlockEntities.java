package com.electra.mod.setup;

import com.electra.mod.Electra;
import com.electra.mod.block.LightningCollectorBlock;
import com.electra.mod.blockentity.AdvancedFurnaceBlockEntity;
import com.electra.mod.blockentity.BareWireBlockEntity;
import com.electra.mod.blockentity.LightningCollectorBlockEntity;
import com.electra.mod.blockentity.RedstoneConverterBlockEntity;
import net.minecraft.world.level.block.entity.BlockEntityType;
import net.neoforged.neoforge.registries.DeferredHolder;
import net.neoforged.neoforge.registries.DeferredRegister;
import net.minecraft.core.registries.BuiltInRegistries;

public class ModBlockEntities {

    public static final DeferredRegister<BlockEntityType<?>> BLOCK_ENTITIES =
            DeferredRegister.create(BuiltInRegistries.BLOCK_ENTITY_TYPE, Electra.MODID);

    public static final DeferredHolder<BlockEntityType<?>, BlockEntityType<LightningCollectorBlockEntity>> LIGHTNING_COLLECTOR_BE =
            BLOCK_ENTITIES.register("lightning_collector_be",
                    () -> BlockEntityType.Builder
                            .of(LightningCollectorBlockEntity::new,
                                    ModBlocks.LIGHTNING_COLLECTOR.get())
                            .build(null));

    public static final DeferredHolder<BlockEntityType<?>, BlockEntityType<BareWireBlockEntity>> BARE_WIRE_BE =
            BLOCK_ENTITIES.register("bare_wire_be",
                    () -> BlockEntityType.Builder
                            .of(BareWireBlockEntity::new,
                                    ModBlocks.BARE_WIRE.get())
                            .build(null));

    public static final DeferredHolder<BlockEntityType<?>, BlockEntityType<RedstoneConverterBlockEntity>> REDSTONE_CONVERTER_BE =
            BLOCK_ENTITIES.register("redstone_converter_be",
                    () -> BlockEntityType.Builder
                            .of(RedstoneConverterBlockEntity::new,
                                    ModBlocks.REDSTONE_CONVERTER.get())
                            .build(null));

    public static final DeferredHolder<BlockEntityType<?>, BlockEntityType<AdvancedFurnaceBlockEntity>> ADVANCED_FURNACE_BE =
            BLOCK_ENTITIES.register("advanced_furnace_be",
                    () -> BlockEntityType.Builder
                            .of(AdvancedFurnaceBlockEntity::new,
                                    ModBlocks.ADVANCED_FURNACE.get())
                            .build(null));
}