package net.momostudios.coldsweat.common.temperature.modifier.block;

import net.minecraft.block.BlockState;
import net.minecraft.block.Blocks;
import net.minecraft.block.CampfireBlock;
import net.minecraft.entity.LivingEntity;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.potion.EffectInstance;
import net.minecraft.potion.Effects;
import net.minecraft.util.math.AxisAlignedBB;
import net.minecraft.util.math.BlockPos;
import net.momostudios.coldsweat.core.util.MathHelperCS;
import net.momostudios.coldsweat.core.util.Units;

public class SoulCampfireBlockEffect extends BlockEffect
{
    @Override
    public double getTemperature(PlayerEntity player, BlockState state, BlockPos pos, double distance)
    {
        if (this.hasBlock(state) && state.get(CampfireBlock.LIT))
        {
            AxisAlignedBB bb = new AxisAlignedBB(pos.getX() - 0.2, pos.getY(), pos.getZ() - 0.2, pos.getX() + 1.2, pos.getY() + 1.2, pos.getZ() + 1.2);
            player.world.getEntitiesWithinAABB(LivingEntity.class, bb).forEach(entity ->
            {
                entity.extinguish();
                if (!entity.getPersistentData().getBoolean("isInSoulFire"))
                    entity.getPersistentData().putBoolean("isInSoulFire", true);
            });

            return MathHelperCS.interpolate(-0.2, 0, distance, 7);
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
        return MathHelperCS.convertUnits(-20, Units.F, Units.MC, false);
    }
}
