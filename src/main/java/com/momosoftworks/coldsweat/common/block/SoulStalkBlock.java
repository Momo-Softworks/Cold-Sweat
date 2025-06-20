package com.momosoftworks.coldsweat.common.block;

import com.momosoftworks.coldsweat.core.itemgroup.ColdSweatGroup;
import com.momosoftworks.coldsweat.config.ConfigSettings;
import com.momosoftworks.coldsweat.data.tag.ModBlockTags;
import com.momosoftworks.coldsweat.data.tag.ModItemTags;
import com.momosoftworks.coldsweat.util.math.CSMath;
import com.momosoftworks.coldsweat.util.registries.ModBlocks;
import com.momosoftworks.coldsweat.util.world.WorldHelper;
import net.minecraft.core.BlockPos;
import net.minecraft.core.BlockSource;
import net.minecraft.core.Direction;
import net.minecraft.core.Position;
import net.minecraft.core.dispenser.DefaultDispenseItemBehavior;
import net.minecraft.core.dispenser.DispenseItemBehavior;
import net.minecraft.core.particles.ParticleTypes;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.sounds.SoundEvents;
import net.minecraft.sounds.SoundSource;
import net.minecraft.util.RandomSource;
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
import net.minecraft.world.level.LevelReader;
import net.minecraft.world.level.block.*;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.block.state.StateDefinition;
import net.minecraft.world.level.block.state.properties.BlockStateProperties;
import net.minecraft.world.level.block.state.properties.EnumProperty;
import net.minecraft.world.level.block.state.properties.IntegerProperty;
import net.minecraft.world.level.material.Material;
import net.minecraft.world.phys.BlockHitResult;
import net.minecraft.world.phys.Vec3;
import net.minecraft.world.phys.shapes.CollisionContext;
import net.minecraft.world.phys.shapes.VoxelShape;
import net.minecraftforge.common.ForgeHooks;
import net.minecraftforge.common.IPlantable;
import net.minecraftforge.event.server.ServerStartedEvent;
import net.minecraftforge.eventbus.api.SubscribeEvent;
import net.minecraftforge.fml.common.Mod;
import net.minecraftforge.fml.event.lifecycle.FMLCommonSetupEvent;
import net.minecraftforge.fml.event.lifecycle.FMLLoadCompleteEvent;
import net.minecraftforge.registries.ForgeRegistries;
import net.minecraftforge.registries.RegisterEvent;
import org.jetbrains.annotations.Nullable;

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

    @SubscribeEvent
    public static void registerDispenserBehaviors(ServerStartedEvent event)
    {
        for (Item item : ForgeRegistries.ITEMS.tags().getTag(ModItemTags.GROWS_SOUL_STALK))
        {   DispenserBlock.registerBehavior(item, GROWABLE_DISPENSE_BEHAVIOR);
        }
    }

    public static Section getRandomMidsection()
    {   return Math.random() < 0.5 ? Section.MIDDLE_SPROUT : Section.MIDDLE;
    }

    public static int getRandomGrowth()
    {   return new Random().nextInt(1, 4);
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
    public void randomTick(BlockState state, ServerLevel level, BlockPos pos, RandomSource rand)
    {
        if (level.isEmptyBlock(pos.above()))
        {
            // Get the height of the plant
            int topY = getTopY(level, pos);
            int baseY = getBaseY(level, pos);
            if (topY - baseY + 1 >= MAX_HEIGHT) return;

            double minTemp = ConfigSettings.MIN_TEMP.get();
            double maxTemp = ConfigSettings.MAX_TEMP.get();
            double tempAtBase = WorldHelper.getRoughTemperatureAt(level, pos.atY(baseY));

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
    public InteractionResult use(BlockState state, Level level, BlockPos pos, Player player, InteractionHand hand, BlockHitResult rayTraceResult)
    {
        ItemStack holding = player.getItemInHand(hand);
        if (!level.isClientSide && holding.is(ModItemTags.GROWS_SOUL_STALK))
        {
            if (getHeight(level, pos) >= MAX_HEIGHT)
            {   return super.use(state, level, pos, player, hand, rayTraceResult);
            }
            // Grow soul stalk
            boolean grew = applyGrowingItem(level, pos);
            if (!player.getAbilities().instabuild)
            {   holding.shrink(1);
            }
            // Spawn particles
            Vec3 centerPos = CSMath.getCenterPos(pos);
            player.swing(hand, true);
            if (grew)
            {
                WorldHelper.spawnParticleBatch(level, ParticleTypes.SOUL, centerPos.x, centerPos.y, centerPos.z, 0.75, 0.75, 0.75, 5, 0.01);
                level.playSound(null, pos, SoundEvents.SWEET_BERRY_BUSH_PLACE, SoundSource.BLOCKS, 1f, 0.8f);
            }
            else
            {   level.playSound(null, pos, SoundEvents.WEEPING_VINES_PLACE, SoundSource.BLOCKS, 0.6f, 1.5f);
            }
            return InteractionResult.CONSUME;
        }
        return super.use(state, level, pos, player, hand, rayTraceResult);
    }

    public static boolean applyGrowingItem(Level level, BlockPos pos)
    {
        // Check height
        int topY = getTopY(level, pos);
        int baseY = getBaseY(level, pos);
        if (topY - baseY + 1 >= MAX_HEIGHT)
        {   return false;
        }
        // Grow soul stalk
        return grow(level, pos.atY(topY), getRandomGrowth());
    }

    public static boolean grow(Level level, BlockPos pos, int growth)
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

    public static int getHeight(Level level, BlockPos pos)
    {   return getTopY(level, pos) - getBaseY(level, pos) + 1;
    }

    public static int getTopY(Level level, BlockPos pos)
    {
        int aboveHeight;
        for(aboveHeight = 1; level.getBlockState(pos.above(aboveHeight)).is(ModBlocks.SOUL_STALK); ++aboveHeight)
        {}
        return pos.getY() + aboveHeight - 1;
    }

    public static int getBaseY(Level level, BlockPos pos)
    {
        int belowHeight;
        for (belowHeight = 1; level.getBlockState(pos.below(belowHeight)).is(ModBlocks.SOUL_STALK); ++belowHeight)
        {}
        return pos.getY() - belowHeight + 1;
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
        if (level.isClientSide()) return state;
        if (!this.canSurvive(state, level, pos))
        {   return Blocks.AIR.defaultBlockState();
        }

        if (direction == Direction.UP)
        {
            boolean isUnderSelf = otherState.getBlock() == this;
            Section section = state.getValue(SECTION);

            switch (section)
            {
                case TOP ->
                {
                    return isUnderSelf
                           ? state.setValue(SECTION, getRandomMidsection())
                           : state;
                }
                case BUD ->
                {
                    return isUnderSelf
                           ? state.setValue(SECTION, Section.BASE)
                           : state;
                }
                default ->
                {
                    if (!isUnderSelf)
                    {   return Blocks.AIR.defaultBlockState();
                    }
                }
            }
        }
        this.ensureProperState(level, pos);
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

    public static final DispenseItemBehavior GROWABLE_DISPENSE_BEHAVIOR = new DefaultDispenseItemBehavior()
    {
        @Override
        protected ItemStack execute(BlockSource source, ItemStack stack)
        {
            Level level = source.getLevel();
            Position position = DispenserBlock.getDispensePosition(source);
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
