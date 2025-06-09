package com.momosoftworks.coldsweat.common.block;

import com.momosoftworks.coldsweat.config.ConfigSettings;
import com.momosoftworks.coldsweat.core.itemgroup.ColdSweatGroup;
import com.momosoftworks.coldsweat.data.tag.ModBlockTags;
import com.momosoftworks.coldsweat.data.tag.ModItemTags;
import com.momosoftworks.coldsweat.util.math.CSMath;
import com.momosoftworks.coldsweat.util.registries.ModBlocks;
import com.momosoftworks.coldsweat.util.serialization.StringRepresentable;
import com.momosoftworks.coldsweat.util.world.WorldHelper;
import net.minecraft.block.*;
import net.minecraft.block.material.Material;
import net.minecraft.client.audio.SoundSource;
import net.minecraft.dispenser.DefaultDispenseItemBehavior;
import net.minecraft.dispenser.IBlockSource;
import net.minecraft.dispenser.IDispenseItemBehavior;
import net.minecraft.dispenser.IPosition;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.item.BlockItemUseContext;
import net.minecraft.item.Item;
import net.minecraft.item.ItemStack;
import net.minecraft.particles.ParticleTypes;
import net.minecraft.state.EnumProperty;
import net.minecraft.state.IntegerProperty;
import net.minecraft.state.StateContainer;
import net.minecraft.state.properties.BlockStateProperties;
import net.minecraft.tags.ItemTags;
import net.minecraft.util.*;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.BlockRayTraceResult;
import net.minecraft.util.math.shapes.ISelectionContext;
import net.minecraft.util.math.shapes.VoxelShape;
import net.minecraft.util.math.vector.Vector3d;
import net.minecraft.world.IBlockReader;
import net.minecraft.world.IWorld;
import net.minecraft.world.IWorldReader;
import net.minecraft.world.World;
import net.minecraft.world.server.ServerWorld;
import net.minecraftforge.common.ForgeHooks;
import net.minecraftforge.common.IPlantable;
import net.minecraftforge.eventbus.api.SubscribeEvent;
import net.minecraftforge.fml.common.Mod;
import net.minecraftforge.fml.event.server.FMLServerStartedEvent;
import net.minecraftforge.registries.ForgeRegistries;

import java.util.Random;

@Mod.EventBusSubscriber
public class SoulStalkBlock extends Block implements IPlantable
{
    public static final IntegerProperty AGE = BlockStateProperties.AGE_15;
    public static final EnumProperty<Section> SECTION = EnumProperty.create("section", Section.class);
    protected static final VoxelShape SHAPE = Block.box(3.0D, 0.0D, 3.0D, 13.0D, 16.0D, 13.0D);
    protected static final VoxelShape SHAPE_BUD = Block.box(4.5D, 0.0D, 4.5D, 11.5D, 14.0D, 11.5D);

    public static final int MAX_HEIGHT = 6;

    public SoulStalkBlock(Properties properties)
    {
        super(properties);
        this.registerDefaultState(this.defaultBlockState().setValue(AGE, 0).setValue(SECTION, Section.BUD));
    }

