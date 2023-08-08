package dev.momostudios.coldsweat.api.event.client;

import com.mojang.blaze3d.matrix.MatrixStack;
import net.minecraft.client.gui.screen.Screen;
import net.minecraft.item.ItemStack;
import net.minecraftforge.eventbus.api.Event;

public class RenderTooltipEvent extends Event
{
    MatrixStack matrix;
    ItemStack stack;
    int mouseX;
    int mouseY;
    Screen screen;

    public RenderTooltipEvent(MatrixStack matrix, ItemStack stack, int mouseX, int mouseY, Screen screen)
    {
        this.matrix = matrix;
        this.stack = stack;
        this.mouseX = mouseX;
        this.mouseY = mouseY;
        this.screen = screen;
    }

    public ItemStack getItem()
    {   return stack;
    }

    public int getMouseX()
    {   return mouseX;
    }

    public int getMouseY()
    {   return mouseY;
    }

    public Screen getScreen()
    {   return screen;
    }

    public MatrixStack getMatrixStack()
    {   return matrix;
    }
}
