package com.momosoftworks.coldsweat.mixin;

import com.momosoftworks.coldsweat.util.entity.EntityHelper;
import net.minecraft.entity.ai.brain.Brain;
import net.minecraft.entity.ai.brain.memory.MemoryModuleType;
import net.minecraft.entity.monster.piglin.PiglinEntity;
import net.minecraft.entity.monster.piglin.PiglinTasks;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.tags.ItemTags;
import net.minecraft.util.Hand;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

import java.util.List;

@Mixin(PiglinTasks.class)
public class MixinPiglinFearPlayers
{
    @Inject(method = "isNearZombified", at = @At("RETURN"), cancellable = true)
    private static void fearSoulspringLamp(PiglinEntity entity, CallbackInfoReturnable<Boolean> cir)
    {
        if (!cir.getReturnValue())
        {
            Brain<PiglinEntity> brain = entity.getBrain();
            List<PlayerEntity> nearbyPlayers = entity.level.getEntitiesOfClass(PlayerEntity.class, entity.getBoundingBox().inflate(6.0D, 3.0D, 6.0D));
            for (PlayerEntity player : nearbyPlayers)
            {
                if (ItemTags.PIGLIN_REPELLENTS.contains(player.getItemInHand(Hand.MAIN_HAND).getItem())
                || ItemTags.PIGLIN_REPELLENTS.contains(player.getItemInHand(Hand.OFF_HAND).getItem()))
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
