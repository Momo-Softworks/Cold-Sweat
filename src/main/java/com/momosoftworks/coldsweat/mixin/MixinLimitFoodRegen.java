package com.momosoftworks.coldsweat.mixin;

import com.momosoftworks.coldsweat.api.temperature.effect.TempEffect;
import com.momosoftworks.coldsweat.api.temperature.effect.TempEffectType;
import com.momosoftworks.coldsweat.common.capability.handler.EntityTempManager;
import com.momosoftworks.coldsweat.common.capability.temperature.ITemperatureCap;
import com.momosoftworks.coldsweat.config.ConfigSettings;
import com.momosoftworks.coldsweat.core.init.TempEffectInit;
import com.momosoftworks.coldsweat.util.math.CSMath;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.util.FoodStats;
import net.minecraft.world.GameRules;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.Redirect;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

import java.util.HashMap;
import java.util.Map;

@Mixin(FoodStats.class)
public class MixinLimitFoodRegen
{
    private static PlayerEntity STORED_PLAYER;

    @Inject(method = "tick", at = @At("HEAD"))
    private void storePlayer(PlayerEntity player, CallbackInfo ci)
    {   STORED_PLAYER = player;
    }

    @Redirect(method = "tick", at = @At(value = "INVOKE", target = "Lnet/minecraft/world/GameRules;getBoolean(Lnet/minecraft/world/GameRules$RuleKey;)Z"))
    private boolean limitRegenIfFreezing(GameRules gameRules, GameRules.RuleKey<GameRules.BooleanValue> key)
    {
        checkFrozenHealth:
        if (key == GameRules.RULE_NATURAL_REGENERATION)
        {
            Map<TempEffectType<?>, TempEffect> tempEffects = EntityTempManager.getTemperatureCap(STORED_PLAYER).map(ITemperatureCap::getTempEffects).orElse(new HashMap<>());
            TempEffect freezeHealingEffect = tempEffects.get(TempEffectInit.FREEZE_HEALING.get());
            if (freezeHealingEffect != null)
            {
                double effect = freezeHealingEffect.getEffectFactor(STORED_PLAYER);
                double heartsFreezePercentage = ConfigSettings.HEARTS_FREEZING_PERCENTAGE.get();
                if (heartsFreezePercentage == 0)
                {   break checkFrozenHealth;
                }

                float maxHealth = STORED_PLAYER.getMaxHealth();

                float maxFrozenHealth = (float) (maxHealth * heartsFreezePercentage);
                float frozenHealth = Math.round(CSMath.blend(0, maxFrozenHealth, effect, 0, 1));
                float unfrozenHealth = maxHealth - frozenHealth;
                if (STORED_PLAYER.getHealth() >= unfrozenHealth)
                {   return false;
                }
            }
        }
        return gameRules.getBoolean(key);
    }
}
