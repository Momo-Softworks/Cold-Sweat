package dev.momostudios.coldsweat.common.event;

import com.mojang.datafixers.util.Pair;
import net.minecraft.core.BlockPos;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.resources.ResourceLocation;
import net.minecraftforge.event.entity.player.PlayerEvent;
import net.minecraftforge.eventbus.api.SubscribeEvent;
import net.minecraftforge.fml.common.Mod;

import java.util.*;

@Mod.EventBusSubscriber
public class HearthSaveDataHandler
{
    public static final Set<Pair<BlockPos, ResourceLocation>> HEARTH_POSITIONS = new HashSet<>();
    public static final Set<Pair<BlockPos, ResourceLocation>> DISABLED_HEARTHS = new HashSet<>();

    /**
     * Save the player's disabled hearths on logout
     */
    @SubscribeEvent
    public static void saveDisabledHearths(PlayerEvent.PlayerLoggedOutEvent event)
    {
        event.getPlayer().getPersistentData().put("disabledHearths", serializeDisabledHearths());
    }

    public static CompoundTag serializeDisabledHearths()
    {
        CompoundTag disabledHearths = new CompoundTag();

        int i = 0;
        for (Pair<BlockPos, ResourceLocation> pair : DISABLED_HEARTHS)
        {
            CompoundTag hearthData = new CompoundTag();
            hearthData.putLong("pos", pair.getFirst().asLong());
            hearthData.putString("level", pair.getSecond().toString());
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
            DISABLED_HEARTHS.add(Pair.of(BlockPos.of(hearthData.getLong("pos")), new ResourceLocation(hearthData.getString("level"))));
        }
    }
}
