package com.momosoftworks.coldsweat.mixin;

import com.momosoftworks.coldsweat.common.capability.handler.ShearableFurManager;
import net.minecraft.core.BlockPos;
import net.minecraft.core.dispenser.ShearsDispenseItemBehavior;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.world.entity.EntitySelector;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.phys.AABB;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

@Mixin(ShearsDispenseItemBehavior.class)
public class MixinShearsDispenseBehavior
{
    @Inject(method = "tryShearLivingEntity", at = @At("TAIL"), cancellable = true)
    private static void tryShearFurCapability(ServerLevel level, BlockPos pos, CallbackInfoReturnable<Boolean> cir)
    {
        boolean success = false;
        for (LivingEntity living : level.getEntitiesOfClass(LivingEntity.class, new AABB(pos), EntitySelector.NO_SPECTATORS))
        {   success |= ShearableFurManager.shear(living, null);
        }
        if (success) cir.setReturnValue(true);
    }
}
