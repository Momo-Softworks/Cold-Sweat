package com.momosoftworks.coldsweat.data.codec.requirement.sub_type;

import com.mojang.serialization.MapCodec;
import net.minecraft.entity.Entity;
import net.minecraft.entity.LivingEntity;
import net.minecraft.item.ItemStack;
import net.minecraft.util.math.vector.Vector3d;
import net.minecraft.world.World;

import javax.annotation.Nullable;

public class PiglinNeutralArmorRequirement implements EntitySubRequirement
{
    public static final PiglinNeutralArmorRequirement INSTANCE = new PiglinNeutralArmorRequirement();
    public static final MapCodec<PiglinNeutralArmorRequirement> CODEC = MapCodec.unit(INSTANCE);

    private PiglinNeutralArmorRequirement() {}

    @Override
    public MapCodec<? extends EntitySubRequirement> getCodec()
    {   return CODEC;
    }

    @Override
    public boolean test(Entity entity, World level, @Nullable Vector3d position)
    {
        if (entity instanceof LivingEntity)
        {
            LivingEntity living = (LivingEntity) entity;
            for (ItemStack armor : living.getArmorSlots())
            {
                if (!armor.isEmpty() && armor.makesPiglinsNeutral(living))
                {   return true;
                }
            }
        }
        return false;
    }
}
