package net.momostudios.coldsweat.core.event;

import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.entity.player.ServerPlayerEntity;
import net.minecraft.nbt.CompoundNBT;
import net.minecraft.nbt.ListNBT;
import net.minecraftforge.event.entity.player.PlayerEvent;
import net.minecraftforge.eventbus.api.SubscribeEvent;
import net.minecraftforge.fml.common.Mod;
import net.momostudios.coldsweat.ColdSweat;
import net.momostudios.coldsweat.common.temperature.modifier.TempModifier;
import net.momostudios.coldsweat.common.world.TempModifierEntries;
import net.momostudios.coldsweat.core.capabilities.PlayerTempCapability;
import net.momostudios.coldsweat.core.util.NBTHelper;
import net.momostudios.coldsweat.core.util.PlayerHelper;
import net.momostudios.coldsweat.core.util.PlayerTemp;

import java.util.ArrayList;
import java.util.List;

@Mod.EventBusSubscriber
public class StorePlayerData
{
    static String ambientTemp = PlayerTemp.getTempTag(PlayerTemp.Types.AMBIENT);
    static String bodyTemp = PlayerTemp.getTempTag(PlayerTemp.Types.BODY);
    static String baseTemp = PlayerTemp.getTempTag(PlayerTemp.Types.BASE);

    @SubscribeEvent
    public static void onJoin(PlayerEvent.PlayerLoggedInEvent event)
    {
        PlayerEntity player = event.getPlayer();
        ColdSweat.LOGGER.debug("Loading temperature data for player " + player.getName().getString() +
                " A: " + player.getPersistentData().getDouble(ambientTemp) +
                " Bo: " + player.getPersistentData().getDouble(bodyTemp) +
                " Ba: " + player.getPersistentData().getDouble(baseTemp));

        player.getCapability(PlayerTempCapability.TEMPERATURE).ifPresent(cap ->
        {
            cap.set(PlayerTemp.Types.AMBIENT, player.getPersistentData().getDouble(ambientTemp));
            cap.set(PlayerTemp.Types.BODY, player.getPersistentData().getDouble(bodyTemp));
            cap.set(PlayerTemp.Types.BASE, player.getPersistentData().getDouble(baseTemp));

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
                        if (newModifier != null && key != null)
                        newModifier.addArgument(key, NBTHelper.getObjectFromINBT(modifierNBT.get(key)));
                    });

                    // Add the modifier to the player's temperature
                    cap.addModifier(type, newModifier);
                });
            }
        });

        if (player instanceof ServerPlayerEntity)
            PlayerHelper.updateModifiers(player);
    }

    @SubscribeEvent
    public static void onPlayerLogout(PlayerEvent.PlayerLoggedOutEvent event)
    {
        ColdSweat.LOGGER.debug("Saving temperature data for player " + event.getPlayer().getName().getString());
        PlayerEntity player = event.getPlayer();

        player.getCapability(PlayerTempCapability.TEMPERATURE).ifPresent(cap ->
        {
            // Save the player's temperature data
            player.getPersistentData().putDouble(ambientTemp, cap.get(PlayerTemp.Types.AMBIENT));
            player.getPersistentData().putDouble(bodyTemp, cap.get(PlayerTemp.Types.BODY));
            player.getPersistentData().putDouble(baseTemp, cap.get(PlayerTemp.Types.BASE));

            // Save the player's modifiers
            PlayerTemp.Types[] validTypes = {PlayerTemp.Types.AMBIENT, PlayerTemp.Types.BODY, PlayerTemp.Types.BASE, PlayerTemp.Types.RATE};
            for (PlayerTemp.Types type : validTypes)
            {
                ListNBT modifiers = new ListNBT();
                for (TempModifier modifier : cap.getModifiers(type))
                {
                    // Write the modifier's data to a CompoundNBT
                    CompoundNBT modifierNBT = new CompoundNBT();
                    modifierNBT.putString("id", modifier.getID());

                    // Add the modifier's arguments
                    modifier.getArguments().forEach((name, value) ->
                    {
                        modifierNBT.put(name, NBTHelper.getINBTFromObject(value));
                    });
                    modifiers.add(modifierNBT);
                }
                // Write the list of modifiers to the player's persistent data
                player.getPersistentData().put(PlayerTemp.getModifierTag(type), modifiers);
            }
        });
    }
}
