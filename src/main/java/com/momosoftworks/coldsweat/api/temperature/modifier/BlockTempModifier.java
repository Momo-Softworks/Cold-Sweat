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
import net.minecraft.world.level.material.Material;
import net.minecraft.world.phys.Vec3;
import oshi.util.tuples.Triplet;

import it.unimi.dsi.fastutil.longs.Long2ObjectOpenHashMap;

import java.util.*;
import java.util.function.Function;

public class BlockTempModifier extends TempModifier
{
    protected static final double LOG_FACTOR = 0.52;

    public BlockTempModifier() {}

    public BlockTempModifier(int range)
    {   if (range > 0) this.getNBT().putInt("RangeOverride", range);
    }

    Map<Long, ChunkAccess> chunks = new LinkedHashMap<>(16, 0.75f, true);
    Map<BlockTemp, Double> blockTempTotals = new HashMap<>(16);
    Map<TagKey<BlockTempData>, Double> groupTotals = new HashMap<>(8);
    Long2ObjectOpenHashMap<BlockState> stateCache = new Long2ObjectOpenHashMap<>(3000);
    List<Triplet<BlockPos, BlockTemp, Double>> triggers = new ArrayList<>(16);

    @Override
    public Function<Double, Double> calculate(LivingEntity entity, Temperature.Trait trait)
    {
        blockTempTotals.clear();
        groupTotals.clear();
        stateCache.clear();
        triggers.clear();

        Level level = entity.level;
        int range = this.getNBT().contains("RangeOverride", 3) ? this.getNBT().getInt("RangeOverride") : ConfigSettings.BLOCK_RANGE.get();
        BlockPos entPos = entity.blockPosition();

        int entX = entPos.getX();
        int entY = entPos.getY();
        int entZ = entPos.getZ();
        BlockPos.MutableBlockPos blockpos = new BlockPos.MutableBlockPos();

        // Only tick advancements every second, because Minecraft advancements are not performant at all
        boolean shouldTickAdvancements = this.getTicksExisted() % 20 == 0;

        ChunkAccess chunk = null;
        long chunkPos = 0;

        for (int x = -range; x < range; x++)
        {
            int chunkX = (entX + x) >> 4;
            for (int z = -range; z < range; z++)
            {
                int chunkZ = (entZ + z) >> 4;
                long newChunkPos = ChunkPos.asLong(chunkX, chunkZ);
                if (chunk == null || newChunkPos != chunkPos)
                {
                    chunkPos = newChunkPos;
                    chunk = chunks.get(chunkPos);
                    if (chunk == null) chunks.put(chunkPos, chunk = WorldHelper.getChunk(level, new ChunkPos(chunkPos)));
                    if (chunk == null) continue;
                }

                for (int y = -range; y < range; y++)
                {
                        blockpos.set(entX + x, entY + y, entZ + z);

                        long blockPosLong = blockpos.asLong();
                        BlockState state = stateCache.get(blockPosLong);
                        if (state == null)
                        {   LevelChunkSection section = WorldHelper.getChunkSection(chunk, blockpos.getY());
                            state = section.getBlockState(blockpos.getX() & 15, blockpos.getY() & 15, blockpos.getZ() & 15);
                            stateCache.put(blockPosLong, state);
                        }

                        if (state.getMaterial() == Material.AIR) continue;

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
                                    double newVal = CSMath.clamp(blockTempTotal + delta,
                                                                 blockTemp.minEffect() + blockGroupDelta,
                                                                 blockTemp.maxEffect() - blockGroupDelta);
                                    blockTempTotals.put(blockTemp, newVal);
                                    updateGroupTotal(blockTemp, newVal - blockTempTotal);
                                }
                                else
                                {   // Dampen the effect with each block between the player and the source
                                    tempToAdd /= (blocks[0] + 1);
                                    // Store this block type's total effect on the player
                                    double newVal = CSMath.clamp(blockTempTotal + tempToAdd,
                                                                 blockTemp.minEffect() + blockGroupDelta,
                                                                 blockTemp.maxEffect() - blockGroupDelta);
                                    blockTempTotals.put(blockTemp, newVal);
                                    updateGroupTotal(blockTemp, newVal - blockTempTotal);
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
        if (!(blockTemp instanceof ConfiguredBlockTemp config) || config.getData().effectGroup().isEmpty())
        {   return this.blockTempTotals.getOrDefault(blockTemp, 0d);
        }
        return groupTotals.getOrDefault(config.getData().effectGroup().get(), 0d);
    }

    private void updateGroupTotal(BlockTemp blockTemp, double delta)
    {
        if (blockTemp instanceof ConfiguredBlockTemp config && config.getData().effectGroup().isPresent())
        {   groupTotals.merge(config.getData().effectGroup().get(), delta, Double::sum);
        }
    }
}