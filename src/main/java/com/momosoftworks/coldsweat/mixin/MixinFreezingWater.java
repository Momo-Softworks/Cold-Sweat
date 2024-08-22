package com.momosoftworks.coldsweat.mixin;

import com.momosoftworks.coldsweat.api.temperature.modifier.TempModifier;
import com.momosoftworks.coldsweat.api.temperature.modifier.compat.SereneSeasonsTempModifier;
import com.momosoftworks.coldsweat.config.ConfigSettings;
import com.momosoftworks.coldsweat.util.compat.CompatManager;
import com.momosoftworks.coldsweat.util.serialization.ObjectBuilder;
import com.momosoftworks.coldsweat.util.world.WorldHelper;
import net.minecraft.block.BlockState;
import net.minecraft.block.Blocks;
import net.minecraft.fluid.FluidState;
import net.minecraft.fluid.Fluids;
import net.minecraft.util.math.BlockPos;
import net.minecraft.world.IWorldReader;
import net.minecraft.world.World;
import net.minecraft.world.biome.Biome;
import net.minecraftforge.fluids.IFluidBlock;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

@Mixin(Biome.class)
public abstract class MixinFreezingWater
{
    private static IWorldReader LEVEL = null;
    private static Boolean IS_CHECKING_FREEZING = false;
    Biome self = (Biome) (Object) this;

    @Inject(method = "shouldFreeze(Lnet/minecraft/world/IWorldReader;Lnet/minecraft/util/math/BlockPos;Z)Z",
            at = @At(value = "HEAD"), cancellable = true)
    private void shouldFreezeBlock(IWorldReader levelReader, BlockPos pos, boolean mustBeAtEdge, CallbackInfoReturnable<Boolean> cir)
    {
        LEVEL = levelReader;
        IS_CHECKING_FREEZING = true;

        if (!ConfigSettings.COLD_SOUL_FIRE.get())
        {   return;
        }
        BlockState blockstate = levelReader.getBlockState(pos);
        FluidState fluidstate = levelReader.getFluidState(pos);
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
                    if (levelReader.getBlockState(mutable).is(Blocks.SOUL_FIRE))
                    {   cir.setReturnValue(true);
                    }
                }
            }
        }
    }

    @Inject(method = "getTemperature", at = @At("HEAD"), cancellable = true)
    private void getTemperature(BlockPos pos, CallbackInfoReturnable<Float> cir)
    {
        if (IS_CHECKING_FREEZING && LEVEL instanceof World)
        {
            World world = ((World) LEVEL);
            double biomeTemp = WorldHelper.getBiomeTemperatureAt(world, self, pos);
            if (CompatManager.isSereneSeasonsLoaded())
            {
                TempModifier modifier = ObjectBuilder.build(SereneSeasonsTempModifier::new);
                biomeTemp = modifier.apply(biomeTemp);
            }
            cir.setReturnValue((float) biomeTemp);
        }
        IS_CHECKING_FREEZING = false;
    }
}
