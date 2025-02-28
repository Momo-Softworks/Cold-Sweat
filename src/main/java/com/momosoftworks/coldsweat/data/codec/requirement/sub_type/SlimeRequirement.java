package com.momosoftworks.coldsweat.data.codec.requirement.sub_type;

import com.mojang.serialization.MapCodec;
import com.mojang.serialization.codecs.RecordCodecBuilder;
import com.momosoftworks.coldsweat.data.codec.util.IntegerBounds;
import net.minecraft.entity.Entity;
import net.minecraft.entity.monster.SlimeEntity;
import net.minecraft.util.math.vector.Vector3d;
import net.minecraft.world.World;

import javax.annotation.Nullable;

public final class SlimeRequirement implements EntitySubRequirement
{
    private final IntegerBounds size;

    public SlimeRequirement(IntegerBounds size)
    {   this.size = size;
    }

    public static final MapCodec<SlimeRequirement> CODEC = RecordCodecBuilder.mapCodec(instance -> instance.group(
            IntegerBounds.CODEC.optionalFieldOf("size", IntegerBounds.NONE).forGetter(SlimeRequirement::size)
    ).apply(instance, SlimeRequirement::new));

    public IntegerBounds size()
    {   return size;
    }

    @Override
    public MapCodec<? extends EntitySubRequirement> getCodec()
    {   return CODEC;
    }

    @Override
    public boolean test(Entity entity, World level, @Nullable Vector3d position)
    {   return entity instanceof SlimeEntity && this.size.test(((SlimeEntity) entity).getSize());
    }
}