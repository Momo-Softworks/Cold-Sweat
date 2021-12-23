package net.momostudios.coldsweat.client.event;

import net.minecraft.client.Minecraft;
import net.minecraft.client.audio.SimpleSound;
import net.minecraft.util.ResourceLocation;
import net.minecraft.util.SoundEvent;
import net.minecraft.util.SoundEvents;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.event.TickEvent;
import net.minecraftforge.eventbus.api.SubscribeEvent;
import net.minecraftforge.fml.common.Mod;
import net.momostudios.coldsweat.ColdSweat;
import net.momostudios.coldsweat.core.util.registrylists.ModSounds;

@Mod.EventBusSubscriber(Dist.CLIENT)
public class PlayerDamageSoundClient
{
    public static int playDamageSound = -1;

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
                    sound = ModSounds.SOUL_LAMP_ON;
                    break;
                case 2:
                    sound = ModSounds.SOUL_LAMP_OFF;
                    break;
                default:
                    sound = SoundEvents.AMBIENT_CAVE;
            }
            if (!Minecraft.getInstance().isSingleplayer())
                Minecraft.getInstance().getSoundHandler().play(SimpleSound.master(sound, (float) Math.random() * 0.3f + 0.8f, 0.7f));

            playDamageSound = -1;
        }
    }
}
