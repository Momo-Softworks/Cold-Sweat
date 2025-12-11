package com.momosoftworks.coldsweat.common.fluid;

import net.minecraft.core.BlockPos;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.sounds.SoundEvent;
import net.minecraft.world.level.BlockAndTintGetter;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.material.Fluid;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.api.distmarker.OnlyIn;
import net.minecraftforge.fluids.FluidAttributes;

import javax.annotation.Nullable;
import java.util.function.BiFunction;
import java.util.function.Supplier;

/**
 * Copy of FluidAttributes that allows for custom color getters.
 */
public class ModFluidAttributes extends FluidAttributes
{
    private final BlockColor colorGetter;
    private final int defaultColor;
    private final Supplier<SoundEvent> fillSound;
    private final Supplier<SoundEvent> emptySound;

    public ModFluidAttributes(CustomBuilder builder, Fluid fluid)
    {   super(builder, fluid);
        this.colorGetter = builder.colorGetter;
        this.defaultColor = builder.defaultColor;
        this.fillSound = builder.fillSound;
        this.emptySound = builder.emptySound;
    }

    @Override
    public int getColor()
    {   return this.defaultColor;
    }

    @Override
    public int getColor(BlockAndTintGetter level, BlockPos pos)
    {   return this.colorGetter.getColor(level.getBlockState(pos), level, pos, 0);
    }

    @Override
    public SoundEvent getEmptySound()
    {   return this.emptySound != null ? this.emptySound.get() : super.getEmptySound();
    }
    @Override
    public SoundEvent getFillSound()
    {   return this.fillSound != null ? this.fillSound.get() : super.getFillSound();
    }

    public static CustomBuilder builder(ResourceLocation stillTexture, ResourceLocation flowingTexture)
    {   return new CustomBuilder(stillTexture, flowingTexture, ModFluidAttributes::new);
    }

    public static class CustomBuilder extends Builder
    {
        private BlockColor colorGetter = (state, level, pos, index) -> 0xFFFFFFFF;
        private int defaultColor = 0xFFFFFFFF;
        private Supplier<SoundEvent> fillSound;
        private Supplier<SoundEvent> emptySound;

        protected CustomBuilder(ResourceLocation stillTexture, ResourceLocation flowingTexture, BiFunction<CustomBuilder, Fluid, ModFluidAttributes> factory)
        {
            super(stillTexture, flowingTexture, (builder, fluid) ->
            {
                if (builder instanceof CustomBuilder customBuilder) return factory.apply(customBuilder, fluid);
                else return null;
            });
        }

        public CustomBuilder color(BlockColor colorGetter)
        {
            this.colorGetter = colorGetter;
            return this;
        }

        public CustomBuilder defaultColor(int color)
        {
            this.defaultColor = color;
            return this;
        }

        public CustomBuilder sound(Supplier<SoundEvent> fillSound, Supplier<SoundEvent> emptySound)
        {
            this.fillSound = fillSound;
            this.emptySound = emptySound;
            return this;
        }
    }

    public interface BlockColor
    {
        int getColor(BlockState pState, @Nullable BlockAndTintGetter pLevel, @Nullable BlockPos pPos, int pTintIndex);

        @OnlyIn(Dist.CLIENT)
        default net.minecraft.client.color.block.BlockColor toMinecraft()
        {
            return new net.minecraft.client.color.block.BlockColor()
            {
                @Override
                public int getColor(BlockState state, BlockAndTintGetter level, BlockPos pos, int tintIndex)
                {   return this.getColor(state, level, pos, tintIndex);
                }
            };
        }
    }
}
