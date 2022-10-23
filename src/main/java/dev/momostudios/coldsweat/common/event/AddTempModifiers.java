package dev.momostudios.coldsweat.common.event;

import dev.momostudios.coldsweat.api.registry.TempModifierRegistry;
import dev.momostudios.coldsweat.api.temperature.Temperature;
import dev.momostudios.coldsweat.api.temperature.modifier.*;
import dev.momostudios.coldsweat.api.util.TempHelper;
import dev.momostudios.coldsweat.common.capability.ModCapabilities;
import dev.momostudios.coldsweat.config.EntitySettingsConfig;
import dev.momostudios.coldsweat.core.event.TaskScheduler;
import dev.momostudios.coldsweat.core.init.BlockInit;
import dev.momostudios.coldsweat.util.config.ConfigSettings;
import dev.momostudios.coldsweat.util.math.CSMath;
import dev.momostudios.coldsweat.util.registries.ModEffects;
import dev.momostudios.coldsweat.util.world.WorldHelper;
import net.minecraft.core.BlockPos;
import net.minecraft.world.effect.MobEffectInstance;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.entity.vehicle.Minecart;
import net.minecraftforge.event.TickEvent;
import net.minecraftforge.event.entity.EntityJoinWorldEvent;
import net.minecraftforge.event.entity.living.LivingEntityUseItemEvent;
import net.minecraftforge.event.entity.living.PotionEvent;
import net.minecraftforge.event.world.SleepFinishedTimeEvent;
import net.minecraftforge.eventbus.api.SubscribeEvent;
import net.minecraftforge.fml.ModList;
import net.minecraftforge.fml.common.Mod;

@Mod.EventBusSubscriber
public class AddTempModifiers
{
    @SubscribeEvent
    public static void onPlayerCreated(EntityJoinWorldEvent event)
    {
        if (event.getEntity() instanceof Player player && !player.level.isClientSide)
        {
            TaskScheduler.scheduleServer(() ->
            {
                TempHelper.addModifier(player, new BiomeTempModifier().tickRate(10),  Temperature.Type.WORLD, false);
                if (ModList.get().isLoaded("sereneseasons"))
                    TempHelper.addModifier(player, TempModifierRegistry.getEntryFor("sereneseasons:season").tickRate(20), Temperature.Type.WORLD, false);
                TempHelper.addModifier(player, new DepthTempModifier().tickRate(10), Temperature.Type.WORLD, false);
                TempHelper.addModifier(player, new BlockTempModifier().tickRate(5),  Temperature.Type.WORLD, false);
            }, 10);
        }
    }

    @SubscribeEvent
    public static void onPlayerTick(TickEvent.PlayerTickEvent event)
    {
        Player player = event.player;

        // Water / Rain
        if (!player.level.isClientSide && player.tickCount % 5 == 0 && player.isInWaterRainOrBubble())
        {
            TempHelper.addModifier(player, new WaterTempModifier(0.01f), Temperature.Type.WORLD, false);
        }

        // Powder snow
        if (!player.level.isClientSide && player.tickCount % 5 == 0 && player.getTicksFrozen() > 0)
        {
            TempHelper.replaceModifier(player, new FreezingTempModifier(player.getTicksFrozen() / 13.5f).expires(5), Temperature.Type.BASE);
        }
    }

    @SubscribeEvent
    public static void onInsulationUpdate(PotionEvent event)
    {
        if (!event.getEntity().level.isClientSide && event.getEntity() instanceof Player player && event.getPotionEffect() != null
        && event.getPotionEffect().getEffect() == ModEffects.INSULATION)
        {
            if (event instanceof PotionEvent.PotionAddedEvent)
            {
                MobEffectInstance effect = event.getPotionEffect();
                TempHelper.replaceModifier(player, new HearthTempModifier(effect.getAmplifier() + 1).expires(effect.getDuration()), Temperature.Type.WORLD);
            }
            else if (event instanceof PotionEvent.PotionRemoveEvent)
            {
                TempHelper.removeModifiers(player, Temperature.Type.WORLD, 1, mod -> mod instanceof HearthTempModifier);
            }
        }
    }

    @SubscribeEvent
    public static void removeInsulation(TickEvent.PlayerTickEvent event)
    {
        Player player = event.player;
        // Removes the Insulated effect if the player has skylight access
        if (event.phase == TickEvent.Phase.END && !player.level.isClientSide && player.tickCount % 20 == 0
        && player.hasEffect(ModEffects.INSULATION) && WorldHelper.canSeeSky(player.level, new BlockPos(player.getX(), CSMath.ceil(player.getY()), player.getZ()), 64))
        {
            player.removeEffect(ModEffects.INSULATION);
        }
    }

    @SubscribeEvent
    public static void onSleep(SleepFinishedTimeEvent event)
    {
        if (!event.getWorld().isClientSide())
        {
            event.getWorld().players().forEach(player ->
            {
                if (player.isSleeping())
                {
                    player.getCapability(ModCapabilities.PLAYER_TEMPERATURE).ifPresent(cap ->
                    {
                        double temp = cap.getTemp(Temperature.Type.CORE);
                        cap.setTemp(Temperature.Type.CORE, temp / 4f);
                        TempHelper.updateTemperature(player, cap, true);
                    });
                }
            });
        }
    }

    @SubscribeEvent
    public static void playerRiding(TickEvent.PlayerTickEvent event)
    {
        if (event.phase == TickEvent.Phase.END)
        {
            Player player = event.player;
            if (player.getVehicle() != null)
            {
                Entity mount = player.getVehicle();
                if (mount instanceof Minecart minecart && minecart.getDisplayBlockState().getBlock() == BlockInit.MINECART_INSULATION.get())
                {
                    TempHelper.addModifier(player, new MountTempModifier(1).expires(1), Temperature.Type.RATE, false);
                }
                else
                {
                    EntitySettingsConfig.INSTANCE.insulatedEntities().stream().filter(entityID -> entityID.get(0).equals(mount.getType().getRegistryName().toString())).findFirst().ifPresent(entityID ->
                    {
                        float insulationAmount = ((Number) entityID.get(1)).floatValue();
                        TempHelper.addModifier(player, new MountTempModifier(insulationAmount).expires(1), Temperature.Type.RATE, false);
                    });
                }
            }
        }
    }

    @SubscribeEvent
    public static void onEatFood(LivingEntityUseItemEvent.Finish event)
    {
        if (event.getEntityLiving() instanceof Player player && event.getItem().isEdible() && !event.getEntityLiving().level.isClientSide)
        {
            float foodTemp = ConfigSettings.VALID_FOODS.get().getOrDefault(event.getItem().getItem(), 0d).floatValue();
            if (foodTemp != 0)
            {
                TempHelper.addModifier(player, new FoodTempModifier(foodTemp).expires(1), Temperature.Type.CORE, true);
            }
        }
    }
}