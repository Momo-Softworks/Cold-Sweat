package com.momosoftworks.coldsweat.common.capability.handler;

import com.google.common.collect.ImmutableSet;
import com.mojang.datafixers.util.Either;
import com.mojang.datafixers.util.Pair;
import com.momosoftworks.coldsweat.ColdSweat;
import com.momosoftworks.coldsweat.api.event.core.init.DefaultTempModifiersEvent;
import com.momosoftworks.coldsweat.api.event.vanilla.ContainerChangedEvent;
import com.momosoftworks.coldsweat.api.event.common.temperautre.TempModifierEvent;
import com.momosoftworks.coldsweat.api.event.core.init.GatherDefaultTempModifiersEvent;
import com.momosoftworks.coldsweat.api.registry.TempModifierRegistry;
import com.momosoftworks.coldsweat.api.temperature.modifier.*;
import com.momosoftworks.coldsweat.api.util.Temperature;
import com.momosoftworks.coldsweat.api.util.Placement;
import com.momosoftworks.coldsweat.api.util.Placement.Mode;
import com.momosoftworks.coldsweat.api.util.Placement.Order;
import com.momosoftworks.coldsweat.common.capability.ModCapabilities;
import com.momosoftworks.coldsweat.common.capability.SidedCapabilityCache;
import com.momosoftworks.coldsweat.common.capability.temperature.EntityTempCap;
import com.momosoftworks.coldsweat.common.capability.temperature.ITemperatureCap;
import com.momosoftworks.coldsweat.common.capability.temperature.PlayerTempCap;
import com.momosoftworks.coldsweat.config.ConfigSettings;
import com.momosoftworks.coldsweat.config.ModUpdater;
import com.momosoftworks.coldsweat.core.event.TaskScheduler;
import com.momosoftworks.coldsweat.core.init.ParticleTypesInit;
import com.momosoftworks.coldsweat.data.codec.configuration.FoodData;
import com.momosoftworks.coldsweat.data.codec.configuration.InsulatorData;
import com.momosoftworks.coldsweat.data.codec.configuration.ItemCarryTempData;
import com.momosoftworks.coldsweat.data.codec.configuration.MountData;
import com.momosoftworks.coldsweat.data.codec.configuration.ItemCarryTempData.SlotType;
import com.momosoftworks.coldsweat.compat.CompatManager;
import com.momosoftworks.coldsweat.data.tag.ModEntityTags;
import com.momosoftworks.coldsweat.util.entity.DummyPlayer;
import com.momosoftworks.coldsweat.util.math.CSMath;
import com.momosoftworks.coldsweat.util.math.FastMap;
import com.momosoftworks.coldsweat.util.registries.ModAttributes;
import com.momosoftworks.coldsweat.util.registries.ModBlocks;
import com.momosoftworks.coldsweat.util.registries.ModEffects;
import com.momosoftworks.coldsweat.util.registries.ModItems;
import com.momosoftworks.coldsweat.util.world.WorldHelper;
import net.minecraft.enchantment.EnchantmentHelper;
import net.minecraft.entity.Entity;
import net.minecraft.entity.EntityType;
import net.minecraft.entity.LivingEntity;
import net.minecraft.entity.ai.attributes.Attribute;
import net.minecraft.entity.ai.attributes.AttributeModifier;
import net.minecraft.entity.ai.attributes.ModifiableAttributeInstance;
import net.minecraft.entity.item.minecart.MinecartEntity;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.inventory.EquipmentSlotType;
import net.minecraft.inventory.container.*;
import net.minecraft.item.Item;
import net.minecraft.item.ItemStack;
import net.minecraft.item.TridentItem;
import net.minecraft.item.UseAction;
import net.minecraft.nbt.CompoundNBT;
import net.minecraft.potion.EffectInstance;
import net.minecraft.util.Direction;
import net.minecraft.util.NonNullList;
import net.minecraft.util.ResourceLocation;
import net.minecraft.world.Difficulty;
import net.minecraftforge.common.MinecraftForge;
import net.minecraftforge.common.capabilities.Capability;
import net.minecraftforge.common.capabilities.ICapabilityProvider;
import net.minecraftforge.common.capabilities.ICapabilitySerializable;
import net.minecraftforge.common.util.LazyOptional;
import net.minecraftforge.event.AttachCapabilitiesEvent;
import net.minecraftforge.event.TickEvent;
import net.minecraftforge.event.entity.EntityJoinWorldEvent;
import net.minecraftforge.event.entity.EntityLeaveWorldEvent;
import net.minecraftforge.event.entity.living.*;
import net.minecraftforge.event.entity.player.PlayerEvent;
import net.minecraftforge.event.world.SleepFinishedTimeEvent;
import net.minecraftforge.eventbus.api.EventPriority;
import net.minecraftforge.eventbus.api.SubscribeEvent;
import net.minecraftforge.fml.common.Mod;
import net.minecraftforge.fml.common.ObfuscationReflectionHelper;
import net.minecraftforge.registries.ForgeRegistries;

