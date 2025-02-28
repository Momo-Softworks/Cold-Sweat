package com.momosoftworks.coldsweat.common.block;

import com.momosoftworks.coldsweat.core.itemgroup.ColdSweatGroup;
import com.momosoftworks.coldsweat.data.tag.ModBlockTags;
import com.momosoftworks.coldsweat.data.tag.ModItemTags;
import net.minecraft.block.Block;
import net.minecraft.block.BlockState;
import net.minecraft.block.SoundType;
import net.minecraft.block.material.Material;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.item.BlockItemUseContext;
import net.minecraft.item.Item;
import net.minecraft.item.ItemStack;
import net.minecraft.state.BooleanProperty;
import net.minecraft.state.EnumProperty;
import net.minecraft.state.StateContainer;
import net.minecraft.util.*;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.BlockRayTraceResult;
import net.minecraft.util.math.shapes.ISelectionContext;
import net.minecraft.util.math.shapes.VoxelShape;
import net.minecraft.world.IBlockReader;
import net.minecraft.world.IWorld;
import net.minecraft.world.World;

public class SmokestackBlock extends Block
{
    public static final EnumProperty<Facing> FACING = EnumProperty.create("facing", Facing.class);
    public static final BooleanProperty END = BooleanProperty.create("end");
    public static final BooleanProperty BASE = BooleanProperty.create("base");
    public static final BooleanProperty ENCASED = BooleanProperty.create("encased");

    public SmokestackBlock(Block.Properties properties)
    {   super(properties);
        this.registerDefaultState(this.defaultBlockState().setValue(FACING, Facing.UP).setValue(END, false).setValue(BASE, false).setValue(ENCASED, false));
    }

    public static Properties getProperties()
    {
        return Properties
                .of(Material.STONE)
                .sound(SoundType.STONE)
                .strength(2f, 10f)
                .requiresCorrectToolForDrops()
                .dynamicShape();
    }

    public static Item.Properties getItemProperties()
    {   return new Item.Properties().tab(ColdSweatGroup.COLD_SWEAT);
    }

    @Override
    public boolean propagatesSkylightDown(BlockState state, IBlockReader level, BlockPos pos)
    {   return state.getValue(FACING) != Facing.JUNCTION;
    }

    @Override
    public VoxelShape getShape(BlockState state, IBlockReader getter, BlockPos pos, ISelectionContext context)
    {
        switch (state.getValue(FACING))
        {
            case UP:
            case DOWN:
                return Block.box(4, 0, 4, 12, 16, 12);
            case NORTH:
            case SOUTH:
                return Block.box(4, 4, 0, 12, 12, 16);
            case EAST:
            case WEST:
                return Block.box(0, 4, 4, 16, 12, 12);
            case JUNCTION:
                return Block.box(0, 0, 0, 16, 16, 16);
        }
        return null;
    }

    @Override
    public BlockState rotate(BlockState state, Rotation rotation)
    {   return state.setValue(FACING, state.getValue(FACING).rotate(rotation));
    }

    @Override
    public BlockState mirror(BlockState state, Mirror mirror)
    {   return state.setValue(FACING, state.getValue(FACING).mirror(mirror));
    }

    @Override
    protected void createBlockStateDefinition(StateContainer.Builder<Block, BlockState> builder)
    {   builder.add(FACING, END, BASE, ENCASED);
    }

    protected Facing calculateFacing(Facing facing, BlockPos pos, IWorld level)
    {
        Direction newDir = null;
        for (Direction dir : Direction.values())
        {
            BlockState neighborState = level.getBlockState(pos.relative(dir));
            if (neighborState.is(ModBlockTags.CONNECTS_SMOKESTACK)
            || dir == Direction.DOWN && neighborState.is(ModBlockTags.THERMAL_SOURCE))
            {
                if (newDir != null && newDir.getAxis() != dir.getAxis())
                {   return Facing.JUNCTION;
                }
                newDir = dir;
            }
        }
        if (newDir != null)
        {
            BlockState neighbor = level.getBlockState(pos.relative(newDir));
            if (neighbor.getBlock() instanceof SmokestackBlock)
            {
                Facing neighborFacing = neighbor.getValue(FACING);
                return neighborFacing == Facing.JUNCTION
                       ? facing.getAxis() != newDir.getAxis()
                         ? Facing.fromDirection(newDir.getOpposite())
                         : facing
                       : neighborFacing.getAxis() != newDir.getAxis()
                         ? Facing.fromDirection(newDir.getOpposite())
                         : neighborFacing;
            }
            else if (neighbor.is(ModBlockTags.THERMAL_SOURCE) && newDir == Direction.DOWN)
            {   return Facing.fromDirection(newDir.getOpposite());
            }
        }
        return facing;
    }

