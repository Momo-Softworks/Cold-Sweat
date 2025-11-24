package com.momosoftworks.coldsweat.compat.jei.category;

import com.mojang.blaze3d.matrix.MatrixStack;
import com.momosoftworks.coldsweat.common.capability.handler.ItemInsulationManager;
import com.momosoftworks.coldsweat.compat.jei.JeiPlugin;
import com.momosoftworks.coldsweat.compat.jei.drawable.ItemDrawable;
import com.momosoftworks.coldsweat.util.registries.ModItems;
import mezz.jei.api.constants.VanillaTypes;
import mezz.jei.api.gui.IRecipeLayout;
import mezz.jei.api.gui.drawable.IDrawable;
import mezz.jei.api.gui.ingredient.IGuiItemStackGroup;
import mezz.jei.api.helpers.IGuiHelper;
import mezz.jei.api.ingredients.IIngredients;
import mezz.jei.api.recipe.category.IRecipeCategory;
import mezz.jei.gui.elements.DrawableResource;
import net.minecraft.item.ItemStack;
import net.minecraft.item.Items;
import net.minecraft.util.ResourceLocation;
import net.minecraft.util.text.TranslationTextComponent;

import java.util.Arrays;

public class SewingCategory implements IRecipeCategory<JeiPlugin.SewingRecipe>
{
    private static final ItemStack INSULATABLE_ITEM = Items.IRON_CHESTPLATE.getDefaultInstance();

    private final IDrawable icon;
    private final IDrawable background;
    protected final IGuiHelper guiHelper;

    public SewingCategory(IGuiHelper guiHelper)
    {
        int width = 130;
        int height = 34;
        this.icon = new ItemDrawable(ModItems.SEWING_TABLE.getDefaultInstance());
        this.background = guiHelper.createBlankDrawable(width, height);
        this.guiHelper = guiHelper;
    }

    @Override
    public String getTitle()
    {   return new TranslationTextComponent("jei.cold_sweat.category.sewing").getString();
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
    public void draw(JeiPlugin.SewingRecipe recipe, MatrixStack poseStack, double mouseX, double mouseY)
    {
        // Draw recipe components
        DrawableResource plusSign = new DrawableResource(new ResourceLocation("jei", "textures/gui/recipe_plus_sign.png"), 0, 0, 13, 13, 0, 0, 0, 0, 13, 13);
        plusSign.draw(poseStack, 31, 12);
        DrawableResource arrow = new DrawableResource(new ResourceLocation("jei", "textures/gui/recipe_arrow.png"), 0, 0, 24, 17, 0, 0, 0, 0, 24, 17);
        arrow.draw(poseStack, 76, 10);
        guiHelper.getSlotDrawable().draw(poseStack, 8, 10);
        guiHelper.getSlotDrawable().draw(poseStack, 53, 10);
        guiHelper.getSlotDrawable().draw(poseStack, 105, 10);
    }

    @Override
    public ResourceLocation getUid()
    {   return JeiPlugin.SEWING_RECIPE_CATEGORY;
    }

    @Override
    public Class<JeiPlugin.SewingRecipe> getRecipeClass()
    {   return JeiPlugin.SewingRecipe.class;
    }

    @Override
    public void setRecipe(IRecipeLayout builder, JeiPlugin.SewingRecipe recipe, IIngredients ingredients)
    {
        IGuiItemStackGroup guiItemStacks = builder.getItemStacks();
        guiItemStacks.init(0, true, 8, 10);
        guiItemStacks.init(1, true, 53, 10);
        guiItemStacks.init(2, false, 105, 10);

        guiItemStacks.set(0, Arrays.asList(recipe.input()));
        guiItemStacks.set(1, Arrays.asList(INSULATABLE_ITEM));
        guiItemStacks.set(2, Arrays.asList(getInsulatedItem(recipe.input())));
    }

    @Override
    public void setIngredients(JeiPlugin.SewingRecipe recipe, IIngredients ingredients)
    {   ingredients.setInputs(VanillaTypes.ITEM, Arrays.asList(recipe.input(), INSULATABLE_ITEM));
        ingredients.setOutput(VanillaTypes.ITEM, getInsulatedItem(recipe.input()));
    }

    private static ItemStack getInsulatedItem(ItemStack input)
    {
        ItemStack insulatedItem = INSULATABLE_ITEM.copy();
        ItemInsulationManager.getInsulationCap(insulatedItem).ifPresent(cap ->
        {
            cap.addInsulationItem(input);
            insulatedItem.getOrCreateTag().merge(cap.serializeNBT());
        });
        return insulatedItem;
    }
}
