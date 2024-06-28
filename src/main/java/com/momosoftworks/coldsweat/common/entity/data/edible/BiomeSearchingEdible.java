package com.momosoftworks.coldsweat.common.entity.data.edible;

import com.mojang.datafixers.util.Pair;
import com.momosoftworks.coldsweat.common.entity.ChameleonEntity;
import com.momosoftworks.coldsweat.core.event.TaskScheduler;
import com.momosoftworks.coldsweat.util.entity.EntityHelper;
import com.momosoftworks.coldsweat.util.registries.ModSounds;
import com.momosoftworks.coldsweat.util.world.WorldHelper;
import net.minecraft.entity.item.ItemEntity;
import net.minecraft.particles.ParticleTypes;
import net.minecraft.util.math.BlockPos;
import net.minecraft.world.World;
import net.minecraft.world.biome.Biome;
import net.minecraft.world.server.ServerWorld;

import java.util.function.BiPredicate;

public abstract class BiomeSearchingEdible extends Edible
{
    private final BiPredicate<World, Biome> biomePredicate;

    public BiomeSearchingEdible(BiPredicate<World, Biome> biomePredicate)
    {   this.biomePredicate = biomePredicate;
    }

    @Override
    public Result onEaten(ChameleonEntity entity, ItemEntity item)
    {
        if (!entity.level.isClientSide)
        {
            // Flag for searching
            entity.setSearching(true);

            // Create a new thread to look for the biome
            Thread searchThread = new Thread(null, () ->
            {
                BlockPos pos = entity.blockPosition();
                BlockPos biomePos = ((ServerWorld) entity.level).getChunkSource().getGenerator().getBiomeSource().findBiomeHorizontal(pos.getX(), pos.getY(), pos.getZ(), 2000,
                                                                                                                                      biome -> this.biomePredicate.test(entity.level, biome), entity.getRandom());
                if (biomePos != null)
                {
                    Pair<BlockPos, Biome> biomePair = Pair.of(biomePos, entity.level.getBiome(biomePos));
                    TaskScheduler.scheduleServer(() ->
                    {
                        // Set the chameleon to track this position
                        entity.setTrackingPos(biomePair.getFirst());

                        WorldHelper.playEntitySound(ModSounds.CHAMELEON_FIND, entity, entity.getSoundSource(), 1.2f, EntityHelper.getVoicePitch(entity));
                        WorldHelper.spawnParticleBatch(entity.level, ParticleTypes.HAPPY_VILLAGER, entity.getX(), entity.getY() + entity.getBbHeight() / 2, entity.getZ(), 1, 1, 1, 6, 0.01);

                        // Stop searching
                        entity.setSearching(false);
                    }, (int) (Math.random() * 20 + 40));
                }
                else
                {
                    TaskScheduler.scheduleServer(() ->
                    {
                        WorldHelper.spawnParticleBatch(entity.level, ParticleTypes.SMOKE, entity.getX(), entity.getY() + entity.getBbHeight() / 2, entity.getZ(), 1, 1, 1, 6, 0.01);

                        // Stop searching
                        entity.setSearching(false);
                    }, (int) (Math.random() * 20 + 40));
                }
            }, "(Cold Sweat) Chameleon search thread");
            // Execute the search thread
            searchThread.start();
        }
        return Result.FAIL;
    }

    @Override
    public boolean shouldEat(ChameleonEntity entity, ItemEntity item)
    {   return item.getOwner() != null && entity.isPlayerTrusted(item.getOwner());
    }
}
