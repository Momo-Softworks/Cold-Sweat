package com.momosoftworks.coldsweat.data.codec.requirement;

import com.mojang.datafixers.util.Either;
import com.mojang.serialization.Codec;
import com.mojang.serialization.JsonOps;
import com.mojang.serialization.codecs.RecordCodecBuilder;
import com.momosoftworks.coldsweat.data.codec.util.NegatableList;
import com.momosoftworks.coldsweat.util.serialization.ConfigHelper;
import net.minecraft.core.BlockPos;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.core.registries.Registries;
import net.minecraft.tags.TagKey;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.material.Fluid;
import net.minecraft.world.level.material.FluidState;

import java.util.Optional;

public record FluidRequirement(NegatableList<Either<TagKey<Fluid>, Fluid>> fluids, BlockRequirement.StateRequirement state, Optional<Boolean> isSource)
{
    public static final FluidRequirement NONE = new FluidRequirement(new NegatableList<>(), BlockRequirement.StateRequirement.NONE, Optional.empty());

    public static final Codec<FluidRequirement> CODEC = RecordCodecBuilder.create(instance -> instance.group(
            NegatableList.listCodec(ConfigHelper.tagOrBuiltinCodec(Registries.FLUID, BuiltInRegistries.FLUID)).optionalFieldOf("fluids", new NegatableList<>()).forGetter(FluidRequirement::fluids),
            BlockRequirement.StateRequirement.CODEC.optionalFieldOf("state", BlockRequirement.StateRequirement.NONE).forGetter(FluidRequirement::state),
            Codec.BOOL.optionalFieldOf("is_source").forGetter(FluidRequirement::isSource)
    ).apply(instance, FluidRequirement::new));

    public boolean test(Level level, BlockPos pos)
    {
        if (!level.isLoaded(pos))
        {   return true;
        }
        else
        {   FluidState lState = level.getFluidState(pos);
            return this.test(lState);
        }
    }

    public boolean test(FluidState state)
    {
        if (!this.fluids.test(either -> either.map(state::is, state::is)))
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
