package com.momosoftworks.coldsweat.common.event;

import com.google.common.collect.ImmutableSet;
import com.momosoftworks.coldsweat.api.event.common.EnableTemperatureEvent;
import com.momosoftworks.coldsweat.api.registry.TempModifierRegistry;
import com.momosoftworks.coldsweat.api.temperature.modifier.*;
import com.momosoftworks.coldsweat.api.util.Temperature;
import com.momosoftworks.coldsweat.api.util.Temperature.Addition;
import com.momosoftworks.coldsweat.api.util.Temperature.Addition.Mode;
import com.momosoftworks.coldsweat.api.util.Temperature.Addition.Order;
import com.momosoftworks.coldsweat.config.ConfigSettings;
import com.momosoftworks.coldsweat.core.properties.IEntityTempProperty;
import com.momosoftworks.coldsweat.core.properties.PlayerTempProperty;
import com.momosoftworks.coldsweat.util.compat.CompatManager;
import com.momosoftworks.coldsweat.util.registries.ModProperties;
import com.momosoftworks.coldsweat.util.world.BlockPos;
import com.momosoftworks.coldsweat.util.world.WorldHelper;
import cpw.mods.fml.common.eventhandler.SubscribeEvent;
import cpw.mods.fml.common.gameevent.TickEvent;
import net.minecraft.entity.Entity;
import net.minecraft.entity.EntityLivingBase;
import net.minecraft.entity.item.EntityMinecartEmpty;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.entity.player.EntityPlayerMP;
import net.minecraft.init.Blocks;
import net.minecraft.init.Items;
import net.minecraft.item.EnumAction;
import net.minecraftforge.common.MinecraftForge;
import net.minecraftforge.event.entity.EntityEvent;
import net.minecraftforge.event.entity.EntityJoinWorldEvent;
import net.minecraftforge.event.entity.living.LivingEvent;
import net.minecraftforge.event.entity.player.PlayerEvent;
import net.minecraftforge.event.entity.player.PlayerUseItemEvent;

import java.util.*;

public class EntityTempManager
{
    public static final Temperature.Type[] VALID_TEMPERATURE_TYPES = {Temperature.Type.CORE, Temperature.Type.BASE, Temperature.Type.FREEZING_POINT, Temperature.Type.BURNING_POINT, Temperature.Type.WORLD};
    public static final Temperature.Type[] VALID_MODIFIER_TYPES    = {Temperature.Type.CORE, Temperature.Type.BASE, Temperature.Type.RATE, Temperature.Type.FREEZING_POINT, Temperature.Type.BURNING_POINT, Temperature.Type.WORLD};
    private static final Set<Class<?>> TEMPERATURE_ENABLED_ENTITIES = new HashSet<>();

    public static final Map<Entity, IEntityTempProperty> SERVER_PROP_CACHE = new HashMap<>();
    public static final Map<Entity, IEntityTempProperty> CLIENT_PROP_CACHE = new HashMap<>();

    /**
     * Attach temperature capability to entities
     */
    @SubscribeEvent
    public void onEntityConstructing(EntityEvent.EntityConstructing event)
    {
        if (event.entity instanceof EntityLivingBase)
        {
            EntityLivingBase entity = (EntityLivingBase) event.entity;
            // Players always get the capability
            if (!(entity instanceof EntityPlayer))
            {   EnableTemperatureEvent enableEvent = new EnableTemperatureEvent(entity);
                MinecraftForge.EVENT_BUS.post(enableEvent);
                if (!enableEvent.isEnabled() || enableEvent.isCanceled()) return;
            }
            TEMPERATURE_ENABLED_ENTITIES.add(entity.getClass());

            entity.registerExtendedProperties(entity instanceof EntityPlayer ? ModProperties.PLAYER_TEMP
                                                                             : ModProperties.ENTITY_TEMP,
                                              new PlayerTempProperty());
        }
    }

