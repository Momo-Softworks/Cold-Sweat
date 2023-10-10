package com.momosoftworks.coldsweat.common.item;

import com.momosoftworks.coldsweat.ColdSweat;
import com.momosoftworks.coldsweat.api.util.Temperature;
import com.momosoftworks.coldsweat.client.gui.Overlays;
import com.momosoftworks.coldsweat.config.ConfigSettings;
import net.minecraft.client.renderer.texture.IIconRegister;
import net.minecraft.item.Item;
import net.minecraft.item.ItemStack;
import net.minecraft.util.IIcon;

import java.util.Arrays;

public class ThermometerItem extends Item
{
    private final IIcon[] icons = new IIcon[9];

    @Override
    public void registerIcons(IIconRegister registry)
    {
        for (int i = 0; i < 9; i++)
        {   String index = String.valueOf(i);
            if (index.length() < 2)
            {   index = "0" + index;
            }
            IIcon icon = registry.registerIcon(ColdSweat.MOD_ID+":thermometer_" + index);
            if (itemIcon == null) itemIcon = icon;
            icons[i] = icon;
        }
        System.out.println(Arrays.toString(icons));
    }

    @Override
    public IIcon getIcon(ItemStack stack, int pass)
    {   double temp = Temperature.convertUnits(Overlays.getWorldTemp(), ConfigSettings.CELSIUS.get() ? Temperature.Units.C : Temperature.Units.F, Temperature.Units.MC, true);
        return icons[Overlays.getWorldSeverity(temp, ConfigSettings.MIN_TEMP.get(), ConfigSettings.MAX_TEMP.get(), Overlays.getMinOffset(), Overlays.getMaxOffset()) + 4];
    }
}
