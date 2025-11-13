package com.momosoftworks.coldsweat.compat.jei.category;

import com.mojang.blaze3d.vertex.PoseStack;
import com.momosoftworks.coldsweat.common.capability.handler.ItemInsulationManager;
import com.momosoftworks.coldsweat.compat.jei.JeiPlugin;
import com.momosoftworks.coldsweat.util.registries.ModItems;
import mezz.jei.api.gui.builder.IRecipeLayoutBuilder;
import mezz.jei.api.gui.drawable.IDrawable;
import mezz.jei.api.gui.ingredient.IRecipeSlotView;
import mezz.jei.api.gui.ingredient.IRecipeSlotsView;
import mezz.jei.api.helpers.IGuiHelper;
import mezz.jei.api.recipe.IFocusGroup;
import mezz.jei.api.recipe.RecipeIngredientRole;
import mezz.jei.api.recipe.RecipeType;
import mezz.jei.api.recipe.category.IRecipeCategory;
import mezz.jei.common.Internal;
import mezz.jei.common.gui.elements.DrawableResource;
import mezz.jei.library.gui.ingredients.RecipeSlot;
import net.minecraft.network.chat.Component;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.Items;

import java.util.List;

public class SewingCategory implements IRecipeCategory<JeiPlugin.SewingRecipe>
{
    private static final ItemStack INSULATABLE_ITEM = Items.IRON_CHESTPLATE.getDefaultInstance();

    private final IDrawable icon;
    private final IDrawable background;

    public SewingCategory(IGuiHelper guiHelper)
    {
        int width = 130;
        int height = 34;
        this.icon = guiHelper.createDrawableItemStack(ModItems.SEWING_TABLE.getDefaultInstance());
        this.background = guiHelper.createBlankDrawable(width, height);
    }

    @Override
    public RecipeType<JeiPlugin.SewingRecipe> getRecipeType()
    {   return JeiPlugin.SEWING_RECIPE_TYPE;
    }

    @Override
    public Component getTitle()
    {   return Component.translatable("jei.cold_sweat.category.sewing");
    }

    @Override
    public IDrawable getBackground()
    {   return this.background;
    }

    @Override
    public IDrawable getIcon()
    {   return this.icon;
    }

    @Override
    public void draw(JeiPlugin.SewingRecipe recipe, IRecipeSlotsView recipeSlotsView, PoseStack poseStack, double mouseX, double mouseY)
    {
        // Draw recipe components
        DrawableResource plusSign = new DrawableResource(ResourceLocation.fromNamespaceAndPath("jei", "textures/gui/recipe_plus_sign.png"), 0, 0, 13, 13, 0, 0, 0, 0, 13, 13);
        plusSign.draw(poseStack, 31, 12);
        DrawableResource arrow = new DrawableResource(ResourceLocation.fromNamespaceAndPath("jei", "textures/gui/recipe_arrow.png"), 0, 0, 24, 17, 0, 0, 0, 0, 24, 17);
        arrow.draw(poseStack, 76, 10);
        // Draw slots
        for (IRecipeSlotView slot : recipeSlotsView.getSlotViews())
        {
            if (slot instanceof RecipeSlot recipeSlot)
            {   recipeSlot.draw(poseStack);
            }
        }
    }

    @Override
    public void setRecipe(IRecipeLayoutBuilder builder, JeiPlugin.SewingRecipe recipe, IFocusGroup focuses)
    {
        builder.addSlot(RecipeIngredientRole.INPUT, 8, 10)
                .setBackground(Internal.getTextures().getSlotDrawable(), -1, -1)
                .addItemStacks(List.of(recipe.input()));

        builder.addSlot(RecipeIngredientRole.INPUT, 53, 10)
                .setBackground(Internal.getTextures().getSlotDrawable(), -1, -1)
                .addItemStacks(List.of(INSULATABLE_ITEM));

        ItemStack insulatedItem = INSULATABLE_ITEM.copy();
        ItemInsulationManager.getInsulationCap(insulatedItem).ifPresent(cap ->
        {
            cap.addInsulationItem(recipe.input());
            insulatedItem.getOrCreateTag().merge(cap.serializeNBT());
        });

        builder.addSlot(RecipeIngredientRole.OUTPUT, 105, 10)
                .setBackground(Internal.getTextures().getSlotDrawable(), -1, -1)
                .addItemStacks(List.of(insulatedItem));
    }
}
