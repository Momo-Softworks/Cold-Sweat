package com.momosoftworks.coldsweat.api.event.common.waterskin;

import net.minecraft.core.BlockPos;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.Level;
import net.neoforged.bus.api.Event;
import net.neoforged.neoforge.fluids.FluidStack;
import org.jetbrains.annotations.Nullable;

public abstract class WaterskinEvent extends Event
{
    public static class Fill extends WaterskinEvent
    {
        private final Level level;
        private final BlockPos pos;
        private final @Nullable Player player;
        private final @Nullable FluidStack source;
        private ItemStack filledWaterskin;

        public Fill(Level level, BlockPos pos, @Nullable Player player, @Nullable FluidStack source, ItemStack filledWaterskin)
        {
            this.level = level;
            this.pos = pos;
            this.player = player;
            this.source = source;
            this.filledWaterskin = filledWaterskin;
        }

        public Level getLevel()
        {   return level;
        }

        public BlockPos getPos()
        {   return pos;
        }

        public @Nullable Player getPlayer()
        {   return player;
        }

        public @Nullable FluidStack getSource()
        {   return source;
        }

        public ItemStack getFilledWaterskin()
        {   return filledWaterskin;
        }

        public void setFilledWaterskin(ItemStack filledWaterskin)
        {   this.filledWaterskin = filledWaterskin;
        }
    }

    public static class Empty extends WaterskinEvent
    {
        private final ItemStack filledWaterskin;
        private ItemStack emptyWaterskin;

        public Empty(ItemStack filledWaterskin, ItemStack emptyWaterskin)
        {
            this.filledWaterskin = filledWaterskin;
            this.emptyWaterskin = emptyWaterskin;
        }

        public ItemStack getFilledWaterskin()
        {   return filledWaterskin;
        }

        public ItemStack getEmptyWaterskin()
        {   return emptyWaterskin;
        }

        public void setEmptyWaterskin(ItemStack emptyWaterskin)
        {   this.emptyWaterskin = emptyWaterskin;
        }
    }
}
