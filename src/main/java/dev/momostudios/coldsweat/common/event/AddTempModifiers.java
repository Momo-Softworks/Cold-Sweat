package dev.momostudios.coldsweat.common.event;

import dev.momostudios.coldsweat.api.registry.TempModifierRegistry;
import dev.momostudios.coldsweat.api.temperature.modifier.*;
import dev.momostudios.coldsweat.api.util.Temperature;
import dev.momostudios.coldsweat.common.capability.ModCapabilities;
import dev.momostudios.coldsweat.common.entity.ChameleonEntity;
import dev.momostudios.coldsweat.config.EntitySettingsConfig;
import dev.momostudios.coldsweat.core.event.TaskScheduler;
import dev.momostudios.coldsweat.core.init.BlockInit;
import dev.momostudios.coldsweat.util.compat.CompatManager;
import dev.momostudios.coldsweat.util.config.ConfigSettings;
import dev.momostudios.coldsweat.util.math.ListBuilder;
import dev.momostudios.coldsweat.util.registries.ModEffects;
import dev.momostudios.coldsweat.util.registries.ModItems;
import dev.momostudios.coldsweat.util.world.WorldHelper;
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
import net.minecraftforge.fml.common.Mod;

@Mod.EventBusSubscriber
public class AddTempModifiers
{
    @SubscribeEvent
    public static void onEntityCreated(EntityJoinWorldEvent event)
    {
        if (event.getEntity() instanceof Player player && !player.level.isClientSide)
        {
            TaskScheduler.scheduleServer(() ->
            {
                Temperature.addModifiersSimple(player, Temperature.Type.WORLD, ListBuilder.begin(new BiomeTempModifier(25).tickRate(10))
                                                                    .addIf(CompatManager.isSereneSeasonsLoaded(),
                                                                        () -> TempModifierRegistry.getEntryFor("sereneseasons:season").tickRate(40))
                                                                    .add(new DepthTempModifier().tickRate(15),
                                                                        new BlockTempModifier(7).tickRate(4)).build(), false);

                Temperature.set(player, Temperature.Type.WORLD, Temperature.apply(0, player, Temperature.Type.WORLD, Temperature.getModifiers(player, Temperature.Type.WORLD)));
            }, 1);
        }
        else if (event.getEntity() instanceof ChameleonEntity chameleon)
        {
            TaskScheduler.scheduleServer(() ->
            {
                Temperature.addModifiersSimple(chameleon, Temperature.Type.WORLD, ListBuilder.begin(new BiomeTempModifier(9).tickRate(40))
                                                                                             .addIf(CompatManager.isSereneSeasonsLoaded(),
                                                                                                 () -> TempModifierRegistry.getEntryFor("sereneseasons:season").tickRate(60))
                                                                                             .add(new DepthTempModifier().tickRate(40),
                                                                                                 new BlockTempModifier(4).tickRate(20)).build(), false);
                Temperature.set(chameleon, Temperature.Type.WORLD, Temperature.apply(0, chameleon, Temperature.Type.WORLD, Temperature.getModifiers(chameleon, Temperature.Type.WORLD)));
                chameleon.setTemperature((float) Temperature.get(chameleon, Temperature.Type.WORLD));
            }, 1);
        }
    }

    @SubscribeEvent
    public static void onPlayerTick(TickEvent.PlayerTickEvent event)
    {
        Player player = event.player;

        // Water / Rain
        if (!player.level.isClientSide && player.tickCount % 5 == 0 && event.phase == TickEvent.Phase.START)
        {
            if (WorldHelper.isWet(player))
                Temperature.addModifier(player, new WaterTempModifier(0.01f), Temperature.Type.WORLD, false);

            if (player.getTicksFrozen() > 0)
                Temperature.addOrReplaceModifier(player, new FreezingTempModifier(player.getTicksFrozen() / 13.5f).expires(5), Temperature.Type.BASE);

            if (player.isOnFire())
                Temperature.addOrReplaceModifier(player, new FireTempModifier().expires(5), Temperature.Type.BASE);
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
                Temperature.addOrReplaceModifier(player, new HearthTempModifier(effect.getAmplifier() + 1).expires(effect.getDuration()), Temperature.Type.WORLD);
            }
            else if (event instanceof PotionEvent.PotionRemoveEvent)
            {
                Temperature.removeModifiers(player, Temperature.Type.WORLD, 1, mod -> mod instanceof HearthTempModifier);
            }
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
                        Temperature.updateTemperature(player, cap, true);
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
                    Temperature.addModifier(player, new MountTempModifier(1).expires(1), Temperature.Type.RATE, false);
                }
                else
                {
                    EntitySettingsConfig.getInstance().getInsulatedEntities().stream().filter(entityID -> entityID.get(0).equals(mount.getType().getRegistryName().toString())).findFirst().ifPresent(entityID ->
                    {
                        float insulationAmount = ((Number) entityID.get(1)).floatValue();
                        Temperature.addModifier(player, new MountTempModifier(insulationAmount).expires(1), Temperature.Type.RATE, false);
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
            float foodTemp = ConfigSettings.TEMPERATURE_FOODS.get().getOrDefault(event.getItem().getItem(), 0d).floatValue();
            if (foodTemp != 0)
            {
                Temperature.addModifier(player, new FoodTempModifier(foodTemp).expires(0), Temperature.Type.CORE, true);
            }

            if (event.getItem().getItem() == ModItems.SOUL_SPROUT)
            {
                Temperature.addOrReplaceModifier(player, new SoulSproutTempModifier().expires(900), Temperature.Type.BASE);
            }
        }
    }
}
