package com.momosoftworks.coldsweat.compat.jei.category;

import com.momosoftworks.coldsweat.client.gui.AbstractHearthScreen;
import com.momosoftworks.coldsweat.common.blockentity.BoilerBlockEntity;
import com.momosoftworks.coldsweat.common.blockentity.HearthBlockEntity;
import com.momosoftworks.coldsweat.compat.jei.JeiPlugin;
import com.momosoftworks.coldsweat.util.registries.ModItems;
import mezz.jei.api.helpers.IGuiHelper;
import net.minecraft.util.ResourceLocation;
import net.minecraft.util.text.IFormattableTextComponent;
import net.minecraft.util.text.TranslationTextComponent;

public class BoilerFuelCategory extends ColdSweatFuelCategory
{
    public BoilerFuelCategory(IGuiHelper guiHelper)
    {   super(guiHelper, ModItems.BOILER.getDefaultInstance());
    }

    @Override
    public IFormattableTextComponent getTitleComponent()
    {   return new TranslationTextComponent("jei.cold_sweat.category.boiler_fuel");
    }

    @Override
    protected HearthBlockEntity getDummyBlockEntity()
    {   return new BoilerBlockEntity();
    }

    @Override
    protected ResourceLocation getFilledGaugeTexture(JeiPlugin.FuelRecipe recipe)
    {   return AbstractHearthScreen.HOT_FUEL_GAUGE;
    }

    @Override
    protected ResourceLocation getEmptyGaugeTexture(JeiPlugin.FuelRecipe recipe)
    {   return AbstractHearthScreen.HOT_FUEL_GAUGE_EMPTY;
    }

    @Override
    public ResourceLocation getUid()
    {   return JeiPlugin.BOILER_RECIPE_CATEGORY;
    }

    @Override
    public Class<JeiPlugin.FuelRecipe> getRecipeClass()
    {   return JeiPlugin.FuelRecipe.class;
    }
}
