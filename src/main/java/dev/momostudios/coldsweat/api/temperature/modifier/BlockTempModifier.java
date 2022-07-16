package dev.momostudios.coldsweat.api.temperature.modifier;

import com.mojang.math.Vector3d;
import dev.momostudios.coldsweat.api.registry.BlockEffectRegistry;
import dev.momostudios.coldsweat.api.temperature.Temperature;
import dev.momostudios.coldsweat.api.temperature.block_effect.BlockEffect;
import dev.momostudios.coldsweat.util.math.CSMath;
import dev.momostudios.coldsweat.util.world.WorldHelper;
import net.minecraft.core.BlockPos;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.level.ChunkPos;
import net.minecraft.world.level.ClipContext;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.chunk.LevelChunk;
import net.minecraft.world.level.chunk.PalettedContainer;
import net.minecraft.world.phys.Vec3;

import java.util.HashMap;
import java.util.Map;
import java.util.concurrent.atomic.AtomicInteger;
import java.util.function.Function;

public class BlockTempModifier extends TempModifier
{
    Map<ChunkPos, LevelChunk> chunkMap = new HashMap<>();

    public BlockTempModifier() {}

    @Override
    public Function<Temperature, Temperature> calculate(Player player)
    {
        Map<BlockEffect, Double> effectAmounts = new HashMap<>();
        ChunkPos playerChunkPos = new ChunkPos((player.blockPosition().getX()) >> 4, (player.blockPosition().getZ()) >> 4);

        if (player.tickCount % 200 == 0)
        {
            chunkMap.keySet().removeIf(chunkPos -> chunkPos.getChessboardDistance(playerChunkPos) > 1);
        }

        Level level = player.level;

        for (int x = -7; x < 7; x++)
        {
            for (int z = -7; z < 7; z++)
            {
                ChunkPos chunkPos = new ChunkPos((player.blockPosition().getX() + x) >> 4, (player.blockPosition().getZ() + z) >> 4);
                LevelChunk chunk = getChunk(level, chunkPos, chunkMap);

                for (int y = -7; y < 7; y++)
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
                        double effectAmount = effectAmounts.getOrDefault(be, 0.0);

                        // Is totalTemp within the bounds of the BlockEffect's min/max allowed temps?
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
                            WorldHelper.gatherRTResults(new ClipContext(player.position().add(0, player.getBbHeight() / 2, 0), pos,
                                    ClipContext.Block.VISUAL, ClipContext.Fluid.NONE, player),
                                    (ctx, bpos) ->
                                    {
                                        BlockState rayState = level.getChunkSource().getChunkNow(bpos.getX() >> 4, bpos.getZ() >> 4).getBlockState(bpos);
                                        if (rayState.isCollisionShapeFullBlock(level, bpos))
                                        {
                                            blocks.getAndIncrement();
                                        }
                                        return rayState;
                                    });

                            // Calculate the decrease in effectiveness due to blocks in the way
                            double blockDampening = blocks.get();

                            // Store this block type's total effect on the player
                            double blockEffectTotal = effectAmount + tempToAdd / (blockDampening * 2 + 1);
                            effectAmounts.put(be, CSMath.clamp(blockEffectTotal, be.minEffect(), be.maxEffect()));
                        }
                    }
                    catch (Exception ignored) {
                    }
                }
            }
        }


        // Add the effects of all the blocks together and return the result
        return temp ->
        {
            for (Map.Entry<BlockEffect, Double> effect : effectAmounts.entrySet())
            {
                BlockEffect be = effect.getKey();
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