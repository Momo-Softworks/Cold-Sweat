package com.momosoftworks.coldsweat.common.fluid;

import com.momosoftworks.coldsweat.ColdSweat;
import com.momosoftworks.coldsweat.core.init.BlockInit;
import com.momosoftworks.coldsweat.core.init.FluidInit;
import com.momosoftworks.coldsweat.core.init.ItemInit;
import com.momosoftworks.coldsweat.core.init.SoundInit;
import com.momosoftworks.coldsweat.util.math.CSMath;
import com.momosoftworks.coldsweat.util.registries.ModSounds;
import com.momosoftworks.coldsweat.util.world.WorldHelper;
import net.minecraft.client.renderer.BiomeColors;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.core.particles.BlockParticleOption;
import net.minecraft.core.particles.ParticleTypes;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.sounds.SoundEvents;
import net.minecraft.sounds.SoundSource;
import net.minecraft.tags.FluidTags;
import net.minecraft.util.FastColor;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.LevelAccessor;
import net.minecraft.world.level.LevelReader;
import net.minecraft.world.level.block.Blocks;
import net.minecraft.world.level.block.LiquidBlock;
import net.minecraft.world.level.block.state.BlockBehaviour;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.block.state.StateDefinition;
import net.minecraft.world.level.material.Fluid;
import net.minecraft.world.level.material.FluidState;
import net.minecraft.world.level.material.Material;
import net.minecraft.world.phys.Vec3;
import net.minecraftforge.event.ForgeEventFactory;
import net.minecraftforge.fluids.ForgeFlowingFluid;

public abstract class SlushFluid extends ForgeFlowingFluid
{
    private static final ResourceLocation SLUSH_STILL = new ResourceLocation(ColdSweat.MOD_ID, "block/slush_still");
    private static final ResourceLocation SLUSH_FLOW = new ResourceLocation(ColdSweat.MOD_ID, "block/slush_flow");

    protected SlushFluid(Properties properties)
    {   super(properties);
    }

    public static BlockBehaviour.Properties getBlockProperties()
    {
        return BlockBehaviour.Properties.of(Material.WATER)
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
                        int red = FastColor.ARGB32.red(color);
                        int green = FastColor.ARGB32.green(color);
                        int blue = FastColor.ARGB32.blue(color);
                        int alphaColor = FastColor.ARGB32.color(240, red, green, blue);
                        int white = FastColor.ARGB32.color(240, 240, 255, 255);
                        return CSMath.blendColors(alphaColor, white, 0.65f);
                    })
                    .defaultColor(FastColor.ARGB32.color(240, 210, 240, 255))
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
    protected int getSlopeFindDistance(LevelReader worldIn)
    {   return 2;
    }

    @Override
    protected int getDropOff(LevelReader worldIn)
    {   return 2;
    }

    @Override
    public int getTickDelay(LevelReader level)
    {   return 30;
    }

    @Override
    protected void spreadTo(LevelAccessor level, BlockPos pos, BlockState blockState, Direction direction, FluidState fluidState)
    {
        FluidState targetFluidState = level.getFluidState(pos);

        if (targetFluidState.is(FluidTags.WATER))
        {
            BlockState resultBlock = Blocks.SNOW_BLOCK.defaultBlockState();

            if (blockState.getBlock() instanceof LiquidBlock)
            {   level.setBlock(pos, ForgeEventFactory.fireFluidPlaceBlockEvent(level, pos, pos, resultBlock), 3);
            }
            fizz(level, pos);
            return;
        }
        super.spreadTo(level, pos, blockState, direction, fluidState);
    }

    public static void fizz(LevelAccessor ilevel, BlockPos pos)
    {
        if (ilevel instanceof Level level)
        {
            Vec3 centerPos = CSMath.getCenterPos(pos);
            BlockParticleOption particleData = new BlockParticleOption(ParticleTypes.BLOCK, Blocks.SNOW_BLOCK.defaultBlockState());
            WorldHelper.spawnParticleBatch(level, particleData, centerPos.x, centerPos.y, centerPos.z, 0.6, 0.6, 0.6, 15, 0.1);
            level.playSound(null, pos, SoundEvents.SNOW_BREAK, SoundSource.BLOCKS, 1f, 1f);
        }
    }

    public static class Flowing extends SlushFluid
    {
        public Flowing(Properties properties)
        {   super(properties);
            registerDefaultState(getStateDefinition().any().setValue(LEVEL, 7));
        }

        protected void createFluidStateDefinition(StateDefinition.Builder<Fluid, FluidState> builder)
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