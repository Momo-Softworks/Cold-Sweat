package dev.momostudios.coldsweat.common.entity.data.edible;

import com.mojang.datafixers.util.Pair;
import dev.momostudios.coldsweat.common.entity.ChameleonEntity;
import dev.momostudios.coldsweat.core.event.TaskScheduler;
import dev.momostudios.coldsweat.util.config.ConfigSettings;
import dev.momostudios.coldsweat.util.math.CSMath;
import dev.momostudios.coldsweat.util.registries.ModSounds;
import dev.momostudios.coldsweat.util.world.WorldHelper;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Holder;
import net.minecraft.core.particles.ParticleTypes;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.world.entity.item.ItemEntity;
import net.minecraft.world.level.biome.Biome;

public class HotBiomeEdible extends Edible
{
    @Override
    public int getCooldown()
    {
        return (int) (Math.random() * 400 + 1200);
    }

    @Override
    public Result onEaten(ChameleonEntity entity, ItemEntity item)
    {
        if (!entity.level.isClientSide)
        {
            // Flag for searching
            entity.setSearching(true);

            // Locate the nearest biome with temp > 1.5
            Pair<BlockPos, Holder<Biome>> biomePair = ((ServerLevel) entity.level).findNearestBiome(holder ->
            {
                Biome biome = holder.value();
                ResourceLocation biomeName = biome.getRegistryName();

                double biomeTemp = CSMath.averagePair(ConfigSettings.BIOME_TEMPS.get().getOrDefault(biomeName,
                          ConfigSettings.BIOME_OFFSETS.get().getOrDefault(biomeName, CSMath.addPairs(Pair.of(0d, 0d),
                          Pair.of(biome.getBaseTemperature(), biome.getBaseTemperature())))));

                return biomeTemp >= 1.5;
            }, entity.blockPosition(), 2000, 8);

            if (biomePair != null)
            {
                TaskScheduler.scheduleServer(() ->
                {
                    // Set the chameleon to track this position
                    entity.setTrackingPos(biomePair.getFirst());

                    WorldHelper.playEntitySound(ModSounds.CHAMELEON_FIND, entity, entity.getSoundSource(), 1.2f, entity.getVoicePitch());
                    WorldHelper.spawnParticleBatch(entity.level, ParticleTypes.HAPPY_VILLAGER, entity.getX(), entity.getY() + entity.getBbHeight() / 2, entity.getZ(), 1, 1, 1, 6, 0.01);

                    // Stop searching
                    entity.setSearching(false);
                }, (int) (Math.random() * 20 + 40));

                return Result.SUCCESS;
            }
            else
            {
                TaskScheduler.scheduleServer(() ->
                {
                    WorldHelper.spawnParticleBatch(entity.level, ParticleTypes.SMOKE, entity.getX(), entity.getY() + entity.getBbHeight() / 2, entity.getZ(), 1, 1, 1, 6, 0.01);

                    // Stop searching
                    entity.setSearching(false);
                }, (int) (Math.random() * 20 + 40));

                return Result.FAIL;
            }
        }
        return Result.FAIL;
    }

    @Override
    public boolean shouldEat(ChameleonEntity entity, ItemEntity item)
    {
        return true;
    }
}
