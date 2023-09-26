package com.momosoftworks.coldsweat.mixin;

import com.momosoftworks.coldsweat.util.registries.ModEffects;
import net.minecraft.world.effect.MobEffect;
import net.minecraft.world.effect.MobEffectInstance;
import net.minecraft.world.effect.MobEffects;
import net.minecraft.world.level.block.entity.BeaconBlockEntity;
import org.spongepowered.asm.mixin.Final;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.ModifyArg;

import java.util.Arrays;
import java.util.Set;
import java.util.stream.Collectors;

@Mixin(BeaconBlockEntity.class)
public class MixinBeaconEffects
{
    @Final
    @Shadow
    public static final MobEffect[][] BEACON_EFFECTS = new MobEffect[][]
    {
        {MobEffects.MOVEMENT_SPEED, MobEffects.DIG_SPEED},
        {MobEffects.DAMAGE_RESISTANCE, MobEffects.JUMP},
        {MobEffects.DAMAGE_BOOST},
        {MobEffects.REGENERATION, ModEffects.INSULATION}
    };

    @Final
    @Shadow
    private static final Set<MobEffect> VALID_EFFECTS = Arrays.stream(BEACON_EFFECTS).flatMap(Arrays::stream).collect(Collectors.toSet());

    @ModifyArg(method = "applyEffects(Lnet/minecraft/world/level/Level;Lnet/minecraft/core/BlockPos;ILnet/minecraft/world/effect/MobEffect;Lnet/minecraft/world/effect/MobEffect;)V",
               at = @At(value = "INVOKE", target = "Lnet/minecraft/world/entity/player/Player;addEffect(Lnet/minecraft/world/effect/MobEffectInstance;)Z", ordinal = 1))
    private static MobEffectInstance modifyEffect(MobEffectInstance effect)
    {
        if (effect.getEffect() == ModEffects.INSULATION)
        {   return new MobEffectInstance(effect.getEffect(), effect.getDuration(), 4, effect.isAmbient(), effect.isVisible(), effect.showIcon());
        }
        return effect;
    }
}
