package com.electra.mod.blockentity;

import com.electra.mod.block.RedstoneDynamoBlock;
import com.electra.mod.config.ElectraConfig;
import com.electra.mod.energy.ModEnergyStorage;
import com.electra.mod.setup.ModBlockEntities;
import com.electra.mod.setup.ModBlocks;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.core.HolderLookup;
import net.minecraft.core.particles.ParticleTypes;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.network.protocol.Packet;
import net.minecraft.network.protocol.game.ClientGamePacketListener;
import net.minecraft.network.protocol.game.ClientboundBlockEntityDataPacket;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.sounds.SoundEvents;
import net.minecraft.sounds.SoundSource;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.Blocks;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraft.world.level.block.state.BlockState;
import net.neoforged.neoforge.common.Tags;
import org.jetbrains.annotations.NotNull;

/**
 * Logique de la Redstone Dynamo (structure décrite dans RedstoneDynamoBlock).
 *
 * - Le bloc de fer rouge posé contre un côté de la Dynamo s'accroche au bras : il quitte le monde
 *   et n'existe plus que dans la Dynamo, qui le dessine en orbite.
 * - speed va de 0 (arrêt) à 1 (pleine vitesse) ; la production vaut speed × generation.
 * - Le bras accélère tant que la Dynamo reçoit un signal, puis ralentit en continuant à produire.
 * - Un obstacle dans le couloir bloque le bras : arrêt immédiat, reprise une fois dégagé.
 * - Source pure : les câbles viennent aspirer l'énergie (par une extrémité de l'axe).
 * - Le plan de la structure dépend de l'axe de la Dynamo (horizontal si AXIS = Y, vertical sinon).
 */
public class RedstoneDynamoBlockEntity extends BlockEntity {

    /** Vitesse du bras à plein régime, en degrés par tick (9 = un tour toutes les 2 secondes). */
    public static final float MAX_DEGREES_PER_TICK = 9.0f;

    private static final int STRUCTURE_CHECK_INTERVAL = 20;
    private static final int SYNC_INTERVAL = 5;

    private final ModEnergyStorage energyStorage = new ModEnergyStorage(
            ElectraConfig.DYNAMO_CAPACITY.get(), 0, ElectraConfig.DYNAMO_MAX_EXTRACT.get(), this::onEnergyChanged);

    private boolean hasRotor = false;
    private boolean pathClear = false;
    private float speed = 0.0f;
    /** Position du bras en degrés, sens horaire vu de dessus : 0 = est, 90 = sud, 180 = ouest, 270 = nord. */
    private float angle = 0.0f;
    /** Fraction de FE pas encore produite (la production par tick n'est pas toujours entière). */
    private float pendingEnergy = 0.0f;

    private boolean structureDirty = true;
    private int structureTimer = 0;
    private int syncTimer = 0;
    private boolean needsSync = false;

    // Client uniquement
    private float prevAngle = 0.0f;
    private boolean clientAngleSet = false;

    public RedstoneDynamoBlockEntity(BlockPos pos, BlockState state) {
        super(ModBlockEntities.REDSTONE_DYNAMO_BE.get(), pos, state);
    }

    public ModEnergyStorage getEnergyStorage() { return energyStorage; }
    public boolean hasRotor()                  { return hasRotor; }
    public float getSpeed()                    { return speed; }
    public ItemStack getRotorStack()           { return new ItemStack(ModBlocks.RED_IRON_BLOCK_ITEM.get()); }

    public void markStructureDirty() { structureDirty = true; }

    private void onEnergyChanged() {
        setChanged();
    }

    // ── Serveur ────────────────────────────────────────────────────────────