    public static IEntityTempProperty getTemperatureProperty(Entity entity)
    {
        Map<Entity, IEntityTempProperty> cache = entity.worldObj.isRemote ? CLIENT_PROP_CACHE : SERVER_PROP_CACHE;
        return cache.computeIfAbsent(entity, e ->
        {
            return (IEntityTempProperty) e.getExtendedProperties(entity instanceof EntityPlayer ? ModProperties.PLAYER_TEMP
                                                                                                : ModProperties.ENTITY_TEMP);
        });
    }

    /**
     * Tick TempModifiers & update temperature for living entities
     */
    @SubscribeEvent
    public void onLivingTick(LivingEvent.LivingUpdateEvent event)
    {
        EntityLivingBase entity = event.entityLiving;
        if (!(entity instanceof EntityPlayer || getEntitiesWithTemperature().contains(entity.getClass()))) return;

        IEntityTempProperty prop = getTemperatureProperty(entity);
        {
            // Tick modifiers serverside
            if (!entity.worldObj.isRemote)
            {   prop.tick(entity);
            }
            // Tick modifiers clientside
            else
            {   prop.tickDummy(entity);
            }

            // Remove expired modifiers
            for (Temperature.Type type : VALID_MODIFIER_TYPES)
            {
                prop.getModifiers(type).removeIf(modifier ->
                {   int expireTime = modifier.getExpireTime();
                    return (modifier.setTicksExisted(modifier.getTicksExisted() + 1) > expireTime && expireTime != -1);
                });
            }

            if (entity instanceof EntityPlayer && entity.ticksExisted % 60 == 0)
            {   Temperature.updateModifiers(entity, prop);
            }
        }
    }

    /**
     * Transfer the player's capability when traveling from the End
     */
    @SubscribeEvent
    public void returnFromEnd(PlayerEvent.Clone event)
    {
        if (!event.wasDeath && !event.entityPlayer.worldObj.isRemote)
        {
            // Get the old player's capability
            EntityPlayer oldPlayer = event.original;

            // Copy the capability to the new player
            getTemperatureProperty(event.entityPlayer).copy(getTemperatureProperty(oldPlayer));
        }
    }

    /**
     * Enable temperature handling for chameleons
     */
    @SubscribeEvent
    public void onEnableTemperatureEvent(EnableTemperatureEvent event)
    {
        //if (event.getEntity() instanceof ChameleonEntity) event.setEnabled(true);
    }

    /**
     * Add modifiers to the player & valid entities when they join the world
     */
    @SubscribeEvent
    public void initModifiersOnEntity(EntityJoinWorldEvent event)
    {
        // Add basic TempModifiers to player
        if (event.entity instanceof EntityPlayerMP)
        {
            EntityPlayerMP player = (EntityPlayerMP) event.entity;
            // Sometimes the entity isn't fully initialized, so wait until next tick
            if (player.mcServer != null)
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

            // Add listener for granting the sewing table recipe when the player gets an insulation item
            /*player.inventory.addSlotListener(new IContainerListener()
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
            });*/
        }
        // Add basic TempModifiers to chameleons
        else if (event.entity instanceof EntityLivingBase && getEntitiesWithTemperature().contains(event.entity.getClass()))
        {
            EntityLivingBase entity = (EntityLivingBase) event.entity;
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
        }
    }

    /**
     * Handle modifiers for freezing, burning, and being wet
     */
    @SubscribeEvent
    public void onPlayerTick(TickEvent.PlayerTickEvent event)
    {
        EntityPlayer player = event.player;

        // Water / Rain
        if (!player.worldObj.isRemote && event.phase == TickEvent.Phase.START)
        {
            if (player.ticksExisted % 5 == 0)
            {
                if (WorldHelper.isInWater(player) || player.ticksExisted % 40 == 0 && WorldHelper.isRainingAt(player.worldObj, new BlockPos(player)))
                    Temperature.addModifier(player, new WaterTempModifier(0.01f).tickRate(5), Temperature.Type.WORLD, false);

                if (player.isBurning())
                    Temperature.addOrReplaceModifier(player, new FireTempModifier().expires(5), Temperature.Type.BASE);
            }
        }
    }

