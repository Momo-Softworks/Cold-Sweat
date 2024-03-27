package com.momosoftworks.coldsweat.data.codec.requirement;

import com.mojang.datafixers.util.Either;
import com.mojang.serialization.Codec;
import com.mojang.serialization.codecs.RecordCodecBuilder;
import com.momosoftworks.coldsweat.data.codec.util.IntegerBounds;
import com.momosoftworks.coldsweat.util.entity.EntityHelper;
import net.minecraft.advancements.AdvancementProgress;
import net.minecraft.advancements.CriterionProgress;
import net.minecraft.entity.Entity;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.entity.player.ServerPlayerEntity;
import net.minecraft.entity.projectile.ProjectileHelper;
import net.minecraft.nbt.CompoundNBT;
import net.minecraft.nbt.ListNBT;
import net.minecraft.stats.Stat;
import net.minecraft.stats.StatType;
import net.minecraft.util.ResourceLocation;
import net.minecraft.util.math.AxisAlignedBB;
import net.minecraft.util.math.EntityRayTraceResult;
import net.minecraft.util.math.RayTraceResult;
import net.minecraft.util.math.vector.Vector3d;
import net.minecraft.util.registry.Registry;
import net.minecraft.world.GameType;
import net.minecraftforge.registries.ForgeRegistries;

import java.util.Map;
import java.util.Optional;
import java.util.stream.Collectors;

public class PlayerDataRequirement
{
    public final Optional<GameType> gameType;
    public final Optional<Map<StatRequirement, IntegerBounds>> stats;
    public final Optional<Map<ResourceLocation, Boolean>> recipes;
    public final Optional<Map<ResourceLocation, Either<AdvancementCompletionRequirement, AdvancementCriteriaRequirement>>> advancements;
    public final Optional<EntityRequirement> lookingAt;
    
    public PlayerDataRequirement(Optional<GameType> gameType, Optional<Map<StatRequirement, IntegerBounds>> stats,
                                 Optional<Map<ResourceLocation, Boolean>> recipes,
                                 Optional<Map<ResourceLocation, Either<AdvancementCompletionRequirement, AdvancementCriteriaRequirement>>> advancements,
                                 Optional<EntityRequirement> lookingAt)
    {
        this.gameType = gameType;
        this.stats = stats;
        this.recipes = recipes;
        this.advancements = advancements;
        this.lookingAt = lookingAt;
    }
    public static final Codec<PlayerDataRequirement> CODEC = RecordCodecBuilder.create(instance -> instance.group(
            Codec.STRING.xmap(GameType::byName, GameType::getName).optionalFieldOf("gameType").forGetter(requirement -> requirement.gameType),
            Codec.unboundedMap(StatRequirement.CODEC, IntegerBounds.CODEC).optionalFieldOf("stats").forGetter(requirement -> requirement.stats),
            Codec.unboundedMap(ResourceLocation.CODEC, Codec.BOOL).optionalFieldOf("recipes").forGetter(requirement -> requirement.recipes),
            Codec.unboundedMap(ResourceLocation.CODEC, Codec.either(AdvancementCompletionRequirement.CODEC, AdvancementCriteriaRequirement.CODEC)).optionalFieldOf("advancements").forGetter(requirement -> requirement.advancements),
            EntityRequirement.getCodec().optionalFieldOf("lookingAt").forGetter(requirement -> requirement.lookingAt)
    ).apply(instance, PlayerDataRequirement::new));

    public static Codec<PlayerDataRequirement> getCodec(Codec<EntityRequirement> lastCodec)
    {
        return RecordCodecBuilder.create(instance -> instance.group(
                Codec.STRING.xmap(GameType::byName, GameType::getName).optionalFieldOf("gameType").forGetter(requirement -> requirement.gameType),
                Codec.unboundedMap(StatRequirement.CODEC, IntegerBounds.CODEC).optionalFieldOf("stats").forGetter(requirement -> requirement.stats),
                Codec.unboundedMap(ResourceLocation.CODEC, Codec.BOOL).optionalFieldOf("recipes").forGetter(requirement -> requirement.recipes),
                Codec.unboundedMap(ResourceLocation.CODEC, Codec.either(AdvancementCompletionRequirement.CODEC, AdvancementCriteriaRequirement.CODEC)).optionalFieldOf("advancements").forGetter(requirement -> requirement.advancements),
                lastCodec.optionalFieldOf("lookingAt").forGetter(requirement -> requirement.lookingAt)
        ).apply(instance, PlayerDataRequirement::new));
    }

