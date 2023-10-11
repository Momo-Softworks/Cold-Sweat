package com.momosoftworks.coldsweat.util.registries.crafting;

import com.momosoftworks.coldsweat.util.registries.ModItems;
import cpw.mods.fml.common.registry.GameRegistry;
import net.minecraft.init.Items;
import net.minecraft.item.ItemStack;

public class ModRecipes
{
    public static void registerRecipes()
    {   addRecipes();
    }

    private static void addRecipes()
    {
        // Waterskin
        GameRegistry.addRecipe(new ItemStack(ModItems.WATERSKIN, 2),
        "  S",
        " L ",
        "L  ",
        'S', Items.string, 'L', Items.leather);

        // Thermometer
        GameRegistry.addRecipe(new ItemStack(ModItems.THERMOMETER, 1),
        "GRG",
        " R ",
        " R ",
        'G', Items.gold_ingot, 'R', Items.redstone);
    }
}
