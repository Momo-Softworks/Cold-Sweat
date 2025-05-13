package com.momosoftworks.coldsweat.client.gui.util;

import com.mojang.blaze3d.systems.RenderSystem;
import com.mojang.blaze3d.vertex.PoseStack;
import net.minecraft.client.gui.screens.inventory.AbstractContainerScreen;
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

    public void render(AbstractContainerMenu menu, PoseStack poseStack, float partialTick, int guiLeft, int guiTop)
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

    private void renderIcon(Slot slot, ResourceLocation icon, float alpha, PoseStack poseStack, int x, int y)
    {
        RenderSystem.setShaderTexture(0, icon);
        RenderSystem.enableBlend();
        RenderSystem.setShaderColor(1, 1, 1, alpha);
        AbstractContainerScreen.blit(poseStack, x + slot.x, y + slot.y, 0, 0, ICON_SIZE, ICON_SIZE, ICON_SIZE, ICON_SIZE);
        RenderSystem.setShaderColor(1, 1, 1, 1);
        RenderSystem.disableBlend();
    }

    private float getIconTransitionTransparency(float partialTick)
    {
        float $$1 = (float)(this.tick % ICON_CHANGE_TICK_RATE) + partialTick;
        return Math.min($$1, ICON_TRANSITION_TICK_DURATION) / ICON_TRANSITION_TICK_DURATION;
    }
}