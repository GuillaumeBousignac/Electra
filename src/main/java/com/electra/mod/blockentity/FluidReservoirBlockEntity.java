package com.electra.mod.blockentity;

import com.electra.mod.config.ElectraConfig;
import com.electra.mod.fluid.FluidPort;
import com.electra.mod.fluid.FluidHelper;
import com.electra.mod.fluid.ModFluidTank;
import com.electra.mod.setup.ModBlockEntities;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.core.HolderLookup;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.network.protocol.Packet;
import net.minecraft.network.protocol.game.ClientGamePacketListener;
import net.minecraft.network.protocol.game.ClientboundBlockEntityDataPacket;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraft.world.level.block.state.BlockState;
import net.neoforged.neoforge.fluids.FluidStack;
import org.jetbrains.annotations.NotNull;

import java.util.ArrayDeque;
import java.util.ArrayList;
import java.util.Comparator;
import java.util.HashSet;
import java.util.List;
import java.util.Set;
import java.util.TreeMap;

/** Réservoir : une cuve de capacité configurable, au rôle « tampon » pour le réseau de tuyaux. */
public class FluidReservoirBlockEntity extends BlockEntity {

    private final ModFluidTank tank = new ModFluidTank(
            ElectraConfig.RESERVOIR_CAPACITY.get(), stack -> true, this::onChanged);

    private final FluidPort port = new GroupPort();

    /** Incrémenté à chaque pose, retrait ou chargement de réservoir : invalide les groupes mémorisés. */
    private static int topologyVersion = 0;
    private static final int MAX_GROUP = 512;

    private List<FluidReservoirBlockEntity> group = List.of();
    private int groupVersion = -1;

    private boolean needsSync = false;
    private int lastComparator = 0;

    public FluidReservoirBlockEntity(BlockPos pos, BlockState state) {
        super(ModBlockEntities.FLUID_RESERVOIR_BE.get(), pos, state);
    }

    public ModFluidTank getTank()      { return tank; }
    public FluidPort getFluidPort()    { return port; }

    @Override
    public void onLoad() {
        super.onLoad();
        topologyVersion++;
    }

    @Override
    public void setRemoved() {
        super.setRemoved();
        topologyVersion++;
    }

    // ── Groupe : tous les réservoirs qui se touchent forment une seule cuve ─

    /** Réservoirs reliés (faces voisines), du plus bas au plus haut. */
    public List<FluidReservoirBlockEntity> getGroup() {
        if (groupVersion != topologyVersion || group.isEmpty()) {
            group = computeGroup();
            groupVersion = topologyVersion;
        }
        return group;
    }

    private List<FluidReservoirBlockEntity> computeGroup() {
        if (level == null) return List.of(this);
        Set<BlockPos> seen = new HashSet<>();
        ArrayDeque<FluidReservoirBlockEntity> queue = new ArrayDeque<>();
        List<FluidReservoirBlockEntity> result = new ArrayList<>();
        seen.add(worldPosition);
        queue.add(this);
        while (!queue.isEmpty() && result.size() < MAX_GROUP) {
            FluidReservoirBlockEntity current = queue.poll();
            result.add(current);
            for (Direction dir : Direction.values()) {
                BlockPos next = current.worldPosition.relative(dir);
                if (!seen.add(next) || !level.isLoaded(next)) continue;
                if (level.getBlockEntity(next) instanceof FluidReservoirBlockEntity other && !other.isRemoved()) {
                    queue.add(other);
                }
            }
        }
        result.sort(Comparator.comparingInt((FluidReservoirBlockEntity r) -> r.worldPosition.getY())
                .thenComparingLong(r -> r.worldPosition.asLong()));
        return List.copyOf(result);
    }

    /** Fluide de la cuve : celui du plus bas réservoir non vide. */
    private FluidStack groupFluid() {
        for (FluidReservoirBlockEntity member : getGroup()) {
            if (!member.tank.isEmpty()) return member.tank.getFluid();
        }
        return FluidStack.EMPTY;
    }

    /** Membres pouvant servir : vides, ou contenant le même fluide (un fluide étranger reste simplement en place). */
    private List<FluidReservoirBlockEntity> usableMembers(FluidStack fluid) {
        List<FluidReservoirBlockEntity> usable = new ArrayList<>();
        for (FluidReservoirBlockEntity member : getGroup()) {
            if (member.tank.isEmpty() || fluid.isEmpty() || FluidStack.isSameFluidSameComponents(member.tank.getFluid(), fluid)) {
                usable.add(member);
            }
        }
        return usable;
    }

    public int groupAmount() {
        FluidStack fluid = groupFluid();
        int total = 0;
        for (FluidReservoirBlockEntity m : usableMembers(fluid)) total += m.tank.getFluidAmount();
        return total;
    }

    public int groupCapacity() {
        int total = 0;
        for (FluidReservoirBlockEntity m : usableMembers(groupFluid())) total += m.tank.getCapacity();
        return total;
    }

    public int comparatorSignal() {
        int amount = groupAmount();
        if (amount <= 0) return 0;
        return 1 + (int) (14L * amount / groupCapacity());
    }

    /** Niveaux (hauteurs) distincts parmi les membres, triés. */
    private static TreeMap<Integer, List<FluidReservoirBlockEntity>> layers(List<FluidReservoirBlockEntity> members) {
        TreeMap<Integer, List<FluidReservoirBlockEntity>> layers = new TreeMap<>();
        for (FluidReservoirBlockEntity m : members) {
            layers.computeIfAbsent(m.worldPosition.getY(), y -> new ArrayList<>()).add(m);
        }
        return layers;
    }

