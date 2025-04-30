package com.momosoftworks.coldsweat.common.capability.handler;

import com.google.common.collect.ImmutableSet;
import com.mojang.datafixers.util.Either;
import com.mojang.datafixers.util.Pair;
import com.momosoftworks.coldsweat.ColdSweat;
import com.momosoftworks.coldsweat.api.event.common.temperautre.TempModifierEvent;
import com.momosoftworks.coldsweat.api.event.core.init.GatherDefaultTempModifiersEvent;
import com.momosoftworks.coldsweat.api.event.vanilla.ContainerChangedEvent;
import com.momosoftworks.coldsweat.api.event.vanilla.LivingEntityLoadAdditionalEvent;
import com.momosoftworks.coldsweat.api.registry.TempModifierRegistry;
import com.momosoftworks.coldsweat.api.temperature.modifier.*;
import com.momosoftworks.coldsweat.api.util.Placement;
import com.momosoftworks.coldsweat.api.util.Placement.Mode;
import com.momosoftworks.coldsweat.api.util.Placement.Order;
import com.momosoftworks.coldsweat.api.util.Temperature;
import com.momosoftworks.coldsweat.common.capability.ModCapabilities;
import com.momosoftworks.coldsweat.common.capability.temperature.ITemperatureCap;
import com.momosoftworks.coldsweat.compat.CompatManager;
import com.momosoftworks.coldsweat.config.ConfigSettings;
import com.momosoftworks.coldsweat.core.event.TaskScheduler;
import com.momosoftworks.coldsweat.core.init.*;
import com.momosoftworks.coldsweat.data.codec.configuration.FoodData;
import com.momosoftworks.coldsweat.data.codec.configuration.InsulatorData;
import com.momosoftworks.coldsweat.data.codec.configuration.ItemCarryTempData;
import com.momosoftworks.coldsweat.data.codec.configuration.ItemCarryTempData.SlotType;
import com.momosoftworks.coldsweat.data.codec.configuration.MountData;
import com.momosoftworks.coldsweat.data.tag.ModEntityTags;
import com.momosoftworks.coldsweat.util.entity.DummyPlayer;
import com.momosoftworks.coldsweat.util.math.CSMath;
import com.momosoftworks.coldsweat.util.math.FastMap;
import com.momosoftworks.coldsweat.util.world.WorldHelper;
import net.minecraft.core.Holder;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.Difficulty;
import net.minecraft.world.effect.MobEffectInstance;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.entity.EquipmentSlot;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.ai.attributes.Attribute;
import net.minecraft.world.entity.ai.attributes.AttributeInstance;
import net.minecraft.world.entity.ai.attributes.AttributeModifier;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.entity.vehicle.Minecart;
import net.minecraft.world.inventory.*;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.TridentItem;
import net.minecraft.world.item.UseAnim;
import net.minecraft.world.item.enchantment.EnchantmentHelper;
import net.neoforged.bus.api.EventPriority;
import net.neoforged.bus.api.SubscribeEvent;
import net.neoforged.fml.common.EventBusSubscriber;
import net.neoforged.fml.util.ObfuscationReflectionHelper;
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
import java.lang.reflect.Field;
import java.util.*;
import java.util.concurrent.atomic.AtomicBoolean;
import java.util.concurrent.atomic.AtomicReference;
import java.util.function.Function;
import java.util.function.Predicate;

@EventBusSubscriber
public class EntityTempManager
{
    public static final Temperature.Trait[] VALID_TEMPERATURE_TRAITS = Arrays.stream(Temperature.Trait.values()).filter(Temperature.Trait::isForTemperature).toArray(Temperature.Trait[]::new);
    public static final Temperature.Trait[] VALID_MODIFIER_TRAITS = Arrays.stream(Temperature.Trait.values()).filter(Temperature.Trait::isForModifiers).toArray(Temperature.Trait[]::new);
    public static final Temperature.Trait[] VALID_ATTRIBUTE_TRAITS = Arrays.stream(Temperature.Trait.values()).filter(Temperature.Trait::isForAttributes).toArray(Temperature.Trait[]::new);

    public static final Set<EntityType<? extends LivingEntity>> TEMPERATURE_ENABLED_ENTITIES = new HashSet<>(ImmutableSet.<EntityType<? extends LivingEntity>>builder().add(EntityType.PLAYER).build());

