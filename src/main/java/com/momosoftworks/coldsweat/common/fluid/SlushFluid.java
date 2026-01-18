package com.momosoftworks.coldsweat.common.fluid;

import com.momosoftworks.coldsweat.ColdSweat;
import com.momosoftworks.coldsweat.core.init.BlockInit;
import com.momosoftworks.coldsweat.core.init.FluidInit;
import com.momosoftworks.coldsweat.core.init.ItemInit;
import com.momosoftworks.coldsweat.core.init.SoundInit;
import com.momosoftworks.coldsweat.util.math.CSMath;
import com.momosoftworks.coldsweat.util.render.PackedColorHelper;
import com.momosoftworks.coldsweat.util.world.WorldHelper;
import net.minecraft.block.Block;
import net.minecraft.block.BlockState;
import net.minecraft.block.Blocks;
import net.minecraft.block.FlowingFluidBlock;
import net.minecraft.block.material.Material;
import net.minecraft.fluid.Fluid;
import net.minecraft.fluid.FluidState;
import net.minecraft.particles.BlockParticleData;
import net.minecraft.particles.ParticleTypes;
import net.minecraft.state.StateContainer;
import net.minecraft.tags.FluidTags;
import net.minecraft.util.*;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.vector.Vector3d;
import net.minecraft.world.IWorld;
import net.minecraft.world.IWorldReader;
import net.minecraft.world.World;
import net.minecraft.world.biome.BiomeColors;
import net.minecraftforge.event.ForgeEventFactory;
import net.minecraftforge.fluids.ForgeFlowingFluid;

public abstract class SlushFluid extends ForgeFlowingFluid
{
    private static final ResourceLocation SLUSH_STILL = new ResourceLocation(ColdSweat.MOD_ID, "block/slush_still");
    private static final ResourceLocation SLUSH_FLOW = new ResourceLocation(ColdSweat.MOD_ID, "block/slush_flow");

    protected SlushFluid(Properties properties)
    {   super(properties);
    }

    public static Block.Properties getBlockProperties()
    {
        return Block.Properties.of(Material.WATER)
                .noCollission()
                .randomTicks()
                .strength(100.0F);
    }

    public static ForgeFlowingFluid.Properties getForgeProperties()
    {
        return new ForgeFlowingFluid.Properties(FluidInit.SLUSH, FluidInit.FLOWING_SLUSH, ModFluidAttributes.builder(SLUSH_STILL, SLUSH_FLOW)
                    .color((state, level, pos, index) -> {
                        if (level == null || pos == null)
                        {   return 0xFFFFFFFF;
                        }
                        int color = BiomeColors.getAverageWaterColor(level, pos);
                        int red = PackedColorHelper.red(color);
                        int green = PackedColorHelper.green(color);
                        int blue = PackedColorHelper.blue(color);
                        int alphaColor = PackedColorHelper.color(240, red, green, blue);
                        int white = PackedColorHelper.color(240, 240, 255, 255);
                        return PackedColorHelper.mix(alphaColor, white, 0.65f);
                    })
                    .defaultColor(PackedColorHelper.color(240, 210, 240, 255))
                    .sound(SoundInit.BUCKET_FILL_SLUSH, SoundInit.BUCKET_EMPTY_SLUSH)
                    .translationKey("block.cold_sweat.slush")
                    .temperature(280)
                    .viscosity(2000))
                .bucket(ItemInit.SLUSH_BUCKET)
                .block(BlockInit.SLUSH);
    }

    @Override
    protected boolean canConvertToSource()
    {   return false;
    }

    @Override
    protected int getSlopeFindDistance(IWorldReader worldIn)
    {   return 2;
    }

    @Override
    protected int getDropOff(IWorldReader worldIn)
    {   return 2;
    }

    @Override
    public int getTickDelay(IWorldReader level)
    {   return 30;
    }

    @Override
    protected void spreadTo(IWorld level, BlockPos pos, BlockState blockState, Direction direction, FluidState fluidState)
    {
        FluidState targetFluidState = level.getFluidState(pos);

        if (targetFluidState.is(FluidTags.WATER))
        {
            BlockState resultBlock = Blocks.SNOW_BLOCK.defaultBlockState();

            if (blockState.getBlock() instanceof FlowingFluidBlock)
            {   level.setBlock(pos, ForgeEventFactory.fireFluidPlaceBlockEvent(level, pos, pos, resultBlock), 3);
            }
            fizz(level, pos);
            return;
        }
        super.spreadTo(level, pos, blockState, direction, fluidState);
    }

    public static void fizz(IWorld ilevel, BlockPos pos)
    {
        if (ilevel instanceof World)
        {
            World level = (World) ilevel;
            Vector3d centerPos = CSMath.getCenterPos(pos);
            BlockParticleData particleData = new BlockParticleData(ParticleTypes.BLOCK, Blocks.SNOW_BLOCK.defaultBlockState());
            WorldHelper.spawnParticleBatch(level, particleData, centerPos.x, centerPos.y, centerPos.z, 0.6, 0.6, 0.6, 15, 0.1);
            level.playSound(null, pos, SoundEvents.SNOW_BREAK, SoundCategory.BLOCKS, 1f, 1f);
        }
    }

    public static class Flowing extends SlushFluid
    {
        public Flowing(Properties properties)
        {   super(properties);
            registerDefaultState(getStateDefinition().any().setValue(LEVEL, 7));
        }

        protected void createFluidStateDefinition(StateContainer.Builder<Fluid, FluidState> builder)
        {   super.createFluidStateDefinition(builder);
            builder.add(LEVEL);
        }

        public int getAmount(FluidState state)
        {   return state.getValue(LEVEL);
        }

        public boolean isSource(FluidState state)
        {   return false;
        }
    }

    public static class Source extends SlushFluid
    {
        public Source(Properties properties)
        {   super(properties);
        }

        public int getAmount(FluidState state)
        {   return 8;
        }

        public boolean isSource(FluidState state)
        {   return true;
        }
    }
}