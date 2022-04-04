package dev.momostudios.coldsweat.common.event;

import dev.momostudios.coldsweat.common.blockentity.HearthBlockEntity;
import dev.momostudios.coldsweat.core.network.ColdSweatPacketHandler;
import dev.momostudios.coldsweat.core.network.message.HearthResetMessage;
import net.minecraft.world.level.chunk.LevelChunk;
import net.minecraftforge.event.world.BlockEvent;
import net.minecraftforge.eventbus.api.SubscribeEvent;
import net.minecraftforge.fml.common.Mod;
import net.minecraftforge.network.PacketDistributor;

@Mod.EventBusSubscriber
public class HearthPathManagement
{
    @SubscribeEvent
    public static void onBlockUpdated(BlockEvent.NeighborNotifyEvent event)
    {
        int chunkX = (event.getPos().getX() >> 4) - 1;
        int chunkZ = (event.getPos().getZ() >> 4) - 1;

        for (int x = 0; x < 3; x++)
        {
            for (int z = 0; z < 3; z++)
            {
                LevelChunk chunk = event.getWorld().getChunkSource().getChunkNow(chunkX + x, chunkZ + z);

                if (chunk != null)
                chunk.getBlockEntities().forEach((pos, block) ->
                {
                    if (block instanceof HearthBlockEntity hearth && !hearth.shouldRebuild())
                    {
                        hearth.setShouldRebuild(true);
                        ColdSweatPacketHandler.INSTANCE.send(PacketDistributor.TRACKING_CHUNK.with(() -> chunk), new HearthResetMessage(pos));
                    }
                });
            }
        }
    }
}
