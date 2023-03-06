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
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.biome.Biome;

public class HumidBiomeEdible extends Edible
{
    @Override
    public int getCooldown()
    {
        return (int) (Math.random() * 400 + 1200);
    }

    @Override
    public void onEaten(ChameleonEntity entity, ItemStack stack)
    {
        if (!entity.level.isClientSide)
        {
            // Flag for searching
            entity.setSearching(true);

            TaskScheduler.scheduleServer(() ->
            {
                // Locate the nearest biome with temp > 0.8 and humid
                Pair<BlockPos, Holder<Biome>> biomePair = ((ServerLevel) entity.level).findNearestBiome(holder -> holder.value().isHumid(), entity.blockPosition(), 2000, 8);

                if (biomePair != null)
                {
                    // Set the chameleon to track this position
                    entity.setTrackingPos(biomePair.getFirst());

                    WorldHelper.playEntitySound(ModSounds.CHAMELEON_FIND, entity, entity.getSoundSource(), 1.2f, entity.getVoicePitch());
                    WorldHelper.spawnParticleBatch(entity.level, ParticleTypes.HAPPY_VILLAGER, entity.getX(), entity.getY() + entity.getBbHeight() / 2, entity.getZ(), 1, 1, 1, 6, 0.01);

                    super.onEaten(entity, stack);
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
