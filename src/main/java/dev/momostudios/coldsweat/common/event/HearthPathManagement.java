package dev.momostudios.coldsweat.common.event;

import com.mojang.datafixers.util.Pair;
import dev.momostudios.coldsweat.api.event.common.BlockChangedEvent;
import dev.momostudios.coldsweat.common.block.HearthBottomBlock;
import dev.momostudios.coldsweat.common.block.HearthTopBlock;
import dev.momostudios.coldsweat.common.blockentity.HearthBlockEntity;
import dev.momostudios.coldsweat.util.world.SpreadPath;
import io.netty.util.internal.ConcurrentSet;
import net.minecraft.core.BlockPos;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.world.level.Level;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.api.distmarker.OnlyIn;
import net.minecraftforge.event.entity.player.PlayerEvent;
import net.minecraftforge.event.server.ServerStoppedEvent;
import net.minecraftforge.eventbus.api.SubscribeEvent;
import net.minecraftforge.fml.common.Mod;

import java.util.*;
import java.util.concurrent.ConcurrentHashMap;

@Mod.EventBusSubscriber
public class HearthPathManagement
{
    public static final Set<BlockPos> HEARTH_POSITIONS = new HashSet<>();
    public static final Set<Pair<BlockPos, String>> DISABLED_HEARTHS = new HashSet<>();

    /**
     * Save the player's disabled hearths on logout
     */
    @SubscribeEvent
    public static void saveDisabledHearths(PlayerEvent.PlayerLoggedOutEvent event)
    {
        event.getEntity().getPersistentData().put("disabledHearths", serializeDisabledHearths());
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
        deserializeDisabledHearths(event.getEntity().getPersistentData().getCompound("disabledHearths"));
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
}
