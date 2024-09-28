package com.momosoftworks.coldsweat.data.codec.requirement.sub_type;

import com.mojang.serialization.MapCodec;
import net.minecraft.advancements.critereon.FishingHookPredicate;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.projectile.FishingHook;
import net.minecraft.world.level.Level;
import net.minecraft.world.phys.Vec3;

import javax.annotation.Nullable;
import java.util.Optional;

public record FishingHookRequirement(Optional<Boolean> inOpenWater) implements EntitySubRequirement
{
    public static final FishingHookRequirement NONE = new FishingHookRequirement(Optional.empty());
    public static final MapCodec<FishingHookRequirement> CODEC = FishingHookPredicate.CODEC.xmap(FishingHookRequirement::new, requirement -> new FishingHookPredicate(requirement.inOpenWater));

    public FishingHookRequirement(FishingHookPredicate predicate)
    {   this(predicate.inOpenWater());
    }

    public static FishingHookRequirement inOpenWater(boolean pInOpenWater)
    {   return new FishingHookRequirement(Optional.of(pInOpenWater));
    }

    @Override
    public boolean test(Entity entity, Level level, @Nullable Vec3 position)
    {
        if (this.inOpenWater.isEmpty())
        {   return true;
        }
        else
        {   return entity instanceof FishingHook fishinghook && this.inOpenWater.get() == fishinghook.isOpenWaterFishing();
        }
    }
}
