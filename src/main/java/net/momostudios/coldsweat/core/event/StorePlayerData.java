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
import net.momostudios.coldsweat.core.capabilities.PlayerTempCapability;
import net.momostudios.coldsweat.util.NBTHelper;
import net.momostudios.coldsweat.util.PlayerHelper;
import net.momostudios.coldsweat.util.PlayerTemp;

@Mod.EventBusSubscriber
public class StorePlayerData
{
    static String ambientTemp = PlayerHelper.getTempTag(PlayerHelper.Types.AMBIENT);
    static String bodyTemp = PlayerHelper.getTempTag(PlayerHelper.Types.BODY);
    static String baseTemp = PlayerHelper.getTempTag(PlayerHelper.Types.BASE);

    @SubscribeEvent
    public static void onJoin(PlayerEvent.PlayerLoggedInEvent event)
    {
        PlayerEntity player = event.getPlayer();
        ColdSweat.LOGGER.debug("Loading temperature data for player " + player.getName().getString() +
                " Ambient: " + player.getPersistentData().getDouble(ambientTemp) +
                " Body: " + player.getPersistentData().getDouble(bodyTemp) +
                " Base: " + player.getPersistentData().getDouble(baseTemp));

        player.getCapability(PlayerTempCapability.TEMPERATURE).ifPresent(cap ->
        {
            cap.set(PlayerHelper.Types.AMBIENT, player.getPersistentData().getDouble(ambientTemp));
            cap.set(PlayerHelper.Types.BODY, player.getPersistentData().getDouble(bodyTemp));
            cap.set(PlayerHelper.Types.BASE, player.getPersistentData().getDouble(baseTemp));

            // Load the player's modifiers
            PlayerHelper.Types[] validTypes = {PlayerHelper.Types.AMBIENT, PlayerHelper.Types.BODY, PlayerHelper.Types.BASE, PlayerHelper.Types.RATE};
            for (PlayerHelper.Types type : validTypes)
            {
                // Get the list of modifiers from the player's persistent data
                ListNBT modifiers = player.getPersistentData().getList(PlayerHelper.getModifierTag(type), 10);

                // For each modifier in the list
                modifiers.forEach(modifier ->
                {
                    CompoundNBT modifierNBT = (CompoundNBT) modifier;

                    // Add the modifier to the player's temperature
                    cap.getModifiers(type).add(NBTHelper.NBTToModifier(modifierNBT));
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
            player.getPersistentData().putDouble(ambientTemp, cap.get(PlayerHelper.Types.AMBIENT));
            player.getPersistentData().putDouble(bodyTemp, cap.get(PlayerHelper.Types.BODY));
            player.getPersistentData().putDouble(baseTemp, cap.get(PlayerHelper.Types.BASE));

            // Save the player's modifiers
            PlayerHelper.Types[] validTypes = {PlayerHelper.Types.AMBIENT, PlayerHelper.Types.BODY, PlayerHelper.Types.BASE, PlayerHelper.Types.RATE};
            for (PlayerHelper.Types type : validTypes)
            {
                ListNBT modifiers = new ListNBT();
                for (TempModifier modifier : cap.getModifiers(type))
                {
                    modifiers.add(NBTHelper.modifierToNBT(modifier));
                }

                // Write the list of modifiers to the player's persistent data
                player.getPersistentData().put(PlayerHelper.getModifierTag(type), modifiers);
            }
        });
    }
}
