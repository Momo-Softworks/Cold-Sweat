package com.momosoftworks.coldsweat.common.event;

import com.momosoftworks.coldsweat.api.util.Temperature;
import com.momosoftworks.coldsweat.config.ColdSweatConfig;
import com.momosoftworks.coldsweat.util.compat.CompatManager;
import com.momosoftworks.coldsweat.util.math.CSMath;
import com.momosoftworks.coldsweat.util.registries.ModAttributes;
import com.momosoftworks.coldsweat.util.registries.ModEffects;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ItemStack;
import net.minecraftforge.event.TickEvent;
import net.minecraftforge.event.entity.living.LivingHealEvent;
import net.minecraftforge.event.entity.living.LivingKnockBackEvent;
import net.minecraftforge.event.entity.player.PlayerEvent;
import net.minecraftforge.eventbus.api.SubscribeEvent;
import net.minecraftforge.fml.common.Mod;

import java.util.Collection;

@Mod.EventBusSubscriber
public class TempEffectsCommon
{
    @SubscribeEvent
    public static void onPlayerMine(PlayerEvent.BreakSpeed event)
    {
        Player player = event.getEntity();
        if (!ColdSweatConfig.getInstance().coldMining() || player.hasEffect(ModEffects.ICE_RESISTANCE) || player.hasEffect(ModEffects.GRACE)) return;

        // Get the player's temperature
        float temp = (float) Temperature.get(player, Temperature.Trait.BODY);

        // If the player is too cold, slow down their mining speed
        if (temp < -50)
        {
            float minMiningSpeed = CSMath.blend(0.25f, 1f, getTempResistance(player, true), 0, 4);
            // Get protection from armor underwear
            event.setNewSpeed(event.getNewSpeed() * CSMath.blend(minMiningSpeed, 1f, temp, -100, -50));
        }
    }

    // Decrease the player's movement speed if their temperature is below -50
    @SubscribeEvent
    public static void onPlayerTick(TickEvent.PlayerTickEvent event)
    {
        Player player = event.player;
        if (event.phase == TickEvent.Phase.END)
        {
            float temp = (float) Temperature.get(player, Temperature.Trait.BODY);
            if (temp < -50)
            {
                if (!ColdSweatConfig.getInstance().coldMovement()
                || player.hasEffect(ModEffects.ICE_RESISTANCE)
                || player.hasEffect(ModEffects.GRACE)) return;

                // If not elytra flying
                if (!player.isFallFlying())
                {
                    // Get protection from armor underwear
                    float minMoveMultiplier = CSMath.blend(player.isOnGround() ? 0.5f : 0.8f, 1, getTempResistance(player, true), 0, 4);
                    if (minMoveMultiplier != 1)
                    {
                        float moveSpeed = CSMath.blend(minMoveMultiplier, 1, temp, -100, -50);
                        player.setDeltaMovement(player.getDeltaMovement().multiply(moveSpeed, 1, moveSpeed));
                    }
                }
            }
        }
    }

    @SubscribeEvent
    public static void onPlayerKnockback(LivingKnockBackEvent event)
    {
        if (event.getEntity().getLastHurtByMob() instanceof Player player)
        {
            if (!ColdSweatConfig.getInstance().coldKnockback() || player.hasEffect(ModEffects.ICE_RESISTANCE) || player.hasEffect(ModEffects.GRACE)) return;

            float temp = (float) Temperature.get(player, Temperature.Trait.BODY);
            if (temp < -50)
            {
                // Get protection from armor underwear
                float liningProtFactor = CSMath.blend(0.5f, 1, getTempResistance(player, true), 0, 4);
                if (liningProtFactor != 1)
                {   event.setStrength(event.getStrength() * CSMath.blend(liningProtFactor, 1, temp, -100, -50));
                }
            }
        }
    }

    // Prevent healing as temp decreases
    @SubscribeEvent
    public static void onHeal(LivingHealEvent event)
    {
        if (event.getEntity() instanceof Player player)
        {
            if (!ColdSweatConfig.getInstance().freezingHearts() || player.hasEffect(ModEffects.ICE_RESISTANCE) || player.hasEffect(ModEffects.GRACE)) return;

            float healing = event.getAmount();
            float temp = (float) Temperature.get(player, Temperature.Trait.BODY);
            if (temp < -50)
            {
                // Get protection from armor underwear
                float minFrozenHealth = CSMath.blend(0.5f, 1, getTempResistance(player, true), 0, 4);
                if (minFrozenHealth != 1)
                {   event.setAmount(CSMath.clamp(healing, 0, CSMath.ceil(player.getMaxHealth() * CSMath.blend(minFrozenHealth, 1, temp, -100, -50)) - player.getHealth()));
                }
            }
        }
    }

    public static int getTempResistance(Player player, boolean cold)
    {
        int strength = 0;
        if (CompatManager.isArmorUnderwearLoaded())
        {
            strength += ((Collection<ItemStack>) player.getArmorSlots()).stream()
                    .map(stack -> cold ? CompatManager.hasOttoLiner(stack) : CompatManager.hasOllieLiner(stack))
                    .filter(Boolean::booleanValue)
                    .mapToInt(i -> 1).sum();
        }
        strength += CSMath.blend(0, 4, CSMath.getIfNotNull(cold ? player.getAttribute(ModAttributes.COLD_RESISTANCE)
                                                                : player.getAttribute(ModAttributes.HEAT_RESISTANCE), att -> att.getValue(), 0).floatValue(), 0, 1);
        return CSMath.clamp(strength, 0, 4);
    }
}
