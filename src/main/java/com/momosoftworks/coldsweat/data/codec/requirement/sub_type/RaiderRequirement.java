package com.momosoftworks.coldsweat.data.codec.requirement.sub_type;

import com.mojang.serialization.Codec;
import com.mojang.serialization.MapCodec;
import com.mojang.serialization.codecs.RecordCodecBuilder;
import net.minecraft.world.entity.Entity;
import com.momosoftworks.coldsweat.data.codec.util.DoubleBounds;
import com.momosoftworks.coldsweat.data.codec.util.IntegerBounds;
import net.minecraft.world.entity.raid.Raid;
import net.minecraft.world.entity.raid.Raider;
import net.minecraft.world.level.Level;
import net.minecraft.world.phys.Vec3;

import javax.annotation.Nullable;
import java.util.Optional;

public record RaiderRequirement(Optional<Boolean> hasRaid, Optional<Boolean> isCaptain, Optional<RaidData> raid) implements EntitySubRequirement
{
    public static final MapCodec<RaiderRequirement> CODEC = RecordCodecBuilder.mapCodec(instance -> instance.group(
            Codec.BOOL.optionalFieldOf("has_raid").forGetter(RaiderRequirement::hasRaid),
            Codec.BOOL.optionalFieldOf("is_captain").forGetter(RaiderRequirement::isCaptain),
            RaidData.CODEC.optionalFieldOf("raid").forGetter(RaiderRequirement::raid)
    ).apply(instance, RaiderRequirement::new));

    @Override
    public MapCodec<? extends EntitySubRequirement> getCodec()
    {   return CODEC;
    }

    @Override
    public boolean test(Entity entity, Level level, @Nullable Vec3 position)
    {
        return entity instanceof Raider raider
            && this.hasRaid.map(bool -> bool == raider.hasActiveRaid()).orElse(true)
            && this.isCaptain.map(bool -> bool == raider.isCaptain()).orElse(true)
            && this.raid.map(raidData -> raidData.test(raider.getCurrentRaid())).orElse(true);
    }

    public record RaidData(Optional<Boolean> isOver, Optional<Boolean> isBetweenWaves, Optional<Boolean> isFirstWaveSpawned,
                           Optional<Boolean> isVictory, Optional<Boolean> isLoss, Optional<Boolean> isStarted,
                           Optional<Boolean> isStopped, Optional<DoubleBounds> totalHealth, Optional<DoubleBounds> badOmenLevel,
                           Optional<IntegerBounds> currentWave)
    {
        public static final Codec<RaidData> CODEC = RecordCodecBuilder.create(instance -> instance.group(
                Codec.BOOL.optionalFieldOf("is_over").forGetter(RaidData::isOver),
                Codec.BOOL.optionalFieldOf("between_waves").forGetter(RaidData::isBetweenWaves),
                Codec.BOOL.optionalFieldOf("first_wave_spawned").forGetter(RaidData::isFirstWaveSpawned),
                Codec.BOOL.optionalFieldOf("is_victory").forGetter(RaidData::isVictory),
                Codec.BOOL.optionalFieldOf("is_loss").forGetter(RaidData::isLoss),
                Codec.BOOL.optionalFieldOf("is_started").forGetter(RaidData::isStarted),
                Codec.BOOL.optionalFieldOf("is_stopped").forGetter(RaidData::isStopped),
                DoubleBounds.CODEC.optionalFieldOf("total_health").forGetter(RaidData::totalHealth),
                DoubleBounds.CODEC.optionalFieldOf("bad_omen_level").forGetter(RaidData::badOmenLevel),
                IntegerBounds.CODEC.optionalFieldOf("current_wave").forGetter(RaidData::currentWave)
        ).apply(instance, RaidData::new));

        public boolean test(Raid raid)
        {
            return this.isOver.map(bool -> bool == raid.isOver()).orElse(true)
                && this.isBetweenWaves.map(bool -> bool == raid.isBetweenWaves()).orElse(true)
                && this.isFirstWaveSpawned.map(bool -> bool == raid.hasFirstWaveSpawned()).orElse(true)
                && this.isVictory.map(bool -> bool == raid.isVictory()).orElse(true)
                && this.isLoss.map(bool -> bool == raid.isLoss()).orElse(true)
                && this.isStarted.map(bool -> bool == raid.isStarted()).orElse(true)
                && this.isStopped.map(bool -> bool == raid.isStopped()).orElse(true)
                && this.totalHealth.map(val -> val.test(raid.getTotalHealth())).orElse(true)
                && this.badOmenLevel.map(val -> val.test(raid.getRaidOmenLevel())).orElse(true)
                && this.currentWave.map(val -> val.test(raid.getGroupsSpawned())).orElse(true);
        }
    }
}
