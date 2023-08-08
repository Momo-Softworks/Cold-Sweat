package dev.momostudios.coldsweat.mixin;

import com.mojang.blaze3d.matrix.MatrixStack;
import dev.momostudios.coldsweat.api.event.client.RenderTooltipEvent;
import net.minecraft.client.gui.screen.Screen;
import net.minecraft.item.ItemStack;
import net.minecraftforge.common.MinecraftForge;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

@Mixin(Screen.class)
public class MixinTooltip
{
    @Inject(method = "renderTooltip(Lcom/mojang/blaze3d/matrix/MatrixStack;Lnet/minecraft/item/ItemStack;II)V", at = @At("TAIL"))
    private void postItemToolTip(MatrixStack matrix, ItemStack item, int mouseX, int mouseY, CallbackInfo ci)
    {   MinecraftForge.EVENT_BUS.post(new RenderTooltipEvent(matrix, item, mouseX, mouseY, (Screen) (Object) this));
    }
}
