package com.momosoftworks.coldsweat.common.event;

import com.google.common.collect.ImmutableSet;
import com.mojang.datafixers.util.Either;
import com.mojang.datafixers.util.Pair;
import com.momosoftworks.coldsweat.ColdSweat;
import com.momosoftworks.coldsweat.api.event.common.GatherDefaultTempModifiersEvent;
import com.momosoftworks.coldsweat.api.registry.TempModifierRegistry;
import com.momosoftworks.coldsweat.api.temperature.modifier.*;
import com.momosoftworks.coldsweat.api.util.Temperature;
import com.momosoftworks.coldsweat.api.util.Temperature.Addition;
import com.momosoftworks.coldsweat.api.util.Temperature.Addition.Mode;
import com.momosoftworks.coldsweat.api.util.Temperature.Addition.Order;
import com.momosoftworks.coldsweat.common.capability.temperature.EntityTempCap;
import com.momosoftworks.coldsweat.common.capability.temperature.ITemperatureCap;
import com.momosoftworks.coldsweat.common.capability.ModCapabilities;
import com.momosoftworks.coldsweat.common.capability.temperature.PlayerTempCap;
import com.momosoftworks.coldsweat.config.ConfigSettings;
import com.momosoftworks.coldsweat.util.compat.CompatManager;
import com.momosoftworks.coldsweat.util.registries.ModAttributes;
import com.momosoftworks.coldsweat.util.registries.ModBlocks;
import com.momosoftworks.coldsweat.util.registries.ModEffects;
import com.momosoftworks.coldsweat.util.registries.ModItems;
import com.momosoftworks.coldsweat.util.world.WorldHelper;
import net.minecraft.core.Direction;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.damagesource.DamageSource;
import net.minecraft.world.effect.MobEffectInstance;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.ai.attributes.AttributeInstance;
import net.minecraft.world.entity.ai.attributes.AttributeModifier;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.entity.vehicle.Minecart;
import net.minecraft.world.inventory.AbstractContainerMenu;
import net.minecraft.world.inventory.ContainerListener;
import net.minecraft.world.inventory.ResultSlot;
import net.minecraft.world.inventory.Slot;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.UseAnim;
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
import net.minecraftforge.eventbus.api.EventPriority;
import net.minecraftforge.eventbus.api.SubscribeEvent;
import net.minecraftforge.fml.common.Mod;
import net.minecraftforge.registries.ForgeRegistries;

import javax.annotation.Nonnull;
import javax.annotation.Nullable;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;
import java.util.Set;
import java.util.concurrent.atomic.AtomicReference;

@Mod.EventBusSubscriber
public class EntityTempManager
{
    public static final Temperature.Type[] VALID_TEMPERATURE_TYPES = {Temperature.Type.CORE, Temperature.Type.BASE, Temperature.Type.WORLD};

    public static final Temperature.Type[] VALID_MODIFIER_TYPES    = {Temperature.Type.CORE, Temperature.Type.BASE, Temperature.Type.RATE, Temperature.Type.WORLD};

    public static final Either<Temperature.Type, Temperature.Ability>[] VALID_ATTRIBUTES = new Either[]
    {
        Either.left(Temperature.Type.WORLD),
        Either.left(Temperature.Type.BASE),
        Either.right(Temperature.Ability.HEAT_RESISTANCE),
        Either.right(Temperature.Ability.COLD_RESISTANCE),
        Either.right(Temperature.Ability.HEAT_DAMPENING),
        Either.right(Temperature.Ability.COLD_DAMPENING),
        Either.right(Temperature.Ability.FREEZING_POINT),
        Either.right(Temperature.Ability.BURNING_POINT)
    };

    public static final Set<EntityType<? extends LivingEntity>> TEMPERATURE_ENABLED_ENTITIES = new HashSet<>(ImmutableSet.<EntityType<? extends LivingEntity>>builder().add(EntityType.PLAYER).build());

    public static final Map<Entity, LazyOptional<ITemperatureCap>> SERVER_CAP_CACHE = new HashMap<>();
    public static final Map<Entity, LazyOptional<ITemperatureCap>> CLIENT_CAP_CACHE = new HashMap<>();

