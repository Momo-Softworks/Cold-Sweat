package dev.momostudios.coldsweat.client.event;

import com.mojang.blaze3d.systems.RenderSystem;
import dev.momostudios.coldsweat.common.capability.ModCapabilities;
import dev.momostudios.coldsweat.common.temperature.Temperature;
import dev.momostudios.coldsweat.config.ClientSettingsConfig;
import dev.momostudios.coldsweat.config.ConfigCache;
import dev.momostudios.coldsweat.util.math.CSMath;
import dev.momostudios.coldsweat.util.registries.ModItems;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.GuiComponent;
import net.minecraft.client.player.LocalPlayer;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.item.ItemStack;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.client.event.RenderGameOverlayEvent;
import net.minecraftforge.event.TickEvent;
import net.minecraftforge.eventbus.api.SubscribeEvent;
import net.minecraftforge.fml.common.Mod;

@Mod.EventBusSubscriber(Dist.CLIENT)
public class WorldTempGaugeDisplay
{
    private static double prevClientTemp = 0;
    public static double clientTemp = 0;

    static ClientSettingsConfig CCS = ClientSettingsConfig.getInstance();

    @SubscribeEvent
    public static void renderWorldTemperature(RenderGameOverlayEvent.Post event)
    {
        LocalPlayer player = Minecraft.getInstance().player;

        if (event.getType() == RenderGameOverlayEvent.ElementType.ALL &&
        (CSMath.isBetween(player.getInventory().findSlotMatchingItem(new ItemStack(ModItems.THERMOMETER)), 0, 8) ||
        player.getOffhandItem().getItem()  == ModItems.THERMOMETER || !ConfigCache.getInstance().showWorldTemp))
        {
            // Variables
            int scaleX = event.getWindow().getGuiScaledWidth();
            int scaleY = event.getWindow().getGuiScaledHeight();
            double min = ConfigCache.getInstance().minTemp;
            double max = ConfigCache.getInstance().maxTemp;
            double mid = (min + max) / 2;
            boolean bobbing = CCS.iconBobbing();

            // Get player world temperature
            double temp = CSMath.convertUnits(clientTemp, CCS.celsius() ? Temperature.Units.C : Temperature.Units.F, Temperature.Units.MC, true);

            // Set default color (white)
            int color = 14737376;

            // Set default gauge texture
            ResourceLocation gaugeTexture;
            String gaugeLocation = "cold_sweat:textures/gui/overlay/world_temp_gauge/";

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

            RenderSystem.setShaderTexture(0, gaugeTexture);
            RenderSystem.setShaderColor(1.0F, 1.0F, 1.0F, 1.0F);
            GuiComponent.blit(event.getMatrixStack(), (scaleX / 2) + 94, scaleY - 19, 0, 0, 25, 16, 25, 16);

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
            int bob = temp > max || temp < min ? (player.tickCount % 2 == 0 && bobbing ? 16 : 15) : 15;

            // Render text
            int blendedTemp = (int) CSMath.blend(prevClientTemp, clientTemp, Minecraft.getInstance().getFrameTime(), 0, 1);
            Minecraft.getInstance().font.draw(event.getMatrixStack(), "" + (blendedTemp + CCS.tempOffset()) + "",
                    (scaleX / 2f) + 107 + (Integer.toString(blendedTemp + CCS.tempOffset()).length() * -3), scaleY - bob, color);
        }
    }

    @SubscribeEvent
    public static void onClientTick(TickEvent.ClientTickEvent event)
    {
        if (Minecraft.getInstance().player != null)
        {
            Minecraft.getInstance().player.getCapability(ModCapabilities.PLAYER_TEMPERATURE).ifPresent(temp ->
            {
                boolean celsius = CCS.celsius();

                double tempReadout = CSMath.convertUnits(temp.get(Temperature.Types.WORLD), Temperature.Units.MC, celsius ? Temperature.Units.C : Temperature.Units.F, true);
                prevClientTemp = clientTemp;

                clientTemp = clientTemp + (tempReadout - clientTemp) / 10.0;
            });
        }
    }
}
