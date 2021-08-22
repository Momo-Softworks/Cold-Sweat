package net.momostudios.coldsweat.core.util;

import net.minecraft.util.DamageSource;

public class CustomDamageTypes
{
    public static final DamageSource COLD = (new DamageSource("cold")).setDamageBypassesArmor().setDifficultyScaled();
    public static final DamageSource HOT = (new DamageSource("hot")).setDamageBypassesArmor().setFireDamage().setDifficultyScaled();
}
