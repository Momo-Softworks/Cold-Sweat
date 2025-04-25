package com.momosoftworks.coldsweat.mixin;

import com.momosoftworks.coldsweat.config.ConfigSettings;
import net.minecraft.core.BlockPos;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.BaseFireBlock;
import net.minecraft.world.level.block.SoulFireBlock;
import net.minecraft.world.level.block.state.BlockState;
import org.spongepowered.asm.mixin.Final;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

@Mixin(BaseFireBlock.class)
public class MixinSoulFire
{
    @Shadow @Final private float fireDamage;

    @Inject(method = "entityInside(Lnet/minecraft/world/level/block/state/BlockState;Lnet/minecraft/world/level/Level;Lnet/minecraft/core/BlockPos;Lnet/minecraft/world/entity/Entity;)V",
    at = @At("HEAD"), cancellable = true)
    public void entityInside(BlockState state, Level level, BlockPos pos, Entity entity, CallbackInfo ci)
    {
        if (state.getBlock() instanceof SoulFireBlock && ConfigSettings.COLD_SOUL_FIRE.get())
        {
            entity.setIsInPowderSnow(true);
            entity.clearFire();
            entity.hurt(entity.damageSources().inFire(), this.fireDamage);
            ci.cancel();
        }
    }
}
