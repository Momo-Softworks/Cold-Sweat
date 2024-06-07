package com.momosoftworks.coldsweat.api.event.client;

import com.mojang.blaze3d.platform.GlStateManager;
import net.minecraft.client.Minecraft;
import net.minecraft.client.renderer.ActiveRenderInfo;
import net.minecraft.client.renderer.FogRenderer;
import net.minecraftforge.client.event.EntityViewRenderEvent;
import net.minecraftforge.eventbus.api.Cancelable;

@Cancelable
public class RenderFogEvent extends EntityViewRenderEvent
{
    private final GlStateManager.FogMode mode;
    private final FogRenderer.FogType type;
    private float farPlaneDistance;
    private float nearPlaneDistance;

    public RenderFogEvent(GlStateManager.FogMode mode, FogRenderer.FogType type, ActiveRenderInfo camera, float partialTicks, float nearPlaneDistance, float farPlaneDistance)
    {
        super(Minecraft.getInstance().gameRenderer, camera, partialTicks);
        this.mode = mode;
        this.type = type;
        setFarPlaneDistance(farPlaneDistance);
        setNearPlaneDistance(nearPlaneDistance);
    }

    /**
     * {@return the mode of fog being rendered}
     */
    public GlStateManager.FogMode getMode()
    {
        return mode;
    }

    /**
     * {@return the type of fog being rendered}
     */
    public FogRenderer.FogType getType()
    {
        return type;
    }

    /**
     * {@return the distance to the far plane where the fog ends}
     */
    public float getFarPlaneDistance()
    {
        return farPlaneDistance;
    }

    /**
     * {@return the distance to the near plane where the fog starts}
     */
    public float getNearPlaneDistance()
    {
        return nearPlaneDistance;
    }

    /**
     * Sets the distance to the far plane of the fog.
     *
     * @param distance the new distance to the far place
     * @see #scaleFarPlaneDistance(float)
     */
    public void setFarPlaneDistance(float distance)
    {
        farPlaneDistance = distance;
    }

    /**
     * Sets the distance to the near plane of the fog.
     *
     * @param distance the new distance to the near plane
     * @see #scaleNearPlaneDistance(float)
     */
    public void setNearPlaneDistance(float distance)
    {
        nearPlaneDistance = distance;
    }

    /**
     * Scales the distance to the far plane of the fog by a given factor.
     *
     * @param factor the factor to scale the far plane distance by
     */
    public void scaleFarPlaneDistance(float factor)
    {
        farPlaneDistance *= factor;
    }

    /**
     * Scales the distance to the near plane of the fog by a given factor.
     *
     * @param factor the factor to scale the near plane distance by
     */
    public void scaleNearPlaneDistance(float factor)
    {
        nearPlaneDistance *= factor;
    }
}
