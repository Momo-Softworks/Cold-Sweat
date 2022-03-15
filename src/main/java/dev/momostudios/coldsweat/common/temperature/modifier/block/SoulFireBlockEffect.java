package dev.momostudios.coldsweat.common.temperature.modifier.block;

import dev.momostudios.coldsweat.common.temperature.Temperature;
import net.minecraft.core.BlockPos;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.level.block.Blocks;
import net.minecraft.world.level.block.state.BlockState;
import dev.momostudios.coldsweat.util.math.CSMath;
import net.minecraft.world.phys.AABB;

public class SoulFireBlockEffect extends BlockEffect
{
    public SoulFireBlockEffect()
    {
        super(Blocks.SOUL_FIRE);
    }

    @Override
    public double getTemperature(Player player, BlockState state, BlockPos pos, double distance)
    {
        if (hasBlock(state))
        {
            return CSMath.blend(-0.2, 0, distance, 0.5, 7);
        }
        return 0;
    }

    @Override
    public double minEffect() {
        return CSMath.convertUnits(-32, Temperature.Units.F, Temperature.Units.MC, false);
    }

    @Override
    public double minTemperature() {
        return CSMath.convertUnits(400, Temperature.Units.F, Temperature.Units.MC, true);
    }
}
