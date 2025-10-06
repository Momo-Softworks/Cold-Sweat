package com.momosoftworks.coldsweat.mixin;

import com.momosoftworks.coldsweat.data.tag.ModItemTags;
import net.minecraft.world.InteractionHand;
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
        Brain<Piglin> brain = entity.getBrain();
        // Check if piglin should still be fleeing from current target
        if (entity.getPersistentData().getBoolean("FleeingItem"))
        {
            brain.getMemory(MemoryModuleType.NEAREST_VISIBLE_ZOMBIFIED).ifPresent(ent ->
            {
                if (!(ent instanceof Player player) || !isPlayerHoldingScaryItem(player) || !player.closerThan(entity, 6))
                {
                    entity.getPersistentData().putBoolean("FleeingItem", false);
                    brain.eraseMemory(MemoryModuleType.NEAREST_VISIBLE_ZOMBIFIED);
                }
            });
        }
        // Check for targets to flee from
        List<Player> nearbyPlayers = entity.level().getEntitiesOfClass(Player.class, entity.getBoundingBox().inflate(6.0D, 6.0D, 6.0D));
        for (Player player : nearbyPlayers)
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

    private static boolean isPlayerHoldingScaryItem(Player entity)
    {
        return entity.getItemInHand(InteractionHand.MAIN_HAND).is(ModItemTags.SCARES_PIGLINS)
            || entity.getItemInHand(InteractionHand.OFF_HAND).is(ModItemTags.SCARES_PIGLINS);
    }
}
