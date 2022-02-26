package dev.momostudios.coldsweat.client.event;

import net.minecraft.client.Minecraft;
import net.minecraft.client.resources.sounds.EntityBoundSoundInstance;
import net.minecraft.sounds.SoundEvent;
import net.minecraft.sounds.SoundEvents;
import net.minecraft.sounds.SoundSource;
import net.minecraft.world.entity.Entity;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.event.TickEvent;
import net.minecraftforge.eventbus.api.SubscribeEvent;
import net.minecraftforge.fml.common.Mod;
import dev.momostudios.coldsweat.util.registrylists.ModSounds;

@Mod.EventBusSubscriber(Dist.CLIENT)
public class ClientSoundHandler
{
    public static int playDamageSound = -1;
    public static float volume = 1f;
    public static float pitch = 1f;
    public static Entity entity = null;

    @SubscribeEvent
    public static void playDamageSound(TickEvent.ClientTickEvent event)
    {
        if (playDamageSound != -1)
        {
            SoundEvent sound;
            switch (playDamageSound)
            {
                case 0:
                    sound = ModSounds.FREEZE;
                    break;
                case 1:
                    sound = ModSounds.NETHER_LAMP_ON;
                    break;
                case 2:
                    sound = ModSounds.NETHER_LAMP_OFF;
                    break;
                default:
                    sound = SoundEvents.AMBIENT_CAVE;
            }

            if (entity != null)
                Minecraft.getInstance().getSoundManager().play(new EntityBoundSoundInstance(sound, SoundSource.PLAYERS, volume, pitch, entity));

            playDamageSound = -1;
            volume = 1f;
            pitch = 1f;
        }
    }
}
