package com.momosoftworks.coldsweat.data.codec.requirement.sub_type;

import com.mojang.serialization.Codec;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.level.Level;
import net.minecraft.world.phys.Vec3;

import javax.annotation.Nullable;

public interface EntitySubRequirement
{
    Codec<EntitySubRequirement> CODEC = ResourceLocation.CODEC.dispatch("type",
    rq -> switch (rq.getClass().getSimpleName())
    {
        case "EntityVariantRequirement" -> ResourceLocation.withDefaultNamespace("variant");
        case "FishingHookRequirement" -> ResourceLocation.withDefaultNamespace("fishing_hook");
        case "LightningBoltRequirement" -> ResourceLocation.withDefaultNamespace("lightning_bolt");
        case "PiglinNeutralArmorRequirement" -> ResourceLocation.withDefaultNamespace("piglin_neutral_armor");
        case "PlayerDataRequirement" -> ResourceLocation.withDefaultNamespace("player");
        case "RaiderRequirement" -> ResourceLocation.withDefaultNamespace("raider");
        case "SlimeRequirement" -> ResourceLocation.withDefaultNamespace("slime");
        case "SnowBootsRequirement" -> ResourceLocation.withDefaultNamespace("snow_boots");
        default -> throw new IllegalStateException("Unexpected value: " + rq.getClass().getSimpleName());
    },
    rl -> switch (rl.getPath())
    {
        case "variant" -> EntityVariantRequirement.CODEC;
        case "fishing_hook" -> FishingHookRequirement.CODEC;
        case "lightning" -> LightningBoltRequirement.CODEC;
        case "piglin_neutral_armor" -> PiglinNeutralArmorRequirement.CODEC;
        case "player" -> PlayerDataRequirement.getCodec();
        case "raider" -> RaiderRequirement.CODEC;
        case "slime" -> SlimeRequirement.CODEC;
        case "snow_boots" -> SnowBootsRequirement.CODEC;
        default -> null;
    });

    boolean test(Entity entity, Level level, @Nullable Vec3 position);
}
