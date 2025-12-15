package com.momosoftworks.coldsweat.common.capability.handler;

import com.google.common.collect.ImmutableSet;
import com.mojang.datafixers.util.Pair;
import com.momosoftworks.coldsweat.ColdSweat;
import com.momosoftworks.coldsweat.api.event.core.init.DefaultTempModifiersEvent;
import com.momosoftworks.coldsweat.api.event.vanilla.ContainerChangedEvent;
import com.momosoftworks.coldsweat.api.event.common.temperautre.TempModifierEvent;
import com.momosoftworks.coldsweat.api.event.core.init.GatherDefaultTempModifiersEvent;
import com.momosoftworks.coldsweat.api.insulation.Insulation;
import com.momosoftworks.coldsweat.api.registry.TempModifierRegistry;
import com.momosoftworks.coldsweat.api.temperature.modifier.*;
import com.momosoftworks.coldsweat.api.util.placement.Matcher;
import com.momosoftworks.coldsweat.api.util.placement.Placement;
import com.momosoftworks.coldsweat.api.util.placement.Mode;
import com.momosoftworks.coldsweat.api.util.placement.Order;
import com.momosoftworks.coldsweat.api.util.Temperature;
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
import com.momosoftworks.coldsweat.data.codec.configuration.ItemTempData;
import com.momosoftworks.coldsweat.data.codec.configuration.MountData;
import com.momosoftworks.coldsweat.data.codec.configuration.ItemTempData.SlotType;
import com.momosoftworks.coldsweat.compat.CompatManager;
import com.momosoftworks.coldsweat.data.codec.impl.RequirementHolder;
import com.momosoftworks.coldsweat.data.tag.ModEntityTags;
import com.momosoftworks.coldsweat.util.entity.DummyPlayer;
import com.momosoftworks.coldsweat.util.math.CSMath;
import com.momosoftworks.coldsweat.util.registries.ModAttributes;
import com.momosoftworks.coldsweat.util.registries.ModBlocks;
import com.momosoftworks.coldsweat.util.registries.ModEffects;
import com.momosoftworks.coldsweat.util.registries.ModItems;
import com.momosoftworks.coldsweat.util.world.WorldHelper;
import net.minecraft.core.Direction;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.Difficulty;
import net.minecraft.world.effect.MobEffectInstance;
import net.minecraft.world.effect.MobEffects;
import net.minecraft.world.entity.*;
import net.minecraft.world.entity.ai.attributes.Attribute;
import net.minecraft.world.entity.ai.attributes.AttributeInstance;
import net.minecraft.world.entity.ai.attributes.AttributeModifier;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.entity.vehicle.Minecart;
import net.minecraft.world.inventory.*;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.TridentItem;
import net.minecraft.world.item.UseAnim;
import net.minecraft.world.item.enchantment.EnchantmentHelper;
import net.minecraftforge.common.MinecraftForge;
import net.minecraftforge.common.capabilities.Capability;
import net.minecraftforge.common.capabilities.ICapabilityProvider;
import net.minecraftforge.common.capabilities.ICapabilitySerializable;
import net.minecraftforge.common.util.LazyOptional;
import net.minecraftforge.event.AttachCapabilitiesEvent;
import net.minecraftforge.event.TickEvent;
import net.minecraftforge.event.entity.EntityJoinLevelEvent;
import net.minecraftforge.event.entity.EntityLeaveLevelEvent;
import net.minecraftforge.event.entity.living.*;
import net.minecraftforge.event.entity.player.PlayerEvent;
import net.minecraftforge.event.level.SleepFinishedTimeEvent;
import net.minecraftforge.eventbus.api.EventPriority;
import net.minecraftforge.eventbus.api.SubscribeEvent;
import net.minecraftforge.fml.common.Mod;
import net.minecraftforge.fml.util.ObfuscationReflectionHelper;
import net.minecraftforge.registries.ForgeRegistries;

import javax.annotation.Nonnull;
import javax.annotation.Nullable;
import java.lang.reflect.Field;
import java.util.*;
import java.util.concurrent.atomic.AtomicBoolean;
import java.util.concurrent.atomic.AtomicReference;
import java.util.function.Function;

import static com.momosoftworks.coldsweat.api.util.Temperature.Trait;

