package com.momosoftworks.coldsweat.common.capability.handler;

import com.google.common.collect.ImmutableSet;
import com.mojang.datafixers.util.Pair;
import com.momosoftworks.coldsweat.ColdSweat;
import com.momosoftworks.coldsweat.api.event.common.TempModifierEvent;
import com.momosoftworks.coldsweat.api.event.core.GatherDefaultTempModifiersEvent;
import com.momosoftworks.coldsweat.api.registry.TempModifierRegistry;
import com.momosoftworks.coldsweat.api.temperature.modifier.*;
import com.momosoftworks.coldsweat.api.util.Placement;
import com.momosoftworks.coldsweat.api.util.Placement.Mode;
import com.momosoftworks.coldsweat.api.util.Placement.Order;
import com.momosoftworks.coldsweat.api.util.Temperature;
import com.momosoftworks.coldsweat.common.capability.ModCapabilities;
import com.momosoftworks.coldsweat.common.capability.temperature.ITemperatureCap;
import com.momosoftworks.coldsweat.config.ConfigSettings;
import com.momosoftworks.coldsweat.config.type.InsulatingMount;
import com.momosoftworks.coldsweat.config.type.Insulator;
import com.momosoftworks.coldsweat.config.type.PredicateItem;
import com.momosoftworks.coldsweat.core.init.ModAttributes;
import com.momosoftworks.coldsweat.core.init.ModBlocks;
import com.momosoftworks.coldsweat.core.init.ModEffects;
import com.momosoftworks.coldsweat.core.init.ModItems;
import com.momosoftworks.coldsweat.util.compat.CompatManager;
import com.momosoftworks.coldsweat.util.math.CSMath;
import com.momosoftworks.coldsweat.util.world.WorldHelper;
import net.minecraft.core.Holder;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.nbt.Tag;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.effect.MobEffectInstance;
import net.minecraft.world.entity.*;
import net.minecraft.world.entity.ai.attributes.Attribute;
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
import net.neoforged.bus.api.EventPriority;
import net.neoforged.bus.api.SubscribeEvent;
import net.neoforged.fml.common.EventBusSubscriber;
import net.neoforged.neoforge.common.NeoForge;
import net.neoforged.neoforge.event.entity.EntityJoinLevelEvent;
import net.neoforged.neoforge.event.entity.EntityLeaveLevelEvent;
import net.neoforged.neoforge.event.entity.living.LivingEntityUseItemEvent;
import net.neoforged.neoforge.event.entity.living.LivingEquipmentChangeEvent;
import net.neoforged.neoforge.event.entity.living.LivingIncomingDamageEvent;
import net.neoforged.neoforge.event.entity.living.MobEffectEvent;
import net.neoforged.neoforge.event.entity.player.PlayerEvent;
import net.neoforged.neoforge.event.level.SleepFinishedTimeEvent;
import net.neoforged.neoforge.event.tick.EntityTickEvent;
import net.neoforged.neoforge.event.tick.PlayerTickEvent;

import javax.annotation.Nullable;
import java.util.*;
import java.util.concurrent.atomic.AtomicReference;
import java.util.function.Function;
import java.util.stream.Stream;

@EventBusSubscriber
public class EntityTempManager
{
    public static final Temperature.Trait[] VALID_TEMPERATURE_TRAITS = { Temperature.Trait.CORE, Temperature.Trait.BASE, Temperature.Trait.WORLD,
                                                                         Temperature.Trait.HEAT_RESISTANCE, Temperature.Trait.COLD_RESISTANCE,
                                                                         Temperature.Trait.HEAT_DAMPENING, Temperature.Trait.COLD_DAMPENING,
                                                                         Temperature.Trait.FREEZING_POINT, Temperature.Trait.BURNING_POINT };

