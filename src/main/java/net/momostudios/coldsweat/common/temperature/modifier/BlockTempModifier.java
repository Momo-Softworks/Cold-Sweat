package net.momostudios.coldsweat.common.temperature.modifier;

import net.minecraft.block.BlockState;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.util.math.AxisAlignedBB;
import net.minecraft.util.math.BlockPos;
import net.minecraftforge.registries.IForgeRegistryEntry;
import net.momostudios.coldsweat.common.temperature.Temperature;
import net.momostudios.coldsweat.common.temperature.modifier.block.BlockEffect;
import net.momostudios.coldsweat.common.world.BlockEffectEntries;
import net.momostudios.coldsweat.core.util.MathHelperCS;
import net.momostudios.coldsweat.core.util.registrylists.ModBlocks;

import java.util.ArrayList;
import java.util.List;
import java.util.stream.Stream;

public class BlockTempModifier extends TempModifier
{
    public BlockTempModifier()
    {
        addArgument("value", 0.0);
    }

    @Override
    public double getValue(Temperature temp, PlayerEntity player)
    {
        if (player.ticksExisted % 2 == 0)
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

                        BlockState state = player.world.getBlockState(blockpos);
                        BlockEffect be = BlockEffectEntries.getEntries().getEntryFor(state.getBlock());
                        if (be != null)
                        {
                            if (MathHelperCS.isBetween(totalTemp, be.minTemp(), be.maxTemp()))
                            {
                                totalTemp += be.getTemperature(player, state, blockpos,
                                        Math.sqrt(player.getDistanceSq(blockpos.getX() + 0.5, blockpos.getY() + 0.5, blockpos.getZ() + 0.5)));
                            }
                        }
                    } catch (Exception e) {}
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