package dev.momostudios.coldsweat.common.event;

import dev.momostudios.coldsweat.common.entity.Chameleon;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.world.entity.player.Player;
import net.minecraftforge.event.TickEvent;
import net.minecraftforge.eventbus.api.SubscribeEvent;
import net.minecraftforge.fml.common.Mod;

@Mod.EventBusSubscriber
public class DismountChameleon
{
    @SubscribeEvent
    public static void onPlayerTick(TickEvent.PlayerTickEvent event)
    {
        Player player = event.player;
        if (player.isVehicle() && player.getPassengers().get(0) instanceof Chameleon chameleon)
        {
            CompoundTag data = event.player.getPersistentData();
            if (player.isCrouching())
            {
                if (!data.getBoolean("Sneaking"))
                {
                    if (player.tickCount - data.getLong("LastSneak") < 8)
                        data.putInt("SneakCount", data.getInt("SneakCount") + 1);
                    else data.putInt("SneakCount", 1);
                    data.putLong("LastSneak", player.tickCount);
                    data.putBoolean("Sneaking", true);

                    if (data.getInt("SneakCount") >= 3)
                    {
                        chameleon.stopRiding();
                        data.putInt("SneakCount", 0);
                    }
                }
            }
            else data.putBoolean("Sneaking", false);
        }
    }
}
