package com.momosoftworks.coldsweat.data.codec.requirement;

import com.mojang.datafixers.util.Either;
import com.mojang.serialization.Codec;
import com.mojang.serialization.codecs.RecordCodecBuilder;
import com.momosoftworks.coldsweat.util.serialization.ConfigHelper;
import com.momosoftworks.coldsweat.util.serialization.NBTHelper;
import net.minecraft.block.Block;
import net.minecraft.block.BlockState;
import net.minecraft.fluid.FluidState;
import net.minecraft.item.DirectionalPlaceContext;
import net.minecraft.item.ItemStack;
import net.minecraft.nbt.CompoundNBT;
import net.minecraft.nbt.ListNBT;
import net.minecraft.nbt.StringNBT;
import net.minecraft.state.Property;
import net.minecraft.state.StateContainer;
import net.minecraft.state.StateHolder;
import net.minecraft.tags.BlockTags;
import net.minecraft.tags.ITag;
import net.minecraft.tileentity.TileEntity;
import net.minecraft.util.Direction;
import net.minecraft.util.ResourceLocation;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.registry.Registry;
import net.minecraft.world.World;
import net.minecraftforge.registries.ForgeRegistries;

import java.util.List;
import java.util.Optional;
import java.util.stream.Collectors;

public class BlockRequirement
{
    public final Optional<List<Either<ITag<Block>, Block>>> blocks;
    public final Optional<StateRequirement> state;
    public final Optional<NbtRequirement> nbt;
    public final Optional<Direction> sturdyFace;
    public final Optional<Boolean> withinWorldBounds;
    public final Optional<Boolean> replaceable;
    public final boolean negate;

    public BlockRequirement(Optional<List<Either<ITag<Block>, Block>>> blocks, Optional<StateRequirement> state,
                            Optional<NbtRequirement> nbt, Optional<Direction> sturdyFace,
                            Optional<Boolean> withinWorldBounds, Optional<Boolean> replaceable,
                            boolean negate)
    {
        this.blocks = blocks;
        this.state = state;
        this.nbt = nbt;
        this.sturdyFace = sturdyFace;
        this.withinWorldBounds = withinWorldBounds;
        this.replaceable = replaceable;
        this.negate = negate;
    }

    public static final BlockRequirement NONE = new BlockRequirement(Optional.empty(), Optional.empty(), Optional.empty(),
                                                                     Optional.empty(), Optional.empty(), Optional.empty(),
                                                                     false);

    public static final Codec<BlockRequirement> CODEC = RecordCodecBuilder.create(instance -> instance.group(
            ConfigHelper.tagOrBuiltinCodec(Registry.BLOCK_REGISTRY, Registry.BLOCK).listOf().optionalFieldOf("blocks").forGetter(predicate -> predicate.blocks),
            StateRequirement.CODEC.optionalFieldOf("state").forGetter(predicate -> predicate.state),
            NbtRequirement.CODEC.optionalFieldOf("nbt").forGetter(predicate -> predicate.nbt),
            Codec.STRING.xmap(Direction::byName, Direction::getName).optionalFieldOf("has_sturdy_face").forGetter(predicate -> predicate.sturdyFace),
            Codec.BOOL.optionalFieldOf("within_world_bounds").forGetter(predicate -> predicate.withinWorldBounds),
            Codec.BOOL.optionalFieldOf("replaceable").forGetter(predicate -> predicate.replaceable),
            Codec.BOOL.optionalFieldOf("negate", false).forGetter(predicate -> predicate.negate)
    ).apply(instance, BlockRequirement::new));

