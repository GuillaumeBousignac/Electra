package com.electra.mod.setup;

import com.electra.mod.Electra;
import net.minecraft.core.registries.Registries;
import net.minecraft.resources.ResourceKey;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.damagesource.DamageSource;
import net.minecraft.world.damagesource.DamageType;
import net.minecraft.world.level.Level;

/** Type de dégâts défini dans data/electra/damage_type/electrocution.json. */
public final class ModDamageTypes {

    public static final ResourceKey<DamageType> ELECTROCUTION = ResourceKey.create(
            Registries.DAMAGE_TYPE, ResourceLocation.fromNamespaceAndPath(Electra.MODID, "electrocution"));

    private ModDamageTypes() {}

    public static DamageSource electrocution(Level level) {
        return new DamageSource(level.registryAccess()
                .registryOrThrow(Registries.DAMAGE_TYPE)
                .getHolderOrThrow(ELECTROCUTION));
    }
}
