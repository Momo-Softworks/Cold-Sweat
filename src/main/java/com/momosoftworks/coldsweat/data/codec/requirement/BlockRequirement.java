package com.momosoftworks.coldsweat.data.codec.requirement;

import com.mojang.datafixers.util.Either;
import com.mojang.serialization.Codec;
import com.mojang.serialization.JsonOps;
import com.mojang.serialization.codecs.RecordCodecBuilder;
import com.momosoftworks.coldsweat.data.codec.util.ExtraCodecs;
import com.momosoftworks.coldsweat.data.codec.util.IntegerBounds;
import com.momosoftworks.coldsweat.data.codec.util.NegatableList;
import com.momosoftworks.coldsweat.util.serialization.ConfigHelper;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.core.registries.Registries;
import net.minecraft.tags.TagKey;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.block.state.StateDefinition;
import net.minecraft.world.level.block.state.StateHolder;
import net.minecraft.world.level.block.state.properties.Property;
import net.minecraft.world.level.material.FluidState;
import net.minecraftforge.registries.ForgeRegistries;

import java.util.*;

public record BlockRequirement(NegatableList<Either<TagKey<Block>, Block>> blocks, StateRequirement state,
                               NbtRequirement nbt, List<Direction> sturdyFaces,
                               Optional<Boolean> replaceable)
{
    public static final BlockRequirement NONE = new BlockRequirement(new NegatableList<>(), StateRequirement.NONE, NbtRequirement.NONE, List.of(),  Optional.empty());

    public static final Codec<BlockRequirement> CODEC = RecordCodecBuilder.create(instance -> instance.group(
            NegatableList.listCodec(ConfigHelper.tagOrBuiltinCodec(Registries.BLOCK, ForgeRegistries.BLOCKS)).optionalFieldOf("blocks", new NegatableList<>()).forGetter(predicate -> predicate.blocks),
            StateRequirement.CODEC.optionalFieldOf("state", StateRequirement.NONE).forGetter(predicate -> predicate.state),
            NbtRequirement.CODEC.optionalFieldOf("nbt", NbtRequirement.NONE).forGetter(predicate -> predicate.nbt),
            Direction.CODEC.listOf().optionalFieldOf("sturdy_faces", List.of()).forGetter(predicate -> predicate.sturdyFaces),
            Codec.BOOL.optionalFieldOf("replaceable").forGetter(predicate -> predicate.replaceable)
    ).apply(instance, BlockRequirement::new));

    public BlockRequirement(List<Either<TagKey<Block>, Block>> blocks)
    {
        this(new NegatableList<>(blocks), StateRequirement.NONE, NbtRequirement.NONE, List.of(), Optional.empty());
    }

    public boolean test(Level level, BlockPos pos, BlockState state)
    {
        if (!level.isLoaded(pos)) return false;

        if (!this.blocks.isEmpty() && this.blocks.test(either -> either.map(state::is, state::is)))
        {   return false;
        }
        if (!this.state.test(state))
        {   return false;
        }
        if (!this.nbt.isEmpty())
        {
            BlockEntity blockentity = level.getBlockEntity(pos);
            if (blockentity != null && !this.nbt.test(blockentity.saveWithFullMetadata()))
            {   return false;
            }
        }
        if (!this.sturdyFaces.isEmpty() && this.sturdyFaces.stream().noneMatch(face -> state.isFaceSturdy(level, pos, face)))
        {   return false;
        }
        if (this.replaceable.isPresent())
        {   return state.isAir() || state.canBeReplaced();
        }
        return true;
    }

    public boolean test(Level level, BlockPos pos)
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
            && replaceable.equals(that.replaceable);
    }

    public record StateRequirement(Map<String, Object> properties)
    {
        public static final Codec<StateRequirement> CODEC = Codec.unboundedMap(Codec.STRING, ExtraCodecs.anyOf(IntegerBounds.CODEC, Codec.BOOL, Codec.STRING, Codec.STRING.listOf()))
                                                                 .xmap(StateRequirement::new, StateRequirement::properties);

        public static final StateRequirement NONE = new StateRequirement(new HashMap<>());

        public boolean test(BlockState state)
        {   return this.test(state.getBlock().getStateDefinition(), state);
        }

        public boolean test(FluidState state)
        {   return this.test(state.getType().getStateDefinition(), state);
        }

        public <S extends StateHolder<?, S>> boolean test(StateDefinition<?, S> stateDefinition, S state)
        {
            for (Map.Entry<String, Object> entry : this.properties.entrySet())
            {
                String key = entry.getKey();
                Object value = entry.getValue();

                Property<?> property = stateDefinition.getProperty(key);

                if (property == null)
                {   return false;
                }
                if (value instanceof IntegerBounds bounds)
                {
                    if (!property.getPossibleValues().contains(bounds.min())
                    || !property.getPossibleValues().contains(bounds.max())
                    || !bounds.test((Integer) state.getValue(property)))
                    {   return false;
                    }
                }
                else if (value instanceof List<?> list)
                {
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
                else if (value instanceof Boolean bool)
                {
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
