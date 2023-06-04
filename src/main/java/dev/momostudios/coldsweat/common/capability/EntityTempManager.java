package dev.momostudios.coldsweat.common.capability;

import dev.momostudios.coldsweat.ColdSweat;
import dev.momostudios.coldsweat.api.event.common.EnableTemperatureEvent;
import dev.momostudios.coldsweat.api.util.Temperature;
import net.minecraft.core.Direction;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.player.Player;
import net.minecraftforge.common.MinecraftForge;
import net.minecraftforge.common.capabilities.Capability;
import net.minecraftforge.common.capabilities.ICapabilityProvider;
import net.minecraftforge.common.capabilities.ICapabilitySerializable;
import net.minecraftforge.common.util.LazyOptional;
import net.minecraftforge.event.AttachCapabilitiesEvent;
import net.minecraftforge.event.entity.living.LivingEvent;
import net.minecraftforge.event.entity.player.PlayerEvent;
import net.minecraftforge.eventbus.api.SubscribeEvent;
import net.minecraftforge.fml.common.Mod;

import javax.annotation.Nonnull;
import javax.annotation.Nullable;

@Mod.EventBusSubscriber
public class EntityTempManager
{
    @SubscribeEvent
    public static void attachCapabilityToEntityHandler(AttachCapabilitiesEvent<Entity> event)
    {
        if (event.getObject() instanceof LivingEntity entity)
        {
            // Players always get the capability
            if (!(entity instanceof Player))
            {
                EnableTemperatureEvent enableEvent = new EnableTemperatureEvent(entity);
                MinecraftForge.EVENT_BUS.post(enableEvent);
                if (!enableEvent.isEnabled() || enableEvent.isCanceled()) return;
                EnableTemperatureEvent.ENABLED_ENTITIES.add(entity.getType());
            }

            // Make a new capability instance to attach to the entity
            ITemperatureCap tempCap = entity instanceof Player ? new PlayerTempCap() : new EntityTempCap();
            // Optional that holds the capability instance
            LazyOptional<ITemperatureCap> capOptional = LazyOptional.of(() -> tempCap);

            // Capability provider
            ICapabilityProvider provider = new ICapabilitySerializable<CompoundTag>()
            {
                @Nonnull
                @Override
                public <T> LazyOptional<T> getCapability(@Nonnull Capability<T> cap, @Nullable Direction direction)
                {
                    // If the requested cap is the temperature cap, return the temperature cap
                    if (cap == ModCapabilities.PLAYER_TEMPERATURE)
                    {
                        return capOptional.cast();
                    }
                    return LazyOptional.empty();
                }

                @Override
                public CompoundTag serializeNBT()
                {
                    return tempCap.serializeNBT();
                }

                @Override
                public void deserializeNBT(CompoundTag nbt)
                {
                    tempCap.deserializeNBT(nbt);
                }
            };

            // Attach the capability to the entity
            event.addCapability(new ResourceLocation(ColdSweat.MOD_ID, "temperature"), provider);
        }
    }

    @SubscribeEvent
    public static void onLivingTick(LivingEvent.LivingTickEvent event)
    {
        LivingEntity entity = event.getEntity();
        if (!(entity instanceof Player || EnableTemperatureEvent.ENABLED_ENTITIES.contains(entity.getType()))) return;

        Temperature.getTemperatureCap(entity).ifPresent(cap ->
        {
            if (!entity.level.isClientSide)
            {   // Tick modifiers serverside
                cap.tick(entity);
            }
            else
            {   // Tick modifiers clientside
                cap.tickDummy(entity);
            }

            // Remove expired modifiers
            for (Temperature.Type type : PlayerTempCap.VALID_MODIFIER_TYPES)
            {
                cap.getModifiers(type).removeIf(modifier ->
                {
                    int expireTime = modifier.getExpireTime();
                    return (modifier.setTicksExisted(modifier.getTicksExisted() + 1) > expireTime && expireTime != -1);
                });
            }

            if (entity instanceof Player && entity.tickCount % 60 == 0)
            {   Temperature.updateModifiers(entity, cap);
            }
        });
    }

    /**
     * Transfer the player's capability when traveling from the End
     */
    @SubscribeEvent
    public static void returnFromEnd(PlayerEvent.Clone event)
    {
        if (!event.isWasDeath() && !event.getEntity().level.isClientSide)
        {
            // Get the old player's capability
            Player oldPlayer = event.getOriginal();
            oldPlayer.reviveCaps();

            // Copy the capability to the new player
            event.getEntity().getCapability(ModCapabilities.PLAYER_TEMPERATURE).ifPresent(cap ->
            {
               oldPlayer.getCapability(ModCapabilities.PLAYER_TEMPERATURE).ifPresent(cap::copy);
            });

            oldPlayer.invalidateCaps();
        }
    }
}