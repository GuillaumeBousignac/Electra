package com.electra.mod.block;

import com.electra.mod.blockentity.ElectricLampBlockEntity;
import com.electra.mod.setup.ModBlockEntities;
import com.mojang.serialization.MapCodec;
import net.minecraft.ChatFormatting;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.network.chat.Component;
import net.minecraft.sounds.SoundEvents;
import net.minecraft.sounds.SoundSource;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.InteractionResult;
import net.minecraft.world.ItemInteractionResult;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.DyeColor;
import net.minecraft.world.item.DyeItem;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.TooltipFlag;
import net.minecraft.world.item.context.BlockPlaceContext;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.LevelAccessor;
import net.minecraft.world.level.block.BaseEntityBlock;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.PipeBlock;
import net.minecraft.world.level.block.RenderShape;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraft.world.level.block.entity.BlockEntityTicker;
import net.minecraft.world.level.block.entity.BlockEntityType;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.block.state.StateDefinition;
import net.minecraft.world.level.block.state.properties.BlockStateProperties;
import net.minecraft.world.level.block.state.properties.BooleanProperty;
import net.minecraft.world.level.block.state.properties.EnumProperty;
import net.minecraft.world.phys.BlockHitResult;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.util.List;
import java.util.Map;

/**
 * Lampe électrique : s'allume tant que du courant arrive sur l'une de ses faces, et consomme un peu de FE.
 * Clic droit avec une teinture : change la couleur du verre (la teinture est consommée).
 * Accroupi + clic droit, main vide : passe au motif suivant (aucun → A → … → Z → aucun).
 * Accroupi + clic droit avec une teinture : change la couleur du motif seulement.
 * (Accroupi, Minecraft ignore normalement le bloc quand on tient un objet : voir LampInteractionEvents.)
 *
 * Texture connectée : les propriétés NORTH…DOWN indiquent qu'une lampe de même couleur touche cette face.
 * Le modèle (multipart, ModBlockStateProvider) retire alors la bordure en cuivre de ce côté,
 * si bien que plusieurs lampes forment un seul panneau. Allumées ou non, elles restent connectées.
 */
public class ElectricLampBlock extends BaseEntityBlock {

    public static final MapCodec<ElectricLampBlock> CODEC = simpleCodec(ElectricLampBlock::new);

    public static final EnumProperty<DyeColor> COLOR = EnumProperty.create("color", DyeColor.class);
    public static final BooleanProperty LIT = BlockStateProperties.LIT;

    public static final Map<Direction, BooleanProperty> CONNECTIONS = PipeBlock.PROPERTY_BY_DIRECTION;

    public ElectricLampBlock(Properties properties) {
        super(properties);
        BlockState state = stateDefinition.any().setValue(COLOR, DyeColor.WHITE).setValue(LIT, false);
        for (BooleanProperty connection : CONNECTIONS.values()) state = state.setValue(connection, false);
        registerDefaultState(state);
    }

    @Override
    protected void createBlockStateDefinition(StateDefinition.Builder<Block, BlockState> builder) {
        builder.add(COLOR, LIT);
        CONNECTIONS.values().forEach(builder::add);
    }

    // ── Texture connectée ─────────────────────────────────────────────────

    private static boolean connects(BlockState self, BlockState neighbor) {
        return neighbor.getBlock() instanceof ElectricLampBlock && neighbor.getValue(COLOR) == self.getValue(COLOR);
    }

    /** Recalcule les 6 connexions (pose, changement de couleur). */
    private static BlockState withConnections(BlockState state, LevelAccessor level, BlockPos pos) {
        for (Direction dir : Direction.values()) {
            state = state.setValue(CONNECTIONS.get(dir), connects(state, level.getBlockState(pos.relative(dir))));
        }
        return state;
    }

    @Override
    public BlockState getStateForPlacement(@NotNull BlockPlaceContext context) {
        return withConnections(defaultBlockState(), context.getLevel(), context.getClickedPos());
    }

    @Override
    protected @NotNull BlockState updateShape(@NotNull BlockState state, @NotNull Direction direction,
                                              @NotNull BlockState neighborState, @NotNull LevelAccessor level,
                                              @NotNull BlockPos pos, @NotNull BlockPos neighborPos) {
        return state.setValue(CONNECTIONS.get(direction), connects(state, neighborState));
    }

