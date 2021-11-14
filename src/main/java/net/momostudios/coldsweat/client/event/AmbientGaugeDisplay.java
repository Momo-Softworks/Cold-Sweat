package net.momostudios.coldsweat.client.event;

import com.mojang.blaze3d.systems.RenderSystem;
import net.minecraft.client.Minecraft;
import net.minecraft.client.renderer.texture.TextureManager;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.item.ItemStack;
import net.minecraft.util.ResourceLocation;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.client.event.RenderGameOverlayEvent;
import net.minecraftforge.eventbus.api.SubscribeEvent;
import net.minecraftforge.fml.common.Mod;
import net.momostudios.coldsweat.config.ClientSettingsConfig;
import net.momostudios.coldsweat.config.ColdSweatConfig;
import net.momostudios.coldsweat.core.util.MathHelperCS;
import net.momostudios.coldsweat.core.util.PlayerTemp;
import net.momostudios.coldsweat.core.util.registrylists.ModItems;

@Mod.EventBusSubscriber(Dist.CLIENT)
public class AmbientGaugeDisplay
{
    @SubscribeEvent
    public static void renderAmbientTemperature(RenderGameOverlayEvent.Post event)
    {
        ClientSettingsConfig CCS = ClientSettingsConfig.getInstance();

        PlayerEntity player = Minecraft.getInstance().player;

        if (event.getType() == RenderGameOverlayEvent.ElementType.ALL &&
                (MathHelperCS.isBetween(player.inventory.getSlotFor(new ItemStack(ModItems.THERMOMETER)), 0, 8) ||
                 player.getHeldItemOffhand().getItem()  == ModItems.THERMOMETER ||
                 !ColdSweatConfig.getInstance().showAmbient()))
        {
            int scaleX = event.getWindow().getScaledWidth();
            int scaleY = event.getWindow().getScaledHeight();

            double min = ColdSweatConfig.getInstance().minHabitable();
            double max = ColdSweatConfig.getInstance().maxHabitable();
            double mid = (min + max) / 2;
            boolean celsius = CCS.celsius();
            boolean bobbing = CCS.iconBobbing();
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
            RenderSystem.defaultBlendFunc();

            textureManager.bindTexture(gaugeTexture);
            RenderSystem.color4f(1.0F, 1.0F, 1.0F, 1.0F);
            Minecraft.getInstance().ingameGUI.blit(event.getMatrixStack(), (scaleX / 2) + 94, scaleY - 19,
                0, 0, 25, 16, 25, 16);

            RenderSystem.disableBlend();

            if (temp > (mid + max) / 2 && temp <= max)
                color = 16297781;
            else if (temp > max)
                color = 16728089;
            else if (temp < (mid + min) / 2 && temp >= min)
                color = 8443135;
            else if (temp < min)
                color = 4236031;

            int tempScaled = (int) MathHelperCS.convertToF(temp);
            int tempMeasurement = (int) (celsius ? MathHelperCS.FtoC(tempScaled) : tempScaled) + CCS.tempOffset();

            if (temp > max || temp < min)
                Minecraft.getInstance().fontRenderer.drawString(event.getMatrixStack(), "" + tempMeasurement + "",
                    (scaleX / 2f) + 107 + (Integer.toString(tempMeasurement).length() * -3), scaleY - (player.ticksExisted % 2 == 0 && bobbing ? 16 : 15), color);
            else
                Minecraft.getInstance().fontRenderer.drawString(event.getMatrixStack(), "" + tempMeasurement + "",
                    (scaleX / 2f) + 107 + (Integer.toString(tempMeasurement).length() * -3), scaleY - 15, color);
        }
    }
}
