package com.momosoftworks.coldsweat.data.codec.requirement;

import com.mojang.datafixers.util.Either;
import com.mojang.serialization.Codec;
import com.mojang.serialization.codecs.RecordCodecBuilder;
import com.momosoftworks.coldsweat.util.serialization.ConfigHelper;
import com.momosoftworks.coldsweat.util.serialization.NBTHelper;
import com.momosoftworks.coldsweat.util.serialization.RegistryHelper;
import net.minecraft.core.BlockPos;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.core.registries.Registries;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.nbt.NbtOps;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.tags.TagKey;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.material.Fluid;
import net.minecraft.world.level.material.FluidState;

import java.util.List;
import java.util.Optional;
import java.util.stream.Collectors;

public record FluidRequirement(Optional<List<Either<TagKey<Fluid>, Fluid>>> fluids, Optional<TagKey<Fluid>> tag, Optional<BlockRequirement.StateRequirement> state, Optional<NbtRequirement> nbt)
{
    public static final Codec<FluidRequirement> CODEC = RecordCodecBuilder.create(instance -> instance.group(
            Codec.either(ConfigHelper.tagOrBuiltinCodec(Registries.FLUID, BuiltInRegistries.FLUID).listOf(),
                         ConfigHelper.tagOrBuiltinCodec(Registries.FLUID, BuiltInRegistries.FLUID))
            .xmap(either -> either.map(l -> l, r -> List.of(r)),
                  either -> either.size() == 1 ? Either.right(either.get(0)) : Either.left(either))
            .optionalFieldOf("fluids").forGetter(predicate -> predicate.fluids),
            TagKey.codec(Registries.FLUID).optionalFieldOf("tag").forGetter(predicate -> predicate.tag),
            BlockRequirement.StateRequirement.CODEC.optionalFieldOf("state").forGetter(predicate -> predicate.state),
            NbtRequirement.CODEC.optionalFieldOf("nbt").forGetter(predicate -> predicate.nbt)
    ).apply(instance, FluidRequirement::new));

    public boolean test(Level level, BlockPos pos)
    {
        if (!level.isLoaded(pos))
        {   return false;
        }
        else
        {
            FluidState flState = level.getFluidState(pos);
            if (this.tag.isPresent() && !flState.is(this.tag.get()))
            {   return false;
            }
            else if (this.fluids.isPresent() && !RegistryHelper.mapRegistryTagList(Registries.FLUID, fluids.get(), level.registryAccess()).contains(flState.getType()))
            {   return false;
            }
            else
            {   return this.state.isEmpty() || state.get().matches(flState);
            }
        }
    }

    public CompoundTag serialize()
    {   return (CompoundTag) CODEC.encodeStart(NbtOps.INSTANCE, this).result().orElseGet(CompoundTag::new);
    }

    public static FluidRequirement deserialize(CompoundTag tag)
    {   return CODEC.decode(NbtOps.INSTANCE, tag).result().orElseThrow(() -> new IllegalArgumentException("Could not deserialize FluidRequirement")).getFirst();
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

        FluidRequirement that = (FluidRequirement) obj;

        if (!fluids.equals(that.fluids))
        {   return false;
        }
        if (!tag.equals(that.tag))
        {   return false;
        }
        if (!state.equals(that.state))
        {   return false;
        }
        return nbt.equals(that.nbt);
    }

    /*@Override
    public String toString()
    {
        StringBuilder lBuilder = new StringBuilder();
        this.fluids.ifPresent(fluids -> lBuilder.append("Fluids: ").append(fluids.toString()));
        this.tag.ifPresent(tag -> lBuilder.append("Tag: ").append(tag.toString()));
        this.state.ifPresent(state -> lBuilder.append("State: ").append(state.toString()));
        this.nbt.ifPresent(nbt -> lBuilder.append("NBT: ").append(nbt.toString()));

        return lBuilder.toString();
    }*/
}
