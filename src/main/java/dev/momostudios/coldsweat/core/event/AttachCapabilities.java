package dev.momostudios.coldsweat.core.event;

import dev.momostudios.coldsweat.common.capability.*;
import net.minecraft.core.Direction;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.player.Player;
import net.minecraftforge.common.capabilities.Capability;
import net.minecraftforge.common.capabilities.ICapabilityProvider;
import net.minecraftforge.common.util.INBTSerializable;
import net.minecraftforge.common.util.LazyOptional;
import net.minecraftforge.event.AttachCapabilitiesEvent;
import net.minecraftforge.event.entity.player.PlayerEvent;
import net.minecraftforge.eventbus.api.SubscribeEvent;
import net.minecraftforge.fml.common.Mod;
import dev.momostudios.coldsweat.ColdSweat;

import javax.annotation.Nonnull;
import javax.annotation.Nullable;

@Mod.EventBusSubscriber
public class AttachCapabilities
{
    private static class TempCapProvider implements ICapabilityProvider, INBTSerializable<CompoundTag>
    {
        private ITemperatureCap playerTempCap = new PlayerTempCapability();
        private final LazyOptional<ITemperatureCap> capOptional = LazyOptional.of(this::getCapability);

        @Nonnull
        private ITemperatureCap getCapability()
        {
            if (playerTempCap == null)
            {
                playerTempCap = new PlayerTempCapability();
            }
            return playerTempCap;
        }

        @Nonnull
        @Override
        public <T> LazyOptional<T> getCapability(@Nonnull Capability<T> cap, @Nullable Direction direction)
        {
            if (cap == ModCapabilities.PLAYER_TEMPERATURE)
            {
                return capOptional.cast();
            }
            return LazyOptional.empty();
        }

        void invalidate() {
            this.capOptional.invalidate();
        }

        @Override
        public CompoundTag serializeNBT() {
            return this.playerTempCap.serializeNBT();
        }

        @Override
        public void deserializeNBT(CompoundTag nbt) {
            this.playerTempCap.deserializeNBT(nbt);
        }
    }

    @SubscribeEvent
    public static void attachCapabilityToEntityHandler(final AttachCapabilitiesEvent<Entity> event)
    {
        if (event.getObject() instanceof Player)
        {
            if (!event.getObject().getCapability(ModCapabilities.PLAYER_TEMPERATURE, null).isPresent())
            {
                event.addCapability(new ResourceLocation(ColdSweat.MOD_ID, "temperature"), new TempCapProvider());
            }
        }
    }

    @SubscribeEvent
    public static void copyCaps(PlayerEvent.Clone event)
    {
        if (!event.isWasDeath())
        {
            Player oldPlayer = event.getOriginal();
            oldPlayer.revive();
            oldPlayer.getCapability(ModCapabilities.PLAYER_TEMPERATURE).ifPresent(oldTempCap ->
            {
                event.getPlayer().getCapability(ModCapabilities.PLAYER_TEMPERATURE).ifPresent(newTempCap ->
                {
                    newTempCap.copy(oldTempCap);
                });
            });
            oldPlayer.invalidateCaps();
        }
    }
}