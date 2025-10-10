package com.momosoftworks.coldsweat.mixin;

import com.momosoftworks.coldsweat.compat.CompatManager;
import com.momosoftworks.coldsweat.config.ConfigSettings;
import com.momosoftworks.coldsweat.data.tag.ModBlockTags;
import net.minecraft.block.AbstractFireBlock;
import net.minecraft.block.BlockState;
import net.minecraft.entity.Entity;
import net.minecraft.entity.item.ItemEntity;
import net.minecraft.util.math.BlockPos;
import net.minecraft.world.World;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

@Mixin(AbstractFireBlock.class)
public class MixinSoulFire
{
    @Inject(method = "entityInside(Lnet/minecraft/block/BlockState;Lnet/minecraft/world/World;Lnet/minecraft/util/math/BlockPos;Lnet/minecraft/entity/Entity;)V",
    at = @At("HEAD"), cancellable = true)
    public void entityInside(BlockState state, World level, BlockPos pos, Entity entity, CallbackInfo ci)
    {
        if (state.is(ModBlockTags.SOUL_FIRE) && ConfigSettings.COLD_SOUL_FIRE.get()
        && !(entity instanceof ItemEntity && CompatManager.isSpiritLoaded()))
        {
            entity.clearFire();
            ci.cancel();
        }
    }
}
