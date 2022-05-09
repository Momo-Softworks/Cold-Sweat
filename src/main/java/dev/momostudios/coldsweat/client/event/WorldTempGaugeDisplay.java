package dev.momostudios.coldsweat.client.event;

import com.mojang.blaze3d.systems.RenderSystem;
import dev.momostudios.coldsweat.common.capability.ITemperatureCap;
import dev.momostudios.coldsweat.common.capability.ModCapabilities;
import dev.momostudios.coldsweat.common.capability.PlayerTempCapability;
import dev.momostudios.coldsweat.api.temperature.Temperature;
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
    private static double PREV_BLENDED_TEMP = 0;
    public static double BLENDED_TEMP = 0;

    static ClientSettingsConfig CCS = ClientSettingsConfig.getInstance();

    @SubscribeEvent
    public static void renderWorldTemperature(RenderGameOverlayEvent.Post event)
    {
        LocalPlayer player = Minecraft.getInstance().player;

        if (event.getType() == RenderGameOverlayEvent.ElementType.ALL &&
        (CSMath.isBetween(player.getInventory().findSlotMatchingItem(new ItemStack(ModItems.THERMOMETER)), 0, 8) ||
        player.getOffhandItem().getItem()  == ModItems.THERMOMETER || !ConfigCache.getInstance().showWorldTemp))
        {
            int scaleX = event.getWindow().getGuiScaledWidth();
            int scaleY = event.getWindow().getGuiScaledHeight();

            ITemperatureCap tempCap = player.getCapability(ModCapabilities.PLAYER_TEMPERATURE).orElse(new PlayerTempCapability());

            double min = ConfigCache.getInstance().minTemp + tempCap.get(Temperature.Types.MIN);
            double max = ConfigCache.getInstance().maxTemp + tempCap.get(Temperature.Types.MAX);

            boolean bobbing = CCS.iconBobbing();

            // Get player world temperature
            double temp = CSMath.convertUnits(BLENDED_TEMP, CCS.celsius() ? Temperature.Units.C : Temperature.Units.F, Temperature.Units.MC, true);

            // Get the temperature severity
            int severity = getSeverity(temp, min, max);

            // Set text color (white)
            int color = switch (severity)
            {
                case  2,3  -> 16297781;
                case  4    -> 16728089;
                case -2,-3 -> 8443135;
                case -4    -> 4236031;
                default -> 14737376;
            };

            // Set default gauge texture
            String gaugeLocation = "cold_sweat:textures/gui/overlay/world_temp_gauge/";
            ResourceLocation gaugeTexture = switch (severity)
            {
                case  1 -> new ResourceLocation(gaugeLocation + "temp_gauge_hot.png");
                case  2 -> new ResourceLocation(gaugeLocation + "temp_gauge_burning_0.png");
                case  3 -> new ResourceLocation(gaugeLocation + "temp_gauge_burning_1.png");
                case  4 -> new ResourceLocation(gaugeLocation + "temp_gauge_burning_2.png");
                case -1 -> new ResourceLocation(gaugeLocation + "temp_gauge_cold.png");
                case -2 -> new ResourceLocation(gaugeLocation + "temp_gauge_freezing_0.png");
                case -3 -> new ResourceLocation(gaugeLocation + "temp_gauge_freezing_1.png");
                case -4 -> new ResourceLocation(gaugeLocation + "temp_gauge_freezing_2.png");
                default -> new ResourceLocation(gaugeLocation + "temp_gauge_normal.png");
            };

            /*
             Render gauge
             */
            event.getMatrixStack().pushPose();
            RenderSystem.enableBlend();
            RenderSystem.defaultBlendFunc();
            RenderSystem.setShaderTexture(0, gaugeTexture);
            RenderSystem.setShaderColor(1.0F, 1.0F, 1.0F, 1.0F);

            // Render frame
            GuiComponent.blit(event.getMatrixStack(), (scaleX / 2) + 94 + CCS.tempGaugeX(), scaleY - 19 + CCS.tempGaugeY(), 0, 0, 25, 16, 25, 16);

            RenderSystem.disableBlend();

            // Sets the text bobbing offset (or none if disabled)
            int bob = temp > max || temp < min ? (player.tickCount % 2 == 0 && bobbing ? 16 : 15) : 15;

            // Render text
            int blendedTemp = (int) CSMath.blend(PREV_BLENDED_TEMP,
                                                 BLENDED_TEMP, Minecraft.getInstance().getFrameTime(), 0, 1);
            Minecraft.getInstance().font.draw(event.getMatrixStack(), "" + (blendedTemp + CCS.tempOffset()) + "",
                    (scaleX / 2f) + 107 + (Integer.toString(blendedTemp + CCS.tempOffset()).length() * -3) + CCS.tempGaugeX(), scaleY - bob + CCS.tempGaugeY(), color);
            event.getMatrixStack().popPose();
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

                double realTemp = CSMath.convertUnits(temp.get(Temperature.Types.WORLD), Temperature.Units.MC, celsius ? Temperature.Units.C : Temperature.Units.F, true);
                PREV_BLENDED_TEMP = BLENDED_TEMP;

                BLENDED_TEMP = BLENDED_TEMP + (realTemp - BLENDED_TEMP) / 10.0;
            });
        }
    }

    static int getSeverity(double temp, double min, double max)
    {
        double mid = (min + max) / 2;

        return
          (temp > max)
            ? 4
        : (temp > mid + ((max - mid) * 0.75))
            ? 3
        : (temp > mid + ((max - mid) * 0.5))
            ? 2
        : (temp > mid + ((max - mid) * 0.25))
            ? 1
        : (temp >= mid - ((mid - min) * 0.25))
            ? 0
        : (temp >= mid - ((mid - min) * 0.5))
            ? -1
        : (temp >= mid - ((mid - min) * 0.75))
            ? -2
        : (temp >= min)
            ? -3
        : -4;
    }
}
