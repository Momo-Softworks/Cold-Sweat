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
            int xPos = mc.getMainWindow().getScaledWidth() / 2 - 91;
            FontRenderer fontRenderer = mc.fontRenderer;
            int scaledWidth = mc.getMainWindow().getScaledWidth();
            int scaledHeight = mc.getMainWindow().getScaledHeight();
            if (event.getType() == RenderGameOverlayEvent.ElementType.EXPERIENCE)
            {
                event.setCanceled(true);

                if (mc.playerController.gameIsSurvivalOrAdventure()) {
                    mc.getProfiler().startSection("expBar");
                    mc.getTextureManager().bindTexture(new ResourceLocation("cold_sweat:textures/gui/overlay/xp_bars.png"));
                    int i = mc.player.xpBarCap();
                    if (i > 0) {
                        int j = 182;
                        int k = (int) (mc.player.experience * 183.0F);
                        int l = scaledHeight - 32 + 3;
                        Minecraft.getInstance().ingameGUI.blit(matrixStack, xPos, l, 0, 64, 182, 5);
                        if (k > 0)
                        {
                            Minecraft.getInstance().ingameGUI.blit(matrixStack, xPos, l, 0, 69, k, 5);
                        }
                    }
                }

                mc.getProfiler().endSection();
                if (mc.player.experienceLevel > 0) {
                    mc.getProfiler().startSection("expLevel");
                    String s = "" + mc.player.experienceLevel;
                    int i1 = (scaledWidth - fontRenderer.getStringWidth(s)) / 2 - 100;
                    int j1 = scaledHeight - 31 + 1;
                    fontRenderer.drawString(matrixStack, s, (float) (i1 + 1), (float) j1, 0);
                    fontRenderer.drawString(matrixStack, s, (float) (i1 - 1), (float) j1, 0);
                    fontRenderer.drawString(matrixStack, s, (float) i1, (float) (j1 + 1), 0);
                    fontRenderer.drawString(matrixStack, s, (float) i1, (float) (j1 - 1), 0);
                    fontRenderer.drawString(matrixStack, s, (float) i1, (float) j1, 8453920);
                    mc.getProfiler().endSection();
                }
            }
            /*else if (event.getType() == RenderGameOverlayEvent.ElementType.ALL)
            {
                mc.gameSettings.heldItemTooltips = false;

                int remainingHighlightTicks = 20;

                ItemStack highlightingItemStack = mc.player.inventory.getCurrentItem();

                mc.getProfiler().startSection("selectedItemName");
                if (remainingHighlightTicks > 0 && !highlightingItemStack.isEmpty())
                {
                    IFormattableTextComponent iformattabletextcomponent = (new StringTextComponent("")).appendSibling(highlightingItemStack.getDisplayName()).mergeStyle(highlightingItemStack.getRarity().color);
                    if (highlightingItemStack.hasDisplayName()) {
                        iformattabletextcomponent.mergeStyle(TextFormatting.ITALIC);
                    }

                    ITextComponent highlightTip = highlightingItemStack.getHighlightTip(iformattabletextcomponent);
                    int i = fontRenderer.getStringPropertyWidth(highlightTip);
                    int j = (scaledWidth - i) / 2;
                    int k = scaledHeight - 64;
                    if (!mc.playerController.shouldDrawHUD()) {
                        k += 14;
                    }

                    int l = (int)((float) remainingHighlightTicks * 256.0F / 10.0F);
                    if (l > 255) {
                        l = 255;
                    }

                    if (l > 0) {
                        RenderSystem.pushMatrix();
                        RenderSystem.enableBlend();
                        RenderSystem.defaultBlendFunc();
                        IngameGui.fill(matrixStack, j - 2, k - 2, j + i + 2, k + 9 + 2, mc.gameSettings.getChatBackgroundColor(0));
                        FontRenderer font = highlightingItemStack.getItem().getFontRenderer(highlightingItemStack);
                        if (font == null) {
                            fontRenderer.drawTextWithShadow(matrixStack, highlightTip, (float)j, (float)k, 16777215 + (l << 24));
                        } else {
                            j = (scaledWidth - font.getStringPropertyWidth(highlightTip)) / 2;
                            font.drawTextWithShadow(matrixStack, highlightTip, (float)j, (float)k, 16777215 + (l << 24));
                        }
                        RenderSystem.disableBlend();
                        RenderSystem.popMatrix();
                    }
                }
            }*/
        }
    }
}
