package dev.momostudios.coldsweat.common.event;

import dev.momostudios.coldsweat.api.temperature.Temperature;
import dev.momostudios.coldsweat.util.registries.ModBlocks;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.entity.vehicle.Minecart;
import net.minecraftforge.event.TickEvent;
import net.minecraftforge.eventbus.api.SubscribeEvent;
import net.minecraftforge.fml.common.Mod;
import dev.momostudios.coldsweat.api.temperature.modifier.MountTempModifier;
import dev.momostudios.coldsweat.config.EntitySettingsConfig;
import dev.momostudios.coldsweat.util.entity.TempHelper;

import java.util.List;

@Mod.EventBusSubscriber
public class MountEventHandler
{
    @SubscribeEvent
    public static void playerRiding(TickEvent.PlayerTickEvent event)
    {
        if (event.phase == TickEvent.Phase.END)
        {
            Player player = event.player;
            if (player.getVehicle() != null)
            {
                if (player.getVehicle() instanceof Minecart minecart && minecart.getDisplayBlockState().getBlock() == ModBlocks.MINECART_INSULATION)
                {
                    TempHelper.addModifier(player, new MountTempModifier(0.75).expires(1), Temperature.Type.RATE, false);
                }
                else
                {
                    for (List<Object> entity : EntitySettingsConfig.INSTANCE.insulatedEntities())
                    {
                        if (player.getVehicle().getType().getRegistryName().toString().equals(entity.get(0)))
                        {
                            Number number = (Number) entity.get(1);
                            double value = number.doubleValue();
                            TempHelper.addModifier(player, new MountTempModifier(value).expires(1), Temperature.Type.RATE, false);
                            break;
                        }
                    }
                }
            }
        }
    }
}