    /**
     * Handle HearthTempModifier when the player has the Insulation effect
     */
    // TODO: 9/24/23 Probably need mixins for adding/removing/expiring events
    /*@SubscribeEvent
    public static void onInsulationUpdate(PotionEvent event)
    {
        if (!event.getEntity().level.isClientSide && event.getEntity() instanceof EntityPlayer && event.getPotionEffect() != null
        && event.getPotionEffect().getEffect() == ModEffects.INSULATION)
        {
            EntityPlayer player = (EntityPlayer) event.getEntityLiving();
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
    }*/

    /**
     * Improve the player's temperature when they sleep
     */
    // TODO: 9/24/23 Probably mixin WorldServer#wakeAllPlayers() and fire a custom event
    /*@SubscribeEvent
    public static void onSleep(SleepFinishedTimeEvent event)
    {
        if (!event.getWorld().isClientSide())
        {
            event.getWorld().players().forEach(player ->
            {
                if (player.isSleeping())
                {
                    // Divide the player's current temperature by 4
                    getTemperatureProperty(player).ifPresent(cap ->
                    {
                        double temp = cap.getTemp(Temperature.Type.CORE);
                        cap.setTemp(Temperature.Type.CORE, temp / 4f);
                        Temperature.updateTemperature(player, cap, true);
                    });
                }
            });
        }
    }*/

    /**
     * Handle insulation on mounted entity
     */
    @SubscribeEvent
    public void playerRiding(TickEvent.PlayerTickEvent event)
    {
        if (event.phase == TickEvent.Phase.END && !event.player.worldObj.isRemote && event.player.ticksExisted % 5 == 0)
        {
            EntityPlayer player = event.player;
            if (player.ridingEntity != null)
            {
                Entity mount = player.ridingEntity;
                // If insulated minecart
                // TODO: 9/24/23 Fix this when blocks are added
                if (mount instanceof EntityMinecartEmpty && ((EntityMinecartEmpty) mount).func_145820_n() == Blocks.air)//ModBlocks.MINECART_INSULATION)
                {   Temperature.addModifier(player, new MountTempModifier(20, 20).expires(1), Temperature.Type.RATE, false);
                }
                // If insulated entity (defined in config)
                else
                {
                    // TODO: 9/24/23 Add this when configs are added
                    /*EntitySettingsConfig.getInstance().getInsulatedEntities().stream().filter(entry ->
                    entry.get(0).equals(ForgeRegistries.ENTITIES.getKey(mount.getType()).toString())).findFirst()
                    .ifPresent(entry ->
                    {   int warming = ((Number) entry.get(1)).intValue();
                        int cooling = entry.size() < 3
                                    ? warming
                                    : ((Number) entry.get(2)).intValue();
                        Temperature.addModifier(player, new MountTempModifier(warming, cooling).expires(5), Temperature.Type.RATE, false);
                    });*/
                }
            }
        }
    }

    /**
     * Handle TempModifiers for consumables
     */
    @SubscribeEvent
    public void onEatFood(PlayerUseItemEvent.Finish event)
    {
        if (!event.entity.worldObj.isRemote
        && event.item.getItemUseAction() == EnumAction.drink || event.item.getItemUseAction() == EnumAction.eat)
        {
            EntityPlayer player = event.entityPlayer;
            // If food item defined in config
            float foodTemp = ConfigSettings.FOOD_TEMPERATURES.get().getOrDefault(event.item.getItem(), 0d).floatValue();
            if (foodTemp != 0)
            {   Temperature.addModifier(player, new FoodTempModifier(foodTemp).expires(0), Temperature.Type.CORE, true);
            }
            // Soul sprout
            // TODO: 9/24/23 Fix this when items are added
            else if (event.item.getItem() == Items.arrow)//ModItems.SOUL_SPROUT)
            {   Temperature.addOrReplaceModifier(player, new SoulSproutTempModifier().expires(900), Temperature.Type.BASE);
            }
        }
    }

    public static Set<Class<?>> getEntitiesWithTemperature()
    {   return ImmutableSet.copyOf(TEMPERATURE_ENABLED_ENTITIES);
    }
}