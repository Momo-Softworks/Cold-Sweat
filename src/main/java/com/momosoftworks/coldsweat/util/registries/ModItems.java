package com.momosoftworks.coldsweat.util.registries;

import com.momosoftworks.coldsweat.ColdSweat;
import com.momosoftworks.coldsweat.common.item.FilledWaterskinItem;
import com.momosoftworks.coldsweat.common.item.ThermometerItem;
import com.momosoftworks.coldsweat.common.item.WaterskinItem;
import cpw.mods.fml.common.registry.GameRegistry;
import net.minecraft.creativetab.CreativeTabs;
import net.minecraft.item.Item;

public class ModItems
{
    public static Item WATERSKIN;
    public static Item FILLED_WATERSKIN;
    public static Item THERMOMETER;

    public static void init()
    {
        WATERSKIN = namedItem(new WaterskinItem(), "waterskin").setCreativeTab(ColdSweat.TAB_COLD_SWEAT).setMaxStackSize(16);
        FILLED_WATERSKIN = namedItem(new FilledWaterskinItem(), "filled_waterskin").setCreativeTab(ColdSweat.TAB_COLD_SWEAT).setMaxStackSize(1);

        THERMOMETER = namedItem(new ThermometerItem(), "thermometer").setCreativeTab(ColdSweat.TAB_COLD_SWEAT).setMaxStackSize(1);

        GameRegistry.registerItem(WATERSKIN, WATERSKIN.getUnlocalizedName());
        GameRegistry.registerItem(FILLED_WATERSKIN, FILLED_WATERSKIN.getUnlocalizedName());
        GameRegistry.registerItem(THERMOMETER, THERMOMETER.getUnlocalizedName());
    }

    private static Item namedItem(Item item, String name)
    {   return item.setUnlocalizedName(name).setTextureName(ColdSweat.MOD_ID + ":" + name);
    }
}
