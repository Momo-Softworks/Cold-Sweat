package dev.momostudios.coldsweat.api.temperature.modifier;

import dev.momostudios.coldsweat.api.registry.BlockTempRegistry;
import dev.momostudios.coldsweat.api.temperature.block_temp.BlockTemp;
import dev.momostudios.coldsweat.api.util.Temperature;
import dev.momostudios.coldsweat.core.advancement.trigger.ModAdvancementTriggers;
import dev.momostudios.coldsweat.util.math.CSMath;
import dev.momostudios.coldsweat.util.world.WorldHelper;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.entity.LivingEntity;
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
    public BlockTempModifier()
    {
        this(7);
    }

    public BlockTempModifier(int range)
    {
        this.getNBT().putInt("Range", range);
    }

    @Override
    public Function<Double, Double> calculate(LivingEntity entity, Temperature.Type type)
    {
        Map<BlockTemp, Double> effectAmounts = new HashMap<>();

        Level level = entity.level;
        int range = this.getNBT().getInt("Range");
        // Failsafe for old TempModifiers
        if (range < 1) {
            range = 7;
            this.getNBT().putInt("Range", 7);
        }

        for (int x = -range; x < range; x++)
        {
            for (int z = -range; z < range; z++)
            {
                LevelChunk chunk = (LevelChunk) level.getChunkSource().getChunk((entity.blockPosition().getX() + x) >> 4, (entity.blockPosition().getZ() + z) >> 4, ChunkStatus.FULL, false);
                if (chunk == null) continue;

                for (int y = -range; y < range; y++)
                {
                    try
                    {
                        BlockPos blockpos = entity.blockPosition().offset(x, y, z);

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
                            Vec3 pos = Vec3.atCenterOf(blockpos);

                            // Cast a ray between the player and the block
                            // Lessen the effect with each block between the player and the block
                            AtomicInteger blocks = new AtomicInteger();

                            // Gets the closest point in the player's BB to the block
                            double playerRadius = entity.getBbWidth() / 2;
                            Vec3 playerClosest = new Vec3(CSMath.clamp(pos.x, entity.getX() - playerRadius, entity.getX() + playerRadius),
                                                          CSMath.clamp(pos.y, entity.getY(), entity.getY() + entity.getBbHeight()),
                                                          CSMath.clamp(pos.z, entity.getZ() - playerRadius, entity.getZ() + playerRadius));

                            // Get the temperature of the block given the player's distance
                            double distance = CSMath.getDistance(playerClosest, pos);
                            double tempToAdd = be.getTemperature(level, entity, state, blockpos, distance);

                            Vec3 ray = pos.subtract(playerClosest);
                            Direction direction = Direction.getNearest(ray.x, ray.y, ray.z);
                            WorldHelper.forBlocksInRay(playerClosest, pos, level, chunk,
                            (rayChunk, rayState, bpos) ->
                            {
                                if (!bpos.equals(blockpos) && WorldHelper.isSpreadBlocked(level, rayState, bpos, direction, direction))
                                    blocks.getAndIncrement();
                            }, 3);

                            // Store this block type's total effect on the player
                            // Dampen the effect with each block between the player and the block
                            double blockTempTotal = effectAmount + tempToAdd / (blocks.get() + 1);
                            effectAmounts.put(be, CSMath.clamp(blockTempTotal, be.minEffect(), be.maxEffect()));

                            if (entity instanceof ServerPlayer player)
                                ModAdvancementTriggers.BLOCK_AFFECTS_TEMP.trigger(player, blockpos, distance, blockTempTotal);
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
                if (!CSMath.withinRange(temp, min, max)) continue;
                temp = CSMath.clamp(temp + effect.getValue(), min, max);
            }
            return temp;
        };
    }

    public String getID()
    {
        return "cold_sweat:nearby_blocks";
    }
}