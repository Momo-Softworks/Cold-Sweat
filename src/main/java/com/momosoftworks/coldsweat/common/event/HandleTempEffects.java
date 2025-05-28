package com.momosoftworks.coldsweat.common.event;

import com.momosoftworks.coldsweat.api.util.Temperature;
import com.momosoftworks.coldsweat.common.capability.handler.EntityTempManager;
import com.momosoftworks.coldsweat.common.capability.temperature.ITemperatureCap;
import com.momosoftworks.coldsweat.config.ConfigSettings;
import com.momosoftworks.coldsweat.core.event.TaskScheduler;
import com.momosoftworks.coldsweat.core.init.ModEffects;
import com.momosoftworks.coldsweat.data.codec.configuration.TempEffectsData;
import com.momosoftworks.coldsweat.util.serialization.ConfigHelper;
import glitchcore.event.TickEvent;
import net.minecraft.client.Minecraft;
import net.minecraft.world.effect.MobEffects;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.player.Player;
import net.neoforged.api.distmarker.Dist;
import net.neoforged.bus.api.EventPriority;
import net.neoforged.bus.api.SubscribeEvent;
import net.neoforged.fml.common.EventBusSubscriber;
import net.neoforged.neoforge.client.event.ClientTickEvent;
import net.neoforged.neoforge.event.entity.EntityJoinLevelEvent;
import net.neoforged.neoforge.event.entity.EntityLeaveLevelEvent;

@EventBusSubscriber
public class HandleTempEffects
{
    @SubscribeEvent(priority = EventPriority.LOWEST)
    public static void addTempEffects(EntityJoinLevelEvent event)
    {
        TaskScheduler.schedule(() ->
        {
            if (event.getEntity() instanceof LivingEntity living)
            {
                TempEffectsData effectsData = ConfigHelper.getFirstOrNull(ConfigSettings.ENTITY_TEMP_EFFECTS, living.getType(), data -> data.test(living));
                if (effectsData == null) return;
                EntityTempManager.getTemperatureCap(living).ifPresent(cap ->
                {
                    effectsData.effects().forEach(holder ->
                    {   cap.addTempEffect(holder.effect().create(living, holder.range()));
                    });
                });
            }
        }, 1);
    }

    @SubscribeEvent
    public static void clearTempEffects(EntityLeaveLevelEvent event)
    {
        if (event.getEntity() instanceof LivingEntity living)
        {   EntityTempManager.getTemperatureCap(living).ifPresent(ITemperatureCap::clearTempEffects);
        }
    }

    @EventBusSubscriber(Dist.CLIENT)
    public static class Client
    {
        public static double COLD_IMMUNITY = 0;
        public static double HOT_IMMUNITY  = 0;

        @SubscribeEvent
        public static void onClientTick(ClientTickEvent.Post event)
        {
            Player player = Minecraft.getInstance().player;
            if (isPlayerImmune(player)) return;
            if (player.tickCount % 5 == 0)
            {
                // Set cold immunity
                if (player.hasEffect(ModEffects.ICE_RESISTANCE) && ConfigSettings.ICE_RESISTANCE_ENABLED.get())
                {   COLD_IMMUNITY = 1;
                }
                else COLD_IMMUNITY = Temperature.get(player, Temperature.Trait.COLD_RESISTANCE);
                // Set heat immunity
                if (player.hasEffect(MobEffects.FIRE_RESISTANCE) && ConfigSettings.FIRE_RESISTANCE_ENABLED.get())
                {   HOT_IMMUNITY = 1;
                }
                else HOT_IMMUNITY  = Temperature.get(player, Temperature.Trait.HEAT_RESISTANCE);
            }
        }
    }


    public static boolean isPlayerImmune(Player player)
    {   return player == null || !player.isAlive() || EntityTempManager.isPeacefulMode(player) || player.hasEffect(ModEffects.GRACE);
    }
}