    @Override
    protected @NotNull ItemInteractionResult useItemOn(@NotNull ItemStack stack, @NotNull BlockState state,
                                                       @NotNull Level level, @NotNull BlockPos pos,
                                                       @NotNull Player player, @NotNull InteractionHand hand,
                                                       @NotNull BlockHitResult hit) {
        if (!(stack.getItem() instanceof DyeItem dye)) {
            return ItemInteractionResult.PASS_TO_DEFAULT_BLOCK_INTERACTION;
        }
        if (player.isShiftKeyDown()) {
            // Couleur du motif
            if (!(level.getBlockEntity(pos) instanceof ElectricLampBlockEntity lamp)) {
                return ItemInteractionResult.PASS_TO_DEFAULT_BLOCK_INTERACTION;
            }
            if (lamp.getSymbolColor() == dye.getDyeColor()) return ItemInteractionResult.CONSUME;
            if (!level.isClientSide()) {
                lamp.setSymbolColor(dye.getDyeColor());
                level.playSound(null, pos, SoundEvents.DYE_USE, SoundSource.BLOCKS, 1.0f, 1.2f);
                stack.consume(1, player);
            }
            return ItemInteractionResult.sidedSuccess(level.isClientSide());
        }
        if (state.getValue(COLOR) == dye.getDyeColor()) return ItemInteractionResult.CONSUME;
        if (!level.isClientSide()) {
            // Nouvelle couleur : ses connexions changent ; les voisines se mettent à jour via updateShape
            level.setBlock(pos, withConnections(state.setValue(COLOR, dye.getDyeColor()), level, pos), Block.UPDATE_ALL);
            level.playSound(null, pos, SoundEvents.DYE_USE, SoundSource.BLOCKS, 1.0f, 1.0f);
            stack.consume(1, player);
        }
        return ItemInteractionResult.sidedSuccess(level.isClientSide());
    }

    /** Accroupi + clic droit, main vide : motif suivant. */
    @Override
    protected @NotNull InteractionResult useWithoutItem(@NotNull BlockState state, @NotNull Level level,
                                                        @NotNull BlockPos pos, @NotNull Player player,
                                                        @NotNull BlockHitResult hit) {
        if (!player.isShiftKeyDown()) return InteractionResult.PASS;
        if (!(level.getBlockEntity(pos) instanceof ElectricLampBlockEntity lamp)) return InteractionResult.PASS;
        if (!level.isClientSide()) {
            LampSymbol symbol = lamp.getSymbol().next();
            lamp.setSymbol(symbol);
            level.playSound(null, pos, SoundEvents.ITEM_FRAME_ROTATE_ITEM, SoundSource.BLOCKS, 0.8f, 1.0f);
            player.displayClientMessage(Component.translatable("message.electra.lamp_symbol", symbol.displayName()), true);
        }
        return InteractionResult.sidedSuccess(level.isClientSide());
    }

    @Override
    public void appendHoverText(@NotNull ItemStack stack, @NotNull Item.TooltipContext context,
                                @NotNull List<Component> tooltip, @NotNull TooltipFlag flag) {
        tooltip.add(Component.translatable("tooltip.electra.electric_lamp").withStyle(ChatFormatting.GRAY));
        tooltip.add(Component.translatable("tooltip.electra.electric_lamp_symbol").withStyle(ChatFormatting.GRAY));
    }

    @Override
    protected @NotNull MapCodec<? extends BaseEntityBlock> codec() {
        return CODEC;
    }

    @Override
    protected @NotNull RenderShape getRenderShape(@NotNull BlockState state) {
        return RenderShape.MODEL;
    }

    @Nullable
    @Override
    public BlockEntity newBlockEntity(@NotNull BlockPos pos, @NotNull BlockState state) {
        return new ElectricLampBlockEntity(pos, state);
    }

    @Nullable
    @Override
    public <T extends BlockEntity> BlockEntityTicker<T> getTicker(@NotNull Level level, @NotNull BlockState state,
                                                                  @NotNull BlockEntityType<T> type) {
        if (level.isClientSide()) return null;
        return createTickerHelper(type, ModBlockEntities.ELECTRIC_LAMP_BE.get(), (lvl, pos, st, be) -> be.tick());
    }
}
