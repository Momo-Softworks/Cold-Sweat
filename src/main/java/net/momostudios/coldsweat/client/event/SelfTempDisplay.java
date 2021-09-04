package net.momostudios.coldsweat.client.event;

import com.mojang.blaze3d.matrix.MatrixStack;
import net.minecraft.client.gui.FontRenderer;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.api.distmarker.OnlyIn;
import net.minecraftforge.fml.common.Mod;
import net.minecraftforge.eventbus.api.SubscribeEvent;
import net.minecraftforge.eventbus.api.EventPriority;
import net.minecraftforge.client.event.RenderGameOverlayEvent;

import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.client.Minecraft;
import com.mojang.blaze3d.platform.GlStateManager;
import com.mojang.blaze3d.systems.RenderSystem;
import net.minecraft.util.ResourceLocation;
import net.momostudios.coldsweat.config.ColdSweatConfig;

@Mod.EventBusSubscriber
public class SelfTempDisplay
{
    @OnlyIn(Dist.CLIENT)
    @SubscribeEvent(priority = EventPriority.NORMAL)
    public static void eventHandler(RenderGameOverlayEvent event)
    {
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

            double temp = entity.getPersistentData().getDouble("composite_temperature");
            //temp = PlayerTemp.getTemperature(entity, PlayerTemp.Types.COMPOSITE).get();

            int threatLevel = 0;

            ResourceLocation icon;
            int color;
            int colorBG;
            if (temp >= 1)       {color=16744509;colorBG=5376516;}
            else if (temp <= -1) {color=4299519; colorBG=1122644;}
            else                 {color=11513775;colorBG=0;}

            if      (temp >= 100)   {icon = new ResourceLocation("cold_sweat:textures/gui/overlay/temp_gauge_hot_2.png");   threatLevel = 2;}
            else if (temp >= 66)    {icon = new ResourceLocation("cold_sweat:textures/gui/overlay/temp_gauge_hot_1.png");   threatLevel = 1;}
            else if (temp >= 33)    {icon = new ResourceLocation("cold_sweat:textures/gui/overlay/temp_gauge_hot_0.png");}
            else if (temp >= 0)     {icon = new ResourceLocation("cold_sweat:textures/gui/overlay/temp_gauge_default.png");}
            else if (temp > -33)    {icon = new ResourceLocation("cold_sweat:textures/gui/overlay/temp_gauge_default.png");}
            else if (temp > -66)    {icon = new ResourceLocation("cold_sweat:textures/gui/overlay/temp_gauge_cold_0.png");}
            else if (temp > -100)    {icon = new ResourceLocation("cold_sweat:textures/gui/overlay/temp_gauge_cold_1.png");  threatLevel = 1;}
            else                    {icon = new ResourceLocation("cold_sweat:textures/gui/overlay/temp_gauge_cold_2.png");  threatLevel = 2;}
            RenderSystem.blendFuncSeparate(GlStateManager.SourceFactor.SRC_ALPHA, GlStateManager.DestFactor.ONE_MINUS_SRC_ALPHA, GlStateManager.SourceFactor.ONE, GlStateManager.DestFactor.ZERO);

            int threatOffset = 0;
            if (ColdSweatConfig.getInstance().iconBobbing())
            {
                if (threatLevel == 1) threatOffset = entity.ticksExisted % 10 == 0 && Math.random() < 0.5 ? 1 : 0;
                if (threatLevel == 2) threatOffset = entity.ticksExisted % 2 == 0 ? 1 : 0;
            }

            mc.getTextureManager().bindTexture(icon);
            mc.ingameGUI.blit(event.getMatrixStack(), (scaleX / 2) - 5 + ColdSweatConfig.getInstance().steveHeadX(),
                scaleY - 51 + threatOffset + ColdSweatConfig.getInstance().steveHeadY(), 0, 0, 10, 10, 10, 10);


            FontRenderer fontRenderer = mc.fontRenderer;
            int scaledWidth = mc.getMainWindow().getScaledWidth();
            int scaledHeight = mc.getMainWindow().getScaledHeight();
            MatrixStack matrixStack = event.getMatrixStack();

            String s = "" + (int) Math.abs(temp);
            int i1 = (scaledWidth - fontRenderer.getStringWidth(s)) / 2 + ColdSweatConfig.getInstance().tempGaugeX();
            int j1 = scaledHeight - 31 - 7 + ColdSweatConfig.getInstance().tempGaugeY();
            fontRenderer.drawString(matrixStack, s, (float) (i1 + 1), (float) j1, colorBG);
            fontRenderer.drawString(matrixStack, s, (float) (i1 - 1), (float) j1, colorBG);
            fontRenderer.drawString(matrixStack, s, (float) i1, (float) (j1 + 1), colorBG);
            fontRenderer.drawString(matrixStack, s, (float) i1, (float) (j1 - 1), colorBG);
            fontRenderer.drawString(matrixStack, s, (float) i1, (float) j1, color);
        }
    }
}

