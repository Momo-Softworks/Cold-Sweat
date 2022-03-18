package dev.momostudios.coldsweat.api.event.client;

import com.mojang.blaze3d.vertex.PoseStack;
import net.minecraft.client.gui.Font;
import net.minecraft.client.gui.screens.inventory.tooltip.ClientTooltipComponent;
import net.minecraft.world.item.ItemStack;
import net.minecraftforge.client.event.RenderTooltipEvent;

import javax.annotation.Nonnull;
import java.util.List;

public class RenderTooltipPostEvent extends RenderTooltipEvent
{
    public RenderTooltipPostEvent(@Nonnull ItemStack itemStack, PoseStack poseStack, int x, int y, @Nonnull Font font, @Nonnull List<ClientTooltipComponent> components)
    {
        super(itemStack, poseStack, x, y, font, components);
    }
}