    protected BlockState calculateConnections(BlockState state, BlockPos pos, IWorld level)
    {
        Facing facing = state.getValue(FACING);
        if (facing == Facing.JUNCTION)
        {   return state.setValue(END, false).setValue(BASE, false);
        }
        boolean connectedTop = level.getBlockState(pos.relative(facing.toDirection())).is(ModBlockTags.CONNECTS_SMOKESTACK);
        boolean connectedBase = level.getBlockState(pos.relative(facing.toDirection().getOpposite())).is(ModBlockTags.CONNECTS_SMOKESTACK);
        return state.setValue(END, connectedTop).setValue(BASE, connectedBase);
    }

    @Override
    public BlockState getStateForPlacement(BlockItemUseContext context)
    {
        Direction placeDir = context.getClickedFace();
        BlockPos pos = context.getClickedPos();
        World level = context.getLevel();
        Facing facing = Facing.fromDirection(placeDir);

        facing = calculateFacing(facing, pos, level);

        BlockState state = this.defaultBlockState().setValue(FACING, facing);
        if (facing != Facing.JUNCTION)
        {   state = calculateConnections(state, pos, level);
        }
        return state;
    }

    @Override
    public BlockState updateShape(BlockState state, Direction neighborDir, BlockState neighborState,
                                  IWorld level, BlockPos pos, BlockPos neighborPos)
    {
        Facing facing = state.getValue(ENCASED)
                        ? Facing.JUNCTION
                        : calculateFacing(state.getValue(FACING), pos, level);
        state = state.setValue(FACING, facing);
        state = calculateConnections(state, pos, level);
        return state;
    }

    @Override
    public ActionResultType use(BlockState state, World level, BlockPos pos, PlayerEntity player, Hand hand, BlockRayTraceResult rayTraceResult)
    {
        ItemStack stack = player.getItemInHand(hand);
        if (!player.isCrouching() && ModItemTags.ENCASES_SMOKESTACK.contains(stack.getItem()))
        {
            if (!state.getValue(ENCASED))
            {
                level.setBlock(pos, state.setValue(ENCASED, true).setValue(FACING, Facing.JUNCTION).setValue(END, false).setValue(BASE, false), 3);
                player.swing(hand, true);
                level.playSound(null, pos, this.getSoundType(state, level, pos, player).getPlaceSound(), SoundCategory.BLOCKS, 1f, 1f);
                if (!player.isCreative())
                {   stack.shrink(1);
                }
                return ActionResultType.CONSUME;
            }
        }
        return super.use(state, level, pos, player, hand, rayTraceResult);
    }

    public enum Facing implements IStringSerializable
    {
        UP("up"),
        DOWN("down"),
        NORTH("north"),
        SOUTH("south"),
        EAST("east"),
        WEST("west"),
        JUNCTION("junction");

        private final String name;

        Facing(String name)
        {   this.name = name;
        }

        @Override
        public String getSerializedName()
        {   return name;
        }

        public static Facing byName(String name)
        {   for (Facing facing : values())
            {   if (facing.name.equals(name))
                {   return facing;
                }
            }
            return UP;
        }

        public static Facing fromDirection(Direction direction)
        {
            switch (direction)
            {
                case DOWN : return DOWN;
                case NORTH : return NORTH;
                case SOUTH : return SOUTH;
                case EAST : return EAST;
                case WEST : return WEST;
                default: return UP;
            }

        }

        public Direction toDirection()
        {
            switch (this)
            {
                case DOWN : return Direction.DOWN;
                case NORTH : return Direction.NORTH;
                case SOUTH : return Direction.SOUTH;
                case EAST : return Direction.EAST;
                case WEST : return Direction.WEST;
                default: return Direction.UP;
            }
        }

        public Direction.Axis getAxis()
        {   return this.toDirection().getAxis();
        }

        public Facing rotate(Rotation rotation)
        {
            switch (rotation) {
                case CLOCKWISE_90:
                    switch (this)
                    {
                        case NORTH: return EAST;
                        case EAST: return SOUTH;
                        case SOUTH: return WEST;
                        case WEST: return NORTH;
                        default: return this;
                    }
                case CLOCKWISE_180:
                    switch (this)
                    {
                        case NORTH: return SOUTH;
                        case SOUTH: return NORTH;
                        case EAST: return WEST;
                        case WEST: return EAST;
                        default: return this;
                    }
                case COUNTERCLOCKWISE_90:
                    switch (this)
                    {
                        case NORTH: return WEST;
                        case WEST: return SOUTH;
                        case SOUTH: return EAST;
                        case EAST: return NORTH;
                        default: return this;
                    }
                default: return this;
            }
        }

        public Facing mirror(Mirror mirror)
        {
            switch (mirror)
            {
                case FRONT_BACK:
                    switch (this)
                    {
                        case NORTH: return SOUTH;
                        case SOUTH: return NORTH;
                        default: return this;
                    }
                case LEFT_RIGHT:
                    switch (this)
                    {
                        case EAST: return WEST;
                        case WEST: return EAST;
                        default: return this;
                    }
                default: return this;
            }
        }
    }
}
