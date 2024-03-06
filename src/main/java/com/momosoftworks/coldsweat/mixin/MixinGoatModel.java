package com.momosoftworks.coldsweat.mixin;

import com.momosoftworks.coldsweat.common.event.capability.ShearableFurManager;
import com.momosoftworks.coldsweat.util.math.CSMath;
import net.minecraft.client.Minecraft;
import net.minecraft.client.model.GoatModel;
import net.minecraft.client.model.QuadrupedModel;
import net.minecraft.client.model.geom.ModelPart;
import net.minecraft.client.model.geom.PartPose;
import net.minecraft.client.model.geom.builders.CubeListBuilder;
import net.minecraft.client.model.geom.builders.LayerDefinition;
import net.minecraft.client.model.geom.builders.MeshDefinition;
import net.minecraft.client.model.geom.builders.PartDefinition;
import net.minecraft.world.entity.animal.goat.Goat;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;
import org.spongepowered.asm.mixin.injection.callback.LocalCapture;

@Mixin(GoatModel.class)
public class MixinGoatModel extends QuadrupedModel<Goat>
{
    protected MixinGoatModel(ModelPart p_170857_, boolean p_170858_, float p_170859_, float p_170860_, float p_170861_, float p_170862_, int p_170863_)
    {
        super(p_170857_, p_170858_, p_170859_, p_170860_, p_170861_, p_170862_, p_170863_);
    }

    @Inject(method = "createBodyLayer()Lnet/minecraft/client/model/geom/builders/LayerDefinition;", at = @At("TAIL"),
    locals = LocalCapture.CAPTURE_FAILSOFT)
    private static void createBodyLayer(CallbackInfoReturnable<LayerDefinition> cir, MeshDefinition mesh, PartDefinition base, PartDefinition head)
    {
        base.addOrReplaceChild("body", CubeListBuilder.create().texOffs(1, 1).addBox(-4.0F, -17.0F, -7.0F, 9.0F, 11.0F, 16.0F), PartPose.offset(0.0F, 24.0F, 0.0F));
        base.getChild("body").addOrReplaceChild("body2", CubeListBuilder.create().texOffs(0, 28).addBox(-5.0F, -18.0F, -8.0F, 11.0F, 14.0F, 11.0F), PartPose.offset(0.0F, 0.0F, 0.0F));
    }

    @Inject(method = "setupAnim(Lnet/minecraft/world/entity/animal/goat/Goat;FFFFF)V", at = @At("TAIL"))
    private void setupAnim(Goat goat, float p_170588_, float p_170589_, float p_170590_, float p_170591_, float partialTick, CallbackInfo ci)
    {
        ShearableFurManager.getFurCap(goat).ifPresent(cap ->
        {
            body.getChild("body2").visible = !cap.isSheared();
        });
        // smooth out head ramming animation
        if (CSMath.isBetween(goat.getRammingXHeadRot(), 0f, 0.523f))
        {
            head.xRot += CSMath.toRadians(Minecraft.getInstance().getFrameTime());
        }
    }
}
