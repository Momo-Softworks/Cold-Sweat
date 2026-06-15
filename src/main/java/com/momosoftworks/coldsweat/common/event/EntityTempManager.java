package com.momosoftworks.coldsweat.common.event;

import com.google.common.collect.ImmutableSet;
import com.momosoftworks.coldsweat.api.event.common.EnableTemperatureEvent;
import com.momosoftworks.coldsweat.api.registry.TempModifierRegistry;
import com.momosoftworks.coldsweat.api.temperature.modifier.*;
import com.momosoftworks.coldsweat.api.util.Temperature;
import com.momosoftworks.coldsweat.api.util.placement.Matcher;
import com.momosoftworks.coldsweat.api.util.placement.Mode;
import com.momosoftworks.coldsweat.api.util.placement.Order;
import com.momosoftworks.coldsweat.api.util.placement.Placement;
import com.momosoftworks.coldsweat.config.ConfigSettings;
import com.momosoftworks.coldsweat.core.network.ColdSweatPacketHandler;
import com.momosoftworks.coldsweat.core.network.message.SyncConfigSettingsMessage;
import com.momosoftworks.coldsweat.core.properties.IEntityTempProperty;
import com.momosoftworks.coldsweat.core.properties.PlayerTempProperty;
import com.momosoftworks.coldsweat.util.compat.CompatManager;
import com.momosoftworks.coldsweat.util.registries.ModItems;
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
import java.util.stream.Stream;

public class EntityTempManager
{
    public static final Temperature.Type[] VALID_TEMPERATURE_TYPES = Arrays.stream(Temperature.Type.values()).filter(Temperature.Type::isForTemperature).toArray(Temperature.Type[]::new);
    public static final Temperature.Type[] VALID_MODIFIER_TYPES    = Arrays.stream(Temperature.Type.values()).filter(Temperature.Type::isForModifiers).toArray(Temperature.Type[]::new);
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

    /**
     * Push all synced config settings to the client when a player logs in.
     */
    @SubscribeEvent
    public void onPlayerLogin(cpw.mods.fml.common.gameevent.PlayerEvent.PlayerLoggedInEvent event)
    {
        if (event.player instanceof EntityPlayerMP)
        {   ColdSweatPacketHandler.CHANNEL.sendTo(SyncConfigSettingsMessage.create(), (EntityPlayerMP) event.player);
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

            // Tick each modifier, then remove expired modifiers
            for (Temperature.Type type : VALID_MODIFIER_TYPES)
            {
                final Temperature.Type modType = type;
                prop.getModifiers(type).removeIf(modifier ->
                {   modifier.tick(entity);
                    int expireTime = modifier.getExpireTime();
                    boolean expired = (modifier.setTicksExisted(modifier.getTicksExisted() + 1) > expireTime && expireTime != -1);
                    if (expired)
                    {   modifier.onRemoved(entity, modType);
                    }
                    return expired;
                });
            }

            if (entity instanceof EntityPlayer && entity.ticksExisted % 60 == 0)
            {   Temperature.updateModifiers(entity, prop);
            }
        }
    }

