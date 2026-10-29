package com.electra.mod.network;

import com.electra.mod.block.IronPipeBlock;
import com.electra.mod.blockentity.IronPipeBlockEntity;
import com.electra.mod.fluid.FluidHelper;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.resources.ResourceKey;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.world.level.Level;
import net.neoforged.neoforge.fluids.FluidStack;
import org.jetbrains.annotations.Nullable;

import java.util.*;

/**
 * Un gestionnaire par dimension, jumeau d'EnergyNetworkManager pour les tuyaux.
 * Ne lit jamais le monde pour trouver les tuyaux : il ne connaît que ceux qui se sont
 * déclarés (onLoad), ce qui évite tout chargement de tronçon involontaire.
 */
public final class FluidNetworkManager {

    private static final Map<ResourceKey<Level>, FluidNetworkManager> MANAGERS = new HashMap<>();
    private static boolean active = true;

    private final Map<BlockPos, IronPipeBlockEntity> pipes = new HashMap<>();
    private final Map<BlockPos, FluidNetwork> byPos = new HashMap<>();
    private final Set<FluidNetwork> networks = new LinkedHashSet<>();

    private FluidNetworkManager() {}

    // ── Cycle de vie ───────────────────────────────────────────────────────

    public static FluidNetworkManager get(Level level) {
        return MANAGERS.computeIfAbsent(level.dimension(), key -> new FluidNetworkManager());
    }

    public static boolean isActive() { return active; }

    /** Au démarrage du serveur (y compris en réouvrant un monde solo). */
    public static void start() {
        MANAGERS.clear();
        active = true;
    }

    /** À l'arrêt : recopie le fluide dans les tuyaux AVANT la sauvegarde finale. */
    public static void shutdown() {
        for (FluidNetworkManager manager : MANAGERS.values()) {
            for (FluidNetwork network : manager.networks) network.flushToPipes();
        }
        MANAGERS.clear();
        active = false;
    }

    public void tick(ServerLevel level) {
        for (FluidNetwork network : List.copyOf(networks)) network.tick(level);
    }

    @Nullable
    public FluidNetwork getNetwork(BlockPos pos) {
        return byPos.get(pos);
    }

    // ── Ajout ──────────────────────────────────────────────────────────────

    public void onPipePlaced(IronPipeBlockEntity pipe) {
        BlockPos pos = pipe.getBlockPos();
        if (pipes.containsKey(pos)) return;
        pipes.put(pos, pipe);

        Set<FluidNetwork> adjacent = new LinkedHashSet<>();
        for (IronPipeBlockEntity neighbor : connectedNeighbors(pipe)) {
            FluidNetwork net = byPos.get(neighbor.getBlockPos());
            if (net != null) adjacent.add(net);
        }

        FluidNetwork target;
        List<FluidStack> carried = new ArrayList<>();
        carried.add(pipe.getShare());

        if (adjacent.size() == 1) {
            target = adjacent.iterator().next();
        } else {
            target = new FluidNetwork();
            networks.add(target);
            // Fusion de plusieurs réseaux
            for (FluidNetwork old : adjacent) {
                carried.add(FluidHelper.copyOf(old.getFluid()));
                networks.remove(old);
                for (IronPipeBlockEntity p : old.getPipes()) {
                    target.addPipe(p);
                    byPos.put(p.getBlockPos(), target);
                }
            }
        }

        target.addPipe(pipe);
        byPos.put(pos, target);
        for (FluidStack fluid : carried) target.addFluid(fluid);
    }

    // ── Retrait ────────────────────────────────────────────────────────────

    public void onPipeRemoved(IronPipeBlockEntity pipe) {
        BlockPos pos = pipe.getBlockPos();
        if (pipes.get(pos) != pipe) return;
        pipes.remove(pos);

        FluidNetwork old = byPos.remove(pos);
        if (old == null) return;

        int oldSize = old.getPipes().size();
        FluidStack oldFluid = FluidHelper.copyOf(old.getFluid());
        List<IronPipeBlockEntity> neighbors = connectedNeighbors(pipe).stream()
                .filter(n -> byPos.get(n.getBlockPos()) == old)
                .toList();
        old.removePipe(pipe);

        if (old.getPipes().isEmpty()) {
            networks.remove(old);
            return;
        }

        // Cas courant (bout de ligne, déchargement) : pas de découpe possible
        if (neighbors.size() <= 1) {
            old.setContent(scaled(oldFluid, oldSize - 1, oldSize));
            return;
        }

        // Découpe éventuelle en plusieurs réseaux, fluide réparti au prorata
        networks.remove(old);
        Set<IronPipeBlockEntity> visited = new HashSet<>();
        for (IronPipeBlockEntity start : neighbors) {
            if (!visited.add(start)) continue;
            FluidNetwork part = new FluidNetwork();
            Deque<IronPipeBlockEntity> queue = new ArrayDeque<>();
            queue.add(start);
            while (!queue.isEmpty()) {
                IronPipeBlockEntity current = queue.poll();
                part.addPipe(current);
                byPos.put(current.getBlockPos(), part);
                for (IronPipeBlockEntity next : connectedNeighbors(current)) {
                    if (byPos.get(next.getBlockPos()) == old && visited.add(next)) queue.add(next);
                }
            }
            networks.add(part);
            part.setContent(scaled(oldFluid, part.getPipes().size(), oldSize));
        }
    }

    // ── Utilitaires ────────────────────────────────────────────────────────

    private static FluidStack scaled(FluidStack fluid, int numerator, int denominator) {
        if (fluid.isEmpty()) return FluidStack.EMPTY;
        return FluidHelper.withAmount(fluid, (int) ((long) fluid.getAmount() * numerator / denominator));
    }

    /** Tuyaux voisins déjà déclarés : tous les tuyaux adjacents se connectent. */
    private List<IronPipeBlockEntity> connectedNeighbors(IronPipeBlockEntity pipe) {
        List<IronPipeBlockEntity> result = new ArrayList<>(6);
        for (Direction dir : Direction.values()) {
            IronPipeBlockEntity other = pipes.get(pipe.getBlockPos().relative(dir));
            if (other != null && other.getBlockState().getBlock() instanceof IronPipeBlock) {
                result.add(other);
            }
        }
        return result;
    }
}
