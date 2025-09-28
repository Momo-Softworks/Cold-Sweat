package com.momosoftworks.coldsweat.mixin;

import com.momosoftworks.coldsweat.api.event.vanilla.EntityMoveEvent;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.phys.Vec3;
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
    private static Vec3 getInputVector(Vec3 pRelative, float pMotionScaler, float pFacing)
    {   return null;
    }

    Entity self = (Entity) (Object) this;

    @Inject(method = "moveRelative", at = @At(value = "INVOKE", target = "Lnet/minecraft/world/entity/Entity;setDeltaMovement(Lnet/minecraft/world/phys/Vec3;)V"),
            locals = LocalCapture.CAPTURE_FAILHARD, cancellable = true)
    private void getInputVector(float speed, Vec3 direction, CallbackInfo ci,
                                // locals
                                Vec3 inputVector)
    {
        EntityMoveEvent event = new EntityMoveEvent((Entity) (Object) this, speed, direction);
        if (MinecraftForge.EVENT_BUS.post(event))
        {
            ci.cancel();
            Vec3 motion = getInputVector(event.getDirection(), event.getSpeed(), self.getYRot());
            self.setDeltaMovement(self.getDeltaMovement().add(motion));
        }
    }
}
