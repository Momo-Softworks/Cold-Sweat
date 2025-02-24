package com.momosoftworks.coldsweat.data.codec.requirement.sub_type;

import com.mojang.serialization.Codec;
import com.mojang.serialization.MapCodec;
import com.mojang.serialization.codecs.RecordCodecBuilder;
import com.momosoftworks.coldsweat.data.codec.util.DoubleBounds;
import com.momosoftworks.coldsweat.data.codec.util.IntegerBounds;
import net.minecraft.entity.Entity;
import net.minecraft.entity.monster.AbstractRaiderEntity;
import net.minecraft.inventory.EquipmentSlotType;
import net.minecraft.item.ItemStack;
import net.minecraft.util.math.vector.Vector3d;
import net.minecraft.world.World;
import net.minecraft.world.raid.Raid;

import javax.annotation.Nullable;
import java.util.Objects;
import java.util.Optional;

public final class RaiderRequirement implements EntitySubRequirement
{
    private final Optional<Boolean> hasRaid;
    private final Optional<Boolean> isCaptain;
    private final Optional<RaidData> raid;

    public RaiderRequirement(Optional<Boolean> hasRaid, Optional<Boolean> isCaptain, Optional<RaidData> raid)
    {
        this.hasRaid = hasRaid;
        this.isCaptain = isCaptain;
        this.raid = raid;
    }
    public static final MapCodec<RaiderRequirement> CODEC = RecordCodecBuilder.mapCodec(instance -> instance.group(
            Codec.BOOL.optionalFieldOf("has_raid").forGetter(RaiderRequirement::hasRaid),
            Codec.BOOL.optionalFieldOf("is_captain").forGetter(RaiderRequirement::isCaptain),
            RaidData.CODEC.optionalFieldOf("raid").forGetter(RaiderRequirement::raid)
    ).apply(instance, RaiderRequirement::new));

    public Optional<Boolean> hasRaid()
    {   return hasRaid;
    }
    public Optional<Boolean> isCaptain()
    {   return isCaptain;
    }
    public Optional<RaidData> raid()
    {   return raid;
    }

    @Override
    public MapCodec<? extends EntitySubRequirement> getCodec()
    {   return CODEC;
    }

    @Override
    public boolean test(Entity entity, World level, @Nullable Vector3d position)
    {
        return entity instanceof AbstractRaiderEntity
                && this.hasRaid.map(bool -> bool == ((AbstractRaiderEntity) entity).hasActiveRaid()).orElse(true)
                && this.isCaptain.map(bool -> bool == isCaptain(((AbstractRaiderEntity) entity))).orElse(true)
                && this.raid.map(raidData -> raidData.test(((AbstractRaiderEntity) entity).getCurrentRaid())).orElse(true);
    }

    private static boolean isCaptain(AbstractRaiderEntity raider)
    {
        ItemStack itemstack = raider.getItemBySlot(EquipmentSlotType.HEAD);
        boolean wearingBanner = !itemstack.isEmpty() && ItemStack.matches(itemstack, Raid.getLeaderBannerInstance());
        return wearingBanner && raider.isPatrolLeader();
    }

    public static final class RaidData
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
        private final Optional<Boolean> isOver;
        private final Optional<Boolean> isBetweenWaves;
        private final Optional<Boolean> isFirstWaveSpawned;
        private final Optional<Boolean> isVictory;
        private final Optional<Boolean> isLoss;
        private final Optional<Boolean> isStarted;
        private final Optional<Boolean> isStopped;
        private final Optional<DoubleBounds> totalHealth;
        private final Optional<DoubleBounds> badOmenLevel;
        private final Optional<IntegerBounds> currentWave;

        public RaidData(Optional<Boolean> isOver, Optional<Boolean> isBetweenWaves, Optional<Boolean> isFirstWaveSpawned,
                        Optional<Boolean> isVictory, Optional<Boolean> isLoss, Optional<Boolean> isStarted,
                        Optional<Boolean> isStopped, Optional<DoubleBounds> totalHealth, Optional<DoubleBounds> badOmenLevel,
                        Optional<IntegerBounds> currentWave)
        {
            this.isOver = isOver;
            this.isBetweenWaves = isBetweenWaves;
            this.isFirstWaveSpawned = isFirstWaveSpawned;
            this.isVictory = isVictory;
            this.isLoss = isLoss;
            this.isStarted = isStarted;
            this.isStopped = isStopped;
            this.totalHealth = totalHealth;
            this.badOmenLevel = badOmenLevel;
            this.currentWave = currentWave;
        }

        public boolean test(Raid raid)
        {
            return this.isOver.map(bool -> bool == raid.isOver()).orElse(true)
                    && this.isBetweenWaves.map(bool -> bool == raid.isBetweenWaves()).orElse(true)
                    && this.isFirstWaveSpawned.map(bool -> bool == raid.hasFirstWaveSpawned()).orElse(true)
                    && this.isVictory.map(bool -> bool == raid.isVictory()).orElse(true)
                    && this.isLoss.map(bool -> bool == raid.isLoss()).orElse(true)
                    && this.isStarted.map(bool -> bool == raid.isStarted()).orElse(true)
                    && this.isStopped.map(bool -> bool == raid.isStopped()).orElse(true)
                    && this.totalHealth.map(val -> val.test(raid.getHealthOfLivingRaiders())).orElse(true)
                    && this.badOmenLevel.map(val -> val.test(raid.getBadOmenLevel())).orElse(true)
                    && this.currentWave.map(val -> val.test(raid.getGroupsSpawned())).orElse(true);
        }

        public Optional<Boolean> isOver() {return isOver;}
        public Optional<Boolean> isBetweenWaves() {return isBetweenWaves;}
        public Optional<Boolean> isFirstWaveSpawned() {return isFirstWaveSpawned;}
        public Optional<Boolean> isVictory() {return isVictory;}
        public Optional<Boolean> isLoss() {return isLoss;}
        public Optional<Boolean> isStarted() {return isStarted;}
        public Optional<Boolean> isStopped() {return isStopped;}
        public Optional<DoubleBounds> totalHealth() {return totalHealth;}
        public Optional<DoubleBounds> badOmenLevel() {return badOmenLevel;}
        public Optional<IntegerBounds> currentWave() {return currentWave;}
    }
}
