package com.momosoftworks.coldsweat.data.loot;

import com.momosoftworks.coldsweat.util.math.CSMath;
import net.minecraft.core.BlockPos;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.world.damagesource.DamageSource;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.storage.loot.LootContext;
import net.minecraft.world.level.storage.loot.parameters.LootContextParamSets;
import net.minecraft.world.level.storage.loot.parameters.LootContextParams;
import net.minecraft.world.phys.Vec3;
import org.jetbrains.annotations.Nullable;

import java.util.List;

public class ModLootTables
{
    public static final ResourceLocation GOAT_SHEARING = new ResourceLocation("cold_sweat", "entities/goat_shearing");
    public static final ResourceLocation CHAMELEON_SHEDDING = new ResourceLocation("cold_sweat", "entities/chameleon_shedding");

    public static final ResourceLocation CUSTOM_ICE_DROP = new ResourceLocation("cold_sweat", "blocks/special/ice");
    public static final ResourceLocation CUSTOM_PACKED_ICE_DROP = new ResourceLocation("cold_sweat", "blocks/special/packed_ice");
    public static final ResourceLocation CUSTOM_BLUE_ICE_DROP = new ResourceLocation("cold_sweat", "blocks/special/blue_ice");

    public static List<ItemStack> getEntityDropsLootTable(Entity entity, @Nullable Player player, ResourceLocation lootTable)
    {
        LootContext lootContext = new LootContext.Builder(((ServerLevel) entity.level))
            .withParameter(LootContextParams.THIS_ENTITY, entity)
            .withParameter(LootContextParams.DAMAGE_SOURCE, DamageSource.GENERIC)
            .withParameter(LootContextParams.ORIGIN, entity.position())
            .withParameter(LootContextParams.DAMAGE_SOURCE, player != null ? DamageSource.playerAttack(player) : DamageSource.GENERIC)
            .withLuck(CSMath.getIfNotNull(player, Player::getLuck, 0f))
            .withRandom(entity.level.random)
            .create(LootContextParamSets.ENTITY);

        return entity.getServer().getLootTables().get(lootTable).getRandomItems(lootContext);
    }

    public static List<ItemStack> getBlockDropsLootTable(ServerLevel level, BlockPos pos, BlockState state, @Nullable Player player, ResourceLocation lootTable)
    {
        LootContext lootContext = new LootContext.Builder(level)
            .withParameter(LootContextParams.BLOCK_STATE, state)
            .withParameter(LootContextParams.TOOL, CSMath.getIfNotNull(player, Player::getMainHandItem, ItemStack.EMPTY))
            .withParameter(LootContextParams.ORIGIN, new Vec3(pos.getX() + 0.5, pos.getY() + 0.5, pos.getZ() + 0.5))
            .withOptionalParameter(LootContextParams.THIS_ENTITY, player)
            .withLuck(CSMath.getIfNotNull(player, Player::getLuck, 0f))
            .create(LootContextParamSets.BLOCK);

        return level.getServer().getLootTables().get(lootTable).getRandomItems(lootContext);
    }
}
