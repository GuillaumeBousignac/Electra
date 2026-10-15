package com.electra.mod.blockentity;

import com.electra.mod.block.ElectricLampBlock;
import com.electra.mod.block.LampSymbol;
import com.electra.mod.energy.LampGroups;
import com.electra.mod.energy.ModEnergyStorage;
import com.electra.mod.setup.ModBlockEntities;
import net.minecraft.core.BlockPos;
import net.minecraft.core.HolderLookup;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.network.protocol.Packet;
import net.minecraft.network.protocol.game.ClientGamePacketListener;
import net.minecraft.network.protocol.game.ClientboundBlockEntityDataPacket;
import net.minecraft.world.item.DyeColor;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraft.world.level.block.state.BlockState;
import org.jetbrains.annotations.NotNull;

/**
 * Lampe : l'état allumé est décidé pour tout le groupe de lampes qui se touchent (LampGroups) :
 * il suffit qu'une seule lampe du groupe soit raccordée pour que toutes s'allument.
 * Chaque lampe se recharge par ses faces raccordées (poussée des câbles, ou prélèvement direct
 * sur une machine voisine) ; la consommation du groupe est prise dans l'ensemble des réserves.
 *
 * Elle porte aussi un motif (lettre…) et sa couleur, indépendante de celle de la lampe.
 * Ces deux valeurs sont envoyées aux clients, qui les dessinent (ElectricLampRenderer).
 * Elles ne sont pas des propriétés d'état : 27 motifs × 16 couleurs multiplieraient par 432
 * les 2 048 états déjà nécessaires à la texture connectée.
 */
public class ElectricLampBlockEntity extends BlockEntity {

    /** Une lampe raccordée peut alimenter environ 200 lampes à 1 FE/tick. */
    private static final int BUFFER = 1_000;
    private static final int MAX_RECEIVE = 200;

    private final ModEnergyStorage buffer = new ModEnergyStorage(BUFFER, MAX_RECEIVE, 0, this::setChanged);

    private LampSymbol symbol = LampSymbol.NONE;
    private DyeColor symbolColor = DyeColor.BLACK;

    public ElectricLampBlockEntity(BlockPos pos, BlockState state) {
        super(ModBlockEntities.ELECTRIC_LAMP_BE.get(), pos, state);
    }

    /** Récepteur pur sur toutes les faces : les câbles s'y raccordent et le rechargent. */
    public ModEnergyStorage getEnergyStorage() { return buffer; }

    public void tick() {
        if (level == null || level.isClientSide()) return;

        boolean lit = LampGroups.isLit(level, worldPosition);
        BlockState state = getBlockState();
        if (state.getValue(ElectricLampBlock.LIT) != lit) {
            level.setBlock(worldPosition, state.setValue(ElectricLampBlock.LIT, lit), Block.UPDATE_CLIENTS);
        }
    }

    // ── Motif ──────────────────────────────────────────────────────────────

    public LampSymbol getSymbol() { return symbol; }
    public DyeColor getSymbolColor() { return symbolColor; }

    public void setSymbol(LampSymbol symbol) {
        this.symbol = symbol;
        syncToClients();
    }

    public void setSymbolColor(DyeColor color) {
        this.symbolColor = color;
        syncToClients();
    }

    /** Autocollant retiré : plus de motif, couleur par défaut pour le prochain. */
    public void clearSymbol() {
        this.symbol = LampSymbol.NONE;
        this.symbolColor = DyeColor.BLACK;
        syncToClients();
    }

    private void syncToClients() {
        setChanged();
        if (level != null && !level.isClientSide()) {
            level.sendBlockUpdated(worldPosition, getBlockState(), getBlockState(), Block.UPDATE_CLIENTS);
        }
    }

    private void writeSymbol(CompoundTag tag) {
        tag.putString("symbol", symbol.id());
        tag.putString("symbol_color", symbolColor.getSerializedName());
    }

    // ── Sauvegarde et envoi aux clients ────────────────────────────────────

    @Override
    protected void saveAdditional(@NotNull CompoundTag tag, HolderLookup.@NotNull Provider registries) {
        super.saveAdditional(tag, registries);
        tag.putInt("energy", buffer.getEnergyStored());
        writeSymbol(tag);
    }

    @Override
    protected void loadAdditional(@NotNull CompoundTag tag, HolderLookup.@NotNull Provider registries) {
        super.loadAdditional(tag, registries);
        // Les paquets envoyés aux clients ne contiennent pas l'énergie : on ne la remet pas à zéro
        if (tag.contains("energy")) buffer.setEnergy(tag.getInt("energy"));
        symbol = LampSymbol.byId(tag.getString("symbol"));
        symbolColor = DyeColor.byName(tag.getString("symbol_color"), DyeColor.BLACK);
    }

    /** Envoyé au chargement du tronçon de monde : seulement le motif. */
    @Override
    public @NotNull CompoundTag getUpdateTag(HolderLookup.@NotNull Provider registries) {
        CompoundTag tag = new CompoundTag();
        writeSymbol(tag);
        return tag;
    }

    /** Envoyé à chaque changement (sendBlockUpdated) ; le client le lit avec loadAdditional. */
    @Override
    public Packet<ClientGamePacketListener> getUpdatePacket() {
        return ClientboundBlockEntityDataPacket.create(this);
    }
}
