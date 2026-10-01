package com.momosoftworks.coldsweat.data.codec.requirement.sub_type;

import com.google.common.collect.BiMap;
import com.google.common.collect.HashBiMap;
import com.mojang.serialization.Codec;
import com.mojang.serialization.MapCodec;
import com.momosoftworks.coldsweat.data.codec.requirement.EntityRequirement;
import net.minecraft.resources.Identifier;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.level.Level;
import net.minecraft.world.phys.Vec3;

import javax.annotation.Nullable;
import java.util.Map;
import java.util.function.Supplier;

public interface EntitySubRequirement
{
    BiMap<Identifier, Supplier<MapCodec<? extends EntitySubRequirement>>> REQUIREMENT_MAP = HashBiMap.create(Map.of(
            Identifier.withDefaultNamespace("variant"), () -> EntityVariantRequirement.CODEC,
            Identifier.withDefaultNamespace("fishing_hook"), () -> FishingHookRequirement.CODEC,
            Identifier.withDefaultNamespace("lightning_bolt"), () -> LightningBoltRequirement.CODEC,
            Identifier.withDefaultNamespace("piglin_neutral_armor"), () -> PiglinNeutralArmorRequirement.CODEC,
            Identifier.withDefaultNamespace("player"), () -> PlayerDataRequirement.getCodec(EntityRequirement.getCodec()),
            Identifier.withDefaultNamespace("raider"), () -> RaiderRequirement.CODEC,
            Identifier.withDefaultNamespace("slime"), () -> SlimeRequirement.CODEC,
            Identifier.withDefaultNamespace("snow_boots"), () -> SnowBootsRequirement.CODEC
    ));

    Codec<EntitySubRequirement> CODEC = Identifier.CODEC.dispatch(
            "type",
            requirement -> {
                Supplier<MapCodec<? extends EntitySubRequirement>> matchingSupplier = REQUIREMENT_MAP.values().stream()
                        .filter(supplier -> supplier.get().equals(requirement.getCodec()))
                        .findFirst()
                        .orElseThrow(() -> new IllegalStateException("Unknown requirement type: " + requirement.getClass()));
                return REQUIREMENT_MAP.inverse().get(matchingSupplier);
            },
            location -> {
                Supplier<MapCodec<? extends EntitySubRequirement>> supplier = REQUIREMENT_MAP.get(location);
                if (supplier == null) {
                    throw new IllegalStateException("Unknown requirement type: " + location);
                }
                return supplier.get();
            }
    );

    default Identifier getType() {
        return REQUIREMENT_MAP.inverse().get(this.getCodec());
    }

    boolean test(Entity entity, Level level, @Nullable Vec3 position);
    MapCodec<? extends EntitySubRequirement> getCodec();
}
