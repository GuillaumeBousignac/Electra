package com.electra.mod.block;

import com.electra.mod.blockentity.ElectricLampBlockEntity;
import com.electra.mod.item.LampStickerItem;
import com.electra.mod.setup.ModBlockEntities;
import com.electra.mod.setup.ModItems;
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
import net.minecraft.world.entity.LivingEntity;
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
import net.neoforged.neoforge.common.Tags;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.util.List;
import java.util.Map;

/**
 * Lampe électrique : s'allume tant que du courant arrive sur l'une de ses faces, et consomme un peu de FE.
 * Clic droit avec une teinture : change la couleur du verre (la teinture est consommée).
 *
 * Motif (lettre…) : clic droit avec un autocollant (consommé) pour coller le premier motif de sa famille.
 * Ensuite, sur une lampe qui porte un autocollant :
 *   accroupi + clic droit, main vide       → motif suivant (A → … → Z → A) ;
 *   accroupi + clic droit avec une teinture → couleur du motif seulement ;
 *   clic droit avec une cisaille            → retire l'autocollant, qui est rendu.
 * Une lampe cassée rend aussi son autocollant.
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
        ElectricLampBlockEntity lamp = level.getBlockEntity(pos) instanceof ElectricLampBlockEntity be ? be : null;

        // ── Autocollant : pose le premier motif de sa famille (une seule fois par lampe)
        if (stack.getItem() instanceof LampStickerItem sticker) {
            if (lamp == null) return ItemInteractionResult.PASS_TO_DEFAULT_BLOCK_INTERACTION;
            if (!lamp.getSymbol().isNone()) {
                if (!level.isClientSide()) {
                    player.displayClientMessage(Component.translatable("message.electra.lamp_has_sticker"), true);
                }
                return ItemInteractionResult.CONSUME;
            }
            if (!level.isClientSide()) {
                LampSymbol symbol = LampSymbol.firstOf(sticker.getFamily());
                lamp.setSymbol(symbol);
                level.playSound(null, pos, SoundEvents.ITEM_FRAME_ADD_ITEM, SoundSource.BLOCKS, 1.0f, 1.2f);
                player.displayClientMessage(Component.translatable("message.electra.lamp_symbol", symbol.displayName()), true);
                stack.consume(1, player);
            }
            return ItemInteractionResult.sidedSuccess(level.isClientSide());
        }

        // ── Cisaille : retire l'autocollant et le rend
        if (stack.is(Tags.Items.TOOLS_SHEAR)) {
            if (lamp == null || lamp.getSymbol().isNone()) return ItemInteractionResult.PASS_TO_DEFAULT_BLOCK_INTERACTION;
            if (!level.isClientSide()) {
                ItemStack removed = ModItems.stickerFor(lamp.getSymbol().family());
                lamp.clearSymbol();
                Block.popResourceFromFace(level, pos, hit.getDirection(), removed);
                level.playSound(null, pos, SoundEvents.SHEEP_SHEAR, SoundSource.BLOCKS, 1.0f, 1.0f);
                stack.hurtAndBreak(1, player, LivingEntity.getSlotForHand(hand));
            }
            return ItemInteractionResult.sidedSuccess(level.isClientSide());
        }

        if (!(stack.getItem() instanceof DyeItem dye)) {
            return ItemInteractionResult.PASS_TO_DEFAULT_BLOCK_INTERACTION;
        }

        // ── Accroupi + teinture : couleur du motif (il faut un autocollant)
        if (player.isShiftKeyDown()) {
            if (lamp == null || lamp.getSymbol().isNone()) return ItemInteractionResult.PASS_TO_DEFAULT_BLOCK_INTERACTION;
            if (lamp.getSymbolColor() == dye.getDyeColor()) return ItemInteractionResult.CONSUME;
            if (!level.isClientSide()) {
                lamp.setSymbolColor(dye.getDyeColor());
                level.playSound(null, pos, SoundEvents.DYE_USE, SoundSource.BLOCKS, 1.0f, 1.2f);
                stack.consume(1, player);
            }
            return ItemInteractionResult.sidedSuccess(level.isClientSide());
        }

        // ── Teinture : couleur de la lampe
        if (state.getValue(COLOR) == dye.getDyeColor()) return ItemInteractionResult.CONSUME;
        if (!level.isClientSide()) {
            // Nouvelle couleur : ses connexions changent ; les voisines se mettent à jour via updateShape
            level.setBlock(pos, withConnections(state.setValue(COLOR, dye.getDyeColor()), level, pos), Block.UPDATE_ALL);
            level.playSound(null, pos, SoundEvents.DYE_USE, SoundSource.BLOCKS, 1.0f, 1.0f);
            stack.consume(1, player);
        }
        return ItemInteractionResult.sidedSuccess(level.isClientSide());
    }

    /** Accroupi + clic droit, main vide, sur une lampe qui porte un autocollant : motif suivant. */
    @Override
    protected @NotNull InteractionResult useWithoutItem(@NotNull BlockState state, @NotNull Level level,
                                                        @NotNull BlockPos pos, @NotNull Player player,
                                                        @NotNull BlockHitResult hit) {
        if (!player.isShiftKeyDown()) return InteractionResult.PASS;
        if (!(level.getBlockEntity(pos) instanceof ElectricLampBlockEntity lamp) || lamp.getSymbol().isNone()) {
            return InteractionResult.PASS;
        }
        if (!level.isClientSide()) {
            LampSymbol symbol = lamp.getSymbol().next();
            lamp.setSymbol(symbol);
            level.playSound(null, pos, SoundEvents.ITEM_FRAME_ROTATE_ITEM, SoundSource.BLOCKS, 0.8f, 1.0f);
            player.displayClientMessage(Component.translatable("message.electra.lamp_symbol", symbol.displayName()), true);
        }
        return InteractionResult.sidedSuccess(level.isClientSide());
    }

    /** Lampe cassée (ou remplacée) : l'autocollant est rendu en plus de la lampe. */
    @Override
    protected void onRemove(@NotNull BlockState state, @NotNull Level level, @NotNull BlockPos pos,
                            @NotNull BlockState newState, boolean movedByPiston) {
        if (!state.is(newState.getBlock())
                && level.getBlockEntity(pos) instanceof ElectricLampBlockEntity lamp
                && !lamp.getSymbol().isNone()) {
            Block.popResource(level, pos, ModItems.stickerFor(lamp.getSymbol().family()));
        }
        super.onRemove(state, level, pos, newState, movedByPiston);
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
