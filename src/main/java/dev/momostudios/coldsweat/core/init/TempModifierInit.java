package dev.momostudios.coldsweat.core.init;

import dev.momostudios.coldsweat.ColdSweat;
import dev.momostudios.coldsweat.api.event.core.BlockEffectRegisterEvent;
import dev.momostudios.coldsweat.api.event.core.TempModifierRegisterEvent;
import dev.momostudios.coldsweat.api.registry.BlockEffectRegistry;
import dev.momostudios.coldsweat.api.registry.TempModifierRegistry;
import dev.momostudios.coldsweat.api.temperature.block_effect.*;
import dev.momostudios.coldsweat.api.temperature.modifier.*;
import dev.momostudios.coldsweat.config.ColdSweatConfig;
import dev.momostudios.coldsweat.util.config.ConfigHelper;
import dev.momostudios.coldsweat.util.math.CSMath;
import net.minecraft.core.BlockPos;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraftforge.common.MinecraftForge;
import net.minecraftforge.event.world.WorldEvent;
import net.minecraftforge.eventbus.api.SubscribeEvent;
import net.minecraftforge.fml.ModList;
import net.minecraftforge.fml.common.Mod;

import java.util.ArrayList;
import java.util.List;

@Mod.EventBusSubscriber
public class TempModifierInit
{
    // Trigger TempModifierEvent.Init
    @SubscribeEvent
    public static void registerTempModifiers(WorldEvent.Load event)
    {
        TempModifierRegistry.flush();
        BlockEffectRegistry.flush();

        try { MinecraftForge.EVENT_BUS.post(new TempModifierRegisterEvent()); }
        catch (Exception e)
        {
            ColdSweat.LOGGER.error("Registering TempModifiers failed!");
            e.printStackTrace();
        }

        try { MinecraftForge.EVENT_BUS.post(new BlockEffectRegisterEvent()); }
        catch (Exception e)
        {
            ColdSweat.LOGGER.error("Registering BlockEffects failed!");
            e.printStackTrace();
        }
    }

    // Register BlockEffects
    @SubscribeEvent
    public static void registerBlockEffects(BlockEffectRegisterEvent event)
    {
        event.register(new LavaBlockEffect());
        event.register(new FurnaceBlockEffect());
        event.register(new CampfireBlockEffect());
        event.register(new IceboxBlockEffect());
        event.register(new BoilerBlockEffect());
        event.register(new SoulCampfireBlockEffect());
        event.register(new NetherPortalBlockEffect());

        // Auto-generate BlockEffects from config
        for (List<?> effectBuilder : ColdSweatConfig.getInstance().getBlockEffects())
        {
            try
            {
                // Check if required fields are present
                if (!(effectBuilder.get(0) instanceof String)
                || !(effectBuilder.get(1) instanceof Number configTemp)
                || !(effectBuilder.get(2) instanceof Number configRange))
                {
                    throw new Exception("Invalid BlockEffect format");
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
                        new BlockEffect(effectBlocks.toArray(new Block[0]))
                        {
                            @Override
                            public double getTemperature(Player player, BlockState state, BlockPos pos, double distance)
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
                ColdSweat.LOGGER.error("Invalid configuration for BlockEffects in config file \"main.toml\"");
                e.printStackTrace();
                break;
            }
        }
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
        event.register(new TimeTempModifier());
        event.register(new WaterskinTempModifier());
        event.register(new HellLampTempModifier());
        event.register(new WaterTempModifier());
        event.register(new HearthTempModifier());
        event.register(new FoodTempModifier());
        event.register(new FreezingTempModifier());

        if (ModList.get().isLoaded("sereneseasons"))
        {
            try { event.register((TempModifier) Class.forName(sereneSeasons).getConstructor().newInstance()); }
            catch (Exception ignored) {}
        }
    }
}