    public static Properties getProperties()
    {
        return Properties
                .of(Material.PLANT)
                .sound(SoundType.CROP)
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

    @SubscribeEvent
    public static void registerDispenserBehaviors(FMLServerStartedEvent event)
    {
        for (Item item : ModItemTags.GROWS_SOUL_STALK.getValues())
        {   DispenserBlock.registerBehavior(item, GROWABLE_DISPENSE_BEHAVIOR);
        }
    }

    public static Section getRandomMidsection()
    {   return Math.random() < 0.5 ? Section.MIDDLE_SPROUT : Section.MIDDLE;
    }

    public static int getRandomGrowth()
    {   return new Random().nextInt(2) + 2;
    }

    protected void ensureProperState(LevelAccessor level, BlockPos pos)
    {
            BlockState aboveState = level.getBlockState(pos.above());
            BlockState belowState = level.getBlockState(pos.below());
            if (aboveState.is(this))
            {
                if (belowState.is(this))
                {   this.ensureSectionAt(level, pos, getRandomMidsection());
                }
                else
                {   this.ensureSectionAt(level, pos, Section.BASE);
                }
            }
            else if (belowState.is(this))
            {   this.ensureSectionAt(level, pos, Section.TOP);
            }
            else
            {   this.ensureSectionAt(level, pos, Section.BUD);
            }
    }

    private void ensureSectionAt(LevelAccessor level, BlockPos pos, Section section)
    {
        BlockState oldState = level.getBlockState(pos);
        if (!oldState.is(this)) return;
        Section oldSection = oldState.getValue(SECTION);
        if (section.isMiddle() ? !oldSection.isMiddle() : oldSection != section)
        {   level.setBlock(pos, oldState.setValue(SECTION, section), 3);
        }
    }

    @Override
    public void randomTick(BlockState state, ServerWorld level, BlockPos pos, Random rand)
    {
        this.ensureProperState(level, pos);
        if (level.isEmptyBlock(pos.above()))
        {
            // Get the height of the plant
            int topY = getTopY(level, pos);
            int baseY = getBaseY(level, pos);
            if (topY - baseY + 1 >= MAX_HEIGHT) return;

            double minTemp = ConfigSettings.MIN_TEMP.get();
            double maxTemp = ConfigSettings.MAX_TEMP.get();
            double tempAtBase = WorldHelper.getRoughTemperatureAt(level, new BlockPos(pos.getX(), baseY, pos.getZ()));

            if (rand.nextDouble() < 0.05 + CSMath.blend(0, 0.95, tempAtBase, minTemp, maxTemp))
            {
                if (ForgeHooks.onCropsGrowPre(level, pos, state, true))
                {   grow(level, pos, 1);
                }
            }
        }
    }

    @SuppressWarnings("deprecation")
    @Override
    public ActionResultType use(BlockState state, World level, BlockPos pos, PlayerEntity player, Hand hand, BlockRayTraceResult rayTraceResult)
    {
        ItemStack holding = player.getItemInHand(hand);
        if (!level.isClientSide && ModItemTags.GROWS_SOUL_STALK.contains(holding.getItem()))
        {
            if (getHeight(level, pos) >= MAX_HEIGHT)
            {   return super.use(state, level, pos, player, hand, rayTraceResult);
            }
            // Grow soul stalk
            boolean grew = applyGrowingItem(level, pos);
            if (!player.abilities.instabuild)
            {   holding.shrink(1);
            }
            // Spawn particles
            Vector3d centerPos = CSMath.getCenterPos(pos);
            player.swing(hand, true);
            if (grew)
            {
                WorldHelper.spawnParticleBatch(level, ParticleTypes.SOUL, centerPos.x, centerPos.y, centerPos.z, 0.75, 0.75, 0.75, 5, 0.01);
                level.playSound(null, pos, SoundEvents.SWEET_BERRY_BUSH_PLACE, SoundCategory.BLOCKS, 1f, 0.8f);
            }
            else
            {   level.playSound(null, pos, SoundEvents.WEEPING_VINES_PLACE, SoundCategory.BLOCKS, 0.6f, 1.5f);
            }
            return ActionResultType.CONSUME;
        }
        return super.use(state, level, pos, player, hand, rayTraceResult);
    }

    public static boolean applyGrowingItem(World level, BlockPos pos)
    {
        // Check height
        int topY = getTopY(level, pos);
        int baseY = getBaseY(level, pos);
        if (topY - baseY + 1 >= MAX_HEIGHT)
        {   return false;
        }
        // Grow soul stalk
        return grow(level, new BlockPos(pos.getX(), topY, pos.getZ()), getRandomGrowth());
    }

    public static boolean grow(World level, BlockPos pos, int growth)
    {
        BlockState state = level.getBlockState(pos);
        BlockState defaultState = state.getBlock().defaultBlockState();
        int age = state.getValue(AGE) + growth;
        Section section = state.getValue(SECTION);
        if (age >= 4)
        {
            // Growing taller
            if (section == Section.TOP)
            {
                level.setBlockAndUpdate(pos, defaultState.setValue(AGE, 0).setValue(SECTION, getRandomMidsection()));
                level.setBlockAndUpdate(pos.above(), defaultState.setValue(AGE, 0).setValue(SECTION, Section.TOP));
            }
            // Growing from a bud
            else if (section == Section.BUD)
            {
                level.setBlockAndUpdate(pos, defaultState.setValue(AGE, 0).setValue(SECTION, Section.BASE));
                level.setBlockAndUpdate(pos.above(), defaultState.setValue(AGE, 0).setValue(SECTION, Section.TOP));
            }
            return true;
        }
        else
        {   level.setBlock(pos, state.setValue(AGE, age), 2);
        }
        return false;
    }

    public static int getHeight(World level, BlockPos pos)
    {   return getTopY(level, pos) - getBaseY(level, pos) + 1;
    }

    public static int getTopY(World level, BlockPos pos)
    {
        int aboveHeight;
        for(aboveHeight = 1; level.getBlockState(pos.above(aboveHeight)).is(ModBlocks.SOUL_STALK); ++aboveHeight)
        {}
        return pos.getY() + aboveHeight - 1;
    }

    public static int getBaseY(World level, BlockPos pos)
    {
        int belowHeight;
        for (belowHeight = 1; level.getBlockState(pos.below(belowHeight)).is(ModBlocks.SOUL_STALK); ++belowHeight)
        {}
        return pos.getY() - belowHeight + 1;
    }

    @Override
    public BlockState getStateForPlacement(BlockItemUseContext context)
    {
        BlockPos pos = context.getClickedPos();
        return context.getLevel().getBlockState(pos.below()).getBlock() == this
               ? this.defaultBlockState().setValue(SECTION, Section.TOP)
             : this.canSurvive(this.defaultBlockState(), context.getLevel(), pos)
               ? this.defaultBlockState()
             : null;
    }

    @Override
    public BlockState updateShape(BlockState state, Direction direction, BlockState otherState, IWorld level, BlockPos pos, BlockPos otherPos)
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
                    case TOP : return state.setValue(SECTION, getRandomMidsection());
                    case BUD : return state.setValue(SECTION, Section.BASE);
                }
            }
        }
        this.ensureProperState(level, pos);
        return state;
    }

    @Override
    public VoxelShape getShape(BlockState state, IBlockReader getter, BlockPos pos, ISelectionContext context)
    {
        return state.getValue(SECTION) == Section.BUD ? SHAPE_BUD : SHAPE;
    }

    @Override
    protected void createBlockStateDefinition(StateContainer.Builder<Block, BlockState> builder)
    {   builder.add(AGE, SECTION);
    }

    @Override
    public BlockState getPlant(IBlockReader level, BlockPos pos)
    {   return this.defaultBlockState();
    }

    @Override
    public boolean canSurvive(BlockState state, IWorldReader level, BlockPos pos)
    {   BlockState below = level.getBlockState(pos.below());
        return below.is(ModBlockTags.SOUL_STALK_PLACEABLE_ON) || below.getBlock() == this;
    }

    public enum Section implements StringRepresentable
    {
        BASE("base"),
        MIDDLE("middle"),
        MIDDLE_SPROUT("middle_sprout"),
        TOP("top"),
        BUD("bud");

        private final String name;

        Section(String name)
        {   this.name = name;
        }

        public boolean hasFruit()
        {   return this == TOP || this == MIDDLE_SPROUT || this == BUD;
        }
        public boolean isMiddle()
        {   return this == MIDDLE || this == MIDDLE_SPROUT;
        }

        @Override
        public String getSerializedName()
        {   return name;
        }
    }

    public static final IDispenseItemBehavior GROWABLE_DISPENSE_BEHAVIOR = new DefaultDispenseItemBehavior()
    {
        @Override
        protected ItemStack execute(IBlockSource source, ItemStack stack)
        {
            World level = source.getLevel();
            IPosition position = DispenserBlock.getDispensePosition(source);
            BlockPos pos = new BlockPos(position);
            BlockState state = level.getBlockState(pos);
            if (state.is(ModBlocks.SOUL_STALK))
            {
                stack.shrink(1);
                applyGrowingItem(level, pos);
                return stack;
            }
            return super.execute(source, stack);
        }
    };
}
