package com.electra.mod.network;

import com.electra.mod.block.IronPipeBlock;
import com.electra.mod.blockentity.IronPipeBlockEntity;
import com.electra.mod.config.ElectraConfig;
import com.electra.mod.fluid.FluidHelper;
import com.electra.mod.fluid.FluidPort;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.server.level.ServerLevel;
import net.neoforged.neoforge.capabilities.BlockCapabilityCache;
import net.neoforged.neoforge.capabilities.Capabilities;
import net.neoforged.neoforge.fluids.FluidStack;
import net.neoforged.neoforge.fluids.capability.IFluidHandler;

import java.util.*;

/**
 * Un ensemble de tuyaux connectés, partageant une seule réserve d'un seul fluide (en mB).
 * Même principe que le réseau de câbles : les points de raccordement sont trouvés via les
 * capacités de fluide standard de NeoForge, donc toute cuve compatible fonctionne.
 *
 * Règles du flux, à chaque tick :
 *  1. les sources (pompes) remplissent le réseau ;
 *  2. si les consommateurs en demandent plus que le réseau n'en contient, les réservoirs complètent ;
 *  3. le réseau distribue aux consommateurs, puis le surplus part dans les réservoirs.
 * Un réseau ne contient jamais deux fluides à la fois : tant qu'il en contient, l'autre est refusé.
 */
public class FluidNetwork {

    private static final int SYNC_INTERVAL = 20;   // part de chaque tuyau mise à jour chaque seconde

    private final Set<IronPipeBlockEntity> pipes = new LinkedHashSet<>();
    private final List<BlockCapabilityCache<IFluidHandler, Direction>> endpoints = new ArrayList<>();
    private boolean endpointsDirty = true;

    private FluidStack stored = FluidStack.EMPTY;
    private int syncTimer = 0;
    private int roundRobin = 0;

    // ── Composition ────────────────────────────────────────────────────────

    void addPipe(IronPipeBlockEntity pipe) {
        pipes.add(pipe);
        endpointsDirty = true;
    }

    void removePipe(IronPipeBlockEntity pipe) {
        pipes.remove(pipe);
        endpointsDirty = true;
        setContent(stored);   // ramène le contenu à la nouvelle capacité
    }

    public Set<IronPipeBlockEntity> getPipes() { return pipes; }

    // ── Contenu ────────────────────────────────────────────────────────────

    public int getCapacity()      { return pipes.size() * IronPipeBlockEntity.capacity(); }
    public FluidStack getFluid()  { return stored; }
    public int getAmount()        { return stored.getAmount(); }

    /** Remplace le contenu (borné par la capacité). */
    void setContent(FluidStack fluid) {
        if (fluid.isEmpty()) {
            stored = FluidStack.EMPTY;
            return;
        }
        stored = FluidHelper.withAmount(fluid, Math.min(fluid.getAmount(), getCapacity()));
    }

    /** Ajoute un fluide ; s'il est différent de celui du réseau, il est perdu (cas rare : fusion de deux réseaux). */
    void addFluid(FluidStack incoming) {
        if (incoming.isEmpty()) return;
        if (stored.isEmpty()) {
            setContent(incoming);
        } else if (FluidStack.isSameFluidSameComponents(stored, incoming)) {
            setContent(FluidHelper.withAmount(stored, stored.getAmount() + incoming.getAmount()));
        }
    }

    private void shrinkStored(int amount) {
        if (amount <= 0 || stored.isEmpty()) return;
        stored = FluidHelper.withAmount(stored, stored.getAmount() - amount);
    }

    /** Injection depuis l'extérieur (autres mods qui versent dans un tuyau). */
    public int receive(FluidStack resource, boolean simulate) {
        if (resource.isEmpty()) return 0;
        if (!stored.isEmpty() && !FluidStack.isSameFluidSameComponents(stored, resource)) return 0;
        int accepted = Math.min(resource.getAmount(), getCapacity() - stored.getAmount());
        if (accepted <= 0) return 0;
        if (!simulate) {
            stored = FluidHelper.withAmount(resource, stored.getAmount() + accepted);
        }
        return accepted;
    }

    /** Recopie la part de chaque tuyau pour la sauvegarde et l'affichage (fluide visible dans les vitres). */
    void flushToPipes() {
        if (pipes.isEmpty()) return;
        int share = stored.getAmount() / pipes.size();
        for (IronPipeBlockEntity pipe : pipes) pipe.updateShare(stored, share);
    }

    // ── Tick ───────────────────────────────────────────────────────────────

