package com.electra.mod.event;

import com.electra.mod.network.EnergyNetworkManager;
import net.neoforged.bus.api.SubscribeEvent;
import net.neoforged.neoforge.event.server.ServerStoppedEvent;
import net.neoforged.neoforge.event.server.ServerStoppingEvent;
import net.neoforged.neoforge.event.tick.LevelTickEvent;

public class ServerTickEvents {

    @SubscribeEvent
    public void onServerTick(LevelTickEvent.Post event) {
        if (event.getLevel().isClientSide()) return;
        EnergyNetworkManager.get().tickAll(event.getLevel());
    }

    @SubscribeEvent
    public void onServerStopping(ServerStoppingEvent event) {
        EnergyNetworkManager.get().clear();
    }

    @SubscribeEvent
    public void onServerStopped(ServerStoppedEvent event) {
        EnergyNetworkManager.get().activate();
    }
}