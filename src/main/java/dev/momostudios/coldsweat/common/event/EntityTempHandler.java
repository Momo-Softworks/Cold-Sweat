package dev.momostudios.coldsweat.common.event;

import dev.momostudios.coldsweat.ColdSweat;
import dev.momostudios.coldsweat.api.event.common.EnableTemperatureEvent;
import dev.momostudios.coldsweat.api.registry.TempModifierRegistry;
import dev.momostudios.coldsweat.api.temperature.modifier.*;
import dev.momostudios.coldsweat.api.util.Temperature;
import dev.momostudios.coldsweat.api.util.Temperature.Addition.Mode;
import dev.momostudios.coldsweat.api.util.Temperature.Addition.Order;
import dev.momostudios.coldsweat.api.util.Temperature.Addition;
import dev.momostudios.coldsweat.common.capability.EntityTempCap;
import dev.momostudios.coldsweat.common.capability.ITemperatureCap;
import dev.momostudios.coldsweat.common.capability.ModCapabilities;
import dev.momostudios.coldsweat.common.capability.PlayerTempCap;
import dev.momostudios.coldsweat.common.entity.ChameleonEntity;
import dev.momostudios.coldsweat.config.EntitySettingsConfig;
import dev.momostudios.coldsweat.core.event.TaskScheduler;
import dev.momostudios.coldsweat.core.init.BlockInit;
import dev.momostudios.coldsweat.util.compat.CompatManager;
import dev.momostudios.coldsweat.util.config.ConfigSettings;
import dev.momostudios.coldsweat.util.registries.ModEffects;
import dev.momostudios.coldsweat.util.registries.ModItems;
import dev.momostudios.coldsweat.util.world.WorldHelper;
import net.minecraft.core.Direction;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.damagesource.DamageSource;
import net.minecraft.world.effect.MobEffectInstance;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.entity.vehicle.Minecart;
import net.minecraftforge.common.MinecraftForge;
import net.minecraftforge.common.capabilities.Capability;
import net.minecraftforge.common.capabilities.ICapabilityProvider;
import net.minecraftforge.common.capabilities.ICapabilitySerializable;
import net.minecraftforge.common.util.LazyOptional;
import net.minecraftforge.event.AttachCapabilitiesEvent;
import net.minecraftforge.event.TickEvent;
import net.minecraftforge.event.entity.EntityJoinWorldEvent;
import net.minecraftforge.event.entity.living.LivingAttackEvent;
import net.minecraftforge.event.entity.living.LivingEntityUseItemEvent;
import net.minecraftforge.event.entity.living.LivingEvent;
import net.minecraftforge.event.entity.living.PotionEvent;
import net.minecraftforge.event.entity.player.PlayerEvent;
import net.minecraftforge.event.world.SleepFinishedTimeEvent;
import net.minecraftforge.eventbus.api.SubscribeEvent;
import net.minecraftforge.fml.common.Mod;

import javax.annotation.Nonnull;
import javax.annotation.Nullable;
import java.util.List;

@Mod.EventBusSubscriber
public class EntityTempHandler
{
    public static final Temperature.Type[] VALID_TEMPERATURE_TYPES = {Temperature.Type.CORE, Temperature.Type.BASE, Temperature.Type.CEIL, Temperature.Type.FLOOR, Temperature.Type.WORLD};
    public static final Temperature.Type[] VALID_MODIFIER_TYPES    = {Temperature.Type.CORE, Temperature.Type.BASE, Temperature.Type.RATE, Temperature.Type.CEIL, Temperature.Type.FLOOR, Temperature.Type.WORLD};

