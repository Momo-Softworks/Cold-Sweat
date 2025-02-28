package com.momosoftworks.coldsweat.data.codec.requirement.sub_type;

import com.mojang.serialization.Codec;
import com.mojang.serialization.MapCodec;
import com.mojang.serialization.codecs.RecordCodecBuilder;
import net.minecraft.entity.Entity;
import net.minecraft.entity.projectile.FishingBobberEntity;
import net.minecraft.util.math.vector.Vector3d;
import net.minecraft.world.World;

import javax.annotation.Nullable;
import java.util.Optional;

public final class FishingHookRequirement implements EntitySubRequirement
{
    public static final FishingHookRequirement NONE = new FishingHookRequirement(Optional.empty());

    private final Optional<Boolean> inOpenWater;

    public FishingHookRequirement(Optional<Boolean> inOpenWater)
    {   this.inOpenWater = inOpenWater;
    }

    public static final MapCodec<FishingHookRequirement> CODEC = RecordCodecBuilder.mapCodec(instance -> instance.group(
            Codec.BOOL.optionalFieldOf("in_open_water").forGetter(FishingHookRequirement::inOpenWater)
    ).apply(instance, FishingHookRequirement::new));

    public Optional<Boolean> inOpenWater()
    {   return inOpenWater;
    }

    @Override
    public MapCodec<? extends EntitySubRequirement> getCodec()
    {   return CODEC;
    }

    @Override
    public boolean test(Entity entity, World level, @Nullable Vector3d position)
    {
        if (this.inOpenWater.isPresent())
        {   return entity instanceof FishingBobberEntity && this.inOpenWater.get() == ((FishingBobberEntity) entity).isOpenWaterFishing();
        }
        return true;
    }
}
