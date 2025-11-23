package com.momosoftworks.coldsweat.compat.jei.category;

import com.mojang.blaze3d.vertex.PoseStack;
import com.momosoftworks.coldsweat.common.blockentity.HearthBlockEntity;
import com.momosoftworks.coldsweat.compat.jei.JeiPlugin;
import com.momosoftworks.coldsweat.util.math.CSMath;
import mezz.jei.api.gui.builder.IRecipeLayoutBuilder;
import mezz.jei.api.gui.drawable.IDrawable;
import mezz.jei.api.gui.drawable.IDrawableStatic;
import mezz.jei.api.gui.ingredient.IRecipeSlotView;
import mezz.jei.api.gui.ingredient.IRecipeSlotsView;
import mezz.jei.api.helpers.IGuiHelper;
import mezz.jei.api.recipe.IFocusGroup;
import mezz.jei.api.recipe.RecipeIngredientRole;
import mezz.jei.api.recipe.RecipeType;
import mezz.jei.api.recipe.category.IRecipeCategory;
import mezz.jei.common.Internal;
import mezz.jei.common.gui.elements.DrawableResource;
import mezz.jei.common.gui.ingredients.RecipeSlot;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.Font;
import net.minecraft.network.chat.Component;
import net.minecraft.network.chat.TranslatableComponent;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.util.StringUtil;
import net.minecraft.world.item.ItemStack;

import java.util.List;

public abstract class ColdSweatFuelCategory implements IRecipeCategory<JeiPlugin.FuelRecipe>
{
    private final IDrawable icon;
    private final IDrawableStatic background;
    private final int width;
    private final int height;

    public ColdSweatFuelCategory(IGuiHelper guiHelper, ItemStack icon)
    {
        this.width = this.getMaxWidth();
        this.height = 34;
        this.icon = guiHelper.createDrawableItemStack(icon);
        this.background = guiHelper.createBlankDrawable(width, height);
    }

    @Override
    public abstract RecipeType<JeiPlugin.FuelRecipe> getRecipeType();

    @Override
    public abstract Component getTitle();

    protected abstract HearthBlockEntity getDummyBlockEntity();

    protected abstract ResourceLocation getEmptyGaugeTexture(JeiPlugin.FuelRecipe recipe);
    protected abstract ResourceLocation getFilledGaugeTexture(JeiPlugin.FuelRecipe recipe);

    @Override
    public IDrawable getIcon()
    {   return this.icon;
    }

    @Override
    public ResourceLocation getUid()
    {   return this.getRecipeType().getUid();
    }

    @Override
    public Class<? extends JeiPlugin.FuelRecipe> getRecipeClass()
    {   return this.getRecipeType().getRecipeClass();
    }

    protected int getMaxWidth()
    {
        // width of the recipe depends on the text, which is different in each language
        Minecraft minecraft = Minecraft.getInstance();
        Font fontRenderer = minecraft.font;
        HearthBlockEntity dummyBlockEntity = this.getDummyBlockEntity();
        Component maxSmeltCountText = createFillAmountText(dummyBlockEntity.getMaxFuel(), dummyBlockEntity);
        int maxStringWidth = fontRenderer.width(maxSmeltCountText.getString());
        int textPadding = 20;
        return 18 + textPadding + maxStringWidth;
    }

    @Override
    public IDrawableStatic getBackground()
    {   return this.background;
    }

    @Override
    public void setRecipe(IRecipeLayoutBuilder builder, JeiPlugin.FuelRecipe recipe, IFocusGroup focuses)
    {
        builder.addSlot(RecipeIngredientRole.INPUT, 1, 17)
                .setBackground(Internal.getTextures().getSlotDrawable(), -1, -1)
                .addItemStacks(List.of(recipe.input()));
    }

    @Override
    public void draw(JeiPlugin.FuelRecipe recipe, IRecipeSlotsView recipeSlotsView, PoseStack poseStack, double mouseX, double mouseY)
    {
        Minecraft minecraft = Minecraft.getInstance();
        Font font = minecraft.font;
        HearthBlockEntity dummyBlockEntity = this.getDummyBlockEntity();
        double fuel = Math.abs(recipe.fuelData().fuel());

        Component fillAmountText = createFillAmountText(fuel, dummyBlockEntity);
        int textWidth = font.width(fillAmountText.getString());
        // draw text centered
        int textX = this.width - 12 - textWidth;
        int textY = this.height - 20;
        font.draw(poseStack, fillAmountText, textX, textY, 0xFF808080);

        int gaugeHeight  = fuel <= 0 ? 0 : (int) Math.round(CSMath.blend(2, 14, fuel, 0, dummyBlockEntity.getMaxFuel()));
        int maxGaugeHeight = 14;
        DrawableResource fuelGauge = new DrawableResource(this.getFilledGaugeTexture(recipe), 0, 14-gaugeHeight, 14, gaugeHeight, 0, 0, 0, 0, 14, 14);
        DrawableResource emptyFuelGauge = new DrawableResource(this.getEmptyGaugeTexture(recipe), 0, 0, 14, 14, 0, 0, 0, 0, 14, 14);
        emptyFuelGauge.draw(poseStack, 2, 0);
        fuelGauge.draw(poseStack, 2, 0 + (maxGaugeHeight-gaugeHeight));
        // Draw slots
        for (IRecipeSlotView slot : recipeSlotsView.getSlotViews())
        {
            if (slot instanceof RecipeSlot recipeSlot)
            {   recipeSlot.draw(poseStack);
            }
        }
    }

    private static Component createFillAmountText(double fuelAmount, HearthBlockEntity dummyBlockEntity)
    {
        int fuelInterval = dummyBlockEntity.getFuelDrainInterval();
        int durationTicks = (int) Math.round(fuelAmount * fuelInterval);
        return new TranslatableComponent("jei.cold_sweat.fuel_duration", StringUtil.formatTickDuration(durationTicks));
    }
}
