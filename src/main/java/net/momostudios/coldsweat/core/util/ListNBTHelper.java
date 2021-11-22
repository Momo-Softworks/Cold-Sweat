package net.momostudios.coldsweat.core.util;

import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.nbt.CompoundNBT;
import net.minecraft.nbt.INBT;
import net.minecraft.nbt.ListNBT;
import net.momostudios.coldsweat.common.temperature.modifier.TempModifier;
import net.momostudios.coldsweat.common.world.TempModifierEntries;

import java.util.ArrayList;
import java.util.List;
import java.util.Set;

public class ListNBTHelper
{
    public static ListNBT createIfNull(String key, PlayerEntity player)
    {
        ListNBT listNBT = new ListNBT();
        if (player.getPersistentData().get(key) != null)
        {
            try
            {
                listNBT = (ListNBT) player.getPersistentData().get(key);
            }
            catch(ClassCastException e)
            {
                throw new UnsupportedOperationException("ListNBTHelper.createIfNull already has nbt \"" + key + "\" as a different type");
            }
        }
        else player.getPersistentData().put(key, listNBT);
        return listNBT;
    }

    /**
     * Returns true if the player has this {@link TempModifier} stored in their NBT data
     * @param nbt should be a {@link PlayerTemp.Types}
     */
    public static boolean doesNBTContain(ListNBT nbt, TempModifier modifier)
    {
        for (INBT iterator : nbt)
        {
            if (iterator instanceof CompoundNBT)
            {
                if (((CompoundNBT) iterator).get("modifier_name").getString().equals(TempModifierEntries.getEntries().getEntryName(modifier)))
                    return true;
            }
        }
        return false;
    }

    /**
     * Returns the list of {@link TempModifier}s of the specified type
     * @param nbt should be a {@link PlayerTemp.Types}
     */
    public static List<TempModifier> getModifierList(ListNBT nbt)
    {
        List<TempModifier> returnList = new ArrayList<TempModifier>();
        for (INBT modifierInstance : nbt)
        {
            if (modifierInstance instanceof CompoundNBT)
            {
                //Get the class of the TempModifier
                TempModifier modifier = TempModifierEntries.getEntries().getEntryFor(((CompoundNBT) modifierInstance).getString("modifier_name"));
                //Get the list of argument keys (including the TempModifier class)
                Set<String> modifierArguments = ((CompoundNBT) modifierInstance).keySet();

                //Does the TempModifier have arguments?
                if (modifierArguments.size() > 1)
                {
                    List<INBT> args = new ArrayList<>();

                    //Iterate through the set of argument keys
                    for (String modifierArgument : modifierArguments)
                    {
                        //Gets the actual value of the argument from the key
                        if (!modifierArgument.equals("modifier_name"))
                        {
                            args.add(((CompoundNBT) modifierInstance).get(modifierArgument));
                        }
                    }
                    //Apply the arguments (if any)
                    modifier = modifier.with(args);
                }
                //Add the TempModifier with all its arguments to the return list of TempModifiers
                returnList.add(modifier);
            }
        }
        return returnList;
    }
}
