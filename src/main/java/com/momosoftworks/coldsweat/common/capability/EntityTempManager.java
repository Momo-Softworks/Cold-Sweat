package com.momosoftworks.coldsweat.common.capability;

import com.google.common.collect.ImmutableSet;
import com.momosoftworks.coldsweat.ColdSweat;
import com.momosoftworks.coldsweat.api.event.common.EnableTemperatureEvent;
import com.momosoftworks.coldsweat.api.registry.TempModifierRegistry;
import com.momosoftworks.coldsweat.api.temperature.modifier.*;
import com.momosoftworks.coldsweat.api.util.Temperature;
import com.momosoftworks.coldsweat.api.util.Temperature.Addition.Mode;
import com.momosoftworks.coldsweat.api.util.Temperature.Addition.Order;
import com.momosoftworks.coldsweat.api.util.Temperature.Addition;
import com.momosoftworks.coldsweat.common.entity.ChameleonEntity;
import com.momosoftworks.coldsweat.config.EntitySettingsConfig;
import com.momosoftworks.coldsweat.util.compat.CompatManager;
import com.momosoftworks.coldsweat.config.ConfigSettings;
import com.momosoftworks.coldsweat.util.registries.ModBlocks;
import com.momosoftworks.coldsweat.util.registries.ModEffects;
import com.momosoftworks.coldsweat.util.registries.ModItems;
import com.momosoftworks.coldsweat.util.world.WorldHelper;
import net.minecraft.entity.Entity;
import net.minecraft.entity.EntityType;
import net.minecraft.entity.LivingEntity;
import net.minecraft.entity.item.minecart.MinecartEntity;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.entity.player.ServerPlayerEntity;
import net.minecraft.inventory.container.Container;
import net.minecraft.inventory.container.CraftingResultSlot;
import net.minecraft.inventory.container.IContainerListener;
import net.minecraft.inventory.container.Slot;
import net.minecraft.item.ItemStack;
import net.minecraft.item.UseAction;
import net.minecraft.nbt.CompoundNBT;
import net.minecraft.potion.EffectInstance;
import net.minecraft.util.Direction;
import net.minecraft.util.NonNullList;
import net.minecraft.util.ResourceLocation;
import net.minecraftforge.common.MinecraftForge;
import net.minecraftforge.common.capabilities.Capability;
import net.minecraftforge.common.capabilities.ICapabilityProvider;
import net.minecraftforge.common.capabilities.ICapabilitySerializable;
import net.minecraftforge.common.util.LazyOptional;
import net.minecraftforge.event.AttachCapabilitiesEvent;
import net.minecraftforge.event.TickEvent;
import net.minecraftforge.event.entity.EntityJoinWorldEvent;
import net.minecraftforge.event.entity.living.LivingEntityUseItemEvent;
import net.minecraftforge.event.entity.living.LivingEvent;
import net.minecraftforge.event.entity.living.PotionEvent;
import net.minecraftforge.event.entity.player.PlayerEvent;
import net.minecraftforge.event.world.SleepFinishedTimeEvent;
import net.minecraftforge.eventbus.api.SubscribeEvent;
import net.minecraftforge.fml.common.Mod;
import net.minecraftforge.registries.ForgeRegistries;

import javax.annotation.Nonnull;
import javax.annotation.Nullable;
import java.util.Arrays;
import java.util.*;

@Mod.EventBusSubscriber
public class EntityTempManager
{
    public static final Temperature.Type[] VALID_TEMPERATURE_TYPES = {Temperature.Type.CORE, Temperature.Type.BASE, Temperature.Type.FREEZING_POINT, Temperature.Type.BURNING_POINT, Temperature.Type.WORLD};
    public static final Temperature.Type[] VALID_MODIFIER_TYPES    = {Temperature.Type.CORE, Temperature.Type.BASE, Temperature.Type.RATE, Temperature.Type.FREEZING_POINT, Temperature.Type.BURNING_POINT, Temperature.Type.WORLD};
    private static final Set<EntityType<?>> TEMPERATURE_ENABLED_ENTITIES = new HashSet<>();

