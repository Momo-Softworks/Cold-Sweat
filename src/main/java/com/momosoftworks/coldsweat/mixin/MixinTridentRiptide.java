package com.momosoftworks.coldsweat.mixin;

import com.momosoftworks.coldsweat.api.temperature.modifier.WaterTempModifier;
import com.momosoftworks.coldsweat.api.util.Temperature;
import com.momosoftworks.coldsweat.core.event.TaskScheduler;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.TridentItem;
import net.minecraft.world.level.Level;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.Redirect;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

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

    @Inject(method = "releaseUsing", at = @At(value = "INVOKE", target = "Lnet/minecraft/world/entity/player/Player;startAutoSpinAttack(I)V"))
    private void removeWetnessOnUse(ItemStack stack, Level level, LivingEntity entity, int timeLeft, CallbackInfo ci)
    {
        TaskScheduler.scheduleServer(() ->
        {
            if (!entity.isInWaterOrBubble())
            {   Temperature.removeModifiers(entity, Temperature.Trait.WORLD, mod -> mod instanceof WaterTempModifier);
            }
        }, 5);
    }
}
