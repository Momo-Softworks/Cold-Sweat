package dev.momosoftworks.coldsweat.mixin;

import com.mojang.blaze3d.vertex.PoseStack;
import dev.momosoftworks.coldsweat.ColdSweat;
import dev.momosoftworks.coldsweat.config.ClientSettingsConfig;
import net.minecraft.client.gui.Gui;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.Slice;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

@Mixin(value = Gui.class)
public class MixinXPBar
{
    /**
     * @author iMikul
     * @reason Move XP bar number to make room for body temperature readout (2 methods needed)
     */
    @Inject(method = "renderExperienceBar(Lcom/mojang/blaze3d/vertex/PoseStack;I)V",
            at = @At
            (
                value = "INVOKE",
                target = "Lnet/minecraft/util/profiling/ProfilerFiller;push(Ljava/lang/String;)V",
                shift = At.Shift.AFTER
            ),
            slice = @Slice
            (
                from = @At(value = "INVOKE", target = "Lnet/minecraft/util/profiling/ProfilerFiller;pop()V"),
                to   = @At(value = "INVOKE", target = "Lnet/minecraft/client/gui/Font;width(Ljava/lang/String;)I")
            ),
            remap = ColdSweat.REMAP_MIXINS)
    public void renderExperienceBar1(PoseStack poseStack, int xPos, CallbackInfo ci)
    {
        // Render XP bar
        if (ClientSettingsConfig.getInstance().customHotbarEnabled())
        {
            poseStack.translate(0.0D, 4.0D, 0.0D);
        }
    }

    @Inject(method = "renderExperienceBar(Lcom/mojang/blaze3d/vertex/PoseStack;I)V",
            at = @At
            (
                value = "INVOKE",
                target = "Lnet/minecraft/util/profiling/ProfilerFiller;pop()V"
            ),
            slice = @Slice
            (
                from = @At(value = "INVOKE", target = "Lnet/minecraft/client/gui/Font;width(Ljava/lang/String;)I"),
                to   = @At(value = "RETURN")
            ),
            remap = ColdSweat.REMAP_MIXINS)
    public void renderExperienceBar2(PoseStack poseStack, int xPos, CallbackInfo ci)
    {
        // Render XP bar
        if (ClientSettingsConfig.getInstance().customHotbarEnabled())
        {
            poseStack.translate(0.0D, -4.0D, 0.0D);
        }
    }

    @Mixin(Gui.class)
    public static class MixinItemLabel
    {
        @Inject(method = "renderSelectedItemName(Lcom/mojang/blaze3d/vertex/PoseStack;)V",
                at = @At
                (
                    value = "INVOKE",
                    target = "Lnet/minecraft/util/profiling/ProfilerFiller;push(Ljava/lang/String;)V",
                    shift = At.Shift.AFTER
                ), remap = ColdSweat.REMAP_MIXINS)
        public void renderItemNamePre(PoseStack matrixStack, CallbackInfo ci)
        {
            if (ClientSettingsConfig.getInstance().customHotbarEnabled())
            {
                matrixStack.translate(0, -4, 0);
            }
        }

        @Inject(method = "renderSelectedItemName(Lcom/mojang/blaze3d/vertex/PoseStack;)V",
                at = @At
                (
                    value = "INVOKE",
                    target = "Lnet/minecraft/util/profiling/ProfilerFiller;pop()V",
                    shift = At.Shift.BEFORE
                ), remap = ColdSweat.REMAP_MIXINS)
        public void renderItemNamePost(PoseStack matrixStack, CallbackInfo ci)
        {
            if (ClientSettingsConfig.getInstance().customHotbarEnabled())
            {
                matrixStack.translate(0, 4, 0);
            }
        }
    }
}
