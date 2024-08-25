package com.momosoftworks.coldsweat.common.event;

import com.mojang.datafixers.util.Pair;
import com.momosoftworks.coldsweat.core.network.ColdSweatPacketHandler;
import com.momosoftworks.coldsweat.core.network.message.DisableHearthParticlesMessage;
import net.minecraft.entity.player.ServerPlayerEntity;
import net.minecraft.nbt.CompoundNBT;
import net.minecraft.nbt.INBT;
import net.minecraft.nbt.ListNBT;
import net.minecraft.util.ResourceLocation;
import net.minecraft.util.math.BlockPos;
import net.minecraftforge.event.entity.player.PlayerEvent;
import net.minecraftforge.eventbus.api.SubscribeEvent;
import net.minecraftforge.fml.common.Mod;
import net.minecraftforge.fml.network.PacketDistributor;

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
        event.getPlayer().getPersistentData().put("disabledHearths", serializeDisabledHearths());
    }

    public static CompoundNBT serializeDisabledHearths()
    {
        CompoundNBT tag = new CompoundNBT();
        ListNBT disabledHearths = new ListNBT();

        for (Pair<BlockPos, ResourceLocation> pair : DISABLED_HEARTHS)
        {
            CompoundNBT hearthData = new CompoundNBT();
            hearthData.putLong("Pos", pair.getFirst().asLong());
            hearthData.putString("Level", pair.getSecond().toString());
            disabledHearths.add(hearthData);
        }
        tag.put("DisabledHearths", disabledHearths);
        return tag;
    }

    public static void deserializeDisabledHearths(CompoundNBT disabledHearths)
    {
        DISABLED_HEARTHS.clear();
        for (INBT tag : disabledHearths.getList("DisabledHearths", 10))
        {
            CompoundNBT hearthData = (CompoundNBT) tag;
            DISABLED_HEARTHS.add(Pair.of(BlockPos.of(hearthData.getLong("Pos")), new ResourceLocation(hearthData.getString("Level"))));
        }
    }

    /**
     * Load the player's disabled Hearths on login
     */
    @SubscribeEvent
    public static void loadDisabledHearths(PlayerEvent.PlayerLoggedInEvent event)
    {
        if (!event.getEntity().level.isClientSide && event.getEntity() instanceof ServerPlayerEntity)
        {
            ServerPlayerEntity player = ((ServerPlayerEntity) event.getEntity());
            CompoundNBT disabledHearths = new CompoundNBT();
            disabledHearths.put("DisabledHearths", player.getPersistentData().getList("DisabledHearths", 10));
            ColdSweatPacketHandler.INSTANCE.send(PacketDistributor.PLAYER.with(() -> player), new DisableHearthParticlesMessage(disabledHearths));
        }
    }

    @SubscribeEvent
    public static void transferDisabledHearths(PlayerEvent.Clone event)
    {
        if (!event.getEntity().level.isClientSide())
        {
            ListNBT disabledHearths = event.getOriginal().getPersistentData().getList("DisabledHearths", 10);
            event.getEntity().getPersistentData().put("DisabledHearths", disabledHearths);
        }
    }
}
