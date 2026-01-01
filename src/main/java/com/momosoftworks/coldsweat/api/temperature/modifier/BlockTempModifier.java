package com.momosoftworks.coldsweat.api.temperature.modifier;

import com.momosoftworks.coldsweat.api.registry.BlockTempRegistry;
import com.momosoftworks.coldsweat.api.temperature.block_temp.BlockTemp;
import com.momosoftworks.coldsweat.api.temperature.block_temp.ConfiguredBlockTemp;
import com.momosoftworks.coldsweat.api.util.Temperature;
import com.momosoftworks.coldsweat.config.ConfigSettings;
import com.momosoftworks.coldsweat.core.advancement.trigger.ModAdvancementTriggers;
import com.momosoftworks.coldsweat.data.codec.configuration.BlockTempData;
import com.momosoftworks.coldsweat.util.math.CSMath;
import com.momosoftworks.coldsweat.util.world.WorldHelper;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.tags.TagKey;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.level.ChunkPos;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.chunk.ChunkAccess;
import net.minecraft.world.level.chunk.LevelChunkSection;
import net.minecraft.world.phys.Vec3;
import oshi.util.tuples.Triplet;

import java.util.*;
import java.util.function.Function;

public class BlockTempModifier extends TempModifier
{
    protected static final double LOG_FACTOR = 0.52;

    public BlockTempModifier() {}

    public BlockTempModifier(int range)
    {   if (range > 0) this.getNBT().putInt("RangeOverride", range);
    }

    Map<ChunkPos, ChunkAccess> chunks = new HashMap<>(9);
    Map<BlockTemp, Double> blockTempTotals = new HashMap<>(16);
    Map<BlockPos, BlockState> stateCache = new HashMap<>(3000);
    List<Triplet<BlockPos, BlockTemp, Double>> triggers = new ArrayList<>(16);

