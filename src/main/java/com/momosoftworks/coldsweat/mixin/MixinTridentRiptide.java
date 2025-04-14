package com.momosoftworks.coldsweat.mixin;

import com.momosoftworks.coldsweat.api.temperature.modifier.WaterTempModifier;
import com.momosoftworks.coldsweat.api.util.Temperature;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.TridentItem;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Redirect;

@Mixin(TridentItem.class)
public class MixinTridentRiptide
{
    @Redirect(method = "releaseUsing", at = @At(value = "INVOKE", target = "Lnet/minecraft/world/entity/player/Player;isInWaterOrRain()Z"))
    private boolean allowRiptideWhenWet(Player player)
    {   return player.isInWaterOrRain() || Temperature.getModifier(player, Temperature.Trait.WORLD, WaterTempModifier.class).isPresent();
    }

    @Redirect(method = "use", at = @At(value = "INVOKE", target = "Lnet/minecraft/world/entity/player/Player;isInWaterOrRain()Z"))
    private boolean allowUseWhenWet(Player player)
    {   return player.isInWaterOrRain() || Temperature.getModifier(player, Temperature.Trait.WORLD, WaterTempModifier.class).isPresent();
    }
}
