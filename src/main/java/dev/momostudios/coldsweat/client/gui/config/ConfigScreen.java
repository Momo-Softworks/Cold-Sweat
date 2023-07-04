package dev.momostudios.coldsweat.client.gui.config;

import dev.momostudios.coldsweat.client.gui.config.pages.ConfigPageDifficulty;
import dev.momostudios.coldsweat.client.gui.config.pages.ConfigPageOne;
import dev.momostudios.coldsweat.client.gui.config.pages.ConfigPageTwo;
import dev.momostudios.coldsweat.core.network.message.SyncConfigSettingsMessage;
import dev.momostudios.coldsweat.config.ConfigSettings;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.screens.Screen;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.client.event.ScreenEvent;
import net.minecraftforge.eventbus.api.SubscribeEvent;
import net.minecraftforge.fml.common.Mod;
import dev.momostudios.coldsweat.core.network.ColdSweatPacketHandler;

import java.text.DecimalFormat;
import java.util.Arrays;
import java.util.List;
import java.util.function.Supplier;

@Mod.EventBusSubscriber(value = Dist.CLIENT)
public class ConfigScreen
{
    public static final int TITLE_HEIGHT = 16;
    public static final int BOTTOM_BUTTON_HEIGHT_OFFSET = 26;
    public static final int OPTION_SIZE = 25;
    public static final int BOTTOM_BUTTON_WIDTH = 150;

    public static Minecraft MC = Minecraft.getInstance();

    public static DecimalFormat TWO_PLACES = new DecimalFormat("#.##");

    public static boolean IS_MOUSE_DOWN = false;
    public static int MOUSE_X = 0;
    public static int MOUSE_Y = 0;

    static List<Class<? extends AbstractConfigPage>> PAGES = Arrays.asList(ConfigPageOne.class, ConfigPageTwo.class);
    public static int FIRST_PAGE = 0;
    public static int LAST_PAGE = PAGES.size() - 1;

    public static final Supplier<Integer> SHIFT_AMOUNT = () -> Screen.hasShiftDown() && Screen.hasControlDown() ? 100 : Screen.hasShiftDown() ? 10 : 1;

    public static Screen getPage(int index, Screen parentScreen)
    {
        index = Math.max(FIRST_PAGE, Math.min(LAST_PAGE, index));
        try
        {
            return PAGES.get(index).getConstructor(Screen.class).newInstance(parentScreen);
        }
        catch (Exception e)
        {
            e.printStackTrace();
            return null;
        }
    }

    public static void saveConfig()
    {
        if (Minecraft.getInstance().player != null)
        {
            if (Minecraft.getInstance().player.getPermissionLevel() >= 2)
            {
                if (!MC.isLocalServer())
                    ColdSweatPacketHandler.INSTANCE.sendToServer(new SyncConfigSettingsMessage());
                else
                    ConfigSettings.saveValues();
            }
        }
        else
            ConfigSettings.saveValues();
    }

    @SubscribeEvent
    public static void onClicked(ScreenEvent.MouseClickedEvent event)
    {
        if (event.getButton() == 0 && Minecraft.getInstance().screen instanceof ConfigPageDifficulty)
            IS_MOUSE_DOWN = true;
    }

    @SubscribeEvent
    public static void onReleased(ScreenEvent.MouseReleasedEvent event)
    {
        if (event.getButton() == 0 && Minecraft.getInstance().screen instanceof ConfigPageDifficulty)
            IS_MOUSE_DOWN = false;
    }
}
