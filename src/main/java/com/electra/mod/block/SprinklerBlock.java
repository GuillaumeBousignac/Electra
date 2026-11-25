package com.electra.mod.block;

import com.electra.mod.blockentity.SprinklerBlockEntity;
import com.electra.mod.config.ElectraConfig;
import com.electra.mod.setup.ModBlockEntities;
import com.mojang.serialization.MapCodec;
import net.minecraft.ChatFormatting;
import net.minecraft.core.BlockPos;
import net.minecraft.core.particles.ParticleOptions;
import net.minecraft.core.particles.ParticleTypes;
import net.minecraft.network.chat.Component;
import net.minecraft.util.RandomSource;
import net.minecraft.util.StringRepresentable;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.TooltipFlag;
import net.minecraft.world.level.BlockGetter;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.BaseEntityBlock;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.RenderShape;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraft.world.level.block.entity.BlockEntityTicker;
import net.minecraft.world.level.block.entity.BlockEntityType;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.block.state.StateDefinition;
import net.minecraft.world.level.block.state.properties.EnumProperty;
import net.minecraft.world.phys.shapes.CollisionContext;
import net.minecraft.world.phys.shapes.Shapes;
import net.minecraft.world.phys.shapes.VoxelShape;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.util.List;

/**
 * Arroseur : alimenté en eau ou en lave par les tuyaux, il arrose la zone située sous lui.
 *  - eau  : irrigue les terres labourées et accélère les cultures, tiges et pousses ;
 *  - lave : accélère les verrues du Nether ; les créatures en dessous prennent feu (sans incendie des blocs).
 */
public class SprinklerBlock extends BaseEntityBlock {

    public static final MapCodec<SprinklerBlock> CODEC = simpleCodec(SprinklerBlock::new);

    /** Ce que l'arroseur répand en ce moment : sert à l'état du bloc, aux particules et à la lumière. */
    public enum Mode implements StringRepresentable {
        IDLE("idle"), WATER("water"), LAVA("lava");

        private final String name;

        Mode(String name) { this.name = name; }

        @Override
        public @NotNull String getSerializedName() { return name; }
    }

    public static final EnumProperty<Mode> MODE = EnumProperty.create("mode", Mode.class);

    private static final VoxelShape SHAPE = Shapes.or(
            Block.box(4, 9, 4, 12, 16, 12),     // plateau et collerette
            Block.box(0, 5, 6, 16, 14, 10),     // buses est / ouest
            Block.box(6, 5, 0, 10, 14, 16));    // buses nord / sud

    /** Demi-largeur de la zone de gouttes : 1,5 bloc de chaque côté du centre = 3×3 blocs. */
    private static final double PARTICLE_HALF_WIDTH = 1.5;
    private static final int PARTICLES_PER_TICK = 12;

    public SprinklerBlock(Properties properties) {
        super(properties);
        registerDefaultState(stateDefinition.any().setValue(MODE, Mode.IDLE));
    }

    @Override
    protected void createBlockStateDefinition(StateDefinition.Builder<Block, BlockState> builder) {
        builder.add(MODE);
    }

    @Override
    protected @NotNull MapCodec<? extends BaseEntityBlock> codec() {
        return CODEC;
    }

    @Override
    protected @NotNull VoxelShape getShape(@NotNull BlockState state, @NotNull BlockGetter level,
                                           @NotNull BlockPos pos, @NotNull CollisionContext ctx) {
        return SHAPE;
    }

    @Override
    protected @NotNull RenderShape getRenderShape(@NotNull BlockState state) {
        return RenderShape.MODEL;
    }

    @Nullable
    @Override
    public BlockEntity newBlockEntity(@NotNull BlockPos pos, @NotNull BlockState state) {
        return new SprinklerBlockEntity(pos, state);
    }

    @Nullable
    @Override
    public <T extends BlockEntity> BlockEntityTicker<T> getTicker(@NotNull Level level, @NotNull BlockState state,
                                                                  @NotNull BlockEntityType<T> type) {
        if (level.isClientSide()) return null;
        return createTickerHelper(type, ModBlockEntities.SPRINKLER_BE.get(), (lvl, pos, st, be) -> be.tick());
    }

    /** Gouttes qui tombent sur toute la zone arrosée (côté client uniquement). */
    @Override
    public void animateTick(@NotNull BlockState state, @NotNull Level level, @NotNull BlockPos pos,
                            @NotNull RandomSource random) {
        Mode mode = state.getValue(MODE);
        if (mode == Mode.IDLE || !ElectraConfig.PARTICLES_ENABLED.get()) return;

        // Gouttes visibles : zone réduite de 3×3 blocs sous l'arroseur (l'arrosage réel suit la portée configurée)
        double depth = ElectraConfig.SPRINKLER_DEPTH.get();
        ParticleOptions drop = mode == Mode.WATER ? ParticleTypes.FALLING_WATER : ParticleTypes.FALLING_LAVA;
        for (int i = 0; i < PARTICLES_PER_TICK; i++) {
            double x = pos.getX() + 0.5 + (random.nextDouble() * 2 - 1) * PARTICLE_HALF_WIDTH;
            double y = pos.getY() + 0.5 - random.nextDouble() * depth;
            double z = pos.getZ() + 0.5 + (random.nextDouble() * 2 - 1) * PARTICLE_HALF_WIDTH;
            level.addParticle(drop, x, y, z, 0, 0, 0);
        }
    }

    @Override
    public void appendHoverText(@NotNull ItemStack stack, @NotNull Item.TooltipContext context,
                                @NotNull List<Component> tooltip, @NotNull TooltipFlag flag) {
        tooltip.add(Component.translatable("tooltip.electra.sprinkler").withStyle(ChatFormatting.GRAY));
    }
}
