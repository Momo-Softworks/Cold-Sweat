package net.momostudios.coldsweat.client.event;

import net.minecraft.client.world.ClientWorld;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.util.ResourceLocation;
import net.minecraft.util.SoundEvent;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.event.entity.living.LivingAttackEvent;
import net.minecraftforge.eventbus.api.SubscribeEvent;
import net.minecraftforge.fml.common.Mod;
import net.momostudios.coldsweat.ColdSweat;
import net.momostudios.coldsweat.core.util.CustomDamageTypes;

@Mod.EventBusSubscriber(Dist.CLIENT)
public class PlayerDamageSound
{
    @SubscribeEvent
    public static void onPlayerHurt(LivingAttackEvent event)
    {
        if (event.getSource().equals(CustomDamageTypes.COLD) || event.getSource().equals(CustomDamageTypes.COLD_SCALED))
        {
            SoundEvent sound = new SoundEvent(new ResourceLocation(ColdSweat.MOD_ID, "entity.player.damage.freeze"));
            if (event.getEntity() instanceof PlayerEntity && event.getEntity().world.isRemote)
            {
                System.out.println("Playing sound");
                PlayerEntity player = (PlayerEntity) event.getEntity();
                ((PlayerEntity) event.getEntity()).playSound(sound, player.getSoundCategory(), 1f, (float) Math.random() * 0.3f + 0.7f);
            }
        }
    }
}
