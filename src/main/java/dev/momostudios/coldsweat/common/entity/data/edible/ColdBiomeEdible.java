package dev.momostudios.coldsweat.common.entity.data.edible;

import com.mojang.datafixers.util.Pair;
import dev.momostudios.coldsweat.api.util.Temperature;
import dev.momostudios.coldsweat.common.entity.Chameleon;
import dev.momostudios.coldsweat.core.event.TaskScheduler;
import dev.momostudios.coldsweat.config.ConfigSettings;
import dev.momostudios.coldsweat.util.math.CSMath;
import dev.momostudios.coldsweat.util.registries.ModSounds;
import dev.momostudios.coldsweat.util.registries.ModTags;
import dev.momostudios.coldsweat.util.world.WorldHelper;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Holder;
import net.minecraft.core.particles.ParticleTypes;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.tags.TagKey;
import net.minecraft.world.entity.item.ItemEntity;
import net.minecraft.world.item.Item;
import net.minecraft.world.level.biome.Biome;
import oshi.util.tuples.Triplet;

public class ColdBiomeEdible extends Edible
{
    @Override
    public int getCooldown()
    {
        return (int) (Math.random() * 400 + 1200);
    }

    @Override
    public Result onEaten(Chameleon entity, ItemEntity item)
    {
        if (!entity.level.isClientSide)
        {
            // Flag for searching
            entity.setSearching(true);

            Pair<BlockPos, Holder<Biome>> biomePair = ((ServerLevel) entity.level).findNearestBiome(holder ->
            {
                Biome biome = holder.value();
                ResourceLocation biomeName = biome.getRegistryName();

                Triplet<Double, Double, Temperature.Units> tempConfig = ConfigSettings.BIOME_TEMPS.get().getOrDefault(biomeName,
                                                                                                                      ConfigSettings.BIOME_OFFSETS.get().getOrDefault(biomeName,
                                                                        new Triplet<>((double) biome.getBaseTemperature(), (double) biome.getBaseTemperature(), Temperature.Units.MC)));
                Pair<Double, Double> minMax = Pair.of(tempConfig.getA(), tempConfig.getB());
                double biomeTemp = CSMath.averagePair(minMax);

                return biomeTemp <= 0.2;
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
    public boolean shouldEat(Chameleon entity, ItemEntity item)
    {   return true;
    }

    @Override
    public TagKey<Item> associatedItems()
    {   return ModTags.Items.CHAMELEON_COLD;
    }
}
