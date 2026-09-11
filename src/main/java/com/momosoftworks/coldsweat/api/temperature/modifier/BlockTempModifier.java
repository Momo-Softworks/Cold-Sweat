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
    Map<BlockTemp, BlockEffectData> blockTempTotals = new HashMap<>(16);
    Map<TagKey<BlockTempData>, Double> groupTotals = new HashMap<>(8);
    Long2ObjectOpenHashMap<BlockState> stateCache = new Long2ObjectOpenHashMap<>(3000);
    List<Triplet<BlockPos, BlockTemp, Double>> triggers = new ArrayList<>(16);

    long lastTick = 0;

    @Override
    public Function<Double, Double> calculate(LivingEntity entity, Temperature.Trait trait)
    {
        groupTotals.clear();
        blockTempTotals.clear();
        triggers.clear();

        Level level = entity.level;
        long gameTime = level.getGameTime();
        if (lastTick != gameTime)
        {
            lastTick = gameTime;
            stateCache.clear();
            chunks.clear();
        }

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

                    for (BlockTemp blockTemp : blockTemps)
                    {   blockTempTotals.putIfAbsent(blockTemp, new BlockEffectData(entity, blockTemp, level, blockpos, state));
                    }

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

                            BlockEffectData blockEffectData = blockTempTotals.get(blockTemp);
                            if (blockEffectData == null) continue;

                            double tempToAdd = blockEffectData.fades()
                                               ? CSMath.blend(temperature, 0, distance, 0.5, blockEffectData.range())
                                               : temperature;

                            double blockGroupTotal = this.getGroupTotal(blockTemp);
                            double blockGroupDelta = blockGroupTotal - blockEffectData.getTotalEffect();

                            if (blockTemp.isLogarithmic(entity, level, blockpos, state))
                            {   // Calculate amount of increase
                                double newTotal = Math.pow(Math.pow(blockEffectData.getTotalEffect(), 1/LOG_FACTOR) + tempToAdd, LOG_FACTOR);
                                double delta = newTotal - blockEffectData.getTotalEffect();
                                // Dampen the effect with each block between the player and the source
                                delta /= (blocks[0] + 1);
                                // Store this block type's total effect on the player
                                double newVal = CSMath.clamp(blockEffectData.getTotalEffect() + delta,
                                                             blockEffectData.minEffect() + blockGroupDelta,
                                                             blockEffectData.maxEffect() - blockGroupDelta);
                                blockEffectData.setTotalEffect(newVal);
                                updateGroupTotal(blockTemp, newVal - blockEffectData.getTotalEffect());
                            }
                            else
                            {   // Dampen the effect with each block between the player and the source
                                tempToAdd /= (blocks[0] + 1);
                                // Store this block type's total effect on the player
                                double newVal = CSMath.clamp(blockEffectData.getTotalEffect() + tempToAdd,
                                                             blockEffectData.minEffect() + blockGroupDelta,
                                                             blockEffectData.maxEffect() - blockGroupDelta);
                                blockEffectData.setTotalEffect(newVal);
                                updateGroupTotal(blockTemp, newVal - blockEffectData.getTotalEffect());
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
            {   ModAdvancementTriggers.BLOCK_AFFECTS_TEMP.trigger(player, trigger.getA(), trigger.getC(), blockTempTotals.get(trigger.getB()).getTotalEffect());
            }
        }

        // Remove old chunks from the cache
        while (chunks.size() >= 16)
        {   chunks.remove(chunks.keySet().iterator().next());
        }

        // Add the effects of all the blocks together and return the result
        Map<BlockTemp, BlockEffectData> totals = new HashMap<>(blockTempTotals);
        return temp ->
        {
            for (Map.Entry<BlockTemp, BlockEffectData> entry : totals.entrySet())
            {
                BlockEffectData data = entry.getValue();
                double min = data.minTemp();
                double max = data.maxTemp();
                if (!CSMath.betweenInclusive(temp, min, max)) continue;
                double effectValue = data.getTotalEffect();
                temp = CSMath.clamp(temp + effectValue, min, max);
            }
            return temp;
        };
    }

    private boolean areAnyBlockTempsInRange(Collection<BlockTemp> blockTemps)
    {
        for (BlockTemp blockTemp : blockTemps)
        {
            BlockEffectData blockTempTotal = blockTempTotals.get(blockTemp);
            if (blockTempTotal == null)
            {   return true;
            }
            double effectTotal = getGroupTotal(blockTemp);
            if (CSMath.betweenInclusive(effectTotal, blockTempTotal.minEffect(), blockTempTotal.maxEffect()))
            {   return true;
            }
        }
        return false;
    }

    private double getGroupTotal(BlockTemp blockTemp)
    {
        if (!(blockTemp instanceof ConfiguredBlockTemp config) || config.getData().effectGroup().isEmpty())
        {   return this.blockTempTotals.get(blockTemp).getTotalEffect();
        }
        return groupTotals.getOrDefault(config.getData().effectGroup().get(), 0d);
    }

    private void updateGroupTotal(BlockTemp blockTemp, double delta)
    {
        if (blockTemp instanceof ConfiguredBlockTemp config && config.getData().effectGroup().isPresent())
        {   groupTotals.merge(config.getData().effectGroup().get(), delta, Double::sum);
        }
    }

    protected static class BlockEffectData
    {
        private final double maxTemp;
        private final double minTemp;
        private final double maxEffect;
        private final double minEffect;
        private final double range;
        private final boolean fade;
        private double totalEffect;

        public BlockEffectData(LivingEntity entity, BlockTemp blockTemp, Level level, BlockPos pos, BlockState state)
        {
            this.maxTemp = blockTemp.getMaxTemp(entity, level, pos, state);
            this.minTemp = blockTemp.getMinTemp(entity, level, pos, state);
            this.maxEffect = blockTemp.getMaxEffect(entity, level, pos, state);
            this.minEffect = blockTemp.getMinEffect(entity, level, pos, state);
            this.range = blockTemp.getRange(entity, level, pos, state);
            this.fade = blockTemp.fades(entity, level, pos, state);
            this.totalEffect = 0;
        }

        public double maxTemp()
        {   return maxTemp;
        }

        public double minTemp()
        {   return minTemp;
        }

        public double getTotalEffect()
        {   return totalEffect;
        }

        public double maxEffect()
        {   return maxEffect;
        }

        public double minEffect()
        {   return minEffect;
        }

        public double range()
        {   return range;
        }

        public boolean fades()
        {   return fade;
        }

        public void setTotalEffect(double effect)
        {   this.totalEffect = effect;
        }
    }
}