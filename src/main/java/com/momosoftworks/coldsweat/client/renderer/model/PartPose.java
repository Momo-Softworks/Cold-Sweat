package com.momosoftworks.coldsweat.client.renderer.model;

import net.minecraft.client.renderer.model.ModelRenderer;

public class PartPose
{
    public float offsetX;
    public float offsetY;
    public float offsetZ;
    public float rotationX;
    public float rotationY;
    public float rotationZ;

    public PartPose(float offsetX, float offsetY, float offsetZ, float rotationX, float rotationY, float rotationZ)
    {   this.offsetX = offsetX;
        this.offsetY = offsetY;
        this.offsetZ = offsetZ;
        this.rotationX = rotationX;
        this.rotationY = rotationY;
        this.rotationZ = rotationZ;
    }

    public static PartPose offset(float x, float y, float z)
    {   return new PartPose(x, y, z, 0, 0, 0);
    }

    public static PartPose rotation(float x, float y, float z)
    {   return new PartPose(0, 0, 0, x, y, z);
    }

    public static PartPose offsetAndRotation(float offsetX, float offsetY, float offsetZ, float rotationX, float rotationY, float rotationZ)
    {   return new PartPose(offsetX, offsetY, offsetZ, rotationX, rotationY, rotationZ);
    }

    public static void set(ModelRenderer renderer, PartPose pose)
    {   renderer.xRot = pose.rotationX;
        renderer.yRot = pose.rotationY;
        renderer.zRot = pose.rotationZ;
        renderer.x = pose.offsetX;
        renderer.y = pose.offsetY;
        renderer.z = pose.offsetZ;
    }
}
