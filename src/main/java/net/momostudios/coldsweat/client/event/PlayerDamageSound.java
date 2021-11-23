package net.momostudios.coldsweat.client.event;

import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.util.ResourceLocation;
import net.minecraft.util.SoundEvent;
import net.minecraft.util.math.vector.Vector3d;
import net.minecraft.world.server.ServerWorld;
import net.minecraftforge.event.entity.living.LivingHurtEvent;
import net.minecraftforge.eventbus.api.SubscribeEvent;
import net.minecraftforge.fml.common.Mod;
import net.momostudios.coldsweat.ColdSweat;
import net.momostudios.coldsweat.core.util.CustomDamageTypes;

@Mod.EventBusSubscriber
public class PlayerDamageSound
{
    @SubscribeEvent
    public static void onPlayerHurt(LivingHurtEvent event)
    {
        if (event.getSource().equals(CustomDamageTypes.COLD) || event.getSource().equals(CustomDamageTypes.COLD_SCALED))
        {
            SoundEvent sound = new SoundEvent(new ResourceLocation(ColdSweat.MOD_ID, "entity.player.damage.freeze"));
            Vector3d pos = event.getEntity().getPositionVec();
            if (event.getEntity() instanceof PlayerEntity && event.getEntity().world instanceof ServerWorld)
            {
                PlayerEntity player = (PlayerEntity) event.getEntity();
                ((ServerWorld) player.world).playMovingSound(null, player, sound, player.getSoundCategory(), 2f, (float) Math.random() * 0.3f + 0.7f);
            }
        }
    }
}
