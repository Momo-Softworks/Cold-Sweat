package com.momosoftworks.coldsweat.util.entity;

import com.mojang.authlib.GameProfile;
import com.momosoftworks.coldsweat.util.world.WorldHelper;
import net.minecraft.core.BlockPos;
import net.minecraft.resources.ResourceKey;
import net.minecraft.server.MinecraftServer;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.world.RandomSequences;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.level.GameType;
import net.minecraft.world.level.CustomSpawner;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.dimension.LevelStem;
import net.minecraft.world.level.storage.LevelStorageSource;
import net.minecraft.world.level.storage.ServerLevelData;
import net.neoforged.neoforge.server.ServerLifecycleHooks;
import org.jetbrains.annotations.Nullable;

import java.util.List;
import java.util.UUID;
import java.util.concurrent.Executor;

public class DummyPlayer extends Player
{
    public DummyPlayer(Level level)
    {   super(level, new GameProfile(UUID.randomUUID(), "DummyPlayer"));
    }

    public DummyPlayer()
    {   super(ServerLifecycleHooks.getCurrentServer().getLevel(Level.OVERWORLD), new GameProfile(UUID.randomUUID(), "DummyPlayer"));
    }

    @Override
    public GameType gameMode()
    {   return GameType.SURVIVAL;
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
