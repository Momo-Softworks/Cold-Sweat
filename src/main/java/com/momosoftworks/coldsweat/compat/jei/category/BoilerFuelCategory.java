package com.momosoftworks.coldsweat.compat.jei.category;

import com.momosoftworks.coldsweat.client.gui.AbstractHearthScreen;
import com.momosoftworks.coldsweat.common.blockentity.BoilerBlockEntity;
import com.momosoftworks.coldsweat.common.blockentity.HearthBlockEntity;
import com.momosoftworks.coldsweat.compat.jei.JeiPlugin;
import com.momosoftworks.coldsweat.core.init.ModBlocks;
import com.momosoftworks.coldsweat.core.init.ModItems;
import mezz.jei.api.helpers.IGuiHelper;
import mezz.jei.api.recipe.RecipeType;
import net.minecraft.core.BlockPos;
import net.minecraft.network.chat.Component;
import net.minecraft.resources.ResourceLocation;

public class BoilerFuelCategory extends ColdSweatFuelCategory
{
    public BoilerFuelCategory(IGuiHelper guiHelper)
    {   super(guiHelper, ModItems.BOILER.toStack());
    }

    @Override
    public RecipeType<JeiPlugin.FuelRecipe> getRecipeType()
    {   return JeiPlugin.BOILER_RECIPE_TYPE;
    }

    @Override
    public Component getTitle()
    {   return Component.translatable("jei.cold_sweat.category.boiler_fuel");
    }

    @Override
    protected HearthBlockEntity getDummyBlockEntity()
    {   return new BoilerBlockEntity(BlockPos.ZERO, ModBlocks.BOILER.value().defaultBlockState());
    }

    @Override
    protected ResourceLocation getFilledGaugeTexture(JeiPlugin.FuelRecipe recipe)
    {   return AbstractHearthScreen.HOT_FUEL_GAUGE;
    }

    @Override
    protected ResourceLocation getEmptyGaugeTexture(JeiPlugin.FuelRecipe recipe)
    {   return AbstractHearthScreen.HOT_FUEL_GAUGE_EMPTY;
    }
}
