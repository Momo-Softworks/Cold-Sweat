package com.momosoftworks.coldsweat.api.event.vanilla;

import net.minecraft.entity.Entity;
import net.minecraft.util.math.vector.Vector3d;
import net.minecraftforge.event.entity.EntityEvent;
import net.minecraftforge.eventbus.api.Cancelable;

/**
 * Fired when an entity moves of its own volition.<br>
 * Cancel this event when making changes to movement.
 */
@Cancelable
public class EntityMoveEvent extends EntityEvent
{
    private final Vector3d originalDirection;
    private Vector3d direction;
    private final float originalSpeed;
    private float speed;

    public EntityMoveEvent(Entity entity, float speed, Vector3d direction)
    {   super(entity);
        this.originalDirection = direction;
        this.direction = direction;
        this.originalSpeed = speed;
        this.speed = speed;
    }

    public Vector3d getOriginalDirection()
    {   return originalDirection;
    }
    public Vector3d getDirection()
    {   return direction;
    }
    public void setDirection(Vector3d direction)
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
