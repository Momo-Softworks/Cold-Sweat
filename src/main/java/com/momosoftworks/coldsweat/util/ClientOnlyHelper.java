package com.momosoftworks.coldsweat.util;

import com.momosoftworks.coldsweat.client.event.HearthDebugRenderer;
import com.momosoftworks.coldsweat.client.gui.config.pages.ConfigPageOne;
import net.minecraft.client.Minecraft;
import net.minecraft.client.model.HumanoidModel;
import net.minecraft.client.model.PlayerModel;
import net.minecraft.client.renderer.entity.layers.RenderLayer;
import net.minecraft.client.resources.sounds.EntityBoundSoundInstance;
import net.minecraft.core.BlockPos;
import net.minecraft.network.protocol.game.ServerboundSetCreativeModeSlotPacket;
import net.minecraft.sounds.SoundEvent;
import net.minecraft.sounds.SoundSource;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.level.GameType;
import net.minecraft.world.level.Level;
import net.neoforged.fml.util.ObfuscationReflectionHelper;

import java.lang.reflect.Field;
import java.util.HashMap;

/**
 * This class is an abstraction layer for some methods in client-oriented classes
 * so Forge doesn't throw a fit when it tries to load the class on the wrong side.
 */
public class ClientOnlyHelper
{
    public static void playEntitySound(SoundEvent sound, SoundSource source, float volume, float pitch, Entity entity)
    {   Minecraft.getInstance().getSoundManager().play(new EntityBoundSoundInstance(sound, source, volume, pitch, entity, entity.level().random.nextLong()));
    }

    public static Level getClientLevel()
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

    public static Player getClientPlayer()
    {   return Minecraft.getInstance().player;
    }

    public static void sendPacketToServer(ServerboundSetCreativeModeSlotPacket packet)
    {   Minecraft.getInstance().getConnection().send(packet);
    }

    public static GameType getGameMode()
    {   return Minecraft.getInstance().gameMode.getPlayerMode();
    }

    private static final Field SLIM = ObfuscationReflectionHelper.findField(PlayerModel.class, "slim");
    static { SLIM.setAccessible(true); }

    public static boolean isPlayerModelSlim(RenderLayer<?, ?> layer)
    {
        if (layer.getParentModel() instanceof PlayerModel<?> playerModel)
        {
            try
            {   return (boolean) SLIM.get(playerModel);
            }
            catch (IllegalAccessException e)
            {   e.printStackTrace();
            }
        }
        return false;
    }

    public static boolean isPlayerModelSlim(HumanoidModel<?> model)
    {
        if (model instanceof PlayerModel<?> playerModel)
        {
            try
            {   return (boolean) SLIM.get(playerModel);
            }
            catch (IllegalAccessException e)
            {   e.printStackTrace();
            }
        }
        return false;
    }
}
