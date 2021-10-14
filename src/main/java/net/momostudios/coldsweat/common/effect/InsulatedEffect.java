package net.momostudios.coldsweat.common.effect;

import net.minecraft.entity.LivingEntity;
import net.minecraft.entity.ai.attributes.AttributeModifierManager;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.potion.Effect;
import net.minecraft.potion.EffectType;
import net.momostudios.coldsweat.common.temperature.modifier.HearthTempModifier;
import net.momostudios.coldsweat.core.util.PlayerTemp;

import javax.annotation.Nonnull;

public class InsulatedEffect extends Effect
{
    public InsulatedEffect() {
        super(EffectType.BENEFICIAL, 7355178);
    }

    @Nonnull
    public String getName() {
        return "effect.insulated";
    }

    public boolean isInstant() {
        return false;
    }
}
