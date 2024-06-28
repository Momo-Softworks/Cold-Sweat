package com.momosoftworks.coldsweat.common.event;

import com.mojang.datafixers.util.Pair;
import com.momosoftworks.coldsweat.core.network.ModPacketHandlers;
import com.momosoftworks.coldsweat.core.network.message.DisableHearthParticlesMessage;
import net.minecraft.core.BlockPos;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.server.level.ServerPlayer;
import net.neoforged.bus.api.SubscribeEvent;
import net.neoforged.fml.common.EventBusSubscriber;
import net.neoforged.neoforge.event.entity.EntityJoinLevelEvent;
import net.neoforged.neoforge.network.PacketDistributor;

import java.util.*;

@EventBusSubscriber
public class HearthSaveDataHandler
{
    public static final Set<Pair<BlockPos, ResourceLocation>> HEARTH_POSITIONS = new HashSet<>();
    public static final Set<Pair<BlockPos, ResourceLocation>> DISABLED_HEARTHS = new HashSet<>();

    public static CompoundTag serializeDisabledHearths()
    {
        CompoundTag disabledHearths = new CompoundTag();

        int i = 0;
        for (Pair<BlockPos, ResourceLocation> pair : DISABLED_HEARTHS)
        {
            CompoundTag hearthData = new CompoundTag();
            hearthData.putLong("Pos", pair.getFirst().asLong());
            hearthData.putString("Level", pair.getSecond().toString());
            disabledHearths.put(String.valueOf(i), hearthData);
            i++;
        }
        return disabledHearths;
    }

    /**
     * Load the player's disabled Hearths on login
     */
    @SubscribeEvent
    public static void loadDisabledHearths(EntityJoinLevelEvent event)
    {
        if (!event.getEntity().level().isClientSide && event.getEntity() instanceof ServerPlayer player)
        {   PacketDistributor.sendToPlayer(player, new DisableHearthParticlesMessage(player.getPersistentData().getCompound("DisabledHearths")));
        }
    }

    public static void deserializeDisabledHearths(CompoundTag disabledHearths)
    {
        DISABLED_HEARTHS.clear();
        for (String key : disabledHearths.getAllKeys())
        {
            CompoundTag hearthData = disabledHearths.getCompound(key);
            DISABLED_HEARTHS.add(Pair.of(BlockPos.of(hearthData.getLong("Pos")), ResourceLocation.parse(hearthData.getString("Level"))));
        }
    }
}
