package com.momosoftworks.coldsweat.mixin;

import com.momosoftworks.coldsweat.api.util.Temperature;
import com.momosoftworks.coldsweat.client.gui.Overlays;
import com.momosoftworks.coldsweat.common.item.ThermometerItem;
import com.momosoftworks.coldsweat.config.ConfigSettings;
import com.momosoftworks.coldsweat.util.world.WorldHelper;
import net.minecraft.client.Minecraft;
import net.minecraft.client.renderer.entity.ItemFrameRenderer;
import net.minecraft.network.chat.Component;
import net.minecraft.network.chat.Style;
import net.minecraft.world.entity.decoration.ItemFrame;
import net.minecraft.world.item.ItemStack;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.Redirect;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

/**
 * Item frames holding a thermometer display the temperature as their name tag
 */
@Mixin(ItemFrameRenderer.class)
public class MixinItemFrameLabel
{
    @Inject(method = "getNameTag(Lnet/minecraft/world/entity/decoration/ItemFrame;)Lnet/minecraft/network/chat/Component;", at = @At("HEAD"), cancellable = true)
    private void modifyItemFrameLabel(ItemFrame entity, CallbackInfoReturnable<Component> cir)
    {
        if (entity.getItem().getItem() instanceof ThermometerItem && Minecraft.getInstance().level != null)
        {
            double minTemp = ConfigSettings.MIN_TEMP.get();
            double maxTemp = ConfigSettings.MAX_TEMP.get();
            double worldTemp = WorldHelper.getTemperatureAt(Minecraft.getInstance().level, entity.blockPosition());
            Temperature.Units units = ConfigSettings.UNITS.get();
            Style tempColor = Style.EMPTY.withColor(Overlays.getWorldTempColor(worldTemp, minTemp, maxTemp));
            int convertedTemp = (int) Temperature.convert(worldTemp, Temperature.Units.MC, units, true) + ConfigSettings.TEMP_OFFSET.get();
            cir.setReturnValue(Component.literal(convertedTemp + " " + units.getFormattedName().getString()).withStyle(tempColor));
        }
    }

    @Redirect(method = "shouldShowName(Lnet/minecraft/world/entity/decoration/ItemFrame;D)Z",
              at = @At(value = "INVOKE", target = "Lnet/minecraft/world/item/ItemStack;getCustomName()Lnet/minecraft/network/chat/Component;"))
    private Component alwaysShowThermometerName(ItemStack instance)
    {
        if (instance.getItem() instanceof ThermometerItem)
        {   return Component.empty();
        }
        return instance.getCustomName();
    }
}
