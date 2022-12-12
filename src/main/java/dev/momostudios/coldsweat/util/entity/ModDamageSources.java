package dev.momostudios.coldsweat.util.entity;

import net.minecraft.world.damagesource.DamageSource;

public class ModDamageSources
{
    private ModDamageSources() {}

    public static final DamageSource COLD = (new DamageSource("cold"))
        .bypassArmor()
        .bypassMagic();

    public static final DamageSource HOT  = (new DamageSource("hot"))
        .bypassArmor()
        .bypassMagic()
        .setIsFire();
}
