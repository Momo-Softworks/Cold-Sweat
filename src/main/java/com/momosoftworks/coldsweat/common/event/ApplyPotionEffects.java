package com.momosoftworks.coldsweat.common.event;

import com.momosoftworks.coldsweat.config.ConfigSettings;
import com.momosoftworks.coldsweat.util.registries.ModEffects;
import net.minecraft.world.effect.MobEffectInstance;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.Items;
import net.minecraftforge.event.entity.EntityJoinLevelEvent;
import net.minecraftforge.event.entity.living.LivingEntityUseItemEvent;
import net.minecraftforge.eventbus.api.SubscribeEvent;
import net.minecraftforge.fml.common.Mod;

@Mod.EventBusSubscriber
public class ApplyPotionEffects
{
    @SubscribeEvent
    public static void onSpawn(EntityJoinLevelEvent event)
    {
        if (!event.getLevel().isClientSide && event.getEntity() instanceof Player
        && ConfigSettings.GRACE_ENABLED.get() && !event.getEntity().getPersistentData().getBoolean("GivenGracePeriod"))
        {
            event.getEntity().getPersistentData().putBoolean("GivenGracePeriod", true);
            ((Player) event.getEntity()).addEffect(new MobEffectInstance(ModEffects.GRACE, ConfigSettings.GRACE_LENGTH.get(), 0, false, false, true));
        }
    }

    @SubscribeEvent
    public static void onGoldenAppleEaten(LivingEntityUseItemEvent.Finish event)
    {
        ItemStack item = event.getItem();
        if (item.is(Items.ENCHANTED_GOLDEN_APPLE))
        {   event.getEntity().addEffect(new MobEffectInstance(ModEffects.ICE_RESISTANCE, 6000, 0));
        }
    }
}