    public static final Temperature.Trait[] VALID_MODIFIER_TRAITS = { Temperature.Trait.CORE, Temperature.Trait.BASE,
                                                                      Temperature.Trait.RATE, Temperature.Trait.WORLD,
                                                                      Temperature.Trait.HEAT_RESISTANCE, Temperature.Trait.COLD_RESISTANCE,
                                                                      Temperature.Trait.HEAT_DAMPENING, Temperature.Trait.COLD_DAMPENING,
                                                                      Temperature.Trait.FREEZING_POINT, Temperature.Trait.BURNING_POINT };

    public static final Temperature.Trait[] VALID_ATTRIBUTE_TYPES = new Temperature.Trait[]
    {
        Temperature.Trait.WORLD,
        Temperature.Trait.BASE,
        Temperature.Trait.HEAT_RESISTANCE,
        Temperature.Trait.COLD_RESISTANCE,
        Temperature.Trait.HEAT_DAMPENING,
        Temperature.Trait.COLD_DAMPENING,
        Temperature.Trait.FREEZING_POINT,
        Temperature.Trait.BURNING_POINT
    };

    public static final Set<EntityType<? extends LivingEntity>> TEMPERATURE_ENABLED_ENTITIES = new HashSet<>(ImmutableSet.<EntityType<? extends LivingEntity>>builder().add(EntityType.PLAYER).build());

    public static final Map<Entity, ITemperatureCap> SERVER_CAP_CACHE = new HashMap<>();
    public static final Map<Entity, ITemperatureCap> CLIENT_CAP_CACHE = new HashMap<>();

    public static ITemperatureCap getTemperatureCap(Entity entity)
    {
        Map<Entity, ITemperatureCap> cache = entity.level().isClientSide ? CLIENT_CAP_CACHE : SERVER_CAP_CACHE;
        return cache.computeIfAbsent(entity, e -> e.getCapability(entity instanceof Player
                                                                  ? ModCapabilities.PLAYER_TEMPERATURE
                                                                  : ModCapabilities.ENTITY_TEMPERATURE));
    }

    /**
     * Add modifiers to the player and valid entities when they join the world
     */
    @SubscribeEvent
    public static void initModifiersOnEntity(EntityJoinLevelEvent event)
    {
        if (event.getEntity() instanceof LivingEntity living && !living.level().isClientSide()
        && TEMPERATURE_ENABLED_ENTITIES.contains(living.getType()))
        {
            ITemperatureCap cap = getTemperatureCap(living);
            // If entity has never been initialized, add default modifiers
            List<TempModifier> allModifiers = new ArrayList<>();
            for (Temperature.Trait trait : VALID_MODIFIER_TRAITS)
            {   allModifiers.addAll(cap.getModifiers(trait));
            }
            if (allModifiers.isEmpty())
            {
                for (Temperature.Trait trait : VALID_MODIFIER_TRAITS)
                {
                    GatherDefaultTempModifiersEvent gatherEvent = new GatherDefaultTempModifiersEvent(living, trait);
                    NeoForge.EVENT_BUS.post(gatherEvent);

                    cap.getModifiers(trait).addAll(gatherEvent.getModifiers());
                }
                living.getPersistentData().putBoolean("InitializedModifiers", true);
            }
        }
    }

    /**
     * Tick TempModifiers and update temperature for living entities
     */
    @SubscribeEvent
    public static void onLivingTick(EntityTickEvent.Pre event)
    {
        if (!(event.getEntity() instanceof LivingEntity entity) || !TEMPERATURE_ENABLED_ENTITIES.contains(entity.getType())) return;

        ITemperatureCap cap = getTemperatureCap(entity);
        if (!entity.level().isClientSide)
        {   // Tick modifiers serverside
            cap.tick(entity);
        }
        else
        {   // Tick modifiers clientside
            cap.tickDummy(entity);
        }

        // Remove expired modifiers
        for (Temperature.Trait trait : VALID_MODIFIER_TRAITS)
        {
            cap.getModifiers(trait).removeIf(modifier ->
            {   int expireTime = modifier.getExpireTime();
                return (modifier.setTicksExisted(modifier.getTicksExisted() + 1) > expireTime && expireTime != -1);
            });
        }

        if (entity instanceof Player && entity.tickCount % 60 == 0)
        {   Temperature.updateModifiers(entity, cap);
        }
    }

