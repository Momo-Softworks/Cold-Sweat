package com.momosoftworks.coldsweat.util;

import com.momosoftworks.coldsweat.client.event.HearthDebugRenderer;
import com.momosoftworks.coldsweat.client.gui.config.pages.ConfigPageOne;
import com.momosoftworks.coldsweat.util.math.CSMath;
import net.minecraft.client.Minecraft;
import net.minecraft.client.audio.EntityTickableSound;
import net.minecraft.client.network.play.ClientPlayNetHandler;
import net.minecraft.entity.Entity;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.util.SoundCategory;
import net.minecraft.util.SoundEvent;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.registry.DynamicRegistries;
import net.minecraft.world.GameType;
import net.minecraft.world.World;

import java.util.HashMap;

/**
 * This class is an abstraction layer for some methods in client-oriented classes
 * so Forge doesn't throw a fit when it tries to load the class on the wrong side.
 */
public class ClientOnlyHelper
{
    public static void playEntitySound(SoundEvent sound, SoundCategory source, float volume, float pitch, Entity entity)
    {   Minecraft.getInstance().getSoundManager().play(new EntityTickableSound(sound, source, volume, pitch, entity));
    }

    public static World getClientWorld()
    {   return Minecraft.getInstance().level;
    }

    public static void addHearthPosition(BlockPos pos)
    {   HearthDebugRenderer.HEARTH_LOCATIONS.put(pos, new HashMap<>());
    }

    public static void removeHearthPosition(BlockPos pos)
    {   HearthDebugRenderer.HEARTH_LOCATIONS.remove(pos);
    }

    public static void openConfigScreen()
    {   Minecraft.getInstance().setScreen(new ConfigPageOne(Minecraft.getInstance().screen));
    }

    public static PlayerEntity getClientPlayer()
    {   return Minecraft.getInstance().player;
    }

    public static GameType getGameMode()
    {   return Minecraft.getInstance().gameMode.getPlayerMode();
    }

    public static DynamicRegistries getRegistryAccess()
    {   return CSMath.orElse(CSMath.getIfNotNull(Minecraft.getInstance().getConnection(), ClientPlayNetHandler::registryAccess,  null),
                             CSMath.getIfNotNull(Minecraft.getInstance().level, World::registryAccess, null));
    }
}
