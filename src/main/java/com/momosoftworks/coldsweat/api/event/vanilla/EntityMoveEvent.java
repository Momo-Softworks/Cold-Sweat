package com.momosoftworks.coldsweat.api.event.vanilla;

import net.minecraft.world.entity.Entity;
import net.minecraft.world.phys.Vec3;
import net.minecraftforge.event.entity.EntityEvent;
import net.minecraftforge.eventbus.api.Cancelable;

/**
 * Fired when an entity moves of its own volition.<br>
 * Cancel this event when making changes to movement.
 */
@Cancelable
public class EntityMoveEvent extends EntityEvent
{
    private final Vec3 originalDirection;
    private Vec3 direction;
    private final float originalSpeed;
    private float speed;

    public EntityMoveEvent(Entity entity, float speed, Vec3 direction)
    {   super(entity);
        this.originalDirection = direction;
        this.direction = direction;
        this.originalSpeed = speed;
        this.speed = speed;
    }

    public Vec3 getOriginalDirection()
    {   return originalDirection;
    }
    public Vec3 getDirection()
    {   return direction;
    }
    public void setDirection(Vec3 direction)
    {   this.direction = direction;
    }

    public float getOriginalSpeed()
    {   return originalSpeed;
    }
    public float getSpeed()
    {   return speed;
    }
    public void setSpeed(float speed)
    {   this.speed = speed;
    }
}
