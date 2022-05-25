package dev.momostudios.coldsweat.mixin;

import com.mojang.blaze3d.vertex.PoseStack;
import dev.momostudios.coldsweat.ColdSweat;
import dev.momostudios.coldsweat.api.event.client.RenderTooltipPostEvent;
import net.minecraft.client.gui.Font;
import net.minecraft.client.gui.screens.Screen;
import net.minecraft.client.gui.screens.inventory.tooltip.ClientTooltipComponent;
import net.minecraft.world.item.ItemStack;
import net.minecraftforge.common.MinecraftForge;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

import java.util.List;

@Mixin(Screen.class)
public class MixinScreen
{
    Screen screen = (Screen) (Object) this;

    @Shadow
    private ItemStack tooltipStack;
    @Shadow
    protected Font font;

    @Inject(method = "renderTooltipInternal(Lcom/mojang/blaze3d/vertex/PoseStack;Ljava/util/List;II)V", at = @At(value = "INVOKE", target = "Lcom/mojang/blaze3d/vertex/PoseStack;popPose()V"), remap = ColdSweat.REMAP_MIXINS)
    public void renderTooltipInternal(PoseStack poseStack, List<ClientTooltipComponent> components, int x, int y, CallbackInfo ci)
    {
        int longestComponent = 0;
        for(ClientTooltipComponent clienttooltipcomponent : components)
        {
            int k = clienttooltipcomponent.getWidth(font);
            if (k > longestComponent) {
                longestComponent = k;
            }
        }
        int offset = x + 12;
        if (offset + longestComponent > screen.width) {
            offset -= 28 + longestComponent;
        }

        MinecraftForge.EVENT_BUS.post(new RenderTooltipPostEvent(tooltipStack, poseStack, offset, y, font, components));
    }
}
