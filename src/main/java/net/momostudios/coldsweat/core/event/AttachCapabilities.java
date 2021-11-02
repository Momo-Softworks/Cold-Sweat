package net.momostudios.coldsweat.core.event;

import net.minecraft.entity.Entity;
import net.minecraft.tileentity.TileEntity;
import net.minecraft.util.Direction;
import net.minecraft.util.ResourceLocation;
import net.minecraftforge.common.capabilities.Capability;
import net.minecraftforge.common.capabilities.ICapabilityProvider;
import net.minecraftforge.common.util.LazyOptional;
import net.minecraftforge.event.AttachCapabilitiesEvent;
import net.minecraftforge.eventbus.api.SubscribeEvent;
import net.minecraftforge.fml.common.Mod;
import net.momostudios.coldsweat.ColdSweat;
import net.momostudios.coldsweat.common.te.HearthTileEntity;
import net.momostudios.coldsweat.core.capabilities.HearthRadiusCapability;
import net.momostudios.coldsweat.core.capabilities.IBlockStorageCap;
import net.momostudios.coldsweat.core.capabilities.ITemperatureCapability;
import net.momostudios.coldsweat.core.capabilities.PlayerTempCapability;

import javax.annotation.Nullable;


@Mod.EventBusSubscriber
public class AttachCapabilities
{
    @SubscribeEvent
    public static void attachCapabilityToEntityHandler(AttachCapabilitiesEvent<Entity> event)
    {
        PlayerTempCapability backend = new PlayerTempCapability();
        LazyOptional<ITemperatureCapability> optionalStorage = LazyOptional.of(() -> backend);

        ICapabilityProvider provider = new ICapabilityProvider()
        {
            @Override
            public <T> LazyOptional<T> getCapability(Capability<T> cap, @Nullable Direction direction)
            {
                if (cap == PlayerTempCapability.TEMPERATURE)
                {
                    return optionalStorage.cast();
                }
                return LazyOptional.empty();
            }
        };

        event.addCapability(new ResourceLocation(ColdSweat.MOD_ID, "temperature"), provider);
        event.addListener(optionalStorage::invalidate);
    }

    @SubscribeEvent
    public static void attachCapabilityToTileHandler(AttachCapabilitiesEvent<TileEntity> event)
    {
        if (!(event.getObject() instanceof HearthTileEntity)) return;

        HearthRadiusCapability backend = new HearthRadiusCapability();
        LazyOptional<IBlockStorageCap> optionalStorage = LazyOptional.of(() -> backend);

        ICapabilityProvider provider = new ICapabilityProvider()
        {
            @Override
            public <T> LazyOptional<T> getCapability(Capability<T> cap, @Nullable Direction direction)
            {
                if (cap == HearthRadiusCapability.HEARTH_BLOCKS)
                {
                    return optionalStorage.cast();
                }
                return LazyOptional.empty();
            }
        };

        event.addCapability(new ResourceLocation(ColdSweat.MOD_ID, "hearth_points"), provider);
        event.addListener(optionalStorage::invalidate);
    }
}
