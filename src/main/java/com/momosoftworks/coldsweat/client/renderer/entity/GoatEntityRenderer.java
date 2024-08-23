package com.momosoftworks.coldsweat.client.renderer.entity;

import com.momosoftworks.coldsweat.ColdSweat;
import com.momosoftworks.coldsweat.client.renderer.model.entity.GoatModel;
import com.momosoftworks.coldsweat.common.capability.handler.ShearableFurManager;
import com.momosoftworks.coldsweat.common.capability.shearing.IShearableCap;
import com.momosoftworks.coldsweat.common.entity.GoatEntity;
import net.minecraft.client.renderer.entity.EntityRendererManager;
import net.minecraft.client.renderer.entity.MobRenderer;
import net.minecraft.util.ResourceLocation;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.api.distmarker.OnlyIn;
import net.minecraftforge.common.util.LazyOptional;

@OnlyIn(Dist.CLIENT)
public class GoatEntityRenderer extends MobRenderer<GoatEntity, GoatModel<GoatEntity>>
{
    private static final ResourceLocation TEXTURE = new ResourceLocation(ColdSweat.MOD_ID, "textures/entity/goat/goat.png");
    private static final ResourceLocation SHEARED_TEXTURE = new ResourceLocation(ColdSweat.MOD_ID, "textures/entity/goat/goat_shaven.png");

    public GoatEntityRenderer(EntityRendererManager renderManager)
    {   super(renderManager, new GoatModel<>(), 0.7F);
    }

    @Override
    public ResourceLocation getTextureLocation(GoatEntity goat)
    {
        LazyOptional<IShearableCap> goatCap = ShearableFurManager.getFurCap(goat);
        if (goatCap.isPresent() && goatCap.resolve().get().isSheared())
        {   return SHEARED_TEXTURE;
        }
        return TEXTURE;
    }
}