    public boolean test(World pLevel, BlockPos pPos)
    {
        if (!pLevel.isLoaded(pPos))
        {   return false;
        }
        else
        {
            BlockState blockstate = pLevel.getBlockState(pPos);
            if (this.blocks.isPresent() && this.blocks.get().stream().noneMatch(either -> either.map(blockstate::is, blockstate::is)))
            {   return false ^ this.negate;
            }
            else if (this.state.isPresent() && !this.state.get().matches(blockstate))
            {   return false ^ this.negate;
            }
            else if (this.nbt.isPresent())
            {
                TileEntity blockentity = pLevel.getBlockEntity(pPos);
                return (blockentity != null && this.nbt.get().test(blockentity.save(new CompoundNBT()))) ^ this.negate;
            }
            else if (this.sturdyFace.isPresent())
            {   return blockstate.isFaceSturdy(pLevel, pPos, this.sturdyFace.get()) ^ this.negate;
            }
            else if (this.withinWorldBounds.isPresent())
            {   return pLevel.getWorldBorder().isWithinBounds(pPos) ^ this.negate;
            }
            else if (this.replaceable.isPresent())
            {   return blockstate.isAir() || blockstate.canBeReplaced(new DirectionalPlaceContext(pLevel, pPos, Direction.DOWN, ItemStack.EMPTY, Direction.UP)) ^ this.negate;
            }
            else
            {   return true ^ this.negate;
            }
        }
    }

    public CompoundNBT serialize()
    {   CompoundNBT compound = new CompoundNBT();

        blocks.ifPresent(blocks ->
        {
            compound.put("blocks", NBTHelper.listTagOf(blocks.stream().map(either ->
                                   {
                                       return StringNBT.valueOf(either.map(
                                              tag ->
                                              {    return "#" + BlockTags.getAllTags().getId(tag);
                                              },
                                              block ->
                                              {    return ForgeRegistries.BLOCKS.getKey(block).toString();
                                              }));
                                   })
                                   .collect(Collectors.toList())));
        });
        state.ifPresent(state -> compound.put("state", state.serialize()));
        nbt.ifPresent(nbt -> compound.put("nbt", nbt.serialize()));
        sturdyFace.ifPresent(face -> compound.putString("has_sturdy_face", face.getName()));
        withinWorldBounds.ifPresent(bounds -> compound.putBoolean("within_world_bounds", bounds));
        replaceable.ifPresent(replaceable -> compound.putBoolean("replaceable", replaceable));
        compound.putBoolean("negate", negate);

        return compound;
    }

    public static BlockRequirement deserialize(CompoundNBT tag)
    {
        Optional<List<Either<ITag<Block>, Block>>> blocks = tag.contains("blocks") ? Optional.of(tag.getList("blocks", 8).stream().map(tg ->
        {
            String string = tg.getAsString();
            if (string.startsWith("#"))
            {   return Either.<ITag<Block>, Block>left(BlockTags.getAllTags().getTag(new ResourceLocation(string.substring(1))));
            }
            else
            {   return Either.<ITag<Block>, Block>right(ForgeRegistries.BLOCKS.getValue(new ResourceLocation(string)));
            }
        }).collect(Collectors.toList())) : Optional.empty();
        Optional<StateRequirement> state = tag.contains("state") ? Optional.of(StateRequirement.deserialize(tag.getCompound("state"))) : Optional.empty();
        Optional<NbtRequirement> nbt = tag.contains("nbt") ? Optional.of(NbtRequirement.deserialize(tag.getCompound("nbt"))) : Optional.empty();
        Optional<Direction> sturdyFace = tag.contains("has_sturdy_face") ? Optional.of(Direction.byName(tag.getString("has_sturdy_face"))) : Optional.empty();
        Optional<Boolean> withinWorldBounds = tag.contains("within_world_bounds") ? Optional.of(tag.getBoolean("within_world_bounds")) : Optional.empty();
        Optional<Boolean> replaceable = tag.contains("replaceable") ? Optional.of(tag.getBoolean("replaceable")) : Optional.empty();
        boolean negate = tag.getBoolean("negate");

        return new BlockRequirement(blocks, state, nbt, sturdyFace, withinWorldBounds, replaceable, negate);
    }

