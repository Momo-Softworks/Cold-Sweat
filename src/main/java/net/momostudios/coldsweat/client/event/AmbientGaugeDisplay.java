package net.momostudios.coldsweat.client.event;

import com.mojang.blaze3d.systems.RenderSystem;
import net.minecraft.client.Minecraft;
import net.minecraft.client.renderer.texture.TextureManager;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.util.ResourceLocation;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.api.distmarker.OnlyIn;
import net.minecraftforge.client.event.RenderGameOverlayEvent;
import net.minecraftforge.eventbus.api.SubscribeEvent;
import net.minecraftforge.fml.common.Mod;
import net.minecraftforge.fml.common.ObfuscationReflectionHelper;
import net.momostudios.coldsweat.client.config.ClientConfigSettings;
import net.momostudios.coldsweat.config.ColdSweatConfig;
import net.momostudios.coldsweat.core.util.MathHelperCS;
import net.momostudios.coldsweat.core.util.ModItems;
import net.momostudios.coldsweat.core.util.PlayerTemp;

import java.lang.reflect.Field;

@Mod.EventBusSubscriber
public class AmbientGaugeDisplay
{
    public static class SmoothedAmbientTemperature
    {
        public double smoothTemp = 0;
        private static SmoothedAmbientTemperature INSTANCE = new SmoothedAmbientTemperature();
        public static SmoothedAmbientTemperature getInstance() {
            return INSTANCE;
        }
    }

    @OnlyIn(Dist.CLIENT)
    @SubscribeEvent
    public static void renderAmbientTemperature(RenderGameOverlayEvent.Post event)
    {
        ClientConfigSettings CCS = ClientConfigSettings.getInstance();

        PlayerEntity player = Minecraft.getInstance().player;

        if (event.getType() == RenderGameOverlayEvent.ElementType.ALL &&
           ((player.inventory.getSlotFor(ModItems.THERMOMETER.getDefaultInstance()) >= 0 &&
                   player.inventory.getSlotFor(ModItems.THERMOMETER.getDefaultInstance()) <= 8) ||
           player.getHeldItemOffhand().getItem() == ModItems.THERMOMETER || !ColdSweatConfig.getInstance().requireThermometer()))
        {
            int scaleX = event.getWindow().getScaledWidth();
            int scaleY = event.getWindow().getScaledHeight();

            double min = ColdSweatConfig.getInstance().minHabitable();
            double max = ColdSweatConfig.getInstance().maxHabitable();
            double mid = (min + max) / 2;
            boolean celsius = CCS.celsius;
            boolean bobbing = CCS.iconBobbing;
            TextureManager textureManager = Minecraft.getInstance().getTextureManager();

            double temp = PlayerTemp.getTemperature(player, PlayerTemp.Types.AMBIENT).get();
            double temp2 = SmoothedAmbientTemperature.getInstance().smoothTemp;
            int color = 14737376;

            ResourceLocation gaugeTexture = new ResourceLocation("cold_sweat:textures/gui/overlay/ambient/temp_gauge_normal.png");
            String gaugeLocation = "cold_sweat:textures/gui/overlay/ambient/";

            {
                if (temp2 > max)
                    gaugeTexture = new ResourceLocation(gaugeLocation + "temp_gauge_burning_2.png");
                else if (temp2 > mid + ((max - mid) * 0.75))
                    gaugeTexture = new ResourceLocation(gaugeLocation + "temp_gauge_burning_1.png");
                else if (temp2 > mid + ((max - mid) * 0.5))
                    gaugeTexture = new ResourceLocation(gaugeLocation + "temp_gauge_burning_0.png");
                else if (temp2 > mid + ((max - mid) * 0.25))
                    gaugeTexture = new ResourceLocation(gaugeLocation + "temp_gauge_hot.png");
                else if (temp2 >= mid - ((mid - min) * 0.25))
                    gaugeTexture = new ResourceLocation(gaugeLocation + "temp_gauge_normal.png");
                else if (temp2 >= mid - ((mid - min) * 0.5))
                    gaugeTexture = new ResourceLocation(gaugeLocation + "temp_gauge_cold.png");
                else if (temp2 >= mid - ((mid - min) * 0.75))
                    gaugeTexture = new ResourceLocation(gaugeLocation + "temp_gauge_freezing_0.png");
                else if (temp2 >= min)
                    gaugeTexture = new ResourceLocation(gaugeLocation + "temp_gauge_freezing_1.png");
                else if (temp2 < min)
                    gaugeTexture = new ResourceLocation(gaugeLocation + "temp_gauge_freezing_2.png");
            }

            RenderSystem.enableBlend();
            RenderSystem.defaultBlendFunc();

            textureManager.bindTexture(gaugeTexture);
            RenderSystem.color4f(1.0F, 1.0F, 1.0F, 1.0F);
            Minecraft.getInstance().ingameGUI.blit(event.getMatrixStack(), (scaleX / 2) + 94, scaleY - 19,
                0, 0, 25, 16, 25, 16);

            RenderSystem.disableBlend();

            if (temp2 > (mid + max) / 2 && temp2 <= max)
                color = 16767257;
            else if (temp2 > max)
                color = 16728089;
            else if (temp2 < (mid + min) / 2 && temp2 >= min)
                color = 8443135;
            else if (temp2 < min)
                color = 4236031;


            int fps = 1;
            try {
                Field debugFPS = ObfuscationReflectionHelper.findField(Minecraft.class, "debugFPS");
                fps = debugFPS.getInt(debugFPS);
            } catch (Exception e) {
                System.out.println(e.getMessage());
            }
            SmoothedAmbientTemperature.getInstance().smoothTemp = temp2 + ((temp - temp2) / (fps / 4d));

            int tempScaled = (int) MathHelperCS.convertToF(SmoothedAmbientTemperature.getInstance().smoothTemp);
            int tempMeasurement = (int) (celsius ? MathHelperCS.FtoC(tempScaled) : tempScaled) + CCS.tempOffset;

            if (temp2 > max || temp2 < min)
                Minecraft.getInstance().fontRenderer.drawString(event.getMatrixStack(), "" + tempMeasurement + "",
                    (scaleX / 2) + 107 + (Integer.toString(tempMeasurement).length() * -3), scaleY - (player.ticksExisted % 2 == 0 && bobbing ? 16 : 15), color);
            else
                Minecraft.getInstance().fontRenderer.drawString(event.getMatrixStack(), "" + tempMeasurement + "",
                    (scaleX / 2) + 107 + (Integer.toString(tempMeasurement).length() * -3), scaleY - 15, color);
        }
    }
}
