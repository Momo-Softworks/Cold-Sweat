package dev.momostudios.coldsweat.util;

import net.minecraft.world.damagesource.DamageSource;

public class CSDamageTypes
{
    public static final DamageSource COLD = (new DamageSource("cold"))
        .bypassArmor()
        .bypassMagic();

    public static final DamageSource HOT  = (new DamageSource("hot"))
        .bypassArmor()
        .bypassMagic()
        .setIsFire();

    public static final DamageSource COLD_SCALED = (new DamageSource("cold"))
        .bypassArmor()
        .bypassMagic()
        .setScalesWithDifficulty();

    public static final DamageSource HOT_SCALED  = (new DamageSource("hot"))
        .bypassArmor()
        .bypassMagic()
        .setIsFire()
        .setScalesWithDifficulty();
}
