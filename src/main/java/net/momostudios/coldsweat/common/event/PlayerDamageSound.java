package net.momostudios.coldsweat.common.event;

import net.minecraft.client.Minecraft;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.entity.player.ServerPlayerEntity;
import net.minecraft.util.ResourceLocation;
import net.minecraft.util.SoundCategory;
import net.minecraft.util.SoundEvent;
import net.minecraftforge.event.entity.living.LivingHurtEvent;
import net.minecraftforge.eventbus.api.SubscribeEvent;
import net.minecraftforge.fml.common.Mod;
import net.minecraftforge.fml.network.PacketDistributor;
import net.momostudios.coldsweat.ColdSweat;
import net.momostudios.coldsweat.core.network.ColdSweatPacketHandler;
import net.momostudios.coldsweat.core.network.message.PlayerDamageMessage;
import net.momostudios.coldsweat.core.util.CustomDamageTypes;

@Mod.EventBusSubscriber
public class PlayerDamageSound
{
    @SubscribeEvent
    public static void onPlayerHurt(LivingHurtEvent event)
    {
        if (event.getSource().equals(CustomDamageTypes.COLD) || event.getSource().equals(CustomDamageTypes.COLD_SCALED))
        {
            if (event.getEntity() instanceof PlayerEntity)
            {
                SoundEvent sound = new SoundEvent(new ResourceLocation(ColdSweat.MOD_ID, "entity.player.damage.freeze"));
                event.getEntity().world.playMovingSound(null, event.getEntity(), sound, SoundCategory.PLAYERS, 1f, (float) Math.random() * 0.3f + 0.7f);
                ColdSweatPacketHandler.INSTANCE.send(PacketDistributor.PLAYER.with(() -> (ServerPlayerEntity) event.getEntity()), new PlayerDamageMessage());
            }
        }
    }
}
