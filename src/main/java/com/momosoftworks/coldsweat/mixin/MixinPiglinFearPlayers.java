package com.momosoftworks.coldsweat.mixin;

import com.momosoftworks.coldsweat.util.entity.EntityHelper;
import net.minecraft.world.entity.ai.Brain;
import net.minecraft.world.entity.ai.memory.MemoryModuleType;
import net.minecraft.world.entity.monster.piglin.Piglin;
import net.minecraft.world.entity.monster.piglin.PiglinAi;
import net.minecraft.world.entity.player.Player;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

import java.util.List;

@Mixin(PiglinAi.class)
public class MixinPiglinFearPlayers
{
    @Inject(method = "isNearZombified", at = @At("RETURN"), cancellable = true)
    private static void fearSoulspringLamp(Piglin entity, CallbackInfoReturnable<Boolean> cir)
    {
        if (!cir.getReturnValue())
        {
            Brain<Piglin> brain = entity.getBrain();
            List<Player> nearbyPlayers = entity.level.getEntitiesOfClass(Player.class, entity.getBoundingBox().inflate(6.0D, 3.0D, 6.0D));
            for (Player player : nearbyPlayers)
            {
                if (EntityHelper.holdingLitLamp(player))
                {
                    brain.setMemory(MemoryModuleType.NEAREST_VISIBLE_ZOMBIFIED, player);
                    cir.setReturnValue(true);
                    return;
                }
                else if (brain.getMemory(MemoryModuleType.NEAREST_VISIBLE_ZOMBIFIED).map(mem -> mem.equals(player)).orElse(false))
                {   brain.eraseMemory(MemoryModuleType.NEAREST_VISIBLE_ZOMBIFIED);
                }
            }
        }
    }
}
