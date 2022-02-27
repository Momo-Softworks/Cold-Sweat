package dev.momostudios.coldsweat.mixin;

import com.mojang.blaze3d.systems.RenderSystem;
import com.mojang.blaze3d.vertex.PoseStack;
import dev.momostudios.coldsweat.ColdSweat;
import net.minecraft.client.Minecraft;
import dev.momostudios.coldsweat.client.event.RearrangeHotbar;
import net.minecraft.client.gui.Font;
import net.minecraft.client.gui.Gui;
import net.minecraft.client.gui.GuiComponent;
import net.minecraft.resources.ResourceLocation;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Overwrite;
import org.spongepowered.asm.mixin.Shadow;

@Mixin(value = Gui.class, priority = 900)
public class MixinXPBar
{
    @Shadow
    protected int screenWidth;
    @Shadow
    protected int screenHeight;

    /**
     * @author iMikul
     * @reason Move XP bar elements to make room for body temperature readout
     */
    @Overwrite(remap = ColdSweat.remapMixins)
    public void renderExperienceBar(PoseStack matrixStack, int xPos)
    {
        Minecraft mc = Minecraft.getInstance();
        Gui gui = (Gui) (Object) this;
        Font fontRenderer = gui.getFont();
        if (RearrangeHotbar.customHotbar)
        {
            xPos += 10;

            if (mc.player != null)
            {
                // Draw XP bar
                mc.getProfiler().push("expBar");
                RenderSystem.setShaderTexture(0, new ResourceLocation("cold_sweat:textures/gui/overlay/xp_bars.png"));
                int i = mc.player.totalExperience;
                if (i > 0)
                {
                    int l = screenHeight - 32 + 2;
                    // Render shorter bar to make room for experience level
                    if (mc.player.experienceLevel > 0)
                    {
                        int k = (int) (mc.player.experienceProgress * 167.88F) + 2;
                        gui.blit(matrixStack, xPos, l, 10, 64, 182, 5);
                        if (k > 0)
                        {
                            gui.blit(matrixStack, xPos, l, 10, 69, k, 5);
                        }
                    }
                    // Render full bar if player is level 0
                    else
                    {
                        int k = (int) (mc.player.totalExperience * 183.0F);
                        gui.blit(matrixStack, xPos - 10, l, 0, 74, 182, 5);
                        if (k > 0)
                        {
                            gui.blit(matrixStack, xPos - 10, l, 0, 79, k, 5);
                        }
                    }
                }
                mc.getProfiler().pop();

                // Draw XP level
                if (mc.player.experienceLevel > 0)
                {
                    mc.getProfiler().push("expLevel");
                    String s = "" + mc.player.experienceLevel;

                    int i1 = (fontRenderer.width(s) < 8) ?
                            screenWidth / 2 - fontRenderer.width(s) / 2 - 82
                            : (fontRenderer.width(s) < 13) ? screenWidth / 2 - fontRenderer.width(s) / 2 - 84
                            : screenWidth / 2 - fontRenderer.width(s) - 78;
                    int j1 = screenHeight - 31;

                    fontRenderer.draw(matrixStack, s, (float) (i1 + 1), (float) j1, 0);
                    fontRenderer.draw(matrixStack, s, (float) (i1 - 1), (float) j1, 0);
                    fontRenderer.draw(matrixStack, s, (float) i1, (float) (j1 + 1), 0);
                    fontRenderer.draw(matrixStack, s, (float) i1, (float) (j1 - 1), 0);
                    fontRenderer.draw(matrixStack, s, (float) i1, (float) j1, 8453920);
                    mc.getProfiler().pop();
                }
            }
        }
        else
        {
            mc.getProfiler().push("expBar");
            RenderSystem.setShaderTexture(0, GuiComponent.GUI_ICONS_LOCATION);
            int i = mc.player.totalExperience;
            if (i > 0) {
                int j = 182;
                int k = (int)(mc.player.experienceProgress * 183.0F);
                int l = this.screenHeight - 32 + 3;
                gui.blit(matrixStack, xPos, l, 0, 64, 182, 5);
                if (k > 0) {
                    gui.blit(matrixStack, xPos, l, 0, 69, k, 5);
                }
            }

            mc.getProfiler().pop();
            if (mc.player.experienceLevel > 0) {
                mc.getProfiler().push("expLevel");
                String s = "" + mc.player.experienceLevel;
                int i1 = (this.screenWidth - fontRenderer.width(s)) / 2;
                int j1 = this.screenHeight - 31 - 4;
                fontRenderer.draw(matrixStack, s, (float)(i1 + 1), (float)j1, 0);
                fontRenderer.draw(matrixStack, s, (float)(i1 - 1), (float)j1, 0);
                fontRenderer.draw(matrixStack, s, (float)i1, (float)(j1 + 1), 0);
                fontRenderer.draw(matrixStack, s, (float)i1, (float)(j1 - 1), 0);
                fontRenderer.draw(matrixStack, s, (float)i1, (float)j1, 8453920);
                mc.getProfiler().pop();
            }
        }
    }
}
