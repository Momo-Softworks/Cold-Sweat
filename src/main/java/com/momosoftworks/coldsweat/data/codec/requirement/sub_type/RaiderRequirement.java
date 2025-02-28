package com.momosoftworks.coldsweat.data.codec.requirement.sub_type;

import com.mojang.serialization.Codec;
import com.mojang.serialization.MapCodec;
import com.mojang.serialization.codecs.RecordCodecBuilder;
import net.minecraft.entity.Entity;
import net.minecraft.entity.monster.AbstractRaiderEntity;
import net.minecraft.inventory.EquipmentSlotType;
import net.minecraft.item.ItemStack;
import net.minecraft.util.math.vector.Vector3d;
import net.minecraft.world.World;
import net.minecraft.world.raid.Raid;

import javax.annotation.Nullable;
import java.util.Objects;

public final class RaiderRequirement implements EntitySubRequirement
{
    private final boolean hasRaid;
    private final boolean isCaptain;

    public RaiderRequirement(boolean hasRaid, boolean isCaptain)
    {
        this.hasRaid = hasRaid;
        this.isCaptain = isCaptain;
    }

    public static final MapCodec<RaiderRequirement> CODEC = RecordCodecBuilder.mapCodec(instance -> instance.group(
            Codec.BOOL.optionalFieldOf("has_raid", false).forGetter(RaiderRequirement::hasRaid),
            Codec.BOOL.optionalFieldOf("is_captain", false).forGetter(RaiderRequirement::isCaptain)
    ).apply(instance, RaiderRequirement::new));

    public boolean hasRaid()
    {   return hasRaid;
    }
    public boolean isCaptain()
    {   return isCaptain;
    }

    @Override
    public MapCodec<? extends EntitySubRequirement> getCodec()
    {   return CODEC;
    }

    @Override
    public boolean test(Entity entity, World level, @Nullable Vector3d position)
    {
        return entity instanceof AbstractRaiderEntity
                && ((AbstractRaiderEntity) entity).hasActiveRaid() == this.hasRaid
                && isCaptain(((AbstractRaiderEntity) entity)) == this.isCaptain;
    }

    private static boolean isCaptain(AbstractRaiderEntity raider)
    {
        ItemStack itemstack = raider.getItemBySlot(EquipmentSlotType.HEAD);
        boolean wearingBanner = !itemstack.isEmpty() && ItemStack.matches(itemstack, Raid.getLeaderBannerInstance());
        return wearingBanner && raider.isPatrolLeader();
    }

    @Override
    public boolean equals(Object obj)
    {
        if (obj == this) return true;
        if (obj == null || obj.getClass() != this.getClass()) return false;
        RaiderRequirement that = (RaiderRequirement) obj;
        return this.hasRaid == that.hasRaid &&
                this.isCaptain == that.isCaptain;
    }
}
