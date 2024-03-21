package com.momosoftworks.coldsweat.data.loot;

import com.momosoftworks.coldsweat.util.math.CSMath;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.storage.loot.LootContext;
import net.minecraft.world.level.storage.loot.LootParams;
import net.minecraft.world.level.storage.loot.parameters.LootContextParamSets;
import net.minecraft.world.level.storage.loot.parameters.LootContextParams;
import org.jetbrains.annotations.Nullable;

import java.util.List;

public class ModLootTables
{
    public static final ResourceLocation GOAT_SHEARING = new ResourceLocation("cold_sweat", "entities/goat_shearing");
    public static final ResourceLocation CHAMELEON_SHEDDING = new ResourceLocation("cold_sweat", "entities/chameleon_shedding");

    public static List<ItemStack> getDropsLootTable(Entity entity, @Nullable Player player, ResourceLocation lootTable)
    {   LootParams lootContext = new LootParams.Builder(((ServerLevel) entity.level()))
            .withParameter(LootContextParams.THIS_ENTITY, entity)
            .withLuck(CSMath.getIfNotNull(player, Player::getLuck, 0f))
            .create(LootContextParamSets.ENTITY);

        return entity.getServer().getLootData().getLootTable(lootTable).getRandomItems(lootContext);
    }
}
