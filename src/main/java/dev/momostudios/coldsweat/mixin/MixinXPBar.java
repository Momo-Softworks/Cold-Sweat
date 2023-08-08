package dev.momostudios.coldsweat.mixin;

import com.mojang.blaze3d.matrix.MatrixStack;
import dev.momostudios.coldsweat.ColdSweat;
import dev.momostudios.coldsweat.config.ClientSettingsConfig;
import net.minecraft.client.gui.IngameGui;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.Slice;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

@Mixin(IngameGui.class)
public class MixinXPBar
{
    @Inject(method = "renderExperienceBar(Lcom/mojang/blaze3d/matrix/MatrixStack;I)V",
            at = @At
            (
                value = "INVOKE",
                target = "Lnet/minecraft/profiler/IProfiler;push(Ljava/lang/String;)V",
                shift = At.Shift.AFTER
            ),
            slice = @Slice
            (
                from = @At(value = "INVOKE", target = "Lnet/minecraft/profiler/IProfiler;pop()V"),
                to   = @At(value = "INVOKE", target = "Lnet/minecraft/client/gui/FontRenderer;width(Ljava/lang/String;)I")
            ),
            remap = ColdSweat.REMAP_MIXINS)
    public void renderExperienceBar1(MatrixStack poseStack, int xPos, CallbackInfo ci)
    {
        // Render XP bar
        if (ClientSettingsConfig.getInstance().customHotbarEnabled())
        {   poseStack.translate(0.0D, 4.0D, 0.0D);
        }
    }

    @Inject(method = "renderExperienceBar(Lcom/mojang/blaze3d/matrix/MatrixStack;I)V",
            at = @At
            (
                value = "INVOKE",
                target = "Lnet/minecraft/profiler/IProfiler;pop()V"
            ),
            slice = @Slice
            (
                from = @At(value = "INVOKE", target = "Lnet/minecraft/client/gui/FontRenderer;width(Ljava/lang/String;)I"),
                to   = @At(value = "RETURN")
            ),
            remap = ColdSweat.REMAP_MIXINS)
    public void renderExperienceBar2(MatrixStack poseStack, int xPos, CallbackInfo ci)
    {
        // Render XP bar
        if (ClientSettingsConfig.getInstance().customHotbarEnabled())
        {   poseStack.translate(0.0D, -4.0D, 0.0D);
        }
    }

    @Mixin(IngameGui.class)
    public static class MixinItemLabel
    {
        @Inject(method = "renderExperienceBar(Lcom/mojang/blaze3d/matrix/MatrixStack;I)V",
                at = @At
                (
                    value = "INVOKE",
                    target = "Lnet/minecraft/client/gui/FontRenderer;draw(Lcom/mojang/blaze3d/matrix/MatrixStack;Ljava/lang/String;FFI)I",
                    ordinal = 0
                ), remap = ColdSweat.REMAP_MIXINS)
        public void renderItemNamePre(MatrixStack matrixStack, int p_238454_2_, CallbackInfo ci)
        {
            if (ClientSettingsConfig.getInstance().customHotbarEnabled())
            {   matrixStack.translate(0, -4, 0);
            }
        }

        @Inject(method = "renderExperienceBar(Lcom/mojang/blaze3d/matrix/MatrixStack;I)V",
                at = @At
                (
                    value = "INVOKE",
                    target = "Lnet/minecraft/client/gui/FontRenderer;draw(Lcom/mojang/blaze3d/matrix/MatrixStack;Ljava/lang/String;FFI)I",
                    shift = At.Shift.AFTER,
                    ordinal = 4
                ), remap = ColdSweat.REMAP_MIXINS)
        public void renderItemNamePost(MatrixStack matrixStack, int p_238454_2_, CallbackInfo ci)
        {
            if (ClientSettingsConfig.getInstance().customHotbarEnabled())
            {   matrixStack.translate(0, 4, 0);
            }
        }
    }
}