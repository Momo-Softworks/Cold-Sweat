package dev.momostudios.coldsweat.api.temperature.modifier;

import com.mojang.math.Vector3d;
import dev.momostudios.coldsweat.api.registry.BlockEffectRegistry;
import dev.momostudios.coldsweat.api.temperature.Temperature;
import dev.momostudios.coldsweat.api.temperature.block_effect.BlockEffect;
import dev.momostudios.coldsweat.util.math.CSMath;
import dev.momostudios.coldsweat.util.world.WorldHelper;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.level.ChunkPos;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.chunk.LevelChunk;
import net.minecraft.world.level.chunk.PalettedContainer;
import net.minecraft.world.phys.Vec3;

import java.util.HashMap;
import java.util.Map;

public class BlockTempModifier extends TempModifier
{
    Map<ChunkPos, LevelChunk> chunkMap = new HashMap<>();

    public BlockTempModifier() {}

    @Override
    public Temperature getResult(Temperature temp, Player player)
    {
        Map<Block, Double> effectAmounts = new HashMap<>();

        if (player.tickCount % 60 == 0)
        {
            chunkMap.clear();
        }

        Level level = player.level;

        for (int x = -7; x < 14; x++)
        {
            for (int z = -7; z < 14; z++)
            {
                ChunkPos chunkPos = new ChunkPos((player.blockPosition().getX() + x) >> 4, (player.blockPosition().getZ() + z) >> 4);
                LevelChunk chunk = getChunk(level, chunkPos, chunkMap);

                for (int y = -7; y < 14; y++)
                {
                    try
                    {
                        BlockPos blockpos = player.blockPosition().offset(x, y, z);

                        PalettedContainer<BlockState> palette = chunk.getSection((blockpos.getY() >> 4) - chunk.getMinSection()).getStates();
                        BlockState state = palette.get(blockpos.getX() & 15, blockpos.getY() & 15, blockpos.getZ() & 15);

                        if (state.isAir()) continue;

                        // Get the BlockEffect associated with the block
                        BlockEffect be = BlockEffectRegistry.getEntryFor(state);

                        if (be == null || be.equals(BlockEffectRegistry.DEFAULT_BLOCK_EFFECT)) continue;

                        // Get the amount that this block has affected the player so far
                        double effectAmount = effectAmounts.getOrDefault(state.getBlock(), 0.0);

                        // Is totalTemp within the bounds of the BlockEffect's min/max allowed temps?
                        if (CSMath.isBetween(effectAmount, be.minEffect(), be.maxEffect())
                        && CSMath.isInRange(temp.get(), be.minTemperature(), be.maxTemperature()))
                        {
                            // Get Vector positions of the centers of the source block and player
                            Vec3 pos = new Vec3(blockpos.getX() + 0.5, blockpos.getY() + 0.5, blockpos.getZ() + 0.5);
                            Vec3 playerPos1 = new Vec3(player.getX(), player.getY() + player.getEyeHeight() * 0.25, player.getZ());
                            Vec3 playerPos2 = new Vec3(player.getX(), player.getY() + player.getEyeHeight(), player.getZ());

                            // Get the temperature of the block given the player's distance
                            double distance = CSMath.getDistance(player, new Vector3d(pos.x, pos.y, pos.z));
                            double tempToAdd = be.getTemperature(player, state, blockpos, distance);

                            // Cast a ray between the player and the block
                            // Lessen the effect with each block between the player and the block
                            Vec3 prevPos1 = playerPos1;
                            Vec3 prevPos2 = playerPos2;
                            int blocksBetween = 0;
                            for (int i = 0; i < distance * 1.25; i++)
                            {
                                // Get the position on the line of this current iteration
                                double factor = (i / (distance * 1.25));

                                // Get the next block (sub)position
                                double x1 = playerPos1.x - (playerPos1.x - pos.x) * factor;
                                double y1 = playerPos1.y - (playerPos1.y - pos.y) * factor;
                                double z1 = playerPos1.z - (playerPos1.z - pos.z) * factor;
                                Vec3 newPos1 = new Vec3(x1, y1, z1);

                                double x2 = playerPos2.x - (playerPos2.x - pos.x) * factor;
                                double y2 = playerPos2.y - (playerPos2.y - pos.y) * factor;
                                double z2 = playerPos2.z - (playerPos2.z - pos.z) * factor;
                                Vec3 newPos2 = new Vec3(x2, y2, z2);

                                /*
                                 Check if the newPos1 is not a duplicate BlockPos or solid block
                                 */
                                BlockPos bpos1 = new BlockPos(newPos1);
                                Vec3 facing1 = newPos1.subtract(prevPos1);
                                Direction dir1 = CSMath.getDirectionFromVector(facing1.x, facing1.y, facing1.z);

                                LevelChunk newChunk = null;

                                //Skip this iteration if this is a duplicate BlockPos
                                if (!bpos1.equals(new BlockPos(prevPos1)) && !bpos1.equals(blockpos))
                                {
                                    // Only get the blockstate if we're actually looking at this position
                                    BlockState state1 = palette.get((int) x1 & 15, (int) y1 & 15, (int) z1 & 15);
                                    // Only get the chunk if we're actually looking at this position
                                    newChunk = getChunk(level, new ChunkPos(bpos1), chunkMap);

                                    if (WorldHelper.isSpreadBlocked(newChunk, state1, bpos1, dir1))
                                    {
                                        // Divide the added temperature by 2 for each block between the player and the block
                                        blocksBetween++;
                                    }
                                }

                                /*
                                 Check if the newPos2 is not a duplicate BlockPos or solid block
                                 */
                                BlockPos bpos2 = new BlockPos(newPos2);
                                // Skip this iteration if the head/feet rays intersect (it will be the same blockstate)
                                if (bpos2 != bpos1)
                                {
                                    Vec3 facing2 = newPos2.subtract(prevPos2);
                                    Direction dir2 = CSMath.getDirectionFromVector(facing2.x, facing2.y, facing2.z);

                                    // Skip this iteration if this is a duplicate BlockPos
                                    if (!bpos2.equals(new BlockPos(prevPos2)) && !bpos2.equals(blockpos))
                                    {
                                        // Only get the blockstate if we're actually looking at this position
                                        BlockState state2 = palette.get((int) x2 & 15, (int) y2 & 15, (int) z2 & 15);
                                        // Only get the chunk if we're actually looking at this position
                                        if (newChunk == null) newChunk = getChunk(level, new ChunkPos(bpos1), chunkMap);

                                        if (WorldHelper.isSpreadBlocked(newChunk, state2, bpos2, dir2))
                                        {
                                            // Divide the added temperature by 2 for each block between the player and the block
                                            blocksBetween++;
                                        }
                                    }
                                }

                                prevPos1 = newPos1;
                                prevPos2 = newPos2;
                            }

                            // Calculate the decrease in effectiveness due to blocks in the way
                            double blockDampening = Math.pow(1.5, blocksBetween);

                            // Store this block type's total effect on the player
                            double blockEffectTotal = effectAmount + tempToAdd / blockDampening;
                            effectAmounts.put(state.getBlock(), CSMath.clamp(blockEffectTotal, be.minEffect(), be.maxEffect()));

                        }
                    }
                    catch (Exception ignored) {}
                }
            }
        }

        // Add the effects of all the blocks together and return the result
        double totalTemp = 0;
        for (Map.Entry<Block, Double> effect : effectAmounts.entrySet())
        {
            totalTemp += effect.getValue();
        }

        return temp.add(totalTemp);
    }

    public String getID()
    {
        return "cold_sweat:nearby_blocks";
    }

    LevelChunk getChunk(Level world, ChunkPos pos, Map<ChunkPos, LevelChunk> chunks)
    {
        ChunkPos chunkPos = new ChunkPos(pos.x, pos.z);
        LevelChunk chunk = chunks.get(chunkPos);
        if (chunk == null)
        {
            chunk = world.getChunkSource().getChunkNow(chunkPos.x, chunkPos.z);
            chunks.put(chunkPos, chunk);
        }
        return chunk;
    }
}