    public void serverTick() {
        if (level == null) return;

        // 1. Structure : vérifiée chaque seconde, ou dès qu'un voisin direct change
        if (structureDirty || ++structureTimer >= STRUCTURE_CHECK_INTERVAL) {
            structureDirty = false;
            structureTimer = 0;
            updateStructure();
        }
        boolean formed = getBlockState().getValue(RedstoneDynamoBlock.FORMED);

        // 2. Vitesse
        float previousSpeed = speed;
        if (!formed || !pathClear) {
            speed = 0.0f;                                           // structure incomplète ou bras bloqué
        } else if (level.hasNeighborSignal(worldPosition)) {
            speed = Math.min(1.0f, speed + 1.0f / ElectraConfig.DYNAMO_SPIN_UP_TICKS.get());
        } else {
            speed = Math.max(0.0f, speed - 1.0f / ElectraConfig.DYNAMO_SPIN_DOWN_TICKS.get());
        }
        if (speed != previousSpeed) needsSync = true;

        // 3. Rotation et production
        if (speed > 0.0f) {
            angle = (angle + speed * MAX_DEGREES_PER_TICK) % 360.0f;
            pendingEnergy += speed * ElectraConfig.DYNAMO_GENERATION.get();
            int whole = (int) pendingEnergy;
            if (whole > 0) {
                energyStorage.insertInternal(whole);
                pendingEnergy -= whole;
            }
            setChanged();
        }

        // 4. Synchronisation avec les clients (vitesse du bras)
        if (needsSync && ++syncTimer >= SYNC_INTERVAL) {
            syncTimer = 0;
            needsSync = false;
            sync();
        }
    }

    /** Accroche le fer rouge, vérifie l'anneau et le couloir, met à jour l'état FORMED. */
    private void updateStructure() {
        boolean ringComplete = isRingComplete();

        // Accrochage : anneau complet + bloc de fer rouge contre un côté de la Dynamo (hors extrémités de l'axe)
        if (ringComplete && !hasRotor) {
            for (Direction side : Direction.values()) {
                if (side.getAxis() == axis()) continue;
                BlockPos sidePos = worldPosition.relative(side);
                if (level.getBlockState(sidePos).is(ModBlocks.RED_IRON_BLOCK.get())) {
                    level.setBlock(sidePos, Blocks.AIR.defaultBlockState(), Block.UPDATE_ALL);
                    hasRotor = true;
                    angle = angleOf(axis(), side);
                    setChanged();
                    onMounted();
                    break;
                }
            }
        }

        pathClear = isPathClear();

        boolean formed = ringComplete && hasRotor;
        BlockState state = getBlockState();
        if (state.getValue(RedstoneDynamoBlock.FORMED) != formed) {
            level.setBlock(worldPosition, state.setValue(RedstoneDynamoBlock.FORMED, formed), Block.UPDATE_ALL);
        }
        sync();
    }

    private Direction.Axis axis() {
        return getBlockState().getValue(RedstoneDynamoBlock.AXIS);
    }

    /** Position dans le plan de rotation (perpendiculaire à l'axe) : a et b en blocs depuis la Dynamo. */
    private BlockPos inPlane(int a, int b) {
        return switch (axis()) {
            case X -> worldPosition.offset(0, a, b);
            case Y -> worldPosition.offset(a, 0, b);
            case Z -> worldPosition.offset(a, b, 0);
        };
    }

    /** Anneau 5×5 autour de la Dynamo, dans son plan : 12 blocs de redstone, coins facultatifs. */
    private boolean isRingComplete() {
        for (int a = -2; a <= 2; a++) {
            for (int b = -2; b <= 2; b++) {
                boolean onRing = Math.abs(a) == 2 || Math.abs(b) == 2;
                boolean corner = Math.abs(a) == 2 && Math.abs(b) == 2;
                if (!onRing || corner) continue;
                if (!level.getBlockState(inPlane(a, b)).is(Tags.Blocks.STORAGE_BLOCKS_REDSTONE)) return false;
            }
        }
        return true;
    }

    /** Les 8 cases autour de la Dynamo, dans son plan, doivent être libres pour que le bras tourne. */
    private boolean isPathClear() {
        for (int a = -1; a <= 1; a++) {
            for (int b = -1; b <= 1; b++) {
                if (a == 0 && b == 0) continue;
                BlockState state = level.getBlockState(inPlane(a, b));
                if (!state.isAir() && !state.canBeReplaced()) return false;
            }
        }
        return true;
    }