    /**
     * Attach temperature capability to entities
     */
    @SubscribeEvent
    public static void attachCapabilityToEntityHandler(AttachCapabilitiesEvent<Entity> event)
    {
        if (event.getObject() instanceof LivingEntity entity)
        {
            // Players always get the capability
            if (!(entity instanceof Player))
            {
                EnableTemperatureEvent enableEvent = new EnableTemperatureEvent(entity);
                MinecraftForge.EVENT_BUS.post(enableEvent);
                if (!enableEvent.isEnabled() || enableEvent.isCanceled()) return;
                EnableTemperatureEvent.ENABLED_ENTITIES.add(entity.getType());
            }

            // Make a new capability instance to attach to the entity
            ITemperatureCap tempCap = entity instanceof Player ? new PlayerTempCap() : new EntityTempCap();
            // Optional that holds the capability instance
            LazyOptional<ITemperatureCap> capOptional = LazyOptional.of(() -> tempCap);

            // Capability provider
            ICapabilityProvider provider = new ICapabilitySerializable<CompoundTag>()
            {
                @Nonnull
                @Override
                public <T> LazyOptional<T> getCapability(@Nonnull Capability<T> cap, @Nullable Direction direction)
                {
                    // If the requested cap is the temperature cap, return the temperature cap
                    if (cap == ModCapabilities.PLAYER_TEMPERATURE)
                    {
                        return capOptional.cast();
                    }
                    return LazyOptional.empty();
                }

                @Override
                public CompoundTag serializeNBT()
                {
                    return tempCap.serializeNBT();
                }

                @Override
                public void deserializeNBT(CompoundTag nbt)
                {
                    tempCap.deserializeNBT(nbt);
                }
            };

            // Attach the capability to the entity
            event.addCapability(new ResourceLocation(ColdSweat.MOD_ID, "temperature"), provider);
        }
    }

    /**
     * Tick TempModifiers & update temperature for living entities
     */
    @SubscribeEvent
    public static void onLivingTick(LivingEvent.LivingUpdateEvent event)
    {
        LivingEntity entity = event.getEntityLiving();
        if (!(entity instanceof Player || EnableTemperatureEvent.ENABLED_ENTITIES.contains(entity.getType()))) return;

        Temperature.getTemperatureCap(entity).ifPresent(cap ->
        {
            if (!entity.level.isClientSide)
            {   // Tick modifiers serverside
                cap.tick(entity);
            }
            else
            {   // Tick modifiers clientside
                cap.tickDummy(entity);
            }

            // Remove expired modifiers
            for (Temperature.Type type : VALID_MODIFIER_TYPES)
            {
                cap.getModifiers(type).removeIf(modifier ->
                {
                    int expireTime = modifier.getExpireTime();
                    return (modifier.setTicksExisted(modifier.getTicksExisted() + 1) > expireTime && expireTime != -1);
                });
            }

            if (entity instanceof Player && entity.tickCount % 60 == 0)
            {   Temperature.updateModifiers(entity, cap);
            }
        });
    }

    /**
     * Transfer the player's capability when traveling from the End
     */
    @SubscribeEvent
    public static void returnFromEnd(PlayerEvent.Clone event)
    {
        if (!event.isWasDeath() && !event.getPlayer().level.isClientSide)
        {
            // Get the old player's capability
            Player oldPlayer = event.getOriginal();
            oldPlayer.reviveCaps();

            // Copy the capability to the new player
            event.getPlayer().getCapability(ModCapabilities.PLAYER_TEMPERATURE).ifPresent(cap ->
            {
               oldPlayer.getCapability(ModCapabilities.PLAYER_TEMPERATURE).ifPresent(cap::copy);
            });

            oldPlayer.invalidateCaps();
        }
    }

    /**
     * Enable temperature handling for chameleons
     */
    @SubscribeEvent
    public static void onEnableTemperatureEvent(EnableTemperatureEvent event)
    {
        if (event.getEntity() instanceof ChameleonEntity) event.setEnabled(true);
    }

