package net.momostudios.coldsweat.client.event;

import com.mojang.blaze3d.matrix.MatrixStack;
import com.mojang.blaze3d.platform.GlStateManager;
import com.mojang.blaze3d.systems.RenderSystem;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.FontRenderer;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.util.ResourceLocation;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.api.distmarker.OnlyIn;
import net.minecraftforge.client.event.RenderGameOverlayEvent;
import net.minecraftforge.eventbus.api.EventPriority;
import net.minecraftforge.eventbus.api.SubscribeEvent;
import net.minecraftforge.fml.common.Mod;
import net.momostudios.coldsweat.client.config.ClientConfigSettings;
import net.momostudios.coldsweat.config.ColdSweatConfig;
import net.momostudios.coldsweat.core.util.PlayerTemp;

@Mod.EventBusSubscriber
public class SelfTempDisplay
{
    @OnlyIn(Dist.CLIENT)
    @SubscribeEvent(priority = EventPriority.NORMAL)
    public static void eventHandler(RenderGameOverlayEvent event)
    {
        ClientConfigSettings CCS = ClientConfigSettings.getInstance();
        Minecraft mc = Minecraft.getInstance();

        if (mc.getRenderViewEntity() != null && mc.getRenderViewEntity() instanceof PlayerEntity &&
        !event.isCancelable() && event.getType() == RenderGameOverlayEvent.ElementType.HOTBAR && !((PlayerEntity) mc.getRenderViewEntity()).abilities.isCreativeMode &&
        !mc.getRenderViewEntity().isSpectator())
        {
            int scaleX = event.getWindow().getScaledWidth();
            int scaleY = event.getWindow().getScaledHeight();
            PlayerEntity entity = (PlayerEntity) Minecraft.getInstance().getRenderViewEntity();
            double x = entity.getPosX();
            double y = entity.getPosY();
            double z = entity.getPosZ();

            double temp = (int) PlayerTemp.getTemperature(entity, PlayerTemp.Types.COMPOSITE).get();

            int threatLevel = 0;

            ResourceLocation icon;
            int color =
                    temp > 0 ? 16744509 :
                    temp < 0 ? 4233468 :
                    11513775;
            int colorBG =
                    temp < 0 ? 1122643 :
                    temp > 0 ? 5376516 :
                    0;
            int colorBG2 =
                    temp < -100 && temp >= -110 ? 6866175 :
                    temp < -110 && temp >= -120 ? 7390719 :
                    temp < -120 && temp >= -130 ? 9824511 :
                    temp < -130 && temp >= -140 ? 12779519 :
                    temp < - 140 ?                16777215 :
                    temp > 100 && temp <= 110 ? 16744509 :
                    temp > 110 && temp <= 120 ? 16755544 :
                    temp > 120 && temp <= 130 ? 16766325 :
                    temp > 130 && temp <= 140 ? 16771509 :
                    temp > 140 ? 16777215 : 0;

            if      (temp >= 100)   {icon = new ResourceLocation("cold_sweat:textures/gui/overlay/temp_gauge_hot_2.png");   threatLevel = 2;}
            else if (temp >= 66)    {icon = new ResourceLocation("cold_sweat:textures/gui/overlay/temp_gauge_hot_1.png");   threatLevel = 1;}
            else if (temp >= 33)    {icon = new ResourceLocation("cold_sweat:textures/gui/overlay/temp_gauge_hot_0.png");}
            else if (temp >= 0)     {icon = new ResourceLocation("cold_sweat:textures/gui/overlay/temp_gauge_default.png");}
            else if (temp > -33)    {icon = new ResourceLocation("cold_sweat:textures/gui/overlay/temp_gauge_default.png");}
            else if (temp > -66)    {icon = new ResourceLocation("cold_sweat:textures/gui/overlay/temp_gauge_cold_0.png");}
            else if (temp > -100)   {icon = new ResourceLocation("cold_sweat:textures/gui/overlay/temp_gauge_cold_1.png");  threatLevel = 1;}
            else                    {icon = new ResourceLocation("cold_sweat:textures/gui/overlay/temp_gauge_cold_2.png");  threatLevel = 2;}
            RenderSystem.blendFuncSeparate(GlStateManager.SourceFactor.SRC_ALPHA, GlStateManager.DestFactor.ONE_MINUS_SRC_ALPHA, GlStateManager.SourceFactor.ONE, GlStateManager.DestFactor.ZERO);

            int threatOffset = 0;
            if (CCS.iconBobbing)
            {
                if (threatLevel == 1) threatOffset = entity.ticksExisted % 10 == 0 && Math.random() < 0.5 ? 1 : 0;
                if (threatLevel == 2) threatOffset = entity.ticksExisted % 2 == 0 ? 1 : 0;
            }

            mc.getTextureManager().bindTexture(icon);
            mc.ingameGUI.blit(event.getMatrixStack(), (scaleX / 2) - 5 + CCS.steveHeadX,
                scaleY - 51 + threatOffset + CCS.steveHeadY, 0, 0, 10, 10, 10, 10);


            FontRenderer fontRenderer = mc.fontRenderer;
            int scaledWidth = mc.getMainWindow().getScaledWidth();
            int scaledHeight = mc.getMainWindow().getScaledHeight();
            MatrixStack matrixStack = event.getMatrixStack();

            String s = "" + (int) Math.min(Math.abs(temp), 100);
            float i1 = (scaledWidth - fontRenderer.getStringWidth(s)) / 2f + CCS.tempGaugeX;
            float j1 = scaledHeight - 31f - 7f + CCS.tempGaugeY;
            if (temp > 100 || temp < -100)
            {
                fontRenderer.drawString(matrixStack, s, i1 + 2f, j1, colorBG2);
                fontRenderer.drawString(matrixStack, s, i1 - 2f, j1, colorBG2);
                fontRenderer.drawString(matrixStack, s, i1, j1 + 2f, colorBG2);
                fontRenderer.drawString(matrixStack, s, i1, j1 - 2f, colorBG2);
                fontRenderer.drawString(matrixStack, s, i1 + 1f, j1 + 1f, colorBG2);
                fontRenderer.drawString(matrixStack, s, i1 + 1f, j1 - 1f, colorBG2);
                fontRenderer.drawString(matrixStack, s, i1 - 1f, j1 - 1f, colorBG2);
                fontRenderer.drawString(matrixStack, s, i1 - 1f, j1 + 1f, colorBG2);
            }
            fontRenderer.drawString(matrixStack, s, i1 + 1, j1, colorBG);
            fontRenderer.drawString(matrixStack, s, i1 - 1, j1, colorBG);
            fontRenderer.drawString(matrixStack, s, i1, j1 + 1, colorBG);
            fontRenderer.drawString(matrixStack, s, i1, j1 - 1, colorBG);
            fontRenderer.drawString(matrixStack, s, i1, j1, color);
        }
    }
}

