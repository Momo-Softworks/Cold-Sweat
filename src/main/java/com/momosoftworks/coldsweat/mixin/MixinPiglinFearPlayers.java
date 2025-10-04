package com.momosoftworks.coldsweat.mixin;

import com.momosoftworks.coldsweat.data.tag.ModItemTags;
import net.minecraft.entity.ai.brain.Brain;
import net.minecraft.entity.ai.brain.memory.MemoryModuleType;
import net.minecraft.entity.monster.piglin.PiglinEntity;
import net.minecraft.entity.monster.piglin.PiglinTasks;
import net.minecraft.entity.player.PlayerEntity;
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
        Brain<PiglinEntity> brain = entity.getBrain();
        // Check if piglin should still be fleeing from current target
        if (entity.getPersistentData().getBoolean("FleeingItem"))
        {
            brain.getMemory(MemoryModuleType.NEAREST_VISIBLE_ZOMBIFIED).ifPresent(ent ->
            {
                if (!(ent instanceof PlayerEntity) || !isPlayerHoldingScaryItem((PlayerEntity) ent) || !ent.closerThan(entity, 6))
                {
                    entity.getPersistentData().putBoolean("FleeingItem", false);
                    brain.eraseMemory(MemoryModuleType.NEAREST_VISIBLE_ZOMBIFIED);
                }
            });
        }
        // Check for targets to flee from
        List<PlayerEntity> nearbyPlayers = entity.level.getEntitiesOfClass(PlayerEntity.class, entity.getBoundingBox().inflate(6.0D, 6.0D, 6.0D));
        for (PlayerEntity player : nearbyPlayers)
        {
            if (isPlayerHoldingScaryItem(player))
            {
                brain.setMemory(MemoryModuleType.NEAREST_VISIBLE_ZOMBIFIED, player);
                entity.getPersistentData().putBoolean("FleeingItem", true);
                cir.setReturnValue(true);
                return;
            }
        }
    }

    private static boolean isPlayerHoldingScaryItem(PlayerEntity entity)
    {
        return ModItemTags.SCARES_PIGLINS.contains(entity.getItemInHand(Hand.MAIN_HAND).getItem())
            || ModItemTags.SCARES_PIGLINS.contains(entity.getItemInHand(Hand.OFF_HAND).getItem());
    }
}