    /**
     * Transfer the player's capability when traveling from the End
     */
    @SubscribeEvent
    public static void carryOverPersistentAttributes(PlayerEvent.Clone event)
    {
        Player oldPlayer = event.getOriginal();
        Player newPlayer = event.getEntity();

        if (!newPlayer.level().isClientSide)
        {
            // Get the old player's capability
            getTemperatureCap(oldPlayer).getPersistentAttributes()
            .forEach(attr ->
            {   newPlayer.getAttribute(Holder.direct(attr)).setBaseValue(oldPlayer.getAttribute(Holder.direct(attr)).getBaseValue());
            });
        }
    }

    /**
     * Reset the player's temperature upon respawning
     */
    @SubscribeEvent
    public static void handlePlayerReset(PlayerEvent.Clone event)
    {
        Player oldPlayer = event.getOriginal();
        Player newPlayer = event.getEntity();

        SERVER_CAP_CACHE.remove(oldPlayer);
        CLIENT_CAP_CACHE.remove(oldPlayer);

        ITemperatureCap cap = getTemperatureCap(newPlayer);
        {
            if (!event.isWasDeath())
            {   cap.copy(getTemperatureCap(oldPlayer));
            }
            if (!newPlayer.level().isClientSide)
            {   Temperature.updateTemperature(newPlayer, cap, true);
            }
        }
    }

    @SubscribeEvent
    public static void invalidateDespawnedEntity(EntityLeaveLevelEvent event)
    {
        SERVER_CAP_CACHE.remove(event.getEntity());
        CLIENT_CAP_CACHE.remove(event.getEntity());
    }

    /**
     * Add default modifiers to players and temperature-enabled entities
     */
    @SubscribeEvent(priority = EventPriority.HIGHEST)
    public static void defineDefaultModifiers(GatherDefaultTempModifiersEvent event)
    {
        // Default TempModifiers for players
        if (event.getEntity() instanceof Player && event.getTrait() == Temperature.Trait.WORLD)
        {
            event.addModifier(new BiomeTempModifier(25).tickRate(10), Placement.Duplicates.BY_CLASS, Placement.BEFORE_FIRST);
            event.addModifier(new UndergroundTempModifier().tickRate(10), Placement.Duplicates.BY_CLASS, Placement.of(Mode.AFTER, Order.FIRST, mod -> mod instanceof BiomeTempModifier));
            event.addModifier(new BlockTempModifier().tickRate(4), Placement.Duplicates.BY_CLASS, Placement.of(Mode.AFTER, Order.FIRST, mod -> mod instanceof UndergroundTempModifier));

            // Serene Seasons compat
            if (CompatManager.isSereneSeasonsLoaded())
            {
                TempModifierRegistry.getValue(ResourceLocation.parse("sereneseasons:season")).ifPresent(mod -> event.addModifier(mod.tickRate(60), Placement.Duplicates.BY_CLASS, Placement.of(Mode.BEFORE, Order.FIRST,
                                                                                                                                                 mod2 -> mod2 instanceof UndergroundTempModifier)));
            }
            // Weather2 Compat
            if (CompatManager.isWeather2Loaded())
            {
                TempModifierRegistry.getValue(ResourceLocation.parse("weather2:storm")).ifPresent(mod -> event.addModifier(mod.tickRate(60), Placement.Duplicates.BY_CLASS, Placement.of(Mode.BEFORE, Order.FIRST,
                                                                                                                                           mod2 -> mod2 instanceof UndergroundTempModifier)));
            }
        }
        // Default TempModifiers for other temperature-enabled entities
        else if (event.getTrait() == Temperature.Trait.WORLD && TEMPERATURE_ENABLED_ENTITIES.contains(event.getEntity().getType()))
        {   // Basic modifiers
            event.addModifier(new BiomeTempModifier(16).tickRate(40), Placement.Duplicates.BY_CLASS, Placement.BEFORE_FIRST);
            event.addModifier(new UndergroundTempModifier().tickRate(40), Placement.Duplicates.BY_CLASS, Placement.of(Mode.AFTER, Order.FIRST, mod -> mod instanceof BiomeTempModifier));
            event.addModifier(new BlockTempModifier(4).tickRate(20), Placement.Duplicates.BY_CLASS, Placement.of(Mode.AFTER, Order.FIRST, mod -> mod instanceof UndergroundTempModifier));

            // Serene Seasons compat
            if (CompatManager.isSereneSeasonsLoaded())
            {   TempModifierRegistry.getValue(ResourceLocation.parse("sereneseasons:season")).ifPresent(mod -> event.addModifier(mod.tickRate(60), Placement.Duplicates.BY_CLASS, Placement.of(Mode.BEFORE, Order.FIRST,
                                                                                                                                          mod2 -> mod2 instanceof UndergroundTempModifier)));
            }
            // Weather2 Compat
            if (CompatManager.isWeather2Loaded())
            {   TempModifierRegistry.getValue(ResourceLocation.parse("weather2:storm")).ifPresent(mod -> event.addModifier(mod.tickRate(60), Placement.Duplicates.BY_CLASS, Placement.of(Mode.BEFORE, Order.FIRST,
                                                                                                                                    mod2 -> mod2 instanceof UndergroundTempModifier)));
            }
        }
    }

