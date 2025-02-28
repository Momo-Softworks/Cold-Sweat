package com.momosoftworks.coldsweat.data.codec.requirement.sub_type;

import com.google.common.collect.BiMap;
import com.google.common.collect.HashBiMap;
import com.mojang.serialization.Codec;
import com.mojang.serialization.MapCodec;
import com.momosoftworks.coldsweat.data.codec.requirement.EntityRequirement;
import com.momosoftworks.coldsweat.data.codec.requirement.PlayerDataRequirement;
import com.momosoftworks.coldsweat.util.math.CSMath;
import net.minecraft.entity.Entity;
import net.minecraft.util.ResourceLocation;
import net.minecraft.util.math.vector.Vector3d;
import net.minecraft.world.World;

import javax.annotation.Nullable;

public interface EntitySubRequirement
{
    BiMap<ResourceLocation, MapCodec<? extends EntitySubRequirement>> REQUIREMENT_MAP = HashBiMap.create(CSMath.mapOf(
            new ResourceLocation("fishing_hook"), FishingHookRequirement.CODEC,
            new ResourceLocation("piglin_neutral_armor"), PiglinNeutralArmorRequirement.CODEC,
            new ResourceLocation("player"), PlayerDataRequirement.getCodec(EntityRequirement.getCodec()),
            new ResourceLocation("raider"), RaiderRequirement.CODEC,
            new ResourceLocation("slime"), SlimeRequirement.CODEC
    ));

    Codec<EntitySubRequirement> CODEC = ResourceLocation.CODEC.dispatch("type",
    requirement -> REQUIREMENT_MAP.inverse().get(requirement.getCodec()),
    REQUIREMENT_MAP::get);

    boolean test(Entity entity, World level, @Nullable Vector3d position);
    MapCodec<? extends EntitySubRequirement> getCodec();
}
