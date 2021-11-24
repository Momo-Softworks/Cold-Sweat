package net.momostudios.coldsweat.core.event;

import net.minecraft.client.Minecraft;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.nbt.CompoundNBT;
import net.minecraft.nbt.ListNBT;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.api.distmarker.OnlyIn;
import net.minecraftforge.common.capabilities.Capability;
import net.minecraftforge.common.capabilities.CapabilityManager;
import net.minecraftforge.event.TickEvent;
import net.minecraftforge.event.entity.player.PlayerEvent;
import net.minecraftforge.eventbus.api.SubscribeEvent;
import net.minecraftforge.fml.common.Mod;
import net.momostudios.coldsweat.ColdSweat;
import net.momostudios.coldsweat.client.event.AmbientGaugeDisplay;
import net.momostudios.coldsweat.common.temperature.modifier.TempModifier;
import net.momostudios.coldsweat.common.world.TempModifierEntries;
import net.momostudios.coldsweat.core.capabilities.ITemperatureCapability;
import net.momostudios.coldsweat.core.capabilities.PlayerTempCapability;
import net.momostudios.coldsweat.core.util.NBTHelper;
import net.momostudios.coldsweat.core.util.PlayerTemp;

import java.util.ArrayList;
import java.util.List;

@Mod.EventBusSubscriber
public class StorePlayerData
{
    @SubscribeEvent
    public static void onJoin(PlayerEvent.PlayerLoggedInEvent event)
    {
        PlayerEntity player = event.getPlayer();
        ColdSweat.LOGGER.debug("Reading temperature data for player " + player.getName().getString());

        player.getCapability(PlayerTempCapability.TEMPERATURE).ifPresent(cap ->
        {
            cap.set(PlayerTemp.Types.BODY, player.getPersistentData().getDouble("body_temperature"));
            cap.set(PlayerTemp.Types.BASE, player.getPersistentData().getDouble("base_temperature"));
            cap.set(PlayerTemp.Types.AMBIENT, player.getPersistentData().getDouble("ambient_temperature"));

            // Load the player's modifiers
            PlayerTemp.Types[] validTypes = {PlayerTemp.Types.AMBIENT, PlayerTemp.Types.BODY, PlayerTemp.Types.BASE, PlayerTemp.Types.RATE};
            for (PlayerTemp.Types type : validTypes)
            {
                // Get the list of modifiers from the player's persistent data
                ListNBT modifiers = player.getPersistentData().getList(PlayerTemp.getModifierTag(type), 10);
                // For each modifier in the list
                modifiers.forEach(modifier ->
                {
                    CompoundNBT modifierNBT = (CompoundNBT) modifier;

                    // Create a new modifier from the CompoundNBT
                    TempModifier newModifier = TempModifierEntries.getEntries().getEntryFor(modifierNBT.getString("id"));

                    modifierNBT.keySet().forEach(key ->
                    {
                        // Add the modifier's arguments
                        newModifier.addArgument(key, NBTHelper.getObjectFromINBT(modifierNBT.get(key)));
                    });

                    // Add the modifier to the player's temperature
                    cap.addModifier(type, newModifier);
                });
            }
        });
    }

    @SubscribeEvent
    public static void updateDataSync(TickEvent.PlayerTickEvent event)
    {
        if (event.player.ticksExisted % 60 == 0)
        {
            syncData(event.player);
        }
    }

    @SubscribeEvent
    public static void onPlayerLogout(PlayerEvent.PlayerLoggedOutEvent event)
    {
        ColdSweat.LOGGER.debug("Writing temperature data for player " + event.getPlayer().getName().getString());
        syncData(event.getPlayer());
    }

    public static void syncData(PlayerEntity player)
    {
        player.getCapability(PlayerTempCapability.TEMPERATURE).ifPresent(cap ->
        {
            // Save the player's temperature data
            String ambientTemp = PlayerTemp.getTempTag(PlayerTemp.Types.AMBIENT);
            String bodyTemp = PlayerTemp.getTempTag(PlayerTemp.Types.BODY);
            String baseTemp = PlayerTemp.getTempTag(PlayerTemp.Types.BASE);

            player.getPersistentData().putDouble(ambientTemp, cap.get(PlayerTemp.Types.BODY));
            player.getPersistentData().putDouble(bodyTemp, cap.get(PlayerTemp.Types.BASE));
            player.getPersistentData().putDouble(baseTemp, cap.get(PlayerTemp.Types.AMBIENT));

            // Save the player's modifiers
            PlayerTemp.Types[] validTypes = {PlayerTemp.Types.AMBIENT, PlayerTemp.Types.BODY, PlayerTemp.Types.BASE, PlayerTemp.Types.RATE};
            for (PlayerTemp.Types type : validTypes)
            {
                ListNBT modifiers = new ListNBT();
                List<String> modifierIds = new ArrayList<>();
                for (TempModifier modifier : cap.getModifiers(type))
                {
                    if (!modifierIds.contains(modifier.getID()))
                    {
                        // Write the modifier's data to a CompoundNBT
                        CompoundNBT modifierNBT = new CompoundNBT();
                        modifierNBT.putString("id", modifier.getID());
                        modifierIds.add(modifier.getID());

                        // Add the modifier's arguments
                        modifier.getArguments().forEach((name, value) ->
                        {
                            modifierNBT.put(name, NBTHelper.getINBTFromObject(value));
                        });
                        modifiers.add(modifierNBT);
                    }
                }
                // Write the list of modifiers to the player's persistent data
                player.getPersistentData().put(PlayerTemp.getModifierTag(type), modifiers);
            }
        });
    }
}
