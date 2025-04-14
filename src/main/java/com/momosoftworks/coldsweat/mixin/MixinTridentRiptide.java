package com.momosoftworks.coldsweat.mixin;

import com.momosoftworks.coldsweat.api.temperature.modifier.WaterTempModifier;
import com.momosoftworks.coldsweat.api.util.Temperature;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.item.TridentItem;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Redirect;

@Mixin(TridentItem.class)
public class MixinTridentRiptide
{
    @Redirect(method = "releaseUsing", at = @At(value = "INVOKE", target = "Lnet/minecraft/entity/player/PlayerEntity;isInWaterOrRain()Z"))
    private boolean allowRiptideWhenWet(PlayerEntity player)
    {   return player.isInWaterOrRain() || Temperature.getModifier(player, Temperature.Trait.WORLD, WaterTempModifier.class).isPresent();
    }

    @Redirect(method = "use", at = @At(value = "INVOKE", target = "Lnet/minecraft/entity/player/PlayerEntity;isInWaterOrRain()Z"))
    private boolean allowUseWhenWet(PlayerEntity player)
    {   return player.isInWaterOrRain() || Temperature.getModifier(player, Temperature.Trait.WORLD, WaterTempModifier.class).isPresent();
    }
}