    /**
     * Used to grant the player the sewing table recipe when they get an insulation item
     */
    @SubscribeEvent
    public static void addSewingIngredientListener(EntityJoinLevelEvent event)
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
                        && (ConfigSettings.INSULATION_ITEMS.get().containsKey(stack.getItem())))
                        {   player.awardRecipesByKey(List.of(ResourceLocation.fromNamespaceAndPath(ColdSweat.MOD_ID, "sewing_table")));
                        }
                    }
                }
                public void dataChanged(AbstractContainerMenu menu, int slot, int value) {}
            });
    }

    @SubscribeEvent
    public static void calcModifierImmunity(TempModifierEvent.Calculate.Modify event)
    {
        if (!Arrays.stream(VALID_ATTRIBUTE_TYPES).toList().contains(event.getTrait())) return;
        TempModifier mod = event.getModifier();
        ResourceLocation modifierKey = TempModifierRegistry.getKey(mod);
        LivingEntity entity = event.getEntity();

        for (Map.Entry<ItemStack, Insulator> entry : getInsulatorsOnEntity(event.getEntity()).entrySet())
        {
            Insulator insulator = entry.getValue();
            ItemStack stack = entry.getKey();

            Double immunity = insulator.immuneTempModifiers().get(modifierKey);
            if (immunity != null && insulator.test(event.getEntity(), stack))
            {
                Function<Double, Double> func = event.getFunction();
                double lastInput = mod instanceof BiomeTempModifier
                                   ? (Temperature.get(entity, Temperature.Trait.FREEZING_POINT) + Temperature.get(entity, Temperature.Trait.BURNING_POINT)) / 2
                                   : mod.getLastInput();
                event.setFunction(temp -> CSMath.blend(func.apply(temp), lastInput, immunity, 0, 1));
            }
        }
    }

    /**
     * Handle modifiers for freezing, burning, and being wet
     */
    @SubscribeEvent
    public static void onPlayerTick(PlayerTickEvent.Pre event)
    {
        Player player = event.getEntity();

        // Water / Rain
        if (!player.level().isClientSide)
        {
            if (player.tickCount % 5 == 0)
            {
                if (!player.isSpectator() && WorldHelper.isInWater(player) || player.tickCount % 40 == 0
                && WorldHelper.isRainingAt(player.level(), player.blockPosition()))
                {   Temperature.addModifier(player, new WaterTempModifier(0.01f).tickRate(5), Temperature.Trait.WORLD, Placement.Duplicates.BY_CLASS);
                }

                if (player.isFreezing())
                {   Temperature.addOrReplaceModifier(player, new FreezingTempModifier(player.getTicksFrozen() / 13.5f).expires(5), Temperature.Trait.BASE, Placement.Duplicates.BY_CLASS);
                }

                if (player.isOnFire())
                {   Temperature.addOrReplaceModifier(player, new FireTempModifier().expires(5), Temperature.Trait.BASE, Placement.Duplicates.BY_CLASS);
                }
            }

            if (player.isFreezing() && player.getTicksFrozen() > 0)
            {
                AtomicReference<Double> insulation = new AtomicReference<>((double) 0);
                boolean hasIcePotion = player.hasEffect(ModEffects.ICE_RESISTANCE) && ConfigSettings.ICE_RESISTANCE_ENABLED.get();

                if (!hasIcePotion)
                {
                    Temperature.getModifier(player, Temperature.Trait.RATE, ArmorInsulationTempModifier.class).ifPresent(insulModifier ->
                    {   insulation.updateAndGet(v -> (v + insulModifier.getNBT().getDouble("Hot") + insulModifier.getNBT().getDouble("Cold")));
                    });
                }

                if (!(hasIcePotion || insulation.get() > 0) && (player.tickCount % Math.max(1, 37 - insulation.get())) == 0)
                {   player.setTicksFrozen(player.getTicksFrozen() - 1);
                }
            }
        }
    }

    @SubscribeEvent
    public static void updateAttributeModifiersOnSlotChange(LivingEquipmentChangeEvent event)
    {
        updateInsulationAttributeModifiers(event.getEntity());
    }

    public static void updateInsulationAttributeModifiers(LivingEntity entity)
    {
        Stream.of(ConfigSettings.INSULATION_ITEMS.get().values(),
                  ConfigSettings.INSULATING_ARMORS.get().values(),
                  ConfigSettings.INSULATING_CURIOS.get().values())
        .flatMap(Collection::stream)
        .forEach(insulator ->
        {
            for (Map.Entry<Attribute, AttributeModifier> entry : insulator.attributes().getMap().entries())
            {
                Attribute attribute = entry.getKey();
                AttributeModifier modifier = entry.getValue();
                AttributeInstance instance = entity.getAttribute(Holder.direct(attribute));
                if (instance != null)
                {   instance.removeModifier(modifier);
                }
            }
        });

        for (Map.Entry<ItemStack, Insulator> insulationItem : getInsulatorsOnEntity(entity).entrySet())
        {
            Insulator insulator = insulationItem.getValue();
            ItemStack stack = insulationItem.getKey();
            if (insulator.test(entity, stack))
            {
                for (Map.Entry<Attribute, AttributeModifier> entry : insulator.attributes().getMap().entries())
                {
                    Attribute attribute = entry.getKey();
                    AttributeModifier modifier = entry.getValue();
                    AttributeInstance instance = entity.getAttribute(Holder.direct(attribute));
                    if (instance != null)
                    {   instance.addTransientModifier(modifier);
                    }
                }
            }
        }
    }

    /**
     * Cancel freezing damage when the player has the Ice Resistance effect
     */
    @SubscribeEvent
    public static void cancelFreezingDamage(LivingIncomingDamageEvent event)
    {
        if (event.getSource().equals(event.getEntity().level().damageSources().freeze()) && event.getEntity().hasEffect(ModEffects.ICE_RESISTANCE) && ConfigSettings.ICE_RESISTANCE_ENABLED.get())
        {   event.setCanceled(true);
        }
    }

    /**
     * Handle HearthTempModifier when the player has the Insulation effect
     */
    @SubscribeEvent
    public static void onInsulationAdded(MobEffectEvent.Added event)
    {
        if (!event.getEntity().level().isClientSide && event.getEntity() instanceof Player player && event.getEffectInstance() != null
        && event.getEffectInstance().getEffect() == ModEffects.INSULATED)
        {
            // Add TempModifier on potion effect added
            MobEffectInstance effect = event.getEffectInstance();
            // New HearthTempModifier
            TempModifier newMod = new BlockInsulationTempModifier(effect.getAmplifier() + 1).expires(effect.getDuration());
            Temperature.addOrReplaceModifier(player, newMod, Temperature.Trait.WORLD, Placement.Duplicates.BY_CLASS);
        }
    }

    @SubscribeEvent
    public static void onInsulationRemoved(MobEffectEvent.Remove event)
    {
        if (!event.getEntity().level().isClientSide && event.getEntity() instanceof Player player && event.getEffectInstance() != null
        && event.getEffectInstance().getEffect() == ModEffects.INSULATED)
        {
            // Remove TempModifier on potion effect removed
            Temperature.removeModifiers(player, Temperature.Trait.WORLD, mod -> mod instanceof BlockInsulationTempModifier);
        }
    }

    /**
     * Improve the player's temperature when they sleep
     */
    @SubscribeEvent
    public static void onSleep(SleepFinishedTimeEvent event)
    {
        if (!event.getLevel().isClientSide())
        {
            event.getLevel().players().forEach(player ->
            {
                if (player.isSleeping())
                {
                    // Divide the player's current temperature by 4
                    ITemperatureCap cap = getTemperatureCap(player);
                    double temp = cap.getTrait(Temperature.Trait.CORE);
                    cap.setTrait(Temperature.Trait.CORE, temp / 4f);
                    Temperature.updateTemperature(player, cap, true);
                }
            });
        }
    }

    /**
     * Handle insulation on mounted entity
     */
    @SubscribeEvent
    public static void playerRiding(PlayerTickEvent.Pre event)
    {
        if (!event.getEntity().level().isClientSide() && event.getEntity().tickCount % 5 == 0)
        {
            Player player = event.getEntity();
            if (player.getVehicle() != null)
            {
                Entity mount = player.getVehicle();
                // If insulated minecart
                if (mount instanceof Minecart minecart && minecart.getDisplayBlockState().getBlock() == ModBlocks.MINECART_INSULATION.value())
                {   Temperature.addOrReplaceModifier(player, new MountTempModifier(1, 1).tickRate(5).expires(5), Temperature.Trait.RATE, Placement.Duplicates.BY_CLASS);
                }
                // If insulated entity (defined in config)
                else
                {
                    InsulatingMount entityInsul = ConfigSettings.INSULATED_ENTITIES.get().get(mount.getType());
                    if (entityInsul != null && entityInsul.test(mount))
                    {   Temperature.addOrReplaceModifier(player, new MountTempModifier(entityInsul.coldInsulation(), entityInsul.heatInsulation()).tickRate(5).expires(5), Temperature.Trait.RATE, Placement.Duplicates.BY_CLASS);
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
        && !event.getEntity().level().isClientSide)
        {
            // If food item defined in config
            PredicateItem foodData = ConfigSettings.FOOD_TEMPERATURES.get().get(event.getItem().getItem());
            if (foodData != null && foodData.test(event.getItem()))
            {
                double effect = foodData.value();
                if (foodData.extraData().contains("duration", Tag.TAG_INT))
                {
                    int duration = foodData.extraData().getInt("duration");
                    // Special case for soul sprouts
                    FoodTempModifier foodModifier = event.getItem().getItem() == ModItems.SOUL_SPROUT.value()
                                                    ? new SoulSproutTempModifier(effect)
                                                    : new FoodTempModifier(effect);
                    // Store the duration of the TempModifier
                    foodModifier.getNBT().put("extraData", foodData.extraData());
                    // Add the TempModifier
                    Temperature.addModifier(player, foodModifier.expires(duration), Temperature.Trait.BASE, Placement.Duplicates.BY_CLASS);
                }
                else
                {   Temperature.addModifier(player, new FoodTempModifier(effect).expires(0), Temperature.Trait.CORE, Placement.Duplicates.EXACT);
                }
            }
        }
    }

    public static Set<EntityType<? extends LivingEntity>> getEntitiesWithTemperature()
    {   return ImmutableSet.copyOf(TEMPERATURE_ENABLED_ENTITIES);
    }

    public static Map<ItemStack, Insulator> getInsulatorsOnEntity(LivingEntity entity)
    {
        Map<ItemStack, Insulator> insulators = new HashMap<>();
        for (EquipmentSlot slot : EquipmentSlot.values())
        {
            if (!slot.isArmor()) continue;
            ItemStack stack = entity.getItemBySlot(slot);
            if (!stack.isEmpty())
            {
                Optional.ofNullable(ConfigSettings.INSULATING_ARMORS.get().get(stack.getItem())).ifPresent(insul -> insulators.put(stack, insul));
                ItemInsulationManager.getInsulationCap(stack).getInsulation().stream().map(Pair::getFirst)
                .forEach(item ->
                {
                    Optional.ofNullable(ConfigSettings.INSULATION_ITEMS.get().get(item.getItem())).ifPresent(insul -> insulators.put(item, insul));
                });
            }
        }
        for (ItemStack curio : CompatManager.getCurios(entity))
        {   Optional.ofNullable(ConfigSettings.INSULATING_CURIOS.get().get(curio.getItem())).ifPresent(insul -> insulators.put(curio, insul));
        }
        return insulators;
    }

    /**
     * Sets the corresponding attribute value for the given {@link Temperature.Trait}.
     * @param trait the type or ability to get the attribute for
     */
    public static void setAttribute(Temperature.Trait trait, LivingEntity entity, double value)
    {
        switch (trait)
        {
            case WORLD -> CSMath.doIfNotNull(entity.getAttribute(ModAttributes.WORLD_TEMPERATURE), att -> att.setBaseValue(value));
            case BASE  -> CSMath.doIfNotNull(entity.getAttribute(ModAttributes.BASE_BODY_TEMPERATURE), att -> att.setBaseValue(value));
            case HEAT_RESISTANCE -> CSMath.doIfNotNull(entity.getAttribute(ModAttributes.HEAT_RESISTANCE), att -> att.setBaseValue(value));
            case COLD_RESISTANCE -> CSMath.doIfNotNull(entity.getAttribute(ModAttributes.COLD_RESISTANCE), att -> att.setBaseValue(value));
            case HEAT_DAMPENING  -> CSMath.doIfNotNull(entity.getAttribute(ModAttributes.HEAT_DAMPENING), att -> att.setBaseValue(value));
            case COLD_DAMPENING  -> CSMath.doIfNotNull(entity.getAttribute(ModAttributes.COLD_DAMPENING), att -> att.setBaseValue(value));
            case FREEZING_POINT -> CSMath.doIfNotNull(entity.getAttribute(ModAttributes.FREEZING_POINT), att -> att.setBaseValue(value));
            case BURNING_POINT  -> CSMath.doIfNotNull(entity.getAttribute(ModAttributes.BURNING_POINT), att -> att.setBaseValue(value));
        }
    }

    /**
     * Gets the corresponding attribute value for the given {@link Temperature.Trait}.
     * @param trait the type or ability to get the attribute for
     */
    @Nullable
    public static AttributeInstance getAttribute(Temperature.Trait trait, LivingEntity entity)
    {
        return switch (trait)
        {
            case WORLD -> entity.getAttribute(ModAttributes.WORLD_TEMPERATURE);
            case BASE  -> entity.getAttribute(ModAttributes.BASE_BODY_TEMPERATURE);
            case FREEZING_POINT  -> entity.getAttribute(ModAttributes.FREEZING_POINT);
            case BURNING_POINT   -> entity.getAttribute(ModAttributes.BURNING_POINT);
            case HEAT_RESISTANCE -> entity.getAttribute(ModAttributes.HEAT_RESISTANCE);
            case COLD_RESISTANCE -> entity.getAttribute(ModAttributes.COLD_RESISTANCE);
            case HEAT_DAMPENING  -> entity.getAttribute(ModAttributes.HEAT_DAMPENING);
            case COLD_DAMPENING  -> entity.getAttribute(ModAttributes.COLD_DAMPENING);

            default -> throw ColdSweat.LOGGER.throwing(new IllegalArgumentException("\"" + trait + "\" is not a valid trait!"));
        };
    }

    public static Collection<AttributeModifier> getAllAttributeModifiers(LivingEntity entity, AttributeInstance attribute, @Nullable AttributeModifier.Operation operation)
    {
        Collection<AttributeModifier> modifiers = new ArrayList<>(operation == null
                                                                  ? attribute.getModifiers()
                                                                  : attribute.getModifiers().stream().filter(modifier -> modifier.operation() == operation).toList());
        modifiers.addAll(getAllEquipmentAttributeModifiers(entity, attribute, operation));

        return modifiers;
    }

    public static Collection<AttributeModifier> getAllEquipmentAttributeModifiers(LivingEntity entity, AttributeInstance attribute, @Nullable AttributeModifier.Operation operation)
    {
        Collection<AttributeModifier> modifiers = new ArrayList<>();

        for (EquipmentSlot slot : EquipmentSlot.values())
        {
            if (!slot.isArmor()) continue;
            ItemStack stack = entity.getItemBySlot(slot);
            if (!stack.isEmpty())
            {   modifiers.addAll(ItemInsulationManager.getAttributeModifiersForSlot(stack, attribute.getAttribute().value(), slot, operation, entity));
            }
        }
        return modifiers;
    }

    public static AttributeModifier makeAttributeModifier(Temperature.Trait trait, double value, AttributeModifier.Operation operation)
    {
        return switch (trait)
        {
            case WORLD -> new AttributeModifier(ResourceLocation.fromNamespaceAndPath(ColdSweat.MOD_ID, "world_temp_modifier"), value, operation);
            case BASE  -> new AttributeModifier(ResourceLocation.fromNamespaceAndPath(ColdSweat.MOD_ID, "base_temp_modifier"), value, operation);

            case FREEZING_POINT -> new AttributeModifier(ResourceLocation.fromNamespaceAndPath(ColdSweat.MOD_ID, "freezing_point_modifier"), value, operation);
            case BURNING_POINT  -> new AttributeModifier(ResourceLocation.fromNamespaceAndPath(ColdSweat.MOD_ID, "burning_point_modifier"), value, operation);
            case HEAT_RESISTANCE -> new AttributeModifier(ResourceLocation.fromNamespaceAndPath(ColdSweat.MOD_ID, "heat_resistance_modifier"), value, operation);
            case COLD_RESISTANCE -> new AttributeModifier(ResourceLocation.fromNamespaceAndPath(ColdSweat.MOD_ID, "cold_resistance_modifier"), value, operation);
            case HEAT_DAMPENING  -> new AttributeModifier(ResourceLocation.fromNamespaceAndPath(ColdSweat.MOD_ID, "heat_dampening_modifier"), value, operation);
            case COLD_DAMPENING  -> new AttributeModifier(ResourceLocation.fromNamespaceAndPath(ColdSweat.MOD_ID, "cold_dampening_modifier"), value, operation);
            default -> throw ColdSweat.LOGGER.throwing(new IllegalArgumentException("\"" + trait + "\" is not a valid trait!"));
        };
    }

    public static boolean isTemperatureAttribute(Attribute attribute)
    {
        return CSMath.containsAny(BuiltInRegistries.ATTRIBUTE.getKey(attribute).toString(),
                                  Arrays.stream(EntityTempManager.VALID_ATTRIBUTE_TYPES)
                                        .map(Temperature.Trait::getSerializedName).toArray(String[]::new));
    }
}