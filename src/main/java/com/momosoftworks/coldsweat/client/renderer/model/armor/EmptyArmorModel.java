package com.momosoftworks.coldsweat.client.renderer.model.armor;

import net.minecraft.client.renderer.entity.model.BipedModel;
import net.minecraft.entity.LivingEntity;

public class EmptyArmorModel<T extends LivingEntity> extends BipedModel<T>
{
    public EmptyArmorModel()
    {   super(0.5f, 0.0f, 64, 128);
    }

    @Override
    public void setupAnim(T entity, float limbSwing, float limbSwingAmount, float ageInTicks, float netHeadYaw, float headPitch)
    {   super.setupAnim(entity, limbSwing, limbSwingAmount, ageInTicks, netHeadYaw, headPitch);
    }
}