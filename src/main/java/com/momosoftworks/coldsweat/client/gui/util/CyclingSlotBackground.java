package com.momosoftworks.coldsweat.client.gui.util;

import com.mojang.blaze3d.matrix.MatrixStack;
import com.mojang.blaze3d.systems.RenderSystem;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.screen.Screen;
import net.minecraft.inventory.container.Container;
import net.minecraft.inventory.container.Slot;
import net.minecraft.util.ResourceLocation;

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

    public void render(Container menu, MatrixStack poseStack, float partialTick, int guiLeft, int guiTop)
    {
        Slot slot = menu.getSlot(this.slotIndex);
        if (!this.icons.isEmpty() && !slot.hasItem())
        {
            boolean shouldTransition = this.icons.size() > 1 && this.tick >= ICON_CHANGE_TICK_RATE;
            float transparency = shouldTransition ? this.getIconTransitionTransparency(partialTick) : 1.0F;
            if (transparency < 1.0F)
            {   int iconIndex = Math.floorMod(this.iconIndex - 1, this.icons.size());
                this.renderIcon(slot, this.icons.get(iconIndex), 1.0F - transparency, poseStack, guiLeft, guiTop);
            }

            this.renderIcon(slot, this.icons.get(this.iconIndex), transparency, poseStack, guiLeft, guiTop);
        }
    }

    private void renderIcon(Slot slot, ResourceLocation icon, float alpha, MatrixStack poseStack, int x, int y)
    {
        Minecraft.getInstance().textureManager.bind(icon);
        RenderSystem.enableBlend();
        RenderSystem.color4f(1, 1, 1, alpha);
        Screen.blit(poseStack, x + slot.x, y + slot.y, 0, 0, ICON_SIZE, ICON_SIZE, ICON_SIZE, ICON_SIZE);
        RenderSystem.color4f(1, 1, 1, 1);
        RenderSystem.disableBlend();
    }

    private float getIconTransitionTransparency(float partialTick)
    {
        float $$1 = (float)(this.tick % ICON_CHANGE_TICK_RATE) + partialTick;
        return Math.min($$1, ICON_TRANSITION_TICK_DURATION) / ICON_TRANSITION_TICK_DURATION;
    }
}