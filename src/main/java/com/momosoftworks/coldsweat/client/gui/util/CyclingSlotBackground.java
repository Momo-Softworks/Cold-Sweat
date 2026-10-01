package com.momosoftworks.coldsweat.client.gui.util;

import net.minecraft.client.renderer.RenderPipelines;
import net.minecraft.util.ARGB;
import net.minecraft.client.gui.GuiGraphicsExtractor;
import net.minecraft.resources.Identifier;
import net.minecraft.world.inventory.AbstractContainerMenu;
import net.minecraft.world.inventory.Slot;

import java.util.List;

public class CyclingSlotBackground
{
    private static final int ICON_CHANGE_TICK_RATE = 30;
    private static final int ICON_SIZE = 16;
    private static final int ICON_TRANSITION_TICK_DURATION = 4;
    private final int slotIndex;
    private final List<Identifier> icons;
    private int tick;
    private int iconIndex;

    public CyclingSlotBackground(int slotIndex, List<Identifier> icons)
    {   this.slotIndex = slotIndex;
        this.icons = icons;
    }

    public void tick()
    {
        if (!this.icons.isEmpty() && ++this.tick % ICON_CHANGE_TICK_RATE == 0)
        {   this.iconIndex = (this.iconIndex + 1) % this.icons.size();
        }
    }

    public void render(AbstractContainerMenu menu, GuiGraphicsExtractor graphics, float partialTick, int guiLeft, int guiTop)
    {
        Slot slot = menu.getSlot(this.slotIndex);
        if (!this.icons.isEmpty() && !slot.hasItem())
        {
            boolean shouldTransition = this.icons.size() > 1 && this.tick >= ICON_CHANGE_TICK_RATE;
            float transparency = shouldTransition ? this.getIconTransitionTransparency(partialTick) : 1.0F;
            if (transparency < 1.0F)
            {   int iconIndex = Math.floorMod(this.iconIndex - 1, this.icons.size());
                this.renderIcon(slot, this.icons.get(iconIndex), 1.0F - transparency, graphics, guiLeft, guiTop);
            }

            this.renderIcon(slot, this.icons.get(this.iconIndex), transparency, graphics, guiLeft, guiTop);
        }
    }

    private void renderIcon(Slot slot, Identifier icon, float alpha, GuiGraphicsExtractor graphics, int x, int y)
    {
        graphics.blit(RenderPipelines.GUI_TEXTURED, icon, x + slot.x, y + slot.y, 0, 0, ICON_SIZE, ICON_SIZE, ICON_SIZE, ICON_SIZE, ARGB.white(alpha));
    }

    private float getIconTransitionTransparency(float partialTick)
    {
        float $$1 = (float)(this.tick % ICON_CHANGE_TICK_RATE) + partialTick;
        return Math.min($$1, ICON_TRANSITION_TICK_DURATION) / ICON_TRANSITION_TICK_DURATION;
    }
}