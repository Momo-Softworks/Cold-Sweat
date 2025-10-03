package com.momosoftworks.coldsweat.mixin;

import com.momosoftworks.coldsweat.api.event.vanilla.EntityMoveEvent;
import net.minecraft.entity.Entity;
import net.minecraft.util.math.vector.Vector3d;
import net.minecraftforge.common.MinecraftForge;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;
import org.spongepowered.asm.mixin.injection.callback.LocalCapture;

@Mixin(Entity.class)
public abstract class MixinEntityMove
{
    @Shadow
    private static Vector3d getInputVector(Vector3d pRelative, float pMotionScaler, float pFacing)
    {   return null;
    }

    Entity self = (Entity) (Object) this;

    @Inject(method = "moveRelative", at = @At(value = "INVOKE", target = "Lnet/minecraft/entity/Entity;setDeltaMovement(Lnet/minecraft/util/math/vector/Vector3d;)V"),
            locals = LocalCapture.CAPTURE_FAILHARD, cancellable = true)
    private void modifyInputVector(float speed, Vector3d direction, CallbackInfo ci)
    {
        EntityMoveEvent event = new EntityMoveEvent((Entity) (Object) this, speed, direction);
        if (MinecraftForge.EVENT_BUS.post(event))
        {
            ci.cancel();
            Vector3d motion = getInputVector(event.getDirection(), event.getSpeed(), self.getYHeadRot());
            self.setDeltaMovement(self.getDeltaMovement().add(motion));
        }
    }
}
