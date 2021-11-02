package net.momostudios.coldsweat.common.temperature.modifier;

import net.minecraft.block.BlockState;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.util.math.AxisAlignedBB;
import net.minecraft.util.math.BlockPos;
import net.minecraftforge.registries.IForgeRegistryEntry;
import net.momostudios.coldsweat.common.temperature.Temperature;
import net.momostudios.coldsweat.common.temperature.modifier.block.BlockEffect;
import net.momostudios.coldsweat.common.world.BlockEffectEntries;

import java.util.ArrayList;
import java.util.List;

public class BlockTempModifier extends TempModifier
{
    @Override
    public double calculate(Temperature temp, PlayerEntity player)
    {
        final double[] totalTemp = {0};
        int x = player.getPosition().getX();
        int y = player.getPosition().getY();
        int z = player.getPosition().getZ();

        BlockPos.getAllInBox(new AxisAlignedBB(x - 7, y - 7, z - 7, x + 7, y + 7, z + 7)).forEach(blockpos ->
        {
            BlockState state = player.world.getBlockState(blockpos);
            BlockEffect be = BlockEffectEntries.getEntries().getEntryFor(state.getBlock());
            if (be != null)
            {
                if (totalTemp[0] < be.maxTemp() && totalTemp[0] > be.minTemp())
                {
                    totalTemp[0] += be.getTemperature(player, state, blockpos,
                            Math.sqrt(player.getDistanceSq(blockpos.getX() + 0.5, blockpos.getY() + 0.5, blockpos.getZ() + 0.5)));
                }
            }
        });

        return temp.get() + totalTemp[0];
    }

    public String getID()
    {
        return "cold_sweat:nearby_blocks";
    }
}