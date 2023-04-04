package dev.momostudios.coldsweat.common.capability;

import dev.momostudios.coldsweat.ColdSweat;
import net.minecraft.core.Direction;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.animal.goat.Goat;
import net.minecraftforge.common.capabilities.Capability;
import net.minecraftforge.common.capabilities.ICapabilityProvider;
import net.minecraftforge.common.capabilities.ICapabilitySerializable;
import net.minecraftforge.common.util.LazyOptional;
import net.minecraftforge.event.AttachCapabilitiesEvent;
import net.minecraftforge.eventbus.api.SubscribeEvent;
import net.minecraftforge.fml.common.Mod;

import javax.annotation.Nonnull;
import javax.annotation.Nullable;

@Mod.EventBusSubscriber
public class AttachGoatCaps
{
    @SubscribeEvent
    public static void attachCapabilityToEntityHandler(AttachCapabilitiesEvent<Entity> event)
    {
        if (event.getObject() instanceof Goat)
        {
            // Make a new capability instance to attach to the entity
            IShearableCap cap = new GoatFurCap();
            // Optional that holds the capability instance
            LazyOptional<IShearableCap> capOptional = LazyOptional.of(() -> cap);
            Capability<IShearableCap> capability = ModCapabilities.SHEARABLE_FUR;

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
                    return cap.serializeNBT();
                }

                @Override
                public void deserializeNBT(CompoundTag nbt)
                {
                    cap.deserializeNBT(nbt);
                }
            };

            event.addCapability(new ResourceLocation(ColdSweat.MOD_ID, "goat_fur"), provider);
        }
    }
}
