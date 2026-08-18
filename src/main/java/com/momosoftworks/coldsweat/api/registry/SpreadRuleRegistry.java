package com.momosoftworks.coldsweat.api.registry;

import com.momosoftworks.coldsweat.api.spread_rule.DefaultSpreadRule;
import com.momosoftworks.coldsweat.api.spread_rule.SmokestackSpreadRule;
import com.momosoftworks.coldsweat.api.spread_rule.SpreadRule;
import com.momosoftworks.coldsweat.api.spread_rule.compat.CreatePipeSpreadRule;
import com.momosoftworks.coldsweat.compat.CompatManager;
import net.minecraft.block.BlockState;

import java.util.ArrayList;
import java.util.List;

public class SpreadRuleRegistry
{
    private static final List<SpreadRule> RULES = new ArrayList<>();
    // Used only if get() is somehow called before registerDefaults() has run
    private static final SpreadRule FAILSAFE_DEFAULT = new DefaultSpreadRule();

    public static synchronized void register(SpreadRule rule)
    {   RULES.add(rule);
    }

    public static synchronized void registerFirst(SpreadRule rule)
    {   RULES.add(0, rule);
    }

    public static SpreadRule get(BlockState state)
    {
        for (SpreadRule rule : RULES)
        {   if (rule.matches(state)) return rule;
        }
        return FAILSAFE_DEFAULT;
    }

    public static synchronized void registerDefaults()
    {
        RULES.clear();
        register(new SmokestackSpreadRule());
        if (CompatManager.isCreateLoaded())
        {   register(new CreatePipeSpreadRule());
        }
        // Must be registered last: matches() is unconditional
        register(new DefaultSpreadRule());
    }
}
