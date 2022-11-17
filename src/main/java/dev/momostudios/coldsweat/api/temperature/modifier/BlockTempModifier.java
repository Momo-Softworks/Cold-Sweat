package dev.momostudios.coldsweat.api.temperature.modifier;

import dev.momostudios.coldsweat.api.registry.BlockTempRegistry;
import dev.momostudios.coldsweat.api.temperature.Temperature;
import dev.momostudios.coldsweat.api.temperature.block_temp.BlockTemp;
import dev.momostudios.coldsweat.util.math.CSMath;
import dev.momostudios.coldsweat.util.world.WorldHelper;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.chunk.ChunkStatus;
import net.minecraft.world.level.chunk.LevelChunk;
import net.minecraft.world.level.chunk.LevelChunkSection;
import net.minecraft.world.phys.Vec3;

import java.util.HashMap;
import java.util.Map;
import java.util.concurrent.atomic.AtomicInteger;
import java.util.function.Function;

public class BlockTempModifier extends TempModifier
{
    public BlockTempModifier() {}

    @Override
    public Function<Temperature, Temperature> calculate(Player player)
    {
        Map<BlockTemp, Double> effectAmounts = new HashMap<>();

        Level level = player.level;

        for (int x = -7; x < 7; x++)
        {
            for (int z = -7; z < 7; z++)
            {
                LevelChunk chunk = (LevelChunk) level.getChunkSource().getChunk((player.blockPosition().getX() + x) >> 4, (player.blockPosition().getZ() + z) >> 4, ChunkStatus.FULL, false);
                if (chunk == null) continue;

                for (int y = -7; y < 7; y++)
                {
                    try
                    {
                        BlockPos blockpos = player.blockPosition().offset(x, y, z);

                        LevelChunkSection subchunk = WorldHelper.getChunkSection(chunk, blockpos.getY());
                        BlockState state = subchunk.getBlockState(blockpos.getX() & 15, blockpos.getY() & 15, blockpos.getZ() & 15);

                        if (state.isAir()) continue;

                        // Get the BlockTemp associated with the block
                        BlockTemp be = BlockTempRegistry.getEntryFor(state);

                        if (be == null || be.equals(BlockTempRegistry.DEFAULT_BLOCK_EFFECT)) continue;

                        // Get the amount that this block has affected the player so far
                        double effectAmount = effectAmounts.getOrDefault(be, 0.0);

                        // Is totalTemp within the bounds of the BlockTemp's min/max allowed temps?
                        if (CSMath.isBetween(effectAmount, be.minEffect(), be.maxEffect()))
                        {
                            // Get Vector positions of the centers of the source block and player
                            Vec3 pos = new Vec3(blockpos.getX() + 0.5, blockpos.getY() + 0.5, blockpos.getZ() + 0.5);

                            // Cast a ray between the player and the block
                            // Lessen the effect with each block between the player and the block
                            AtomicInteger blocks = new AtomicInteger();

                            // Gets the closest point in the player's BB to the block
                            double playerRadius = player.getBbWidth() / 2;
                            Vec3 playerClosest = new Vec3(CSMath.clamp(pos.x, player.getX() - playerRadius, player.getX() + playerRadius),
                                                          CSMath.clamp(pos.y, player.getY(), player.getY() + player.getBbHeight()),
                                                          CSMath.clamp(pos.z, player.getZ() - playerRadius, player.getZ() + playerRadius));

                            // Get the temperature of the block given the player's distance
                            double distance = CSMath.getDistance(playerClosest, pos);
                            double tempToAdd = be.getTemperature(player, state, blockpos, distance);

                            Vec3 ray = pos.subtract(playerClosest);
                            Direction direction = Direction.getNearest(ray.x, ray.y, ray.z);
                            WorldHelper.forBlocksInRay(playerClosest, pos, level, chunk,
                            (rayChunk, rayState, bpos) ->
                            {
                                if (!bpos.equals(blockpos) && WorldHelper.isSpreadBlocked(level, rayState, bpos, direction, direction))
                                    blocks.getAndIncrement();
                            }, 3);

                            // Calculate the decrease in effectiveness due to blocks in the way
                            double blockDampening = blocks.get();

                            // Store this block type's total effect on the player
                            double blockTempTotal = effectAmount + tempToAdd / (blockDampening + 1);
                            effectAmounts.put(be, CSMath.clamp(blockTempTotal, be.minEffect(), be.maxEffect()));
                        }
                    }
                    catch (Exception ignored) {}
                }
            }
        }


        // Add the effects of all the blocks together and return the result
        return temp ->
        {
            for (Map.Entry<BlockTemp, Double> effect : effectAmounts.entrySet())
            {
                BlockTemp be = effect.getKey();
                double min = be.minTemperature();
                double max = be.maxTemperature();
                if (!CSMath.isInRange(temp.get(), min, max)) continue;
                temp.set(CSMath.clamp(temp.get() + effect.getValue(), min, max));
            }
            return temp;
        };
    }

    public String getID()
    {
        return "cold_sweat:nearby_blocks";
    }
}