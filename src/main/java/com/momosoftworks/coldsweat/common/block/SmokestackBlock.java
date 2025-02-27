package com.momosoftworks.coldsweat.common.block;

import com.momosoftworks.coldsweat.core.itemgroup.ColdSweatGroup;
import com.momosoftworks.coldsweat.data.tag.ModBlockTags;
import com.momosoftworks.coldsweat.data.tag.ModItemTags;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.sounds.SoundSource;
import net.minecraft.util.StringRepresentable;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.InteractionResult;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.context.BlockPlaceContext;
import net.minecraft.world.level.BlockGetter;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.LevelAccessor;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.Mirror;
import net.minecraft.world.level.block.Rotation;
import net.minecraft.world.level.block.SoundType;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.block.state.StateDefinition;
import net.minecraft.world.level.block.state.properties.BooleanProperty;
import net.minecraft.world.level.block.state.properties.EnumProperty;
import net.minecraft.world.phys.BlockHitResult;
import net.minecraft.world.level.material.Material;
import net.minecraft.world.phys.shapes.CollisionContext;
import net.minecraft.world.phys.shapes.VoxelShape;

import java.util.Random;

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
                .strength(2f)
                .explosionResistance(10f)
                .requiresCorrectToolForDrops()
                .dynamicShape();
    }

    public static Item.Properties getItemProperties()
    {   return new Item.Properties().tab(ColdSweatGroup.COLD_SWEAT);
    }

    @Override
    public boolean propagatesSkylightDown(BlockState state, BlockGetter level, BlockPos pos)
    {   return state.getValue(FACING) != Facing.JUNCTION;
    }

    @Override
    public VoxelShape getShape(BlockState state, BlockGetter getter, BlockPos pos, CollisionContext context)
    {
        return switch (state.getValue(FACING))
        {
            case UP, DOWN -> Block.box(4, 0, 4, 12, 16, 12);
            case NORTH, SOUTH -> Block.box(4, 4, 0, 12, 12, 16);
            case EAST, WEST -> Block.box(0, 4, 4, 16, 12, 12);
            case JUNCTION -> Block.box(0, 0, 0, 16, 16, 16);
        };
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
    protected void createBlockStateDefinition(StateDefinition.Builder<Block, BlockState> builder)
    {   builder.add(FACING, END, BASE, ENCASED);
    }

    protected Facing calculateFacing(Facing facing, BlockPos pos, LevelAccessor level)
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

    protected BlockState calculateConnections(BlockState state, BlockPos pos, LevelAccessor level)
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
    public BlockState getStateForPlacement(BlockPlaceContext context)
    {
        Direction placeDir = context.getClickedFace();
        BlockPos pos = context.getClickedPos();
        Level level = context.getLevel();
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
                                  LevelAccessor level, BlockPos pos, BlockPos neighborPos)
    {
        Facing facing = state.getValue(ENCASED)
                        ? Facing.JUNCTION
                        : calculateFacing(state.getValue(FACING), pos, level);
        state = state.setValue(FACING, facing);
        state = calculateConnections(state, pos, level);
        return state;
    }

    @Override
    public InteractionResult use(BlockState state, Level level, BlockPos pos, Player player, InteractionHand hand, BlockHitResult rayTraceResult)
    {
        ItemStack stack = player.getItemInHand(hand);
        if (!player.isCrouching() && stack.is(ModItemTags.ENCASES_SMOKESTACK))
        {
            if (!state.getValue(ENCASED))
            {
                level.setBlock(pos, state.setValue(ENCASED, true).setValue(FACING, Facing.JUNCTION).setValue(END, false).setValue(BASE, false), 3);
                player.swing(hand, true);
                level.playSound(null, pos, this.getSoundType(state, level, pos, player).getPlaceSound(), SoundSource.BLOCKS, 1f, 1f);
                if (!player.isCreative())
                {   stack.shrink(1);
                }
                return InteractionResult.SUCCESS;
            }
        }
        return super.use(state, level, pos, player, hand, rayTraceResult);
    }

    public enum Facing implements StringRepresentable
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
            return switch (direction)
            {
                case DOWN -> DOWN;
                case NORTH -> NORTH;
                case SOUTH -> SOUTH;
                case EAST -> EAST;
                case WEST -> WEST;
                default -> UP;
            };
        }

        public Direction toDirection()
        {
            return switch (this)
            {
                case DOWN -> Direction.DOWN;
                case NORTH -> Direction.NORTH;
                case SOUTH -> Direction.SOUTH;
                case EAST -> Direction.EAST;
                case WEST -> Direction.WEST;
                default -> Direction.UP;
            };
        }

        public Direction.Axis getAxis()
        {   return this.toDirection().getAxis();
        }

        public Facing rotate(Rotation rotation)
        {
            return switch (rotation)
            {
                case CLOCKWISE_90 -> switch (this)
                {
                    case NORTH -> EAST;
                    case EAST -> SOUTH;
                    case SOUTH -> WEST;
                    case WEST -> NORTH;
                    default -> this;
                };
                case CLOCKWISE_180 -> switch (this)
                {
                    case NORTH -> SOUTH;
                    case SOUTH -> NORTH;
                    case EAST -> WEST;
                    case WEST -> EAST;
                    default -> this;
                };
                case COUNTERCLOCKWISE_90 -> switch (this)
                {
                    case NORTH -> WEST;
                    case WEST -> SOUTH;
                    case SOUTH -> EAST;
                    case EAST -> NORTH;
                    default -> this;
                };
                default -> this;
            };
        }

        public Facing mirror(Mirror mirror)
        {
            return switch (mirror)
            {
                case FRONT_BACK -> switch (this)
                {
                    case NORTH -> SOUTH;
                    case SOUTH -> NORTH;
                    default -> this;
                };
                case LEFT_RIGHT -> switch (this)
                {
                    case EAST -> WEST;
                    case WEST -> EAST;
                    default -> this;
                };
                default -> this;
            };
        }
    }
}
