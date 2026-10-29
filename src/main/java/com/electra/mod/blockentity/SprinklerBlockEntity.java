package com.electra.mod.blockentity;

import com.electra.mod.block.SprinklerBlock;
import com.electra.mod.config.ElectraConfig;
import com.electra.mod.fluid.FluidPort;
import com.electra.mod.fluid.FluidPorts;
import com.electra.mod.fluid.ModFluidTank;
import com.electra.mod.setup.ModBlockEntities;
import net.minecraft.core.BlockPos;
import net.minecraft.core.HolderLookup;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.Blocks;
import net.minecraft.world.level.block.CropBlock;
import net.minecraft.world.level.block.FarmBlock;
import net.minecraft.world.level.block.NetherWartBlock;
import net.minecraft.world.level.block.SaplingBlock;
import net.minecraft.world.level.block.StemBlock;
import net.minecraft.world.level.block.SweetBerryBushBlock;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.material.Fluids;
import net.minecraft.world.phys.AABB;
import net.neoforged.neoforge.fluids.FluidStack;
import net.neoforged.neoforge.fluids.capability.IFluidHandler;
import org.jetbrains.annotations.NotNull;

/**
 * Arroseur : une petite cuve (eau ou lave uniquement) vidée à débit constant tant qu'elle n'est pas vide.
 * Chaque seconde, il balaie la zone sous lui. Les plantes ne reçoivent qu'un « tick aléatoire » de plus,
 * donc toutes les règles vanilla restent valables (lumière, terre irriguée, probabilité de croissance).
 */
public class SprinklerBlockEntity extends BlockEntity {

    private static final int SPRAY_INTERVAL = 20;   // ticks entre deux arrosages (1 seconde)

    private final ModFluidTank tank = new ModFluidTank(ElectraConfig.SPRINKLER_TANK.get(),
            SprinklerBlockEntity::isSprayable, this::setChanged);

    /** Reçoit seulement : le réseau de tuyaux ne peut pas le vider. */
    private final FluidPort port = FluidPorts.sink(tank);

    private int cooldown = 0;

    public SprinklerBlockEntity(BlockPos pos, BlockState state) {
        super(ModBlockEntities.SPRINKLER_BE.get(), pos, state);
    }

    public ModFluidTank getTank()   { return tank; }
    public FluidPort getFluidPort() { return port; }

    private static boolean isSprayable(FluidStack stack) {
        return stack.getFluid().isSame(Fluids.WATER) || stack.getFluid().isSame(Fluids.LAVA);
    }

    // ── Tick ───────────────────────────────────────────────────────────────

    public void tick() {
        if (!(level instanceof ServerLevel serverLevel)) return;
        if (--cooldown > 0) return;
        cooldown = SPRAY_INTERVAL;

        FluidStack content = tank.getFluid();
        SprinklerBlock.Mode mode = SprinklerBlock.Mode.IDLE;
        if (!content.isEmpty()) {
            mode = content.getFluid().isSame(Fluids.LAVA) ? SprinklerBlock.Mode.LAVA : SprinklerBlock.Mode.WATER;
            tank.drain(ElectraConfig.SPRINKLER_MB_PER_SECOND.get(), IFluidHandler.FluidAction.EXECUTE);
        }

        BlockState state = getBlockState();
        if (state.getValue(SprinklerBlock.MODE) != mode) {
            serverLevel.setBlock(worldPosition, state.setValue(SprinklerBlock.MODE, mode), Block.UPDATE_ALL);
        }
        if (mode == SprinklerBlock.Mode.IDLE) return;

        spray(serverLevel, mode == SprinklerBlock.Mode.LAVA);
    }

    private void spray(ServerLevel level, boolean lava) {
        int radius = ElectraConfig.SPRINKLER_RADIUS.get();
        int depth = ElectraConfig.SPRINKLER_DEPTH.get();
        double chance = ElectraConfig.SPRINKLER_GROWTH_CHANCE.get();

        BlockPos.MutableBlockPos pos = new BlockPos.MutableBlockPos();
        for (int dx = -radius; dx <= radius; dx++) {
            for (int dz = -radius; dz <= radius; dz++) {
                for (int dy = 1; dy <= depth; dy++) {
                    pos.set(worldPosition.getX() + dx, worldPosition.getY() - dy, worldPosition.getZ() + dz);
                    if (!level.isLoaded(pos)) continue;            // ne jamais charger de tronçon
                    BlockState state = level.getBlockState(pos);

                    if (lava) {
                        if (state.getBlock() instanceof NetherWartBlock && level.random.nextDouble() < chance) {
                            state.randomTick(level, pos.immutable(), level.random);
                        }
                    } else if (state.is(Blocks.FARMLAND)) {
                        // Terre irriguée en permanence tant que l'arroseur marche
                        if (state.getValue(FarmBlock.MOISTURE) < FarmBlock.MAX_MOISTURE) {
                            level.setBlock(pos, state.setValue(FarmBlock.MOISTURE, FarmBlock.MAX_MOISTURE), Block.UPDATE_CLIENTS);
                        }
                    } else if (isWaterPlant(state) && level.random.nextDouble() < chance) {
                        state.randomTick(level, pos.immutable(), level.random);
                    }
                }
            }
        }

        if (lava) burnEntities(level, radius, depth);
    }

    private static boolean isWaterPlant(BlockState state) {
        Block block = state.getBlock();
        return block instanceof CropBlock || block instanceof StemBlock
                || block instanceof SaplingBlock || block instanceof SweetBerryBushBlock;
    }

    /** Sous les gouttes de lave : feu et dégâts sur les créatures, mais aucun bloc n'est enflammé. */
    private void burnEntities(ServerLevel level, int radius, int depth) {
        AABB zone = new AABB(
                worldPosition.getX() - radius, worldPosition.getY() - depth, worldPosition.getZ() - radius,
                worldPosition.getX() + radius + 1, worldPosition.getY(), worldPosition.getZ() + radius + 1);
        float damage = ElectraConfig.SPRINKLER_LAVA_DAMAGE.get().floatValue();
        for (LivingEntity entity : level.getEntitiesOfClass(LivingEntity.class, zone)) {
            if (entity.fireImmune()) continue;
            entity.igniteForSeconds(4);
            if (damage > 0) entity.hurt(level.damageSources().lava(), damage);
        }
    }

    // ── Sauvegarde ─────────────────────────────────────────────────────────

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
