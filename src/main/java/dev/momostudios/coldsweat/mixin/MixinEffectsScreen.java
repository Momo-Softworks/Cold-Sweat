package dev.momostudios.coldsweat.mixin;

import dev.momostudios.coldsweat.common.container.HearthContainer;
import net.minecraft.client.entity.player.ClientPlayerEntity;
import net.minecraft.client.gui.DisplayEffectsScreen;
import net.minecraft.client.gui.screen.inventory.ContainerScreen;
import net.minecraft.potion.EffectInstance;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Redirect;

import java.util.Collection;

@Mixin(DisplayEffectsScreen.class)
public class MixinEffectsScreen
{
    ContainerScreen screen = (ContainerScreen) (Object) this;

    @Redirect(method = "renderEffects(Lcom/mojang/blaze3d/matrix/MatrixStack;)V",
              at = @At(value = "INVOKE", target = "Lnet/minecraft/client/entity/player/ClientPlayerEntity;getActiveEffects()Ljava/util/Collection;"))
    private Collection<EffectInstance> getEffects(ClientPlayerEntity instance)
    {
        if (screen.getMenu() instanceof HearthContainer)
        {   return ((HearthContainer) screen.getMenu()).te.getEffects();
        }
        else return instance.getActiveEffects();
    }
}
