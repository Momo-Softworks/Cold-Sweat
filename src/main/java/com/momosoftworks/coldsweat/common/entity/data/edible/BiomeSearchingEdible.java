package com.momosoftworks.coldsweat.common.entity.data.edible;

import com.mojang.datafixers.util.Pair;
import com.momosoftworks.coldsweat.common.entity.Chameleon;
import com.momosoftworks.coldsweat.core.event.TaskScheduler;
import com.momosoftworks.coldsweat.util.registries.ModSounds;
import com.momosoftworks.coldsweat.util.world.WorldHelper;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Holder;
import net.minecraft.core.particles.ParticleTypes;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.world.entity.item.ItemEntity;
import net.minecraft.world.level.biome.Biome;

import java.util.function.Predicate;

public abstract class BiomeSearchingEdible extends Edible
{
    private final Predicate<Holder<Biome>> biomePredicate;

    public BiomeSearchingEdible(Predicate<Holder<Biome>> biomePredicate)
    {   this.biomePredicate = biomePredicate;
    }

    @Override
    public Result onEaten(Chameleon entity, ItemEntity item)
    {
        if (!entity.level.isClientSide)
        {
            // Flag for searching
            entity.setSearching(true);

            // Create a new thread to look for the biome
            Thread searchThread = new Thread(null, () ->
            {
                // Search for a cold biome
                Pair<BlockPos, Holder<Biome>> biomePair = ((ServerLevel) entity.level).findClosestBiome3d(this.biomePredicate, entity.blockPosition(), 2000, 32, 64);
                System.out.println("Biome pair: " + biomePair);

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
    public boolean shouldEat(Chameleon entity, ItemEntity item)
    {   return item.getOwner() != null && entity.isPlayerTrusted(item.getOwner());
    }
}
