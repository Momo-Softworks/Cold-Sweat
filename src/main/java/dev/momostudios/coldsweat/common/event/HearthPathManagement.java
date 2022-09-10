package dev.momostudios.coldsweat.common.event;

import com.mojang.datafixers.util.Pair;
import dev.momostudios.coldsweat.common.blockentity.HearthBlockEntity;
import net.minecraft.core.BlockPos;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.world.entity.player.Player;
import net.minecraftforge.event.entity.player.PlayerEvent;
import net.minecraftforge.event.server.ServerStoppedEvent;
import net.minecraftforge.event.world.BlockEvent;
import net.minecraftforge.eventbus.api.SubscribeEvent;
import net.minecraftforge.fml.common.Mod;

import java.util.HashSet;
import java.util.LinkedList;
import java.util.Set;

@Mod.EventBusSubscriber
public class HearthPathManagement
{
    public static LinkedList<BlockPos> HEARTH_POSITIONS = new LinkedList<>();

    public static final Set<Pair<BlockPos, String>> DISABLED_HEARTHS = new HashSet<>();

    // When a block update happens in the world, store the position of the chunk so nearby Hearths will be notified
    @SubscribeEvent
    public static void onBlockUpdated(BlockEvent.NeighborNotifyEvent event)
    {
        int chunkX = (event.getPos().getX() >> 4);
        int chunkZ = (event.getPos().getZ() >> 4);

    /**
     * Save the player's disabled hearths on logout
     */
    @SubscribeEvent
    public static void saveDisabledHearths(PlayerEvent.PlayerLoggedOutEvent event)
    {
        Player player = event.getPlayer();
        CompoundTag playerData = player.getPersistentData();
        CompoundTag disabledHearths = serializeDisabledHearths();
        playerData.put("disabledHearths", disabledHearths);
    }

    public static CompoundTag serializeDisabledHearths()
    {
        CompoundTag disabledHearths = new CompoundTag();

        int i = 0;
        for (Pair<BlockPos, String> pair : DISABLED_HEARTHS)
        {
            CompoundTag hearthData = new CompoundTag();
            hearthData.putLong("pos", pair.getFirst().asLong());
            hearthData.putString("level", pair.getSecond());
            disabledHearths.put(String.valueOf(i), hearthData);
            i++;
        }
        return disabledHearths;
    }

    /**
     * Load the player's disabled Hearths on login
     */
    @SubscribeEvent
    public static void loadDisabledHearths(PlayerEvent.PlayerLoggedInEvent event)
    {
        deserializeDisabledHearths(event.getPlayer().getPersistentData().getCompound("disabledHearths"));
    }

    public static void deserializeDisabledHearths(CompoundTag disabledHearths)
    {
        DISABLED_HEARTHS.clear();
        for (String key : disabledHearths.getAllKeys())
        {
            CompoundTag hearthData = disabledHearths.getCompound(key);
            DISABLED_HEARTHS.add(Pair.of(BlockPos.of(hearthData.getLong("pos")), hearthData.getString("level")));
        }
    }

    /**
     * Clear the list of Hearths when the server is closed (the player might be changing worlds)
     */
    @SubscribeEvent
    public static void onServerClosed(ServerStoppedEvent event)
    {
        HEARTH_POSITIONS.clear();
    }
}
