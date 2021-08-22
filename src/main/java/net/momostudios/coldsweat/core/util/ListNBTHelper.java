package net.momostudios.coldsweat.core.util;

import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.nbt.*;
import net.minecraft.util.ResourceLocation;
import net.minecraftforge.registries.ForgeRegistries;
import net.minecraftforge.registries.IForgeRegistry;
import net.momostudios.coldsweat.common.temperature.modifier.TempModifier;
import net.momostudios.coldsweat.core.init.TempModifierInit;

import javax.annotation.Nullable;
import java.rmi.registry.Registry;
import java.util.ArrayList;
import java.util.Arrays;
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
                System.err.println("ListNBTHelper.createIfNull already has nbt \"" + key + "\" as a different type");
            }
        }
        else player.getPersistentData().put(key, listNBT);
        return listNBT;
    }

    public static boolean doesNBTContain(ListNBT nbt, Object object)
    {
        for (INBT iterator : nbt)
        {
            if (iterator instanceof CompoundNBT)
            {
                try
                {
                    if (Class.forName(((CompoundNBT) iterator).get("modifier_name").getString()).equals(object.getClass()))
                    {
                        return true;
                    }
                }
                catch (Exception e)
                {
                    System.err.println("ListNBTHelper threw " + e);
                }
            }
        }
        return false;
    }

    /**
     * Converts the specified ListNBT into a List of TempModifiers
     */
    public static List<TempModifier> getModifierList(ListNBT nbt)
    {
        List<TempModifier> returnList = new ArrayList<TempModifier>();
        try
        {
            for (INBT modifierInstance : nbt)
            {
                if (modifierInstance instanceof CompoundNBT)
                {
                    //Get the class of the TempModifier
                    TempModifier modifier = (TempModifier) Class.forName(((StringNBT) ((CompoundNBT) modifierInstance).get("modifier_name")).getString()).newInstance();
                    //Get the list of argument keys (including the TempModifier class)
                    Set<String> modifierArguments = ((CompoundNBT) modifierInstance).keySet();

                    //Does the TempModifier have arguments?
                    if (modifierArguments.size() > 1)
                    {
                        List<INBT> args = new ArrayList<>();

                        //Iterate through the set of argument keys
                        int iter = 0;
                        for (String modifierArgument : modifierArguments)
                        {
                            //Gets the actual value of the argument from the key
                            if (iter > 0)
                            {
                                args.add(((CompoundNBT) modifierInstance).get(modifierArgument));
                            }
                            iter++;
                        }
                        //Apply the arguments (if any)
                        modifier = modifier.with(args);
                    }
                    //Add the TempModifier with all its arguments to the return list of TempModifiers
                    returnList.add(modifier);
                }
            }
        }
        catch (Exception e) { System.err.println("ListNBTHelper.getModifierList threw " + e); }
        return returnList;
    }
}
