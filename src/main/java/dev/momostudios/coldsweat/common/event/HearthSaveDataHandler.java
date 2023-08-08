package dev.momostudios.coldsweat.common.event;

import com.mojang.datafixers.util.Pair;
import net.minecraft.nbt.CompoundNBT;
import net.minecraft.util.ResourceLocation;
import net.minecraft.util.math.BlockPos;
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

    public static CompoundNBT serializeDisabledHearths()
    {
        CompoundNBT disabledHearths = new CompoundNBT();

        int i = 0;
        for (Pair<BlockPos, ResourceLocation> pair : DISABLED_HEARTHS)
        {
            CompoundNBT hearthData = new CompoundNBT();
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

    public static void deserializeDisabledHearths(CompoundNBT disabledHearths)
    {
        DISABLED_HEARTHS.clear();
        for (String key : disabledHearths.getAllKeys())
        {
            CompoundNBT hearthData = disabledHearths.getCompound(key);
            DISABLED_HEARTHS.add(Pair.of(BlockPos.of(hearthData.getLong("pos")), new ResourceLocation(hearthData.getString("level"))));
        }
    }
}
