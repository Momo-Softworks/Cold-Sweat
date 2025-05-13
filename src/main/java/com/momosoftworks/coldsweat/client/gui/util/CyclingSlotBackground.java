package com.momosoftworks.coldsweat.client.gui.util;

import com.mojang.blaze3d.systems.RenderSystem;
import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.inventory.AbstractContainerMenu;
import net.minecraft.world.inventory.Slot;

import java.util.List;

public class CyclingSlotBackground
{
    private static final int ICON_CHANGE_TICK_RATE = 30;
    private static final int ICON_SIZE = 16;
    private static final int ICON_TRANSITION_TICK_DURATION = 4;
    private final int slotIndex;
    private final List<ResourceLocation> icons;
    private int tick;
    private int iconIndex;

    public CyclingSlotBackground(int slotIndex, List<ResourceLocation> icons)
    {   this.slotIndex = slotIndex;
        this.icons = icons;
    }

    public void tick()
    {
        if (!this.icons.isEmpty() && ++this.tick % ICON_CHANGE_TICK_RATE == 0)
        {   this.iconIndex = (this.iconIndex + 1) % this.icons.size();
        }
    }

    public void render(AbstractContainerMenu menu, GuiGraphics graphics, float partialTick, int guiLeft, int guiTop)
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

    private void renderIcon(Slot slot, ResourceLocation icon, float alpha, GuiGraphics graphics, int x, int y)
    {
        RenderSystem.enableBlend();
        graphics.setColor(1, 1, 1, alpha);
        graphics.blit(icon, x + slot.x, y + slot.y, 0, 0, ICON_SIZE, ICON_SIZE, ICON_SIZE, ICON_SIZE);
        graphics.setColor(1, 1, 1, 1);
        RenderSystem.disableBlend();
    }

    private float getIconTransitionTransparency(float partialTick)
    {
        float $$1 = (float)(this.tick % ICON_CHANGE_TICK_RATE) + partialTick;
        return Math.min($$1, ICON_TRANSITION_TICK_DURATION) / ICON_TRANSITION_TICK_DURATION;
    }
}