    public boolean test(Entity entity)
    {
        if (!(entity instanceof PlayerEntity)) return false;
        PlayerEntity player = (PlayerEntity) entity;
        ServerPlayerEntity serverPlayer = EntityHelper.getServerPlayer(player);

        if (gameType.isPresent() && EntityHelper.getGameModeForPlayer(player) != gameType.get())
        {   return false;
        }
        if (stats.isPresent())
        {
            for (Map.Entry<StatRequirement, IntegerBounds> entry : stats.get().entrySet())
            {
                int value = serverPlayer.getStats().getValue(entry.getKey().stat());
                if (!entry.getKey().test(entry.getKey().stat(), value))
                {   return false;
                }
            }
        }
        if (recipes.isPresent())
        {
            for (Map.Entry<ResourceLocation, Boolean> entry : recipes.get().entrySet())
            {
                if (serverPlayer.getRecipeBook().contains(entry.getKey()) != entry.getValue())
                {   return false;
                }
            }
        }
        if (advancements.isPresent())
        {
            for (Map.Entry<ResourceLocation, Either<AdvancementCompletionRequirement, AdvancementCriteriaRequirement>> entry : advancements.get().entrySet())
            {
                AdvancementProgress progress = serverPlayer.getAdvancements().getOrStartProgress(serverPlayer.getServer().getAdvancements().getAdvancement(entry.getKey()));
                if (entry.getValue().map(complete -> complete.test(progress), criteria -> criteria.test(progress)))
                {   return false;
                }
            }
        }
        if (lookingAt.isPresent())
        {
            Vector3d vec3 = player.getEyePosition(0);
            Vector3d vec31 = player.getViewVector(1.0F);
            Vector3d vec32 = vec3.add(vec31.x * 100.0D, vec31.y * 100.0D, vec31.z * 100.0D);
            EntityRayTraceResult entityhitresult = ProjectileHelper.getEntityHitResult(player.level, player, vec3, vec32, (new AxisAlignedBB(vec3, vec32)).inflate(1.0D), (ent) -> !ent.isSpectator());
            if (entityhitresult == null || entityhitresult.getType() != RayTraceResult.Type.ENTITY)
            {   return false;
            }

            Entity hitEntity = entityhitresult.getEntity();
            if (!this.lookingAt.get().test(hitEntity) || !player.canSee(hitEntity))
            {   return false;
            }
        }
        return true;
    }

    public CompoundNBT serialize()
    {
        CompoundNBT tag = new CompoundNBT();
        gameType.ifPresent(gameType -> tag.putString("gameType", gameType.getName()));
        stats.ifPresent(stats ->
        {
            ListNBT statsTag = new ListNBT();
            for (Map.Entry<StatRequirement, IntegerBounds> entry : stats.entrySet())
            {
                CompoundNBT statTag = new CompoundNBT();
                statTag.put("stat", entry.getKey().serialize());
                statTag.put("value", entry.getValue().serialize());
                statsTag.add(statTag);
            }
            tag.put("stats", statsTag);
        });
        recipes.ifPresent(recipes ->
        {
            CompoundNBT recipesTag = new CompoundNBT();
            for (Map.Entry<ResourceLocation, Boolean> entry : recipes.entrySet())
            {   recipesTag.putBoolean(entry.getKey().toString(), entry.getValue());
            }
            tag.put("recipes", recipesTag);
        });
        advancements.ifPresent(advancements ->
        {
            CompoundNBT advancementsTag = new CompoundNBT();
            for (Map.Entry<ResourceLocation, Either<AdvancementCompletionRequirement, AdvancementCriteriaRequirement>> entry : advancements.entrySet())
            {
                CompoundNBT advancementTag = new CompoundNBT();
                entry.getValue().ifLeft(complete -> advancementTag.putBoolean("complete", complete.complete));
                entry.getValue().ifRight(criteria -> {
                    CompoundNBT criteriaTag = new CompoundNBT();
                    for (Map.Entry<String, Boolean> criteriaEntry : criteria.criteria.entrySet())
                    {
                        criteriaTag.putBoolean(criteriaEntry.getKey(), criteriaEntry.getValue());
                    }
                    advancementTag.put("criteria", criteriaTag);
                });
                advancementsTag.put(entry.getKey().toString(), advancementTag);
            }
            tag.put("advancements", advancementsTag);
        });
        lookingAt.ifPresent(lookingAt -> tag.put("lookingAt", lookingAt.serialize()));

        return tag;
    }

