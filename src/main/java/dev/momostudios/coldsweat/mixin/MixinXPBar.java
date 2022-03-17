package dev.momostudios.coldsweat.mixin;

import com.mojang.blaze3d.systems.RenderSystem;
import com.mojang.blaze3d.vertex.*;
import dev.momostudios.coldsweat.ColdSweat;
import net.minecraft.client.Minecraft;
import dev.momostudios.coldsweat.client.event.RearrangeHotbar;
import net.minecraft.client.gui.Font;
import net.minecraft.client.gui.Gui;
import net.minecraft.client.gui.GuiComponent;
import net.minecraft.client.renderer.GameRenderer;
import net.minecraft.client.renderer.RenderType;
import net.minecraft.resources.ResourceLocation;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Overwrite;
import org.spongepowered.asm.mixin.Shadow;

@Mixin(value = Gui.class, priority = 900)
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
    @Overwrite(remap = ColdSweat.remapMixins)
    public void renderExperienceBar(PoseStack poseStack, int xPos)
    {
        Minecraft mc = Minecraft.getInstance();
        Font fontRenderer = gui.getFont();

        // Render XP bar
        mc.getProfiler().push("expBar");
        RenderSystem.setShaderTexture(0, GuiComponent.GUI_ICONS_LOCATION);
        int i = mc.player.totalExperience;
        if (i > 0)
        {
            int k = (int)(mc.player.experienceProgress * 183.0F);
            int l = this.screenHeight - 32 + 3;
            gui.blit(poseStack, xPos, l, 0, 64, 182, 5);
            if (k > 0)
            {
                gui.blit(poseStack, xPos, l, 0, 69, k, 5);
            }
        }

        // Render XP level
        mc.getProfiler().pop();
        if (mc.player.experienceLevel > 0)
        {
            mc.getProfiler().push("expLevel");
            String s = "" + mc.player.experienceLevel;
            int i1 = (this.screenWidth - fontRenderer.width(s)) / 2;
            int j1 = this.screenHeight - 31 - (RearrangeHotbar.customHotbar ? 0 : 4);

            // Render XP level background
            GuiComponent.fill(poseStack, i1, j1, i1 + fontRenderer.width(s), j1 + 8, 0);

            fontRenderer.draw(poseStack, s, (float)(i1 + 1), (float)j1, 0);
            fontRenderer.draw(poseStack, s, (float)(i1 - 1), (float)j1, 0);
            fontRenderer.draw(poseStack, s, (float)i1, (float)(j1 + 1), 0);
            fontRenderer.draw(poseStack, s, (float)i1, (float)(j1 - 1), 0);
            fontRenderer.draw(poseStack, s, (float)i1, (float)j1, 8453920);
            mc.getProfiler().pop();
        }
    }
}
