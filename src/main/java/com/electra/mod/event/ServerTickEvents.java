package com.electra.mod.event;

import com.electra.mod.network.EnergyNetworkManager;
import net.minecraft.server.level.ServerLevel;
import net.neoforged.bus.api.SubscribeEvent;
import net.neoforged.neoforge.event.server.ServerAboutToStartEvent;
import net.neoforged.neoforge.event.server.ServerStoppingEvent;
import net.neoforged.neoforge.event.tick.LevelTickEvent;

public class ServerTickEvents {

    /** Chaque dimension ne fait tourner que ses propres réseaux. */
    @SubscribeEvent
    public void onLevelTick(LevelTickEvent.Post event) {
        if (event.getLevel() instanceof ServerLevel serverLevel && EnergyNetworkManager.isActive()) {
            EnergyNetworkManager.get(serverLevel).tick(serverLevel);
        }
    }

    @SubscribeEvent
    public void onServerAboutToStart(ServerAboutToStartEvent event) {
        EnergyNetworkManager.start();
    }

    @SubscribeEvent
    public void onServerStopping(ServerStoppingEvent event) {
        EnergyNetworkManager.shutdown();
    }
}
