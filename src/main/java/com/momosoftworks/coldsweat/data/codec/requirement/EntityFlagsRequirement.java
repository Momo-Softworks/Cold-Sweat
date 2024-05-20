package com.momosoftworks.coldsweat.data.codec.requirement;

import com.mojang.serialization.Codec;
import com.mojang.serialization.codecs.RecordCodecBuilder;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.world.entity.AgeableMob;
import net.minecraft.world.entity.Entity;

import java.util.Optional;

public record EntityFlagsRequirement(Optional<Boolean> onFire, Optional<Boolean> sneaking, Optional<Boolean> sprinting, Optional<Boolean> swimming, Optional<Boolean> invisible, Optional<Boolean> glowing, Optional<Boolean> baby)
{
    public static final Codec<EntityFlagsRequirement> CODEC = RecordCodecBuilder.create(instance -> instance.group(
            Codec.BOOL.optionalFieldOf("is_on_fire").forGetter(predicate -> predicate.onFire),
            Codec.BOOL.optionalFieldOf("is_sneaking").forGetter(predicate -> predicate.sneaking),
            Codec.BOOL.optionalFieldOf("is_sprinting").forGetter(predicate -> predicate.sprinting),
            Codec.BOOL.optionalFieldOf("is_swimming").forGetter(predicate -> predicate.swimming),
            Codec.BOOL.optionalFieldOf("is_invisible").forGetter(predicate -> predicate.invisible),
            Codec.BOOL.optionalFieldOf("is_glowing").forGetter(predicate -> predicate.glowing),
            Codec.BOOL.optionalFieldOf("is_baby").forGetter(predicate -> predicate.baby)
    ).apply(instance, EntityFlagsRequirement::new));

    public boolean test(Entity entity)
    {
        return (onFire.isEmpty() || entity.isOnFire() == onFire.get())
            && (sneaking.isEmpty() || entity.isCrouching() == sneaking.get())
            && (sprinting.isEmpty() || entity.isSprinting() == sprinting.get())
            && (swimming.isEmpty() || entity.isInWater() == swimming.get())
            && (invisible.isEmpty() || entity.isInvisible() == invisible.get())
            && (glowing.isEmpty() || entity.isCurrentlyGlowing() == glowing.get())
            && (baby.isEmpty() || (entity instanceof AgeableMob mob && mob.isBaby()) == baby.get());
    }

    public CompoundTag serialize()
    {
        CompoundTag tag = new CompoundTag();
        onFire.ifPresent(value -> tag.putBoolean("is_on_fire", value));
        sneaking.ifPresent(value -> tag.putBoolean("is_sneaking", value));
        sprinting.ifPresent(value -> tag.putBoolean("is_sprinting", value));
        swimming.ifPresent(value -> tag.putBoolean("is_swimming", value));
        invisible.ifPresent(value -> tag.putBoolean("is_invisible", value));
        glowing.ifPresent(value -> tag.putBoolean("is_glowing", value));
        baby.ifPresent(value -> tag.putBoolean("is_baby", value));
        return tag;
    }

    public static EntityFlagsRequirement deserialize(CompoundTag tag)
    {
        return new EntityFlagsRequirement(
            tag.contains("is_on_fire") ? Optional.of(tag.getBoolean("is_on_fire")) : Optional.empty(),
            tag.contains("is_sneaking") ? Optional.of(tag.getBoolean("is_sneaking")) : Optional.empty(),
            tag.contains("is_sprinting") ? Optional.of(tag.getBoolean("is_sprinting")) : Optional.empty(),
            tag.contains("is_swimming") ? Optional.of(tag.getBoolean("is_swimming")) : Optional.empty(),
            tag.contains("is_invisible") ? Optional.of(tag.getBoolean("is_invisible")) : Optional.empty(),
            tag.contains("is_glowing") ? Optional.of(tag.getBoolean("is_glowing")) : Optional.empty(),
            tag.contains("is_baby") ? Optional.of(tag.getBoolean("is_baby")) : Optional.empty()
        );
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

        EntityFlagsRequirement that = (EntityFlagsRequirement) obj;

        return onFire.equals(that.onFire)
            && sneaking.equals(that.sneaking)
            && sprinting.equals(that.sprinting)
            && swimming.equals(that.swimming)
            && invisible.equals(that.invisible)
            && glowing.equals(that.glowing)
            && baby.equals(that.baby);
    }

    @Override
    public String toString()
    {
        StringBuilder builder = new StringBuilder();
        onFire.ifPresent(value -> builder.append("is_on_fire=").append(value));
        sneaking.ifPresent(value -> builder.append(", is_sneaking=").append(value));
        sprinting.ifPresent(value -> builder.append(", is_sprinting=").append(value));
        swimming.ifPresent(value -> builder.append(", is_swimming=").append(value));
        invisible.ifPresent(value -> builder.append(", is_invisible=").append(value));
        glowing.ifPresent(value -> builder.append(", is_glowing=").append(value));
        baby.ifPresent(value -> builder.append(", is_baby=").append(value));

        return builder.toString();
    }
}
