package net.momostudios.coldsweat.client.event;

import net.minecraft.client.Minecraft;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.util.ResourceLocation;
import net.minecraft.util.SoundEvent;
import net.minecraftforge.event.entity.living.LivingHurtEvent;
import net.minecraftforge.eventbus.api.SubscribeEvent;
import net.minecraftforge.fml.common.Mod;
import net.minecraftforge.fml.common.ObfuscationReflectionHelper;
import net.momostudios.coldsweat.ColdSweat;
import net.momostudios.coldsweat.core.util.CustomDamageTypes;

import java.lang.reflect.Method;

@Mod.EventBusSubscriber
public class PlayerDamageSound
{
    static Method playMaster;
    static Method getSoundHandler;
    static Method play;
    static
    {
        try
        {
            playMaster = ObfuscationReflectionHelper.findMethod(Class.forName("net.minecraft.client.audio.SimpleSound"), "func_184371_a", SoundEvent.class, float.class, float.class);
            getSoundHandler = ObfuscationReflectionHelper.findMethod(Minecraft.class, "func_147118_V");
            play = ObfuscationReflectionHelper.findMethod(Class.forName("net.minecraft.client.audio.SoundHandler"), "func_147682_a", Class.forName("net.minecraft.client.audio.ISound"));
        } catch (Exception e) {}
    }

    @SubscribeEvent
    public static void onPlayerHurt(LivingHurtEvent event)
    {
        if (event.getSource().equals(CustomDamageTypes.COLD) || event.getSource().equals(CustomDamageTypes.COLD_SCALED))
        {
            if (event.getEntity() instanceof PlayerEntity)
            {
                SoundEvent sound = new SoundEvent(new ResourceLocation(ColdSweat.MOD_ID, "entity.player.damage.freeze"));
                event.getEntity().playSound(sound, 1f, (float) Math.random() * 0.3f + 0.7f);
                try
                {
                    play.invoke(getSoundHandler.invoke(Minecraft.getInstance()), playMaster.invoke(null, sound, (float) Math.random() * 0.3f + 0.7f, 1f));
                } catch (Exception e) {}
            }
        }
    }
}
