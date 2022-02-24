package dev.momostudios.coldsweat.mixin;

import com.mojang.blaze3d.matrix.MatrixStack;
import dev.momostudios.coldsweat.ColdSweat;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.AbstractGui;
import net.minecraft.client.gui.FontRenderer;
import net.minecraft.client.gui.IngameGui;
import net.minecraft.util.ResourceLocation;
import dev.momostudios.coldsweat.client.event.RearrangeHotbar;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Overwrite;
import org.spongepowered.asm.mixin.Shadow;

@Mixin(value = IngameGui.class, priority = 900)
public class MixinXPBar
{
    @Shadow
    protected int scaledWidth;
    @Shadow
    protected int scaledHeight;

    /**
     * @author iMikul
     * @reason Move XP bar elements to make room for body temperature readout
     */
    @Overwrite(remap = ColdSweat.remapMixins)
    public void renderExpBar(MatrixStack matrixStack, int xPos)
    {
        Minecraft mc = Minecraft.getInstance();
        IngameGui gui = (IngameGui) (Object) this;
        FontRenderer fontRenderer = gui.getFontRenderer();
        if (RearrangeHotbar.customHotbar)
        {
            xPos += 10;

            if (mc.player != null)
            {
                // Draw XP bar
                mc.getProfiler().startSection("expBar");
                mc.getTextureManager().bindTexture(new ResourceLocation("cold_sweat:textures/gui/overlay/xp_bars.png"));
                int i = mc.player.xpBarCap();
                if (i > 0)
                {
                    int l = scaledHeight - 32 + 2;
                    // Render shorter bar to make room for experience level
                    if (mc.player.experienceLevel > 0)
                    {
                        int k = (int) (mc.player.experience * 167.88F) + 2;
                        Minecraft.getInstance().ingameGUI.blit(matrixStack, xPos, l, 10, 64, 182, 5);
                        if (k > 0)
                        {
                            Minecraft.getInstance().ingameGUI.blit(matrixStack, xPos, l, 10, 69, k, 5);
                        }
                    }
                    // Render full bar if player is level 0
                    else
                    {
                        int k = (int) (mc.player.experience * 183.0F);
                        Minecraft.getInstance().ingameGUI.blit(matrixStack, xPos - 10, l, 0, 74, 182, 5);
                        if (k > 0)
                        {
                            Minecraft.getInstance().ingameGUI.blit(matrixStack, xPos - 10, l, 0, 79, k, 5);
                        }
                    }
                }
                mc.getProfiler().endSection();

                // Draw XP level
                if (mc.player.experienceLevel > 0)
                {
                    mc.getProfiler().startSection("expLevel");
                    String s = "" + mc.player.experienceLevel;

                    int i1 = (fontRenderer.getStringWidth(s) < 8) ?
                            scaledWidth / 2 - fontRenderer.getStringWidth(s) / 2 - 82
                            : (fontRenderer.getStringWidth(s) < 13) ? scaledWidth / 2 - fontRenderer.getStringWidth(s) / 2 - 84
                            : scaledWidth / 2 - fontRenderer.getStringWidth(s) - 78;
                    int j1 = scaledHeight - 31;

                    fontRenderer.drawString(matrixStack, s, (float) (i1 + 1), (float) j1, 0);
                    fontRenderer.drawString(matrixStack, s, (float) (i1 - 1), (float) j1, 0);
                    fontRenderer.drawString(matrixStack, s, (float) i1, (float) (j1 + 1), 0);
                    fontRenderer.drawString(matrixStack, s, (float) i1, (float) (j1 - 1), 0);
                    fontRenderer.drawString(matrixStack, s, (float) i1, (float) j1, 8453920);
                    mc.getProfiler().endSection();
                }
            }
        }
        else
        {
            mc.getProfiler().startSection("expBar");
            mc.getTextureManager().bindTexture(AbstractGui.GUI_ICONS_LOCATION);
            int i = mc.player.xpBarCap();
            if (i > 0) {
                int j = 182;
                int k = (int)(mc.player.experience * 183.0F);
                int l = this.scaledHeight - 32 + 3;
                gui.blit(matrixStack, xPos, l, 0, 64, 182, 5);
                if (k > 0) {
                    gui.blit(matrixStack, xPos, l, 0, 69, k, 5);
                }
            }

            mc.getProfiler().endSection();
            if (mc.player.experienceLevel > 0) {
                mc.getProfiler().startSection("expLevel");
                String s = "" + mc.player.experienceLevel;
                int i1 = (this.scaledWidth - fontRenderer.getStringWidth(s)) / 2;
                int j1 = this.scaledHeight - 31 - 4;
                fontRenderer.drawString(matrixStack, s, (float)(i1 + 1), (float)j1, 0);
                fontRenderer.drawString(matrixStack, s, (float)(i1 - 1), (float)j1, 0);
                fontRenderer.drawString(matrixStack, s, (float)i1, (float)(j1 + 1), 0);
                fontRenderer.drawString(matrixStack, s, (float)i1, (float)(j1 - 1), 0);
                fontRenderer.drawString(matrixStack, s, (float)i1, (float)j1, 8453920);
                mc.getProfiler().endSection();
            }
        }
    }
}
