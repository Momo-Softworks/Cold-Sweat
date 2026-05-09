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
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.item.ItemEntity;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.biome.Biome;

import java.util.function.BiPredicate;

public abstract class BiomeSearchingEdible extends Edible
{
    private final BiPredicate<Level, Holder<Biome>> biomePredicate;

    public BiomeSearchingEdible(BiPredicate<Level, Holder<Biome>> biomePredicate)
    {   this.biomePredicate = biomePredicate;
    }

    @Override
    public Result onEaten(ItemStack item, Chameleon entity, Entity thrower)
    {
        if (!entity.level.isClientSide)
        {
            // Flag for searching
            entity.setSearching(true);

            // Create a new thread to look for the biome
            Thread searchThread = new Thread(null, () ->
            {
                // Search for a cold biome
                BlockPos entityPos = WorldHelper.sublevelToWorld(entity.level, entity.blockPosition());
                Pair<BlockPos, Holder<Biome>> biomePair = ((ServerLevel) entity.level).findClosestBiome3d(biome -> this.biomePredicate.test(entity.level, biome), entityPos, 2000, 32, 64);

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
        return Result.SUCCESS;
    }

    @Override
    public boolean shouldEat(ItemStack item, Chameleon entity, Entity thrower)
    {   return thrower != null && entity.isPlayerTrusted(thrower.getUUID());
    }
}
