package com.momosoftworks.coldsweat.data.loot;

import com.momosoftworks.coldsweat.ColdSweat;
import com.momosoftworks.coldsweat.util.math.CSMath;
import net.minecraft.core.BlockPos;
import net.minecraft.core.registries.Registries;
import net.minecraft.resources.ResourceKey;
import net.minecraft.resources.Identifier;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.phys.Vec3;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.storage.loot.LootParams;
import net.minecraft.world.level.storage.loot.parameters.LootContextParamSets;
import net.minecraft.world.level.storage.loot.parameters.LootContextParams;
import org.jetbrains.annotations.Nullable;

import java.util.List;

public class ModLootTables
{
    public static final Identifier GOAT_SHEARING = Identifier.fromNamespaceAndPath(ColdSweat.MOD_ID, "entities/goat_shearing");
    public static final Identifier CHAMELEON_SHEDDING = Identifier.fromNamespaceAndPath(ColdSweat.MOD_ID, "entities/chameleon_shedding");

    public static final Identifier CUSTOM_ICE_DROP = Identifier.fromNamespaceAndPath("cold_sweat", "blocks/special/ice");
    public static final Identifier CUSTOM_PACKED_ICE_DROP = Identifier.fromNamespaceAndPath("cold_sweat", "blocks/special/packed_ice");
    public static final Identifier CUSTOM_BLUE_ICE_DROP = Identifier.fromNamespaceAndPath("cold_sweat", "blocks/special/blue_ice");

    public static List<ItemStack> getEntityDropsLootTable(Entity entity, @Nullable Player player, Identifier lootTable)
    {
        if (entity.level().isClientSide()) return List.of();

        LootParams lootContext = new LootParams.Builder(((ServerLevel) entity.level()))
            .withParameter(LootContextParams.THIS_ENTITY, entity)
            .withParameter(LootContextParams.ORIGIN, entity.position())
            .withParameter(LootContextParams.DAMAGE_SOURCE, player != null ? entity.damageSources().playerAttack(player) : entity.damageSources().generic())
            .withLuck(CSMath.getIfNotNull(player, Player::getLuck, 0f))
            .create(LootContextParamSets.ENTITY);

        return entity.level().getServer().reloadableRegistries().getLootTable(ResourceKey.create(Registries.LOOT_TABLE, lootTable)).getRandomItems(lootContext);
    }

    public static List<ItemStack> getBlockDropsLootTable(ServerLevel level, BlockPos pos, BlockState state, @Nullable Player player, Identifier lootTable)
    {
        LootParams lootContext = new LootParams.Builder(level)
            .withParameter(LootContextParams.BLOCK_STATE, state)
            .withParameter(LootContextParams.TOOL, CSMath.getIfNotNull(player, Player::getMainHandItem, ItemStack.EMPTY))
            .withParameter(LootContextParams.ORIGIN, Vec3.atCenterOf(pos))
            .withOptionalParameter(LootContextParams.THIS_ENTITY, player)
            .withLuck(CSMath.getIfNotNull(player, Player::getLuck, 0f))
            .create(LootContextParamSets.BLOCK);

        return level.getServer().reloadableRegistries().getLootTable(ResourceKey.create(Registries.LOOT_TABLE, lootTable)).getRandomItems(lootContext);
    }
}