    public static PlayerDataRequirement deserialize(CompoundNBT tag)
    {
        Optional<GameType> gameType = tag.contains("gameType") ? Optional.of(GameType.byName(tag.getString("gameType"))) : Optional.empty();
        Optional<Map<StatRequirement, IntegerBounds>> stats = tag.contains("stats") ? Optional.of(tag.getList("stats", 10)
                .stream()
                .map(tg -> (CompoundNBT) tg)
                .collect(Collectors.<CompoundNBT, StatRequirement, IntegerBounds>toMap(tg -> StatRequirement.deserialize(tg.getCompound("stat")),
                                          tg -> IntegerBounds.deserialize(tg.getCompound("value"))))
        ) : Optional.empty();

        Optional<Map<ResourceLocation, Boolean>> recipes = tag.contains("recipes") ? Optional.of(tag.getCompound("recipes").getAllKeys().stream()
                .collect(java.util.stream.Collectors.toMap(key -> new ResourceLocation(key), key -> tag.getCompound("recipes").getBoolean(key)))) : Optional.empty();

        Optional<Map<ResourceLocation, Either<AdvancementCompletionRequirement, AdvancementCriteriaRequirement>>> advancements = tag.contains("advancements") ? Optional.of(tag.getCompound("advancements").getAllKeys().stream()
                .collect(java.util.stream.Collectors.toMap(key -> new ResourceLocation(key), key -> {
                    CompoundNBT advancementTag = tag.getCompound("advancements").getCompound(key);
                    if (advancementTag.contains("complete"))
                    {   return Either.left(new AdvancementCompletionRequirement(advancementTag.getBoolean("complete")));
                    }
                    else
                    {   return Either.right(new AdvancementCriteriaRequirement(advancementTag.getCompound("criteria").getAllKeys().stream()
                            .collect(Collectors.toMap(criteriaKey -> criteriaKey, criteriaKey -> advancementTag.getCompound("criteria").getBoolean(criteriaKey)))));
                    }
                }))) : Optional.empty();

        Optional<EntityRequirement> lookingAt = tag.contains("lookingAt") ? Optional.of(EntityRequirement.deserialize(tag.getCompound("lookingAt"))) : Optional.empty();

        return new PlayerDataRequirement(gameType, stats, recipes, advancements, lookingAt);
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

        PlayerDataRequirement that = (PlayerDataRequirement) obj;

        if (!gameType.equals(that.gameType))
        {   return false;
        }
        if (!stats.equals(that.stats))
        {   return false;
        }
        if (!recipes.equals(that.recipes))
        {   return false;
        }
        if (!advancements.equals(that.advancements))
        {   return false;
        }
        return lookingAt.equals(that.lookingAt);
    }

    public static class StatRequirement
    {
        private final StatType<?> type;
        private ResourceLocation statId;
        private final Stat<?> stat;
        private final IntegerBounds value;

        public static final Codec<StatRequirement> CODEC = RecordCodecBuilder.create(instance -> instance.group(
                Registry.STAT_TYPE.fieldOf("type").forGetter(stat -> stat.type),
                ResourceLocation.CODEC.fieldOf("stat").forGetter(stat -> stat.statId),
                IntegerBounds.CODEC.fieldOf("value").forGetter(stat -> stat.value)
        ).apply(instance, StatRequirement::new));

        public StatRequirement(StatType<?> type, ResourceLocation statId, IntegerBounds value)
        {   this(type, (Stat<?>) type.getRegistry().get(statId), value);
        }

        public StatRequirement(StatType<?> type, Stat<?> stat, IntegerBounds value)
        {   this.type = type;
            this.stat = stat;
            this.value = value;
        }

        public StatType<?> type()
        {   return this.type;
        }
        public Stat<?> stat()
        {   return this.stat;
        }
        public IntegerBounds value()
        {   return this.value;
        }

        public boolean test(Stat<?> stat, int value)
        {   return this.stat.equals(stat) && this.value.test(value);
        }