    /**
     * Transfer the player's temperature data on death/respawn and on End travel.
     */
    @SubscribeEvent
    public void onPlayerClone(PlayerEvent.Clone event)
    {
        if (!event.entityPlayer.worldObj.isRemote)
        {
            EntityPlayer oldPlayer = event.original;
            IEntityTempProperty oldProp = getTemperatureProperty(oldPlayer);
            IEntityTempProperty newProp = getTemperatureProperty(event.entityPlayer);

            if (event.wasDeath)
            {
                // On death, reset CORE temp but preserve BASE modifiers (e.g. acclimation)
                newProp.copy(oldProp);
                newProp.setTemp(Temperature.Type.CORE, 0);
            }
            else
            {   // End travel: copy everything
                newProp.copy(oldProp);
            }
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
            Temperature.addModifier(player, new BiomeTempModifier(25).tickRate(10), Temperature.Type.WORLD, Placement.FIRST.noDuplicates(Matcher.SAME_CLASS));
            Temperature.addModifier(player, new ElevationTempModifier().tickRate(10), Temperature.Type.WORLD, Placement.of(Mode.ADD_AFTER, Order.FIRST, mod -> mod instanceof BiomeTempModifier).noDuplicates(Matcher.SAME_CLASS));
            Temperature.addModifier(player, new ShadeTempModifier().tickRate(10), Temperature.Type.WORLD, Placement.of(Mode.ADD_AFTER, Order.FIRST, mod -> mod instanceof ElevationTempModifier).noDuplicates(Matcher.SAME_CLASS));
            Temperature.addModifier(player, new BlockTempModifier().tickRate(4), Temperature.Type.WORLD, Placement.of(Mode.ADD_AFTER, Order.FIRST, mod -> mod instanceof ShadeTempModifier).noDuplicates(Matcher.SAME_CLASS));
            Temperature.addModifier(player, new EntitiesTempModifier().tickRate(20), Temperature.Type.WORLD, Placement.of(Mode.ADD_AFTER, Order.FIRST, mod -> mod instanceof BlockTempModifier).noDuplicates(Matcher.SAME_CLASS));
            Temperature.addModifier(player, new InventoryItemsTempModifier().tickRate(5), Temperature.Type.WORLD, Placement.LAST.noDuplicates(Matcher.SAME_CLASS));
            Temperature.addModifier(player, new AcclimationTempModifier().tickRate(20), Temperature.Type.FREEZING_POINT, Placement.LAST.noDuplicates(Matcher.SAME_CLASS));
            Temperature.addModifier(player, new AcclimationTempModifier().tickRate(20), Temperature.Type.BURNING_POINT, Placement.LAST.noDuplicates(Matcher.SAME_CLASS));

            // Serene Seasons compat
            if (CompatManager.isSereneSeasonsLoaded())
            {
                TempModifierRegistry.getEntryFor("sereneseasons:season").ifPresent(mod -> Temperature.addModifier(player, mod.tickRate(60), Temperature.Type.WORLD,
                                        Placement.of(Mode.ADD_BEFORE, Order.FIRST, mod2 -> mod2 instanceof ElevationTempModifier).noDuplicates(Matcher.SAME_CLASS)));
            }
            // Weather2 Compat
            if (CompatManager.isWeather2Loaded())
            {
                TempModifierRegistry.getEntryFor("weather2:storm").ifPresent(mod -> Temperature.addModifier(player, mod.tickRate(60), Temperature.Type.WORLD,
                                        Placement.of(Mode.ADD_BEFORE, Order.FIRST, mod2 -> mod2 instanceof ElevationTempModifier).noDuplicates(Matcher.SAME_CLASS)));
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
                                                           new ElevationTempModifier().tickRate(40),
                                                           new BlockTempModifier(4).tickRate(20)), Temperature.Type.WORLD, false);
            // Serene Seasons compat
            if (CompatManager.isSereneSeasonsLoaded())
            {
                TempModifierRegistry.getEntryFor("sereneseasons:season").ifPresent(mod -> Temperature.addModifier(entity, mod.tickRate(60), Temperature.Type.WORLD,
                                                                                                                  Placement.of(Mode.ADD_BEFORE, Order.FIRST, mod2 -> mod2 instanceof ElevationTempModifier).noDuplicates(Matcher.SAME_CLASS)));
            }
            // Weather2 Compat
            if (CompatManager.isWeather2Loaded())
            {
                TempModifierRegistry.getEntryFor("weather2:storm").ifPresent(mod -> Temperature.addModifier(entity, mod.tickRate(60), Temperature.Type.WORLD,
                                                                                                            Placement.of(Mode.ADD_BEFORE, Order.FIRST, mod2 -> mod2 instanceof ElevationTempModifier).noDuplicates(Matcher.SAME_CLASS)));
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

        if (!player.worldObj.isRemote && event.phase == TickEvent.Phase.START)
        {
            // Water / Rain
            if (player.ticksExisted % 5 == 0)
            {
                if (WorldHelper.isInWater(player) || player.ticksExisted % 40 == 0 && WorldHelper.isRainingAt(player.worldObj, new BlockPos(player)))
                    Temperature.addModifier(player, new WaterTempModifier(0.01f).tickRate(5), Temperature.Type.WORLD, false);

                if (player.isBurning())
                    Temperature.addOrReplaceModifier(player, new FireTempModifier().expires(5), Temperature.Type.BASE);
            }

            // Waterskin in hotbar — applies temperature effect while held
            if (player.ticksExisted % 20 == 0)
            {
                boolean hasWaterskin = false;
                for (int i = 0; i < 9; i++)
                {
                    net.minecraft.item.ItemStack stack = player.inventory.mainInventory[i];
                    if (stack != null && stack.getItem() == ModItems.FILLED_WATERSKIN)
                    {
                        hasWaterskin = true;
                        double temp = stack.hasTagCompound() ? stack.getTagCompound().getDouble("Temperature") : 0;
                        Temperature.addOrReplaceModifier(player, new WaterskinTempModifier(temp).<WaterskinTempModifier>expires(20), Temperature.Type.WORLD);
                        break;
                    }
                }
                if (!hasWaterskin)
                {   Temperature.removeModifiers(player, Temperature.Type.WORLD, mod -> mod instanceof WaterskinTempModifier);
                }
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