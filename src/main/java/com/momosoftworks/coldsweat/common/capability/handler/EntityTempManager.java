package com.momosoftworks.coldsweat.common.capability.handler;

import com.google.common.collect.ImmutableSet;
import com.momosoftworks.coldsweat.ColdSweat;
import com.momosoftworks.coldsweat.api.event.core.GatherDefaultTempModifiersEvent;
import com.momosoftworks.coldsweat.api.registry.TempModifierRegistry;
import com.momosoftworks.coldsweat.api.temperature.modifier.*;
import com.momosoftworks.coldsweat.api.util.Temperature;
import com.momosoftworks.coldsweat.api.util.Placement;
import com.momosoftworks.coldsweat.api.util.Placement.Mode;
import com.momosoftworks.coldsweat.api.util.Placement.Order;
import com.momosoftworks.coldsweat.common.capability.ModCapabilities;
import com.momosoftworks.coldsweat.common.capability.temperature.EntityTempCap;
import com.momosoftworks.coldsweat.common.capability.temperature.ITemperatureCap;
import com.momosoftworks.coldsweat.common.capability.temperature.PlayerTempCap;
import com.momosoftworks.coldsweat.config.ConfigSettings;
import com.momosoftworks.coldsweat.config.type.InsulatingMount;
import com.momosoftworks.coldsweat.config.type.PredicateItem;
import com.momosoftworks.coldsweat.core.event.TaskScheduler;
import com.momosoftworks.coldsweat.util.compat.CompatManager;
import com.momosoftworks.coldsweat.util.math.CSMath;
import com.momosoftworks.coldsweat.util.registries.ModAttributes;
import com.momosoftworks.coldsweat.util.registries.ModBlocks;
import com.momosoftworks.coldsweat.util.registries.ModEffects;
import com.momosoftworks.coldsweat.util.registries.ModItems;
import com.momosoftworks.coldsweat.util.world.WorldHelper;
import net.minecraft.entity.Entity;
import net.minecraft.entity.EntityType;
import net.minecraft.entity.LivingEntity;
import net.minecraft.entity.ai.attributes.Attribute;
import net.minecraft.entity.ai.attributes.AttributeModifier;
import net.minecraft.entity.ai.attributes.ModifiableAttributeInstance;
import net.minecraft.entity.item.minecart.MinecartEntity;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.inventory.EquipmentSlotType;
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
import net.minecraftforge.eventbus.api.EventPriority;
import net.minecraftforge.eventbus.api.SubscribeEvent;
import net.minecraftforge.fml.common.Mod;
import net.minecraftforge.registries.ForgeRegistries;

import javax.annotation.Nonnull;
import javax.annotation.Nullable;
import java.util.*;

