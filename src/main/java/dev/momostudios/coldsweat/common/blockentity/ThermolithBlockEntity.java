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
    private ChameleonEntity dummy = null;
    private int signal = 0;
    ConfigSettings config = ConfigSettings.getInstance();

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
            // Use dummy chameleon entity for TempModifiers
            if (dummy == null)
            {
                dummy = new ChameleonEntity(ModEntities.CHAMELEON, this.level);
                dummy.setPos(pos.getX(), pos.getY(), pos.getZ());
            }

            // TempModifiers to use for temp calculation
            List<TempModifier> modifiers = new ArrayList<>(List.of(
                    new BlockTempModifier(7),
                    new DepthTempModifier(),
                    new BiomeTempModifier(16)));
            if (CompatManager.isSereneSeasonsLoaded())
            {   modifiers.add(TempModifierRegistry.getEntryFor("sereneseasons:season").tickRate(20));
            }

            // Handle signal output / neighbor updates
            double temperature = Temperature.apply(0, dummy, Temperature.Type.WORLD, modifiers);
            int newSignal = (int) CSMath.blend(0, 15, temperature, config.minTemp, config.maxTemp);

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
