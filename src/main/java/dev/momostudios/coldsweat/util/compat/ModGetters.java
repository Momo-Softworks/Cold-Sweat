package dev.momostudios.coldsweat.util.compat;

import net.minecraftforge.fml.ModList;

public class ModGetters
{
    public static boolean isBiomesOPlentyLoaded()
    {
        return ModList.get().isLoaded("biomesoplenty");
    }

    public static boolean isSereneSeasonsLoaded()
    {
        return ModList.get().isLoaded("sereneseasons");
    }

    public static boolean isCuriosLoaded()
    {
        return ModList.get().isLoaded("curios");
    }

    // werewolves
    public static boolean isWerewolvesLoaded()
    {
        return ModList.get().isLoaded("werewolves");
    }
}