import javax.annotation.Nonnull;
import javax.annotation.Nullable;
import java.lang.reflect.Field;
import java.util.*;
import java.util.concurrent.atomic.AtomicBoolean;
import java.util.function.Function;
import java.util.stream.Collectors;

import static com.momosoftworks.coldsweat.api.util.Temperature.Trait;

@Mod.EventBusSubscriber
public class EntityTempManager
{
    public static final Trait[] VALID_TEMPERATURE_TRAITS = Arrays.stream(Trait.values()).filter(Trait::isForTemperature).toArray(Trait[]::new);
    public static final Trait[] VALID_MODIFIER_TRAITS = Arrays.stream(Trait.values()).filter(Trait::isForModifiers).toArray(Trait[]::new);
    public static final Trait[] VALID_ATTRIBUTE_TRAITS = Arrays.stream(Trait.values()).filter(Trait::isForAttributes).toArray(Trait[]::new);

    public static final Set<EntityType<? extends LivingEntity>> TEMPERATURE_ENABLED_ENTITIES = new HashSet<>(Arrays.asList(EntityType.PLAYER));

    public static SidedCapabilityCache<ITemperatureCap, Entity> CAP_CACHE = new SidedCapabilityCache<>(() -> ModCapabilities.ENTITY_TEMPERATURE, ent -> ent.removed);
    public static Map<Entity, Map<ResourceLocation, Double>> TEMP_MODIFIER_IMMUNITIES = new WeakHashMap<>();

    public static LazyOptional<ITemperatureCap> getTemperatureCap(Entity entity)
    {   return isTemperatureEnabled(entity) ? CAP_CACHE.get(entity) : LazyOptional.empty();
    }

    /**
     * Attach temperature capability to entities
     */
    @SubscribeEvent
    public static void attachCapabilityToEntityHandler(AttachCapabilitiesEvent<Entity> event)
    {
        if (event.getObject() instanceof LivingEntity && TEMPERATURE_ENABLED_ENTITIES.contains(event.getObject().getType()))
        {
            LivingEntity entity = (LivingEntity) event.getObject();
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
                    if (cap == ModCapabilities.ENTITY_TEMPERATURE)
                    {   return capOptional.cast();
                    }
                    return LazyOptional.empty();
                }

                @Override
                public CompoundNBT serializeNBT()
                {   return tempCap.serializeNBT();
                }

                @Override
                public void deserializeNBT(CompoundNBT nbt)
                {   tempCap.deserializeNBT(nbt);
                }
            };