public class EntityTempManager
{
    public static final Trait[] VALID_TEMPERATURE_TRAITS = Arrays.stream(Trait.values()).filter(Trait::isForTemperature).toArray(Trait[]::new);
    public static final Trait[] VALID_MODIFIER_TRAITS = Arrays.stream(Trait.values()).filter(Trait::isForModifiers).toArray(Trait[]::new);
    public static final Trait[] VALID_ATTRIBUTE_TRAITS = Arrays.stream(Trait.values()).filter(Trait::isForAttributes).toArray(Trait[]::new);

    public static final Set<EntityType<? extends LivingEntity>> TEMPERATURE_ENABLED_ENTITIES = new HashSet<>(List.of(EntityType.PLAYER));

    public static SidedCapabilityCache<ITemperatureCap, Entity> CAP_CACHE = new SidedCapabilityCache<>(ModCapabilities.ENTITY_TEMPERATURE, Entity::isRemoved);
    public static Map<Entity, Map<ResourceLocation, Double>> TEMP_MODIFIER_IMMUNITIES = new WeakHashMap<>();

    @Mod.EventBusSubscriber
    public static class Events
    {
        /**
         * Attach temperature capability to entities
         */
        @SubscribeEvent
        public static void attachCapabilityToEntityHandler(AttachCapabilitiesEvent<Entity> event)
        {
            if (event.getObject() instanceof LivingEntity entity && TEMPERATURE_ENABLED_ENTITIES.contains(entity.getType()))
            {
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
                        if (cap == ModCapabilities.ENTITY_TEMPERATURE)
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

        @SubscribeEvent(priority = EventPriority.HIGHEST)
        public static void handleModUpdates(EntityJoinLevelEvent event)
        {
            Entity entity = event.getEntity();
            if (isTemperatureEnabled(entity) && entity instanceof LivingEntity)
            {   ModUpdater.updateEntity(((LivingEntity) entity));
            }
        }

        /**
         * Add default modifiers to players and temperature-enabled entities
         */
        @SubscribeEvent(priority = EventPriority.HIGHEST)
        public static void defineDefaultModifiers(DefaultTempModifiersEvent event)
        {
            LivingEntity entity = event.getEntity();
            boolean isPlayer = entity instanceof Player;
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
                    event.addModifier(List.of(Trait.WORLD, Trait.FREEZING_POINT, Trait.BURNING_POINT),
                                      new EntityClimateTempModifier().tickRate(200),
                                      Placement.FIRST.noDuplicates(Matcher.SAME_CLASS));
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
                              Placement.FIRST.noDuplicates(Matcher.SAME_CLASS));

            event.addModifier(Trait.WORLD, new ElevationTempModifier(isPlayer ? 49 : isTempSensitive ? 16 : 1).tickRate(mediumTickRate),
                              Placement.of(Mode.ADD_AFTER, Order.FIRST, mod -> mod instanceof BiomeTempModifier).noDuplicates(Matcher.SAME_CLASS));

            event.addModifier(Trait.WORLD, new ShadeTempModifier().tickRate(10),
                              Placement.of(Mode.ADD_BEFORE, Order.FIRST, mod -> mod instanceof ElevationTempModifier).noDuplicates(Matcher.SAME_CLASS));

            event.addModifier(Trait.WORLD, new CaveBiomeTempModifier(isPlayer ? 6 : isTempSensitive ? 5 : 3).tickRate(mediumTickRate),
                              Placement.of(Mode.ADD_AFTER, Order.FIRST, mod -> mod instanceof ElevationTempModifier).noDuplicates(Matcher.SAME_CLASS));

            event.addModifier(Trait.WORLD, new BlockTempModifier(isPlayer ? -1 : 4).tickRate(fastTickRate),
                              Placement.LAST.noDuplicates(Matcher.SAME_CLASS));

            event.addModifier(Trait.WORLD, new EntitiesTempModifier().tickRate(mediumTickRate2),
                              Placement.LAST.noDuplicates(Matcher.SAME_CLASS));

            // Serene Seasons compat
            event.addModifierById(Trait.WORLD, new ResourceLocation("sereneseasons:season"),
                                  mod -> mod.tickRate(slowTickRate),
                                  Placement.of(Mode.ADD_AFTER, Order.FIRST, mod2 -> mod2 instanceof BiomeTempModifier).noDuplicates(Matcher.SAME_CLASS));
            // Weather2 Compat
            event.addModifierById(Trait.WORLD, new ResourceLocation("weather2:storm"),
                                  mod -> mod.tickRate(slowTickRate),
                                  Placement.of(Mode.ADD_AFTER, Order.FIRST, mod2 -> mod2 instanceof BiomeTempModifier).noDuplicates(Matcher.SAME_CLASS));
            // Valkyrien Skies Compat
            event.addModifierById(Trait.WORLD, new ResourceLocation("valkyrienskies:ship_blocks"),
                                  mod -> mod.tickRate(mediumTickRate2),
                                  Placement.of(Mode.ADD_AFTER, Order.FIRST, mod2 -> mod2 instanceof BlockTempModifier).noDuplicates(Matcher.SAME_CLASS));
            // Ad Astra Compat
            event.addModifierById(Trait.WORLD, new ResourceLocation("ad_astra:oxygen"),
                                  mod -> mod.tickRate(mediumTickRate2),
                                  Placement.LAST.noDuplicates(Matcher.SAME_CLASS));

            if (isPlayer && !(entity instanceof DummyPlayer))
            {
                event.addModifier(List.of(Trait.FREEZING_POINT, Trait.BURNING_POINT), new AcclimationTempModifier().tickRate(20),
                                  Placement.LAST.noDuplicates(Matcher.SAME_CLASS));
                event.addModifier(Arrays.asList(VALID_MODIFIER_TRAITS), new InventoryItemsTempModifier().tickRate(5),
                                  Placement.LAST.noDuplicates(Matcher.SAME_CLASS));
            }
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

        @SubscribeEvent
        public static synchronized void cleanRemovedEntities(EntityLeaveLevelEvent event)
        {
            if (isTemperatureEnabled(event.getEntity()))
            {   TEMP_MODIFIER_IMMUNITIES.keySet().removeIf(Entity::isRemoved);
            }
        }

        /**
         * Tick TempModifiers and update temperature for living entities
         */
        @SubscribeEvent
        public static void tickTemperature(LivingEvent.LivingTickEvent event)
        {
            LivingEntity entity = event.getEntity();
            if (!TEMPERATURE_ENABLED_ENTITIES.contains(entity.getType())) return;

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

                // Tick modifiers & removed expired
                AtomicBoolean sync = new AtomicBoolean(false);
                for (Trait trait : VALID_MODIFIER_TRAITS)
                {
                    List<TempModifier> modifiers = cap.getModifiers(trait);
                    for (int i = 0; i < modifiers.size(); i++)
                    {
                        TempModifier modifier = modifiers.get(i);
                        // Tick modifier
                        if (modifier.getTicksExisted() % modifier.getTickRate() == 0)
                        {   modifier.tick(entity);
                        }
                        // Sync if the modifier is dirty
                        if (modifier.isDirty())
                        {   sync.set(true);
                            modifier.markClean();
                        }
                        // Remove expired modifiers
                        int expireTime = modifier.getExpireTime();
                        modifier.setTicksExisted(modifier.getTicksExisted() + 1);
                        if (modifier.getTicksExisted() > expireTime && expireTime != -1)
                        {
                            modifier.onRemoved(entity, trait);
                            Temperature.updateSiblingsRemove(modifiers, entity, trait, modifier);
                            modifiers.remove(i);
                            i--;
                        }
                    }
                }
                if (sync.get())
                {   Temperature.updateModifiers(entity, cap);
                }

                // Spawn particles for uninhabitable entities
                if (!entity.level().isClientSide() && hasClimateData(entity))
                {
                    if (entity.tickCount % 5 == 0 && entity.getRandom().nextDouble() < 0.1)
                    {
                        double worldTemp = cap.getTrait(Trait.WORLD);
                        double entityX = entity.getX();
                        double entityY = entity.getY() + entity.getBbHeight();
                        double entityZ = entity.getZ();

                        if (worldTemp < cap.getTrait(Trait.FREEZING_POINT))
                        {
                            WorldHelper.spawnParticleBatch(entity.level(), ParticleTypesInit.MOB_COLD.get(), entityX, entityY, entityZ, 0.5, 0.5, 0.5,
                                                           entity.getRandom().nextInt(2, 4), 0);
                        }
                        else if (worldTemp > cap.getTrait(Trait.BURNING_POINT))
                        {
                            WorldHelper.spawnParticleBatch(entity.level(), ParticleTypesInit.MOB_HOT.get(), entityX, entityY, entityZ, 0.5, 0.5, 0.5,
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
                oldPlayer.reviveCaps();
                getTemperatureCap(oldPlayer).map(ITemperatureCap::getPersistentAttributes).orElse(new HashSet<>())
                .forEach(attr ->
                {
                    AttributeInstance newAttr = newPlayer.getAttribute(attr);
                    AttributeInstance oldAttr = oldPlayer.getAttribute(attr);
                    if (newAttr != null && oldAttr != null)
                    {
                        newAttr.setBaseValue(oldAttr.getBaseValue());
                        getTemperatureCap(newPlayer).ifPresent(cap -> cap.markPersistentAttribute(attr));
                    }
                });
                oldPlayer.invalidateCaps();
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
                {
                    oldPlayer.reviveCaps();
                    getTemperatureCap(oldPlayer).ifPresent(cap::copy);
                    oldPlayer.invalidateCaps();
                }
            });

            CAP_CACHE.remove(oldPlayer);
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
                                player.awardRecipesByKey(new ResourceLocation[]{new ResourceLocation(ColdSweat.MOD_ID, "sewing_table")});
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
        public static void calculateModifierImmunity(LivingEvent.LivingTickEvent event)
        {
            LivingEntity entity = event.getEntity();
            if (!entity.level().isClientSide() && entity.tickCount % 20 == 0 && isTemperatureEnabled(entity))
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

                if (entity instanceof Player player)
                {
                    // Get immunities from inventory items
                    for (Map.Entry<ItemStack, ItemTempData> entry : getItemTemperaturesOnEntity(player).entrySet())
                    {   immunities.putAll(entry.getValue().immuneTempModifiers());
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
            Player player = event.player;

            // Water / Rain
            if (!player.level().isClientSide && event.phase == TickEvent.Phase.START)
            {
                if (player.tickCount % 5 == 0)
                {
                    if (!player.isSpectator() && (WorldHelper.isInWater(player) || player.tickCount % 40 == 0
                    && WorldHelper.isRainingAt(player.level(), player.blockPosition())))
                    {   Temperature.addModifier(player, new WaterTempModifier().tickRate(5), Trait.WORLD, Placement.LAST.noDuplicates(Matcher.SAME_CLASS));
                    }

                    if (player.isFreezing())
                    {   Temperature.replaceOrAddModifier(player, new FreezingTempModifier(), Trait.BASE, Matcher.SAME_CLASS);
                    }
                }

                if (player.isFreezing() && player.getTicksFrozen() > 0)
                {
                    AtomicReference<Double> insulation = new AtomicReference<>((double) 0);
                    boolean hasIcePotion = player.hasEffect(ModEffects.ICE_RESISTANCE) && ConfigSettings.ICE_RESISTANCE_ENABLED.get();

                    if (!hasIcePotion)
                    {
                        Temperature.getModifier(player, Trait.RATE, ArmorInsulationTempModifier.class).ifPresent(insulModifier ->
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
                for (ItemStack item : event.player.getInventory().items)
                {   updateInventoryTempAttributes(item, item, event.player);
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

        @SubscribeEvent
        public static void tickInsulationAttributeChanges(LivingEvent.LivingTickEvent event)
        {
            LivingEntity entity = event.getEntity();
            if (entity.tickCount % 20 == 0)
            {
                for (ItemStack armor : entity.getArmorSlots())
                {
                    if (!armor.isEmpty())
                    {   updateInsulationAttributeModifiers(entity, armor, armor, Insulation.Slot.ARMOR);
                    }
                }
            }
        }

        @SubscribeEvent
        public static void updateInsulationAttributesOnEquipmentChange(LivingEquipmentChangeEvent event)
        {
            updateInsulationAttributeModifiers(event.getEntity(), event.getFrom(), event.getTo(), Insulation.Slot.ARMOR);
            for (ItemStack armor : event.getEntity().getArmorSlots())
            {
                if (!armor.isEmpty())
                {   updateInsulationAttributeModifiers(event.getEntity(), armor, armor, Insulation.Slot.ARMOR);
                }
            }
        }

        /**
         * Cancel freezing damage when the player has the Ice Resistance effect
         */
        @SubscribeEvent
        public static void cancelFreezingDamage(LivingAttackEvent event)
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
                ThermalSourceTempModifier oldMod = Temperature.getModifier(entity, Trait.WORLD, ThermalSourceTempModifier.class).orElse(null);
                if (oldMod == null || oldMod.getStrength() <= strength)
                {
                    Temperature.removeModifiers(entity, Trait.WORLD, newMod.getClass());
                    Temperature.addModifier(entity, newMod, Trait.WORLD, Placement.LAST.noDuplicates(Matcher.SAME_CLASS));
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
                Optional<ThermalSourceTempModifier> modifier = Temperature.getModifier(entity, Trait.WORLD, ThermalSourceTempModifier.class);
                if (modifier.isPresent())
                {
                    boolean isWarmth = effect.getEffect() == ModEffects.WARMTH;
                    CompoundTag nbt = modifier.get().getNBT();

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
            if (!event.getLevel().isClientSide())
            {
                event.getLevel().players().forEach(player ->
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
            if (event.phase == TickEvent.Phase.START && !event.player.level().isClientSide() && event.player.tickCount % 5 == 0)
            {
                Player player = event.player;
                if (player.getVehicle() != null)
                {
                    Entity mount = player.getVehicle();
                    // If insulated minecart
                    if (mount instanceof Minecart minecart && minecart.getDisplayBlockState().getBlock() == ModBlocks.MINECART_INSULATION)
                    {   Temperature.replaceOrAddModifier(player, new MountTempModifier(1, 1).tickRate(5).expires(5), Trait.RATE, Matcher.SAME_CLASS);
                    }
                    // If insulated entity (defined in config)
                    else
                    {
                        MountData entityInsul = ConfigSettings.INSULATED_MOUNTS.get().get(mount.getType())
                                                      .stream().filter(mnt -> mnt.test(mount)).findFirst().orElse(null);
                        if (entityInsul != null)
                        {   Temperature.replaceOrAddModifier(player, new MountTempModifier(entityInsul.coldInsulation(), entityInsul.heatInsulation()).tickRate(5).expires(5), Trait.RATE, Matcher.SAME_CLASS);
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
                        Trait trait = foodData.duration() > 0 ? Trait.BASE : Trait.CORE;
                        // Custom class for soul sprouts
                        FoodTempModifier foodModifier = item.getItem() == ModItems.SOUL_SPROUT
                                                        ? new SoulSproutTempModifier(temperature)
                                                        : new FoodTempModifier(temperature);
                        // Store the item ID & duration of the TempModifier
                        foodModifier.getNBT().putString("item", ForgeRegistries.ITEMS.getKey(item.getItem()).toString());
                        foodModifier.getNBT().putInt("duration", duration);
                        // Set duration & tick rate
                        foodModifier.expires(duration).tickRate(duration);
                        // Add the TempModifier
                        Placement placement = Placement.LAST.limitDuplicates(Matcher.EQUALS, foodData.stackLimit())
                                              .orElse(Placement.of(Mode.REPLACE, Order.FIRST, foodModifier::equals));
                        Temperature.addModifier(player, foodModifier, trait, placement);
                    }
                }
            }
        }
    }

    public static LazyOptional<ITemperatureCap> getTemperatureCap(Entity entity)
    {   return isTemperatureEnabled(entity) ? CAP_CACHE.get(entity) : LazyOptional.empty();
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

    private static void updateInventoryTempAttributes(ItemStack oldStack, ItemStack newStack, LivingEntity entity)
    {
        for (ItemTempData itemTempData : ConfigSettings.ITEM_TEMPERATURES.get().get(oldStack.getItem()))
        {   entity.getAttributes().removeAttributeModifiers(itemTempData.attributeModifiers().getMap());
        }
        for (ItemTempData itemTempData : ConfigSettings.ITEM_TEMPERATURES.get().get(newStack.getItem()))
        {
            if (itemTempData.test(entity, newStack))
            {   entity.getAttributes().addTransientAttributeModifiers(itemTempData.attributeModifiers().getMap());
            }
        }
    }

    private static final Field MENU_OWNER = ObfuscationReflectionHelper.findField(InventoryMenu.class, "f_39703_");
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

    public static void updateInsulationAttributeModifiers(LivingEntity entity, ItemStack from, ItemStack to, Insulation.Slot slot)
    {
        for (InsulatorData insulatorData : ItemInsulationManager.getInsulatorsForStack(from, slot))
        {   entity.getAttributes().removeAttributeModifiers(insulatorData.attributes().getMap());
        }
        for (InsulatorData insulatorData : RequirementHolder.filterValid(ItemInsulationManager.getInsulatorsForStack(to, slot), entity))
        {   entity.getAttributes().addTransientAttributeModifiers(insulatorData.attributes().getMap());
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

    public static boolean isImmuneToTemperature(LivingEntity player)
    {   return player == null || !player.isAlive() || isPeacefulMode(player) || player.hasEffect(ModEffects.GRACE);
    }

    public static double getColdResistance(LivingEntity entity)
    {   return entity.hasEffect(ModEffects.ICE_RESISTANCE) && ConfigSettings.ICE_RESISTANCE_ENABLED.get() ? 1
             : Temperature.get(entity, Trait.COLD_RESISTANCE);
    }
    public static double getHeatResistance(LivingEntity player)
    {   return player.hasEffect(MobEffects.FIRE_RESISTANCE) && ConfigSettings.FIRE_RESISTANCE_ENABLED.get() ? 1
             : Temperature.get(player, Trait.HEAT_RESISTANCE);
    }
    public static double getResistance(double temperature, LivingEntity player)
    {   return temperature < 0 ? getColdResistance(player) : getHeatResistance(player);
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

    public static Map<ItemStack, ItemTempData> getItemTemperaturesOnEntity(LivingEntity entity)
    {
        Map<ItemStack, ItemTempData> tempItems = new HashMap<>();
        /*
         Inventory items
         */
        if (entity instanceof Player player)
        {
            for (int i = 0; i < player.getInventory().items.size(); i++)
            {
                ItemStack stack = player.getInventory().items.get(i);
                if (stack.isEmpty()) continue;
                int slotIndex = i;
                ConfigSettings.ITEM_TEMPERATURES.get().get(stack.getItem()).forEach(temp ->
                {
                    if (temp.test(player, stack, slotIndex, null))
                    {   tempItems.put(stack, temp);
                    }
                });
            }
        }
        /*
         Armor items
         */
        for (EquipmentSlot slot : EquipmentSlot.values())
        {
            ItemStack stack = entity.getItemBySlot(slot);
            if (stack.isEmpty()) continue;
            SlotType slotType = SlotType.fromEquipment(slot);

            ConfigSettings.ITEM_TEMPERATURES.get().get(stack.getItem()).forEach(temp ->
            {
                if (temp.test(entity, stack, slotType))
                {   tempItems.put(stack, temp);
                }
            });
        }
        /*
         Curios
         */
        for (ItemStack curio : CompatManager.Curios.getCurios(entity))
        {
            ConfigSettings.ITEM_TEMPERATURES.get().get(curio.getItem()).forEach(temp ->
            {
                if (temp.test(entity, curio, SlotType.CURIO))
                {   tempItems.put(curio, temp);
                }
            });
        }
        /*
         Offhand
         */
        ItemStack offhand = entity.getOffhandItem();
        if (!offhand.isEmpty())
        {
            ConfigSettings.ITEM_TEMPERATURES.get().get(offhand.getItem()).forEach(temp ->
            {
                if (temp.test(entity, offhand, SlotType.HAND))
                {   tempItems.put(offhand, temp);
                }
            });
        }
        return tempItems;
    }

    /**
     * Gets the corresponding attribute value for the given {@link Trait}.
     * @param trait the type or ability to get the attribute for
     */
    @Nullable
    public static AttributeInstance getAttribute(Trait trait, LivingEntity entity)
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
                                                                  : attribute.getModifiers(operation));
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
            for (Trait trait : VALID_MODIFIER_TRAITS)
            {   allModifiers.addAll(cap.getModifiers(trait));
            }
        });
        return allModifiers;
    }
}