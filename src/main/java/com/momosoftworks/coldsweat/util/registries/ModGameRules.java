package com.momosoftworks.coldsweat.util.registries;

import com.mojang.brigadier.arguments.BoolArgumentType;
import com.mojang.serialization.Codec;
import com.momosoftworks.coldsweat.ColdSweat;
import net.minecraft.core.registries.Registries;
import net.minecraft.world.flag.FeatureFlagSet;
import net.minecraft.world.level.gamerules.GameRule;
import net.minecraft.world.level.gamerules.GameRuleCategory;
import net.minecraft.world.level.gamerules.GameRuleType;
import net.minecraft.world.level.gamerules.GameRuleTypeVisitor;
import net.neoforged.neoforge.registries.DeferredHolder;
import net.neoforged.neoforge.registries.DeferredRegister;

public class ModGameRules
{
    public static final DeferredRegister<GameRule<?>> GAME_RULES = DeferredRegister.create(Registries.GAME_RULE, ColdSweat.MOD_ID);

    public static final DeferredHolder<GameRule<?>, GameRule<Boolean>> RULE_SLUSH_SOURCE_CONVERSION = GAME_RULES.register("slush_source_conversion",
            () -> new GameRule<>(GameRuleCategory.PLAYER, GameRuleType.BOOL, BoolArgumentType.bool(), GameRuleTypeVisitor::visitBoolean,
                                 Codec.BOOL, b -> b ? 1 : 0, false, FeatureFlagSet.of()));
}
