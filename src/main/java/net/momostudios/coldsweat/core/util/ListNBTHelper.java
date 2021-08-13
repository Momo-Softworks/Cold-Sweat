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
            if (iterator instanceof ListNBT)
            {

                try
                {
                    if (Class.forName(((ListNBT) iterator).get(0).getString()).equals(object.getClass()))
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
            for (INBT inbt : nbt)
            {
                if (inbt instanceof ListNBT)
                {
                    TempModifier modifier = (TempModifier) Class.forName(((ListNBT) inbt).get(0).getString()).newInstance();
                    if (((ListNBT) inbt).size() > 1)
                    {
                        List<Object> args = new ArrayList<>();
                        int index = 0;
                        for (INBT inbt1 : ((ListNBT) inbt))
                        {
                            if (index != 0)
                                args.add(inbt1 instanceof StringNBT ? inbt1.getString() : ((NumberNBT) inbt1).getFloat());
                            index++;
                        }
                        modifier = modifier.with(args);
                    }
                    returnList.add(modifier);
                }
            }
        }
        catch (Exception e) { System.err.println("ListNBTHelper.getModifierList threw " + e); }
        return returnList;
    }
}
