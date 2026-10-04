package com.momosoftworks.coldsweat.api.event.common.item;

import net.minecraft.core.BlockPos;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.Level;
import net.minecraftforge.event.entity.player.PlayerEvent;
import net.minecraftforge.fluids.FluidStack;

import javax.annotation.Nullable;

public abstract class WaterskinEvent extends PlayerEvent
{
    protected final ItemStack waterskin;
    protected ItemStack resultWaterskin;

    protected WaterskinEvent(ItemStack waterskin, ItemStack resultWaterskin, @Nullable Player player)
    {   super(player);
        this.waterskin = waterskin;
        this.resultWaterskin = resultWaterskin;
    }

    public ItemStack getWaterskin()
    {   return waterskin;
    }
    public ItemStack getResultWaterskin()
    {   return resultWaterskin;
    }

    public void setResultWaterskin(ItemStack resultWaterskin)
    {   this.resultWaterskin = resultWaterskin;
    }

    public static class Fill extends WaterskinEvent
    {
        protected final Level level;
        protected final BlockPos sourcePos;
        protected final FluidStack source;

        public Fill(ItemStack waterskin, ItemStack resultWaterskin, @Nullable Player player, Level level, BlockPos sourcePos, FluidStack source)
        {   super(waterskin, resultWaterskin, player);
            this.level = level;
            this.sourcePos = sourcePos;
            this.source = source;
        }

        public Level getLevel()
        {   return level;
        }
        public BlockPos getSourcePos()
        {   return sourcePos;
        }
        public FluidStack getSource()
        {   return source;
        }
    }

    public static class Empty extends WaterskinEvent
    {
        public Empty(ItemStack waterskin, ItemStack resultWaterskin, @Nullable Player player)
        {   super(waterskin, resultWaterskin, player);
        }
    }
}
