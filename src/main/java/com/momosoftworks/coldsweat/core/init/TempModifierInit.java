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
import com.momosoftworks.coldsweat.util.serialization.NBTHelper;
import net.minecraft.block.Block;
import net.minecraft.block.BlockState;
import net.minecraft.entity.LivingEntity;
import net.minecraft.nbt.CompoundNBT;
import net.minecraft.state.Property;
import net.minecraft.tileentity.TileEntity;
import net.minecraft.util.ResourceLocation;
import net.minecraft.util.math.BlockPos;
import net.minecraft.world.World;
import net.minecraftforge.common.MinecraftForge;
import net.minecraftforge.eventbus.api.SubscribeEvent;
import net.minecraftforge.fml.common.Mod;

import java.util.*;
import java.util.function.Predicate;

@Mod.EventBusSubscriber
public class TempModifierInit
{
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

                // Parse block IDs into blocks
                Block[] effectBlocks = Arrays.stream(blockIDs).map(ConfigHelper::getBlocks).flatMap(List::stream).toArray(Block[]::new);

                // Get block predicate
                Map<String, Predicate<BlockState>> blockPredicates = effectBuilder.size() >= 6 && effectBuilder.get(5) instanceof String
                                                                     ? ConfigHelper.getBlockStatePredicates(effectBlocks[0], (String) effectBuilder.get(5))
                                                                     : new HashMap<>();

                event.register(new BlockTempConfig(blockPredicates, effectBlocks)
                {
                    // Temp of block
                    final double blockTemp = ((Number) effectBuilder.get(1)).doubleValue();
                    // Range of effect
                    final double blockRange = ((Number) effectBuilder.get(2)).doubleValue();

                    // Weakens over distance?
                    final boolean weaken = effectBuilder.size() > 3 && effectBuilder.get(3) instanceof Boolean
                                          ? (Boolean) effectBuilder.get(3)
                                          : true;

                    // Get min/max effect
                    final double maxChange = effectBuilder.size() > 4 && effectBuilder.get(4) instanceof Number
                                             ? ((Number) effectBuilder.get(4)).doubleValue()
                                             : Double.MAX_VALUE;

                    final Optional<CompoundNBT> tag = effectBuilder.size() > 6 && effectBuilder.get(6) instanceof String
                                                      ? Optional.of(NBTHelper.parseCompoundNbt((String) effectBuilder.get(5)))
                                                      : Optional.empty();

                    final double maxEffect = blockTemp > 0 ?  maxChange :  Double.MAX_VALUE;
                    final double minEffect = blockTemp < 0 ? -maxChange : -Double.MAX_VALUE;

                    @Override
                    public double getTemperature(World level, LivingEntity entity, BlockState state, BlockPos pos, double distance)
                    {
                        // Check the list of predicates first
                        if (blockPredicates.isEmpty() || this.testPredicates(state))
                        {
                            if (tag.isPresent())
                            {
                                TileEntity blockEntity = level.getBlockEntity(pos);
                                if (blockEntity != null)
                                {
                                    CompoundNBT blockTag = blockEntity.save(new CompoundNBT());
                                    for (String key : tag.get().getAllKeys())
                                    {
                                        if (!tag.get().get(key).equals(blockTag.get(key)))
                                        {   return 0;
                                        }
                                    }
                                }
                            }
                            return weaken ? CSMath.blend(blockTemp, 0, distance, 0.5, blockRange) : blockTemp;
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
            {   ColdSweat.LOGGER.error("Invalid configuration for BlockTemps in config file \"main.toml\"");
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
        ColdSweat.LOGGER.debug("Registered BlockTemps in " + (System.currentTimeMillis() - startMS) + "ms");
    }

    // Register TempModifiers
    @SubscribeEvent
    public static void registerTempModifiers(TempModifierRegisterEvent event)
    {
        long startMS = System.currentTimeMillis();
        String compatPath = "com.momosoftworks.coldsweat.api.temperature.modifier.compat.";
        String sereneSeasons = compatPath + "SereneSeasonsTempModifier";
        String betterWeather = compatPath + "BetterWeatherTempModifier";
        String armorUnder = compatPath + "ArmorUnderTempModifier";
        String weatherStorms = compatPath + "StormTempModifier";
        String curios = compatPath + "CuriosTempModifier";

        event.register(new ResourceLocation(ColdSweat.MOD_ID, "blocks"), BlockTempModifier::new);
        event.register(new ResourceLocation(ColdSweat.MOD_ID, "biomes"), BiomeTempModifier::new);
        event.register(new ResourceLocation(ColdSweat.MOD_ID, "underground"), UndergroundTempModifier::new);
        event.register(new ResourceLocation(ColdSweat.MOD_ID, "armor"), ArmorInsulationTempModifier::new);
        event.register(new ResourceLocation(ColdSweat.MOD_ID, "mount"), MountTempModifier::new);
        event.register(new ResourceLocation(ColdSweat.MOD_ID, "waterskin"), WaterskinTempModifier::new);
        event.register(new ResourceLocation(ColdSweat.MOD_ID, "soulspring_lamp"), SoulLampTempModifier::new);
        event.register(new ResourceLocation(ColdSweat.MOD_ID, "water"), WaterTempModifier::new);
        event.register(new ResourceLocation(ColdSweat.MOD_ID, "air_conditioning"), BlockInsulationTempModifier::new);
        event.register(new ResourceLocation(ColdSweat.MOD_ID, "food"), FoodTempModifier::new);
        event.register(new ResourceLocation(ColdSweat.MOD_ID, "freezing"), FreezingTempModifier::new);
        event.register(new ResourceLocation(ColdSweat.MOD_ID, "on_fire"), FireTempModifier::new);
        event.register(new ResourceLocation(ColdSweat.MOD_ID, "soul_sprout"), SoulSproutTempModifier::new);

        // Compat
        if (CompatManager.isSereneSeasonsLoaded())
        {   event.registerByClassName(new ResourceLocation("sereneseasons", "season"), sereneSeasons);
        }
        if (CompatManager.isArmorUnderwearLoaded())
        {   event.registerByClassName(new ResourceLocation("armorunder", "lining"), armorUnder);
        }
        if (CompatManager.isWeather2Loaded())
        {   event.registerByClassName(new ResourceLocation("weather2", "storm"), weatherStorms);
        }
        if (CompatManager.isCuriosLoaded())
        {   event.registerByClassName(new ResourceLocation("curios", "curios"), curios);
        }
        if (CompatManager.isBetterWeatherLoaded())
        {   event.registerByClassName(new ResourceLocation("betterweather", "betterweather"), betterWeather);
        }

        ColdSweat.LOGGER.debug("Registered TempModifiers in " + (System.currentTimeMillis() - startMS) + "ms");
    }
}
