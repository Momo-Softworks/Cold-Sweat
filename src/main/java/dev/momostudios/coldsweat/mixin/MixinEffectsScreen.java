package dev.momostudios.coldsweat.mixin;

import dev.momostudios.coldsweat.common.container.HearthContainer;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.screens.inventory.AbstractContainerScreen;
import net.minecraft.client.gui.screens.inventory.EffectRenderingInventoryScreen;
import net.minecraft.world.effect.MobEffectInstance;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.ModifyVariable;

import java.util.Collection;

@Mixin(EffectRenderingInventoryScreen.class)
public class MixinEffectsScreen
{
    AbstractContainerScreen<?> screen = (AbstractContainerScreen<?>) (Object) this;

    @ModifyVariable(method = "renderEffects",
                    at = @At(value = "STORE"), ordinal = 0)
    private Collection<MobEffectInstance> getEffects(Collection<MobEffectInstance> collection)
    {
        if (screen.getMenu() instanceof HearthContainer container)
        {   return container.te.getEffects();
        }
        else return Minecraft.getInstance().player.getActiveEffects();
    }
}