    /**
     * Add modifiers to the player & valid entities when they join the world
     */
    @SubscribeEvent
    public static void initModifiersOnEntity(EntityJoinWorldEvent event)
    {
        // Add basic TempModifiers to player
        if (event.getEntity() instanceof Player player && !player.level.isClientSide)
        {
            // Sometimes the entity isn't fully initialized, so wait until next tick
            TaskScheduler.scheduleServer(() ->
            {
                // Basic modifiers
                Temperature.addModifiers(player, Temperature.Type.WORLD, List.of(new BiomeTempModifier(25).tickRate(10),
                                                                                 new UndergroundTempModifier().tickRate(10),
                                                                                 new BlockTempModifier(7).tickRate(4)), false);
                // Serene Seasons compat
                if (CompatManager.isSereneSeasonsLoaded())
                    Temperature.addModifier(player, TempModifierRegistry.getEntryFor("sereneseasons:season").tickRate(60), Temperature.Type.WORLD, false,
                                        Addition.of(Mode.BEFORE, Order.FIRST, mod -> mod instanceof UndergroundTempModifier));
                // Weather2 Compat
                if (CompatManager.isWeather2Loaded())
                    Temperature.addModifier(player, TempModifierRegistry.getEntryFor("weather2:storm").tickRate(60), Temperature.Type.WORLD, false,
                                        Addition.of(Mode.BEFORE, Order.FIRST, mod -> mod instanceof UndergroundTempModifier));

                Temperature.set(player, Temperature.Type.WORLD, Temperature.apply(0, player, Temperature.Type.WORLD, Temperature.getModifiers(player, Temperature.Type.WORLD)));
            }, 0);
        }
        // Add basic TempModifiers to chameleons
        else if (event.getEntity() instanceof ChameleonEntity chameleon)
        {
            // Sometimes the entity isn't fully initialized, so wait until next tick
            TaskScheduler.scheduleServer(() ->
            {
                // Basic modifiers
                Temperature.addModifiers(chameleon, Temperature.Type.WORLD, List.of(new BiomeTempModifier(9).tickRate(40),
                                                                                    new UndergroundTempModifier().tickRate(40),
                                                                                    new BlockTempModifier(4).tickRate(20)), false);
                // Serene Seasons compat
                if (CompatManager.isSereneSeasonsLoaded())
                    Temperature.addModifier(chameleon, TempModifierRegistry.getEntryFor("sereneseasons:season").tickRate(60), Temperature.Type.WORLD, false,
                                        Addition.of(Mode.BEFORE, Order.FIRST, mod -> mod instanceof UndergroundTempModifier));
                // Weather2 Compat
                if (CompatManager.isWeather2Loaded())
                    Temperature.addModifier(chameleon, TempModifierRegistry.getEntryFor("weather2:storm").tickRate(60), Temperature.Type.WORLD, false,
                                            Addition.of(Mode.BEFORE, Order.FIRST, mod -> mod instanceof UndergroundTempModifier));

                Temperature.set(chameleon, Temperature.Type.WORLD, Temperature.apply(0, chameleon, Temperature.Type.WORLD, Temperature.getModifiers(chameleon, Temperature.Type.WORLD)));
                chameleon.setTemperature((float) Temperature.get(chameleon, Temperature.Type.WORLD));
            }, 0);
        }
    }

    /**
     * Handle modifiers for freezing, burning, and being wet
     */
    @SubscribeEvent
    public static void onPlayerTick(TickEvent.PlayerTickEvent event)
    {
        Player player = event.player;

        // Water / Rain
        if (!player.level.isClientSide && event.phase == TickEvent.Phase.START)
        {
            if (player.tickCount % 5 == 0)
            {
                if (WorldHelper.isWet(player) || (player.tickCount % 40 == 0 && WorldHelper.isRainingAt(player.level, player.blockPosition())))
                    Temperature.addModifier(player, new WaterTempModifier(0.01f), Temperature.Type.WORLD, false);

                if (player.getTicksFrozen() > 0)
                    Temperature.addOrReplaceModifier(player, new FreezingTempModifier(player.getTicksFrozen() / 13.5f).expires(5), Temperature.Type.BASE);

                if (player.isOnFire())
                    Temperature.addOrReplaceModifier(player, new FireTempModifier().expires(5), Temperature.Type.BASE);
            }

            if (player.getTicksFrozen() > 0)
            {
                TempModifier insulModifier;
                double insulation = 0;
                boolean hasIcePotion = player.hasEffect(ModEffects.ICE_RESISTANCE) && ConfigSettings.ICE_RESISTANCE_ENABLED.get();

                if (!hasIcePotion)
                {   insulModifier = Temperature.getModifier(player, Temperature.Type.RATE, InsulationTempModifier.class);
                    insulation = insulModifier == null ? 0 : insulModifier.getNBT().getDouble("insulation");
                }

                if (!(hasIcePotion || insulation > 0) && (player.tickCount % Math.max(1, 37 - insulation)) == 0)
                {   player.setTicksFrozen(player.getTicksFrozen() - 1);
                }
            }
        }
    }

