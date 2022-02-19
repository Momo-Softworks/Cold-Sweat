package net.momostudios.coldsweat.core.event;

import net.minecraft.entity.Entity;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.nbt.CompoundNBT;
import net.minecraft.nbt.INBT;
import net.minecraft.nbt.ListNBT;
import net.minecraft.tileentity.TileEntity;
import net.minecraft.util.Direction;
import net.minecraft.util.ResourceLocation;
import net.minecraftforge.common.capabilities.Capability;
import net.minecraftforge.common.capabilities.ICapabilityProvider;
import net.minecraftforge.common.capabilities.ICapabilitySerializable;
import net.minecraftforge.common.util.LazyOptional;
import net.minecraftforge.event.AttachCapabilitiesEvent;
import net.minecraftforge.eventbus.api.SubscribeEvent;
import net.minecraftforge.fml.common.Mod;
import net.momostudios.coldsweat.ColdSweat;
import net.momostudios.coldsweat.common.te.HearthTileEntity;
import net.momostudios.coldsweat.common.temperature.modifier.TempModifier;
import net.momostudios.coldsweat.core.capabilities.HearthRadiusCapability;
import net.momostudios.coldsweat.core.capabilities.IBlockStorageCap;
import net.momostudios.coldsweat.core.capabilities.PlayerTempCapability;
import net.momostudios.coldsweat.util.NBTHelper;
import net.momostudios.coldsweat.util.PlayerHelper;

import javax.annotation.Nullable;


@Mod.EventBusSubscriber
public class AttachCapabilities
{
    @SubscribeEvent
    public static void attachCapabilityToEntityHandler(AttachCapabilitiesEvent<Entity> event)
    {
        if (event.getObject() instanceof PlayerEntity)
        {
            PlayerTempCapability backend = new PlayerTempCapability();
            LazyOptional<PlayerTempCapability> optionalStorage = LazyOptional.of(() -> backend);

            ICapabilityProvider provider = new ICapabilitySerializable<CompoundNBT>()
            {
                public CompoundNBT serializeNBT()
                {
                    CompoundNBT nbt = new CompoundNBT();

                    // Save the player's temperature data
                    nbt.putDouble(PlayerHelper.getTempTag(PlayerHelper.Types.BODY), backend.get(PlayerHelper.Types.BODY));
                    nbt.putDouble(PlayerHelper.getTempTag(PlayerHelper.Types.BASE), backend.get(PlayerHelper.Types.BASE));

                    // Save the player's modifiers
                    PlayerHelper.Types[] validTypes = {PlayerHelper.Types.BODY, PlayerHelper.Types.BASE, PlayerHelper.Types.RATE};
                    for (PlayerHelper.Types type : validTypes)
                    {
                        ListNBT modifiers = new ListNBT();
                        for (TempModifier modifier : backend.getModifiers(type))
                        {
                            modifiers.add(NBTHelper.modifierToNBT(modifier));
                        }

                        // Write the list of modifiers to the player's persistent data
                        nbt.put(PlayerHelper.getModifierTag(type), modifiers);
                    }
                    return nbt;
                }

                public void deserializeNBT(CompoundNBT nbt)
                {
                    backend.set(PlayerHelper.Types.BODY, nbt.getDouble(PlayerHelper.getTempTag(PlayerHelper.Types.BODY)));
                    backend.set(PlayerHelper.Types.BASE, nbt.getDouble(PlayerHelper.getTempTag(PlayerHelper.Types.BASE)));

                    // Load the player's modifiers
                    PlayerHelper.Types[] validTypes = {PlayerHelper.Types.BODY, PlayerHelper.Types.BASE, PlayerHelper.Types.RATE};
                    for (PlayerHelper.Types type : validTypes)
                    {
                        // Get the list of modifiers from the player's persistent data
                        ListNBT modifiers = nbt.getList(PlayerHelper.getModifierTag(type), 10);

                        // For each modifier in the list
                        modifiers.forEach(modifier ->
                        {
                            CompoundNBT modifierNBT = (CompoundNBT) modifier;

                            // Add the modifier to the player's temperature
                            backend.getModifiers(type).add(NBTHelper.NBTToModifier(modifierNBT));
                        });
                    }
                }

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