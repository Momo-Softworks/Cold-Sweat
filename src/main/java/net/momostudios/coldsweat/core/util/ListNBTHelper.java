package net.momostudios.coldsweat.core.util;

import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.nbt.INBT;
import net.minecraft.nbt.ListNBT;
import net.minecraft.nbt.StringNBT;

import javax.annotation.Nullable;
import java.util.ArrayList;
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
                System.err.println("ListNBTHelper.createIfNull threw " + e);
            }
        }
        else player.getPersistentData().put(key, listNBT);
        return listNBT;
    }

    public static boolean doesNBTContain(ListNBT nbt, Object object)
    {
        for (INBT iterator : nbt)
        {
            try
            {
                if (iterator instanceof StringNBT && Class.forName(iterator.getString()).equals(object.getClass()))
                {
                    return true;
                }
            } catch (Exception e) { System.err.println("ListNBTHelper threw " + e); }
        }
        return false;
    }

    /**
     * Converts the specified ListNBT into a List
     * @param cast attempts to cast every object in the new List to the specified class
     * Only accepts ObjectNBT types
     */
    public static <T> List<T> asList(ListNBT nbt, @Nullable Class<T> cast)
    {
        List<T> returnList = new ArrayList<T>();
        try
        {
            for (INBT iterator : nbt)
            {
                if (iterator instanceof StringNBT)
                {
                    Class clazz = Class.forName(iterator.getString());
                    Object instance = clazz.newInstance();

                    if (cast == null || cast.isInstance(instance))
                    {
                        returnList.add((T) instance);
                    }
                }
            }
        }
        catch (Exception e) { System.err.println("ListNBTHelper.asList threw " + e); }
        return returnList;
    }
}
