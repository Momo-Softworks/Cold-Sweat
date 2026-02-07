package com.momosoftworks.coldsweat.mixin;

import com.mojang.blaze3d.matrix.MatrixStack;
import com.momosoftworks.coldsweat.api.util.Temperature;
import com.momosoftworks.coldsweat.client.gui.Overlays;
import com.momosoftworks.coldsweat.common.item.ThermometerItem;
import com.momosoftworks.coldsweat.config.ConfigSettings;
import com.momosoftworks.coldsweat.util.world.WorldHelper;
import net.minecraft.client.Minecraft;
import net.minecraft.client.renderer.IRenderTypeBuffer;
import net.minecraft.client.renderer.entity.ItemFrameRenderer;
import net.minecraft.entity.item.ItemFrameEntity;
import net.minecraft.item.ItemStack;
import net.minecraft.util.text.Color;
import net.minecraft.util.text.ITextComponent;
import net.minecraft.util.text.StringTextComponent;
import net.minecraft.util.text.Style;
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
    private static ItemFrameEntity ENTITY = null;

    @Inject(method = "renderNameTag(Lnet/minecraft/entity/item/ItemFrameEntity;Lnet/minecraft/util/text/ITextComponent;Lcom/mojang/blaze3d/matrix/MatrixStack;Lnet/minecraft/client/renderer/IRenderTypeBuffer;I)V", at = @At("HEAD"))
    private void storeItemStack(ItemFrameEntity itemFrame, ITextComponent pDisplayName, MatrixStack pMatrixStack, IRenderTypeBuffer pBuffer, int pPackedLight, CallbackInfo ci)
    {   ITEM = itemFrame.getItem();
        ENTITY = itemFrame;
    }

    @ModifyArg(method = "renderNameTag(Lnet/minecraft/entity/item/ItemFrameEntity;Lnet/minecraft/util/text/ITextComponent;Lcom/mojang/blaze3d/matrix/MatrixStack;Lnet/minecraft/client/renderer/IRenderTypeBuffer;I)V",
               at = @At(value = "INVOKE", target = "Lnet/minecraft/client/renderer/entity/EntityRenderer;renderNameTag(Lnet/minecraft/entity/Entity;Lnet/minecraft/util/text/ITextComponent;Lcom/mojang/blaze3d/matrix/MatrixStack;Lnet/minecraft/client/renderer/IRenderTypeBuffer;I)V"))
    private ITextComponent modifyItemFrameLabel(ITextComponent original)
    {
        if (ITEM.getItem() instanceof ThermometerItem && Minecraft.getInstance().level != null && ENTITY != null)
        {
            double minTemp = ConfigSettings.MIN_TEMP.get();
            double maxTemp = ConfigSettings.MAX_TEMP.get();
            double worldTemp = WorldHelper.getTemperatureAt(Minecraft.getInstance().level, ENTITY.blockPosition());
            Temperature.Units units = ConfigSettings.UNITS.get();
            Style tempColor = Style.EMPTY.withColor(Color.fromRgb(Overlays.getWorldTempColor(worldTemp, minTemp, maxTemp)));
            int convertedTemp = (int) Temperature.convert(worldTemp, Temperature.Units.MC, units, true) + ConfigSettings.TEMP_OFFSET.get();
            return new StringTextComponent(convertedTemp + " " + units.getFormattedName().getString()).withStyle(tempColor);
        }
        return original;
    }

    @Redirect(method = "shouldShowName(Lnet/minecraft/entity/item/ItemFrameEntity;)Z", at = @At(value = "INVOKE", target = "Lnet/minecraft/item/ItemStack;hasCustomHoverName()Z"))
    private boolean alwaysShowThermometerName(ItemStack instance)
    {
        if (instance.getItem() instanceof ThermometerItem)
        {   return true;
        }
        return instance.hasCustomHoverName();
    }
}
