package dev.momostudios.coldsweat.common.event;

import dev.momostudios.coldsweat.util.world.WorldHelper;
import net.minecraft.core.Direction;
import net.minecraft.world.level.ChunkPos;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.Blocks;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.chunk.LevelChunk;
import net.minecraft.world.level.material.Material;
import net.minecraftforge.event.world.BlockEvent;
import net.minecraftforge.eventbus.api.SubscribeEvent;
import net.minecraftforge.fml.common.Mod;

@Mod.EventBusSubscriber
public class PlaceCaveAir
{
    @SubscribeEvent
    public static void onRemoveBlock(BlockEvent.NeighborNotifyEvent event)
    {
        ChunkPos chunkPos = new ChunkPos(event.getPos());
        LevelChunk chunk = event.getWorld().getChunkSource().getChunkNow(chunkPos.x, chunkPos.z);

        if (event.getWorld() instanceof Level level && !WorldHelper.canSeeSky(chunk, level, event.getPos()))
        {
            WorldHelper.schedule(() ->
            {
                if (chunk != null)
                {
                    BlockState state = chunk.getBlockState(event.getPos());

                    if (state.getMaterial() == Material.AIR && state.getBlock() != Blocks.CAVE_AIR)
                    for (Direction direction : event.getNotifiedSides())
                    {
                        if (event.getWorld().getBlockState(event.getPos().relative(direction)).getBlock() == Blocks.CAVE_AIR)
                        {
                            event.getWorld().setBlock(event.getPos(), Blocks.CAVE_AIR.defaultBlockState(), 2);
                            break;
                        }
                    }
                }
            }, 1);
        }
    }
}
