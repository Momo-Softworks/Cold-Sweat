package dev.momostudios.coldsweat.common.blockentity;

import dev.momostudios.coldsweat.api.registry.TempModifierRegistry;
import dev.momostudios.coldsweat.api.temperature.modifier.BiomeTempModifier;
import dev.momostudios.coldsweat.api.temperature.modifier.BlockTempModifier;
import dev.momostudios.coldsweat.api.temperature.modifier.DepthTempModifier;
import dev.momostudios.coldsweat.api.temperature.modifier.TempModifier;
import dev.momostudios.coldsweat.api.util.Temperature;
import dev.momostudios.coldsweat.common.block.ThermolithBlock;
import dev.momostudios.coldsweat.common.entity.ChameleonEntity;
import dev.momostudios.coldsweat.core.init.BlockEntityInit;
import dev.momostudios.coldsweat.util.compat.CompatManager;
import dev.momostudios.coldsweat.util.config.ConfigSettings;
import dev.momostudios.coldsweat.util.math.CSMath;
import dev.momostudios.coldsweat.util.registries.ModEntities;
import net.minecraft.core.BlockPos;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraft.world.level.block.state.BlockState;

import java.util.ArrayList;
import java.util.List;

public class ThermolithBlockEntity extends BlockEntity
{
    private int signal = 0;

    public ThermolithBlockEntity(BlockPos pos, BlockState state)
    {
        super(BlockEntityInit.THERMOLITH_BLOCK_ENTITY_TYPE.get(), pos, state);
    }

    public static <T extends BlockEntity> void tick(Level level, BlockPos pos, BlockState state, T te)
    {
        if (te instanceof ThermolithBlockEntity thermoTE)
        {
            thermoTE.tick(level, state, pos);
        }
    }

    public void tick(Level level, BlockState state, BlockPos pos)
    {
        if (level.getGameTime() % 10 == 0 && !level.isClientSide)
        {
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
            {
                if (state.getValue(ThermolithBlock.POWERED))
                {
                    level.setBlockAndUpdate(pos, state.setValue(ThermolithBlock.POWERED, false));
                }
            }
            else if (!state.getValue(ThermolithBlock.POWERED))
            {
                level.setBlockAndUpdate(pos, state.setValue(ThermolithBlock.POWERED, true));
            }
        }
    }

    public int getSignal()
    {
        return signal;
    }
}
