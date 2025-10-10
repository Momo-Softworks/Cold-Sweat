package com.momosoftworks.coldsweat.mixin;

import com.mojang.blaze3d.vertex.PoseStack;
import com.momosoftworks.coldsweat.api.util.Temperature;
import com.momosoftworks.coldsweat.client.gui.Overlays;
import com.momosoftworks.coldsweat.common.item.ThermometerItem;
import com.momosoftworks.coldsweat.config.ConfigSettings;
import com.momosoftworks.coldsweat.util.world.WorldHelper;
import net.minecraft.ChatFormatting;
import net.minecraft.client.Minecraft;
import net.minecraft.client.renderer.MultiBufferSource;
import net.minecraft.client.renderer.entity.ItemFrameRenderer;
import net.minecraft.network.chat.Component;
import net.minecraft.network.chat.Style;
import net.minecraft.world.entity.decoration.ItemFrame;
import net.minecraft.world.item.ItemStack;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.ModifyArg;
import org.spongepowered.asm.mixin.injection.Redirect;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

@Mixin(ItemFrameRenderer.class)
public class MixinItemFrameLabel
{
    private static ItemStack ITEM = ItemStack.EMPTY;
    private static ItemFrame ENTITY = null;

    @Inject(method = "renderNameTag(Lnet/minecraft/world/entity/decoration/ItemFrame;Lnet/minecraft/network/chat/Component;Lcom/mojang/blaze3d/vertex/PoseStack;Lnet/minecraft/client/renderer/MultiBufferSource;I)V", at = @At("HEAD"))
    private void storeItemStack(ItemFrame itemFrame, Component component, PoseStack poseStack, MultiBufferSource multiBufferSource, int i, CallbackInfo ci)
    {   ITEM = itemFrame.getItem();
        ENTITY = itemFrame;
    }

    @ModifyArg(method = "renderNameTag(Lnet/minecraft/world/entity/decoration/ItemFrame;Lnet/minecraft/network/chat/Component;Lcom/mojang/blaze3d/vertex/PoseStack;Lnet/minecraft/client/renderer/MultiBufferSource;I)V",
               at = @At(value = "INVOKE", target = "Lnet/minecraft/client/renderer/entity/EntityRenderer;renderNameTag(Lnet/minecraft/world/entity/Entity;Lnet/minecraft/network/chat/Component;Lcom/mojang/blaze3d/vertex/PoseStack;Lnet/minecraft/client/renderer/MultiBufferSource;I)V"))
    private Component modifyItemFrameLabel(Component original)
    {
        if (ITEM.getItem() instanceof ThermometerItem && Minecraft.getInstance().level != null && ENTITY != null)
        {
            double minTemp = ConfigSettings.MIN_TEMP.get();
            double maxTemp = ConfigSettings.MAX_TEMP.get();
            double worldTemp = WorldHelper.getTemperatureAt(Minecraft.getInstance().level, ENTITY.blockPosition());
            boolean celsius = ConfigSettings.CELSIUS.get();
            Style tempColor = Style.EMPTY.withColor(Overlays.getWorldTempColor(worldTemp, minTemp, maxTemp));
            int convertedTemp = (int) Temperature.convert(worldTemp, Temperature.Units.MC, celsius ? Temperature.Units.C : Temperature.Units.F, true) + ConfigSettings.TEMP_OFFSET.get();
            return Component.literal(convertedTemp + " " + (celsius ? Temperature.Units.C.getFormattedName()
                                                                          : Temperature.Units.F.getFormattedName())).withStyle(tempColor);
        }
        return original;
    }

    @Redirect(method = "shouldShowName(Lnet/minecraft/world/entity/decoration/ItemFrame;)Z", at = @At(value = "INVOKE", target = "Lnet/minecraft/world/item/ItemStack;hasCustomHoverName()Z"))
    private boolean alwaysShowThermometerName(ItemStack instance)
    {
        if (instance.getItem() instanceof ThermometerItem)
        {   return true;
        }
        return instance.hasCustomHoverName();
    }
}
