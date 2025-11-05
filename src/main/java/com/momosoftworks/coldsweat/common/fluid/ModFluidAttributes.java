package com.momosoftworks.coldsweat.common.fluid;

import net.minecraft.client.color.block.BlockColor;
import net.minecraft.core.BlockPos;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.level.BlockAndTintGetter;
import net.minecraft.world.level.material.Fluid;
import net.minecraftforge.fluids.FluidAttributes;

import java.util.function.BiFunction;

/**
 * Copy of FluidAttributes that allows for custom color getters.
 */
public class ModFluidAttributes extends FluidAttributes
{
    private final BlockColor colorGetter;

    public ModFluidAttributes(CustomBuilder builder, Fluid fluid)
    {   super(builder, fluid);
        this.colorGetter = builder.colorGetter;
    }

    @Override
    public int getColor(BlockAndTintGetter level, BlockPos pos)
    {   return this.colorGetter.getColor(level.getBlockState(pos), level, pos, 0);
    }

    public static CustomBuilder builder(ResourceLocation stillTexture, ResourceLocation flowingTexture)
    {   return new CustomBuilder(stillTexture, flowingTexture, ModFluidAttributes::new);
    }

    public static class CustomBuilder extends Builder
    {
        private BlockColor colorGetter = (state, level, pos, index) -> 0xFFFFFFFF;

        protected CustomBuilder(ResourceLocation stillTexture, ResourceLocation flowingTexture, BiFunction<CustomBuilder, Fluid, ModFluidAttributes> factory)
        {
            super(stillTexture, flowingTexture, (builder, fluid) ->
            {
                if (builder instanceof CustomBuilder customBuilder) return factory.apply(customBuilder, fluid);
                else return null;
            });
        }

        public final CustomBuilder color(BlockColor colorGetter)
        {
            this.colorGetter = colorGetter;
            return this;
        }
    }
}
