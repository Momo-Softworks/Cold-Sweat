package com.momosoftworks.coldsweat.util.entity;

import com.mojang.authlib.GameProfile;
import net.minecraft.core.BlockPos;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.level.Level;

import java.util.UUID;

public class DummyPlayer extends Player
{
    public DummyPlayer(Level level)
    {   super(level, BlockPos.ZERO, 0, new GameProfile(UUID.randomUUID(), "DummyPlayer"));
    }

    @Override
    public boolean isSpectator()
    {   return false;
    }

    @Override
    public boolean isCreative()
    {   return false;
    }
}
