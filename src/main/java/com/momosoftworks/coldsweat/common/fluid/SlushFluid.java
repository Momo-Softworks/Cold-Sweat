package com.momosoftworks.coldsweat.common.fluid;

import com.momosoftworks.coldsweat.core.init.ModBlocks;
import com.momosoftworks.coldsweat.core.init.ModFluids;
import com.momosoftworks.coldsweat.core.init.ModItems;
import com.momosoftworks.coldsweat.core.init.ModSounds;
import com.momosoftworks.coldsweat.util.registries.ModGameRules;
import com.momosoftworks.coldsweat.util.world.WorldHelper;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.core.particles.BlockParticleOption;
import net.minecraft.core.particles.ParticleTypes;
import net.minecraft.sounds.SoundEvents;
import net.minecraft.sounds.SoundSource;
import net.minecraft.tags.FluidTags;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.LevelAccessor;
import net.minecraft.world.level.block.Blocks;
import net.minecraft.world.level.block.LiquidBlock;
import net.minecraft.world.level.block.SoundType;
import net.minecraft.world.level.block.state.BlockBehaviour;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.block.state.StateDefinition;
import net.minecraft.world.level.material.Fluid;
import net.minecraft.world.level.material.FluidState;
import net.minecraft.world.level.material.MapColor;
import net.minecraft.world.level.material.PushReaction;
import net.minecraft.world.phys.Vec3;
import net.neoforged.neoforge.client.extensions.common.IClientFluidTypeExtensions;
import net.neoforged.neoforge.common.SoundActions;
import net.neoforged.neoforge.event.EventHooks;
import net.neoforged.neoforge.fluids.BaseFlowingFluid;
import net.neoforged.neoforge.fluids.FluidType;

public abstract class SlushFluid extends BaseFlowingFluid implements IClientFluidTypeExtensions
{
    protected SlushFluid(Properties properties)
    {   super(properties);
    }

    public static BlockBehaviour.Properties getBlockProperties()
    {
        return BlockBehaviour.Properties.of()
                .mapColor(MapColor.ICE)
                .replaceable()
                .noCollission()
                .randomTicks()
                .strength(100.0F)
                .pushReaction(PushReaction.DESTROY)
                .noLootTable()
                .liquid()
                .sound(SoundType.EMPTY);
    }

    public static FluidType.Properties getFluidProperties()
    {
        return FluidType.Properties.create()
                .descriptionId("block.cold_sweat.slush")
                .canSwim(false)
                .fallDistanceModifier(0.2f)
                .sound(SoundActions.BUCKET_FILL, ModSounds.BUCKET_FILL_SLUSH.value())
                .sound(SoundActions.BUCKET_EMPTY, ModSounds.BUCKET_EMPTY_SLUSH.value())
                .supportsBoating(true)
                .canExtinguish(true)
                .canHydrate(true);
    }

    public static BaseFlowingFluid.Properties getForgeProperties()
    {
        return new BaseFlowingFluid.Properties(ModFluids.SLUSH_TYPE, ModFluids.SLUSH, ModFluids.FLOWING_SLUSH)
                .slopeFindDistance(2)
                .levelDecreasePerBlock(2)
                .tickRate(30)
                .explosionResistance(100)
                .bucket(ModItems.SLUSH_BUCKET)
                .block(ModBlocks.SLUSH);
    }

    @Override
    public boolean canConvertToSource(FluidState state, Level level, BlockPos pos)
    {   return level.getGameRules().getBoolean(ModGameRules.RULE_SLUSH_SOURCE_CONVERSION);
    }

    @Override
    protected void spreadTo(LevelAccessor level, BlockPos pos, BlockState blockState, Direction direction, FluidState fluidState)
    {
        FluidState targetFluidState = level.getFluidState(pos);

        if (targetFluidState.is(FluidTags.WATER))
        {
            BlockState resultBlock = Blocks.SNOW_BLOCK.defaultBlockState();

            if (blockState.getBlock() instanceof LiquidBlock)
            {   level.setBlock(pos, EventHooks.fireFluidPlaceBlockEvent(level, pos, pos, resultBlock), 3);
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
            Vec3 centerPos = pos.getCenter();
            BlockParticleOption particleData = new BlockParticleOption(ParticleTypes.BLOCK, Blocks.SNOW_BLOCK.defaultBlockState());
            WorldHelper.spawnParticleBatch(level, particleData, centerPos.x, centerPos.y, centerPos.z, 0.6, 0.6, 0.6, 15, 0.1);
            level.playSound(null, pos, SoundEvents.SNOW_BREAK, SoundSource.BLOCKS);
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