package com.momosoftworks.coldsweat.core.init;

import com.momosoftworks.coldsweat.ColdSweat;
import com.momosoftworks.coldsweat.api.event.core.BlockTempRegisterEvent;
import com.momosoftworks.coldsweat.api.event.core.TempModifierRegisterEvent;
import com.momosoftworks.coldsweat.api.registry.BlockTempRegistry;
import com.momosoftworks.coldsweat.api.registry.TempModifierRegistry;
import com.momosoftworks.coldsweat.api.temperature.block_temp.*;
import com.momosoftworks.coldsweat.api.temperature.modifier.*;
import com.momosoftworks.coldsweat.config.WorldSettingsConfig;
import com.momosoftworks.coldsweat.util.compat.CompatManager;
import com.momosoftworks.coldsweat.util.serialization.ConfigHelper;
import com.momosoftworks.coldsweat.util.math.CSMath;
import net.minecraft.core.BlockPos;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.block.state.properties.Property;
import net.minecraftforge.common.MinecraftForge;
import net.minecraftforge.event.server.ServerStartingEvent;
import net.minecraftforge.eventbus.api.SubscribeEvent;
import net.minecraftforge.fml.common.Mod;

import java.util.*;
import java.util.function.Predicate;

@Mod.EventBusSubscriber
public class TempModifierInit
{
    @SubscribeEvent
    public static void onServerStart(ServerStartingEvent event)
    {   buildRegistries();
    }

    // Trigger registry events
    public static void buildRegistries()
    {
        TempModifierRegistry.flush();
        BlockTempRegistry.flush();

        try { MinecraftForge.EVENT_BUS.post(new TempModifierRegisterEvent()); }
        catch (Exception e)
        {
            ColdSweat.LOGGER.error("Registering TempModifiers failed!");
            e.printStackTrace();
        }

        try { MinecraftForge.EVENT_BUS.post(new BlockTempRegisterEvent()); }
        catch (Exception e)
        {
            ColdSweat.LOGGER.error("Registering BlockTemps failed!");
            e.printStackTrace();
        }
    }

    // Register BlockTemps
    @SubscribeEvent
    public static void registerBlockTemps(BlockTempRegisterEvent event)
    {
        long startMS = System.currentTimeMillis();
        // Auto-generate BlockTemps from config
        for (List<?> effectBuilder : WorldSettingsConfig.getInstance().getBlockTemps())
        {
            try
            {
                // Get IDs associated with this config entry
                String[] blockIDs = ((String) effectBuilder.get(0)).split(",");
                // Temp of block
                final double blockTemp = ((Number) effectBuilder.get(1)).doubleValue();
                // Range of effect
                final double blockRange = ((Number) effectBuilder.get(2)).doubleValue();

                // Weakens over distance?
                final boolean weaken = effectBuilder.size() < 4 || (boolean) effectBuilder.get(3);

                // Get min/max effect
                final double maxChange = effectBuilder.size() == 5 && effectBuilder.get(4) instanceof Number
                        ? ((Number) effectBuilder.get(4)).doubleValue()
                        : Double.MAX_VALUE;

                final double maxEffect = blockTemp > 0 ?  maxChange :  Double.MAX_VALUE;
                final double minEffect = blockTemp < 0 ? -maxChange : -Double.MAX_VALUE;

                // Parse block IDs into blocks
                Block[] effectBlocks = Arrays.stream(blockIDs).map(ConfigHelper::getBlocks).flatMap(List::stream).toArray(Block[]::new);

                // Get block predicate
                Map<String, Predicate<BlockState>> blockPredicates = new HashMap<>();
                if (effectBuilder.size() == 6 && effectBuilder.get(5) instanceof String)
                {
                    // Separate comma-delineated predicates
                    String[] predicateList = ((String) effectBuilder.get(5)).split(",");

                    // Iterate predicates
                    for (String predicate : predicateList)
                    {
                        // Split predicate into key-value pairs separated by "="
                        String[] pair = predicate.split("=");
                        String key = pair[0];
                        String value = pair[1];

                        // Get the property with the given name
                        Property<?> property = effectBlocks[0].getStateDefinition().getProperty(key);
                        if (property != null)
                        {
                            // Parse the desired value for this property
                            property.getValue(value).ifPresent(propertyValue ->
                            {
                                // Add a new predicate to the list
                                blockPredicates.put(predicate, state ->
                                {   // If the value matches, this predicate returns true
                                    return state.getValue(property).equals(propertyValue);
                                });
                            });
                        }
                    }
                }

                event.register(new BlockTempConfig(blockPredicates, effectBlocks)
                {
                    @Override
                    public double getTemperature(Level level, LivingEntity entity, BlockState state, BlockPos pos, double distance)
                    {
                        // Check the list of predicates first
                        if (blockPredicates.isEmpty() || this.testPredicates(state))
                        {   return weaken ? CSMath.blend(blockTemp, 0, distance, 0.5, blockRange) : blockTemp;
                        }
                        return  0;
                    }

                    @Override
                    public double maxEffect()
                    {   return maxEffect;
                    }

                    @Override
                    public double minEffect()
                    {   return minEffect;
                    }
                });
            }
            catch (Exception e)
            {   ColdSweat.LOGGER.error("Invalid configuration for BlockTemps in config file \"main.toml\"", e);
                break;
            }
        }

        event.register(new LavaBlockTemp());
        event.register(new FurnaceBlockTemp());
        event.register(new CampfireBlockTemp());
        event.register(new IceboxBlockTemp());
        event.register(new BoilerBlockTemp());
        event.register(new NetherPortalBlockTemp());
        ColdSweat.LOGGER.debug("Registered BlockTemps in " + (System.currentTimeMillis() - startMS) + "ms");
    }

    // Register TempModifiers
    @SubscribeEvent
    public static void registerTempModifiers(TempModifierRegisterEvent event)
    {
        long startMS = System.currentTimeMillis();
        String compatPath = "com.momosoftworks.coldsweat.api.temperature.modifier.compat.";
        String sereneSeasons = compatPath + "SereneSeasonsTempModifier";
        String armorUnder = compatPath + "ArmorUnderTempModifier";
        String weatherStorms = compatPath + "StormTempModifier";
        String curios = compatPath + "CuriosTempModifier";

        event.register(BlockTempModifier::new);
        event.register(BiomeTempModifier::new);
        event.register(UndergroundTempModifier::new);
        event.register(InsulationTempModifier::new);
        event.register(MountTempModifier::new);
        event.register(WaterskinTempModifier::new);
        event.register(SoulLampTempModifier::new);
        event.register(WaterTempModifier::new);
        event.register(HearthTempModifier::new);
        event.register(FoodTempModifier::new);
        event.register(FreezingTempModifier::new);
        event.register(FireTempModifier::new);
        event.register(SoulSproutTempModifier::new);

        // Compat
        if (CompatManager.isSereneSeasonsLoaded())
        {   event.registerByClassName(sereneSeasons);
        }

        if (CompatManager.isArmorUnderwearLoaded())
        {   event.registerByClassName(armorUnder);
        }

        if (CompatManager.isWeather2Loaded())
        {   event.registerByClassName(weatherStorms);
        }

        if (CompatManager.isCuriosLoaded())
        {   event.registerByClassName(curios);
        }

        ColdSweat.LOGGER.debug("Registered TempModifiers in " + (System.currentTimeMillis() - startMS) + "ms");
    }
}
