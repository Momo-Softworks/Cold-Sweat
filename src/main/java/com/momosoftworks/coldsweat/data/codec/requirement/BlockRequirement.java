package com.momosoftworks.coldsweat.data.codec.requirement;

import com.mojang.datafixers.util.Either;
import com.mojang.serialization.Codec;
import com.mojang.serialization.JsonOps;
import com.mojang.serialization.codecs.RecordCodecBuilder;
import com.momosoftworks.coldsweat.data.codec.util.ExtraCodecs;
import com.momosoftworks.coldsweat.data.codec.util.IntegerBounds;
import com.momosoftworks.coldsweat.util.serialization.ConfigHelper;
import net.minecraft.block.Block;
import net.minecraft.block.BlockState;
import net.minecraft.fluid.FluidState;
import net.minecraft.nbt.CompoundNBT;
import net.minecraft.state.Property;
import net.minecraft.state.StateContainer;
import net.minecraft.state.StateHolder;
import net.minecraft.tags.ITag;
import net.minecraft.tileentity.TileEntity;
import net.minecraft.util.Direction;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.registry.Registry;
import net.minecraft.world.World;

import java.util.*;

public class BlockRequirement
{
    private final List<Either<ITag<Block>, Block>> blocks;
    private final StateRequirement state;
    private final NbtRequirement nbt;
    private final List<Direction> sturdyFaces;
    private final Optional<Boolean> withinWorldBounds;
    private final Optional<Boolean> replaceable;

    public BlockRequirement(List<Either<ITag<Block>, Block>> blocks, StateRequirement state,
                            NbtRequirement nbt, List<Direction> sturdyFaces,
                            Optional<Boolean> withinWorldBounds, Optional<Boolean> replaceable)
    {
        this.blocks = blocks;
        this.state = state;
        this.nbt = nbt;
        this.sturdyFaces = sturdyFaces;
        this.withinWorldBounds = withinWorldBounds;
        this.replaceable = replaceable;
    }

    public BlockRequirement(List<Either<ITag<Block>, Block>> blocks)
    {
        this(blocks, StateRequirement.NONE, NbtRequirement.NONE, Arrays.asList(), Optional.empty(), Optional.empty());
    }

    public static final BlockRequirement NONE = new BlockRequirement(Arrays.asList(), StateRequirement.NONE, NbtRequirement.NONE, Arrays.asList(), Optional.empty(), Optional.empty());


    public static final Codec<BlockRequirement> CODEC = RecordCodecBuilder.create(instance -> instance.group(
            ConfigHelper.tagOrBuiltinCodec(Registry.BLOCK_REGISTRY, Registry.BLOCK).listOf().optionalFieldOf("blocks", Arrays.asList()).forGetter(predicate -> predicate.blocks),
            StateRequirement.CODEC.optionalFieldOf("state", StateRequirement.NONE).forGetter(predicate -> predicate.state),
            NbtRequirement.CODEC.optionalFieldOf("nbt", NbtRequirement.NONE).forGetter(predicate -> predicate.nbt),
            Codec.STRING.xmap(Direction::byName, Direction::getName).listOf().optionalFieldOf("sturdy_faces", Arrays.asList()).forGetter(predicate -> predicate.sturdyFaces),
            Codec.BOOL.optionalFieldOf("within_world_bounds").forGetter(predicate -> predicate.withinWorldBounds),
            Codec.BOOL.optionalFieldOf("replaceable").forGetter(predicate -> predicate.replaceable)
    ).apply(instance, BlockRequirement::new));

    public List<Either<ITag<Block>, Block>> blocks()
    {   return blocks;
    }
    public StateRequirement state()
    {   return state;
    }
    public NbtRequirement nbt()
    {   return nbt;
    }
    public List<Direction> sturdyFaces()
    {   return sturdyFaces;
    }
    public Optional<Boolean> withinWorldBounds()
    {   return withinWorldBounds;
    }
    public Optional<Boolean> replaceable()
    {   return replaceable;
    }

    public boolean test(World level, BlockPos pos, BlockState state)
    {
        if (!level.isLoaded(pos)) return false;

        if (!this.blocks.isEmpty() && this.blocks.stream().noneMatch(either -> either.map(state::is, state::is)))
        {   return false;
        }
        if (!this.state.test(state))
        {   return false;
        }
        if (!this.nbt.isEmpty())
        {
            TileEntity blockentity = level.getBlockEntity(pos);
            if (blockentity != null && !this.nbt.test(blockentity.save(new CompoundNBT())))
            {   return false;
            }
        }
        if (!this.sturdyFaces.isEmpty() && this.sturdyFaces.stream().noneMatch(face -> state.isFaceSturdy(level, pos, face)))
        {   return false;
        }
        if (this.withinWorldBounds.isPresent())
        {   return level.getWorldBorder().isWithinBounds(pos);
        }
        if (this.replaceable.isPresent())
        {   return state.isAir() || state.getMaterial().isReplaceable();
        }
        return true;
    }

