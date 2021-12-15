package net.momostudios.coldsweat.client.event;

import net.minecraft.client.Minecraft;
import net.minecraft.client.audio.SimpleSound;
import net.minecraft.util.ResourceLocation;
import net.minecraft.util.SoundEvent;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.event.TickEvent;
import net.minecraftforge.eventbus.api.SubscribeEvent;
import net.minecraftforge.fml.common.Mod;
import net.momostudios.coldsweat.ColdSweat;

@Mod.EventBusSubscriber(Dist.CLIENT)
public class PlayerDamageSoundClient
{
    public static boolean playDamageSound = false;

    @SubscribeEvent
    public static void playDamageSound(TickEvent.ClientTickEvent event)
    {
        if (playDamageSound)
        {
            playDamageSound = false;
            SoundEvent sound = new SoundEvent(new ResourceLocation(ColdSweat.MOD_ID, "entity.player.damage.freeze"));
            Minecraft.getInstance().getSoundHandler().play(SimpleSound.master(sound, (float) Math.random() * 0.3f + 0.8f, 0.7f));
        }
    }
}
