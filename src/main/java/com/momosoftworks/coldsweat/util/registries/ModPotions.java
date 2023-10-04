package com.momosoftworks.coldsweat.util.registries;

import com.momosoftworks.coldsweat.ColdSweat;
import com.momosoftworks.coldsweat.common.potions.GracePotion;
import net.minecraft.potion.Potion;

import java.lang.reflect.Field;
import java.lang.reflect.Modifier;

public class ModPotions
{
    public static Potion INSULATED;
    public static Potion GRACE = registerPotion(GracePotion.class, false, 14208625);
    public static Potion ICE_RESISTANCE;

    public static Potion registerPotion(Class<? extends Potion> potionClass, boolean isHarmful, int potionColor)
    {
        Potion[] potionTypes;
        if (Potion.potionTypes.length < 256)
        {
            for (Field f : Potion.class.getDeclaredFields())
            {
                f.setAccessible(true);
                try
                {
                    if (f.get(null).equals(Potion.potionTypes))
                    {   Field modfield = Field.class.getDeclaredField("modifiers");
                        modfield.setAccessible(true);
                        modfield.setInt(f, f.getModifiers() & ~Modifier.FINAL);
                        potionTypes = (Potion[]) f.get(null);
                        final Potion[] newPotionTypes = new Potion[256];
                        System.arraycopy(potionTypes, 0, newPotionTypes, 0, potionTypes.length);
                        f.set(null, newPotionTypes);
                    }
                }
                catch (Exception e)
                {   ColdSweat.LOGGER.error("Error expanding potion array!", e);
                }
            }
        }

        Potion potion;
        // Get the number of elements in the potion array
        int nextPotionID = 0;
        // Array#length can't be used because that gives the capacity, not number of elements
        for (Potion p : Potion.potionTypes)
        {   nextPotionID++;
        }
        try
        {   potion = potionClass.getConstructor(int.class, boolean.class, int.class).newInstance(nextPotionID, isHarmful, potionColor);
        }
        catch (Exception e)
        {   throw new RuntimeException("Failed to instantiate potion " + potionClass.getName() + "!", e);
        }
        Potion.potionTypes[nextPotionID] = potion;

        return potion;
    }
}
