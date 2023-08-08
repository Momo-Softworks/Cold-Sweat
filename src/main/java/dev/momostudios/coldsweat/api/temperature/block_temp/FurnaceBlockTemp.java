package dev.momostudios.coldsweat.api.temperature.block_temp;

import dev.momostudios.coldsweat.api.util.Temperature;
import net.minecraft.core.BlockPos;
import dev.momostudios.coldsweat.util.math.CSMath;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.AbstractFurnaceBlock;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraftforge.registries.ForgeRegistries;

public class FurnaceBlockTemp extends BlockTemp
{
    public FurnaceBlockTemp()
    {
        super(ForgeRegistries.BLOCKS.getValues().stream().filter(block -> block instanceof AbstractFurnaceBlock).toArray(Block[]::new));
    }

    @Override
    public double getTemperature(Level level, LivingEntity entity, BlockState state, BlockPos pos, double distance)
    {
        if (state.getValue(AbstractFurnaceBlock.LIT))
        {   return CSMath.blend(CSMath.convertTemp(15, Temperature.Units.F, Temperature.Units.MC, false), 0, distance, 0.5, 7);
        }
        return 0;
    }

    @Override
    public boolean hasBlock(Block block)
    {
        return block instanceof AbstractFurnaceBlock;
    }

    @Override
    public double maxEffect() {
        return CSMath.convertTemp(40, Temperature.Units.F, Temperature.Units.MC, false);
    }

    @Override
    public double maxTemperature() {
        return CSMath.convertTemp(600, Temperature.Units.F, Temperature.Units.MC, true);
    }
}