    void tick(ServerLevel level) {
        if (pipes.isEmpty()) return;
        if (endpointsDirty) rebuildEndpoints(level);

        int capacity = getCapacity();
        int rate = ElectraConfig.PIPE_TRANSFER_RATE.get();

        Set<IFluidHandler> seen = Collections.newSetFromMap(new IdentityHashMap<>());
        List<IFluidHandler> sources = new ArrayList<>();
        List<IFluidHandler> buffers = new ArrayList<>();
        List<IFluidHandler> sinks = new ArrayList<>();

        for (BlockCapabilityCache<IFluidHandler, Direction> cache : endpoints) {
            BlockPos pos = cache.pos();
            if (!level.isLoaded(pos)) continue;                               // ne jamais charger de tronçon
            if (level.getBlockState(pos).getBlock() instanceof IronPipeBlock) continue;

            IFluidHandler handler = cache.getCapability();
            if (handler == null || !seen.add(handler)) continue;

            if (handler instanceof FluidPort port) {
                switch (port.role()) {
                    case SOURCE -> sources.add(handler);
                    case BUFFER -> buffers.add(handler);
                    case SINK -> sinks.add(handler);
                }
            } else {
                sinks.add(handler);   // cuve d'un autre mod : on la remplit, on ne la vide jamais
            }
        }

        // 1. Les sources (pompes) remplissent le réseau
        for (IFluidHandler source : sources) {
            int room = capacity - stored.getAmount();
            if (room <= 0) break;
            int want = Math.min(rate, room);
            FluidStack pulled = stored.isEmpty()
                    ? source.drain(want, IFluidHandler.FluidAction.EXECUTE)
                    : source.drain(FluidHelper.withAmount(stored, want), IFluidHandler.FluidAction.EXECUTE);
            addFluid(pulled);
        }

        // 2. Les réservoirs ne donnent que ce que les consommateurs vont prendre
        FluidStack probe = stored;
        if (probe.isEmpty()) {
            for (IFluidHandler buffer : buffers) {
                FluidStack peek = buffer.drain(1, IFluidHandler.FluidAction.SIMULATE);
                if (!peek.isEmpty()) {
                    probe = peek;
                    break;
                }
            }
        }
        if (!probe.isEmpty() && !sinks.isEmpty() && !buffers.isEmpty()) {
            long demand = 0;
            for (IFluidHandler sink : sinks) {
                demand += sink.fill(FluidHelper.withAmount(probe, rate), IFluidHandler.FluidAction.SIMULATE);
            }
            int needed = (int) Math.min(Math.max(0, demand - stored.getAmount()), capacity - stored.getAmount());
            for (IFluidHandler buffer : buffers) {
                if (needed <= 0) break;
                FluidStack pulled = buffer.drain(FluidHelper.withAmount(probe, Math.min(rate, needed)),
                        IFluidHandler.FluidAction.EXECUTE);
                if (pulled.isEmpty()) continue;
                addFluid(pulled);
                needed -= pulled.getAmount();
            }
        }

        // 3. Distribution : consommateurs d'abord, puis réservoirs avec le surplus
        distribute(sinks, rate);
        distribute(buffers, rate);

        if (++syncTimer >= SYNC_INTERVAL) {
            syncTimer = 0;
            flushToPipes();
        }
    }

    /** Distribution équitable, avec point de départ tournant. */
    private void distribute(List<IFluidHandler> receivers, int rate) {
        if (stored.isEmpty() || receivers.isEmpty()) return;
        int n = receivers.size();
        int start = Math.floorMod(roundRobin++, n);
        for (int i = 0; i < n && !stored.isEmpty(); i++) {
            IFluidHandler receiver = receivers.get((start + i) % n);
            int give = Math.min(rate, stored.getAmount());
            int filled = receiver.fill(FluidHelper.withAmount(stored, give), IFluidHandler.FluidAction.EXECUTE);
            shrinkStored(filled);
        }
    }

    private void rebuildEndpoints(ServerLevel level) {
        endpoints.clear();
        Set<BlockPos> members = new HashSet<>();
        for (IronPipeBlockEntity pipe : pipes) members.add(pipe.getBlockPos());

        for (IronPipeBlockEntity pipe : pipes) {
            for (Direction dir : Direction.values()) {
                BlockPos neighbor = pipe.getBlockPos().relative(dir);
                if (members.contains(neighbor)) continue;
                endpoints.add(BlockCapabilityCache.create(
                        Capabilities.FluidHandler.BLOCK, level, neighbor, dir.getOpposite()));
            }
        }
        endpointsDirty = false;
    }
}
