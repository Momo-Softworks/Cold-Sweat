package com.momosoftworks.coldsweat.common.fluid;

import net.minecraft.client.renderer.color.IBlockColor;
import net.minecraft.fluid.Fluid;
import net.minecraft.util.ResourceLocation;
import net.minecraft.util.math.BlockPos;
import net.minecraft.world.IBlockDisplayReader;
import net.minecraftforge.fluids.FluidAttributes;

import java.util.function.BiFunction;

/**
 * Copy of FluidAttributes that allows for custom color getters.
 */
public class ModFluidAttributes extends FluidAttributes
{
    private final IBlockColor colorGetter;

    public ModFluidAttributes(CustomBuilder builder, Fluid fluid)
    {   super(builder, fluid);
        this.colorGetter = builder.colorGetter;
    }

    @Override
    public int getColor(IBlockDisplayReader level, BlockPos pos)
    {   return this.colorGetter.getColor(level.getBlockState(pos), level, pos, 0);
    }

    public static CustomBuilder builder(ResourceLocation stillTexture, ResourceLocation flowingTexture)
    {   return new CustomBuilder(stillTexture, flowingTexture, ModFluidAttributes::new);
    }

    public static class CustomBuilder extends Builder
    {
        private IBlockColor colorGetter = (state, level, pos, index) -> 0xFFFFFFFF;

        protected CustomBuilder(ResourceLocation stillTexture, ResourceLocation flowingTexture, BiFunction<CustomBuilder, Fluid, ModFluidAttributes> factory)
        {
            super(stillTexture, flowingTexture, (builder, fluid) ->
            {
                if (builder instanceof CustomBuilder) return factory.apply(((CustomBuilder) builder), fluid);
                else return null;
            });
        }

        public final CustomBuilder color(IBlockColor colorGetter)
        {
            this.colorGetter = colorGetter;
            return this;
        }
    }
}
