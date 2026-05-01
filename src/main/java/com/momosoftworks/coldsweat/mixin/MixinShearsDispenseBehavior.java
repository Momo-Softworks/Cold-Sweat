package com.momosoftworks.coldsweat.mixin;

import com.momosoftworks.coldsweat.common.capability.handler.ShearableFurManager;
import com.momosoftworks.coldsweat.util.world.WorldHelper;
import net.minecraft.dispenser.BeehiveDispenseBehavior;
import net.minecraft.entity.LivingEntity;
import net.minecraft.util.EntityPredicates;
import net.minecraft.util.math.AxisAlignedBB;
import net.minecraft.util.math.BlockPos;
import net.minecraft.world.server.ServerWorld;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

@Mixin(BeehiveDispenseBehavior.class)
public class MixinShearsDispenseBehavior
{
    @Inject(method = "tryShearLivingEntity", at = @At("TAIL"), cancellable = true)
    private static void tryShearFurCapability(ServerWorld level, BlockPos pos, CallbackInfoReturnable<Boolean> cir)
    {
        boolean success = false;
        for (LivingEntity living : WorldHelper.getEntitiesOfClass(LivingEntity.class, level, new AxisAlignedBB(pos), EntityPredicates.NO_SPECTATORS))
        {   success |= ShearableFurManager.shear(living, null);
        }
        if (success) cir.setReturnValue(true);
    }
}
