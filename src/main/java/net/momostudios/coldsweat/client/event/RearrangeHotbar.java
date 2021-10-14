package net.momostudios.coldsweat.client.event;

import com.mojang.blaze3d.matrix.MatrixStack;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.FontRenderer;
import net.minecraft.util.ResourceLocation;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.api.distmarker.OnlyIn;
import net.minecraftforge.client.event.RenderGameOverlayEvent;
import net.minecraftforge.eventbus.api.EventPriority;
import net.minecraftforge.eventbus.api.SubscribeEvent;
import net.minecraftforge.fml.common.Mod;
import net.momostudios.coldsweat.client.config.ClientConfigSettings;
import net.momostudios.coldsweat.config.ColdSweatConfig;

@Mod.EventBusSubscriber
public class RearrangeHotbar
{
    @OnlyIn(Dist.CLIENT)
    @SubscribeEvent(priority = EventPriority.NORMAL)
    public static void eventHandler(RenderGameOverlayEvent event)
    {
        ClientConfigSettings CCS = ClientConfigSettings.getInstance();
        Minecraft mc = Minecraft.getInstance();

        if (CCS.customHotbar && !mc.gameSettings.hideGUI && !mc.player.isSpectator())
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
                if (mc.player.experienceLevel > 0 && mc.playerController.gameIsSurvivalOrAdventure())
                {
                    mc.getProfiler().startSection("expLevel");
                    String s = "" + mc.player.experienceLevel;
                    int i1;
                    int j1;

                    if (fontRenderer.getStringWidth(s) < 8) i1 = scaledWidth / 2 - fontRenderer.getStringWidth(s) / 2 - 82;
                    else if (fontRenderer.getStringWidth(s) < 13) i1 = scaledWidth / 2 - fontRenderer.getStringWidth(s) / 2 - 84;
                    else i1 = scaledWidth / 2 - fontRenderer.getStringWidth(s) - 78;
                    j1 = scaledHeight - 31 + 1;

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
