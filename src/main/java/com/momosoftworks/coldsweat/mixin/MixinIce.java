package com.momosoftworks.coldsweat.mixin;

import net.minecraft.core.BlockPos;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.*;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraft.world.level.block.state.BlockBehaviour;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.block.state.properties.NoteBlockInstrument;
import net.minecraft.world.level.material.Material;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.Redirect;
import org.spongepowered.asm.mixin.injection.Slice;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

public class MixinIce
{
    @Mixin(IceBlock.class)
    public static class NoWaterOnBreak
    {
        @Inject(method = "playerDestroy",
                at = @At(value = "INVOKE", target = "Lnet/minecraft/world/level/Level;getBlockState(Lnet/minecraft/core/BlockPos;)Lnet/minecraft/world/level/block/state/BlockState;"),
                cancellable = true)
        private void noWater(Level pLevel, Player pPlayer, BlockPos pPos, BlockState pState, BlockEntity pTe, ItemStack pStack, CallbackInfo ci)
        {
            ci.cancel();
        }
    }

    @Mixin(Blocks.class)
    public static class RequireTool
    {
        @Redirect(
                method = "<clinit>",
                at = @At(
                        value = "NEW",
                        target = "Lnet/minecraft/world/level/block/IceBlock;",
                        ordinal = 0
                ),
                slice = @Slice(
                        from = @At(value = "CONSTANT", args = "stringValue=ice")
                )
        )
        private static IceBlock redirectIceBlockConstructor(BlockBehaviour.Properties properties)
        {
            return new IceBlock(
                    BlockBehaviour.Properties.of(Material.ICE)
                            .friction(0.98F)
                            .randomTicks()
                            .strength(0.2F)
                            .sound(SoundType.GLASS)
                            .noOcclusion()
                            .isValidSpawn((state, level, pos, entity) -> entity == EntityType.POLAR_BEAR)
                            .isRedstoneConductor((a,b,c) -> false)
                            // Custom properties
                            .requiresCorrectToolForDrops()
            );
        }

        @Redirect(
                method = "<clinit>",
                at = @At(
                        value = "NEW",
                        target = "Lnet/minecraft/world/level/block/Block;",
                        ordinal = 0
                ),
                slice = @Slice(
                        from = @At(value = "CONSTANT", args = "stringValue=packed_ice")
                )
        )
        private static Block redirectPackedIceBlockConstructor(BlockBehaviour.Properties properties)
        {
            return new Block(
                    BlockBehaviour.Properties.of(Material.ICE_SOLID)
                            .friction(0.98F)
                            .strength(0.5F)
                            .sound(SoundType.GLASS)
                            // Custom properties
                            .requiresCorrectToolForDrops()
            );
        }

        @Redirect(
                method = "<clinit>",
                at = @At(
                        value = "NEW",
                        target = "Lnet/minecraft/world/level/block/HalfTransparentBlock;",
                        ordinal = 0
                ),
                slice = @Slice(
                        from = @At(value = "CONSTANT", args = "stringValue=blue_ice")
                )
        )
        private static HalfTransparentBlock redirectBlueIceBlockConstructor(BlockBehaviour.Properties properties)
        {
            return new HalfTransparentBlock(
                    BlockBehaviour.Properties.of(Material.ICE_SOLID)
                            .strength(0.8F)
                            .friction(0.989F)
                            .sound(SoundType.GLASS)
                            // Custom properties
                            .requiresCorrectToolForDrops()
            );
        }
    }
}
