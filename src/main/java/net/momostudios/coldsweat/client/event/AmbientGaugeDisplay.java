package net.momostudios.coldsweat.client.event;

import com.mojang.blaze3d.platform.GlStateManager;
import com.mojang.blaze3d.systems.RenderSystem;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.screen.inventory.InventoryScreen;
import net.minecraft.client.renderer.texture.TextureManager;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.util.ResourceLocation;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.api.distmarker.OnlyIn;
import net.minecraftforge.client.event.GuiScreenEvent;
import net.minecraftforge.client.event.RenderGameOverlayEvent;
import net.minecraftforge.eventbus.api.SubscribeEvent;
import net.minecraftforge.fml.common.Mod;
import net.momostudios.coldsweat.config.ColdSweatConfig;
import net.momostudios.coldsweat.core.util.ModItems;
import net.momostudios.coldsweat.core.util.PlayerTemp;

@Mod.EventBusSubscriber
public class AmbientGaugeDisplay
{
    @OnlyIn(Dist.CLIENT)
    @SubscribeEvent
    public static void renderAmbientTemperature(RenderGameOverlayEvent event)
    {
        PlayerEntity player = Minecraft.getInstance().player;
        if (event.getType() == RenderGameOverlayEvent.ElementType.ALL &&
           (player.getHeldItemMainhand().getItem() == ModItems.THERMOMETER ||
            player.getHeldItemOffhand().getItem()  == ModItems.THERMOMETER ||
           !ColdSweatConfig.getInstance().requireThermometer()))
        {
            int scaleX = event.getWindow().getScaledWidth();
            int scaleY = event.getWindow().getScaledHeight();

            double min = ColdSweatConfig.getInstance().minHabitable();
            double max = ColdSweatConfig.getInstance().maxHabitable();
            double mid = (min + max) / 2;
            boolean celsius = ColdSweatConfig.getInstance().celsius();
            TextureManager textureManager = Minecraft.getInstance().getTextureManager();

            double temp = PlayerTemp.getTemperature(player, PlayerTemp.Types.AMBIENT).get();
            int color = 14737376;

            ResourceLocation gaugeTexture = new ResourceLocation("cold_sweat:textures/gui/overlay/ambient/temp_gauge_normal.png");
            String gaugeLocation = "cold_sweat:textures/gui/overlay/ambient/";

            {
                if (temp > max)
                    gaugeTexture = new ResourceLocation(gaugeLocation + "temp_gauge_burning_2.png");
                else if (temp > mid + ((max - mid) * 0.75))
                    gaugeTexture = new ResourceLocation(gaugeLocation + "temp_gauge_burning_1.png");
                else if (temp > mid + ((max - mid) * 0.5))
                    gaugeTexture = new ResourceLocation(gaugeLocation + "temp_gauge_burning_0.png");
                else if (temp > mid + ((max - mid) * 0.25))
                    gaugeTexture = new ResourceLocation(gaugeLocation + "temp_gauge_hot.png");
                else if (temp >= mid - ((mid - min) * 0.25))
                    gaugeTexture = new ResourceLocation(gaugeLocation + "temp_gauge_normal.png");
                else if (temp >= mid - ((mid - min) * 0.5))
                    gaugeTexture = new ResourceLocation(gaugeLocation + "temp_gauge_cold.png");
                else if (temp >= mid - ((mid - min) * 0.75))
                    gaugeTexture = new ResourceLocation(gaugeLocation + "temp_gauge_freezing_0.png");
                else if (temp >= min)
                    gaugeTexture = new ResourceLocation(gaugeLocation + "temp_gauge_freezing_1.png");
                else if (temp < min)
                    gaugeTexture = new ResourceLocation(gaugeLocation + "temp_gauge_freezing_2.png");
            }

            RenderSystem.enableBlend();

            textureManager.bindTexture(gaugeTexture);
            Minecraft.getInstance().ingameGUI.blit(event.getMatrixStack(), (scaleX / 2) + 94, scaleY - 19,
                0, 0, 25, 16, 25, 16);

            RenderSystem.disableBlend();

            if (temp > (mid + max) / 2 && temp <= max)
                color = 16767257;
            else if (temp > max)
                color = 16728089;
            else if (temp < (mid + min) / 2 && temp >= min)
                color = 8443135;
            else if (temp < min)
                color = 3373055;

            int tempScaled = (int) (temp * 42 + 32);
            int tempMeasurement = celsius ? ((tempScaled - 32) * 5) / 9 + ColdSweatConfig.getInstance().tempOffset() : tempScaled + ColdSweatConfig.getInstance().tempOffset();

            if (temp > max || temp < min)
                Minecraft.getInstance().fontRenderer.drawString(event.getMatrixStack(), "" + tempMeasurement + "",
                    (scaleX / 2) + 107 + (Integer.toString(tempMeasurement).length() * -3), scaleY - (player.ticksExisted % 2 == 0 ? 16 : 15), color);
            else
                Minecraft.getInstance().fontRenderer.drawString(event.getMatrixStack(), "" + tempMeasurement + "",
                    (scaleX / 2) + 107 + (Integer.toString(tempMeasurement).length() * -3), scaleY - 15, color);
        }
    }
}