    public static LazyOptional<ITemperatureCap> getTemperatureCap(Entity entity)
    {
        Map<Entity, LazyOptional<ITemperatureCap>> cache = entity.level.isClientSide ? CLIENT_CAP_CACHE : SERVER_CAP_CACHE;
        return cache.computeIfAbsent(entity, e ->
        {   LazyOptional<ITemperatureCap> cap = e.getCapability(entity instanceof Player ? ModCapabilities.PLAYER_TEMPERATURE : ModCapabilities.ENTITY_TEMPERATURE);
            cap.addListener((opt) -> cache.remove(e));
            return cap;
        });
    }

    /**
     * Attach temperature capability to entities
     */
    @SubscribeEvent
    public static void attachCapabilityToEntityHandler(AttachCapabilitiesEvent<Entity> event)
    {
        if (event.getObject() instanceof LivingEntity entity)
        {
            if (!TEMPERATURE_ENABLED_ENTITIES.contains(entity.getType())) return;

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
                    {   return capOptional.cast();
                    }
                    return LazyOptional.empty();
                }

                @Override
                public CompoundTag serializeNBT()
                {   return tempCap.serializeNBT();
                }

                @Override
                public void deserializeNBT(CompoundTag nbt)
                {   tempCap.deserializeNBT(nbt);
                }
            };

