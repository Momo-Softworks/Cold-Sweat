package dev.momostudios.coldsweat.core.init;

import dev.momostudios.coldsweat.ColdSweat;
import dev.momostudios.coldsweat.api.event.core.BlockTempRegisterEvent;
import dev.momostudios.coldsweat.api.event.core.TempModifierRegisterEvent;
import dev.momostudios.coldsweat.api.registry.BlockTempRegistry;
import dev.momostudios.coldsweat.api.registry.TempModifierRegistry;
import dev.momostudios.coldsweat.api.temperature.block_temp.*;
import dev.momostudios.coldsweat.api.temperature.modifier.*;
import dev.momostudios.coldsweat.config.ColdSweatConfig;
import dev.momostudios.coldsweat.util.compat.ModGetters;
import dev.momostudios.coldsweat.util.config.ConfigHelper;
import dev.momostudios.coldsweat.util.math.CSMath;
import net.minecraft.core.BlockPos;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraftforge.common.MinecraftForge;
import net.minecraftforge.event.server.ServerStartedEvent;
import net.minecraftforge.eventbus.api.SubscribeEvent;
import net.minecraftforge.fml.common.Mod;

import java.util.ArrayList;
import java.util.List;

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
                // Check if required fields are present
                if (!(effectBuilder.get(0) instanceof String)
                || !(effectBuilder.get(1) instanceof Number configTemp)
                || !(effectBuilder.get(2) instanceof Number configRange))
                {
                    throw new Exception("Invalid BlockTemp format");
                }

                String[] blockIDs = ((String) effectBuilder.get(0)).split(",");

                final double temp    = configTemp.doubleValue();
                final double range   = configRange.doubleValue();
                final boolean weaken = effectBuilder.size() < 4 || !(effectBuilder.get(2) instanceof Boolean) || (boolean) effectBuilder.get(3);

                final double maxChange = effectBuilder.size() == 5 && effectBuilder.get(4) instanceof Number
                        ? ((Number) effectBuilder.get(4)).doubleValue()
                        : Double.MAX_VALUE;

                final double maxEffect = temp > 0 ?  maxChange :  Double.MAX_VALUE;
                final double minEffect = temp < 0 ? -maxChange : -Double.MAX_VALUE;

                List<Block> effectBlocks = new ArrayList<>();

                for (String id : blockIDs)
                {
                    effectBlocks.addAll(ConfigHelper.getBlocks(id));
                }

                event.register(
                        new BlockTemp(effectBlocks.toArray(new Block[0]))
                        {
                            @Override
                            public double getTemperature(Level level, LivingEntity entity, BlockState state, BlockPos pos, double distance)
                            {
                                return weaken ? CSMath.blend(temp, 0, distance, 0.5, range) : temp;
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
        event.register(new SoulCampfireBlockTemp());
        event.register(new NetherPortalBlockTemp());
    }

    // Register TempModifiers
    @SubscribeEvent
    public static void registerTempModifiers(TempModifierRegisterEvent event)
    {
        String sereneSeasons = "dev.momostudios.coldsweat.api.temperature.modifier.compat.SereneSeasonsTempModifier";

        event.register(new BlockTempModifier());
        event.register(new BiomeTempModifier());
        event.register(new DepthTempModifier());
        event.register(new InsulationTempModifier());
        event.register(new MountTempModifier());
        event.register(new WaterskinTempModifier());
        event.register(new SoulLampTempModifier());
        event.register(new WaterTempModifier());
        event.register(new HearthTempModifier());
        event.register(new FoodTempModifier());
        event.register(new FreezingTempModifier());
        event.register(new FireTempModifier());

        if (ModGetters.isSereneSeasonsLoaded())
        {
            try { event.register((TempModifier) Class.forName(sereneSeasons).getConstructor().newInstance()); }
            catch (Exception ignored) {}
        }

        if (CompatManager.isArmorUnderwearLoaded())
        {   try { event.register((TempModifier) Class.forName(armorUnder).getConstructor().newInstance()); }
            catch (Exception ignored) {}
        }
    }
}
