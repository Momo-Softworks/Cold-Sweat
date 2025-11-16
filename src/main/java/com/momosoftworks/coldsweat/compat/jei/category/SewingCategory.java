package com.momosoftworks.coldsweat.compat.jei.category;

import com.momosoftworks.coldsweat.common.capability.handler.ItemInsulationManager;
import com.momosoftworks.coldsweat.compat.jei.JeiPlugin;
import com.momosoftworks.coldsweat.core.init.ModItemComponents;
import com.momosoftworks.coldsweat.core.init.ModItems;
import mezz.jei.api.gui.builder.IRecipeLayoutBuilder;
import mezz.jei.api.gui.drawable.IDrawable;
import mezz.jei.api.gui.widgets.IRecipeExtrasBuilder;
import mezz.jei.api.helpers.IGuiHelper;
import mezz.jei.api.recipe.IFocusGroup;
import mezz.jei.api.recipe.RecipeType;
import mezz.jei.api.recipe.category.IRecipeCategory;
import net.minecraft.network.chat.Component;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.Items;

import java.util.List;

public class SewingCategory implements IRecipeCategory<JeiPlugin.SewingRecipe>
{
    private final IDrawable icon;
    private static final ItemStack INSULATABLE_ITEM = Items.IRON_CHESTPLATE.getDefaultInstance();

    public SewingCategory(IGuiHelper guiHelper)
    {   this.icon = guiHelper.createDrawableItemStack(ModItems.SEWING_TABLE.toStack());
    }

    @Override
    public RecipeType getRecipeType()
    {   return JeiPlugin.SEWING_RECIPE_TYPE;
    }

    @Override
    public Component getTitle()
    {   return Component.translatable("jei.cold_sweat.category.sewing");
    }

    @Override
    public IDrawable getIcon()
    {   return this.icon;
    }

    @Override
    public int getWidth()
    {   return 130;
    }

    @Override
    public int getHeight()
    {   return 34;
    }

    @Override
    public void createRecipeExtras(IRecipeExtrasBuilder builder, JeiPlugin.SewingRecipe recipe, IFocusGroup focuses)
    {
        builder.addRecipePlusSign()
                .setPosition(31, 12);

        builder.addRecipeArrow()
                .setPosition(76, 10);
    }

    @Override
    public void setRecipe(IRecipeLayoutBuilder builder, JeiPlugin.SewingRecipe recipe, IFocusGroup focuses)
    {
        builder.addInputSlot(8, 10)
                .setStandardSlotBackground()
                .addItemStacks(List.of(recipe.input()));

        builder.addInputSlot(53, 10)
                .setStandardSlotBackground()
                .addItemStacks(List.of(INSULATABLE_ITEM));

        ItemStack insulatedItem = INSULATABLE_ITEM.copy();
        ItemInsulationManager.getInsulationCap(insulatedItem).ifPresent(cap ->
        {
            insulatedItem.set(ModItemComponents.ARMOR_INSULATION, cap.addInsulationItem(recipe.input()));
        });

        builder.addOutputSlot(105, 10)
                .setStandardSlotBackground()
                .addItemStacks(List.of(insulatedItem));
    }
}