    /** Clic droit accroupi : le fer rouge retourne dans l'inventaire du joueur (ou tombe au sol). */
    public void dismountRotor(Player player) {
        if (!hasRotor) return;
        hasRotor = false;
        speed = 0.0f;
        ItemStack stack = getRotorStack();
        if (!player.getInventory().add(stack)) player.drop(stack, false);
        level.playSound(null, worldPosition, SoundEvents.ARMOR_EQUIP_IRON.value(), SoundSource.BLOCKS, 1.0f, 0.8f);
        setChanged();
        markStructureDirty();
        sync();
    }

    /**
     * Angle de départ du bras pour un fer rouge posé du côté « side ».
     * Le modèle du bras pointe vers +X et tourne autour de son Y ; RedstoneDynamoRenderer aligne ce Y
     * sur l'axe de la Dynamo. On exprime ici « side » dans ce repère (L = rotation inverse de l'alignement),
     * puis angle = atan2(Lz, Lx), en degrés.
     */
    private static float angleOf(Direction.Axis axis, Direction side) {
        float dx = side.getStepX(), dy = side.getStepY(), dz = side.getStepZ();
        float lx, lz;
        switch (axis) {
            case X -> { lx = -dy; lz = dz; }    // alignement : rotation Z de -90°
            case Z -> { lx = dx;  lz = -dy; }   // alignement : rotation X de +90°
            default -> { lx = dx; lz = dz; }    // Y : pas d'alignement
        }
        float degrees = (float) Math.toDegrees(Math.atan2(lz, lx));
        return degrees < 0 ? degrees + 360.0f : degrees;
    }

    /** Retour visuel et sonore : le fer rouge vient de s'accrocher. */
    private void onMounted() {
        if (!(level instanceof ServerLevel serverLevel)) return;
        serverLevel.sendParticles(ParticleTypes.ELECTRIC_SPARK,
                worldPosition.getX() + 0.5, worldPosition.getY() + 0.5, worldPosition.getZ() + 0.5,
                40, 1.2, 0.4, 1.2, 0.15);
        serverLevel.playSound(null, worldPosition, SoundEvents.BEACON_ACTIVATE, SoundSource.BLOCKS, 0.6f, 1.6f);
    }

    private void sync() {
        if (level instanceof ServerLevel serverLevel) {
            serverLevel.sendBlockUpdated(worldPosition, getBlockState(), getBlockState(), Block.UPDATE_CLIENTS);
        }
    }

    // ── Client ─────────────────────────────────────────────────────────────

    public void clientTick() {
        prevAngle = angle;
        angle += speed * MAX_DEGREES_PER_TICK;
        if (angle >= 360.0f) {
            angle -= 360.0f;
            prevAngle -= 360.0f;
        }
    }

    /** Angle interpolé entre deux ticks, pour le rendu. */
    public float getRenderAngle(float partialTick) {
        return prevAngle + (angle - prevAngle) * partialTick;
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
        tag.putInt("energy", energyStorage.getEnergyStored());
        tag.putBoolean("hasRotor", hasRotor);
        tag.putBoolean("pathClear", pathClear);
        tag.putFloat("speed", speed);
        tag.putFloat("angle", angle);
        tag.putFloat("pendingEnergy", pendingEnergy);
    }

    @Override
    protected void loadAdditional(@NotNull CompoundTag tag, HolderLookup.@NotNull Provider registries) {
        super.loadAdditional(tag, registries);
        energyStorage.setEnergy(tag.getInt("energy"));
        boolean hadRotor = hasRotor;
        hasRotor = tag.getBoolean("hasRotor");
        pathClear = tag.getBoolean("pathClear");
        speed = tag.getFloat("speed");
        pendingEnergy = tag.getFloat("pendingEnergy");

        // Côté client, l'angle avance localement : on ne le recale qu'au premier chargement
        // ou quand le fer rouge vient d'être accroché, pour éviter les à-coups.
        boolean client = level != null && level.isClientSide();
        if (!client || !clientAngleSet || (hasRotor && !hadRotor)) {
            angle = tag.getFloat("angle");
            prevAngle = angle;
            clientAngleSet = client;
        }
    }
}
