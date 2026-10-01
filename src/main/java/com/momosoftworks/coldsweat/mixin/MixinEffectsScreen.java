package com.momosoftworks.coldsweat.mixin;

import com.momosoftworks.coldsweat.common.container.HearthContainer;
import net.minecraft.client.gui.screens.inventory.AbstractContainerScreen;
import net.minecraft.client.gui.screens.inventory.EffectsInInventory;
import net.minecraft.world.effect.MobEffectInstance;
import org.spongepowered.asm.mixin.Final;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.ModifyVariable;

import java.util.Collection;

/**
 * Displays the hearth's stored effects instead of the player's in the hearth screen's effect panel
 */
@Mixin(EffectsInInventory.class)
public class MixinEffectsScreen
{
    @Shadow
    @Final
    private AbstractContainerScreen<?> screen;

    @ModifyVariable(method = "extractRenderState",
                    at = @At(value = "STORE"), ordinal = 0)
    private Collection<MobEffectInstance> getEffects(Collection<MobEffectInstance> collection)
    {
        if (screen.getMenu() instanceof HearthContainer container)
        {   return container.te.getEffects();
        }
        return collection;
    }
}
