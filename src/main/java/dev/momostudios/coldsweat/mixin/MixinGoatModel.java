package dev.momostudios.coldsweat.mixin;

import com.blackgear.cavesandcliffs.client.renderer.entity.model.GoatModel;
import com.blackgear.cavesandcliffs.common.entity.GoatEntity;
import dev.momostudios.coldsweat.ColdSweat;
import dev.momostudios.coldsweat.common.capability.ModCapabilities;
import net.minecraft.client.renderer.model.ModelRenderer;
import org.spongepowered.asm.mixin.Final;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Mutable;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;
import org.spongepowered.asm.mixin.injection.callback.LocalCapture;

@Mixin(GoatModel.class)
public class MixinGoatModel<T extends GoatEntity>
{
    GoatModel<GoatEntity> self = (GoatModel<GoatEntity>)(Object)this;

    @Final
    @Shadow(remap = false)
    private ModelRenderer left_back_leg;
    @Final
    @Shadow(remap = false)
    private ModelRenderer right_back_leg;
    @Final
    @Shadow(remap = false)
    private ModelRenderer right_front_leg;
    @Final
    @Shadow(remap = false)
    private ModelRenderer left_front_leg;
    @Final
    @Mutable
    @Shadow(remap = false)
    private ModelRenderer body;
    private ModelRenderer body2;
    @Final
    @Shadow(remap = false)
    private ModelRenderer head;
    @Final
    @Shadow(remap = false)
    private ModelRenderer nose;
    @Final
    @Shadow(remap = false)
    private ModelRenderer right_horn;
    @Final
    @Shadow(remap = false)
    private ModelRenderer left_horn;


    @Inject(method = "<init>", at = @At("RETURN"), remap = false)
    private void createBodyLayer(CallbackInfo ci)
    {   body = new ModelRenderer(self);
        body.setPos(0.0F, 24.0F, 0.0F);
        body.texOffs(1, 1).addBox(-4.0F, -17.0F, -7.0F, 9.0F, 11.0F, 16.0F, 0.0F, false);

        body2 = new ModelRenderer(self);
        body2.texOffs(0, 28).addBox(-5.0F, -18.0F, -8.0F, 11.0F, 14.0F, 11.0F, 0.0F, false);
        body.addChild(body2);
    }

    @Inject(method = "setRotationAngles", at = @At("TAIL"), remap = false)
    private void setRotationAngles(T goat, float limbSwing, float limbSwingAmount, float ageInTicks, float netHeadYaw, float headPitch, CallbackInfo ci)
    {
        goat.getCapability(ModCapabilities.SHEARABLE_FUR).ifPresent(cap ->
        {   body2.visible = !cap.isSheared();
        });
    }
}