    public static final Map<Entity, ITemperatureCap> SERVER_CAP_CACHE = new HashMap<>();
    public static final Map<Entity, ITemperatureCap> CLIENT_CAP_CACHE = new HashMap<>();
    public static Map<Entity, Map<ResourceLocation, Double>> TEMP_MODIFIER_IMMUNITIES = new WeakHashMap<>();

    public static Optional<ITemperatureCap> getTemperatureCap(Entity entity)
    {
        Map<Entity, ITemperatureCap> cache = entity.level().isClientSide ? CLIENT_CAP_CACHE : SERVER_CAP_CACHE;
        return Optional.ofNullable(cache.computeIfAbsent(entity, e -> e.getCapability(entity instanceof Player
                                                                                      ? ModCapabilities.PLAYER_TEMPERATURE
                                                                                      : ModCapabilities.ENTITY_TEMPERATURE)));
    }

    /**
     * Add modifiers to the player and valid entities when they join the world
     */
    @SubscribeEvent
    public static void initModifiersOnEntity(EntityJoinLevelEvent event)
    {
        if (event.getEntity() instanceof LivingEntity living && !living.level().isClientSide()
        && isTemperatureEnabled(living))
        {
            getTemperatureCap(living).ifPresent(cap ->
            {
                // Add default modifiers every time the entity joins the world
                Map<Temperature.Trait, List<TempModifier>> modifiers = gatherTempModifiers(living);
                cap.getModifiers().clear();
                cap.getModifiers().putAll(modifiers);
                TaskScheduler.scheduleServer(() ->
                {   cap.tick(living);
                    Temperature.updateTemperature(living, cap, true);
                }, 1);
            });
        }
    }

    public static Map<Temperature.Trait, List<TempModifier>> gatherTempModifiers(LivingEntity entity)
    {
        Map<Temperature.Trait, List<TempModifier>> modifiers = new EnumMap<>(Temperature.Trait.class);
        for (Temperature.Trait trait : VALID_MODIFIER_TRAITS)
        {
            GatherDefaultTempModifiersEvent gatherEvent = new GatherDefaultTempModifiersEvent(entity, trait);
            NeoForge.EVENT_BUS.post(gatherEvent);
            modifiers.put(trait, gatherEvent.getModifiers());
        }
        return modifiers;
    }

    @SubscribeEvent
    public static synchronized void finalizeEntities(EntityLeaveLevelEvent event)
    {
        if (isTemperatureEnabled(event.getEntity()))
        {
            Predicate<Map.Entry<Entity, ?>> removal = e -> e.getKey().isRemoved();
            SERVER_CAP_CACHE.entrySet().removeIf(removal);
            CLIENT_CAP_CACHE.entrySet().removeIf(removal);
            TEMP_MODIFIER_IMMUNITIES.entrySet().removeIf(removal);
            writeData(event.getEntity());
        }
    }

    @SubscribeEvent
    public static void fixOldAttributeData(LivingEntityLoadAdditionalEvent event)
    {
        if (isTemperatureEnabled(event.getEntity())
        && event.getNBT().getList("Attributes", 10).stream().anyMatch(attribute -> ((CompoundTag) attribute).getString("Name").equals("cold_sweat:world_temperature_offset")))
        {
            TaskScheduler.scheduleServer(() ->
            {
                for (Temperature.Trait attributeType : VALID_ATTRIBUTE_TRAITS)
                {
                    CSMath.doIfNotNull(getAttribute(attributeType, event.getEntity()),
                    attribute ->
                    {
                        attribute.removeModifiers();
                        attribute.setBaseValue(attribute.getAttribute().value().getDefaultValue());
                    });
                }
            }, 1);
        }
    }

