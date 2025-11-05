package com.momosoftworks.coldsweat.util.registries;

import net.minecraft.world.level.GameRules;

public class ModGameRules
{
    public static GameRules.Key<GameRules.BooleanValue> RULE_SLUSH_SOURCE_CONVERSION;

    public static void registerGameRules()
    {   RULE_SLUSH_SOURCE_CONVERSION = GameRules.register("slushSourceConversion", GameRules.Category.PLAYER, GameRules.BooleanValue.create(false));
    }
}
