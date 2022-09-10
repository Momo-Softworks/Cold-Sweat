package dev.momostudios.coldsweat.api.temperature.modifier;

import com.mojang.math.Vector3d;
import dev.momostudios.coldsweat.api.registry.BlockTempRegistry;
import dev.momostudios.coldsweat.api.temperature.Temperature;
import dev.momostudios.coldsweat.api.temperature.block_temp.BlockTemp;
import dev.momostudios.coldsweat.util.math.CSMath;
import dev.momostudios.coldsweat.util.world.WorldHelper;
import net.minecraft.core.BlockPos;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.state.BlockState;
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
                LevelChunk chunk = level.getChunkSource().getChunkNow((player.blockPosition().getX() + x) >> 4, (player.blockPosition().getZ() + z) >> 4);
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

                            // Get the temperature of the block given the player's distance
                            double distance = CSMath.getDistance(player, new Vector3d(pos.x, pos.y, pos.z));
                            double tempToAdd = be.getTemperature(player, state, blockpos, distance);

                            // Cast a ray between the player and the block
                            // Lessen the effect with each block between the player and the block
                            AtomicInteger blocks = new AtomicInteger();
                            Vec3 playerPos = player.position().add(0, player.getBbHeight() / 2, 0);

                            WorldHelper.forBlocksInRay(playerPos, pos, level,
                            (rayState, bpos) ->
                            {
                                if (WorldHelper.isSpreadBlocked(level, rayState, bpos, CSMath.getDirectionFromVector(pos.subtract(playerPos))))
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