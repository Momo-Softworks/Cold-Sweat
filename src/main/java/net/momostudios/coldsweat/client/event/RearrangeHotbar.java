package net.momostudios.coldsweat.client.event;

import com.mojang.blaze3d.matrix.MatrixStack;
import com.mojang.blaze3d.systems.RenderSystem;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.AbstractGui;
import net.minecraft.client.gui.FontRenderer;
import net.minecraft.client.gui.IngameGui;
import net.minecraft.item.ItemStack;
import net.minecraft.util.ResourceLocation;
import net.minecraft.util.text.IFormattableTextComponent;
import net.minecraft.util.text.ITextComponent;
import net.minecraft.util.text.StringTextComponent;
import net.minecraft.util.text.TextFormatting;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.api.distmarker.OnlyIn;
import net.minecraftforge.client.event.RenderGameOverlayEvent;
import net.minecraftforge.eventbus.api.EventPriority;
import net.minecraftforge.eventbus.api.SubscribeEvent;
import net.minecraftforge.fml.common.Mod;

@Mod.EventBusSubscriber
public class RearrangeHotbar
{
    public static Minecraft mc = Minecraft.getInstance();

    @OnlyIn(Dist.CLIENT)
    @SubscribeEvent(priority = EventPriority.NORMAL)
    public static void eventHandler(RenderGameOverlayEvent event)
    {
        if (!mc.gameSettings.hideGUI && !mc.player.isSpectator())
        {
            MatrixStack matrixStack = event.getMatrixStack();
            int xPos = mc.getMainWindow().getScaledWidth() / 2 - 81;
            FontRenderer fontRenderer = mc.fontRenderer;
            int scaledWidth = mc.getMainWindow().getScaledWidth();
            int scaledHeight = mc.getMainWindow().getScaledHeight();

            if (event.getType() == RenderGameOverlayEvent.ElementType.EXPERIENCE)
            {
                event.setCanceled(true);

                // Draw xp bar
                if (mc.playerController.gameIsSurvivalOrAdventure())
                {
                    mc.getProfiler().startSection("expBar");
                    mc.getTextureManager().bindTexture(new ResourceLocation("cold_sweat:textures/gui/overlay/xp_bars.png"));
                    int i = mc.player.xpBarCap();
                    if (i > 0)
                    {
                        // Render full bar if level is 0
                        if (mc.player.experienceLevel > 0)
                        {
                            int j = 182;
                            int k = (int) (mc.player.experience * 167.88F) + 2;
                            int l = scaledHeight - 32 + 3;
                            Minecraft.getInstance().ingameGUI.blit(matrixStack, xPos, l, 10, 64, 182, 5);
                            if (k > 0) {
                                Minecraft.getInstance().ingameGUI.blit(matrixStack, xPos, l, 10, 69, k, 5);
                            }
                        }
                        // Render shorter bar to make room for experience level
                        else
                        {
                            int j = 182;
                            int k = (int) (mc.player.experience * 183.0F);
                            int l = scaledHeight - 32 + 3;
                            Minecraft.getInstance().ingameGUI.blit(matrixStack, xPos - 10, l, 0, 74, 182, 5);
                            if (k > 0) {
                                Minecraft.getInstance().ingameGUI.blit(matrixStack, xPos - 10, l, 0, 79, k, 5);
                            }
                        }
                    }
                }

                //Draw Number
                mc.getProfiler().endSection();
                if (mc.player.experienceLevel > 0)
                {
                    mc.getProfiler().startSection("expLevel");
                    String s = "" + mc.player.experienceLevel;
                    int i1;
                    int j1;
                    // Get game mode for positioning
                    if (mc.playerController.gameIsSurvivalOrAdventure())
                    {
                        if (fontRenderer.getStringWidth(s) < 8) i1 = scaledWidth / 2 - fontRenderer.getStringWidth(s) / 2 - 82;
                        else if (fontRenderer.getStringWidth(s) < 13) i1 = scaledWidth / 2 - fontRenderer.getStringWidth(s) / 2 - 84;
                        else i1 = scaledWidth / 2 - fontRenderer.getStringWidth(s) - 78;
                        j1 = scaledHeight - 31 + 1;
                    }
                    else
                    {
                        i1 = (scaledWidth - fontRenderer.getStringWidth(s)) / 2;
                        j1 = scaledHeight - 31 - 4;
                    }
                    // Draw the number with outline
                    fontRenderer.drawString(matrixStack, s, (float) (i1 + 1), (float) j1, 0);
                    fontRenderer.drawString(matrixStack, s, (float) (i1 - 1), (float) j1, 0);
                    fontRenderer.drawString(matrixStack, s, (float) i1, (float) (j1 + 1), 0);
                    fontRenderer.drawString(matrixStack, s, (float) i1, (float) (j1 - 1), 0);
                    fontRenderer.drawString(matrixStack, s, (float) i1, (float) j1, 8453920);
                    mc.getProfiler().endSection();
                }
            }
        }
    }
}
