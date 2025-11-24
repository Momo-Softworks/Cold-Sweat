package com.momosoftworks.coldsweat.compat.jei.category;

import com.momosoftworks.coldsweat.client.gui.AbstractHearthScreen;
import com.momosoftworks.coldsweat.common.blockentity.HearthBlockEntity;
import com.momosoftworks.coldsweat.common.blockentity.IceboxBlockEntity;
import com.momosoftworks.coldsweat.compat.jei.JeiPlugin;
import com.momosoftworks.coldsweat.util.registries.ModItems;
import mezz.jei.api.helpers.IGuiHelper;
import net.minecraft.util.ResourceLocation;
import net.minecraft.util.text.IFormattableTextComponent;
import net.minecraft.util.text.TranslationTextComponent;

public class IceboxFuelCategory extends ColdSweatFuelCategory
{
    public IceboxFuelCategory(IGuiHelper guiHelper)
    {   super(guiHelper, ModItems.ICEBOX.getDefaultInstance());
    }

    @Override
    public IFormattableTextComponent getTitleComponent()
    {   return new TranslationTextComponent("jei.cold_sweat.category.icebox_fuel");
    }

    @Override
    protected HearthBlockEntity getDummyBlockEntity()
    {   return new IceboxBlockEntity();
    }

    @Override
    protected ResourceLocation getFilledGaugeTexture(JeiPlugin.FuelRecipe recipe)
    {   return AbstractHearthScreen.COLD_FUEL_GAUGE;
    }

    @Override
    protected ResourceLocation getEmptyGaugeTexture(JeiPlugin.FuelRecipe recipe)
    {   return AbstractHearthScreen.COLD_FUEL_GAUGE_EMPTY;
    }

    @Override
    public ResourceLocation getUid()
    {   return JeiPlugin.ICEBOX_RECIPE_CATEGORY;
    }

    @Override
    public Class<JeiPlugin.FuelRecipe> getRecipeClass()
    {   return JeiPlugin.FuelRecipe.class;
    }
}
