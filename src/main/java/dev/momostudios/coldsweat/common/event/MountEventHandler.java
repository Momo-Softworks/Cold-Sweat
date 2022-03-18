package dev.momostudios.coldsweat.common.event;

import dev.momostudios.coldsweat.api.temperature.Temperature;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.entity.vehicle.Minecart;
import net.minecraftforge.event.TickEvent;
import net.minecraftforge.eventbus.api.SubscribeEvent;
import net.minecraftforge.fml.common.Mod;
import net.minecraftforge.registries.ForgeRegistries;
import dev.momostudios.coldsweat.api.temperature.modifier.MountTempModifier;
import dev.momostudios.coldsweat.config.EntitySettingsConfig;
import dev.momostudios.coldsweat.core.init.BlockInit;
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
                if (player.getVehicle() instanceof Minecart minecart && minecart.getDisplayBlockState().getBlock() == BlockInit.MINECART_INSULATION.get())
                {
                    TempHelper.addModifier(player, new MountTempModifier(1).expires(1), Temperature.Types.RATE, false);
                }
                else
                {
                    for (List<Object> entity : EntitySettingsConfig.INSTANCE.insulatedEntities())
                    {
                        if (ForgeRegistries.ENTITIES.getKey(player.getVehicle().getType()).toString().equals(entity.get(0)))
                        {
                            Number number = (Number) entity.get(1);
                            double value = number.doubleValue();
                            TempHelper.addModifier(player, new MountTempModifier(value).expires(1), Temperature.Types.RATE, false);
                            break;
                        }
                    }
                }
            }
        }
    }
}