    @Override
    public Function<Double, Double> calculate(LivingEntity entity, Temperature.Trait trait)
    {
        blockTempTotals.clear();
        stateCache.clear();
        triggers.clear();

        Level level = entity.level();
        int range = this.getNBT().contains("RangeOverride", 3) ? this.getNBT().getInt("RangeOverride") : ConfigSettings.BLOCK_RANGE.get();
        BlockPos blockPos = entity.blockPosition();

        int entX = blockPos.getX();
        int entY = blockPos.getY();
        int entZ = blockPos.getZ();
        BlockPos.MutableBlockPos blockpos = new BlockPos.MutableBlockPos();

        // Only tick advancements every second, because Minecraft advancements are not performant at all
        boolean shouldTickAdvancements = this.getTicksExisted() % 20 == 0;

        for (int x = -range; x < range; x++)
        {
            for (int z = -range; z < range; z++)
            {
                ChunkPos chunkPos = new ChunkPos((entX + x) >> 4, (entZ + z) >> 4);
                ChunkAccess chunk = chunks.get(chunkPos);
                if (chunk == null) chunks.put(chunkPos, chunk = WorldHelper.getChunk(level, chunkPos));
                if (chunk == null) continue;

                for (int y = -range; y < range; y++)
                {
                        blockpos.set(entX + x, entY + y, entZ + z);

                        BlockState state = stateCache.get(blockpos);
                        if (state == null)
                        {   LevelChunkSection section = WorldHelper.getChunkSection(chunk, blockpos.getY());
                            state = section.getBlockState(blockpos.getX() & 15, blockpos.getY() & 15, blockpos.getZ() & 15);
                            stateCache.put(blockpos.immutable(), state);
                        }

                        if (state.isAir()) continue;

                        // Get the BlockTemp associated with the block
                        Collection<BlockTemp> blockTemps = BlockTempRegistry.getBlockTempsFor(state);

                        if (blockTemps.isEmpty() || (blockTemps.size() == 1 && blockTemps.contains(BlockTempRegistry.DEFAULT_BLOCK_TEMP))) continue;

                        // Are any of the block temps able to affect the entity?
                        // This check prevents costly calculations if the block can't affect the entity anyway
                        if (this.areAnyBlockTempsInRange(blockTemps))
                        {
                            // Get Vector positions of the centers of the source block and player
                            Vec3 pos = Vec3.atCenterOf(blockpos);

                            // Gets the closest point in the player's BB to the block
                            Vec3 playerClosest = WorldHelper.getClosestPointOnEntity(entity, pos);

                            // Cast a ray between the player and the block
                            // Lessen the effect with each block between the player and the block
                            int[] blocks = new int[1];
                            Vec3 ray = pos.subtract(playerClosest);
                            Direction direction = Direction.getNearest(ray.x, ray.y, ray.z);

                            WorldHelper.forBlocksInRay(playerClosest, pos, level, chunk, stateCache,
                            (rayState, bpos) ->
                            {   if (!bpos.equals(blockpos) && WorldHelper.isSpreadBlocked(level, rayState, bpos, direction, direction))
                                {   blocks[0]++;
                                }
                            }, 3);

                            // Get the temperature of the block given the player's distance
                            double distance = CSMath.getDistance(playerClosest, pos);

                            for (BlockTemp blockTemp : blockTemps)
                            {
                                if (!blockTemp.isValid(level, blockpos, state)) continue;
                                double temperature = blockTemp.getTemperature(level, entity, state, blockpos, distance);
                                if (temperature == 0) continue;
                                double tempToAdd = blockTemp.fade()
                                                   ? CSMath.blend(temperature, 0, distance, 0.5, blockTemp.range())
                                                   : temperature;

                                double blockTempTotal = blockTempTotals.getOrDefault(blockTemp, 0d);
                                double blockGroupTotal = this.getGroupTotal(blockTemp);
                                double blockGroupDelta = blockGroupTotal - blockTempTotal;

                                if (blockTemp.logarithmic())
                                {   // Calculate amount of increase
                                    double newTotal = Math.pow(Math.pow(blockTempTotal, 1/LOG_FACTOR) + tempToAdd, LOG_FACTOR);
                                    double delta = newTotal - blockTempTotal;
                                    // Dampen the effect with each block between the player and the source
                                    delta /= (blocks[0] + 1);
                                    // Store this block type's total effect on the player
                                    blockTempTotals.put(blockTemp, CSMath.clamp(blockTempTotal + delta,
                                                                                blockTemp.minEffect() + blockGroupDelta,
                                                                                blockTemp.maxEffect() - blockGroupDelta));
                                }
                                else
                                {   // Dampen the effect with each block between the player and the source
                                    tempToAdd /= (blocks[0] + 1);
                                    // Store this block type's total effect on the player
                                    double newTotal = blockTempTotal + tempToAdd;
                                    blockTempTotals.put(blockTemp, CSMath.clamp(newTotal,
                                                                                blockTemp.minEffect() + blockGroupDelta,
                                                                                blockTemp.maxEffect() - blockGroupDelta));
                                }
                                // Used to trigger advancements
                                if (shouldTickAdvancements)
                                {   triggers.add(new Triplet<>(blockpos, blockTemp, distance));
                                }
                                break;
                            }
                        }
                }
            }
        }
        // Trigger advancements at every BlockPos with a BlockEffect attached to it
        if (entity instanceof ServerPlayer player && shouldTickAdvancements)
        {
            for (Triplet<BlockPos, BlockTemp, Double> trigger : triggers)
            {   ModAdvancementTriggers.BLOCK_AFFECTS_TEMP.trigger(player, trigger.getA(), trigger.getC(), blockTempTotals.get(trigger.getB()));
            }
        }

        // Remove old chunks from the cache
        while (chunks.size() >= 16)
        {   chunks.remove(chunks.keySet().iterator().next());
        }

        // Add the effects of all the blocks together and return the result
        return temp ->
        {
            for (Map.Entry<BlockTemp, Double> entry : blockTempTotals.entrySet())
            {
                BlockTemp blockTemp = entry.getKey();
                double min = blockTemp.minTemperature();
                double max = blockTemp.maxTemperature();
                if (!CSMath.betweenInclusive(temp, min, max)) continue;
                double effectValue = entry.getValue();
                temp = CSMath.clamp(temp + effectValue, min, max);
            }
            return temp;
        };
    }

    private boolean areAnyBlockTempsInRange(Collection<BlockTemp> blockTemps)
    {
        for (BlockTemp blockTemp : blockTemps)
        {
            if (!blockTempTotals.containsKey(blockTemp))
            {   return true;
            }
            double effectTotal = getGroupTotal(blockTemp);
            if (CSMath.betweenInclusive(effectTotal, blockTemp.minEffect(), blockTemp.maxEffect()))
            {   return true;
            }
        }
        return false;
    }

    private double getGroupTotal(BlockTemp blockTemp)
    {
        TagKey<BlockTempData> group = blockTemp instanceof ConfiguredBlockTemp config ? config.getData().effectGroup().orElse(null) : null;
        if (group == null) return this.blockTempTotals.getOrDefault(blockTemp, 0d);

        return this.blockTempTotals.keySet().stream()
               .filter(bt -> bt instanceof ConfiguredBlockTemp config && config.isInGroup(group))
               .map(bt -> (ConfiguredBlockTemp) bt).mapToDouble(b -> this.blockTempTotals.getOrDefault(b, 0d))
               .sum();
    }
}