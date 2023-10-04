package com.momosoftworks.coldsweat.util.registries;

import com.momosoftworks.coldsweat.ColdSweat;
import net.minecraft.core.registries.Registries;
import net.minecraft.resources.ResourceKey;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.damagesource.DamageSource;
import net.minecraft.world.damagesource.DamageType;

public class ModDamageSources
{
    private ModDamageSources() {}

    public static final ResourceKey<DamageType> COLD = register("cold");
    public static final ResourceKey<DamageType> COLD_SCALING = register("cold_scaling");
    public static final ResourceKey<DamageType> HOT = register("hot");
    public static final ResourceKey<DamageType> HOT_SCALING  = register("hot_scaling");

    public static boolean isFreezing(DamageSource damageSource)
    {   return damageSource.is(COLD) || damageSource.is(COLD_SCALING);
    }
    
    public static boolean isBurning(DamageSource damageSource)
    {   return damageSource.is(HOT) || damageSource.is(HOT_SCALING);
    }

    private static ResourceKey<DamageType> register(String name)
    {   return ResourceKey.create(Registries.DAMAGE_TYPE, new ResourceLocation(ColdSweat.MOD_ID, name));
    }
}
