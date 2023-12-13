package com.momosoftworks.coldsweat.util.entity;

import com.mojang.authlib.GameProfile;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.util.math.BlockPos;
import net.minecraft.world.World;

import java.util.UUID;

public class DummyPlayer extends PlayerEntity
{
    public DummyPlayer(World level)
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