            // Attach the capability to the entity
            event.addCapability(new ResourceLocation(ColdSweat.MOD_ID, "temperature"), provider);
        }
    }

    @SubscribeEvent(priority = EventPriority.HIGHEST)
    public static void handleModUpdates(EntityJoinWorldEvent event)
    {
        Entity entity = event.getEntity();
        if (isTemperatureEnabled(entity) && entity instanceof LivingEntity)
        {   ModUpdater.updateEntity((LivingEntity) entity);
        }
    }

    /**
     * Add default modifiers to players and temperature-enabled entities
     */
    @SubscribeEvent(priority = EventPriority.HIGHEST)
    public static void defineDefaultModifiers(DefaultTempModifiersEvent event)
    {
        LivingEntity entity = event.getEntity();
        boolean isPlayer = entity instanceof PlayerEntity;
        boolean isTempSensitive = entity.getType().is(ModEntityTags.TEMPERATURE_SENSITIVE);

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
                event.addModifier(Arrays.asList(Trait.WORLD, Trait.FREEZING_POINT, Trait.BURNING_POINT),
                                  new EntityClimateTempModifier().tickRate(200),
                                  Placement.Duplicates.BY_CLASS, Placement.BEFORE_FIRST);
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

        event.addModifier(Trait.WORLD, new BiomeTempModifier(isPlayer ? 49 : isTempSensitive ? 16 : 9).tickRate(mediumTickRate),
                          Placement.Duplicates.BY_CLASS, Placement.BEFORE_FIRST);

        event.addModifier(Trait.WORLD, new ElevationTempModifier(isPlayer ? 49 : isTempSensitive ? 16 : 1).tickRate(mediumTickRate),
                          Placement.Duplicates.BY_CLASS, Placement.of(Mode.AFTER, Order.FIRST, mod -> mod instanceof BiomeTempModifier));

        event.addModifier(Trait.WORLD, new BlockTempModifier(isPlayer ? -1 : 4).tickRate(fastTickRate),
                          Placement.Duplicates.BY_CLASS, Placement.AFTER_LAST);

        event.addModifier(Trait.WORLD, new EntitiesTempModifier().tickRate(mediumTickRate2),
                          Placement.Duplicates.BY_CLASS, Placement.AFTER_LAST);

        // Serene Seasons compat
        event.addModifierById(Trait.WORLD, new ResourceLocation("sereneseasons:season"),
                              mod -> mod.tickRate(slowTickRate),
                              Placement.Duplicates.BY_CLASS,
                              Placement.of(Mode.BEFORE, Order.FIRST, mod2 -> mod2 instanceof ElevationTempModifier));
        // Weather2 Compat
        event.addModifierById(Trait.WORLD, new ResourceLocation("weather2:storm"),
                              mod -> mod.tickRate(slowTickRate),
                              Placement.Duplicates.BY_CLASS,
                              Placement.of(Mode.BEFORE, Order.FIRST, mod2 -> mod2 instanceof ElevationTempModifier));
        // Valkyrien Skies Compat
        event.addModifierById(Trait.WORLD, new ResourceLocation("valkyrienskies:ship_blocks"),
                              mod -> mod.tickRate(mediumTickRate2),
                              Placement.Duplicates.BY_CLASS,
                              Placement.of(Mode.AFTER, Order.FIRST, mod2 -> mod2 instanceof BlockTempModifier));

        if (isPlayer && !(entity instanceof DummyPlayer))
        {
            event.addModifier(Arrays.asList(Trait.FREEZING_POINT, Trait.BURNING_POINT), new AcclimationTempModifier().tickRate(20), Placement.Duplicates.BY_CLASS, Placement.AFTER_LAST);
            event.addModifier(Arrays.asList(VALID_MODIFIER_TRAITS), new InventoryItemsTempModifier().tickRate(5), Placement.Duplicates.BY_CLASS, Placement.AFTER_LAST);
        }
    }

    /**
     * Add modifiers to the player and valid entities when they join the world
     */
    @SubscribeEvent
    public static void initModifiersOnEntity(EntityJoinWorldEvent event)
    {
        if (event.getEntity() instanceof LivingEntity && !event.getWorld().isClientSide()
        && isTemperatureEnabled(event.getEntity()))
        {
            LivingEntity living = (LivingEntity) event.getEntity();
            getTemperatureCap(living).ifPresent(cap ->
            {
                // Add default modifiers every time the entity joins the world
                Map<Trait, List<TempModifier>> modifiers = gatherTempModifiers(living);
                cap.clearModifiers();
                cap.setModifiers(modifiers);
                TaskScheduler.scheduleServer(() ->
                {   cap.tick(living);
                    Temperature.updateTemperature(living, cap, true);
                    Temperature.updateModifiers(living, cap);
                }, 1);
            });
        }
    }

    public static Map<Trait, List<TempModifier>> gatherTempModifiers(LivingEntity entity)
    {
        DefaultTempModifiersEvent modifiersEvent = new DefaultTempModifiersEvent(entity);
        MinecraftForge.EVENT_BUS.post(modifiersEvent);
        Map<Trait, List<TempModifier>> modifiers = modifiersEvent.getModifiers();

        /* DEPRECATED. Will be removed in a future version. */
        for (Trait trait : VALID_MODIFIER_TRAITS)
        {
            GatherDefaultTempModifiersEvent gatherEvent = new GatherDefaultTempModifiersEvent(entity, modifiers.get(trait), trait);
            MinecraftForge.EVENT_BUS.post(gatherEvent);
            modifiers.put(trait, gatherEvent.getModifiers());
        }
        return modifiers;
    }

    @SubscribeEvent
    public static synchronized void cleanRemovedEntities(EntityLeaveWorldEvent event)
    {
        if (isTemperatureEnabled(event.getEntity()))
        {   TEMP_MODIFIER_IMMUNITIES.keySet().removeIf(ent -> ent.removed);
        }
    }

    /**
     * Tick TempModifiers and update temperature for living entities
     */
    @SubscribeEvent
    public static void tickTemperature(LivingEvent.LivingUpdateEvent event)
    {
        LivingEntity entity = event.getEntityLiving();
        if (!getEntitiesWithTemperature().contains(entity.getType())) return;

        getTemperatureCap(entity).ifPresent(cap ->
        {
            // Tick modifiers serverside
            if (!entity.level.isClientSide)
            {
                // Tick modifiers 1/4 as much for entities
                if (entity instanceof PlayerEntity || entity.tickCount % 5 == 0)
                {   cap.tick(entity);
                }
            }
            // Tick modifiers clientside
            else
            {   cap.tickDummy(entity);
            }

            // Tick modifiers & removed expired
            AtomicBoolean sync = new AtomicBoolean(false);
            for (Trait trait : VALID_MODIFIER_TRAITS)
            {
                List<TempModifier> modifiers = cap.getModifiers(trait);
                for (int i = 0; i < modifiers.size(); i++)
                {
                    TempModifier modifier = modifiers.get(i);
                    // Tick modifier
                    modifier.tick(entity);
                    // Sync if the modifier is dirty
                    if (modifier.isDirty())
                    {   sync.set(true);
                        modifier.markClean();
                    }
                    // Remove expired modifiers
                    int expireTime = modifier.getExpireTime();
                    boolean expired = (modifier.setTicksExisted(modifier.getTicksExisted() + 1) > expireTime && expireTime != -1);
                    if (expired)
                    {   cap.removeModifier(modifier, trait);
                        modifier.onRemoved(entity, trait);
                        Temperature.updateSiblingsRemove(modifiers, entity, trait, modifier);
                        i--;
                    }
                }
            }
            if (sync.get())
            {   Temperature.updateModifiers(entity, cap);
            }

            // Spawn particles for uninhabitable entities
            if (!entity.level.isClientSide() && hasClimateData(entity))
            {
                if (entity.tickCount % 5 == 0 && entity.getRandom().nextDouble() < 0.1)
                {
                    double worldTemp = cap.getTrait(Trait.WORLD);
                    double entityX = entity.getX();
                    double entityY = entity.getY() + entity.getBbHeight();
                    double entityZ = entity.getZ();

                    if (worldTemp < cap.getTrait(Trait.FREEZING_POINT))
                    {
                        WorldHelper.spawnParticleBatch(entity.level, ParticleTypesInit.MOB_COLD.get(), entityX, entityY, entityZ, 0.5, 0.5, 0.5,
                                                       entity.getRandom().nextInt(2) + 2, 0);
                    }
                    else if (worldTemp > cap.getTrait(Trait.BURNING_POINT))
                    {
                        WorldHelper.spawnParticleBatch(entity.level, ParticleTypesInit.MOB_HOT.get(), entityX, entityY, entityZ, 0.5, 0.5, 0.5,
                                                       entity.getRandom().nextInt(2) + 2, 0);
                    }
                }
            }
        });
    }

    @SubscribeEvent
    public static void tickInventoryTempItems(LivingEvent.LivingUpdateEvent event)
    {
        LivingEntity entity = event.getEntityLiving();
        if (entity.tickCount % 10 != 0 || !isTemperatureEnabled(entity)) return;

        Map<Temperature.Trait, Double> effectsPerTrait = new EnumMap<>(Temperature.Trait.class);
        Map<ItemCarryTempData, Double> effectsPerCarriedTemp = new FastMap<>();

        // Get temperature of equipped items
        for (EquipmentSlotType slot : EquipmentSlotType.values())
        {
            ItemStack stack = entity.getItemBySlot(slot);
            if (!stack.isEmpty())
            {
                Item item = stack.getItem();
                ConfigSettings.CARRIED_ITEM_TEMPERATURES.get().get(item).forEach(
                carried ->
                {   checkAndAddCarriedTemp(entity, stack, null, slot, carried, effectsPerCarriedTemp);
                });
            }
        }

        // Get temperature of main inventory items
        if (entity instanceof PlayerEntity)
        {
            PlayerEntity player = ((PlayerEntity) entity);
            for (Slot slot : player.inventoryMenu.slots)
            {
                ItemStack stack = slot.getItem();
                if (!stack.isEmpty())
                {
                    Item item = stack.getItem();
                    ConfigSettings.CARRIED_ITEM_TEMPERATURES.get().get(item).forEach(
                    carried ->
                    {   checkAndAddCarriedTemp(entity, stack, slot.index, null, carried, effectsPerCarriedTemp);
                    });
                }
            }
        }

        for (Map.Entry<ItemCarryTempData, Double> entry : effectsPerCarriedTemp.entrySet())
        {
            Temperature.Trait trait = entry.getKey().trait();
            double temp = entry.getValue();

            effectsPerTrait.put(trait, effectsPerTrait.get(trait) + temp);
        }

        effectsPerTrait.forEach((trait, temp) ->
        {
            Optional<InventoryItemsTempModifier> modifier = Temperature.getModifier(entity, trait, InventoryItemsTempModifier.class);
            modifier.ifPresent(mod -> mod.getNBT().putDouble("Effect", temp));
        });
    }

    private static void checkAndAddCarriedTemp(LivingEntity entity, ItemStack stack, Integer slot, EquipmentSlotType equipmentSlot,
                                               ItemCarryTempData carried, Map<ItemCarryTempData, Double> effectsPerCarriedTemp)
    {
        if (carried.test(entity, stack, slot, equipmentSlot))
        {
            double temp = carried.temperature() * stack.getCount();
            double currentEffect = effectsPerCarriedTemp.getOrDefault(carried, 0.0);
            double newEffect = Math.min(carried.maxEffect(), Math.abs(currentEffect + temp)) * CSMath.sign(currentEffect + temp);

            effectsPerCarriedTemp.put(carried, newEffect);
        }
    }
    /**
     * Transfer the player's capability when traveling from the End
     */
    @SubscribeEvent
    public static void carryOverPersistentAttributes(PlayerEvent.Clone event)
    {
        PlayerEntity oldPlayer = event.getOriginal();
        PlayerEntity newPlayer = event.getPlayer();

        if (!newPlayer.level.isClientSide)
        {
            // Get the old player's capability
            getTemperatureCap(oldPlayer).map(ITemperatureCap::getPersistentAttributes).orElse(new HashSet<>())
            .forEach(attr ->
            {
                ModifiableAttributeInstance newAttr = newPlayer.getAttribute(attr);
                ModifiableAttributeInstance oldAttr = oldPlayer.getAttribute(attr);
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
        PlayerEntity oldPlayer = event.getOriginal();
        PlayerEntity newPlayer = event.getPlayer();

        getTemperatureCap(newPlayer).ifPresent(cap ->
        {
            if (!event.isWasDeath())
            {   getTemperatureCap(oldPlayer).ifPresent(cap::copy);
            }
        });

        CAP_CACHE.remove(oldPlayer);
    }

    @SubscribeEvent
    public static void addInventoryListeners(EntityJoinWorldEvent event)
    {
        if (event.getEntity() instanceof PlayerEntity)
        {
            PlayerEntity player = ((PlayerEntity) event.getEntity());
            /*
            Add listener for granting the sewing table recipe when the player gets an insulation item
            */
            player.containerMenu.addSlotListener(new IContainerListener()
            {
                public void slotChanged(Container menu, int slotIndex, ItemStack stack)
                {
                    Slot slot = menu.getSlot(slotIndex);
                    if (!(slot instanceof CraftingResultSlot))
                    {
                        if (slot.container == player.inventory
                        && (ConfigSettings.INSULATION_ITEMS.get().containsKey(stack.getItem())))
                        {
                            player.awardRecipesByKey(new ResourceLocation[]{new ResourceLocation(ColdSweat.MOD_ID, "sewing_table")});
                        }
                    }
                }

                public void setContainerData(Container container, int slot, int value) {}

                public void refreshContainer(Container container, NonNullList<ItemStack> stacks) {}
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
    public static void calculateModifierImmunity(LivingEvent.LivingUpdateEvent event)
    {
        LivingEntity entity = event.getEntityLiving();
        if (!entity.level.isClientSide() && entity.tickCount % 20 == 0 && isTemperatureEnabled(entity))
        {
            Map<ResourceLocation, Double> immunities = new HashMap<>();
            for (Map.Entry<ItemStack, InsulatorData> entry : getInsulatorsOnEntity(entity).entrySet())
            {
                InsulatorData insulator = entry.getValue();
                ItemStack stack = entry.getKey();

                if (insulator.test(entity, stack))
                {   immunities.putAll(insulator.immuneTempModifiers());
                }
            }

            if (entity instanceof PlayerEntity)
            {
                // Get immunities from inventory items
                PlayerEntity player = (PlayerEntity) entity;
                for (Map.Entry<ItemStack, Pair<ItemCarryTempData, Either<Integer, ItemCarryTempData.SlotType>>> entry : getInventoryTemperaturesOnEntity(player).entrySet())
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
    public static void handleWaterFreezingFire(TickEvent.PlayerTickEvent event)
    {
        PlayerEntity player = event.player;

        // Water / Rain
        if (!player.level.isClientSide && event.phase == TickEvent.Phase.START)
        {
            if (player.tickCount % 5 == 0)
            {
                if (!player.isSpectator() && (WorldHelper.isInWater(player) || player.tickCount % 40 == 0
                && WorldHelper.isRainingAt(player.level, player.blockPosition())))
                {   Temperature.addModifier(player, new WaterTempModifier(0.01f).tickRate(5), Trait.WORLD, Placement.Duplicates.BY_CLASS);
                }

                if (player.isOnFire() && Temperature.hasModifier(player, Trait.WORLD, WaterTempModifier.class))
                {   player.clearFire();
                    Temperature.removeModifiers(player, Trait.WORLD, WaterTempModifier.class);
                }
            }
        }
    }

    @SubscribeEvent
    public static void onTridentUse(LivingEntityUseItemEvent.Stop event)
    {
        LivingEntity entity = event.getEntityLiving();
        ItemStack stack = event.getItem();

        if (!entity.level.isClientSide())
        {
            TaskScheduler.scheduleServer(() ->
            {
                if (stack.getItem() instanceof TridentItem && EnchantmentHelper.getRiptide(stack) > 0 && !entity.isInWaterOrBubble())
                {   Temperature.removeModifiers(entity, Trait.WORLD, WaterTempModifier.class);
                }
            }, 5);
        }
    }

    @SubscribeEvent
    public static void tickInventoryAttributeChanges(TickEvent.PlayerTickEvent event)
    {
        if (event.phase == TickEvent.Phase.START && event.player.tickCount % 20 == 0)
        {
            for (ItemStack item : event.player.inventory.items)
            {   updateInventoryTempAttributes(item, item, event.player);
            }
        }
    }

    @SubscribeEvent
    public static void updateInventoryAttributesOnSlotChange(ContainerChangedEvent event)
    {
        if (event.getContainer() instanceof PlayerContainer)
        {
            PlayerContainer inventory = (PlayerContainer) event.getContainer();
            updateInventoryTempAttributes(event.getOldStack(), event.getNewStack(), getOwner(inventory));
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

    private static final Field MENU_OWNER = ObfuscationReflectionHelper.findField(PlayerContainer.class, "field_82862_h");
    static { MENU_OWNER.setAccessible(true); }
    private static PlayerEntity getOwner(PlayerContainer menu)
    {
        try
        {   return (PlayerEntity) MENU_OWNER.get(menu);
        }
        catch (IllegalAccessException e)
        {   return null;
        }
    }

    @SubscribeEvent
    public static void tickInsulationAttributeChanges(LivingEvent.LivingUpdateEvent event)
    {
        LivingEntity entity = event.getEntityLiving();
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
        updateInsulationAttributeModifiers(event.getEntityLiving(), event.getFrom(), event.getTo());
        for (ItemStack armor : event.getEntity().getArmorSlots())
        {
            if (!armor.isEmpty())
            {   updateInsulationAttributeModifiers(event.getEntityLiving(), armor, armor);
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
     * Handle HearthTempModifier when the player has the Insulation effect
     */
    @SubscribeEvent
    public static void onInsulationAdded(PotionEvent.PotionAddedEvent event)
    {
        LivingEntity entity = event.getEntityLiving();
        EffectInstance effect = event.getPotionEffect();

        if (!entity.level.isClientSide && isTemperatureEnabled(entity)
        && (effect.getEffect() == ModEffects.FRIGIDNESS || effect.getEffect() == ModEffects.WARMTH))
        {
            boolean isWarmth = effect.getEffect() == ModEffects.WARMTH;
            int strength = effect.getAmplifier() + 1;
            // Add TempModifier on potion effect added
            ThermalSourceTempModifier newMod = (isWarmth ? new WarmthTempModifier(strength) : new FrigidnessTempModifier(strength)).expires(effect.getDuration());
            ThermalSourceTempModifier oldMod = Temperature.getModifier(entity, Trait.WORLD, ThermalSourceTempModifier.class).orElse(null);
            if (oldMod == null || oldMod.getStrength() <= strength)
            {   Temperature.addOrReplaceModifier(entity, newMod, Trait.WORLD, Placement.Duplicates.BY_CLASS);
            }
        }
    }

    @SubscribeEvent
    public static void onInsulationRemoved(PotionEvent.PotionRemoveEvent event)
    {
        LivingEntity entity = event.getEntityLiving();
        EffectInstance effect = event.getPotionEffect();

        if (effect != null && !entity.level.isClientSide && isTemperatureEnabled(entity)
        && (effect.getEffect() == ModEffects.FRIGIDNESS || effect.getEffect() == ModEffects.WARMTH))
        {
            Optional<ThermalSourceTempModifier> modifier = Temperature.getModifier(entity, Trait.WORLD, ThermalSourceTempModifier.class);
            if (modifier.isPresent())
            {
                boolean isWarmth = effect.getEffect() == ModEffects.WARMTH;
                CompoundNBT nbt = modifier.get().getNBT();

                if (isWarmth) nbt.putInt("Warming", 0);
                else nbt.putInt("Cooling", 0);
                if (isWarmth ? !entity.hasEffect(ModEffects.FRIGIDNESS) : !entity.hasEffect(ModEffects.WARMTH))
                {   Temperature.removeModifiers(entity, Trait.WORLD, mod -> mod instanceof ThermalSourceTempModifier);
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
        if (!event.getWorld().isClientSide())
        {
            event.getWorld().players().forEach(player ->
            {
                if (player.isSleeping())
                {
                    // Divide the player's current temperature by 4
                    double temp = Temperature.get(player, Trait.CORE);
                    Temperature.set(player, Trait.CORE, temp / 4f);
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
            PlayerEntity player = event.player;
            if (player.getVehicle() != null)
            {
                Entity mount = player.getVehicle();
                // If insulated minecart
                if (mount instanceof MinecartEntity && ((MinecartEntity) mount).getDisplayBlockState().getBlock() == ModBlocks.MINECART_INSULATION)
                {   Temperature.addOrReplaceModifier(player, new MountTempModifier(1, 1).tickRate(5).expires(5), Trait.RATE, Placement.Duplicates.BY_CLASS);
                }
                // If insulated entity (defined in config)
                else
                {
                    MountData entityInsul = ConfigSettings.INSULATED_MOUNTS.get().get(mount.getType())
                                                  .stream().filter(mnt -> mnt.test(mount)).findFirst().orElse(null);
                    if (entityInsul != null)
                    {   Temperature.addOrReplaceModifier(player, new MountTempModifier(entityInsul.coldInsulation(), entityInsul.heatInsulation()).tickRate(5).expires(5), Trait.RATE, Placement.Duplicates.BY_CLASS);
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
        if (event.getEntity() instanceof PlayerEntity
        && (item.getUseAnimation() == UseAction.DRINK || item.getUseAnimation() == UseAction.EAT)
        && !event.getEntity().level.isClientSide)
        {
            PlayerEntity player = (PlayerEntity) event.getEntity();
            // If food item defined in config
            for (FoodData foodData : ConfigSettings.FOOD_TEMPERATURES.get().get(item.getItem()))
            {
                if (foodData != null && foodData.test(item))
                {
                    double temperature = foodData.temperature();
                    int duration = foodData.duration();
                    Trait trait = foodData.duration() > 0 ? Trait.BASE : Trait.CORE;
                    // Custom class for soul sprouts
                    FoodTempModifier foodModifier = item.getItem() == ModItems.SOUL_SPROUT
                                                    ? new SoulSproutTempModifier(temperature)
                                                    : new FoodTempModifier(temperature);
                    // Store the duration of the TempModifier
                    foodModifier.getNBT().putString("item", ForgeRegistries.ITEMS.getKey(item.getItem()).toString());
                    foodModifier.getNBT().putInt("duration", duration);
                    // Add the TempModifier
                    Temperature.addOrReplaceModifier(player, foodModifier.expires(duration).tickRate(duration), trait, Placement.Duplicates.EXACT);
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
    {   return entity.level.getDifficulty() == Difficulty.PEACEFUL && ConfigSettings.USE_PEACEFUL_MODE.get();
    }

    public static Map<ItemStack, InsulatorData> getInsulatorsOnEntity(LivingEntity entity)
    {
        Map<ItemStack, InsulatorData> insulators = new HashMap<>();
        for (EquipmentSlotType slot : EquipmentSlotType.values())
        {
            if (slot.getType() != EquipmentSlotType.Group.ARMOR) continue;
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

    public static Map<ItemStack, Pair<ItemCarryTempData, Either<Integer, SlotType>>> getInventoryTemperaturesOnEntity(PlayerEntity player)
    {
        Map<ItemStack, Pair<ItemCarryTempData, Either<Integer, SlotType>>> tempItems = new HashMap<>();
        /*
         Inventory items
         */
        for (int i = 0; i < player.inventory.items.size(); i++)
        {
            ItemStack stack = player.inventory.items.get(i);
            if (stack.isEmpty()) continue;
            int slotIndex = i;
            ConfigSettings.CARRIED_ITEM_TEMPERATURES.get().get(stack.getItem()).forEach(temp ->
            {   tempItems.put(stack, Pair.of(temp, Either.left(slotIndex)));
            });
        }
        /*
         Armor items
         */
        for (EquipmentSlotType slot : EquipmentSlotType.values())
        {
            if (slot.getType() != EquipmentSlotType.Group.ARMOR) continue;
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
     * Sets the corresponding attribute value for the given {@link Trait}.
     * @param trait the type or ability to get the attribute for
     */
    public static void setAttribute(Trait trait, LivingEntity entity, double value)
    {
        CSMath.doIfNotNull(getAttribute(trait, entity), att -> att.setBaseValue(value));
    }

    /**
     * Gets the corresponding attribute value for the given {@link Trait}.
     * @param trait the type or ability to get the attribute for
     */
    @Nullable
    public static ModifiableAttributeInstance getAttribute(Trait trait, LivingEntity entity)
    {
        switch (trait)
        {
            case WORLD : return entity.getAttribute(ModAttributes.WORLD_TEMPERATURE);
            case BASE  : return entity.getAttribute(ModAttributes.BASE_BODY_TEMPERATURE);
            case RATE  : return entity.getAttribute(ModAttributes.TEMP_RATE);
            case FREEZING_POINT  : return entity.getAttribute(ModAttributes.FREEZING_POINT);
            case BURNING_POINT   : return entity.getAttribute(ModAttributes.BURNING_POINT);
            case HEAT_RESISTANCE : return entity.getAttribute(ModAttributes.HEAT_RESISTANCE);
            case COLD_RESISTANCE : return entity.getAttribute(ModAttributes.COLD_RESISTANCE);
            case HEAT_DAMPENING  : return entity.getAttribute(ModAttributes.HEAT_DAMPENING);
            case COLD_DAMPENING  : return entity.getAttribute(ModAttributes.COLD_DAMPENING);

            default : throw ColdSweat.LOGGER.throwing(new IllegalArgumentException("\"" + trait + "\" is not a valid trait!"));
        }
    }

    public static Collection<AttributeModifier> getAllAttributeModifiers(LivingEntity entity, ModifiableAttributeInstance attribute, @Nullable AttributeModifier.Operation operation)
    {
        Collection<AttributeModifier> modifiers = new ArrayList<>(operation == null
                                                                  ? attribute.getModifiers()
                                                                  : attribute.getModifiers(operation));
        modifiers.addAll(getAllEquipmentAttributeModifiers(entity, attribute, operation));

        return modifiers;
    }

    public static Collection<AttributeModifier> getAllEquipmentAttributeModifiers(LivingEntity entity, ModifiableAttributeInstance attribute, @Nullable AttributeModifier.Operation operation)
    {
        Collection<AttributeModifier> modifiers = new ArrayList<>();

        for (EquipmentSlotType slot : EquipmentSlotType.values())
        {
            if (slot.getType() != EquipmentSlotType.Group.ARMOR) continue;
            ItemStack stack = entity.getItemBySlot(slot);
            if (!stack.isEmpty())
            {   modifiers.addAll(ItemInsulationManager.getAttributeModifiersForSlot(stack, attribute.getAttribute(), slot, operation, entity));
            }
        }
        return modifiers;
    }

    public static AttributeModifier makeAttributeModifier(Trait trait, double value, AttributeModifier.Operation operation)
    {
        if (!trait.isForAttributes())
        {   throw ColdSweat.LOGGER.throwing(new IllegalArgumentException("\"" + trait + "\" is not a valid trait!"));
        }
        return new AttributeModifier(String.format("%s temperature modifier", trait.getSerializedName()), value, operation);
    }

    public static boolean isTemperatureAttribute(Attribute attribute)
    {
        return ForgeRegistries.ATTRIBUTES.getKey(attribute).getNamespace().equals(ColdSweat.MOD_ID);
    }

    public static List<ModifiableAttributeInstance> getAllTemperatureAttributes(LivingEntity entity)
    {
        return Arrays.stream(VALID_ATTRIBUTE_TRAITS)
                     .map(trait -> getAttribute(trait, entity))
                     .filter(Objects::nonNull)
                     .collect(Collectors.toList());
    }

    public static List<TempModifier> getAllModifiers(LivingEntity entity)
    {
        List<TempModifier> allModifiers = new ArrayList<>();
        getTemperatureCap(entity).ifPresent(cap ->
        {
            for (Trait trait : VALID_MODIFIER_TRAITS)
            {   allModifiers.addAll(cap.getModifiers(trait));
            }
        });
        return allModifiers;
    }
}