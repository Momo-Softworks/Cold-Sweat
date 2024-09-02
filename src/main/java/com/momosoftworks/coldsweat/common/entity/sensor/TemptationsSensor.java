package com.momosoftworks.coldsweat.common.entity.sensor;

import com.google.common.collect.ImmutableSet;
import com.momosoftworks.coldsweat.core.init.MemoryInit;
import net.minecraft.entity.CreatureEntity;
import net.minecraft.entity.EntityPredicate;
import net.minecraft.entity.ai.brain.Brain;
import net.minecraft.entity.ai.brain.memory.MemoryModuleType;
import net.minecraft.entity.ai.brain.sensor.Sensor;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.entity.player.ServerPlayerEntity;
import net.minecraft.item.ItemStack;
import net.minecraft.item.crafting.Ingredient;
import net.minecraft.util.EntityPredicates;
import net.minecraft.world.server.ServerWorld;

import java.util.Comparator;
import java.util.List;
import java.util.Set;
import java.util.stream.Collectors;
import java.util.stream.Stream;

public class TemptationsSensor extends Sensor<CreatureEntity>
{
    private static final EntityPredicate TEMPTER_PREDICATE = (new EntityPredicate()).range(10.0).ignoreInvisibilityTesting();
    private final Ingredient ingredient;

    public TemptationsSensor(Ingredient ingredient)
    {   this.ingredient = ingredient;
    }

    protected void doTick(ServerWorld worldIn, CreatureEntity entityIn)
    {
        Brain<?> brain = entityIn.getBrain();
        Stream<ServerPlayerEntity> targettablePlayers = worldIn.players().stream()
                                                        .filter(EntityPredicates.NO_SPECTATORS)
                                                        .filter((player) -> TEMPTER_PREDICATE.test(entityIn, player))
                                                        .filter((player) -> entityIn.closerThan(player, 10.0)).filter(this::test);

        List<PlayerEntity> playersByDist = targettablePlayers.sorted(Comparator.comparing(entityIn::distanceToSqr)).collect(Collectors.toList());

        if (!playersByDist.isEmpty())
        {   PlayerEntity player = playersByDist.get(0);
            brain.setMemory(MemoryInit.TEMPTING_PLAYER.get(), player);
        }
        else
        {   brain.eraseMemory(MemoryInit.TEMPTING_PLAYER.get());
        }
    }

    private boolean test(PlayerEntity player)
    {   return this.test(player.getMainHandItem()) || this.test(player.getOffhandItem());
    }

    private boolean test(ItemStack stack)
    {   return this.ingredient.test(stack);
    }

    public Set<MemoryModuleType<?>> requires()
    {   return ImmutableSet.of(MemoryInit.TEMPTING_PLAYER.get());
    }
}
