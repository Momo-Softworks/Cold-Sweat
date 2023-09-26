package com.momosoftworks.coldsweat.mixin;

import com.momosoftworks.coldsweat.common.container.HearthContainer;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.DisplayEffectsScreen;
import net.minecraft.client.gui.screen.inventory.ContainerScreen;
import net.minecraft.potion.EffectInstance;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.ModifyVariable;

import java.util.Collection;

@Mixin(DisplayEffectsScreen.class)
public class MixinEffectsScreen
{
    ContainerScreen<?> screen = (ContainerScreen<?>) (Object) this;

    @ModifyVariable(method = "renderEffects",
              at = @At(value = "STORE"), ordinal = 0)
    private Collection<EffectInstance> getEffects(Collection<EffectInstance> collection)
    {
        if (screen.getMenu() instanceof HearthContainer)
        {   return ((HearthContainer) screen.getMenu()).te.getEffects();
        }
        else return Minecraft.getInstance().player.getActiveEffects();
    }
}
