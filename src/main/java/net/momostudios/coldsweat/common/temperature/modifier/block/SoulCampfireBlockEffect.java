package net.momostudios.coldsweat.common.temperature.modifier.block;

import net.minecraft.block.BlockState;
import net.minecraft.block.Blocks;
import net.minecraft.block.CampfireBlock;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.potion.EffectInstance;
import net.minecraft.potion.Effects;
import net.minecraft.util.math.BlockPos;
import net.momostudios.coldsweat.core.util.MathHelperCS;

public class SoulCampfireBlockEffect extends BlockEffect
{
    @Override
    public double getTemperature(PlayerEntity player, BlockState state, BlockPos pos, double distance)
    {
        if (this.hasBlock(state) && state.get(CampfireBlock.LIT))
        {
            if (distance < 1.4)
            {
                player.extinguish();
                player.addPotionEffect(new EffectInstance(Effects.FIRE_RESISTANCE, 2, 0));
            }

            double temp = -0.005;
            return Math.max(0, temp * (9 - distance));
        }
        return 0;
    }

    @Override
    public boolean hasBlock(BlockState block)
    {
        return block.getBlock() == Blocks.SOUL_CAMPFIRE;
    }

    @Override
    public double minTemp() {
        return MathHelperCS.convertFromF(-20);
    }
}
