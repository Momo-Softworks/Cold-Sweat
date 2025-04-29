package com.momosoftworks.coldsweat.common.block;

import com.momosoftworks.coldsweat.core.itemgroup.ColdSweatGroup;
import com.momosoftworks.coldsweat.config.ConfigSettings;
import com.momosoftworks.coldsweat.data.tag.ModBlockTags;
import com.momosoftworks.coldsweat.util.math.CSMath;
import com.momosoftworks.coldsweat.util.world.WorldHelper;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.util.RandomSource;
import net.minecraft.util.StringRepresentable;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.context.BlockPlaceContext;
import net.minecraft.world.level.BlockGetter;
import net.minecraft.world.level.LevelAccessor;
import net.minecraft.world.level.LevelReader;
import net.minecraft.world.level.block.*;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.block.state.StateDefinition;
import net.minecraft.world.level.block.state.properties.BlockStateProperties;
import net.minecraft.world.level.block.state.properties.EnumProperty;
import net.minecraft.world.level.block.state.properties.IntegerProperty;
import net.minecraft.world.level.material.Material;
import net.minecraft.world.phys.shapes.CollisionContext;
import net.minecraft.world.phys.shapes.VoxelShape;
import net.minecraftforge.common.ForgeHooks;
import net.minecraftforge.common.IPlantable;
import org.jetbrains.annotations.Nullable;

public class SoulStalkBlock extends Block implements IPlantable
{
    public static final IntegerProperty AGE = BlockStateProperties.AGE_15;
    public static final EnumProperty<Section> SECTION = EnumProperty.create("section", Section.class);
    protected static final VoxelShape SHAPE = Block.box(3.0D, 0.0D, 3.0D, 13.0D, 16.0D, 13.0D);
    protected static final VoxelShape SHAPE_BUD = Block.box(4.5D, 0.0D, 4.5D, 11.5D, 14.0D, 11.5D);

    public SoulStalkBlock(Properties p_49795_)
    {
        super(p_49795_);
        this.registerDefaultState(this.defaultBlockState().setValue(AGE, 0).setValue(SECTION, Section.BUD));
    }

    public static Properties getProperties()
    {
        return Properties
                .of(Material.PLANT)
                .sound(SoundType.BIG_DRIPLEAF)
                .strength(0f, 0.5f)
                .randomTicks()
                .lightLevel(state -> state.getValue(SECTION).hasFruit() ? 4 : 0)
                .noOcclusion()
                .noCollission();
    }

    public static Item.Properties getItemProperties()
    {
        return new Item.Properties().tab(ColdSweatGroup.COLD_SWEAT);
    }

    public static Section getRandomMidsection()
    {   return Math.random() < 0.3 ? Section.MIDDLE_SPROUT : Section.MIDDLE;
    }

    @Override
    public void randomTick(BlockState state, ServerLevel level, BlockPos pos, RandomSource rand)
    {
        if (level.isEmptyBlock(pos.above()))
        {
            // Get the height of the plant
            int height;
            for(height = 1; level.getBlockState(pos.below(height)).getBlock() == this; ++height)
            {}

            double minTemp = ConfigSettings.MIN_TEMP.get();
            double maxTemp = ConfigSettings.MAX_TEMP.get();
            double tempAtBase = WorldHelper.getRoughTemperatureAt(level, pos.below(height - 1));

            if (height < 6 && rand.nextDouble() < 0.05 + CSMath.blend(0, 0.95, tempAtBase, minTemp, maxTemp))
            {
                int age = state.getValue(AGE);
                Section section = state.getValue(SECTION);
                if (ForgeHooks.onCropsGrowPre(level, pos, state, true))
                {
                    if (age >= 6)
                    {
                        // Growing taller
                        if (section == Section.TOP)
                        {
                            level.setBlockAndUpdate(pos.above(), this.defaultBlockState().setValue(SECTION, Section.TOP));
                            level.setBlock(pos, state.setValue(AGE, 0).setValue(SECTION, getRandomMidsection()), 4);
                        }
                        // Growing from a bud
                        else if (section == Section.BUD)
                        {
                            level.setBlockAndUpdate(pos, this.defaultBlockState().setValue(AGE, 0).setValue(SECTION, Section.BASE));
                            level.setBlockAndUpdate(pos.above(), this.defaultBlockState().setValue(AGE, 0).setValue(SECTION, Section.TOP));
                        }
                    }
                    else level.setBlock(pos, state.setValue(AGE, age + 1), 4);
                }
            }
        }
    }

    @Nullable
    @Override
    public BlockState getStateForPlacement(BlockPlaceContext context)
    {
        BlockPos pos = context.getClickedPos();
        return context.getLevel().getBlockState(pos.below()).getBlock() == this
               ? this.defaultBlockState().setValue(SECTION, Section.TOP)
             : this.canSurvive(this.defaultBlockState(), context.getLevel(), pos)
               ? this.defaultBlockState()
             : null;
    }

    @Override
    public BlockState updateShape(BlockState state, Direction direction, BlockState otherState, LevelAccessor level, BlockPos pos, BlockPos otherPos)
    {
        if (!this.canSurvive(state, level, pos))
        {   return Blocks.AIR.defaultBlockState();
        }

        if (direction == Direction.UP)
        {
            if (otherState.getBlock() != this)
            {   return Blocks.AIR.defaultBlockState();
            }
            else
            {
                switch (state.getValue(SECTION))
                {
                    case TOP -> {
                        return state.setValue(SECTION, getRandomMidsection());
                    }
                    case BUD -> {
                        return state.setValue(SECTION, Section.BASE);
                    }
                }
            }
        }
        return state;
    }

    @Override
    public VoxelShape getShape(BlockState state, BlockGetter getter, BlockPos pos, CollisionContext context)
    {
        return state.getValue(SECTION) == Section.BUD ? SHAPE_BUD : SHAPE;
    }

    @Override
    protected void createBlockStateDefinition(StateDefinition.Builder<Block, BlockState> builder)
    {   builder.add(AGE, SECTION);
    }

    @Override
    public BlockState getPlant(BlockGetter level, BlockPos pos)
    {   return this.defaultBlockState();
    }

    @Override
    public boolean canSurvive(BlockState state, LevelReader level, BlockPos pos)
    {   BlockState below = level.getBlockState(pos.below());
        return below.is(ModBlockTags.SOUL_STALK_PLACEABLE_ON) || below.getBlock() == this;
    }

    public enum Section implements StringRepresentable
    {
        BASE("base", false),
        MIDDLE("middle", false),
        MIDDLE_SPROUT("middle_sprout", true),
        TOP("top", true),
        BUD("bud", true);

        private final String name;
        private final boolean hasSprout;

        Section(String name, boolean hasSprout)
        {   this.name = name;
            this.hasSprout = hasSprout;
        }

        public boolean hasFruit()
        {   return hasSprout;
        }

        @Override
        public String getSerializedName()
        {   return name;
        }
    }
}
