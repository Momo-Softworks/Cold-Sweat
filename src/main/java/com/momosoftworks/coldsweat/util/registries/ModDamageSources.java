package com.momosoftworks.coldsweat.util.registries;


import net.minecraft.util.DamageSource;

public class ModDamageSources
{
    private ModDamageSources() {}

    public static final DamageSource COLD = (new DamageSource("cold_sweat:cold"))
        .setDamageBypassesArmor()
        .setMagicDamage();

    public static final DamageSource HOT  = (new DamageSource("cold_sweat:hot"))
        .setDamageBypassesArmor()
        .setFireDamage()
        .setMagicDamage();
}
