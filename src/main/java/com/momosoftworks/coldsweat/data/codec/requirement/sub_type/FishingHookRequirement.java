package com.momosoftworks.coldsweat.data.codec.requirement.sub_type;

import com.mojang.serialization.Codec;
import com.mojang.serialization.MapCodec;
import com.mojang.serialization.codecs.RecordCodecBuilder;
import com.momosoftworks.coldsweat.data.codec.requirement.LocationRequirement;
import net.minecraft.entity.Entity;
import net.minecraft.entity.projectile.FishingBobberEntity;
import net.minecraft.util.math.vector.Vector3d;
import net.minecraft.world.World;

import javax.annotation.Nullable;
import java.util.Optional;

public final class FishingHookRequirement implements EntitySubRequirement
{
    public static final FishingHookRequirement NONE = new FishingHookRequirement(Optional.empty(), LocationRequirement.NONE);

    private final Optional<Boolean> inOpenWater;
    private final LocationRequirement location;

    public FishingHookRequirement(Optional<Boolean> inOpenWater, LocationRequirement location)
    {   this.inOpenWater = inOpenWater;
        this.location = location;
    }

    public static final MapCodec<FishingHookRequirement> CODEC = RecordCodecBuilder.mapCodec(instance -> instance.group(
            Codec.BOOL.optionalFieldOf("in_open_water").forGetter(FishingHookRequirement::inOpenWater),
            LocationRequirement.CODEC.optionalFieldOf("location", LocationRequirement.NONE).forGetter(FishingHookRequirement::location)
    ).apply(instance, FishingHookRequirement::new));

    public Optional<Boolean> inOpenWater()
    {   return inOpenWater;
    }
    public LocationRequirement location()
    {   return location;
    }

    @Override
    public MapCodec<? extends EntitySubRequirement> getCodec()
    {   return CODEC;
    }

    @Override
    public boolean test(Entity entity, World level, @Nullable Vector3d position)
    {
        return this.inOpenWater.map(val -> entity instanceof FishingBobberEntity && val == ((FishingBobberEntity) entity).isOpenWaterFishing()).orElse(true)
            && this.location.test(level, position);
    }
}