    public static final Map<Entity, LazyOptional<ITemperatureCap>> SERVER_CAP_CACHE = new HashMap<>();
    public static final Map<Entity, LazyOptional<ITemperatureCap>> CLIENT_CAP_CACHE = new HashMap<>();

    /**
     * Attach temperature capability to entities
     */
    @SubscribeEvent
    public static void attachCapabilityToEntityHandler(AttachCapabilitiesEvent<Entity> event)
    {
        if (event.getObject() instanceof LivingEntity)
        {
            LivingEntity entity = (LivingEntity) event.getObject();
            // Players always get the capability
            if (!(entity instanceof PlayerEntity))
            {
                EnableTemperatureEvent enableEvent = new EnableTemperatureEvent(entity);
                MinecraftForge.EVENT_BUS.post(enableEvent);
                if (!enableEvent.isEnabled() || enableEvent.isCanceled()) return;
                TEMPERATURE_ENABLED_ENTITIES.add(entity.getType());
            }

            // Make a new capability instance to attach to the entity
            ITemperatureCap tempCap = entity instanceof PlayerEntity ? new PlayerTempCap() : new EntityTempCap();
            // Optional that holds the capability instance
            LazyOptional<ITemperatureCap> capOptional = LazyOptional.of(() -> tempCap);

            // Capability provider
            ICapabilityProvider provider = new ICapabilitySerializable<CompoundNBT>()
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
                public CompoundNBT serializeNBT()
                {
                    return tempCap.serializeNBT();
                }

                @Override
                public void deserializeNBT(CompoundNBT nbt)
                {
                    tempCap.deserializeNBT(nbt);
                }
            };

            // Attach the capability to the entity
            event.addCapability(new ResourceLocation(ColdSweat.MOD_ID, "temperature"), provider);
        }
    }

    public static LazyOptional<ITemperatureCap> getTemperatureCap(Entity entity)
    {
        Map<Entity, LazyOptional<ITemperatureCap>> cache = entity.level.isClientSide ? CLIENT_CAP_CACHE : SERVER_CAP_CACHE;
        return cache.computeIfAbsent(entity, e ->
        {   LazyOptional<ITemperatureCap> cap = e.getCapability(entity instanceof PlayerEntity ? ModCapabilities.PLAYER_TEMPERATURE : ModCapabilities.ENTITY_TEMPERATURE);
            cap.addListener((opt) -> cache.remove(e));
            return cap;
        });
    }

    /**
     * Tick TempModifiers & update temperature for living entities
     */
    @SubscribeEvent
    public static void onLivingTick(LivingEvent.LivingUpdateEvent event)
    {
        LivingEntity entity = event.getEntityLiving();
        if (!(entity instanceof PlayerEntity || getEntitiesWithTemperature().contains(entity.getType()))) return;

        getTemperatureCap(entity).ifPresent(cap ->
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

            if (entity instanceof PlayerEntity && entity.tickCount % 60 == 0)
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
            PlayerEntity oldPlayer = event.getOriginal();

            // Copy the capability to the new player
            getTemperatureCap(event.getPlayer()).ifPresent(cap ->
            {
                getTemperatureCap(oldPlayer).ifPresent(cap::copy);
            });
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
        if (event.getEntity() instanceof ServerPlayerEntity && !event.getEntity().level.isClientSide)
        {
            ServerPlayerEntity player = (ServerPlayerEntity) event.getEntity();
            // Sometimes the entity isn't fully initialized, so wait until next tick
            if (player.getServer() != null)
            player.getServer().execute(() ->
            {
                // Add modifiers separately to ensure order
                Temperature.addModifier(player, new BiomeTempModifier(25).tickRate(10), Temperature.Type.WORLD, false, Addition.AT_START);
                Temperature.addModifier(player, new DepthTempModifier().tickRate(10), Temperature.Type.WORLD, false, Addition.of(Mode.AFTER, Order.FIRST, mod -> mod instanceof BiomeTempModifier));
                Temperature.addModifier(player, new BlockTempModifier().tickRate(4), Temperature.Type.WORLD, false, Addition.of(Mode.AFTER, Order.FIRST, mod -> mod instanceof DepthTempModifier));

                // Serene Seasons compat
                if (CompatManager.isSereneSeasonsLoaded())
                {
                    TempModifierRegistry.getEntryFor("sereneseasons:season").ifPresent(mod -> Temperature.addModifier(player, mod.tickRate(60), Temperature.Type.WORLD, false,
                                            Addition.of(Mode.BEFORE, Order.FIRST, mod2 -> mod2 instanceof DepthTempModifier)));
                }
                // Weather2 Compat
                if (CompatManager.isWeather2Loaded())
                {
                    TempModifierRegistry.getEntryFor("weather2:storm").ifPresent(mod -> Temperature.addModifier(player, mod.tickRate(60), Temperature.Type.WORLD, false,
                                            Addition.of(Mode.BEFORE, Order.FIRST, mod2 -> mod2 instanceof DepthTempModifier)));
                }

                // Armor underwear compat
                if (CompatManager.isArmorUnderwearLoaded())
                {
                    TempModifierRegistry.getEntryFor("armorunder:lining").ifPresent(armorUnderMod ->
                    {   Temperature.addModifier(player, armorUnderMod.tickRate(20), Temperature.Type.FREEZING_POINT, false);
                        Temperature.addModifier(player, armorUnderMod.tickRate(20), Temperature.Type.BURNING_POINT, false);
                    });
                }

                Temperature.set(player, Temperature.Type.WORLD, Temperature.apply(0, player, Temperature.Type.WORLD, Temperature.getModifiers(player, Temperature.Type.WORLD)));
            });

            // Add listener for granting the sewing table recipe when the player gets an insulation item
            player.containerMenu.addSlotListener(new IContainerListener()
            {
                public void slotChanged(Container menu, int slotIndex, ItemStack stack)
                {   Slot slot = menu.getSlot(slotIndex);
                    if (!(slot instanceof CraftingResultSlot))
                    {
                        if (slot.container == player.inventory
                        && (ConfigSettings.INSULATION_ITEMS.get().containsKey(stack.getItem())
                        || ConfigSettings.ADAPTIVE_INSULATION_ITEMS.get().containsKey(stack.getItem())))
                        {   player.awardRecipesByKey(new ResourceLocation[]{new ResourceLocation(ColdSweat.MOD_ID, "sewing_table")});
                        }
                    }
                }
                public void setContainerData(Container p_143462_, int p_143463_, int p_143464_) {}
                public void refreshContainer(Container var1, NonNullList<ItemStack> var2) {}
            });
        }
        // Add basic TempModifiers to chameleons
        else if (event.getEntity() instanceof LivingEntity && getEntitiesWithTemperature().contains(event.getEntity().getType()))
        {
            LivingEntity entity = (LivingEntity) event.getEntity();
            // Sometimes the entity isn't fully initialized, so wait until next tick
            if (entity.getServer() != null)
            entity.getServer().execute(() ->
            {
                // Basic modifiers
                Temperature.addModifiers(entity, Arrays.asList(new BiomeTempModifier(9).tickRate(40),
                                                               new DepthTempModifier().tickRate(40),
                                                               new BlockTempModifier(4).tickRate(20)), Temperature.Type.WORLD, false);
                // Serene Seasons compat
                if (CompatManager.isSereneSeasonsLoaded())
                {
                    TempModifierRegistry.getEntryFor("sereneseasons:season").ifPresent(mod -> Temperature.addModifier(entity, mod.tickRate(60), Temperature.Type.WORLD, false,
                                                                                                                      Addition.of(Mode.BEFORE, Order.FIRST, mod2 -> mod2 instanceof DepthTempModifier)));
                }
                // Weather2 Compat
                if (CompatManager.isWeather2Loaded())
                {
                    TempModifierRegistry.getEntryFor("weather2:storm").ifPresent(mod -> Temperature.addModifier(entity, mod.tickRate(60), Temperature.Type.WORLD, false,
                                                                                                                Addition.of(Mode.BEFORE, Order.FIRST, mod2 -> mod2 instanceof DepthTempModifier)));
                }

                Temperature.set(entity, Temperature.Type.WORLD, Temperature.apply(0, entity, Temperature.Type.WORLD, Temperature.getModifiers(entity, Temperature.Type.WORLD)));
            });
        }
    }

    /**
     * Handle modifiers for freezing, burning, and being wet
     */
    @SubscribeEvent
    public static void onPlayerTick(TickEvent.PlayerTickEvent event)
    {
        PlayerEntity player = event.player;

        // Water / Rain
        if (!player.level.isClientSide && event.phase == TickEvent.Phase.START)
        {
            if (player.tickCount % 5 == 0)
            {
                if (WorldHelper.isInWater(player) || player.tickCount % 40 == 0 && WorldHelper.isRainingAt(player.level, player.blockPosition()))
                    Temperature.addModifier(player, new WaterTempModifier(0.01f).tickRate(5), Temperature.Type.WORLD, false);

                if (player.isOnFire())
                    Temperature.addOrReplaceModifier(player, new FireTempModifier().expires(5), Temperature.Type.BASE);
            }
        }
    }

    /**
     * Handle HearthTempModifier when the player has the Insulation effect
     */
    @SubscribeEvent
    public static void onInsulationUpdate(PotionEvent event)
    {
        if (!event.getEntity().level.isClientSide && event.getEntity() instanceof PlayerEntity && event.getPotionEffect() != null
        && event.getPotionEffect().getEffect() == ModEffects.INSULATION)
        {
            PlayerEntity player = (PlayerEntity) event.getEntityLiving();
            // Add TempModifier on potion effect added
            if (event instanceof PotionEvent.PotionAddedEvent)
            {   EffectInstance effect = event.getPotionEffect();
                // New HearthTempModifier
                TempModifier newMod = new HearthTempModifier(effect.getAmplifier() + 1).expires(effect.getDuration());
                Temperature.addOrReplaceModifier(player, newMod, Temperature.Type.WORLD);
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
                    getTemperatureCap(player).ifPresent(cap ->
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
        if (event.phase == TickEvent.Phase.END && !event.player.level.isClientSide() && event.player.tickCount % 5 == 0)
        {
            PlayerEntity player = event.player;
            if (player.getVehicle() != null)
            {
                Entity mount = player.getVehicle();
                // If insulated minecart
                if (mount instanceof MinecartEntity && ((MinecartEntity) mount).getDisplayBlockState().getBlock() == ModBlocks.MINECART_INSULATION)
                {   Temperature.addModifier(player, new MountTempModifier(20, 20).expires(1), Temperature.Type.RATE, false);
                }
                // If insulated entity (defined in config)
                else
                {
                    EntitySettingsConfig.getInstance().getInsulatedEntities().stream().filter(entry ->
                    entry.get(0).equals(ForgeRegistries.ENTITIES.getKey(mount.getType()).toString())).findFirst()
                    .ifPresent(entry ->
                    {   int warming = ((Number) entry.get(1)).intValue();
                        int cooling = entry.size() < 3
                                    ? warming
                                    : ((Number) entry.get(2)).intValue();
                        Temperature.addModifier(player, new MountTempModifier(warming, cooling).expires(5), Temperature.Type.RATE, false);
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
        if (event.getEntity() instanceof PlayerEntity
        && (event.getItem().getUseAnimation() == UseAction.DRINK || event.getItem().getUseAnimation() == UseAction.EAT)
        && !event.getEntity().level.isClientSide)
        {
            PlayerEntity player = (PlayerEntity) event.getEntity();
            // If food item defined in config
            float foodTemp = ConfigSettings.FOOD_TEMPERATURES.get().getOrDefault(event.getItem().getItem(), 0d).floatValue();
            if (foodTemp != 0)
            {   Temperature.addModifier(player, new FoodTempModifier(foodTemp).expires(0), Temperature.Type.CORE, true);
            }
            // Soul sprout
            else if (event.getItem().getItem() == ModItems.SOUL_SPROUT)
            {   Temperature.addOrReplaceModifier(player, new SoulSproutTempModifier().expires(900), Temperature.Type.BASE);
            }
        }
    }

    public static Set<EntityType<?>> getEntitiesWithTemperature()
    {   return ImmutableSet.copyOf(TEMPERATURE_ENABLED_ENTITIES);
    }
}