        public CompoundNBT serialize()
        {   CompoundNBT tag = new CompoundNBT();
            tag.putString("type", ForgeRegistries.STAT_TYPES.getKey(type).toString());
            tag.putString("stat", serializeStat(stat));
            tag.put("value", this.value.serialize());
            return tag;
        }

        public static StatRequirement deserialize(CompoundNBT tag)
        {   StatType<?> type = ForgeRegistries.STAT_TYPES.getValue(new ResourceLocation(tag.getString("type")));
            Stat<?> stat = deserializeStat(type, tag);
            IntegerBounds value = IntegerBounds.deserialize(tag.getCompound("value"));
            return new StatRequirement(type, stat, value);
        }

        private static <T> String serializeStat(Stat<T> stat)
        {   return stat.getType().getRegistry().getKey(stat.getValue()).toString();
        }

        private static <T> Stat<T> deserializeStat(StatType<T> type, CompoundNBT tag)
        {   return type.get(type.getRegistry().get(new ResourceLocation(tag.getString("stat"))));
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

            StatRequirement that = (StatRequirement) obj;

            if (!type.equals(that.type))
            {   return false;
            }
            if (!statId.equals(that.statId))
            {   return false;
            }
            if (!stat.equals(that.stat))
            {   return false;
            }
            return value.equals(that.value);
        }

        @Override
        public String toString()
        {
            return "Stat{" +
                    "type=" + type +
                    ", statId=" + statId +
                    ", stat=" + stat +
                    ", value=" + value +
                    '}';
        }
    }

    public static class AdvancementCompletionRequirement
    {
        private final Boolean complete;

        public AdvancementCompletionRequirement(Boolean complete)
        {   this.complete = complete;
        }

        public static final Codec<AdvancementCompletionRequirement> CODEC = Codec.BOOL.xmap(AdvancementCompletionRequirement::new, req -> req.complete);

        public boolean test(AdvancementProgress progress)
        {   return progress.isDone() == this.complete;
        }

        public CompoundNBT serialize()
        {   CompoundNBT tag = new CompoundNBT();
            tag.putBoolean("completion", complete);
            return tag;
        }

        public static AdvancementCompletionRequirement deserialize(CompoundNBT tag)
        {   return new AdvancementCompletionRequirement(tag.getBoolean("completion"));
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

            AdvancementCompletionRequirement that = (AdvancementCompletionRequirement) obj;

            return complete.equals(that.complete);

        }

        @Override
        public String toString()
        {
            return "Completion{" +
                    "complete=" + complete +
                    '}';
        }
    }
    public static class AdvancementCriteriaRequirement
    {
        private final Map<String, Boolean> criteria;

        public AdvancementCriteriaRequirement(Map<String, Boolean> criteria)
        {   this.criteria = criteria;
        }

        public static final Codec<AdvancementCriteriaRequirement> CODEC = Codec.unboundedMap(Codec.STRING, Codec.BOOL).xmap(AdvancementCriteriaRequirement::new, req -> req.criteria);

        public boolean test(AdvancementProgress progress)
        {
            for (Map.Entry<String, Boolean> entry : this.criteria.entrySet())
            {
                CriterionProgress criterionprogress = progress.getCriterion(entry.getKey());
                if (criterionprogress == null || criterionprogress.isDone() != entry.getValue())
                {   return false;
                }
            }
            return true;
        }

        public CompoundNBT serialize()
        {   CompoundNBT tag = new CompoundNBT();
            for (Map.Entry<String, Boolean> entry : this.criteria.entrySet())
            {   tag.putBoolean(entry.getKey(), entry.getValue());
            }
            return tag;
        }

        public static AdvancementCriteriaRequirement deserialize(CompoundNBT tag)
        {   return new AdvancementCriteriaRequirement(tag.getAllKeys().stream().collect(java.util.stream.Collectors.toMap(key -> key, key -> tag.getBoolean(key))));
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

            AdvancementCriteriaRequirement that = (AdvancementCriteriaRequirement) obj;

            return criteria.equals(that.criteria);
        }

        @Override
        public String toString()
        {
            return "Criteria{" +
                    "criteria=" + criteria +
                    '}';
        }
    }

    @Override
    public String toString()
    {
        return "PlayerData{" +
                "gameType=" + gameType +
                ", stats=" + stats +
                ", recipes=" + recipes +
                ", advancements=" + advancements +
                ", lookingAt=" + lookingAt +
                '}';
    }
}
