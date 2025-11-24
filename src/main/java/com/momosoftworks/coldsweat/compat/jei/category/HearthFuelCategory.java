package com.momosoftworks.coldsweat.compat.jei.category;

import com.momosoftworks.coldsweat.client.gui.AbstractHearthScreen;
import com.momosoftworks.coldsweat.common.blockentity.HearthBlockEntity;
import com.momosoftworks.coldsweat.compat.jei.JeiPlugin;
import com.momosoftworks.coldsweat.util.registries.ModItems;
import mezz.jei.api.helpers.IGuiHelper;
import net.minecraft.util.ResourceLocation;
import net.minecraft.util.text.IFormattableTextComponent;
import net.minecraft.util.text.TranslationTextComponent;

public class HearthFuelCategory extends ColdSweatFuelCategory
{
    public HearthFuelCategory(IGuiHelper guiHelper)
    {   super(guiHelper, ModItems.HEARTH.getDefaultInstance());
    }

    @Override
    public IFormattableTextComponent getTitleComponent()
    {   return new TranslationTextComponent("jei.cold_sweat.category.hearth_fuel");
    }

    @Override
    protected HearthBlockEntity getDummyBlockEntity()
    {   return new HearthBlockEntity();
    }

    @Override
    protected ResourceLocation getFilledGaugeTexture(JeiPlugin.FuelRecipe recipe)
    {
        if (recipe.fuelData().fuel() < 0)
        {   return AbstractHearthScreen.COLD_FUEL_GAUGE;
        }
        else return AbstractHearthScreen.HOT_FUEL_GAUGE;
    }

    @Override
    protected ResourceLocation getEmptyGaugeTexture(JeiPlugin.FuelRecipe recipe)
    {
        if (recipe.fuelData().fuel() < 0)
        {   return AbstractHearthScreen.COLD_FUEL_GAUGE_EMPTY;
        }
        else return AbstractHearthScreen.HOT_FUEL_GAUGE_EMPTY;
    }

    @Override
    public ResourceLocation getUid()
    {   return JeiPlugin.HEARTH_RECIPE_CATEGORY;
    }

    @Override
    public Class<JeiPlugin.FuelRecipe> getRecipeClass()
    {   return JeiPlugin.FuelRecipe.class;
    }
}
