package dev.momostudios.coldsweat.mixin;

import com.mojang.blaze3d.vertex.*;
import dev.momostudios.coldsweat.ColdSweat;
import net.minecraft.client.Minecraft;
import dev.momostudios.coldsweat.client.event.RearrangeHotbar;
import net.minecraft.client.gui.Font;
import net.minecraft.client.gui.Gui;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.Slice;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

@Mixin(value = Gui.class)
public class MixinXPBar
{
    Gui gui = (Gui) (Object) this;

    @Shadow
    protected int screenWidth;
    @Shadow
    protected int screenHeight;

    /**
     * @author iMikul
     * @reason Move XP bar elements to make room for body temperature readout
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
            cancellable = true,
            remap = ColdSweat.remapMixins)
    public void renderExperienceBar(PoseStack poseStack, int xPos, CallbackInfo ci)
    {
        Minecraft mc = Minecraft.getInstance();
        Font font = gui.getFont();

        // Render XP bar
        if (RearrangeHotbar.CUSTOM_HOTBAR)
        {
            String s = "" + mc.player.experienceLevel;
            int i1 = (this.screenWidth - font.width(s)) / 2;
            int j1 = this.screenHeight - 31;
            font.draw(poseStack, s, (float)(i1 + 1), (float)j1, 0);
            font.draw(poseStack, s, (float)(i1 - 1), (float)j1, 0);
            font.draw(poseStack, s, (float)i1, (float)(j1 + 1), 0);
            font.draw(poseStack, s, (float)i1, (float)(j1 - 1), 0);
            font.draw(poseStack, s, (float)i1, (float)j1, 8453920);
            mc.getProfiler().pop();
            ci.cancel();
        }
    }
}
