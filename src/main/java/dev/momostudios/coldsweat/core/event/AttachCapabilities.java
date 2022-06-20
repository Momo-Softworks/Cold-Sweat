package dev.momostudios.coldsweat.core.event;

import dev.momostudios.coldsweat.ColdSweat;
import dev.momostudios.coldsweat.common.capability.ITemperatureCap;
import dev.momostudios.coldsweat.common.capability.ModCapabilities;
import dev.momostudios.coldsweat.common.capability.PlayerTempCapability;
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
import net.minecraftforge.event.entity.player.PlayerEvent;
import net.minecraftforge.eventbus.api.SubscribeEvent;
import net.minecraftforge.fml.common.Mod;

import javax.annotation.Nonnull;
import javax.annotation.Nullable;

@Mod.EventBusSubscriber
public class AttachCapabilities
{
    @SubscribeEvent
    public static void attachCapabilityToEntityHandler(final AttachCapabilitiesEvent<Entity> event)
    {
        if (!(event.getObject() instanceof Player)) return;

        // Make a new capability instance to attach to the entity
        ITemperatureCap playerTempCap = new PlayerTempCapability();
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

    /**
     * Transfer the player's capability when traveling from the End
     */
    @SubscribeEvent
    public static void copyCaps(PlayerEvent.Clone event)
    {
        if (!event.isWasDeath())
        {
            // Get the old player's capability
            Player oldPlayer = event.getOriginal();
            oldPlayer.reviveCaps();

            oldPlayer.getCapability(ModCapabilities.PLAYER_TEMPERATURE).ifPresent(oldTempCap ->
            {
                event.getPlayer().getCapability(ModCapabilities.PLAYER_TEMPERATURE).ifPresent(newTempCap ->
                {
                    // Copy the capability to the new player
                    newTempCap.copy(oldTempCap);
                });
            });
            // Remove the old player's capability (we don't need it anymore)
            oldPlayer.invalidateCaps();
        }
    }
}