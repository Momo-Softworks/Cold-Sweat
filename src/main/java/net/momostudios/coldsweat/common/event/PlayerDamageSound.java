package net.momostudios.coldsweat.common.event;

import net.minecraft.client.Minecraft;
import net.minecraft.client.audio.SimpleSound;
import net.minecraftforge.event.entity.living.LivingHurtEvent;
import net.minecraftforge.eventbus.api.SubscribeEvent;
import net.minecraftforge.fml.common.Mod;
import net.momostudios.coldsweat.core.util.CustomDamageTypes;
import net.momostudios.coldsweat.core.util.registrylists.ModSounds;

@Mod.EventBusSubscriber
public class PlayerDamageSound
{
    @SubscribeEvent
    public static void onPlayerHurt(LivingHurtEvent event)
    {
        if (event.getSource().equals(CustomDamageTypes.COLD) || event.getSource().equals(CustomDamageTypes.COLD_SCALED))
        {
            Minecraft.getInstance().getSoundHandler().play(SimpleSound.master(ModSounds.FREEZE, (float) Math.random() * 0.3f + 0.7f, 1.0f));
            event.getEntity().playSound(ModSounds.FREEZE, 1.0f, (float) Math.random() * 0.3f + 0.7f);
        }
    }
}
