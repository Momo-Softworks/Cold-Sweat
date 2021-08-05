package net.momostudios.coldsweat.nbt;

import net.minecraft.nbt.INBT;
import net.minecraft.nbt.ListNBT;

import javax.annotation.Nullable;
import java.util.ArrayList;
import java.util.List;

public class ListNBTHelper
{
    public static boolean doesNBTContain(ListNBT nbt, Object object)
    {
        for (INBT iterator : nbt)
        {
            if (iterator instanceof ObjectNBT && ((ObjectNBT) iterator).object.getClass().equals(object.getClass()))
            {
                return true;
            }
        }
        return false;
    }

    /**
    * Only accepts ObjectNBT types
     */
    public static <T> List<T> asList(ListNBT nbt, @Nullable Class<T> cast)
    {
        List<T> returnList = new ArrayList<T>();
        for (INBT iterator : nbt)
        {
            if (iterator != null && iterator instanceof ObjectNBT && cast.isInstance(((ObjectNBT) iterator).object))
            {
                returnList.add((T) ((ObjectNBT) iterator).object);
            }
        }
        return returnList;
    }
}
