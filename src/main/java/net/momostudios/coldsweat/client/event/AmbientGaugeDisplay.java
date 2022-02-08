package net.momostudios.coldsweat.client.event;

import com.mojang.blaze3d.systems.RenderSystem;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.AbstractGui;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.item.ItemStack;
import net.minecraft.util.ResourceLocation;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.client.event.RenderGameOverlayEvent;
import net.minecraftforge.client.model.animation.Animation;
import net.minecraftforge.event.TickEvent;
import net.minecraftforge.eventbus.api.SubscribeEvent;
import net.minecraftforge.fml.common.Mod;
import net.momostudios.coldsweat.config.ClientSettingsConfig;
import net.momostudios.coldsweat.config.ConfigCache;
import net.momostudios.coldsweat.util.CSMath;
import net.momostudios.coldsweat.util.PlayerHelper;
import net.momostudios.coldsweat.util.Units;
import net.momostudios.coldsweat.util.registrylists.ModItems;

@Mod.EventBusSubscriber(Dist.CLIENT)
public class AmbientGaugeDisplay
{
    private static double prevClientTemp = 0;
    public static double clientTemp = 0;

    static ClientSettingsConfig CCS = ClientSettingsConfig.getInstance();

    @SubscribeEvent
    public static void renderAmbientTemperature(RenderGameOverlayEvent.Post event)
    {
        PlayerEntity player = Minecraft.getInstance().player;

        if (event.getType() == RenderGameOverlayEvent.ElementType.ALL &&
        (CSMath.isBetween(player.inventory.getSlotFor(new ItemStack(ModItems.THERMOMETER)), 0, 8) ||
        player.getHeldItemOffhand().getItem()  == ModItems.THERMOMETER || !ConfigCache.getInstance().showAmbient))
        {
            // Variables
            int scaleX = event.getWindow().getScaledWidth();
            int scaleY = event.getWindow().getScaledHeight();
            double min = ConfigCache.getInstance().minTemp;
            double max = ConfigCache.getInstance().maxTemp;
            double mid = (min + max) / 2;
            boolean bobbing = CCS.iconBobbing();

            // Get player ambient temperature
            double temp = CSMath.convertUnits(clientTemp, CCS.celsius() ? Units.C : Units.F, Units.MC, true);

            // Set default color (white)
            int color = 14737376;

            // Set default gauge texture
            ResourceLocation gaugeTexture = new ResourceLocation("cold_sweat:textures/gui/overlay/ambient/temp_gauge_normal.png");
            String gaugeLocation = "cold_sweat:textures/gui/overlay/ambient/";

            // Set gauge texture based on temperature
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
            else
                gaugeTexture = new ResourceLocation(gaugeLocation + "temp_gauge_freezing_2.png");

            // Render gauge
            RenderSystem.enableBlend();
            RenderSystem.defaultBlendFunc();

            Minecraft.getInstance().getTextureManager().bindTexture(gaugeTexture);
            RenderSystem.color4f(1.0F, 1.0F, 1.0F, 1.0F);
            AbstractGui.blit(event.getMatrixStack(), (scaleX / 2) + 94, scaleY - 19, 0, 0, 25, 16, 25, 16);

            RenderSystem.disableBlend();

            // Set text based on temperature
            if (temp > (mid + max) / 2 && temp <= max)
                color = 16297781;
            else if (temp > max)
                color = 16728089;
            else if (temp < (mid + min) / 2 && temp >= min)
                color = 8443135;
            else if (temp < min)
                color = 4236031;

            // Sets the text bobbing offset (or none if disabled)
            int bob = temp > max || temp < min ? (player.ticksExisted % 2 == 0 && bobbing ? 16 : 15) : 15;

            // Render text
            int blendedTemp = (int) CSMath.blend(prevClientTemp, clientTemp, Animation.getPartialTickTime(), 0, 1);
            Minecraft.getInstance().fontRenderer.drawString(event.getMatrixStack(), "" + (blendedTemp + CCS.tempOffset()) + "",
                    (scaleX / 2f) + 107 + (Integer.toString(blendedTemp + CCS.tempOffset()).length() * -3), scaleY - bob, color);
        }
    }

    @SubscribeEvent
    public static void onClientTick(TickEvent.ClientTickEvent event)
    {
        if (Minecraft.getInstance().player != null)
        {
            boolean celsius = CCS.celsius();

            double tempReadout = CSMath.convertUnits(PlayerHelper.getTemperature(Minecraft.getInstance().player, PlayerHelper.Types.AMBIENT).get(),
                    Units.MC, celsius ? Units.C : Units.F, true);
            prevClientTemp = clientTemp;

            clientTemp = clientTemp + (tempReadout - clientTemp) / 10.0;
        }
    }
}
