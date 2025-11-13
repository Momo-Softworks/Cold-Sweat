package com.momosoftworks.coldsweat.mixin.compat;

import com.mojang.blaze3d.vertex.PoseStack;
import mezz.jei.api.gui.inputs.RecipeSlotUnderMouse;
import mezz.jei.common.gui.JeiTooltip;
import mezz.jei.library.gui.recipes.RecipeLayout;
import net.minecraft.network.chat.Component;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.Redirect;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

import java.util.Collection;
import java.util.Optional;

/**
 * Fixes an issue in JEI where the item stack isn't passed into the tooltip when hovering over a recipe slot.
 */
@Mixin(RecipeLayout.class)
public abstract class MixinJEITooltip
{
    @Shadow public abstract Optional<RecipeSlotUnderMouse> getSlotUnderMouse(double mouseX, double mouseY);

    private static int MOUSE_X = 0;
    private static int MOUSE_Y = 0;

    @Inject(method = "drawOverlays", at = @At("HEAD"), remap = false)
    private void onDrawOverlays(PoseStack poseStack, int mouseX, int mouseY, CallbackInfo ci)
    {
        MOUSE_X = mouseX;
        MOUSE_Y = mouseY;
    }

    @Redirect(method = "drawOverlays", at = @At(value = "INVOKE", target = "Lmezz/jei/common/gui/JeiTooltip;addAll(Ljava/util/Collection;)V", ordinal = 0), remap = false)
    private void onDrawOverlays(JeiTooltip instance, Collection<? extends Component> components)
    {
        Optional<RecipeSlotUnderMouse> hoveredSlot = this.getSlotUnderMouse(MOUSE_X, MOUSE_Y);
        if (hoveredSlot.isPresent() && hoveredSlot.get().slot().getDisplayedIngredient().isPresent())
        {   instance.setIngredient(hoveredSlot.get().slot().getDisplayedIngredient().get());
        }
        instance.addAll(components);
    }
}
