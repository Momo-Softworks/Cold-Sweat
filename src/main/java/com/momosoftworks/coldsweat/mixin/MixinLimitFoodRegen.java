package com.momosoftworks.coldsweat.mixin;

import com.momosoftworks.coldsweat.api.temperature.effect.TempEffect;
import com.momosoftworks.coldsweat.api.temperature.effect.TempEffectType;
import com.momosoftworks.coldsweat.common.capability.handler.EntityTempManager;
import com.momosoftworks.coldsweat.common.capability.temperature.ITemperatureCap;
import com.momosoftworks.coldsweat.config.ConfigSettings;
import com.momosoftworks.coldsweat.core.init.ModTempEffects;
import com.momosoftworks.coldsweat.util.math.CSMath;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.level.gamerules.GameRule;
import net.minecraft.world.food.FoodData;
import net.minecraft.world.level.gamerules.GameRules;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.Redirect;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

import java.util.HashMap;
import java.util.Map;

@Mixin(FoodData.class)
public class MixinLimitFoodRegen
{
    private static ServerPlayer STORED_PLAYER;

    @Inject(method = "tick", at = @At("HEAD"))
    private void storePlayer(ServerPlayer player, CallbackInfo ci)
    {   STORED_PLAYER = player;
    }

    @Redirect(method = "tick", at = @At(value = "INVOKE", target = "Lnet/minecraft/world/level/gamerules/GameRules;get(Lnet/minecraft/world/level/gamerules/GameRule;)Ljava/lang/Object;"))
    private Object limitRegenIfFreezing(GameRules gameRules, GameRule<?> key)
    {
        checkFrozenHealth:
        if (key == GameRules.NATURAL_HEALTH_REGENERATION)
        {
            Map<TempEffectType<?>, TempEffect> tempEffects = EntityTempManager.getTemperatureCap(STORED_PLAYER).map(ITemperatureCap::getTempEffects).orElse(new HashMap<>());
            TempEffect freezeHealingEffect = tempEffects.get(ModTempEffects.FREEZE_HEALING.get());
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
        return gameRules.get(key);
    }
}
