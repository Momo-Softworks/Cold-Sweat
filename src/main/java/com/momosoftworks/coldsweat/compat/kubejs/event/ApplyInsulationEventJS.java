package com.momosoftworks.coldsweat.compat.kubejs.event;

import com.momosoftworks.coldsweat.api.event.common.insulation.InsulateItemEvent;
import dev.latvian.mods.kubejs.bindings.UtilsWrapper;
import dev.latvian.mods.kubejs.core.PlayerSelector;
import dev.latvian.mods.kubejs.player.PlayerEventJS;
import dev.latvian.mods.kubejs.player.PlayerJS;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.item.ItemStack;

public class ApplyInsulationEventJS extends PlayerEventJS
{
    private final InsulateItemEvent event;

    public ApplyInsulationEventJS(InsulateItemEvent event)
    {
        this.event = event;
    }

    @Override
    public PlayerJS<ServerPlayer> getEntity()
    {   return UtilsWrapper.getServer().getPlayer(PlayerSelector.mc(event.getPlayer()));
    }

    public ItemStack getArmorItem()
    {   return event.getArmorItem();
    }

    public ItemStack getInsulator()
    {   return event.getInsulator();
    }

    public void setInsulator(ItemStack insulator)
    {   event.setInsulator(insulator);
    }
}
