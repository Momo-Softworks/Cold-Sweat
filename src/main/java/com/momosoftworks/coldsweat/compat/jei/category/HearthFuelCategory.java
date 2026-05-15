package com.momosoftworks.coldsweat.compat.jei.category;

import com.momosoftworks.coldsweat.client.gui.AbstractHearthScreen;
import com.momosoftworks.coldsweat.common.blockentity.HearthBlockEntity;
import com.momosoftworks.coldsweat.compat.jei.JeiPlugin;
import com.momosoftworks.coldsweat.util.registries.ModBlocks;
import com.momosoftworks.coldsweat.util.registries.ModItems;
import mezz.jei.api.helpers.IGuiHelper;
import mezz.jei.api.recipe.RecipeType;
import net.minecraft.core.BlockPos;
import net.minecraft.network.chat.Component;
import net.minecraft.network.chat.TranslatableComponent;
import net.minecraft.resources.ResourceLocation;

public class HearthFuelCategory extends ColdSweatFuelCategory
{
    public HearthFuelCategory(IGuiHelper guiHelper)
    {   super(guiHelper, ModItems.HEARTH.getDefaultInstance());
    }

    @Override
    public RecipeType<JeiPlugin.FuelRecipe> getRecipeType()
    {   return JeiPlugin.HEARTH_RECIPE_TYPE;
    }

    @Override
    public Component getTitle()
    {   return new TranslatableComponent("jei.cold_sweat.category.hearth_fuel");
    }

    @Override
    protected HearthBlockEntity getDummyBlockEntity()
    {   return new HearthBlockEntity(BlockPos.ZERO, ModBlocks.HEARTH_BOTTOM.defaultBlockState());
    }

    @Override
    protected ResourceLocation getFilledGaugeTexture(JeiPlugin.FuelRecipe recipe)
    {
        if (recipe.getFuel() < 0)
        {   return AbstractHearthScreen.COLD_FUEL_GAUGE;
        }
        else return AbstractHearthScreen.HOT_FUEL_GAUGE;
    }

    @Override
    protected ResourceLocation getEmptyGaugeTexture(JeiPlugin.FuelRecipe recipe)
    {
        if (recipe.getFuel() < 0)
        {   return AbstractHearthScreen.COLD_FUEL_GAUGE_EMPTY;
        }
        else return AbstractHearthScreen.HOT_FUEL_GAUGE_EMPTY;
    }
}
