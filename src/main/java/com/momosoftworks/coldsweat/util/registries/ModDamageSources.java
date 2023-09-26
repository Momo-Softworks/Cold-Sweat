package com.momosoftworks.coldsweat.util.registries;


import net.minecraft.util.DamageSource;

public class ModDamageSources
{
    private ModDamageSources() {}

    public static final DamageSource COLD = (new DamageSource("cold_sweat:cold"))
        .bypassArmor()
        .bypassMagic();

    public static final DamageSource HOT  = (new DamageSource("cold_sweat:hot"))
        .bypassArmor()
        .bypassMagic()
        .setIsFire();
}
