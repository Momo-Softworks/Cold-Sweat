package dev.momostudios.coldsweat.common.temperature.modifier;

import dev.momostudios.coldsweat.common.world.BlockEffectEntries;
import net.minecraft.block.BlockState;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.util.Direction;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.vector.Vector3d;
import net.minecraft.world.chunk.Chunk;
import dev.momostudios.coldsweat.common.temperature.Temperature;
import dev.momostudios.coldsweat.common.temperature.modifier.block.BlockEffect;
import dev.momostudios.coldsweat.util.CSMath;
import dev.momostudios.coldsweat.util.WorldHelper;

public class BlockTempModifier extends TempModifier
{
    public BlockTempModifier()
    {
        addArgument("value", 0.0);
    }

    @Override
    public double getResult(Temperature temp, PlayerEntity player)
    {
        if (player.ticksExisted % 5 > 0)
        {
            return temp.get() + (double) this.getArgument("value");
        }

        double totalTemp = 0;

        for (int x1 = -7; x1 < 14; x1++)
        {
            for (int y1 = -7; y1 < 14; y1++)
            {
                for (int z1 = -7; z1 < 14; z1++)
                {
                    try
                    {
                        BlockPos blockpos = player.getPosition().add(x1, y1, z1);
                        Chunk chunk = player.world.getChunkProvider().getChunkNow(blockpos.getX() >> 4, blockpos.getZ() >> 4);

                        if (chunk == null) continue;

                        // Get the BlockEffect associated with the block
                        BlockState state = chunk.getBlockState(blockpos);
                        BlockEffect be = BlockEffectEntries.getEntries().getEntryFor(state);

                        if (be == null) continue;


                        // Is totalTemp within the bounds of the BlockEffect's min/max allowed temps?
                        if (CSMath.isBetween(totalTemp, be.minEffect(), be.maxEffect())
                        && CSMath.isBetween(temp.get() + totalTemp, be.minTemperature(), be.maxTemperature()))
                        {
                            // Get Vector positions of the centers of the source block and player
                            Vector3d pos = new Vector3d(blockpos.getX() + 0.5, blockpos.getY() + 0.5, blockpos.getZ() + 0.5);
                            Vector3d playerPos1 = new Vector3d(player.getPosX(), player.getPosY() + player.getHeight() * 0.25, player.getPosZ());
                            Vector3d playerPos2 = new Vector3d(player.getPosX(), player.getPosY() + player.getEyeHeight(), player.getPosZ());

                            // Get the temperature of the block given the player's distance
                            double tempToAdd = be.getTemperature(player, state, blockpos, CSMath.getDistance(player, pos));

                            // Lessen the effect with each block between the player and the block
                            Vector3d prevPos1 = playerPos1;
                            Vector3d prevPos2 = playerPos2;
                            int blocksBetween = 0;
                            for (int i = 0; i < 15; i++)
                            {
                                // Get the next block (sub)position
                                Vector3d newPos1 = playerPos1.subtract(playerPos1.subtract(pos).scale(i / 15.0));
                                Vector3d newPos2 = playerPos2.subtract(playerPos2.subtract(pos).scale(i / 15.0));

                                //player.world.addParticle(ParticleTypes.FLAME, newPos1.x, newPos1.y, newPos1.z, 0, 0, 0);
                                //player.world.addParticle(ParticleTypes.FLAME, newPos2.x, newPos2.y, newPos2.z, 0, 0, 0);

                                // Check if the newPos1 is not a duplicate BlockPos or solid block
                                BlockPos bpos1 = new BlockPos(newPos1);
                                Vector3d facing1 = newPos1.subtract(prevPos1);
                                Direction dir1 = Direction.getFacingFromVector(facing1.x, facing1.y, facing1.z);

                                if (!bpos1.equals(new BlockPos(prevPos1)) && !bpos1.equals(blockpos)
                                && !chunk.getBlockState(bpos1).isAir()
                                && WorldHelper.isFullSide(chunk.getBlockState(bpos1), dir1, bpos1, player.world)
                                && WorldHelper.isFullSide(chunk.getBlockState(bpos1), dir1.getOpposite(), bpos1, player.world))
                                {
                                    // Divide the added temperature by 2 for each block between the player and the block
                                    blocksBetween++;
                                }

                                // Check if the newPos2 is not a duplicate BlockPos or solid block
                                BlockPos bpos2 = new BlockPos(newPos2);
                                Vector3d facing2 = newPos2.subtract(prevPos2);
                                Direction dir2 = Direction.getFacingFromVector(facing2.x, facing2.y, facing2.z);

                                if (!bpos2.equals(new BlockPos(prevPos2)) && !bpos2.equals(blockpos)
                                && !chunk.getBlockState(bpos2).isAir()
                                && WorldHelper.isFullSide(chunk.getBlockState(bpos2), dir2, bpos2, player.world)
                                && WorldHelper.isFullSide(chunk.getBlockState(bpos2), dir2.getOpposite(), bpos2, player.world))
                                {
                                    // Divide the added temperature by 2 for each block between the player and the block
                                    blocksBetween++;
                                }

                                prevPos1 = newPos1;
                                prevPos2 = newPos2;
                            }
                            double blockDampening = Math.pow(1.5, blocksBetween);

                            totalTemp += tempToAdd / blockDampening;
                        }
                    }
                    catch (Exception e) {
                        e.printStackTrace();
                    }
                }
            }
        }
        
        setArgument("value", totalTemp);
        return temp.get() + totalTemp;
    }

    public String getID()
    {
        return "cold_sweat:nearby_blocks";
    }
}