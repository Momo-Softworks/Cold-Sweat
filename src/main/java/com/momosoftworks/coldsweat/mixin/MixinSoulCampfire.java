package com.momosoftworks.coldsweat.mixin;

import net.minecraft.world.entity.InsideBlockEffectApplier;
import com.momosoftworks.coldsweat.config.ConfigSettings;
import com.momosoftworks.coldsweat.compat.CompatManager;
import com.momosoftworks.coldsweat.data.tag.ModBlockTags;
import net.minecraft.core.BlockPos;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.item.ItemEntity;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.Blocks;
import net.minecraft.world.level.block.CampfireBlock;
import net.minecraft.world.level.block.state.BlockState;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

@Mixin(CampfireBlock.class)
public class MixinSoulCampfire
{
    @Inject(method = "entityInside(Lnet/minecraft/world/level/block/state/BlockState;Lnet/minecraft/world/level/Level;Lnet/minecraft/core/BlockPos;Lnet/minecraft/world/entity/Entity;Lnet/minecraft/world/entity/InsideBlockEffectApplier;Z)V",
    at = @At("HEAD"), cancellable = true)
    public void entityInside(BlockState state, Level level, BlockPos pos, Entity entity, InsideBlockEffectApplier effectApplier, boolean isPrecise, CallbackInfo ci)
    {
        if (state.is(ModBlockTags.SOUL_FIRE) && state.getValue(CampfireBlock.LIT)
        && ConfigSettings.COLD_SOUL_FIRE.get() && !(entity instanceof ItemEntity && CompatManager.isSpiritLoaded()))
        {
            entity.setIsInPowderSnow(true);
            entity.clearFire();
            ci.cancel();
        }
    }
}
