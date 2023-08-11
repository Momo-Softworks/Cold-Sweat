package dev.momostudios.coldsweat.mixin;

import com.mojang.blaze3d.matrix.MatrixStack;
import dev.momostudios.coldsweat.client.renderer.model.entity.ModLlamaModel;
import dev.momostudios.coldsweat.common.capability.IShearableCap;
import dev.momostudios.coldsweat.common.capability.ModCapabilities;
import net.minecraft.client.renderer.IRenderTypeBuffer;
import net.minecraft.client.renderer.entity.layers.LlamaDecorLayer;
import net.minecraft.client.renderer.entity.model.LlamaModel;
import net.minecraft.entity.passive.horse.LlamaEntity;
import org.objectweb.asm.Opcodes;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.Redirect;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

@Mixin(LlamaDecorLayer.class)
public class MixinLlamaDecorLayer
{
    ModLlamaModel model = new ModLlamaModel(0.5f);

    @Redirect(method = "render(Lcom/mojang/blaze3d/matrix/MatrixStack;Lnet/minecraft/client/renderer/IRenderTypeBuffer;ILnet/minecraft/entity/passive/horse/LlamaEntity;FFFFFF)V",
            at = @At(value = "FIELD", target = "Lnet/minecraft/client/renderer/entity/layers/LlamaDecorLayer;model:Lnet/minecraft/client/renderer/entity/model/LlamaModel;", opcode = Opcodes.GETFIELD))
    private LlamaModel<LlamaEntity> redirectModel(LlamaDecorLayer llamaDecorLayer)
    {   return model;
    }

    @Inject(method = "render(Lcom/mojang/blaze3d/matrix/MatrixStack;Lnet/minecraft/client/renderer/IRenderTypeBuffer;ILnet/minecraft/entity/passive/horse/LlamaEntity;FFFFFF)V",
            at = @At(value = "HEAD"))
    private void render(MatrixStack matrixStack, IRenderTypeBuffer buffer, int p_225628_3_, LlamaEntity entity, float p_225628_5_, float p_225628_6_, float p_225628_7_, float p_225628_8_, float p_225628_9_, float p_225628_10_, CallbackInfo ci)
    {   model.body.y = entity.getCapability(ModCapabilities.SHEARABLE_FUR).map(IShearableCap::isSheared).orElse(false) ? 6.5f : 5f;
    }
}
