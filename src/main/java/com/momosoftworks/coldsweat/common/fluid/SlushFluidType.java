package com.momosoftworks.coldsweat.common.fluid;

import com.momosoftworks.coldsweat.ColdSweat;
import com.momosoftworks.coldsweat.util.math.CSMath;
import net.minecraft.client.renderer.BiomeColors;
import net.minecraft.core.BlockPos;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.util.FastColor;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.item.ItemEntity;
import net.minecraft.world.level.BlockAndTintGetter;
import net.minecraft.world.level.material.FluidState;
import net.minecraft.world.phys.Vec3;
import net.minecraftforge.client.extensions.common.IClientFluidTypeExtensions;
import net.minecraftforge.fluids.FluidType;

import java.util.function.Consumer;

public class SlushFluidType extends FluidType
{
    public SlushFluidType(Properties properties)
    {   super(properties);
    }

    @Override
    public double motionScale(Entity entity)
    {   return 0.00235D;
    }

    @Override
    public void setItemMovement(ItemEntity entity)
    {
        Vec3 vec3 = entity.getDeltaMovement();
        entity.setDeltaMovement(vec3.x * 0.95F, vec3.y + (vec3.y < 0.06F ? 5.0E-4F : 0.0F), vec3.z * 0.95F);
    }

    @Override
    public void initializeClient(Consumer<IClientFluidTypeExtensions> consumer)
    {
        consumer.accept(new IClientFluidTypeExtensions()
        {
            private static final ResourceLocation SLUSH_STILL = new ResourceLocation(ColdSweat.MOD_ID, "block/slush_still");
            private static final ResourceLocation SLUSH_FLOW = new ResourceLocation(ColdSweat.MOD_ID, "block/slush_flow");

            @Override
            public ResourceLocation getStillTexture()
            {   return SLUSH_STILL;
            }

            @Override
            public ResourceLocation getFlowingTexture()
            {   return SLUSH_FLOW;
            }

            @Override
            public int getTintColor()
            {   return FastColor.ARGB32.color(240, 210, 240, 255);
            }

            @Override
            public int getTintColor(FluidState state, BlockAndTintGetter getter, BlockPos pos)
            {
                int color = BiomeColors.getAverageWaterColor(getter, pos);
                int red = FastColor.ARGB32.red(color);
                int green = FastColor.ARGB32.green(color);
                int blue = FastColor.ARGB32.blue(color);
                int alphaColor = FastColor.ARGB32.color(240, red, green, blue);
                int white = FastColor.ARGB32.color(240, 255, 255, 255);
                return CSMath.blendColors(alphaColor, white, 0.5f);
            }
        });
    }
}
