package com.electra.mod.blockentity;

import com.electra.mod.config.ElectraConfig;
import com.electra.mod.fluid.FluidHelper;
import com.electra.mod.network.FluidNetwork;
import com.electra.mod.network.FluidNetworkManager;
import com.electra.mod.setup.ModBlockEntities;
import net.minecraft.core.BlockPos;
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
import net.neoforged.neoforge.fluids.capability.IFluidHandler;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

/**
 * Entité d'un tuyau. Le fluide réel est stocké dans le FluidNetwork ; chaque tuyau ne garde
 * que sa part, pour la sauvegarde et pour l'affichage côté client (fluide visible dans les vitres).
 */
public class IronPipeBlockEntity extends BlockEntity {

    /** Capacité d'un tuyau en mB, lue dans electra-server.toml. */
    public static int capacity() { return ElectraConfig.PIPE_CAPACITY.get(); }

    private FluidStack share = FluidStack.EMPTY;
    private boolean unloading = false;

    /** Ce que voient les autres mods : on peut verser dans le tuyau, pas y puiser. */
    private final IFluidHandler externalHandler = new IFluidHandler() {
        @Override public int getTanks() { return 1; }

        @Override
        public @NotNull FluidStack getFluidInTank(int tank) {
            FluidNetwork net = getNetwork();
            if (net == null) return share;
            return FluidHelper.withAmount(net.getFluid(), net.getAmount() / Math.max(1, net.getPipes().size()));
        }

        @Override public int getTankCapacity(int tank) { return capacity(); }

        @Override public boolean isFluidValid(int tank, @NotNull FluidStack stack) { return true; }

        @Override
        public int fill(@NotNull FluidStack resource, @NotNull FluidAction action) {
            FluidNetwork net = getNetwork();
            return net == null ? 0 : net.receive(resource, action.simulate());
        }

        @Override public @NotNull FluidStack drain(@NotNull FluidStack resource, @NotNull FluidAction action) {
            return FluidStack.EMPTY;
        }

        @Override public @NotNull FluidStack drain(int maxDrain, @NotNull FluidAction action) {
            return FluidStack.EMPTY;
        }
    };

    public IronPipeBlockEntity(BlockPos pos, BlockState state) {
        super(ModBlockEntities.IRON_PIPE_BE.get(), pos, state);
    }

    // ── Réseau ─────────────────────────────────────────────────────────────

    @Nullable
    public FluidNetwork getNetwork() {
        if (level == null || level.isClientSide() || !FluidNetworkManager.isActive()) return null;
        return FluidNetworkManager.get(level).getNetwork(worldPosition);
    }

    public IFluidHandler getExternalHandler() { return externalHandler; }

    /** Part de ce tuyau : fluide et quantité (mB). Sert à l'affichage et à la sauvegarde. */
    public FluidStack getShare() { return share; }

    /** Appelé par le réseau une fois par seconde, et à l'arrêt du serveur. */
    public void updateShare(FluidStack fluid, int amount) {
        FluidStack next = FluidHelper.withAmount(fluid, amount);
        boolean same = next.isEmpty() ? share.isEmpty()
                : !share.isEmpty() && share.getAmount() == next.getAmount()
                  && FluidStack.isSameFluidSameComponents(share, next);
        if (same) return;
        share = next;
        setChanged();
        if (level instanceof ServerLevel serverLevel) {
            serverLevel.sendBlockUpdated(worldPosition, getBlockState(), getBlockState(), Block.UPDATE_CLIENTS);
        }
    }

    @Override
    public void onLoad() {
        super.onLoad();
        if (level != null && !level.isClientSide() && FluidNetworkManager.isActive()) {
            FluidNetworkManager.get(level).onPipePlaced(this);
        }
    }

    @Override
    public void onChunkUnloaded() {
        super.onChunkUnloaded();
        unloading = true;
        if (level != null && !level.isClientSide() && FluidNetworkManager.isActive()) {
            FluidNetworkManager.get(level).onPipeRemoved(this);
        }
    }

    @Override
    public void setRemoved() {
        // Déchargement du tronçon : déjà traité dans onChunkUnloaded
        if (!unloading && level != null && !level.isClientSide() && FluidNetworkManager.isActive()) {
            FluidNetworkManager.get(level).onPipeRemoved(this);
        }
        super.setRemoved();
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
        FluidHelper.save(tag, "fluid", share, registries);
    }

    @Override
    protected void loadAdditional(@NotNull CompoundTag tag, HolderLookup.@NotNull Provider registries) {
        super.loadAdditional(tag, registries);
        share = FluidHelper.load(tag, "fluid", registries);
    }
}
