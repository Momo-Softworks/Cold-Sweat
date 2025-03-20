package com.momosoftworks.coldsweat.mixin.compat;

import com.momosoftworks.coldsweat.config.ConfigSettings;
import com.momosoftworks.coldsweat.util.registries.ModEffects;
import net.minecraft.core.BlockPos;
import net.minecraft.world.effect.MobEffectInstance;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.level.Level;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;
import org.spongepowered.asm.mixin.injection.callback.LocalCapture;
import toughasnails.api.temperature.TemperatureLevel;
import toughasnails.temperature.TemperatureHelperImpl;

import java.util.Set;

@Mixin(TemperatureHelperImpl.class)
public class MixinTaNThermoregulator
{
    private static Player PLAYER = null;

    @Inject(method = "thermoregulatorModifier", at = @At("HEAD"),
            remap = false)
    private static void storePlayer(Player player, TemperatureLevel current, CallbackInfoReturnable<TemperatureLevel> cir)
    {   PLAYER = player;
    }

    @Inject(method = "modifyTemperatureByThermoregulators", at = @At("RETURN"),
            locals = LocalCapture.CAPTURE_FAILHARD, remap = false)
    private static void modifyTemperatureByThermoregulators(Level level, Set<BlockPos> thermoregulators, BlockPos checkPos, TemperatureLevel current, CallbackInfoReturnable<TemperatureLevel> cir,
                                                            //local
                                                            int coolingCount, int heatingCount, int neutralCount)
    {
        if (PLAYER != null && !PLAYER.isSpectator())
        {
            if (coolingCount > 0)
            {   PLAYER.addEffect(new MobEffectInstance(ModEffects.FRIGIDNESS, 60, ConfigSettings.THERMOREGULATOR_INSULATION.get() - 1, false, false, true));
            }
            if (heatingCount > 0)
            {   PLAYER.addEffect(new MobEffectInstance(ModEffects.WARMTH, 60, ConfigSettings.THERMOREGULATOR_INSULATION.get() - 1, false, false, true));
            }
        }
    }
}
