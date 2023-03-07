package dev.momostudios.coldsweat.common.block;

import dev.momostudios.coldsweat.common.blockentity.HearthBlockEntity;
import dev.momostudios.coldsweat.core.itemgroup.ColdSweatGroup;
import dev.momostudios.coldsweat.util.math.CSMath;
import dev.momostudios.coldsweat.util.registries.ModBlockEntities;
import dev.momostudios.coldsweat.util.registries.ModBlocks;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.core.Vec3i;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.sounds.SoundEvents;
import net.minecraft.sounds.SoundSource;
import net.minecraft.world.Containers;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.InteractionResult;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.Items;
import net.minecraft.world.item.context.BlockPlaceContext;
import net.minecraft.world.level.BlockGetter;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.EntityBlock;
import net.minecraft.world.level.block.Rotation;
import net.minecraft.world.level.block.SoundType;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraft.world.level.block.entity.BlockEntityTicker;
import net.minecraft.world.level.block.entity.BlockEntityType;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.block.state.StateDefinition;
import net.minecraft.world.level.block.state.properties.BlockStateProperties;
import net.minecraft.world.level.block.state.properties.DirectionProperty;
import net.minecraft.world.level.block.state.properties.IntegerProperty;
import net.minecraft.world.level.material.Material;
import net.minecraft.world.level.storage.loot.LootContext;
import net.minecraft.world.phys.BlockHitResult;
import net.minecraft.world.phys.Vec3;
import net.minecraft.world.phys.shapes.CollisionContext;
import net.minecraft.world.phys.shapes.Shapes;
import net.minecraft.world.phys.shapes.VoxelShape;
import net.minecraftforge.network.NetworkHooks;

import javax.annotation.Nullable;
import java.util.*;

public class HearthBottomBlock extends Block implements EntityBlock
{
    public static final DirectionProperty FACING = BlockStateProperties.HORIZONTAL_FACING;
    public static final IntegerProperty WATER = IntegerProperty.create("water", 0, 2);
    public static final IntegerProperty LAVA = IntegerProperty.create("lava", 0, 2);

    private static final Map<Direction, VoxelShape> SHAPES = new HashMap<>();

    public static Properties getProperties()
    {
        return Properties
                .of(Material.STONE)
                .sound(SoundType.STONE)
                .destroyTime(2.0F)
                .explosionResistance(10.0F)
                .requiresCorrectToolForDrops()
                .noOcclusion()
                .dynamicShape()
                .lightLevel(state -> state.getValue(LAVA) * 3);
    }

    public static Item.Properties getItemProperties()
    {
        return new Item.Properties().tab(ColdSweatGroup.COLD_SWEAT).stacksTo(1);
    }

    public HearthBottomBlock(Block.Properties properties)
    {
        super(properties);
        this.registerDefaultState(this.defaultBlockState().setValue(FACING, Direction.NORTH).setValue(WATER, 0).setValue(LAVA, 0));
        calculateFacingShapes(Shapes.or(
            Block.box(3, 0, 3.5, 13, 18, 12.5), // Shell
            Block.box(4, 18, 5, 9, 27, 10), // Exhaust
            Block.box(-1, 3, 6, 17, 11, 10))); // Canisters
    }

    static void calculateFacingShapes(VoxelShape shape)
    {
        for (Direction direction : Direction.values())
        {
            SHAPES.put(direction, CSMath.rotateShape(direction, shape));
        }
    }

    @Override
    public boolean propagatesSkylightDown(BlockState state, BlockGetter level, BlockPos pos)
    {
        return true;
    }

    @Override
    public VoxelShape getShape(BlockState state, BlockGetter getter, BlockPos pos, CollisionContext context)
    {
        return SHAPES.get(state.getValue(FACING));
    }

    @Nullable
    @Override
    public <T extends BlockEntity> BlockEntityTicker<T> getTicker(Level level, BlockState state, BlockEntityType<T> type)
    {
        return type == ModBlockEntities.HEARTH ? HearthBlockEntity::tickSelf : null;
    }

    @Nullable
    @Override
    public BlockEntity newBlockEntity(BlockPos pos, BlockState state)
    {
        return new HearthBlockEntity(pos, state);
    }

