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
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.biome.Biome;

public class ColdBiomeEdible extends Edible
{
    @Override
    public int getCooldown()
    {
        return (int) (Math.random() * 400 + 1200);
    }

    @Override
    public void onEaten(ChameleonEntity entity, ItemEntity item)
    {
        if (!entity.level.isClientSide)
        {
            // Flag for searching
            entity.setSearching(true);

            TaskScheduler.scheduleServer(() ->
            {
                Pair<BlockPos, Holder<Biome>> biomePair = ((ServerLevel) entity.level).findNearestBiome(holder ->
                {
                    Biome biome = holder.value();
                    ResourceLocation biomeName = biome.getRegistryName();

                    double biomeTemp = CSMath.averagePair(ConfigSettings.BIOME_TEMPS.get().getOrDefault(biomeName,
                            ConfigSettings.BIOME_OFFSETS.get().getOrDefault(biomeName, CSMath.addPairs(Pair.of(0d, 0d),
                                    Pair.of(biome.getBaseTemperature(), biome.getBaseTemperature())))));

                    return biomeTemp <= 0.2;
                }, entity.blockPosition(), 2000, 8);
                if (biomePair != null)
                {
                    // Set the chameleon to track this position
                    entity.setTrackingPos(biomePair.getFirst());

                    WorldHelper.playEntitySound(ModSounds.CHAMELEON_FIND, entity, entity.getSoundSource(), 1.2f, entity.getVoicePitch());
                    WorldHelper.spawnParticleBatch(entity.level, ParticleTypes.HAPPY_VILLAGER, entity.getX(), entity.getY() + entity.getBbHeight() / 2, entity.getZ(), 1, 1, 1, 6, 0.01);

                    super.onEaten(entity, item);
                }
                else
                {
                    WorldHelper.spawnParticleBatch(entity.level, ParticleTypes.SMOKE, entity.getX(), entity.getY() + entity.getBbHeight() / 2, entity.getZ(), 1, 1, 1, 6, 0.01);
                }
                // Stop searching
                entity.setSearching(false);
            }, (int) (Math.random() * 20 + 40));
        }
    }
}
