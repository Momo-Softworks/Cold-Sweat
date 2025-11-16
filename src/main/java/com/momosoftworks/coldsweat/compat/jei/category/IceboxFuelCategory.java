package com.momosoftworks.coldsweat.compat.jei.category;

import com.momosoftworks.coldsweat.client.gui.AbstractHearthScreen;
import com.momosoftworks.coldsweat.common.blockentity.HearthBlockEntity;
import com.momosoftworks.coldsweat.common.blockentity.IceboxBlockEntity;
import com.momosoftworks.coldsweat.compat.jei.JeiPlugin;
import com.momosoftworks.coldsweat.core.init.ModBlocks;
import com.momosoftworks.coldsweat.core.init.ModItems;
import mezz.jei.api.helpers.IGuiHelper;
import mezz.jei.api.recipe.RecipeType;
import net.minecraft.core.BlockPos;
import net.minecraft.network.chat.Component;
import net.minecraft.resources.ResourceLocation;

public class IceboxFuelCategory extends ColdSweatFuelCategory
{
    public IceboxFuelCategory(IGuiHelper guiHelper)
    {   super(guiHelper, ModItems.ICEBOX.toStack());
    }

    @Override
    public RecipeType<JeiPlugin.FuelRecipe> getRecipeType()
    {   return JeiPlugin.ICEBOX_RECIPE_TYPE;
    }

    @Override
    public Component getTitle()
    {   return Component.translatable("jei.cold_sweat.category.icebox_fuel");
    }

    @Override
    protected HearthBlockEntity getDummyBlockEntity()
    {   return new IceboxBlockEntity(BlockPos.ZERO, ModBlocks.ICEBOX.value().defaultBlockState());
    }

    @Override
    protected ResourceLocation getFilledGaugeTexture(JeiPlugin.FuelRecipe recipe)
    {   return AbstractHearthScreen.COLD_FUEL_GAUGE;
    }

    @Override
    protected ResourceLocation getEmptyGaugeTexture(JeiPlugin.FuelRecipe recipe)
    {   return AbstractHearthScreen.COLD_FUEL_GAUGE_EMPTY;
    }
}