    public boolean test(World level, BlockPos pos)
    {
        if (!level.isLoaded(pos))
        {   return false;
        }
        return this.test(level, pos, level.getBlockState(pos));
    }

    @Override
    public String toString()
    {   return CODEC.encodeStart(JsonOps.INSTANCE, this).result().map(Object::toString).orElse("serialize_failed");
    }

    @Override
    public boolean equals(Object obj)
    {
        if (this == obj) return true;
        if (obj == null || getClass() != obj.getClass()) return false;

        BlockRequirement that = (BlockRequirement) obj;
        return blocks.equals(that.blocks)
            && state.equals(that.state)
            && nbt.equals(that.nbt)
            && sturdyFaces.equals(that.sturdyFaces)
            && withinWorldBounds.equals(that.withinWorldBounds)
            && replaceable.equals(that.replaceable);
    }

    public static class StateRequirement
    {
        public Map<String, Object> properties;

        public StateRequirement(Map<String, Object> properties)
        {   this.properties = properties;
        }

        public static final Codec<StateRequirement> CODEC = Codec.unboundedMap(Codec.STRING, ExtraCodecs.anyOf(IntegerBounds.CODEC, Codec.BOOL, Codec.STRING, Codec.STRING.listOf()))
                                                                 .xmap(StateRequirement::new, req -> req.properties);

        public static final StateRequirement NONE = new StateRequirement(new HashMap<>());

        public boolean test(BlockState state)
        {   return this.test(state.getBlock().getStateDefinition(), state);
        }

        public boolean test(FluidState state)
        {   return this.test(state.getType().getStateDefinition(), state);
        }

        public <S extends StateHolder<?, S>> boolean test(StateContainer<?, S> stateDefinition, S state)
        {
            for (Map.Entry<String, Object> entry : this.properties.entrySet())
            {
                String key = entry.getKey();
                Object value = entry.getValue();

                Property<?> property = stateDefinition.getProperty(key);

                if (property == null)
                {   return false;
                }
                if (value instanceof IntegerBounds)
                {
                    IntegerBounds bounds = (IntegerBounds) value;
                    if (!property.getPossibleValues().contains(bounds.min)
                    || !property.getPossibleValues().contains(bounds.max)
                    || !bounds.test((Integer) state.getValue(property)))
                    {   return false;
                    }
                }
                else if (value instanceof List<?>)
                {
                    List<?> list = (List<?>) value;
                    if (list.isEmpty())
                    {   return true;
                    }
                    for (Object val : list)
                    {
                        if (state.getValue(property).toString().equals(val.toString()))
                        {   return true;
                        }
                    }
                    return false;
                }
                else if (value instanceof Boolean)
                {
                    Boolean bool = (Boolean) value;
                    if (!property.getPossibleValues().contains(bool)
                    || !state.getValue(property).equals(bool))
                    {   return false;
                    }
                }
                else
                {
                    if (!property.getPossibleValues().contains(value)
                    || !state.getValue(property).toString().equals(value.toString()))
                    {   return false;
                    }
                }
            }
            return true;
        }

        public static StateRequirement fromToml(String[] entries, Block block)
        {   return fromToml(Arrays.asList(entries), block);
        }

        public static StateRequirement fromToml(List<String> entries, Block block)
        {
            Map<String, Object> blockPredicates = new HashMap<>();

            // Iterate predicates
            for (String predicate : entries)
            {
                // Split predicate into key-value pairs separated by "="
                String[] pair = predicate.split("=");
                String key = pair[0];
                String value = pair[1];

                // Get the property with the given name
                Property<?> property = block.getStateDefinition().getProperty(key);
                if (property != null)
                {
                    // Parse the desired value for this property
                    property.getValue(value).ifPresent(propertyValue ->
                    {   // Add a new predicate to the list
                        blockPredicates.put(key, propertyValue);
                    });
                }
            }
            return new StateRequirement(blockPredicates);
        }

        @Override
        public boolean equals(Object obj)
        {
            if (this == obj) return true;
            if (obj == null || getClass() != obj.getClass()) return false;

            StateRequirement that = (StateRequirement) obj;
            return properties.equals(that.properties);
        }
    }
}
