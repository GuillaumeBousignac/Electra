package com.electra.mod.blockentity;

import com.electra.mod.block.IronPipeBlock;
import com.electra.mod.config.ElectraConfig;
import com.electra.mod.energy.ModEnergyStorage;
import com.electra.mod.fluid.FluidHelper;
import com.electra.mod.fluid.FluidPort;
import com.electra.mod.fluid.FluidPorts;
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
import net.minecraft.sounds.SoundEvents;
import net.minecraft.sounds.SoundSource;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.Blocks;
import net.minecraft.world.level.block.LiquidBlock;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.material.FluidState;
import net.minecraft.world.level.material.Fluids;
import net.neoforged.neoforge.capabilities.Capabilities;
import net.neoforged.neoforge.fluids.FluidStack;
import net.neoforged.neoforge.fluids.capability.IFluidHandler;
import org.jetbrains.annotations.NotNull;

/**
 * Pompe : consomme du FE pour aspirer une source de fluide située sous elle (jusqu'à la portée configurée),
 * la stocke dans sa cuve et la propose aux tuyaux voisins (et aux cuves collées contre elle).
 * Une source = 1 000 mB = un seau.
 */
public class FluidPumpBlockEntity extends BlockEntity {

    /** Quantité d'une source de fluide dans le monde (mB). */
    private static final int SOURCE_AMOUNT = 1_000;
    private static final int PUSH_INTERVAL = 5;     // ticks entre deux envois aux cuves voisines
    private static final int PUSH_AMOUNT = 250;     // mB par envoi (= 1 000 mB/s, comme l'aspiration)

    /** Reçoit du FE, n'en rend pas. Valeurs lues dans electra-server.toml. */
    private final ModEnergyStorage energy = new ModEnergyStorage(
            ElectraConfig.PUMP_ENERGY_CAPACITY.get(), ElectraConfig.PUMP_MAX_RECEIVE.get(), 0, this::onChanged);

    private final ModFluidTank tank = new ModFluidTank(
            ElectraConfig.PUMP_TANK_CAPACITY.get(), stack -> true, this::onChanged);

    /** Le réseau de tuyaux peut y puiser ; personne ne peut y verser. */
    private final FluidPort port = FluidPorts.source(tank);

    private int cooldown = 0;
    /** Distance jusqu'à la source visée (0 = aucune) ; sert à dessiner le tube d'aspiration. */
    private int suctionDepth = 0;
    private boolean needsSync = false;

    public FluidPumpBlockEntity(BlockPos pos, BlockState state) {
        super(ModBlockEntities.FLUID_PUMP_BE.get(), pos, state);
    }

    public ModEnergyStorage getEnergyStorage() { return energy; }
    public FluidPort getFluidPort()            { return port; }
    public ModFluidTank getTank()              { return tank; }
    public int getSuctionDepth()               { return suctionDepth; }

    private void onChanged() {
        setChanged();
        needsSync = true;
    }

    // ── Tick ───────────────────────────────────────────────────────────────

    public void tick() {
        if (level == null || level.isClientSide()) return;

        if (--cooldown <= 0) {
            cooldown = ElectraConfig.PUMP_INTERVAL_TICKS.get();
            pump();
        }
        if (level.getGameTime() % PUSH_INTERVAL == 0) pushToNeighbors();

        if (needsSync && level.getGameTime() % 10 == 0 && level instanceof ServerLevel serverLevel) {
            needsSync = false;
            serverLevel.sendBlockUpdated(worldPosition, getBlockState(), getBlockState(), Block.UPDATE_CLIENTS);
        }
    }

    /** Cherche la première source sous la pompe et l'aspire si l'énergie et la place le permettent. */
    private void pump() {
        BlockPos target = null;
        FluidState found = null;
        int depth = 0;
        int range = ElectraConfig.PUMP_RANGE.get();

        for (int d = 1; d <= range; d++) {
            BlockPos pos = worldPosition.below(d);
            BlockState state = level.getBlockState(pos);
            FluidState fluid = state.getFluidState();
            // Seuls les vrais blocs de fluide : on ne détruit jamais une algue ou un bloc « waterlogged »
            if (state.getBlock() instanceof LiquidBlock && fluid.isSource()) {
                target = pos;
                found = fluid;
                depth = d;
                break;
            }
            if (!state.getCollisionShape(level, pos).isEmpty()) break;   // un bloc plein coupe le tube
        }
        setSuctionDepth(target == null ? 0 : depth);
        if (target == null) return;

        int cost = ElectraConfig.PUMP_ENERGY_PER_BUCKET.get();
        if (energy.getEnergyStored() < cost) return;

        FluidStack bucket = new FluidStack(found.getType(), SOURCE_AMOUNT);
        // Cuve pleine, ou autre fluide déjà présent : on n'aspire pas (la source reste intacte)
        if (tank.fill(bucket, IFluidHandler.FluidAction.SIMULATE) < SOURCE_AMOUNT) return;

        energy.extractInternal(cost);
        level.setBlock(target, Blocks.AIR.defaultBlockState(), Block.UPDATE_ALL);
        tank.fill(bucket, IFluidHandler.FluidAction.EXECUTE);
        level.playSound(null, target,
                found.getType().isSame(Fluids.LAVA) ? SoundEvents.BUCKET_FILL_LAVA : SoundEvents.BUCKET_FILL,
                SoundSource.BLOCKS, 0.3f, 1.0f);
    }

    /** Verse aussi directement dans une cuve collée à la pompe (réservoir, machine d'un autre mod…). */
    private void pushToNeighbors() {
        if (tank.isEmpty()) return;
        for (Direction dir : Direction.values()) {
            BlockPos neighbor = worldPosition.relative(dir);
            // Les tuyaux viennent puiser eux-mêmes dans la pompe (voir FluidNetwork)
            if (level.getBlockState(neighbor).getBlock() instanceof IronPipeBlock) continue;
            IFluidHandler handler = level.getCapability(Capabilities.FluidHandler.BLOCK, neighbor, dir.getOpposite());
            if (handler == null) continue;

            FluidStack offer = tank.drain(PUSH_AMOUNT, IFluidHandler.FluidAction.SIMULATE);
            if (offer.isEmpty()) return;
            int accepted = handler.fill(offer, IFluidHandler.FluidAction.SIMULATE);
            if (accepted <= 0) continue;
            FluidStack moved = tank.drain(FluidHelper.withAmount(offer, accepted), IFluidHandler.FluidAction.EXECUTE);
            handler.fill(moved, IFluidHandler.FluidAction.EXECUTE);
        }
    }

    private void setSuctionDepth(int depth) {
        if (depth == suctionDepth) return;
        suctionDepth = depth;
        setChanged();
        needsSync = true;
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
        tag.putInt("energy", energy.getEnergyStored());
        tag.putInt("suction", suctionDepth);
        tank.save(tag, registries);
    }

    @Override
    protected void loadAdditional(@NotNull CompoundTag tag, HolderLookup.@NotNull Provider registries) {
        super.loadAdditional(tag, registries);
        energy.setEnergy(tag.getInt("energy"));
        suctionDepth = tag.getInt("suction");
        tank.load(tag, registries);
    }
}