    /**
     * Cancel freezing damage when the player has the Ice Resistance effect
     */
    @SubscribeEvent
    public static void cancelFreezingDamage(LivingAttackEvent event)
    {
        if (event.getSource() == DamageSource.FREEZE && event.getEntityLiving().hasEffect(ModEffects.ICE_RESISTANCE) && ConfigSettings.ICE_RESISTANCE_ENABLED.get())
        {   event.setCanceled(true);
        }
    }

    /**
     * Handle HearthTempModifier when the player has the Insulation effect
     */
    @SubscribeEvent
    public static void onInsulationUpdate(PotionEvent event)
    {
        if (!event.getEntity().level.isClientSide && event.getEntity() instanceof Player player && event.getPotionEffect() != null
        && event.getPotionEffect().getEffect() == ModEffects.INSULATION)
        {
            // Add TempModifier on potion effect added
            if (event instanceof PotionEvent.PotionAddedEvent)
            {   MobEffectInstance effect = event.getPotionEffect();
                Temperature.addOrReplaceModifier(player, new HearthTempModifier(effect.getAmplifier() + 1).expires(effect.getDuration()), Temperature.Type.WORLD);
            }
            // Remove TempModifier on potion effect removed
            else if (event instanceof PotionEvent.PotionRemoveEvent)
            {   Temperature.removeModifiers(player, Temperature.Type.WORLD, 1, mod -> mod instanceof HearthTempModifier);
            }
        }
    }

    /**
     * Improve the player's temperature when they sleep
     */
    @SubscribeEvent
    public static void onSleep(SleepFinishedTimeEvent event)
    {
        if (!event.getWorld().isClientSide())
        {
            event.getWorld().players().forEach(player ->
            {
                if (player.isSleeping())
                {
                    // Divide the player's current temperature by 4
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

    /**
     * Handle insulation on mounted entity
     */
    @SubscribeEvent
    public static void playerRiding(TickEvent.PlayerTickEvent event)
    {
        if (event.phase == TickEvent.Phase.END)
        {
            Player player = event.player;
            if (player.getVehicle() != null)
            {
                Entity mount = player.getVehicle();
                // If insulated minecart
                if (mount instanceof Minecart minecart && minecart.getDisplayBlockState().getBlock() == BlockInit.MINECART_INSULATION.get())
                {
                    Temperature.addModifier(player, new MountTempModifier(1).expires(1), Temperature.Type.RATE, false);
                }
                // If insulated entity (defined in config)
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

    /**
     * Handle TempModifiers for consumables
     */
    @SubscribeEvent
    public static void onEatFood(LivingEntityUseItemEvent.Finish event)
    {
        if (event.getEntityLiving() instanceof Player player && event.getItem().isEdible() && !event.getEntityLiving().level.isClientSide)
        {
            // If food item defined in config
            float foodTemp = ConfigSettings.TEMPERATURE_FOODS.get().getOrDefault(event.getItem().getItem(), 0d).floatValue();
            if (foodTemp != 0)
            {   Temperature.addModifier(player, new FoodTempModifier(foodTemp).expires(0), Temperature.Type.CORE, true);
            }
            // Soul sprout
            else if (event.getItem().getItem() == ModItems.SOUL_SPROUT)
            {   Temperature.addOrReplaceModifier(player, new SoulSproutTempModifier().expires(900), Temperature.Type.BASE);
            }
        }
    }
}