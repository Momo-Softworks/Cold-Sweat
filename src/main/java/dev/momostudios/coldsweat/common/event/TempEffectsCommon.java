package dev.momostudios.coldsweat.common.event;

import dev.momostudios.coldsweat.api.util.Temperature;
import dev.momostudios.coldsweat.config.ColdSweatConfig;
import dev.momostudios.coldsweat.util.math.CSMath;
import net.minecraft.world.entity.player.Player;
import net.minecraftforge.event.TickEvent;
import net.minecraftforge.event.entity.living.LivingHealEvent;
import net.minecraftforge.event.entity.living.LivingKnockBackEvent;
import net.minecraftforge.event.entity.player.PlayerEvent;
import net.minecraftforge.eventbus.api.SubscribeEvent;
import net.minecraftforge.fml.common.Mod;

@Mod.EventBusSubscriber
class TempEffectsCommon
{
    @SubscribeEvent
    public static void onPlayerMine(PlayerEvent.BreakSpeed event)
    {
        if (!ColdSweatConfig.getInstance().coldMining()) return;

        Player player = event.getPlayer();
        // Get the player's temperature
        float temp = (float) Temperature.get(player, Temperature.Type.BODY);
        // If the player is too cold, slow down their mining speed
        if (temp < -50)
        {
            event.setNewSpeed(event.getNewSpeed() * CSMath.blend(0.25f, 1, temp, -100, -50));
        }
    }

    // Decrease the player's movement speed if their temperature is below -50
    @SubscribeEvent
    public static void onPlayerTick(TickEvent.PlayerTickEvent event)
    {
        if (event.phase == TickEvent.Phase.END && ColdSweatConfig.getInstance().coldMovement())
        {
            Player player = event.player;
            float temp = (float) Temperature.get(player, Temperature.Type.BODY);
            if (temp < -50)
            {
                if (!player.isFallFlying())
                {
                    float moveSpeed = CSMath.blend(player.isOnGround() ? 0.5f : 0.75f, 1, temp, -100, -50);
                    player.setDeltaMovement(player.getDeltaMovement().multiply(moveSpeed, 1, moveSpeed));
                }
            }
        }
    }

    @SubscribeEvent
    public static void onPlayerKnockback(LivingKnockBackEvent event)
    {
        if (event.getEntityLiving().getLastHurtByMob() instanceof Player player && ColdSweatConfig.getInstance().coldKnockback())
        {
            float temp = (float) Temperature.get(player, Temperature.Type.BODY);
            if (temp < -50)
            {
                event.setStrength(event.getStrength() * CSMath.blend(0.5f, 1, temp, -100, -50));
            }
        }
    }

    // Prevent healing as temp decreases
    @SubscribeEvent
    public static void onHeal(LivingHealEvent event)
    {
        if (event.getEntityLiving() instanceof Player player && ColdSweatConfig.getInstance().freezingHearts())
        {
            float healing = event.getAmount();
            float temp = (float) Temperature.get(player, Temperature.Type.BODY);
            if (temp < -50)
            {
                event.setAmount(CSMath.clamp(healing, 0, CSMath.ceil(player.getMaxHealth() * CSMath.blend(0.5f, 1, temp, -100, -50)) - player.getHealth()));
            }
        }
    }
}
