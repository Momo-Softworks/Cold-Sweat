package net.momostudios.coldsweat.config;

import net.minecraft.item.Items;
import net.minecraftforge.common.ForgeConfigSpec;

import java.lang.reflect.Array;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

public final class FuelItemsConfig
{
    public static final ForgeConfigSpec.Builder BUILDER = new ForgeConfigSpec.Builder();
    public static final ForgeConfigSpec SPEC;

    public static final ForgeConfigSpec.ConfigValue<List> boilerItems;
    public static final ForgeConfigSpec.ConfigValue<List> iceBoxItems;

    static
    {
        /*
          Boiler Items
         */
        BUILDER.push("Defines the items that the Boiler can use as fuel and their values");
            boilerItems = BUILDER
                .comment("Format: {item-id, fuel-amount}")
                .define("Boiler", Arrays.asList
                (
                    Arrays.asList("minecraft:coal", "37"),
                    Arrays.asList("minecraft:charcoal", "37"),
                    Arrays.asList("minecraft:coal_block", "333"),
                    Arrays.asList("minecraft:magma_block", "333"),
                    Arrays.asList("minecraft:lava_bucket", "1000")
                ));
        BUILDER.pop();
        /*
          Ice Box Items
         */
        BUILDER.push("Defines the items that the Ice Box can use as fuel and their values");
            iceBoxItems = BUILDER
                .comment("Format: {item-id, fuel-amount}")
                .define("Ice Box", Arrays.asList
                (
                    Arrays.asList("minecraft:snowball", "37"),
                    Arrays.asList("minecraft:clay", "37"),
                    Arrays.asList("minecraft:snow_block", "333"),
                    Arrays.asList("minecraft:water_bucket", "333"),
                    Arrays.asList("minecraft:ice", "333"),
                    Arrays.asList("minecraft:packed_ice", "1000")
                ));
        BUILDER.pop();

        SPEC = BUILDER.build();
    }
}
