package dev.momostudios.coldsweat.mixin;

import dev.momostudios.coldsweat.ColdSweat;
import dev.momostudios.coldsweat.client.renderer.model.PartPose;
import dev.momostudios.coldsweat.common.capability.ModCapabilities;
import dev.momostudios.coldsweat.util.math.CSMath;
import net.minecraft.client.Minecraft;
import net.minecraft.client.renderer.entity.model.EntityModel;
import net.minecraft.client.renderer.entity.model.LlamaModel;
import net.minecraft.client.renderer.entity.model.QuadrupedModel;
import net.minecraft.entity.passive.horse.LlamaEntity;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;
import org.spongepowered.asm.mixin.injection.callback.LocalCapture;

@Mixin(LlamaModel.class)
public class MixinGoatModel
{
    /*@Inject(method = "createBodyLayer()Lnet/minecraft/client/model/geom/builders/LayerDefinition;", at = @At("TAIL"), remap = ColdSweat.REMAP_MIXINS,
    locals = LocalCapture.CAPTURE_FAILSOFT)
    private static void createBodyLayer(CallbackInfoReturnable<LayerDefinition> cir, MeshDefinition mesh, PartDefinition base, PartDefinition head)
    {
        base.addOrReplaceChild("body", CubeListBuilder.create().texOffs(1, 1).addBox(-4.0F, -17.0F, -7.0F, 9.0F, 11.0F, 16.0F), PartPose.offset(0.0F, 24.0F, 0.0F));
        base.getChild("body").addOrReplaceChild("body2", CubeListBuilder.create().texOffs(0, 28).addBox(-5.0F, -18.0F, -8.0F, 11.0F, 14.0F, 11.0F), PartPose.offset(0.0F, 0.0F, 0.0F));
    }

    @Inject(method = "setupAnim(Lnet/minecraft/world/entity/animal/goat/Goat;FFFFF)V", at = @At("TAIL"), remap = ColdSweat.REMAP_MIXINS)
    private void setupAnim(Goat goat, float p_170588_, float p_170589_, float p_170590_, float p_170591_, float partialTick, CallbackInfo ci)
    {
        goat.getCapability(ModCapabilities.SHEARABLE_FUR).ifPresent(cap ->
        {
            body.getChild("body2").visible = !cap.isSheared();
        });
        // smooth out head ramming animation
        if (CSMath.isBetween(goat.getRammingXHeadRot(), 0f, 0.523f))
        {
            head.xRot += CSMath.toRadians(Minecraft.getInstance().getFrameTime());
        }
    }*/
}
