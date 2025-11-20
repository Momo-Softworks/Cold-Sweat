package com.momosoftworks.coldsweat.common.fluid;

import net.minecraft.block.BlockState;
import net.minecraft.client.renderer.color.IBlockColor;
import net.minecraft.fluid.Fluid;
import net.minecraft.util.ResourceLocation;
import net.minecraft.util.math.BlockPos;
import net.minecraft.world.IBlockDisplayReader;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.api.distmarker.OnlyIn;
import net.minecraftforge.fluids.FluidAttributes;

import javax.annotation.Nullable;
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
    public int getColor(IBlockDisplayReader level, BlockPos pos)
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
                if (builder instanceof CustomBuilder) return factory.apply(((CustomBuilder) builder), fluid);
                else return null;
            });
        }

        public final CustomBuilder color(BlockColor colorGetter)
        {
            this.colorGetter = colorGetter;
            return this;
        }
    }

    public interface BlockColor
    {
        int getColor(BlockState state, @Nullable IBlockDisplayReader level, @Nullable BlockPos pos, int tintIndex);

        @OnlyIn(Dist.CLIENT)
        default IBlockColor toMinecraft()
        {
            return new IBlockColor()
            {
                @Override
                public int getColor(BlockState state, IBlockDisplayReader level, BlockPos pos, int tintIndex)
                {   return this.getColor(state, level, pos, tintIndex);
                }
            };
        }
    }
}
