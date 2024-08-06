package com.momosoftworks.coldsweat.mixin;

import com.momosoftworks.coldsweat.api.temperature.modifier.TempModifier;
import com.momosoftworks.coldsweat.api.temperature.modifier.compat.SereneSeasonsTempModifier;
import com.momosoftworks.coldsweat.config.ConfigSettings;
import com.momosoftworks.coldsweat.util.compat.CompatManager;
import com.momosoftworks.coldsweat.util.serialization.ObjectBuilder;
import com.momosoftworks.coldsweat.util.world.WorldHelper;
import net.minecraft.core.BlockPos;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.LevelReader;
import net.minecraft.world.level.biome.Biome;
import net.minecraft.world.level.block.Blocks;
import net.minecraft.world.level.block.LiquidBlock;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.material.FluidState;
import net.minecraft.world.level.material.Fluids;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

@Mixin(Biome.class)
public class MixinFreezingWater
{
    private final ThreadLocal<LevelReader> level = new ThreadLocal<>();
    private final ThreadLocal<Boolean> isCheckingFreezing = ThreadLocal.withInitial(() -> false);
    Biome self = (Biome) (Object) this;

    @Inject(method = "shouldFreeze(Lnet/minecraft/world/level/LevelReader;Lnet/minecraft/core/BlockPos;Z)Z",
            at = @At(value = "HEAD"), cancellable = true)
    private void shouldFreezeBlock(LevelReader levelReader, BlockPos pos, boolean mustBeAtEdge, CallbackInfoReturnable<Boolean> cir)
    {
        this.level.set(levelReader);
        this.isCheckingFreezing.set(true);

        if (!ConfigSettings.COLD_SOUL_FIRE.get())
        {   return;
        }
        BlockState blockstate = levelReader.getBlockState(pos);
        FluidState fluidstate = levelReader.getFluidState(pos);
        if (!(fluidstate.getType() == Fluids.WATER && blockstate.getBlock() instanceof LiquidBlock))
        {   return;
        }
        BlockPos.MutableBlockPos mutable = new BlockPos.MutableBlockPos();
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
        if (this.isCheckingFreezing.get() && this.level.get() instanceof Level level)
        {
            double biomeTemp = WorldHelper.getBiomeTemperatureAt(level, self, pos);
            if (CompatManager.isSereneSeasonsLoaded())
            {
                TempModifier modifier = ObjectBuilder.build(SereneSeasonsTempModifier::new);
                biomeTemp = modifier.apply(biomeTemp);
            }
            cir.setReturnValue((float) biomeTemp);
        }
        this.isCheckingFreezing.set(false);
    }
}
