package com.momosoftworks.coldsweat.mixin;

import com.llamalad7.mixinextras.injector.wrapoperation.Operation;
import com.llamalad7.mixinextras.injector.wrapoperation.WrapOperation;
import com.momosoftworks.coldsweat.common.entity.Chameleon;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.entity.player.Player;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;

/**
 * Since 26.x, entities can't ride non-serializable vehicles (like players) on the server
 */
@Mixin(Entity.class)
public class MixinChameleonRidePlayer
{
    @WrapOperation(method = "startRiding(Lnet/minecraft/world/entity/Entity;ZZ)Z",
                   at = @At(value = "INVOKE", target = "Lnet/minecraft/world/entity/EntityType;canSerialize()Z"))
    private boolean allowChameleonRidePlayer(EntityType<?> type, Operation<Boolean> original,
                                             Entity entityToRide, boolean force, boolean sendEventAndTriggers)
    {
        if ((Object) this instanceof Chameleon && entityToRide instanceof Player)
        {   return true;
        }
        return original.call(type);
    }
}
