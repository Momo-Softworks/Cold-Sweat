package com.momosoftworks.coldsweat.compat.jei.drawable;

import com.mojang.blaze3d.matrix.MatrixStack;
import mezz.jei.api.gui.drawable.IDrawable;
import net.minecraft.client.Minecraft;
import net.minecraft.item.ItemStack;

public class ItemDrawable implements IDrawable
{
    ItemStack stack;

    public ItemDrawable(ItemStack stack)
    {   this.stack = stack;
    }

    @Override
    public int getWidth()
    {   return 16;
    }

    @Override
    public int getHeight()
    {   return 16;
    }

    @Override
    public void draw(MatrixStack matrixStack, int xOffset, int yOffset)
    {   Minecraft.getInstance().getItemRenderer().renderAndDecorateItem(this.stack, xOffset, yOffset);
    }
}
