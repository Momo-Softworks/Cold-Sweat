package com.momosoftworks.coldsweat.mixin;

import net.minecraft.block.*;
import net.minecraft.block.material.Material;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.item.ItemStack;
import net.minecraft.tileentity.TileEntity;
import net.minecraft.util.math.BlockPos;
import net.minecraft.world.World;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.Redirect;
import org.spongepowered.asm.mixin.injection.Slice;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

import static net.minecraft.entity.EntityType.POLAR_BEAR;

public class MixinIce
{
    @Mixin(IceBlock.class)
    public static class NoWaterOnBreak
    {
        @Inject(method = "playerDestroy",
                at = @At(value = "INVOKE", target = "Lnet/minecraft/world/World;getBlockState(Lnet/minecraft/util/math/BlockPos;)Lnet/minecraft/block/BlockState;"),
                cancellable = true)
        private void noWater(World pLevel, PlayerEntity pPlayer, BlockPos pPos, BlockState pState, TileEntity pTe, ItemStack pStack, CallbackInfo ci)
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
                        target = "(Lnet/minecraft/block/AbstractBlock$Properties;)Lnet/minecraft/block/IceBlock;",
                        ordinal = 0
                ),
                slice = @Slice(
                        from = @At(value = "CONSTANT", args = "stringValue=ice")
                )
        )
        private static IceBlock redirectIceBlockConstructor(Block.Properties properties)
        {
            return new IceBlock(
                    Block.Properties.of(Material.ICE)
                            .friction(0.98F)
                            .randomTicks()
                            .strength(0.2F)
                            .sound(SoundType.GLASS)
                            .noOcclusion()
                            .isValidSpawn((state, level, pos, entity) -> entity == POLAR_BEAR)
                            .isRedstoneConductor((a,b,c) -> false)
                            // Custom properties
                            .requiresCorrectToolForDrops()
            );
        }

        @Redirect(
                method = "<clinit>",
                at = @At(
                        value = "NEW",
                        target = "(Lnet/minecraft/block/AbstractBlock$Properties;)Lnet/minecraft/block/Block;",
                        ordinal = 0
                ),
                slice = @Slice(
                        from = @At(value = "CONSTANT", args = "stringValue=packed_ice")
                )
        )
        private static Block redirectPackedIceBlockConstructor(Block.Properties properties)
        {
            return new Block(
                    Block.Properties.of(Material.ICE_SOLID)
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
                        target = "(Lnet/minecraft/block/AbstractBlock$Properties;)Lnet/minecraft/block/BreakableBlock;",
                        ordinal = 0
                ),
                slice = @Slice(
                        from = @At(value = "CONSTANT", args = "stringValue=blue_ice")
                )
        )
        private static BreakableBlock redirectBlueIceBlockConstructor(Block.Properties properties)
        {
            return new BreakableBlock(
                    Block.Properties.of(Material.ICE_SOLID)
                            .strength(0.8F)
                            .friction(0.989F)
                            .sound(SoundType.GLASS)
                            // Custom properties
                            .requiresCorrectToolForDrops()
            );
        }
    }
}
