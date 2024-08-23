package com.momosoftworks.coldsweat.common.entity.goal;

import net.minecraft.entity.MobEntity;
import net.minecraft.entity.ai.goal.Goal;
import net.minecraft.entity.player.PlayerEntity;

import java.util.EnumSet;

public class LazyLookGoal extends Goal
{
    private final MobEntity mob;

    private double relX;
    private double relZ;
    private int lookTime;

    public LazyLookGoal(MobEntity mob) {
        this.mob = mob;
        this.setFlags(EnumSet.of(Goal.Flag.MOVE, Goal.Flag.LOOK));
    }

    public boolean canUse()
    {
        return this.mob.getRandom().nextFloat() < 0.02F;
    }

    public boolean canContinueToUse()
    {
        return this.lookTime >= 0;
    }

    public void start()
    {
        double d0 = (Math.PI * 2D) * this.mob.getRandom().nextDouble();
        // pick a random point within a 120 degree cone in front of the mob
        this.relX = Math.cos(d0);
        this.relZ = Math.sin(d0);
        this.lookTime = 20 + this.mob.getRandom().nextInt(20);
    }

    public void tick()
    {
        --this.lookTime;
        // get the angle between the mob's current rotation and the position (relX, relZ)
        if (!(this.mob.getVehicle() instanceof PlayerEntity))
        {   this.mob.getLookControl().setLookAt(this.mob.getX() + this.relX, this.mob.getEyeY(), this.mob.getZ() + this.relZ);
        }
    }
}

