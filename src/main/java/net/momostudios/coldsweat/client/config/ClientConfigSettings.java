package net.momostudios.coldsweat.client.config;

import net.minecraft.client.gui.screen.MainMenuScreen;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.client.event.GuiOpenEvent;
import net.minecraftforge.event.world.WorldEvent;
import net.minecraftforge.eventbus.api.SubscribeEvent;
import net.minecraftforge.fml.common.Mod;
import net.momostudios.coldsweat.config.ColdSweatConfig;

@Mod.EventBusSubscriber
public class ClientConfigSettings
{
    static final ColdSweatConfig config = ColdSweatConfig.getInstance();
    private static final ClientConfigSettings INSTANCE = new ClientConfigSettings();

    public boolean celsius;
    public int tempOffset;
    public int steveHeadX;
    public int steveHeadY;
    public int tempGaugeX;
    public int tempGaugeY;
    public boolean iconBobbing;
    public boolean customHotbar;

    private ClientConfigSettings()
    {
        celsius = config.celsius();
        tempOffset = config.tempOffset();
        steveHeadX = config.steveHeadX();
        steveHeadY = config.steveHeadY();
        tempGaugeX = config.tempGaugeX();
        tempGaugeY = config.tempGaugeY();
        iconBobbing = config.iconBobbing();
        customHotbar = config.customHotbar();
    }

    public static ClientConfigSettings getInstance()
    {
        return INSTANCE;
    }

    @SubscribeEvent
    public static void titleScreenEvent(WorldEvent.Unload event)
    {
        config.setCelsius(getInstance().celsius);
        config.setTempOffset(getInstance().tempOffset);
        config.setSteveHeadX(getInstance().steveHeadX);
        config.setSteveHeadY(getInstance().steveHeadY);
        config.setTempGaugeX(getInstance().tempGaugeX);
        config.setTempGaugeY(getInstance().tempGaugeY);
        config.setIconBobbing(getInstance().iconBobbing);
        config.setCustomHotbar(getInstance().customHotbar);
        config.save();
    }
}