    /**
     * Tick TempModifiers and update temperature for living entities
     */
    @SubscribeEvent
    public static void tickTemperature(EntityTickEvent.Pre event)
    {
        if (!(event.getEntity() instanceof LivingEntity entity) || !TEMPERATURE_ENABLED_ENTITIES.contains(entity.getType())) return;

        getTemperatureCap(entity).ifPresent(cap ->
        {
            // Tick modifiers serverside
            if (!entity.level().isClientSide)
            {
                // Tick modifiers 1/4 as much for entities
                if (entity instanceof Player || entity.tickCount % 5 == 0)
                {   cap.tick(entity);
                }
            }
            // Tick modifiers clientside
            else
            {   cap.tickDummy(entity);
            }

            // Remove expired modifiers
            AtomicBoolean sync = new AtomicBoolean(false);
            for (Temperature.Trait trait : VALID_MODIFIER_TRAITS)
            {
                cap.getModifiers(trait).removeIf(modifier ->
                {
                    int expireTime = modifier.getExpireTime();
                    if (modifier.isDirty())
                    {   sync.set(true);
                        modifier.markClean();
                    }
                    return (modifier.setTicksExisted(modifier.getTicksExisted() + 1) > expireTime && expireTime != -1);
                });
            }
            if (sync.get())
            {   Temperature.updateModifiers(entity, cap);
            }

            // Spawn particles for uninhabitable entities
            if (!entity.level().isClientSide() && hasClimateData(entity))
            {
                if (entity.tickCount % 5 == 0 && entity.getRandom().nextDouble() < 0.1)
                {
                    double worldTemp = cap.getTrait(Temperature.Trait.WORLD);
                    double entityX = entity.getX();
                    double entityY = entity.getY() + entity.getBbHeight();
                    double entityZ = entity.getZ();

                    if (worldTemp < cap.getTrait(Temperature.Trait.FREEZING_POINT))
                    {
                        WorldHelper.spawnParticleBatch(entity.level(), ModParticleTypes.MOB_COLD.get(), entityX, entityY, entityZ, 0.5, 0.5, 0.5,
                                                       entity.getRandom().nextInt(2, 4), 0);
                    }
                    else if (worldTemp > cap.getTrait(Temperature.Trait.BURNING_POINT))
                    {
                        WorldHelper.spawnParticleBatch(entity.level(), ModParticleTypes.MOB_HOT.get(), entityX, entityY, entityZ, 0.5, 0.5, 0.5,
                                                       entity.getRandom().nextInt(2, 4), 0);
                    }
                }
            }
        });
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
            getTemperatureCap(oldPlayer).map(ITemperatureCap::getPersistentAttributes).orElse(new HashSet<>())
            .forEach(attr ->
            {
                AttributeInstance newAttr = newPlayer.getAttribute(Holder.direct(attr));
                AttributeInstance oldAttr = oldPlayer.getAttribute(Holder.direct(attr));
                if (newAttr != null && oldAttr != null)
                {
                    newAttr.setBaseValue(oldAttr.getBaseValue());
                    getTemperatureCap(newPlayer).ifPresent(cap -> cap.markPersistentAttribute(attr));
                }
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

        getTemperatureCap(newPlayer).ifPresent(cap ->
        {
            if (!event.isWasDeath())
            {   getTemperatureCap(oldPlayer).ifPresent(cap::copy);
            }
        });
    }

    /**
     * Add default modifiers to players and temperature-enabled entities
     */
    @SubscribeEvent(priority = EventPriority.HIGHEST)
    public static void defineDefaultModifiers(GatherDefaultTempModifiersEvent event)
    {
        LivingEntity entity = event.getEntity();
        boolean isPlayer = entity instanceof Player;
        boolean isTempSensitive = entity.getType().is(ModEntityTags.TEMPERATURE_SENSITIVE);
        Temperature.Trait trait = event.getTrait();

        // Use a far more performant (less accurate) check for climate-enabled entities
        if (hasClimateData(entity))
        {
            boolean isAdvanced = ConfigSettings.ADVANCED_ENTITY_TEMPERATURE.get();
            boolean wasAdvanced = entity.getPersistentData().getBoolean("AdvancedTemperature");
            // Clear modifiers if the "Advanced" setting was changed
            if (isAdvanced != wasAdvanced)
            {   Temperature.getModifiers(entity).clear();
                entity.getPersistentData().putBoolean("AdvancedTemperature", isAdvanced);
            }
            // Use basic temp calculation if not advanced
            if (!isAdvanced)
            {
                if (trait.isForWorld())
                {   event.addModifier(new EntityClimateTempModifier().tickRate(200), Placement.Duplicates.BY_CLASS, Placement.BEFORE_FIRST);
                }
                return;
            }
        }

        // TempModifier tick rate is generally slower for entities than for players
        double tickMultiplier = isPlayer ? 1
                              : isTempSensitive ? 4
                              : 40;
        int slowTickRate = (int) Math.min(60 * tickMultiplier, 400);
        int mediumTickRate = (int) (10 * tickMultiplier * 2);
        int mediumTickRate2 = (int) (10 * tickMultiplier);
        int fastTickRate = (int) (5 * tickMultiplier);

        if (trait == Temperature.Trait.WORLD)
        {
            event.addModifier(new BiomeTempModifier(isPlayer ? 49 : isTempSensitive ? 16 : 9).tickRate(mediumTickRate),
                              Placement.Duplicates.BY_CLASS, Placement.BEFORE_FIRST);

            event.addModifier(new ElevationTempModifier(isPlayer ? 49 : isTempSensitive ? 16 : 1).tickRate(mediumTickRate),
                              Placement.Duplicates.BY_CLASS, Placement.of(Mode.AFTER, Order.FIRST, mod -> mod instanceof BiomeTempModifier));

            event.addModifier(new DepthBiomeTempModifier(isPlayer ? 6 : isTempSensitive ? 5 : 3).tickRate(mediumTickRate),
                              Placement.Duplicates.BY_CLASS, Placement.of(Mode.AFTER, Order.FIRST, mod -> mod instanceof ElevationTempModifier));

            event.addModifier(new BlockTempModifier(isPlayer ? -1 : 4).tickRate(fastTickRate),
                              Placement.Duplicates.BY_CLASS, Placement.AFTER_LAST);

            event.addModifier(new EntitiesTempModifier().tickRate(mediumTickRate2),
                              Placement.Duplicates.BY_CLASS, Placement.AFTER_LAST);

            // Serene Seasons compat
            event.addModifierById(ResourceLocation.parse("sereneseasons:season"),
                                  mod -> mod.tickRate(slowTickRate),
                                  Placement.Duplicates.BY_CLASS,
                                  Placement.of(Mode.BEFORE, Order.FIRST, mod2 -> mod2 instanceof ElevationTempModifier));
            // Weather2 Compat
            event.addModifierById(ResourceLocation.parse("weather2:storm"),
                                  mod -> mod.tickRate(slowTickRate),
                                  Placement.Duplicates.BY_CLASS,
                                  Placement.of(Mode.BEFORE, Order.FIRST, mod2 -> mod2 instanceof ElevationTempModifier));
        }
        else if (trait == Temperature.Trait.FREEZING_POINT || trait == Temperature.Trait.BURNING_POINT)
        {
            if (isPlayer) event.addModifier(new AcclimationTempModifier().tickRate(20), Placement.Duplicates.BY_CLASS, Placement.AFTER_LAST);
        }
        else if (trait == Temperature.Trait.ALL)
        {
            if (isPlayer) event.addModifier(new InventoryItemsTempModifier().tickRate(5), Placement.Duplicates.BY_CLASS, Placement.AFTER_LAST);
        }
    }

    @SubscribeEvent
    public static void addInventoryListeners(EntityJoinLevelEvent event)
    {
        if (event.getEntity() instanceof Player player)
        {
            /*
            Add listener for granting the sewing table recipe when the player gets an insulation item
            */
            player.containerMenu.addSlotListener(new ContainerListener()
            {
                public void slotChanged(AbstractContainerMenu menu, int slotIndex, ItemStack stack)
                {
                    Slot slot = menu.getSlot(slotIndex);
                    if (!(slot instanceof ResultSlot))
                    {
                        if (slot.container == player.getInventory()
                        && (ConfigSettings.INSULATION_ITEMS.get().containsKey(stack.getItem())))
                        {
                            player.awardRecipesByKey(List.of(ResourceLocation.fromNamespaceAndPath(ColdSweat.MOD_ID, "sewing_table")));
                        }
                    }
                }
                public void dataChanged(AbstractContainerMenu menu, int slot, int value) {}
            });
        }
    }

    @SubscribeEvent
    public static void cancelDisabledModifiers(TempModifierEvent.Calculate.Pre event)
    {
        TempModifier modifier = event.getModifier();

        ResourceLocation modifierKey = TempModifierRegistry.getKey(modifier);

        if (modifierKey != null && ConfigSettings.DISABLED_MODIFIERS.get().contains(modifierKey))
        {
            modifier.expires(0);
            event.setCanceled(true);
        }
    }

    @SubscribeEvent
    public static void calculateModifierImmunity(EntityTickEvent.Pre event)
    {
        if (event.getEntity() instanceof LivingEntity entity
        && !entity.level().isClientSide() && entity.tickCount % 20 == 0 && isTemperatureEnabled(entity))
        {
            Map<ResourceLocation, Double> immunities = new FastMap<>();
            for (Map.Entry<ItemStack, InsulatorData> entry : getInsulatorsOnEntity(entity).entrySet())
            {
                InsulatorData insulator = entry.getValue();
                ItemStack stack = entry.getKey();

                if (insulator.test(entity, stack))
                {   immunities.putAll(insulator.immuneTempModifiers());
                }
            }

            if (entity instanceof Player player)
            {
                // Get immunities from inventory items
                for (var entry : getInventoryTemperaturesOnEntity(player).entrySet())
                {
                    ItemCarryTempData invTemp = entry.getValue().getFirst();
                    ItemStack stack = entry.getKey();

                    if (entry.getValue().getSecond().map(slot -> invTemp.test(player, stack, slot, null),
                                                         slot -> invTemp.test(entity, stack, slot)))
                    {   immunities.putAll(invTemp.immuneTempModifiers());
                    }
                }
                // Get immunities from mount
                if (player.getVehicle() != null)
                {
                    for (MountData mountData : ConfigSettings.INSULATED_MOUNTS.get().get(player.getVehicle().getType()))
                    {   immunities.putAll(mountData.modifierImmunities());
                    }
                }
            }
            TEMP_MODIFIER_IMMUNITIES.put(entity, immunities);
        }
    }

    /**
     * Check the player's immunity level to temperature modifiers when they tick
     */
    @SubscribeEvent
    public static void checkModifierImmunity(TempModifierEvent.Calculate.Post event)
    {
        if (event.getEntity() instanceof DummyPlayer) return;
        if (!event.getTrait().isForAttributes()) return;

        TempModifier modifier = event.getModifier();
        ResourceLocation modifierKey = TempModifierRegistry.getKey(modifier);
        LivingEntity entity = event.getEntity();

        // Calculate modifier immunity from equipped insulators
        double immunity = TEMP_MODIFIER_IMMUNITIES.getOrDefault(entity, Collections.emptyMap()).getOrDefault(modifierKey, 0.0);
        if (immunity > 0)
        {
            Function<Double, Double> oldFunction = event.getFunction();
            event.setFunction(temp ->
            {
                double lastInput = modifier instanceof BiomeTempModifier ? Temperature.getNeutralWorldTemp(entity)
                                                                         : temp;
                return CSMath.blend(oldFunction.apply(temp), lastInput, immunity, 0, 1);
            });
        }
    }

    @SubscribeEvent
    public static void preventFullyImmuneModifiers(TempModifierEvent.Add event)
    {
        if (event.getEntity() instanceof DummyPlayer) return;
        if (!event.getTrait().isForAttributes()) return;

        TempModifier modifier = event.getModifier();
        ResourceLocation modifierKey = TempModifierRegistry.getKey(modifier);
        LivingEntity entity = event.getEntity();

        // Calculate modifier immunity from equipped insulators
        double immunity = TEMP_MODIFIER_IMMUNITIES.getOrDefault(entity, Collections.emptyMap()).getOrDefault(modifierKey, 0.0);
        if (immunity == 1)
        {   event.setCanceled(true);
        }
    }

    /**
     * Handle modifiers for freezing, burning, and being wet
     */
    @SubscribeEvent
    public static void handleWaterFreezingFire(PlayerTickEvent.Pre event)
    {
        Player player = event.getEntity();

        // Water / Rain
        if (!player.level().isClientSide)
        {
            if (player.tickCount % 5 == 0)
            {
                if (!player.isSpectator() && (WorldHelper.isInWater(player) || player.tickCount % 40 == 0
                && WorldHelper.isRainingAt(player.level(), player.blockPosition())))
                {   Temperature.addModifier(player, new WaterTempModifier(0.01f).tickRate(5), Temperature.Trait.WORLD, Placement.Duplicates.BY_CLASS);
                }

                if (player.isFreezing())
                {   Temperature.addOrReplaceModifier(player, new FreezingTempModifier(), Temperature.Trait.BASE, Placement.Duplicates.BY_CLASS);
                }

                if (player.isOnFire() && Temperature.hasModifier(player, Temperature.Trait.WORLD, WaterTempModifier.class))
                {   player.extinguishFire();
                    Temperature.removeModifiers(player, Temperature.Trait.WORLD, WaterTempModifier.class);
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
    public static void onTridentUse(LivingEntityUseItemEvent.Stop event)
    {
        LivingEntity entity = event.getEntity();
        ItemStack stack = event.getItem();

        if (!entity.level().isClientSide())
        {
            TaskScheduler.scheduleServer(() ->
            {
                if (stack.getItem() instanceof TridentItem && EnchantmentHelper.getTridentSpinAttackStrength(stack, entity) > 0 && !entity.isInWaterOrBubble())
                {   Temperature.removeModifiers(entity, Temperature.Trait.WORLD, WaterTempModifier.class);
                }
            }, 5);
        }
    }

    @SubscribeEvent
    public static void tickInventoryAttributeChanges(PlayerTickEvent.Pre event)
    {
        if (event.getEntity().tickCount % 20 == 0)
        {
            for (ItemStack item : event.getEntity().getInventory().items)
            {   updateInventoryTempAttributes(item, item, event.getEntity());
            }
        }
    }

    @SubscribeEvent
    public static void updateInventoryAttributesOnSlotChange(ContainerChangedEvent event)
    {
        if (event.getContainer() instanceof InventoryMenu inventory)
        {   updateInventoryTempAttributes(event.getOldStack(), event.getNewStack(), getOwner(inventory));
        }
    }

    private static void updateInventoryTempAttributes(ItemStack oldStack, ItemStack newStack, LivingEntity entity)
    {
        for (ItemCarryTempData carryTempData : ConfigSettings.CARRIED_ITEM_TEMPERATURES.get().get(oldStack.getItem()))
        {   entity.getAttributes().removeAttributeModifiers(carryTempData.attributeModifiers().getMap());
        }
        for (ItemCarryTempData carryTempData : ConfigSettings.CARRIED_ITEM_TEMPERATURES.get().get(newStack.getItem()))
        {
            if (carryTempData.test(entity, newStack))
            {   entity.getAttributes().addTransientAttributeModifiers(carryTempData.attributeModifiers().getMap());
            }
        }
    }

    private static final Field MENU_OWNER = ObfuscationReflectionHelper.findField(InventoryMenu.class, "owner");
    static { MENU_OWNER.setAccessible(true); }
    private static Player getOwner(InventoryMenu menu)
    {
        try
        {   return (Player) MENU_OWNER.get(menu);
        }
        catch (IllegalAccessException e)
        {   return null;
        }
    }

    @SubscribeEvent
    public static void tickInsulationAttributeChanges(EntityTickEvent.Pre event)
    {
        if (!(event.getEntity() instanceof LivingEntity entity)) return;
        if (entity.tickCount % 20 == 0)
        {
            for (ItemStack armor : entity.getArmorSlots())
            {
                if (!armor.isEmpty())
                {   updateInsulationAttributeModifiers(entity, armor, armor);
                }
            }
        }
    }

    @SubscribeEvent
    public static void updateInsulationAttributesOnEquipmentChange(LivingEquipmentChangeEvent event)
    {
        updateInsulationAttributeModifiers(event.getEntity(), event.getFrom(), event.getTo());
        for (ItemStack armor : event.getEntity().getArmorSlots())
        {
            if (!armor.isEmpty())
            {   updateInsulationAttributeModifiers(event.getEntity(), armor, armor);
            }
        }
    }

    public static void updateInsulationAttributeModifiers(LivingEntity entity, ItemStack from, ItemStack to)
    {
        for (InsulatorData insulatorData : ItemInsulationManager.getAllInsulatorsForStack(from))
        {   entity.getAttributes().removeAttributeModifiers(insulatorData.attributes().getMap());
        }
        for (InsulatorData insulatorData : ItemInsulationManager.getAllEffectiveInsulation(to, entity))
        {   entity.getAttributes().addTransientAttributeModifiers(insulatorData.attributes().getMap());
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
        LivingEntity entity = event.getEntity();
        MobEffectInstance effect = event.getEffectInstance();

        if (!entity.level().isClientSide && isTemperatureEnabled(entity)
        && (effect.getEffect() == ModEffects.FRIGIDNESS || effect.getEffect() == ModEffects.WARMTH))
        {
            boolean isWarmth = effect.getEffect() == ModEffects.WARMTH;
            int strength = effect.getAmplifier() + 1;
            // Add TempModifier on potion effect added
            ThermalSourceTempModifier newMod = (isWarmth ? new WarmthTempModifier(strength) : new FrigidnessTempModifier(strength)).expires(effect.getDuration());
            ThermalSourceTempModifier oldMod = Temperature.getModifier(entity, Temperature.Trait.WORLD, ThermalSourceTempModifier.class).orElse(null);
            if (oldMod == null || oldMod.getStrength() <= strength)
            {   Temperature.addOrReplaceModifier(entity, newMod, Temperature.Trait.WORLD, Placement.Duplicates.BY_CLASS);
            }
        }
    }

    @SubscribeEvent
    public static void onInsulationRemoved(MobEffectEvent.Remove event)
    {
        LivingEntity entity = event.getEntity();
        MobEffectInstance effect = event.getEffectInstance();

        if (effect != null && !entity.level().isClientSide && isTemperatureEnabled(entity)
        && (effect.getEffect() == ModEffects.FRIGIDNESS || effect.getEffect() == ModEffects.WARMTH))
        {
            Optional<ThermalSourceTempModifier> modifier = Temperature.getModifier(entity, Temperature.Trait.WORLD, ThermalSourceTempModifier.class);
            if (modifier.isPresent())
            {
                boolean isWarmth = effect.getEffect() == ModEffects.WARMTH;
                CompoundTag nbt = modifier.get().getNBT();

                if (isWarmth) nbt.putInt("Warming", 0);
                else nbt.putInt("Cooling", 0);
                if (isWarmth ? !entity.hasEffect(ModEffects.FRIGIDNESS) : !entity.hasEffect(ModEffects.WARMTH))
                {   Temperature.removeModifiers(entity, Temperature.Trait.WORLD, mod -> mod instanceof ThermalSourceTempModifier);
                }
            }
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
                    double temp = Temperature.get(player, Temperature.Trait.CORE);
                    Temperature.set(player, Temperature.Trait.CORE, temp / 4f);
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
                    MountData entityInsul = ConfigSettings.INSULATED_MOUNTS.get().get(mount.getType())
                                                  .stream().filter(mnt -> mnt.test(mount)).findFirst().orElse(null);
                    if (entityInsul != null)
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
        ItemStack item = event.getItem();
        if (event.getEntity() instanceof Player player
        && (item.getUseAnimation() == UseAnim.DRINK || item.getUseAnimation() == UseAnim.EAT)
        && !event.getEntity().level().isClientSide)
        {
            // If food item defined in config
            for (FoodData foodData : ConfigSettings.FOOD_TEMPERATURES.get().get(item.getItem()))
            {
                if (foodData != null && foodData.test(item))
                {
                    double temperature = foodData.temperature();
                    int duration = foodData.duration();
                    Temperature.Trait trait = foodData.duration() > 0 ? Temperature.Trait.BASE : Temperature.Trait.CORE;
                    // Custom class for soul sprouts
                    FoodTempModifier foodModifier = item.getItem() == ModItems.SOUL_SPROUT.value()
                                                    ? new SoulSproutTempModifier(temperature)
                                                    : new FoodTempModifier(temperature);
                    // Store the duration of the TempModifier
                    foodModifier.getNBT().putString("item", BuiltInRegistries.ITEM.getKey(item.getItem()).toString());
                    foodModifier.getNBT().putDouble("temperature", temperature);
                    foodModifier.getNBT().putInt("duration", duration);
                    // Add the TempModifier
                    Temperature.addOrReplaceModifier(player, foodModifier.expires(duration), trait, Placement.Duplicates.EXACT);
                }
            }
        }
    }

    public static Set<EntityType<? extends LivingEntity>> getEntitiesWithTemperature()
    {   return ImmutableSet.copyOf(TEMPERATURE_ENABLED_ENTITIES);
    }

    public static boolean isTemperatureEnabled(EntityType<?> type)
    {   return TEMPERATURE_ENABLED_ENTITIES.contains(type);
    }
    public static boolean isTemperatureEnabled(Entity entity)
    {   return TEMPERATURE_ENABLED_ENTITIES.contains(entity.getType());
    }
    public static boolean hasClimateData(EntityType<?> entity)
    {   return ConfigSettings.ENTITY_CLIMATES.get().containsKey(entity);
    }
    public static boolean hasClimateData(Entity entity)
    {   return ConfigSettings.ENTITY_CLIMATES.get().containsKey(entity.getType());
    }

    public static boolean isPeacefulMode(LivingEntity entity)
    {   return entity.level().getDifficulty() == Difficulty.PEACEFUL && ConfigSettings.USE_PEACEFUL_MODE.get();
    }

    public static Map<ItemStack, InsulatorData> getInsulatorsOnEntity(LivingEntity entity)
    {
        Map<ItemStack, InsulatorData> insulators = new HashMap<>();
        for (EquipmentSlot slot : EquipmentSlot.values())
        {
            if (!slot.isArmor()) continue;
            ItemStack stack = entity.getItemBySlot(slot);
            if (!stack.isEmpty())
            {
                ConfigSettings.INSULATING_ARMORS.get().get(stack.getItem()).forEach(insul -> insulators.put(stack, insul));
                ItemInsulationManager.getInsulationCap(stack).ifPresent(cap ->
                {
                    cap.getInsulation().stream().map(Pair::getFirst).forEach(item ->
                    {
                        ConfigSettings.INSULATION_ITEMS.get().get(item.getItem()).forEach(insul -> insulators.put(item, insul));
                    });
                });
            }
        }
        for (ItemStack curio : CompatManager.Curios.getCurios(entity))
        {   ConfigSettings.INSULATING_CURIOS.get().get(curio.getItem()).forEach(insul -> insulators.put(curio, insul));
        }
        return insulators;
    }

    public static Map<ItemStack, Pair<ItemCarryTempData, Either<Integer, SlotType>>> getInventoryTemperaturesOnEntity(Player player)
    {
        Map<ItemStack, Pair<ItemCarryTempData, Either<Integer, SlotType>>> tempItems = new HashMap<>();
        /*
         Inventory items
         */
        for (int i = 0; i < player.getInventory().items.size(); i++)
        {
            ItemStack stack = player.getInventory().items.get(i);
            if (stack.isEmpty()) continue;
            int slotIndex = i;
            ConfigSettings.CARRIED_ITEM_TEMPERATURES.get().get(stack.getItem()).forEach(temp ->
            {   tempItems.put(stack, Pair.of(temp, Either.left(slotIndex)));
            });
        }
        /*
         Armor items
         */
        for (EquipmentSlot slot : EquipmentSlot.values())
        {
            if (!slot.isArmor()) continue;
            ItemStack stack = player.getItemBySlot(slot);
            if (stack.isEmpty()) continue;
            SlotType slotType = SlotType.fromEquipment(slot);

            ConfigSettings.CARRIED_ITEM_TEMPERATURES.get().get(stack.getItem()).forEach(temp ->
            {   tempItems.put(stack, Pair.of(temp, Either.right(slotType)));
            });
        }
        /*
         Curios
         */
        for (ItemStack curio : CompatManager.Curios.getCurios(player))
        {
            ConfigSettings.CARRIED_ITEM_TEMPERATURES.get().get(curio.getItem()).forEach(temp ->
            {   tempItems.put(curio, Pair.of(temp, Either.right(SlotType.CURIO)));
            });
        }
        /*
         Offhand
         */
        ItemStack offhand = player.getOffhandItem();
        if (!offhand.isEmpty())
        {
            ConfigSettings.CARRIED_ITEM_TEMPERATURES.get().get(offhand.getItem()).forEach(temp ->
            {   tempItems.put(offhand, Pair.of(temp, Either.right(SlotType.HAND)));
            });
        }
        return tempItems;
    }

    /**
     * Sets the corresponding attribute value for the given {@link Temperature.Trait}.
     * @param trait the type or ability to get the attribute for
     */
    public static void setAttribute(Temperature.Trait trait, LivingEntity entity, double value)
    {
        CSMath.doIfNotNull(getAttribute(trait, entity), att -> att.setBaseValue(value));
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
            case RATE  -> entity.getAttribute(ModAttributes.TEMP_RATE);
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
            {   modifiers.addAll(ItemInsulationManager.getAttributeModifiersForSlot(stack, attribute.getAttribute(), slot, operation, entity));
            }
        }
        return modifiers;
    }

    public static AttributeModifier makeAttributeModifier(Temperature.Trait trait, double value, AttributeModifier.Operation operation)
    {
        if (!trait.isForAttributes())
        {   throw ColdSweat.LOGGER.throwing(new IllegalArgumentException("\"" + trait + "\" is not a valid trait!"));
        }
        return new AttributeModifier(ResourceLocation.fromNamespaceAndPath(ColdSweat.MOD_ID, trait.getSerializedName() + "_modifier"), value, operation);
    }

    public static boolean isTemperatureAttribute(Holder<Attribute> attribute)
    {
        return attribute.getKey().location().getNamespace().equals(ColdSweat.MOD_ID);
    }

    public static List<AttributeInstance> getAllTemperatureAttributes(LivingEntity entity)
    {
        return Arrays.stream(VALID_ATTRIBUTE_TRAITS)
                     .map(trait -> getAttribute(trait, entity))
                     .filter(Objects::nonNull)
                     .toList();
    }

    public static List<TempModifier> getAllModifiers(LivingEntity entity)
    {
        List<TempModifier> allModifiers = new ArrayList<>();
        getTemperatureCap(entity).ifPresent(cap ->
        {
            for (Temperature.Trait trait : VALID_MODIFIER_TRAITS)
            {   allModifiers.addAll(cap.getModifiers(trait));
            }
        });
        return allModifiers;
    }

    public static void writeData(Entity entity)
    {
        getTemperatureCap(entity).ifPresent(cap ->
        {   entity.getPersistentData().put("Temperature", cap.serializeNBT());
        });
    }
}