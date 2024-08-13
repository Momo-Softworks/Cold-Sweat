package com.momosoftworks.coldsweat.mixin;

import com.momosoftworks.coldsweat.core.init.ModEffects;
import net.minecraft.core.Holder;
import net.minecraft.world.effect.MobEffect;
import net.minecraft.world.effect.MobEffectInstance;
import net.minecraft.world.level.block.entity.BeaconBlockEntity;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.ModifyArg;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

import static net.minecraft.world.level.block.entity.BeaconBlockEntity.BEACON_EFFECTS;

@Mixin(BeaconBlockEntity.class)
public class MixinBeaconEffects
{
    /*static
    {
        // get the current top-level effects as a mutable list
        List<Holder<MobEffect>> effects = new ArrayList<>(BEACON_EFFECTS.get(3));
        // add the insulation effect
        effects.add(ModEffects.INSULATED);
        // set the top-level effects to the new list
        BEACON_EFFECTS.set(3, effects);
    }

    @ModifyArg(method = "applyEffects",
               at = @At(value = "INVOKE", target = "Lnet/minecraft/world/entity/player/Player;addEffect(Lnet/minecraft/world/effect/MobEffectInstance;)Z", ordinal = 1))
    private static MobEffectInstance modifyEffect(MobEffectInstance effect)
    {
        if (effect.getEffect() == ModEffects.INSULATED)
        {   return new MobEffectInstance(effect.getEffect(), effect.getDuration(), 4, effect.isAmbient(), effect.isVisible(), effect.showIcon());
        }
        return effect;
    }*/
}