    @SuppressWarnings("deprecation")
    @Override
    public InteractionResult use(BlockState state, Level worldIn, BlockPos pos, Player player, InteractionHand hand, BlockHitResult rayTraceResult)
    {
        if (!worldIn.isClientSide)
        {
            if (worldIn.getBlockEntity(pos) instanceof HearthBlockEntity te)
            {
                ItemStack stack = player.getItemInHand(hand);

                // If the held item is a bucket, try to extract fluids
                if (player.getItemInHand(hand).getItem() == Items.BUCKET)
                {
                    int lavaFuel = Math.abs(HearthBlockEntity.getItemFuel(Items.LAVA_BUCKET.getDefaultInstance()));
                    if (te.getHotFuel() >= lavaFuel * 0.99)
                    {
                        Vec3i lavaSideOffset = state.getValue(FACING).getClockWise().getNormal();
                        Vec3 lavaSidePos = CSMath.getCenterPos(pos).add(lavaSideOffset.getX() * 0.65, lavaSideOffset.getY() * 0.65, lavaSideOffset.getZ() * 0.65);

                        if (rayTraceResult.getLocation().distanceTo(lavaSidePos) < 0.4)
                        {
                            if (lavaFuel > 0)
                            {
                                // Remove fuel
                                te.setHotFuel(te.getHotFuel() - lavaFuel);
                                // Give filled bucket item
                                if (stack.getCount() == 1)
                                    player.setItemInHand(hand, Items.LAVA_BUCKET.getDefaultInstance());
                                else
                                {
                                    stack.shrink(1);
                                    player.addItem(Items.LAVA_BUCKET.getDefaultInstance());
                                }
                                // Play bucket sound
                                worldIn.playSound(null, pos, SoundEvents.BUCKET_FILL_LAVA, SoundSource.BLOCKS, 1.0F, 0.9f + new Random().nextFloat() * 0.2F);

                                return InteractionResult.SUCCESS;
                            }
                        }
                    }
                    int waterFuel = Math.abs(HearthBlockEntity.getItemFuel(Items.WATER_BUCKET.getDefaultInstance()));
                    if (te.getColdFuel() >= waterFuel * 0.99)
                    {
                        Vec3i waterSideOffset = state.getValue(FACING).getCounterClockWise().getNormal();
                        Vec3 waterSidePos = CSMath.getCenterPos(pos).add(waterSideOffset.getX() * 0.65, waterSideOffset.getY() * 0.65, waterSideOffset.getZ() * 0.65);

                        if (rayTraceResult.getLocation().distanceTo(waterSidePos) < 0.4)
                        {
                            if (waterFuel > 0)
                            {
                                // Remove fuel
                                te.setColdFuel(te.getColdFuel() - waterFuel);
                                // Give filled bucket item
                                if (stack.getCount() == 1)
                                    player.setItemInHand(hand, Items.WATER_BUCKET.getDefaultInstance());
                                else
                                {
                                    stack.shrink(1);
                                    player.addItem(Items.WATER_BUCKET.getDefaultInstance());
                                }
                                // Play bucket sound
                                worldIn.playSound(null, pos, SoundEvents.BUCKET_FILL, SoundSource.BLOCKS, 1.0F, 0.9f + new Random().nextFloat() * 0.2F);

                                return InteractionResult.SUCCESS;
                            }
                        }
                    }
                }

                // If the held item is fuel, try to insert the fuel
                int itemFuel = HearthBlockEntity.getItemFuel(stack);
                int hearthFuel = itemFuel > 0 ? te.getHotFuel() : te.getColdFuel();

                if (itemFuel != 0 && hearthFuel + Math.abs(itemFuel) * 0.75 < HearthBlockEntity.MAX_FUEL)
                {
                    // Consume the item if not in creative
                    if (!player.isCreative())
                    {
                        if (stack.hasContainerItem())
                        {
                            ItemStack container = stack.getContainerItem();
                            player.setItemInHand(hand, container);
                        }
                        else
                        {
                            stack.shrink(1);
                        }
                    }
                    // Add the fuel
                    te.addFuel(itemFuel);

                    // Play the fuel filling sound
                    worldIn.playSound(null, pos, itemFuel > 0 ? SoundEvents.BUCKET_EMPTY_LAVA : SoundEvents.BUCKET_EMPTY,
                            SoundSource.BLOCKS, 1.0F, 0.9f + new Random().nextFloat() * 0.2F);
                }
                // Open the GUI
                else
                {
                    NetworkHooks.openGui((ServerPlayer) player, te, pos);
                }
            }
        }
        return InteractionResult.SUCCESS;
    }

    @Override
    public void onPlace(BlockState state, Level level, BlockPos pos, BlockState lastState, boolean p_60570_)
    {
        if (level.getBlockState(pos.above()).isAir())
        {
            level.setBlock(pos.above(), ModBlocks.HEARTH_TOP.defaultBlockState().setValue(HearthTopBlock.FACING, state.getValue(FACING)), 2);
        }
    }

    @Override
    public void neighborChanged(BlockState state, Level level, BlockPos pos, Block block, BlockPos fromPos, boolean isMoving)
    {
        super.neighborChanged(state, level, pos, block, fromPos, isMoving);
        if (level.getBlockState(pos.above()).getBlock() != ModBlocks.HEARTH_TOP)
        {
            this.destroy(level, pos, state);
        }
    }

    @SuppressWarnings("deprecation")
    @Override
    public List<ItemStack> getDrops(BlockState state, LootContext.Builder builder)
    {
        List<ItemStack> dropsOriginal = super.getDrops(state, builder);
        if (!dropsOriginal.isEmpty())
            return dropsOriginal;
        return Collections.singletonList(new ItemStack(this, 1));
    }

    @SuppressWarnings("deprecation")
    @Override
    public void onRemove(BlockState state, Level level, BlockPos pos, BlockState newState, boolean isMoving)
    {
        if (state.getBlock() != newState.getBlock())
        {
            if (level.getBlockState(pos.above()).getBlock() == ModBlocks.HEARTH_TOP)
            {
                level.destroyBlock(pos.above(), false);
            }

            BlockEntity tileentity = level.getBlockEntity(pos);
            if (tileentity instanceof HearthBlockEntity)
            {
                Containers.dropContents(level, pos, (HearthBlockEntity) tileentity);
                level.updateNeighborsAt(pos, this);
            }
        }
        super.onRemove(state, level, pos, newState, isMoving);
    }

    @Override
    public BlockState rotate(BlockState state, Rotation direction)
    {
        return state.setValue(FACING, direction.rotate(state.getValue(FACING)));
    }

    @Override
    protected void createBlockStateDefinition(StateDefinition.Builder<Block, BlockState> builder)
    {
        builder.add(FACING, WATER, LAVA);
    }

    @Override
    public BlockState getStateForPlacement(BlockPlaceContext context)
    {
        Level level = context.getLevel();
        BlockPos topPos = context.getClickedPos().above();
        return level.getBlockState(topPos).canBeReplaced(context) && level.getWorldBorder().isWithinBounds(topPos)
                ? this.defaultBlockState().setValue(FACING, context.getHorizontalDirection().getOpposite()).setValue(WATER, 0).setValue(LAVA, 0)
                : null;
    }
}
