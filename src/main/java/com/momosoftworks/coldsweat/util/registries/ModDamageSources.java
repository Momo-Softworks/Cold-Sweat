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
    public static final ResourceKey<DamageType> HOT = register("hot");

    public static boolean isFreezing(DamageSource damageSource)
    {   return damageSource.is(COLD);
    }
    
    public static boolean isBurning(DamageSource damageSource)
    {   return damageSource.is(HOT);
    }

    private static ResourceKey<DamageType> register(String name)
    {   return ResourceKey.create(Registries.DAMAGE_TYPE, ResourceLocation.fromNamespaceAndPath(ColdSweat.MOD_ID, name));
    }
}
