package com.momosoftworks.coldsweat.compat.jei.category;

import com.mojang.blaze3d.matrix.MatrixStack;
import com.mojang.blaze3d.systems.RenderSystem;
import com.momosoftworks.coldsweat.common.blockentity.HearthBlockEntity;
import com.momosoftworks.coldsweat.compat.jei.JeiPlugin;
import com.momosoftworks.coldsweat.compat.jei.drawable.ItemDrawable;
import com.momosoftworks.coldsweat.util.math.CSMath;
import mezz.jei.api.constants.VanillaTypes;
import mezz.jei.api.gui.IRecipeLayout;
import mezz.jei.api.gui.drawable.IDrawable;
import mezz.jei.api.gui.drawable.IDrawableStatic;
import mezz.jei.api.gui.ingredient.IGuiItemStackGroup;
import mezz.jei.api.helpers.IGuiHelper;
import mezz.jei.api.ingredients.IIngredients;
import mezz.jei.api.recipe.category.IRecipeCategory;
import mezz.jei.gui.elements.DrawableResource;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.FontRenderer;
import net.minecraft.item.ItemStack;
import net.minecraft.util.ResourceLocation;
import net.minecraft.util.StringUtils;
import net.minecraft.util.text.IFormattableTextComponent;
import net.minecraft.util.text.TranslationTextComponent;

import java.util.Arrays;

public abstract class ColdSweatFuelCategory implements IRecipeCategory<JeiPlugin.FuelRecipe>
{
    private final IDrawable icon;
    private final IDrawableStatic background;
    private final int width;
    private final int height;
    protected final IGuiHelper guiHelper;

    public ColdSweatFuelCategory(IGuiHelper guiHelper, ItemStack icon)
    {
        this.width = this.getMaxWidth();
        this.height = 34;
        this.icon = new ItemDrawable(icon);
        this.background = guiHelper.createBlankDrawable(width, height);
        this.guiHelper = guiHelper;
    }

    public abstract IFormattableTextComponent getTitleComponent();

    @Override
    public abstract ResourceLocation getUid();

    @Override
    public abstract Class<? extends JeiPlugin.FuelRecipe> getRecipeClass();

    protected abstract HearthBlockEntity getDummyBlockEntity();

    protected abstract ResourceLocation getEmptyGaugeTexture(JeiPlugin.FuelRecipe recipe);
    protected abstract ResourceLocation getFilledGaugeTexture(JeiPlugin.FuelRecipe recipe);

    @Override
    public String getTitle()
    {   return this.getTitleComponent().getString();
    }

    @Override
    public IDrawable getIcon()
    {   return this.icon;
    }

    protected int getMaxWidth()
    {
        // width of the recipe depends on the text, which is different in each language
        Minecraft minecraft = Minecraft.getInstance();
        FontRenderer fontRenderer = minecraft.font;
        HearthBlockEntity dummyBlockEntity = this.getDummyBlockEntity();
        IFormattableTextComponent maxSmeltCountText = createFillAmountText(dummyBlockEntity.getMaxFuel(), dummyBlockEntity);
        int maxStringWidth = fontRenderer.width(maxSmeltCountText.getString());
        int textPadding = 20;
        return 18 + textPadding + maxStringWidth;
    }

    @Override
    public IDrawableStatic getBackground()
    {   return this.background;
    }

    @Override
    public void setRecipe(IRecipeLayout builder, JeiPlugin.FuelRecipe recipe, IIngredients ingredients)
    {
        IGuiItemStackGroup guiItemStacks = builder.getItemStacks();
        guiItemStacks.init(0, true, 1, 17);
        guiItemStacks.set(ingredients);
    }

    @Override
    public void draw(JeiPlugin.FuelRecipe recipe, MatrixStack poseStack, double mouseX, double mouseY)
    {
        Minecraft minecraft = Minecraft.getInstance();
        FontRenderer font = minecraft.font;
        HearthBlockEntity dummyBlockEntity = this.getDummyBlockEntity();
        double fuel = Math.abs(recipe.fuelData().fuel());

        IFormattableTextComponent fillAmountText = createFillAmountText(fuel, dummyBlockEntity);
        int textWidth = font.width(fillAmountText.getString());
        // draw text centered
        int textX = this.width - 12 - textWidth;
        int textY = this.height - 20;
        font.draw(poseStack, fillAmountText, textX, textY, 0xFF808080);

        int gaugeHeight  = fuel <= 0 ? 0 : (int) Math.round(CSMath.blend(2, 14, fuel, 0, dummyBlockEntity.getMaxFuel()));
        int maxGaugeHeight = 14;
        DrawableResource fuelGauge = new DrawableResource(this.getFilledGaugeTexture(recipe), 0, 14-gaugeHeight, 14, gaugeHeight, 0, 0, 0, 0, 14, 14);
        DrawableResource emptyFuelGauge = new DrawableResource(this.getEmptyGaugeTexture(recipe), 0, 0, 14, 14, 0, 0, 0, 0, 14, 14);
        RenderSystem.enableBlend();
        RenderSystem.defaultBlendFunc();
        emptyFuelGauge.draw(poseStack, 2, 0);
        fuelGauge.draw(poseStack, 2, 0 + (maxGaugeHeight-gaugeHeight));
        guiHelper.getSlotDrawable().draw(poseStack, 1, 17);
    }

    @Override
    public void setIngredients(JeiPlugin.FuelRecipe recipe, IIngredients ingredients)
    {   ingredients.setInputs(VanillaTypes.ITEM, Arrays.asList(recipe.input()));
    }

    private static IFormattableTextComponent createFillAmountText(double fuelAmount, HearthBlockEntity dummyBlockEntity)
    {
        int fuelInterval = dummyBlockEntity.getFuelDrainInterval();
        int durationTicks = (int) Math.round(fuelAmount * fuelInterval);
        return new TranslationTextComponent("jei.cold_sweat.fuel_duration", StringUtils.formatTickDuration(durationTicks));
    }
}
