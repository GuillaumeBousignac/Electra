package com.electra.mod.event;

import com.electra.mod.blockentity.LightningCollectorBlockEntity;
import com.electra.mod.setup.ModBlocks;
import net.minecraft.core.BlockPos;
import net.minecraft.world.entity.LightningBolt;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.Blocks;
import net.neoforged.bus.api.SubscribeEvent;
import net.neoforged.fml.common.EventBusSubscriber;
import net.neoforged.neoforge.event.entity.EntityStruckByLightningEvent;

import static com.electra.mod.Electra.LOGGER;

@EventBusSubscriber
public class LightningEvents {

    private static final int ENERGY_PER_STRIKE = 5_000;

    @SubscribeEvent
    public static void onLightningStrike(EntityStruckByLightningEvent event) {
        Level level = event.getEntity().level();
        if (level.isClientSide()) return;

        LightningBolt lightning = event.getLightning();
        BlockPos struck = lightning.blockPosition();

        for (int dx = -3; dx <= 3; dx++) {
            for (int dz = -3; dz <= 3; dz++) {
                for (int dy = -1; dy <= 3; dy++) {
                    BlockPos checkPos = struck.offset(dx, dy, dz);
                    if (level.getBlockState(checkPos).is(Blocks.LIGHTNING_ROD)) {
                        tryCollectEnergy(level, checkPos);
                        return;
                    }
                }
            }
        }
    }

    private static void tryCollectEnergy(Level level, BlockPos rodPos) {
        for (int i = 1; i <= 5; i++) {
            BlockPos below = rodPos.below(i);
            if (level.getBlockState(below).is(ModBlocks.LIGHTNING_COLLECTOR.get())) {
                if (level.getBlockEntity(below) instanceof LightningCollectorBlockEntity collector) {
                    collector.addEnergy(ENERGY_PER_STRIKE);
                    LOGGER.info("Eclair capté ! Energie stockée : {} FE", collector.getEnergy());
                }
                return;
            }
            if (level.getBlockState(below).isSolidRender(level, below)) return;
        }
    }
}