            // Attach the capability to the entity
            event.addCapability(new ResourceLocation(ColdSweat.MOD_ID, "temperature"), provider);
        }
    }

    /**
     * Add modifiers to the player and valid entities when they join the world
     */
    @SubscribeEvent
    public static void initModifiersOnEntity(EntityJoinWorldEvent event)
    {
        if (event.getEntity() instanceof LivingEntity living && !living.level.isClientSide()
        && TEMPERATURE_ENABLED_ENTITIES.contains(living.getType()))
        {
            if (living.getServer() != null) living.getServer().execute(() ->
            {
                for (Temperature.Type type : VALID_MODIFIER_TYPES)
                {
                    GatherDefaultTempModifiersEvent gatherEvent = new GatherDefaultTempModifiersEvent(living, type);
                    MinecraftForge.EVENT_BUS.post(gatherEvent);

                    getTemperatureCap(living).ifPresent(cap ->
                    {   cap.clearModifiers(type);
                        cap.getModifiers(type).addAll(gatherEvent.getModifiers());
                    });
                }
            });
        }
    }

    /**
     * Tick TempModifiers and update temperature for living entities
     */
    @SubscribeEvent
    public static void onLivingTick(LivingEvent.LivingUpdateEvent event)
    {
        LivingEntity entity = event.getEntityLiving();
        if (!TEMPERATURE_ENABLED_ENTITIES.contains(entity.getType())) return;

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
                {   int expireTime = modifier.getExpireTime();
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

            if (!event.isWasDeath())
            {
                // Copy the capability to the new player
                getTemperatureCap(event.getEntity()).ifPresent(cap ->
                {   getTemperatureCap(oldPlayer).ifPresent(cap::copy);
                });
            }
            getTemperatureCap(oldPlayer).map(ITemperatureCap::getPersistentAttributes).orElse(new HashSet<>()).forEach(attr ->
            {   event.getPlayer().getAttribute(attr).setBaseValue(oldPlayer.getAttribute(attr).getBaseValue());
            });
            oldPlayer.invalidateCaps();
        }
    }

    /**
     * Add default modifiers to players and temperature-enabled entities
     */
    @SubscribeEvent(priority = EventPriority.HIGHEST)
    public static void defineDefaultModifiers(GatherDefaultTempModifiersEvent event)
    {
        // Default TempModifiers for players
        if (event.getEntity() instanceof Player)
        {
            if (event.getType() == Temperature.Type.WORLD)
            {
                event.addModifier(new BiomeTempModifier(25).tickRate(10), false, Addition.BEFORE_FIRST);
                event.addModifier(new UndergroundTempModifier().tickRate(10), false, Addition.of(Mode.AFTER, Order.FIRST, mod -> mod instanceof BiomeTempModifier));
                event.addModifier(new BlockTempModifier().tickRate(4), false, Addition.of(Mode.AFTER, Order.FIRST, mod -> mod instanceof UndergroundTempModifier));

                // Serene Seasons compat
                if (CompatManager.isSereneSeasonsLoaded())
                {
                    TempModifierRegistry.getEntryFor("sereneseasons:season").ifPresent(mod -> event.addModifier(mod.tickRate(60), false, Addition.of(Mode.BEFORE, Order.FIRST,
                                                                                                                                                     mod2 -> mod2 instanceof UndergroundTempModifier)));
                }
                // Weather2 Compat
                if (CompatManager.isWeather2Loaded())
                {
                    TempModifierRegistry.getEntryFor("weather2:storm").ifPresent(mod -> event.addModifier(mod.tickRate(60), false, Addition.of(Mode.BEFORE, Order.FIRST,
                                                                                                                                               mod2 -> mod2 instanceof UndergroundTempModifier)));
                }
            }
        }
        // Default TempModifiers for other temperature-enabled entities
        else if (event.getType() == Temperature.Type.WORLD && TEMPERATURE_ENABLED_ENTITIES.contains(event.getEntity().getType()))
        {   // Basic modifiers
            event.addModifier(new BiomeTempModifier(16).tickRate(40), false, Addition.BEFORE_FIRST);
            event.addModifier(new UndergroundTempModifier().tickRate(40), false, Addition.of(Mode.AFTER, Order.FIRST, mod -> mod instanceof BiomeTempModifier));
            event.addModifier(new BlockTempModifier(4).tickRate(20), false, Addition.of(Mode.AFTER, Order.FIRST, mod -> mod instanceof UndergroundTempModifier));

            // Serene Seasons compat
            if (CompatManager.isSereneSeasonsLoaded())
            {   TempModifierRegistry.getEntryFor("sereneseasons:season").ifPresent(mod -> event.addModifier(mod.tickRate(60), false, Addition.of(Mode.BEFORE, Order.FIRST,
                                                                                                                                          mod2 -> mod2 instanceof UndergroundTempModifier)));
            }
            // Weather2 Compat
            if (CompatManager.isWeather2Loaded())
            {   TempModifierRegistry.getEntryFor("weather2:storm").ifPresent(mod -> event.addModifier(mod.tickRate(60), false, Addition.of(Mode.BEFORE, Order.FIRST,
                                                                                                                                    mod2 -> mod2 instanceof UndergroundTempModifier)));
            }
        }
    }

    /**
     * Used to grant the player the sewing table recipe when they get an insulation item
     */
    @SubscribeEvent
    public static void addSewingIngredientListener(EntityJoinWorldEvent event)
    {
        // Add listener for granting the sewing table recipe when the player gets an insulation item
        if (event.getEntity() instanceof Player player)
            player.containerMenu.addSlotListener(new ContainerListener()
            {
                public void slotChanged(AbstractContainerMenu menu, int slotIndex, ItemStack stack)
                {   Slot slot = menu.getSlot(slotIndex);
                    if (!(slot instanceof ResultSlot))
                    {
                        if (slot.container == player.getInventory()
                        && (ConfigSettings.INSULATION_ITEMS.get().containsKey(stack.getItem())
                        || ConfigSettings.ADAPTIVE_INSULATION_ITEMS.get().containsKey(stack.getItem())))
                        {   player.awardRecipesByKey(new ResourceLocation[]{new ResourceLocation(ColdSweat.MOD_ID, "sewing_table")});
                        }
                    }
                }
                public void dataChanged(AbstractContainerMenu menu, int slot, int value) {}
            });
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
                if (WorldHelper.isInWater(player) || player.tickCount % 40 == 0
                && WorldHelper.isRainingAt(player.level, player.blockPosition()))
                {   Temperature.addModifier(player, new WaterTempModifier(0.01f).tickRate(5), Temperature.Type.WORLD, false);
                }

                if (player.isFreezing())
                {   Temperature.addOrReplaceModifier(player, new FreezingTempModifier(player.getTicksFrozen() / 13.5f).expires(5), Temperature.Type.BASE);
                }

                if (player.isOnFire())
                {   Temperature.addOrReplaceModifier(player, new FireTempModifier().expires(5), Temperature.Type.BASE);
                }
            }

            if (player.isFreezing() && player.getTicksFrozen() > 0)
            {
                AtomicReference<Double> insulation = new AtomicReference<>((double) 0);
                boolean hasIcePotion = player.hasEffect(ModEffects.ICE_RESISTANCE) && ConfigSettings.ICE_RESISTANCE_ENABLED.get();

                if (!hasIcePotion)
                {
                    Temperature.getModifier(player, Temperature.Type.RATE, InsulationTempModifier.class).ifPresent(insulModifier ->
                    {   insulation.updateAndGet(v -> (v + insulModifier.getNBT().getDouble("Hot") + insulModifier.getNBT().getDouble("Cold")));
                    });
                }

                if (!(hasIcePotion || insulation.get() > 0) && (player.tickCount % Math.max(1, 37 - insulation.get())) == 0)
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
        if (event.phase == TickEvent.Phase.START && !event.player.level.isClientSide() && event.player.tickCount % 5 == 0)
        {
            Player player = event.player;
            if (player.getVehicle() != null)
            {
                Entity mount = player.getVehicle();
                // If insulated minecart
                if (mount instanceof Minecart minecart && minecart.getDisplayBlockState().getBlock() == ModBlocks.MINECART_INSULATION)
                {   Temperature.addOrReplaceModifier(player, new MountTempModifier(1, 1).tickRate(5).expires(5), Temperature.Type.RATE);
                }
                // If insulated entity (defined in config)
                else
                {
                    Pair<Double, Double> entityInsul = ConfigSettings.INSULATED_ENTITIES.get().get(ForgeRegistries.ENTITIES.getKey(mount.getType()));
                    if (entityInsul != null)
                    {   Temperature.addOrReplaceModifier(player, new MountTempModifier(entityInsul.getFirst(), entityInsul.getSecond()).tickRate(5).expires(5), Temperature.Type.RATE);
                    }
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
        if (event.getEntity() instanceof Player player
        && (event.getItem().getUseAnimation() == UseAnim.DRINK || event.getItem().getUseAnimation() == UseAnim.EAT)
        && !event.getEntity().level.isClientSide)
        {
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

    /**
     * Reset the player's temperature upon respawning
     */
    @SubscribeEvent
    public static void resetTempOnRespawn(PlayerEvent.PlayerRespawnEvent event)
    {
        if (!event.isEndConquered())
        getTemperatureCap(event.getEntity()).ifPresent(cap ->
        {
            cap.copy(new PlayerTempCap());
            if (!event.getEntity().level.isClientSide)
            {   Temperature.updateTemperature(event.getEntityLiving(), cap, true);
            }
        });
    }

    public static Set<EntityType<? extends LivingEntity>> getEntitiesWithTemperature()
    {   return ImmutableSet.copyOf(TEMPERATURE_ENABLED_ENTITIES);
    }

    /**
     * Sets the corresponding attribute value for the given {@link Temperature.Type} or {@link Temperature.Ability}.
     * @param param the type or ability to get the attribute for
     */
    public static void setAttribute(Object param, LivingEntity entity, double value)
    {
        if (param instanceof Temperature.Type type)
        {
            switch (type)
            {
                case WORLD -> entity.getAttribute(ModAttributes.WORLD_TEMPERATURE).setBaseValue(value);
                case BASE  -> entity.getAttribute(ModAttributes.BASE_BODY_TEMPERATURE).setBaseValue(value);
            }
        }
        else if (param instanceof Temperature.Ability ability)
        {
            switch (ability)
            {
                case HEAT_RESISTANCE -> entity.getAttribute(ModAttributes.HEAT_RESISTANCE).setBaseValue(value);
                case COLD_RESISTANCE -> entity.getAttribute(ModAttributes.COLD_RESISTANCE).setBaseValue(value);
                case HEAT_DAMPENING  -> entity.getAttribute(ModAttributes.HEAT_DAMPENING).setBaseValue(value);
                case COLD_DAMPENING  -> entity.getAttribute(ModAttributes.COLD_DAMPENING).setBaseValue(value);
                case FREEZING_POINT -> entity.getAttribute(ModAttributes.FREEZING_POINT).setBaseValue(value);
                case BURNING_POINT  -> entity.getAttribute(ModAttributes.BURNING_POINT).setBaseValue(value);
            }
        }

        throw ColdSweat.LOGGER.throwing(new IllegalArgumentException("EntityTempManager.getAttribute(): \"" + param + "\" is not a valid type or ability!"));
    }

    /**
     * Gets the corresponding attribute value for the given {@link Temperature.Type} or {@link Temperature.Ability}.
     * @param param the type or ability to get the attribute for
     */
    @Nullable
    public static AttributeInstance getAttribute(Object param, LivingEntity entity)
    {
        if (param instanceof Either<?, ?> either)
        {
            return ((Either<Temperature.Type, Temperature.Ability>) either).map(
                type -> switch (type)
                {
                    case WORLD -> entity.getAttribute(ModAttributes.WORLD_TEMPERATURE);
                    case BASE  -> entity.getAttribute(ModAttributes.BASE_BODY_TEMPERATURE);
                    default -> throw ColdSweat.LOGGER.throwing(new IllegalArgumentException("EntityTempManager.getAttribute(): \"" + type + "\" is not a valid type!"));
                },
                ability -> switch (ability)
                {
                    case FREEZING_POINT  -> entity.getAttribute(ModAttributes.FREEZING_POINT);
                    case BURNING_POINT   -> entity.getAttribute(ModAttributes.BURNING_POINT);
                    case HEAT_RESISTANCE -> entity.getAttribute(ModAttributes.HEAT_RESISTANCE);
                    case COLD_RESISTANCE -> entity.getAttribute(ModAttributes.COLD_RESISTANCE);
                    case HEAT_DAMPENING  -> entity.getAttribute(ModAttributes.HEAT_DAMPENING);
                    case COLD_DAMPENING  -> entity.getAttribute(ModAttributes.COLD_DAMPENING);
                }
            );
        }
        else if (param instanceof Temperature.Type type)
        {   return getAttribute(Either.left(type), entity);
        }
        else if (param instanceof Temperature.Ability ability)
        {   return getAttribute(Either.right(ability), entity);
        }

        throw ColdSweat.LOGGER.throwing(new IllegalArgumentException("EntityTempManager.getAttribute(): \"" + param + "\" is not a valid type or ability!"));
    }

    public static AttributeModifier makeAttributeModifier(Either<Temperature.Type, Temperature.Ability> param, double value, AttributeModifier.Operation operation)
    {
        return param.map(
        type -> switch (type)
        {
            case WORLD -> new AttributeModifier("World Temperature Modifier", value, operation);
            case BASE  -> new AttributeModifier("Base Body Temperature Modifier", value, operation);
            default -> throw ColdSweat.LOGGER.throwing(new IllegalArgumentException("EntityTempManager.makeAttributeModifier(): \"" + type + "\" is not a valid type!"));
        },
        ability -> switch (ability)
        {
            case FREEZING_POINT -> new AttributeModifier("Freezing Point Modifier", value, operation);
            case BURNING_POINT  -> new AttributeModifier("Burning Point Modifier", value, operation);
            case HEAT_RESISTANCE -> new AttributeModifier("Heat Resistance Modifier", value, operation);
            case COLD_RESISTANCE -> new AttributeModifier("Cold Resistance Modifier", value, operation);
            case HEAT_DAMPENING  -> new AttributeModifier("Heat Dampening Modifier", value, operation);
            case COLD_DAMPENING  -> new AttributeModifier("Cold Dampening Modifier", value, operation);
        });
    }
}