package com.momosoftworks.coldsweat.mixin;

import com.momosoftworks.coldsweat.client.renderer.model.entity.ModLlamaModel;
import com.momosoftworks.coldsweat.common.capability.shearing.IShearableCap;
import com.momosoftworks.coldsweat.common.capability.handler.ShearableFurManager;
import net.minecraft.client.renderer.entity.EntityRendererManager;
import net.minecraft.client.renderer.entity.LivingRenderer;
import net.minecraft.client.renderer.entity.LlamaRenderer;
import net.minecraft.client.renderer.entity.model.EntityModel;
import net.minecraft.client.renderer.entity.model.LlamaModel;
import net.minecraft.entity.LivingEntity;
import net.minecraft.entity.passive.horse.LlamaEntity;
import net.minecraft.util.ResourceLocation;
import net.minecraftforge.common.util.LazyOptional;
import org.spongepowered.asm.mixin.Final;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

public class MixinLlamaRender<T extends LivingEntity, M extends EntityModel<T>>
{

    @Mixin(value = LivingRenderer.class)
    public static class RemapModel<T extends LivingEntity, M extends EntityModel<T>>
    {
        LivingRenderer<T, M> self = (LivingRenderer<T, M>)(Object)this;

        @Shadow
        protected M model;

        @Inject(method = "<init>",
                at = @At(value = "RETURN"))
        private void init(EntityRendererManager renderManager, M model, float shadowSize, CallbackInfo ci)
        {
            if (self instanceof LlamaRenderer)
            {   this.model = (M) new ModLlamaModel(0f);
            }
        }
    }

    @Mixin(value = LlamaRenderer.class)
    public static class ChangeTexture
    {
        private static final ResourceLocation[] SHAVED_TEXTURES = new ResourceLocation[]{new ResourceLocation("textures/entity/llama/creamy_shaven.png"), new ResourceLocation("textures/entity/llama/white_shaven.png"),
                                                                                         new ResourceLocation("textures/entity/llama/brown_shaven.png"), new ResourceLocation("textures/entity/llama/gray_shaven.png") };

        @Inject(method = "getTextureLocation(Lnet/minecraft/entity/passive/horse/LlamaEntity;)Lnet/minecraft/util/ResourceLocation;",
                at = @At(value = "HEAD"),
                cancellable = true)
        private void getTextureLocation(LlamaEntity entity, CallbackInfoReturnable<ResourceLocation> cir)
        {
            LazyOptional<IShearableCap> llamaCap = ShearableFurManager.getFurCap(entity);
            if (llamaCap.isPresent() && llamaCap.resolve().get().isSheared())
            {   cir.setReturnValue(SHAVED_TEXTURES[entity.getVariant()]);
            }
        }
    }
}
