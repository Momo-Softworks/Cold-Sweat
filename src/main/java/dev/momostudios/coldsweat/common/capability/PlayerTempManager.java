package dev.momostudios.coldsweat.common.capability;

import dev.momostudios.coldsweat.ColdSweat;
import dev.momostudios.coldsweat.api.util.Temperature;
import net.minecraft.core.Direction;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.player.Player;
import net.minecraftforge.common.capabilities.Capability;
import net.minecraftforge.common.capabilities.ICapabilityProvider;
import net.minecraftforge.common.capabilities.ICapabilitySerializable;
import net.minecraftforge.common.util.LazyOptional;
import net.minecraftforge.event.AttachCapabilitiesEvent;
import net.minecraftforge.event.TickEvent;
import net.minecraftforge.event.entity.player.PlayerEvent;
import net.minecraftforge.eventbus.api.SubscribeEvent;
import net.minecraftforge.fml.common.Mod;

import javax.annotation.Nonnull;
import javax.annotation.Nullable;

@Mod.EventBusSubscriber
public class PlayerTempManager
{
    @SubscribeEvent
    public static void attachCapabilityToEntityHandler(AttachCapabilitiesEvent<Entity> event)
    {
        if (event.getObject() instanceof Player)
        {
            // Make a new capability instance to attach to the entity
            ITemperatureCap playerTempCap = new PlayerTempCap();
            // Optional that holds the capability instance
            LazyOptional<ITemperatureCap> capOptional = LazyOptional.of(() -> playerTempCap);
            Capability<ITemperatureCap> capability = ModCapabilities.PLAYER_TEMPERATURE;

            ICapabilityProvider provider = new ICapabilitySerializable<CompoundTag>()
            {
                @Nonnull
                @Override
                public <T> LazyOptional<T> getCapability(@Nonnull Capability<T> cap, @Nullable Direction direction)
                {
                    // If the requested cap is the temperature cap, return the temperature cap
                    if (cap == capability)
                    {
                        return capOptional.cast();
                    }
                    return LazyOptional.empty();
                }

                @Override
                public CompoundTag serializeNBT()
                {
                    return playerTempCap.serializeNBT();
                }

                @Override
                public void deserializeNBT(CompoundTag nbt)
                {
                    playerTempCap.deserializeNBT(nbt);
                }
            };

            event.addCapability(new ResourceLocation(ColdSweat.MOD_ID, "temperature"), provider);
        }
    }

    @SubscribeEvent
    public static void onPlayerTick(TickEvent.PlayerTickEvent event)
    {
        if (event.phase == TickEvent.Phase.START)
        {
            Player player = event.player;
            player.getCapability(ModCapabilities.PLAYER_TEMPERATURE).ifPresent(cap ->
            {
                if (event.side.isServer())
                {
                    // Tick modifiers serverside
                    cap.tick(player);
                }
                else
                {
                    // Tick modifiers clientside
                    cap.tickDummy(player);
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
            });
        }
    }

    /**
     * Transfer the player's capability when traveling from the End
     */
    @SubscribeEvent
    public static void returnFromEnd(PlayerEvent.Clone event)
    {
        if (!event.isWasDeath() && !event.getPlayer().level.isClientSide)
        {
            // Get the old player's capability
            Player oldPlayer = event.getOriginal();
            oldPlayer.reviveCaps();

            // Copy the capability to the new player
            event.getPlayer().getCapability(ModCapabilities.PLAYER_TEMPERATURE).ifPresent(cap ->
            {
               oldPlayer.getCapability(ModCapabilities.PLAYER_TEMPERATURE).ifPresent(cap::copy);
            });

            oldPlayer.invalidateCaps();
        }
    }
}