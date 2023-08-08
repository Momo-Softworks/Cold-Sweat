package dev.momostudios.coldsweat.mixin;

import dev.momostudios.coldsweat.common.container.HearthContainer;
import net.minecraft.client.gui.screens.inventory.AbstractContainerScreen;
import net.minecraft.client.gui.screens.inventory.EffectRenderingInventoryScreen;
import net.minecraft.client.player.LocalPlayer;
import net.minecraft.world.effect.MobEffectInstance;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Redirect;

import java.util.Collection;

@Mixin(EffectRenderingInventoryScreen.class)
public class MixinEffectsScreen
{
    AbstractContainerScreen screen = (AbstractContainerScreen) (Object) this;

    @Redirect(method = "renderEffects(Lcom/mojang/blaze3d/vertex/PoseStack;II)V",
              at = @At(value = "INVOKE", target = "Lnet/minecraft/client/player/LocalPlayer;getActiveEffects()Ljava/util/Collection;"))
    private Collection<MobEffectInstance> getEffects(LocalPlayer instance)
    {
        if (screen.getMenu() instanceof HearthContainer container)
        {   return container.te.getEffects();
        }
        else return instance.getActiveEffects();
    }
}
