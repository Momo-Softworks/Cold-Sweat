package dev.momostudios.coldsweat.core.event;

import net.minecraft.core.Direction;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.nbt.ListTag;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraftforge.common.capabilities.Capability;
import net.minecraftforge.common.capabilities.ICapabilityProvider;
import net.minecraftforge.common.capabilities.ICapabilitySerializable;
import net.minecraftforge.common.util.LazyOptional;
import net.minecraftforge.event.AttachCapabilitiesEvent;
import net.minecraftforge.eventbus.api.SubscribeEvent;
import net.minecraftforge.fml.common.Mod;
import dev.momostudios.coldsweat.ColdSweat;
import dev.momostudios.coldsweat.common.te.HearthBlockEntity;
import dev.momostudios.coldsweat.common.temperature.modifier.TempModifier;
import dev.momostudios.coldsweat.core.capabilities.HearthRadiusCapability;
import dev.momostudios.coldsweat.core.capabilities.IBlockStorageCap;
import dev.momostudios.coldsweat.core.capabilities.PlayerTempCapability;
import dev.momostudios.coldsweat.util.NBTHelper;
import dev.momostudios.coldsweat.util.PlayerHelper;

import javax.annotation.Nonnull;
import javax.annotation.Nullable;

@Mod.EventBusSubscriber
public class AttachCapabilities
{
    @SubscribeEvent
    public static void attachCapabilityToEntityHandler(final AttachCapabilitiesEvent<Entity> event)
    {
        if (!(event.getObject() instanceof Player)) return;

        PlayerTempCapability backend = new PlayerTempCapability();
        LazyOptional<PlayerTempCapability> optionalStorage = LazyOptional.of(() -> backend);
        Capability<PlayerTempCapability> capability = PlayerTempCapability.TEMPERATURE;

        ICapabilityProvider provider = new ICapabilitySerializable<CompoundTag>()
        {
            @Nonnull
            @Override
            public <T> LazyOptional<T> getCapability(@Nonnull Capability<T> cap, @Nullable Direction direction)
            {
                if (cap == capability)
                {
                    return optionalStorage.cast();
                }
                return LazyOptional.empty();
            }

            @Override
            public CompoundTag serializeNBT()
            {
                CompoundTag nbt = new CompoundTag();

                // Save the player's temperature data
                nbt.putDouble(PlayerHelper.getTempTag(PlayerHelper.Types.BODY), backend.get(PlayerHelper.Types.BODY));
                nbt.putDouble(PlayerHelper.getTempTag(PlayerHelper.Types.BASE), backend.get(PlayerHelper.Types.BASE));

                // Save the player's modifiers
                PlayerHelper.Types[] validTypes = {PlayerHelper.Types.BODY, PlayerHelper.Types.BASE, PlayerHelper.Types.RATE};
                for (PlayerHelper.Types type : validTypes)
                {
                    ListTag modifiers = new ListTag();
                    for (TempModifier modifier : backend.getModifiers(type))
                    {
                        modifiers.add(NBTHelper.modifierToTag(modifier));
                    }

                    // Write the list of modifiers to the player's persistent data
                    nbt.put(PlayerHelper.getModifierTag(type), modifiers);
                }
                return nbt;
            }

            @Override
            public void deserializeNBT(CompoundTag nbt)
            {
                backend.set(PlayerHelper.Types.BODY, nbt.getDouble(PlayerHelper.getTempTag(PlayerHelper.Types.BODY)));
                backend.set(PlayerHelper.Types.BASE, nbt.getDouble(PlayerHelper.getTempTag(PlayerHelper.Types.BASE)));

                // Load the player's modifiers
                PlayerHelper.Types[] validTypes = {PlayerHelper.Types.BODY, PlayerHelper.Types.BASE, PlayerHelper.Types.RATE};
                for (PlayerHelper.Types type : validTypes)
                {
                    // Get the list of modifiers from the player's persistent data
                    ListTag modifiers = nbt.getList(PlayerHelper.getModifierTag(type), 10);

                    // For each modifier in the list
                    modifiers.forEach(modifier ->
                    {
                        CompoundTag modifierNBT = (CompoundTag) modifier;

                        // Add the modifier to the player's temperature
                        backend.getModifiers(type).add(NBTHelper.TagToModifier(modifierNBT));
                    });
                }
            }
        };

        event.addCapability(new ResourceLocation(ColdSweat.MOD_ID, "temperature"), provider);
        event.addListener(optionalStorage::invalidate);
    }

    @SubscribeEvent
    public static void attachCapabilityToTileHandler(AttachCapabilitiesEvent<BlockEntity> event)
    {
        if (!(event.getObject() instanceof HearthBlockEntity)) return;

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