package dev.momostudios.coldsweat.core.init;

import dev.momostudios.coldsweat.ColdSweat;
import dev.momostudios.coldsweat.api.event.core.BlockTempRegisterEvent;
import dev.momostudios.coldsweat.api.event.core.TempModifierRegisterEvent;
import dev.momostudios.coldsweat.api.registry.BlockTempRegistry;
import dev.momostudios.coldsweat.api.registry.TempModifierRegistry;
import dev.momostudios.coldsweat.api.temperature.block_temp.*;
import dev.momostudios.coldsweat.api.temperature.modifier.*;
import dev.momostudios.coldsweat.config.ColdSweatConfig;
import dev.momostudios.coldsweat.util.compat.CompatManager;
import dev.momostudios.coldsweat.util.config.ConfigHelper;
import dev.momostudios.coldsweat.util.math.CSMath;
import net.minecraft.core.BlockPos;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.block.state.properties.Property;
import net.minecraftforge.common.MinecraftForge;
import net.minecraftforge.event.server.ServerStartedEvent;
import net.minecraftforge.eventbus.api.SubscribeEvent;
import net.minecraftforge.fml.common.Mod;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.function.Predicate;

@Mod.EventBusSubscriber
public class TempModifierInit
{
    // Trigger registry events
    @SubscribeEvent
    public static void registerServer(ServerStartedEvent event)
    {
        rebuildRegistries();
    }

    public static void rebuildRegistries()
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
        // Auto-generate BlockTemps from config
        for (List<?> effectBuilder : ColdSweatConfig.getInstance().getBlockTemps())
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
                List<Predicate<BlockState>> blockPredicates = new ArrayList<>();
                if (effectBuilder.size() == 6 && effectBuilder.get(5) instanceof String)
                {
                    String[] predicateList = ((String) effectBuilder.get(5)).split(",");

                    for (String predicate : predicateList)
                    {
                        // Split into name-value pairs separated by "="
                        String[] pair = predicate.split("=");
                        // Get the property with the given name
                        Property<?> property = effectBlocks[0].getStateDefinition().getProperty(pair[0]);
                        if (property != null)
                        {
                            // Parse the desired values that the user configured
                            String rawValue = pair[1];
                            Object value = property.getValueClass().equals(Boolean.class)
                                    ? Boolean.parseBoolean(rawValue)
                                    : property.getValueClass().equals(Integer.class)
                                    ? Integer.parseInt(rawValue)
                                    : property.getValueClass().equals(String.class)
                                    ? rawValue
                                    : null;

                            // Add a new predicate to the list
                            blockPredicates.add(state ->
                            {
                                // If the value is valid and matches, return true
                                return value != null && state.getValue(property).equals(value);
                            });
                        }
                    }
                }

                event.register(
                        new BlockTemp(effectBlocks)
                        {
                            @Override
                            public double getTemperature(Level level, LivingEntity entity, BlockState state, BlockPos pos, double distance)
                            {
                                // Check the list of predicates first
                                if (!blockPredicates.isEmpty() && !blockPredicates.stream().allMatch(pred -> pred.test(state))) return 0;

                                return weaken ? CSMath.blend(blockTemp, 0, distance, 0.5, blockRange) : blockTemp;
                            }

                            @Override
                            public double maxEffect()
                            {
                                return maxEffect;
                            }

                            @Override
                            public double minEffect()
                            {
                                return minEffect;
                            }
                        });
            }
            catch (Exception e)
            {
                ColdSweat.LOGGER.error("Invalid configuration for BlockTemps in config file \"main.toml\"");
                e.printStackTrace();
                break;
            }
        }

        event.register(new LavaBlockTemp());
        event.register(new FurnaceBlockTemp());
        event.register(new CampfireBlockTemp());
        event.register(new IceboxBlockTemp());
        event.register(new BoilerBlockTemp());
        event.register(new NetherPortalBlockTemp());
    }

    // Register TempModifiers
    @SubscribeEvent
    public static void registerTempModifiers(TempModifierRegisterEvent event)
    {
        String compatPath = "dev.momostudios.coldsweat.api.temperature.modifier.compat.";
        String sereneSeasons = compatPath + "SereneSeasonsTempModifier";
        String armorUnder = compatPath + "ArmorUnderTempModifier";
        String weatherStorms = compatPath + "StormTempModifier";

        event.register(new BlockTempModifier());
        event.register(new BiomeTempModifier());
        event.register(new UndergroundTempModifier());
        event.register(new InsulationTempModifier());
        event.register(new MountTempModifier());
        event.register(new WaterskinTempModifier());
        event.register(new SoulLampTempModifier());
        event.register(new WaterTempModifier());
        event.register(new HearthTempModifier());
        event.register(new FoodTempModifier());
        event.register(new FreezingTempModifier());
        event.register(new FireTempModifier());
        event.register(new SoulSproutTempModifier());

        // Compat
        if (CompatManager.isSereneSeasonsLoaded())
        {   try { event.register((TempModifier) Class.forName(sereneSeasons).getConstructor().newInstance()); }
            catch (Exception ignored) {}
        }

        if (CompatManager.isArmorUnderwearLoaded())
        {   try { event.register((TempModifier) Class.forName(armorUnder).getConstructor().newInstance()); }
            catch (Exception ignored) {}
        }

        if (CompatManager.isWeather2Loaded())
        {   try { event.register((TempModifier) Class.forName(weatherStorms).getConstructor().newInstance()); }
            catch (Exception ignored) {}
        }
    }
}
