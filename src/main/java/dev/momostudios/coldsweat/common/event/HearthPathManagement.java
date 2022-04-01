package dev.momostudios.coldsweat.common.event;

import dev.momostudios.coldsweat.common.blockentity.HearthBlockEntity;
import dev.momostudios.coldsweat.core.network.ColdSweatPacketHandler;
import dev.momostudios.coldsweat.core.network.message.BlockDataUpdateMessage;
import net.minecraft.nbt.ByteTag;
import net.minecraft.nbt.IntTag;
import net.minecraft.world.level.chunk.LevelChunk;
import net.minecraftforge.event.TickEvent;
import net.minecraftforge.event.world.BlockEvent;
import net.minecraftforge.eventbus.api.SubscribeEvent;
import net.minecraftforge.fml.common.Mod;
import net.minecraftforge.network.PacketDistributor;

import java.util.List;

@Mod.EventBusSubscriber
public class HearthPathManagement
{
    static int HEARTH_REBUILD_COOLDOWN = 0;
    static boolean PENDING_BLOCK_UPDATES = false;

    @SubscribeEvent
    public static void onBlockUpdated(BlockEvent.NeighborNotifyEvent event)
    {
        PENDING_BLOCK_UPDATES = true;
        if (HEARTH_REBUILD_COOLDOWN <= 0)
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
                            ColdSweatPacketHandler.INSTANCE.send(PacketDistributor.TRACKING_CHUNK.with(() -> chunk),
                                    new BlockDataUpdateMessage(pos, List.of("shouldRebuild"), List.of(ByteTag.ONE)));
                        }
                    });
                }
            }
            HEARTH_REBUILD_COOLDOWN = 100;
            PENDING_BLOCK_UPDATES = false;
        }
    }

    @SubscribeEvent
    public static void onWorldTick(TickEvent.WorldTickEvent event)
    {
        if (!event.world.isClientSide && HEARTH_REBUILD_COOLDOWN > 0)
        {
            HEARTH_REBUILD_COOLDOWN--;
        }
    }
}
