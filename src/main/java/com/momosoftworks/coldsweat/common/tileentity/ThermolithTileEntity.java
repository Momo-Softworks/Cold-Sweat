package com.momosoftworks.coldsweat.common.tileentity;

import com.momosoftworks.coldsweat.api.util.Temperature;
import com.momosoftworks.coldsweat.common.block.ThermolithBlock;
import com.momosoftworks.coldsweat.core.init.TileEntityInit;
import com.momosoftworks.coldsweat.config.ConfigSettings;
import com.momosoftworks.coldsweat.util.math.CSMath;
import net.minecraft.block.BlockState;
import net.minecraft.tileentity.ITickableTileEntity;
import net.minecraft.tileentity.TileEntity;
import net.minecraft.util.math.BlockPos;

public class ThermolithTileEntity extends TileEntity implements ITickableTileEntity
{
    private int signal = 0;

    public ThermolithTileEntity()
    {
        super(TileEntityInit.THERMOLITH_BLOCK_ENTITY_TYPE.get());
    }

    @Override
    public void tick()
    {
        if (level.getGameTime() % 10 == 0 && !level.isClientSide)
        {
            BlockPos pos = this.getBlockPos();
            BlockState state = this.getBlockState();
            // Handle signal output / neighbor updates
            double temperature = Temperature.getTemperatureAt(pos, level);
            int newSignal = (int) CSMath.blend(0, 15, temperature, ConfigSettings.MIN_TEMP.get(), ConfigSettings.MAX_TEMP.get());

            if (newSignal != signal)
            {
                signal = newSignal;
                level.updateNeighborsAt(pos, state.getBlock());
            }

            // Handle turning on/off
            if (signal == 0)
            {   if (state.getValue(ThermolithBlock.POWERED))
                {   level.setBlockAndUpdate(pos, state.setValue(ThermolithBlock.POWERED, false));
                }
            }
            else if (!state.getValue(ThermolithBlock.POWERED))
            {   level.setBlockAndUpdate(pos, state.setValue(ThermolithBlock.POWERED, true));
            }
        }
    }

    public int getSignal()
    {
        return signal;
    }
}