    @Override
    public boolean equals(Object obj)
    {
        if (this == obj)
        {   return true;
        }
        if (obj == null || getClass() != obj.getClass())
        {   return false;
        }

        BlockRequirement that = (BlockRequirement) obj;

        if (!blocks.equals(that.blocks))
        {   return false;
        }
        if (!state.equals(that.state))
        {   return false;
        }
        if (!nbt.equals(that.nbt))
        {   return false;
        }
        if (!sturdyFace.equals(that.sturdyFace))
        {   return false;
        }
        if (!withinWorldBounds.equals(that.withinWorldBounds))
        {   return false;
        }
        if (!replaceable.equals(that.replaceable))
        {   return false;
        }
        return negate == that.negate;
    }

    public static class StateRequirement
    {
        private final List<Either<StateProperty, RangedProperty>> properties;

        public StateRequirement(List<Either<StateProperty, RangedProperty>> properties)
        {   this.properties = properties;
        }

        public static final Codec<StateRequirement> CODEC = RecordCodecBuilder.create(instance -> instance.group(
                Codec.either(StateProperty.CODEC, RangedProperty.CODEC).listOf().fieldOf("properties").forGetter(state -> state.properties)
        ).apply(instance, StateRequirement::new));

        public CompoundNBT serialize()
        {   CompoundNBT compound = new CompoundNBT();
            ListNBT list = new ListNBT();
            this.properties.forEach(property -> list.add(property.map(
                    StateProperty::serialize,
                    RangedProperty::serialize)));
            compound.put("properties", list);
            return compound;
        }

        public static StateRequirement deserialize(CompoundNBT tag)
        {
            List<Either<StateProperty, RangedProperty>> properties = NBTHelper.listTagOf(tag.getList("properties", 10)).stream().map(tg -> {
                CompoundNBT compound = (CompoundNBT) tg;
                if (compound.contains("value"))
                {   return Either.<StateProperty, RangedProperty>left(StateProperty.deserialize(compound));
                }
                else
                {   return Either.<StateProperty, RangedProperty>right(RangedProperty.deserialize(compound));
                }
            }).collect(Collectors.toList());
            return new StateRequirement(properties);
        }

        public boolean matches(BlockState pState)
        {   return this.matches(pState.getBlock().getStateDefinition(), pState);
        }

        public boolean matches(FluidState pState)
        {   return this.matches(pState.getType().getStateDefinition(), pState);
        }

        public <S extends StateHolder<?, S>> boolean matches(StateContainer<?, S> pProperties, S pTargetProperty)
        {
            for(Either<StateProperty, RangedProperty> property : this.properties)
            {
                if (!property.map(
                    stateProperty -> stateProperty.match(pProperties, pTargetProperty),
                    rangedProperty -> rangedProperty.match(pProperties, pTargetProperty)))
                {   return false;
                }
            }
            return true;
        }

        @Override
        public boolean equals(Object obj)
        {
            if (this == obj)
            {   return true;
            }
            if (obj == null || getClass() != obj.getClass())
            {   return false;
            }

            StateRequirement that = (StateRequirement) obj;

            return properties.equals(that.properties);
        }
    }

    public static class StateProperty
    {
        private final String name;
        private final String value;

        public StateProperty(String name, String value)
        {
            this.name = name;
            this.value = value;
        }
        public static final Codec<StateProperty> CODEC = RecordCodecBuilder.create(instance -> instance.group(
                Codec.STRING.fieldOf("name").forGetter(property -> property.name),
                Codec.STRING.fieldOf("value").forGetter(property -> property.value)
        ).apply(instance, StateProperty::new));

        public CompoundNBT serialize()
        {   CompoundNBT compound = new CompoundNBT();
            compound.putString("name", this.name);
            compound.putString("value", this.value);
            return compound;
        }

        public static StateProperty deserialize(CompoundNBT tag)
        {   return new StateProperty(tag.getString("name"), tag.getString("value"));
        }

