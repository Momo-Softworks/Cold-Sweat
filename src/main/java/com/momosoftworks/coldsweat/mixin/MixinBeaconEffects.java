package com.momosoftworks.coldsweat.mixin;

import com.momosoftworks.coldsweat.util.registries.ModEffects;
import net.minecraft.potion.Effect;
import net.minecraft.potion.EffectInstance;
import net.minecraft.tileentity.BeaconTileEntity;
import org.spongepowered.asm.mixin.Final;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.ModifyArg;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

@Mixin(BeaconTileEntity.class)
public class MixinBeaconEffects
{
    @Shadow @Final public static Effect[][] BEACON_EFFECTS;

    static
    {
        // get the current top-level effects as a mutable list
        List<Effect> effects = new ArrayList(Arrays.asList(BEACON_EFFECTS[3]));
        // add the insulation effect
        effects.add(ModEffects.INSULATION);
        // set the top-level effects to the new list
        BEACON_EFFECTS[3] = effects.toArray(new Effect[0]);
    }

    @ModifyArg(method = "applyEffects()V",
               at = @At(value = "INVOKE", target = "Lnet/minecraft/entity/player/PlayerEntity;addEffect(Lnet/minecraft/potion/EffectInstance;)Z", ordinal = 1),
               index = 0)
    private EffectInstance modifyEffect(EffectInstance effect)
    {
        if (effect.getEffect() == ModEffects.INSULATION)
        {   return new EffectInstance(effect.getEffect(), effect.getDuration(), 4, effect.isAmbient(), effect.isVisible(), effect.showIcon());
        }
        return effect;
    }
}