    /**
     * Raccord commun à toute la cuve : le liquide se range du bas vers le haut et,
     * à hauteur égale, se répartit à parts égales ; on puise d'abord en haut.
     */
    private final class GroupPort implements FluidPort {

        @Override public Role role() { return Role.BUFFER; }

        @Override public int getTanks() { return 1; }

        @Override
        public @NotNull FluidStack getFluidInTank(int index) {
            return FluidHelper.withAmount(groupFluid(), groupAmount());
        }

        @Override public int getTankCapacity(int index) { return groupCapacity(); }

        @Override
        public boolean isFluidValid(int index, @NotNull FluidStack stack) {
            FluidStack current = groupFluid();
            return current.isEmpty() || FluidStack.isSameFluidSameComponents(current, stack);
        }

        @Override
        public int fill(@NotNull FluidStack resource, @NotNull FluidAction action) {
            if (resource.isEmpty() || !isFluidValid(0, resource)) return 0;
            List<FluidReservoirBlockEntity> members = usableMembers(resource);
            int free = 0;
            for (FluidReservoirBlockEntity m : members) free += m.tank.getCapacity() - m.tank.getFluidAmount();
            int toFill = Math.min(resource.getAmount(), free);
            if (toFill <= 0 || action.simulate()) return Math.max(toFill, 0);

            int remaining = toFill;
            for (List<FluidReservoirBlockEntity> layer : layers(members).values()) {   // du bas vers le haut
                if (remaining <= 0) break;
                layer.sort(Comparator.comparingInt(m -> m.tank.getCapacity() - m.tank.getFluidAmount()));
                for (int i = 0; i < layer.size() && remaining > 0; i++) {
                    FluidReservoirBlockEntity m = layer.get(i);
                    int share = remaining / (layer.size() - i);
                    if (share <= 0) share = Math.min(1, remaining);
                    int given = m.tank.fill(FluidHelper.withAmount(resource, share), FluidAction.EXECUTE);
                    remaining -= given;
                }
            }
            return toFill - remaining;
        }

        @Override
        public @NotNull FluidStack drain(@NotNull FluidStack resource, @NotNull FluidAction action) {
            if (resource.isEmpty()) return FluidStack.EMPTY;
            FluidStack current = groupFluid();
            if (current.isEmpty() || !FluidStack.isSameFluidSameComponents(current, resource)) return FluidStack.EMPTY;
            return doDrain(current, resource.getAmount(), action);
        }

        @Override
        public @NotNull FluidStack drain(int maxDrain, @NotNull FluidAction action) {
            FluidStack current = groupFluid();
            if (current.isEmpty() || maxDrain <= 0) return FluidStack.EMPTY;
            return doDrain(current, maxDrain, action);
        }

        private FluidStack doDrain(FluidStack fluid, int max, FluidAction action) {
            List<FluidReservoirBlockEntity> members = usableMembers(fluid);
            int total = 0;
            for (FluidReservoirBlockEntity m : members) total += m.tank.getFluidAmount();
            int toDrain = Math.min(max, total);
            if (toDrain <= 0) return FluidStack.EMPTY;
            if (action.simulate()) return FluidHelper.withAmount(fluid, toDrain);

            int remaining = toDrain;
            for (List<FluidReservoirBlockEntity> layer : layers(members).descendingMap().values()) {   // du haut vers le bas
                if (remaining <= 0) break;
                layer.sort(Comparator.comparingInt(m -> m.tank.getFluidAmount()));
                for (int i = 0; i < layer.size() && remaining > 0; i++) {
                    FluidReservoirBlockEntity m = layer.get(i);
                    int share = remaining / (layer.size() - i);
                    if (share <= 0) share = Math.min(1, remaining);
                    FluidStack taken = m.tank.drain(share, FluidAction.EXECUTE);
                    remaining -= taken.getAmount();
                }
            }
            return FluidHelper.withAmount(fluid, toDrain - remaining);
        }
    }

    private void onChanged() {
        setChanged();
        needsSync = true;
    }

    public void tick() {
        if (level == null || level.isClientSide()) return;

        // Synchronisation client limitée à deux fois par seconde
        if (needsSync && level.getGameTime() % 10 == 0 && level instanceof ServerLevel serverLevel) {
            needsSync = false;
            serverLevel.sendBlockUpdated(worldPosition, getBlockState(), getBlockState(), Block.UPDATE_CLIENTS);

            // Le comparateur ne se met à jour que si son niveau a changé
            int comparator = comparatorSignal();
            if (comparator != lastComparator) {
                lastComparator = comparator;
                level.updateNeighbourForOutputSignal(worldPosition, getBlockState().getBlock());
            }
        }
    }

    // ── Synchronisation et sauvegarde ──────────────────────────────────────

    @Override
    public @NotNull CompoundTag getUpdateTag(HolderLookup.@NotNull Provider registries) {
        return saveWithoutMetadata(registries);
    }

    @Override
    public Packet<ClientGamePacketListener> getUpdatePacket() {
        return ClientboundBlockEntityDataPacket.create(this);
    }

    @Override
    protected void saveAdditional(@NotNull CompoundTag tag, HolderLookup.@NotNull Provider registries) {
        super.saveAdditional(tag, registries);
        tank.save(tag, registries);
    }

    @Override
    protected void loadAdditional(@NotNull CompoundTag tag, HolderLookup.@NotNull Provider registries) {
        super.loadAdditional(tag, registries);
        tank.load(tag, registries);
    }
}