        public <S extends StateHolder<?, S>> boolean match(StateContainer<?, S> pProperties, S pPropertyToMatch)
        {   Property<?> property = pProperties.getProperty(this.name);
            return property != null && this.match(pPropertyToMatch, property);
        }

        private <V extends Comparable<V>> boolean match(StateHolder<?, ?> properties, Property<V> property)
        {   V stateValue = properties.getValue(property);
            Optional<V> propValue = property.getValue(this.value);
            return propValue.isPresent() && stateValue.compareTo(propValue.get()) == 0;
        }

        @Override
        public boolean equals(Object obj)
        {
            if (this == obj)
            {   return true;
            }
            if (obj == null || getClass() != obj.getClass())
            {   return false;
            }

            StateProperty that = (StateProperty) obj;

            if (!name.equals(that.name))
            {   return false;
            }
            return value.equals(that.value);
        }
    }

    public static class RangedProperty
    {
        private final String name;
        private final String min;
        private final String max;

        public RangedProperty(String name, String min, String max)
        {
            this.name = name;
            this.min = min;
            this.max = max;
        }
        public static final Codec<RangedProperty> CODEC = RecordCodecBuilder.create(instance -> instance.group(
                Codec.STRING.fieldOf("name").forGetter(property -> property.name),
                Codec.STRING.fieldOf("min").forGetter(property -> property.min),
                Codec.STRING.fieldOf("max").forGetter(property -> property.max)
        ).apply(instance, RangedProperty::new));

        public CompoundNBT serialize()
        {   CompoundNBT compound = new CompoundNBT();
            compound.putString("name", this.name);
            compound.putString("min", this.min);
            compound.putString("max", this.max);
            return compound;
        }

        public static RangedProperty deserialize(CompoundNBT tag)
        {   return new RangedProperty(tag.getString("name"), tag.getString("min"), tag.getString("max"));
        }

        public <S extends StateHolder<?, S>> boolean match(StateContainer<?, S> pProperties, S pTargetProperty)
        {   Property<?> property = pProperties.getProperty(this.name);
            return property != null && this.match(pTargetProperty, property);
        }

        private <T extends Comparable<T>> boolean match(StateHolder<?, ?> pProperties, Property<T> pPropertyTarget)
        {
            T t = pProperties.getValue(pPropertyTarget);

            if (this.min != null)
            {   Optional<T> optional = pPropertyTarget.getValue(this.min);
                if (!optional.isPresent() || t.compareTo(optional.get()) < 0)
                {   return false;
                }
            }
            if (this.max != null)
            {   Optional<T> optional1 = pPropertyTarget.getValue(this.max);
                if (!optional1.isPresent() || t.compareTo(optional1.get()) > 0)
                {   return false;
                }
            }

            return true;
        }

        @Override
        public boolean equals(Object obj)
        {
            if (this == obj)
            {   return true;
            }
            if (obj == null || getClass() != obj.getClass())
            {   return false;
            }

            RangedProperty that = (RangedProperty) obj;

            if (!name.equals(that.name))
            {   return false;
            }
            if (!min.equals(that.min))
            {   return false;
            }
            return max.equals(that.max);
        }
    }

    @Override
    public String toString()
    {
        StringBuilder builder = new StringBuilder();
        builder.append("BlockRequirement{");
        blocks.ifPresent(blocks -> builder.append("blocks=").append(blocks));
        state.ifPresent(state -> builder.append("state=").append(state));
        nbt.ifPresent(nbt -> builder.append("nbt=").append(nbt));
        sturdyFace.ifPresent(face -> builder.append("has_sturdy_face=").append(face));
        withinWorldBounds.ifPresent(bounds -> builder.append("within_world_bounds=").append(bounds));
        replaceable.ifPresent(replaceable -> builder.append("replaceable=").append(replaceable));
        builder.append("negate=").append(negate);
        builder.append("}");

        return builder.toString();
    }
}