@Mod.EventBusSubscriber
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

    public static final Map<Entity, LazyOptional<ITemperatureCap>> SERVER_CAP_CACHE = new HashMap<>();
    public static final Map<Entity, LazyOptional<ITemperatureCap>> CLIENT_CAP_CACHE = new HashMap<>();

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
     * Attach temperature capability to entities
     */
    @SubscribeEvent
    public static void attachCapabilityToEntityHandler(AttachCapabilitiesEvent<Entity> event)
    {
        if (event.getObject() instanceof LivingEntity)
        {
            LivingEntity entity = (LivingEntity) event.getObject();
            if (!TEMPERATURE_ENABLED_ENTITIES.contains(entity.getType())) return;

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

    /**
     * Add modifiers to the player and valid entities when they join the world
     */
    @SubscribeEvent
    public static void initModifiersOnEntity(EntityJoinWorldEvent event)
    {
        if (event.getEntity() instanceof LivingEntity && !event.getEntity().level.isClientSide()
        && TEMPERATURE_ENABLED_ENTITIES.contains(event.getEntity().getType()))
        {
            LivingEntity living = ((LivingEntity) event.getEntity());
            getTemperatureCap(living).ifPresent(cap ->
            {
                // If entity has never been initialized, add default modifiers
                if (!event.getEntity().getPersistentData().getBoolean("InitializedModifiers"))
                {
                    for (Temperature.Trait trait : VALID_MODIFIER_TRAITS)
                    {
                        GatherDefaultTempModifiersEvent gatherEvent = new GatherDefaultTempModifiersEvent(living, trait);
                        MinecraftForge.EVENT_BUS.post(gatherEvent);

                        cap.clearModifiers(trait);
                        cap.getModifiers(trait).addAll(gatherEvent.getModifiers());
                    }
                    living.getPersistentData().putBoolean("InitializedModifiers", true);
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
        if (!getEntitiesWithTemperature().contains(entity.getType())) return;

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
            for (Temperature.Trait trait : VALID_MODIFIER_TRAITS)
            {
                cap.getModifiers(trait).removeIf(modifier ->
                {   int expireTime = modifier.getExpireTime();
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
        if (!event.getEntity().level.isClientSide)
        {
            // Get the old player's capability
            PlayerEntity oldPlayer = event.getOriginal();

            if (!event.isWasDeath())
            {
                // Copy the capability to the new player
                getTemperatureCap(event.getPlayer()).ifPresent(cap ->
                {   getTemperatureCap(oldPlayer).ifPresent(cap::copy);
                });
            }
            getTemperatureCap(oldPlayer).map(ITemperatureCap::getPersistentAttributes).orElse(new HashSet<>())
            .forEach(attr ->
            {   event.getPlayer().getAttribute(attr).setBaseValue(oldPlayer.getAttribute(attr).getBaseValue());
            });
        }
    }

    /**
     * Add default modifiers to players and temperature-enabled entities
     */
    @SubscribeEvent(priority = EventPriority.HIGHEST)
    public static void defineDefaultModifiers(GatherDefaultTempModifiersEvent event)
    {
        // Default TempModifiers for players
        if (event.getEntity() instanceof PlayerEntity && event.getTrait() == Temperature.Trait.WORLD)
        {
            event.addModifier(new BiomeTempModifier(25).tickRate(10), Placement.Duplicates.BY_CLASS, Placement.BEFORE_FIRST);
            event.addModifier(new UndergroundTempModifier().tickRate(10), Placement.Duplicates.BY_CLASS, Placement.of(Mode.AFTER, Order.FIRST, mod -> mod instanceof BiomeTempModifier));
            event.addModifier(new BlockTempModifier().tickRate(4), Placement.Duplicates.BY_CLASS, Placement.of(Mode.AFTER, Order.FIRST, mod -> mod instanceof UndergroundTempModifier));

            // Serene Seasons compat
            if (CompatManager.isSereneSeasonsLoaded())
            {
                TempModifierRegistry.getValue(new ResourceLocation("sereneseasons:season")).ifPresent(mod -> event.addModifier(mod.tickRate(60), Placement.Duplicates.BY_CLASS, Placement.of(Mode.BEFORE, Order.FIRST,
                                                                                                                                                 mod2 -> mod2 instanceof UndergroundTempModifier)));
            }
            // Weather2 Compat
            if (CompatManager.isWeather2Loaded())
            {
                TempModifierRegistry.getValue(new ResourceLocation("weather2:storm")).ifPresent(mod -> event.addModifier(mod.tickRate(60), Placement.Duplicates.BY_CLASS, Placement.of(Mode.BEFORE, Order.FIRST,
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
            {   TempModifierRegistry.getValue(new ResourceLocation("sereneseasons:season")).ifPresent(mod -> event.addModifier(mod.tickRate(60), Placement.Duplicates.BY_CLASS, Placement.of(Mode.BEFORE, Order.FIRST,
                                                                                                                                          mod2 -> mod2 instanceof UndergroundTempModifier)));
            }
            // Weather2 Compat
            if (CompatManager.isWeather2Loaded())
            {   TempModifierRegistry.getValue(new ResourceLocation("weather2:storm")).ifPresent(mod -> event.addModifier(mod.tickRate(60), Placement.Duplicates.BY_CLASS, Placement.of(Mode.BEFORE, Order.FIRST,
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
        if (event.getEntity() instanceof PlayerEntity)
        {
            PlayerEntity player = ((PlayerEntity) event.getEntity());
            player.containerMenu.addSlotListener(new IContainerListener()
            {
                public void slotChanged(Container menu, int slotIndex, ItemStack stack)
                {
                    Slot slot = menu.getSlot(slotIndex);
                    if (!(slot instanceof CraftingResultSlot))
                    {
                        if (slot.container == player.inventory
                        && (ConfigSettings.INSULATION_ITEMS.get().containsKey(stack.getItem())))
                        {   player.awardRecipesByKey(new ResourceLocation[]{new ResourceLocation(ColdSweat.MOD_ID, "sewing_table")});
                        }
                    }
                }

                public void setContainerData(Container container, int slot, int value) {}

                public void refreshContainer(Container container, NonNullList<ItemStack> stacks) {}
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
                if (WorldHelper.isInWater(player) || player.tickCount % 40 == 0
                && WorldHelper.isRainingAt(player.level, player.blockPosition()))
                {   Temperature.addModifier(player, new WaterTempModifier(0.01f).tickRate(5), Temperature.Trait.WORLD, Placement.Duplicates.BY_CLASS);
                }

                if (player.isOnFire())
                {   Temperature.addOrReplaceModifier(player, new FireTempModifier().expires(5), Temperature.Trait.BASE, Placement.Duplicates.BY_CLASS);
                }
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
                TempModifier newMod = new BlockInsulationTempModifier(effect.getAmplifier() + 1).expires(effect.getDuration());
                Temperature.addOrReplaceModifier(player, newMod, Temperature.Trait.WORLD, Placement.Duplicates.BY_CLASS);
            }
            // Remove TempModifier on potion effect removed
            else if (event instanceof PotionEvent.PotionRemoveEvent)
            {   Temperature.removeModifiers(player, Temperature.Trait.WORLD, mod -> mod instanceof BlockInsulationTempModifier);
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
                        double temp = cap.getTrait(Temperature.Trait.CORE);
                        cap.setTrait(Temperature.Trait.CORE, temp / 4f);
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
            PlayerEntity player = event.player;
            if (player.getVehicle() != null)
            {
                Entity mount = player.getVehicle();
                // If insulated minecart
                if (mount instanceof MinecartEntity && ((MinecartEntity) mount).getDisplayBlockState().getBlock() == ModBlocks.MINECART_INSULATION)
                {   Temperature.addOrReplaceModifier(player, new MountTempModifier(1, 1).tickRate(5).expires(5), Temperature.Trait.RATE, Placement.Duplicates.BY_CLASS);
                }
                // If insulated entity (defined in config)
                else
                {
                    InsulatingMount entityInsul = ConfigSettings.INSULATED_ENTITIES.get().get(mount.getType());
                    if (entityInsul != null && entityInsul.test(mount))
                    {   Temperature.addOrReplaceModifier(player, new MountTempModifier(entityInsul.coldInsulation, entityInsul.heatInsulation).tickRate(5).expires(5), Temperature.Trait.RATE, Placement.Duplicates.BY_CLASS);
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
        if (event.getEntity() instanceof PlayerEntity
        && (event.getItem().getUseAnimation() == UseAction.DRINK || event.getItem().getUseAnimation() == UseAction.EAT)
        && !event.getEntity().level.isClientSide)
        {
            PlayerEntity player = (PlayerEntity) event.getEntity();
            // If food item defined in config
            PredicateItem foodData = ConfigSettings.FOOD_TEMPERATURES.get().get(event.getItem().getItem());
            if (foodData != null && foodData.test(event.getItem()))
            {
                double effect = foodData.value;
                if (foodData.extraData.contains("duration", 3))
                {
                    int duration = foodData.extraData.getInt("duration");
                    // Special case for soul sprouts
                    FoodTempModifier foodModifier = event.getItem().getItem() == ModItems.SOUL_SPROUT
                                                    ? new SoulSproutTempModifier(effect)
                                                    : new FoodTempModifier(effect);
                    // Store the duration of the TempModifier
                    foodModifier.getNBT().put("extraData", foodData.extraData);
                    // Add the TempModifier
                    Temperature.addModifier(player, foodModifier.expires(duration), Temperature.Trait.BASE, Placement.Duplicates.BY_CLASS);
                }
                else
                {   Temperature.addModifier(player, new FoodTempModifier(effect).expires(0), Temperature.Trait.CORE, Placement.Duplicates.EXACT);
                }
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
     * Sets the corresponding attribute value for the given {@link Temperature.Trait}.
     * @param trait the type or ability to get the attribute for
     */
    public static void setAttribute(Temperature.Trait trait, LivingEntity entity, double value)
    {
        switch (trait)
        {
            case WORLD : CSMath.doIfNotNull(entity.getAttribute(ModAttributes.WORLD_TEMPERATURE), att -> att.setBaseValue(value)); break;
            case BASE  : CSMath.doIfNotNull(entity.getAttribute(ModAttributes.BASE_BODY_TEMPERATURE), att -> att.setBaseValue(value)); break;
            case HEAT_RESISTANCE : CSMath.doIfNotNull(entity.getAttribute(ModAttributes.HEAT_RESISTANCE), att -> att.setBaseValue(value)); break;
            case COLD_RESISTANCE : CSMath.doIfNotNull(entity.getAttribute(ModAttributes.COLD_RESISTANCE), att -> att.setBaseValue(value)); break;
            case HEAT_DAMPENING  : CSMath.doIfNotNull(entity.getAttribute(ModAttributes.HEAT_DAMPENING), att -> att.setBaseValue(value)); break;
            case COLD_DAMPENING  : CSMath.doIfNotNull(entity.getAttribute(ModAttributes.COLD_DAMPENING), att -> att.setBaseValue(value)); break;
            case FREEZING_POINT : CSMath.doIfNotNull(entity.getAttribute(ModAttributes.FREEZING_POINT), att -> att.setBaseValue(value)); break;
            case BURNING_POINT  : CSMath.doIfNotNull(entity.getAttribute(ModAttributes.BURNING_POINT), att -> att.setBaseValue(value)); break;
        }
    }

    /**
     * Gets the corresponding attribute value for the given {@link Temperature.Trait}.
     * @param trait the type or ability to get the attribute for
     */
    @Nullable
    public static ModifiableAttributeInstance getAttribute(Temperature.Trait trait, LivingEntity entity)
    {
        switch (trait)
        {
            case WORLD : return entity.getAttribute(ModAttributes.WORLD_TEMPERATURE);
            case BASE  : return entity.getAttribute(ModAttributes.BASE_BODY_TEMPERATURE);
            case FREEZING_POINT  : return entity.getAttribute(ModAttributes.FREEZING_POINT);
            case BURNING_POINT   : return entity.getAttribute(ModAttributes.BURNING_POINT);
            case HEAT_RESISTANCE : return entity.getAttribute(ModAttributes.HEAT_RESISTANCE);
            case COLD_RESISTANCE : return entity.getAttribute(ModAttributes.COLD_RESISTANCE);
            case HEAT_DAMPENING  : return entity.getAttribute(ModAttributes.HEAT_DAMPENING);
            case COLD_DAMPENING  : return entity.getAttribute(ModAttributes.COLD_DAMPENING);

            default : throw ColdSweat.LOGGER.throwing(new IllegalArgumentException("\"" + trait + "\" is not a valid trait!"));
        }
    }

    public static Collection<AttributeModifier> getAttributeModifiers(LivingEntity entity, ModifiableAttributeInstance attribute, @Nullable AttributeModifier.Operation operation)
    {
        Collection<AttributeModifier> modifiers = new ArrayList<>(operation == null
                                                                  ? attribute.getModifiers()
                                                                  : attribute.getModifiers(operation));
        for (EquipmentSlotType slot : EquipmentSlotType.values())
        {
            if (slot.getType() != EquipmentSlotType.Group.ARMOR) continue;
            ItemStack stack = entity.getItemBySlot(slot);
            if (!stack.isEmpty())
            {   modifiers.addAll(ItemInsulationManager.getAttributeModifiers(stack, attribute.getAttribute(), slot, operation, entity));
            }
        }
        return modifiers;
    }

    public static AttributeModifier makeAttributeModifier(Temperature.Trait trait, double value, AttributeModifier.Operation operation)
    {
        switch (trait)
        {
            case WORLD : return new AttributeModifier("World Temperature Modifier", value, operation);
            case BASE  : return new AttributeModifier("Base Body Temperature Modifier", value, operation);

            case FREEZING_POINT : return new AttributeModifier("Freezing Point Modifier", value, operation);
            case BURNING_POINT  : return new AttributeModifier("Burning Point Modifier", value, operation);
            case HEAT_RESISTANCE : return new AttributeModifier("Heat Resistance Modifier", value, operation);
            case COLD_RESISTANCE : return new AttributeModifier("Cold Resistance Modifier", value, operation);
            case HEAT_DAMPENING  : return new AttributeModifier("Heat Dampening Modifier", value, operation);
            case COLD_DAMPENING  : return new AttributeModifier("Cold Dampening Modifier", value, operation);
            default : throw ColdSweat.LOGGER.throwing(new IllegalArgumentException("\"" + trait + "\" is not a valid trait!"));
        }
    }

    public static boolean isTemperatureAttribute(Attribute attribute)
    {
        return CSMath.containsAny(ForgeRegistries.ATTRIBUTES.getKey(attribute).toString(),
                                  Arrays.stream(EntityTempManager.VALID_ATTRIBUTE_TYPES)
                                        .map(Temperature.Trait::getSerializedName).toArray(String[]::new));
    }
}