package com.momosoftworks.coldsweat.mixin;

import com.momosoftworks.coldsweat.api.temperature.modifier.compat.SereneSeasonsTempModifier;
import com.momosoftworks.coldsweat.config.ConfigSettings;
import com.momosoftworks.coldsweat.util.compat.CompatManager;
import com.momosoftworks.coldsweat.util.world.WorldHelper;
import net.minecraft.block.BlockState;
import net.minecraft.block.Blocks;
import net.minecraft.fluid.FluidState;
import net.minecraft.fluid.Fluids;
import net.minecraft.util.math.BlockPos;
import net.minecraft.world.IWorld;
import net.minecraft.world.IWorldReader;
import net.minecraft.world.World;
import net.minecraft.world.biome.Biome;
import net.minecraftforge.fluids.IFluidBlock;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.Redirect;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

@Mixin(Biome.class)
public abstract class MixinFreezingWater
{
    @Shadow public abstract float getTemperature(BlockPos pPos);

    private static IWorldReader LEVEL = null;

    @Inject(method = "shouldFreeze(Lnet/minecraft/world/IWorldReader;Lnet/minecraft/util/math/BlockPos;Z)Z",
            at = @At(value = "HEAD"), cancellable = true)
    private void shouldFreezeBlock(IWorldReader level, BlockPos pos, boolean onlyAtEdge, CallbackInfoReturnable<Boolean> cir)
    {
        LEVEL = level;
        if (!ConfigSettings.COLD_SOUL_FIRE.get())
        {   return;
        }
        BlockState blockstate = level.getBlockState(pos);
        FluidState fluidstate = level.getFluidState(pos);
        if (!(fluidstate.getType() == Fluids.WATER && blockstate.getBlock() instanceof IFluidBlock))
        {   return;
        }
        BlockPos.Mutable mutable = new BlockPos.Mutable();
        for (int x = -1; x <= 1; x++)
        {
            for (int y = -1; y <= 1; y++)
            {
                for (int z = -1; z <= 1; z++)
                {
                    mutable.set(pos).move(x, y, z);
                    if (!(mutable.getX() == pos.getX() || mutable.getZ() == pos.getZ()))
                    {   continue;
                    }
                    if (level.getBlockState(mutable).is(Blocks.SOUL_FIRE))
                    {   cir.setReturnValue(true);
                    }
                }
            }
        }
    }

    @Redirect(method = "shouldFreeze(Lnet/minecraft/world/IWorldReader;Lnet/minecraft/util/math/BlockPos;Z)Z",
              at = @At(value = "INVOKE", target = "Lnet/minecraft/world/biome/Biome;getTemperature(Lnet/minecraft/util/math/BlockPos;)F"))
    private float warmEnoughToRain(Biome biome, BlockPos pos)
    {
        if (LEVEL instanceof World)
        {
            World level = (World) LEVEL;
            double biomeTemp = WorldHelper.getBiomeTemperatureAt(level, biome, pos);
            if (CompatManager.isSereneSeasonsLoaded())
            {   biomeTemp += new SereneSeasonsTempModifier().apply(0);
            }
            return (float) biomeTemp;
        }
        return getTemperature(pos);
    }
}
