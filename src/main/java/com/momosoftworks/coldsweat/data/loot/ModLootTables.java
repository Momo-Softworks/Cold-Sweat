package com.momosoftworks.coldsweat.data.loot;

import com.jozufozu.flywheel.util.vec.Vec3;
import com.momosoftworks.coldsweat.util.math.CSMath;
import net.minecraft.block.BlockState;
import net.minecraft.entity.Entity;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.item.ItemStack;
import net.minecraft.loot.LootContext;
import net.minecraft.loot.LootParameterSets;
import net.minecraft.loot.LootParameters;
import net.minecraft.util.DamageSource;
import net.minecraft.util.ResourceLocation;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.vector.Vector3d;
import net.minecraft.world.server.ServerWorld;

import javax.annotation.Nullable;
import java.util.List;

public class ModLootTables
{
    public static final ResourceLocation GOAT_SHEARING = new ResourceLocation("cold_sweat", "entities/goat_shearing");
    public static final ResourceLocation CHAMELEON_SHEDDING = new ResourceLocation("cold_sweat", "entities/chameleon_shedding");

    public static final ResourceLocation CUSTOM_ICE_DROP = new ResourceLocation("cold_sweat", "blocks/special/ice");
    public static final ResourceLocation CUSTOM_PACKED_ICE_DROP = new ResourceLocation("cold_sweat", "blocks/special/packed_ice");
    public static final ResourceLocation CUSTOM_BLUE_ICE_DROP = new ResourceLocation("cold_sweat", "blocks/special/blue_ice");

    public static List<ItemStack> getEntityDropsLootTable(Entity entity, @Nullable PlayerEntity player, ResourceLocation lootTable)
    {
        LootContext lootContext = new LootContext.Builder(((ServerWorld) entity.level))
            .withParameter(LootParameters.THIS_ENTITY, entity)
            .withParameter(LootParameters.DAMAGE_SOURCE, DamageSource.GENERIC)
            .withParameter(LootParameters.ORIGIN, entity.position())
            .withParameter(LootParameters.DAMAGE_SOURCE, player != null ? DamageSource.playerAttack(player) : DamageSource.GENERIC)
            .withLuck(CSMath.getIfNotNull(player, PlayerEntity::getLuck, 0f))
            .withRandom(entity.level.random)
            .create(LootParameterSets.ENTITY);

        return entity.getServer().getLootTables().get(lootTable).getRandomItems(lootContext);
    }

    public static List<ItemStack> getBlockDropsLootTable(ServerWorld level, BlockPos pos, BlockState state, @Nullable PlayerEntity player, ResourceLocation lootTable)
    {
        LootContext lootContext = new LootContext.Builder(level)
            .withParameter(LootParameters.BLOCK_STATE, state)
            .withParameter(LootParameters.TOOL, CSMath.getIfNotNull(player, PlayerEntity::getMainHandItem, ItemStack.EMPTY))
            .withParameter(LootParameters.ORIGIN, new Vector3d(pos.getX() + 0.5, pos.getY() + 0.5, pos.getZ() + 0.5))
            .withOptionalParameter(LootParameters.THIS_ENTITY, player)
            .withLuck(CSMath.getIfNotNull(player, PlayerEntity::getLuck, 0f))
            .create(LootParameterSets.BLOCK);

        return level.getServer().getLootTables().get(lootTable).getRandomItems(lootContext);
    }
}
