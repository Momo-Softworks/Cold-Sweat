package com.momosoftworks.coldsweat.common.event;

import com.mojang.datafixers.util.Pair;
import com.momosoftworks.coldsweat.core.network.ColdSweatPacketHandler;
import com.momosoftworks.coldsweat.core.network.message.DisableHearthParticlesMessage;
import net.minecraft.core.BlockPos;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.nbt.ListTag;
import net.minecraft.nbt.Tag;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.server.level.ServerPlayer;
import net.minecraftforge.event.entity.player.PlayerEvent;
import net.minecraftforge.eventbus.api.SubscribeEvent;
import net.minecraftforge.fml.common.Mod;
import net.minecraftforge.network.PacketDistributor;

import java.util.HashSet;
import java.util.Set;

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
        event.getEntity().getPersistentData().put("disabledHearths", serializeDisabledHearths());
    }

    public static CompoundTag serializeDisabledHearths()
    {
        CompoundTag tag = new CompoundTag();
        ListTag disabledHearths = new ListTag();

        for (Pair<BlockPos, ResourceLocation> pair : DISABLED_HEARTHS)
        {
            CompoundTag hearthData = new CompoundTag();
            hearthData.putLong("Pos", pair.getFirst().asLong());
            hearthData.putString("Level", pair.getSecond().toString());
            disabledHearths.add(hearthData);
        }
        tag.put("DisabledHearths", disabledHearths);
        return tag;
    }

    public static void deserializeDisabledHearths(CompoundTag disabledHearths)
    {
        DISABLED_HEARTHS.clear();
        for (Tag tag : disabledHearths.getList("DisabledHearths", 10))
        {
            CompoundTag hearthData = (CompoundTag) tag;
            DISABLED_HEARTHS.add(Pair.of(BlockPos.of(hearthData.getLong("Pos")), new ResourceLocation(hearthData.getString("Level"))));
        }
    }

    /**
     * Load the player's disabled Hearths on login
     */
    @SubscribeEvent
    public static void loadDisabledHearths(PlayerEvent.PlayerLoggedInEvent event)
    {
        if (!event.getEntity().level.isClientSide && event.getEntity() instanceof ServerPlayer player)
        {
            CompoundTag disabledHearths = new CompoundTag();
            disabledHearths.put("DisabledHearths", player.getPersistentData().getList("DisabledHearths", 10));
            ColdSweatPacketHandler.INSTANCE.send(PacketDistributor.PLAYER.with(() -> player), new DisableHearthParticlesMessage(disabledHearths));
        }
    }
}
