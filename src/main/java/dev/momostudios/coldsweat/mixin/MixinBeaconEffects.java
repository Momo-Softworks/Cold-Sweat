package dev.momostudios.coldsweat.mixin;

import dev.momostudios.coldsweat.ColdSweat;
import dev.momostudios.coldsweat.util.registries.ModEffects;
import net.minecraft.potion.Effect;
import net.minecraft.potion.EffectInstance;
import net.minecraft.potion.Effects;
import net.minecraft.tileentity.BeaconTileEntity;
import org.spongepowered.asm.mixin.Final;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.ModifyArg;

import java.util.Arrays;
import java.util.Set;
import java.util.stream.Collectors;

@Mixin(BeaconTileEntity.class)
public class MixinBeaconEffects
{
    @Final
    @Shadow
    public static final Effect[][] BEACON_EFFECTS = new Effect[][]
    {
        {Effects.MOVEMENT_SPEED, Effects.DIG_SPEED},
        {Effects.DAMAGE_RESISTANCE, Effects.JUMP},
        {Effects.DAMAGE_BOOST},
        {Effects.REGENERATION, ModEffects.INSULATION}
    };

    @Final
    @Shadow
    private static final Set<Effect> VALID_EFFECTS = Arrays.stream(BEACON_EFFECTS).flatMap(Arrays::stream).collect(Collectors.toSet());

    @ModifyArg(method = "applyEffects()V",
               at = @At(value = "INVOKE", target = "Lnet/minecraft/entity/player/PlayerEntity;addEffect(Lnet/minecraft/potion/EffectInstance;)Z", ordinal = 1),
               remap = ColdSweat.REMAP_MIXINS)
    private EffectInstance modifyEffect(EffectInstance effect)
    {
        if (effect.getEffect() == ModEffects.INSULATION)
        {   return new EffectInstance(effect.getEffect(), effect.getDuration(), 4, effect.isAmbient(), effect.isVisible(), effect.showIcon());
        }
        return effect;
    }
}
