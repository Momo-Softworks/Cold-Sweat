package com.momosoftworks.coldsweat.data.loot;

import com.momosoftworks.coldsweat.util.math.CSMath;
import net.minecraft.entity.Entity;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.item.ItemStack;
import net.minecraft.loot.LootContext;
import net.minecraft.loot.LootParameterSets;
import net.minecraft.loot.LootParameters;
import net.minecraft.util.DamageSource;
import net.minecraft.util.ResourceLocation;
import net.minecraft.world.server.ServerWorld;

import javax.annotation.Nullable;
import java.util.List;

public class ModLootTables
{
    public static final ResourceLocation GOAT_SHEARING = new ResourceLocation("cold_sweat", "entities/goat_shearing");
    public static final ResourceLocation CHAMELEON_SHEDDING = new ResourceLocation("cold_sweat", "entities/chameleon_shedding");

    public static List<ItemStack> getDropsLootTable(Entity entity, @Nullable PlayerEntity player, ResourceLocation lootTable)
    {   LootContext lootContext = new LootContext.Builder(((ServerWorld) entity.level))
            .withParameter(LootParameters.THIS_ENTITY, entity)
            .withParameter(LootParameters.DAMAGE_SOURCE, DamageSource.GENERIC)
            .withParameter(LootParameters.ORIGIN, entity.position())
            .withLuck(CSMath.getIfNotNull(player, PlayerEntity::getLuck, 0f))
            .withRandom(entity.level.random)
            .create(LootParameterSets.ENTITY);

        return entity.getServer().getLootTables().get(lootTable).getRandomItems(lootContext);
    }
}
