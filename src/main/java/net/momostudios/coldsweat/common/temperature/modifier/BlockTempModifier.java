package net.momostudios.coldsweat.common.temperature.modifier;

import net.minecraft.block.BlockState;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.util.math.BlockPos;
import net.minecraftforge.registries.IForgeRegistryEntry;
import net.momostudios.coldsweat.common.temperature.Temperature;
import net.momostudios.coldsweat.common.world.BlockEffectEntries;

public class BlockTempModifier extends TempModifier
{
    @Override
    public double calculate(Temperature temp, PlayerEntity player)
    {
        double totalTemp = 0;
        int x = player.getPosition().getX();
        int y = player.getPosition().getY();
        int z = player.getPosition().getZ();
        int blocksDetected = 0;

        for (int x1 = -7; x1 < 14; x1++)
        {
            for (int y1 = -7; y1 < 14; y1++)
            {
                for (int z1 = -7; z1 < 14; z1++)
                {
                    BlockPos blockpos = new BlockPos(x + x1, y + y1, z + z1);
                    BlockState state = player.world.getBlockState(blockpos);
                    if (BlockEffectEntries.getEntries().getEntryFor(state.getBlock()) != null)
                    {
                        totalTemp += BlockEffectEntries.getEntries().getEntryFor(state.getBlock()).getTemperature(player, state, blockpos,
                            Math.sqrt(player.getDistanceSq(blockpos.getX(), blockpos.getY(), blockpos.getZ())));
                        blocksDetected++;
                    }
                }
            }
        }

        return temp.get() + totalTemp;
    }

    public String getID()
    {
        return "cold_sweat:nearby_blocks";
    }
}