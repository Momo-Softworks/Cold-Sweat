package com.momosoftworks.coldsweat.compat.jei.category;

import com.momosoftworks.coldsweat.common.capability.handler.ItemInsulationManager;
import com.momosoftworks.coldsweat.compat.jei.JeiPlugin;
import com.momosoftworks.coldsweat.core.init.ModItemComponents;
import com.momosoftworks.coldsweat.core.init.ModItems;
import mezz.jei.api.gui.builder.IRecipeLayoutBuilder;
import mezz.jei.api.gui.builder.IRecipeSlotBuilder;
import mezz.jei.api.gui.drawable.IDrawable;
import mezz.jei.api.gui.widgets.IRecipeExtrasBuilder;
import mezz.jei.api.helpers.IGuiHelper;
import mezz.jei.api.recipe.IFocusGroup;
import mezz.jei.api.recipe.RecipeIngredientRole;
import mezz.jei.api.recipe.RecipeType;
import mezz.jei.api.recipe.category.IRecipeCategory;
import mezz.jei.api.recipe.types.IRecipeType;
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
    public IRecipeType<JeiPlugin.SewingRecipe> getRecipeType()
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
        IRecipeSlotBuilder insulatorSlot = builder.addSlot(RecipeIngredientRole.INPUT, 8, 10)
            .setStandardSlotBackground()
            .addItemStacks(recipe.inputs());

        builder.addSlot(RecipeIngredientRole.INPUT, 53, 10)
            .setStandardSlotBackground()
            .addItemStacks(List.of(INSULATABLE_ITEM));

        List<ItemStack> insulationResults = recipe.inputs().stream().map(insulator ->
        {
            ItemStack armorItem = INSULATABLE_ITEM.copy();
            ItemInsulationManager.getInsulationCap(armorItem).ifPresent(cap ->
            {   armorItem.set(ModItemComponents.ARMOR_INSULATION, cap.addInsulationItem(insulator));
            });
            return armorItem;
        }).toList();

        IRecipeSlotBuilder outputSlot = builder.addSlot(RecipeIngredientRole.OUTPUT, 105, 10)
            .setStandardSlotBackground()
            .addItemStacks(insulationResults);

        builder.createFocusLink(insulatorSlot, outputSlot);
    }
}
