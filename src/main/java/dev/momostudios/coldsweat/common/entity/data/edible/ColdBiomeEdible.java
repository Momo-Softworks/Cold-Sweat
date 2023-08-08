package dev.momostudios.coldsweat.common.entity.data.edible;

import com.mojang.datafixers.util.Pair;
import dev.momostudios.coldsweat.api.util.Temperature;
import dev.momostudios.coldsweat.common.entity.ChameleonEntity;
import dev.momostudios.coldsweat.core.event.TaskScheduler;
import dev.momostudios.coldsweat.config.ConfigSettings;
import dev.momostudios.coldsweat.data.tags.ModItemTags;
import dev.momostudios.coldsweat.util.entity.EntityHelper;
import dev.momostudios.coldsweat.util.math.CSMath;
import dev.momostudios.coldsweat.util.serialization.Triplet;
import dev.momostudios.coldsweat.util.registries.ModSounds;
import dev.momostudios.coldsweat.util.world.WorldHelper;
import net.minecraft.entity.item.ItemEntity;
import net.minecraft.item.Item;
import net.minecraft.particles.ParticleTypes;
import net.minecraft.tags.ITag;
import net.minecraft.util.ResourceLocation;
import net.minecraft.util.math.BlockPos;
import net.minecraft.world.server.ServerWorld;

public class ColdBiomeEdible extends Edible
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

            BlockPos pos = entity.blockPosition();
            BlockPos biomePos = ((ServerWorld) entity.level).getChunkSource().getGenerator().getBiomeSource().findBiomeHorizontal(pos.getX(), pos.getY(), pos.getZ(), 2000,
            biome ->
            {
                ResourceLocation biomeName = biome.getRegistryName();

                Triplet<Double, Double, Temperature.Units> tempConfig = ConfigSettings.BIOME_TEMPS.get().getOrDefault(biomeName,
                                                                        ConfigSettings.BIOME_OFFSETS.get().getOrDefault(biomeName,
                                                                        new Triplet<>((double) biome.getBaseTemperature(), (double) biome.getBaseTemperature(), Temperature.Units.MC)));
                Pair<Double, Double> minMax = Pair.of(tempConfig.getFirst(), tempConfig.getSecond());
                double biomeTemp = CSMath.averagePair(minMax);

                return biomeTemp <= 0.2;
            }, entity.getRandom());

            if (biomePos != null)
            {
                TaskScheduler.scheduleServer(() ->
                {
                    // Set the chameleon to track this position
                    entity.setTrackingPos(biomePos);

                    WorldHelper.playEntitySound(ModSounds.CHAMELEON_FIND, entity, entity.getSoundSource(), 1.2f, EntityHelper.getVoicePitch(entity));
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
    {   return true;
    }

    @Override
    public ITag.INamedTag<Item> associatedItems()
    {   return ModItemTags.CHAMELEON_COLD;
    }
}
