package com.momosoftworks.coldsweat.compat.jei.category;

import com.momosoftworks.coldsweat.common.blockentity.HearthBlockEntity;
import com.momosoftworks.coldsweat.compat.jei.JeiPlugin;
import com.momosoftworks.coldsweat.util.math.CSMath;
import mezz.jei.api.gui.builder.IRecipeLayoutBuilder;
import mezz.jei.api.gui.drawable.IDrawable;
import mezz.jei.api.gui.placement.HorizontalAlignment;
import mezz.jei.api.gui.placement.VerticalAlignment;
import mezz.jei.api.gui.widgets.IRecipeExtrasBuilder;
import mezz.jei.api.helpers.IGuiHelper;
import mezz.jei.api.recipe.IFocusGroup;
import mezz.jei.api.recipe.RecipeType;
import mezz.jei.api.recipe.category.IRecipeCategory;
import mezz.jei.common.gui.elements.DrawableResource;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.Font;
import net.minecraft.network.chat.Component;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.util.StringUtil;
import net.minecraft.world.item.ItemStack;

import java.util.List;

public abstract class ColdSweatFuelCategory implements IRecipeCategory<JeiPlugin.FuelRecipe>
{
    private final IDrawable icon;
    private final int width;
    private final int height;

    public ColdSweatFuelCategory(IGuiHelper guiHelper, ItemStack icon)
    {
        this.width = this.getMaxWidth();
        this.icon = guiHelper.createDrawableItemStack(icon);
        this.height = 34;
    }

    @Override
    public abstract RecipeType<JeiPlugin.FuelRecipe> getRecipeType();

    @Override
    public abstract Component getTitle();

    protected abstract HearthBlockEntity getDummyBlockEntity();

    protected abstract ResourceLocation getEmptyGaugeTexture(JeiPlugin.FuelRecipe recipe);
    protected abstract ResourceLocation getFilledGaugeTexture(JeiPlugin.FuelRecipe recipe);

    @Override
    public int getWidth()
    {   return this.width;
    }

    @Override
    public int getHeight()
    {   return this.height;
    }

    @Override
    public IDrawable getIcon()
    {   return this.icon;
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
    public void setRecipe(IRecipeLayoutBuilder builder, JeiPlugin.FuelRecipe recipe, IFocusGroup focuses)
    {
        builder.addInputSlot(1, 17)
                .setStandardSlotBackground()
                .addItemStacks(List.of(recipe.input()));
    }

    @Override
    public void createRecipeExtras(IRecipeExtrasBuilder builder, JeiPlugin.FuelRecipe recipe, IFocusGroup focuses)
    {
        HearthBlockEntity dummyBlockEntity = this.getDummyBlockEntity();
        double fuel = Math.abs(recipe.getFuel());

        Component fillAmountText = createFillAmountText(fuel, dummyBlockEntity);
        builder.addText(fillAmountText, getWidth() - 20, getHeight())
                .setPosition(20, 0)
                .setTextAlignment(HorizontalAlignment.CENTER)
                .setTextAlignment(VerticalAlignment.CENTER)
                .setColor(0xFF808080);

        int gaugeHeight  = fuel <= 0 ? 0 : (int) Math.round(CSMath.blend(2, 14, fuel, 0, dummyBlockEntity.getMaxFuel()));
        int maxGaugeHeight = 14;
        DrawableResource fuelGauge = new DrawableResource(this.getFilledGaugeTexture(recipe), 0, 14-gaugeHeight, 14, gaugeHeight, 0, 0, 0, 0, 14, 14);
        DrawableResource emptyFuelGauge = new DrawableResource(this.getEmptyGaugeTexture(recipe), 0, 0, 14, 14, 0, 0, 0, 0, 14, 14);
        builder.addDrawable(emptyFuelGauge, 2, 0);
        builder.addDrawable(fuelGauge, 2, 0 + (maxGaugeHeight-gaugeHeight));
    }

    private static Component createFillAmountText(double fuelAmount, HearthBlockEntity dummyBlockEntity)
    {
        int fuelInterval = dummyBlockEntity.getFuelDrainInterval();
        int durationTicks = (int) Math.round(fuelAmount * fuelInterval);
        return Component.translatable("jei.cold_sweat.fuel_duration", StringUtil.formatTickDuration(durationTicks, 20));
    }
}
