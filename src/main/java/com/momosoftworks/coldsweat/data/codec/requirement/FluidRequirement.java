package com.momosoftworks.coldsweat.data.codec.requirement;

import com.mojang.datafixers.util.Either;
import com.mojang.serialization.Codec;
import com.mojang.serialization.JsonOps;
import com.mojang.serialization.codecs.RecordCodecBuilder;
import com.momosoftworks.coldsweat.util.serialization.ConfigHelper;
import net.minecraft.fluid.Fluid;
import net.minecraft.fluid.FluidState;
import net.minecraft.tags.ITag;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.registry.Registry;
import net.minecraft.world.World;

import java.util.Arrays;
import java.util.List;
import java.util.Optional;

public class FluidRequirement
{
    private final List<Either<ITag<Fluid>, Fluid>> fluids;
    private final BlockRequirement.StateRequirement state;
    private final Optional<Boolean> isSource;

    public FluidRequirement(List<Either<ITag<Fluid>, Fluid>> fluids, BlockRequirement.StateRequirement state, Optional<Boolean> isSource)
    {
        this.fluids = fluids;
        this.state = state;
        this.isSource = isSource;
    }

    public static final Codec<FluidRequirement> CODEC = RecordCodecBuilder.create(instance -> instance.group(
            ConfigHelper.tagOrBuiltinCodec(Registry.FLUID_REGISTRY, Registry.FLUID).listOf().optionalFieldOf("fluids", Arrays.asList()).forGetter(FluidRequirement::fluids),
            BlockRequirement.StateRequirement.CODEC.optionalFieldOf("state", BlockRequirement.StateRequirement.NONE).forGetter(FluidRequirement::state),
            Codec.BOOL.optionalFieldOf("is_source").forGetter(FluidRequirement::isSource)
    ).apply(instance, FluidRequirement::new));

    public List<Either<ITag<Fluid>, Fluid>> fluids()
    {   return fluids;
    }
    public BlockRequirement.StateRequirement state()
    {   return state;
    }
    public Optional<Boolean> isSource()
    {   return isSource;
    }

    public boolean test(World pLevel, BlockPos pPos)
    {
        if (!pLevel.isLoaded(pPos))
        {   return false;
        }
        else
        {   FluidState lState = pLevel.getFluidState(pPos);
            return this.test(lState);
        }
    }

    public boolean test(FluidState state)
    {
        if (this.fluids.stream().anyMatch(either -> either.map(tag -> state.is(tag), fluid -> state.getType() == fluid)))
        {   return false;
        }
        if (this.isSource.isPresent() && this.isSource.get() != state.isSource())
        {   return false;
        }
        else
        {   return this.state.test(state);
        }
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

        FluidRequirement that = (FluidRequirement) obj;
        return fluids.equals(that.fluids)
            && state.equals(that.state);
    }
}
