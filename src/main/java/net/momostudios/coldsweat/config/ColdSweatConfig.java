package net.momostudios.coldsweat.config;

import com.electronwill.nightconfig.core.file.CommentedFileConfig;
import com.electronwill.nightconfig.core.io.WritingMode;
import net.minecraftforge.common.ForgeConfigSpec;
import net.momostudios.coldsweat.ColdSweat;

import java.io.File;

public final class ColdSweatConfig
{
    public static final ForgeConfigSpec.Builder BUILDER = new ForgeConfigSpec.Builder();
    public static final ForgeConfigSpec SPEC;

    public static final ForgeConfigSpec.ConfigValue<Boolean> celsius;
    public static final ForgeConfigSpec.ConfigValue<Integer> tempOffset;

    public static final ForgeConfigSpec.ConfigValue<Boolean> fireResistanceEffect;
    public static final ForgeConfigSpec.ConfigValue<Boolean> iceResistanceEffect;

    public static final ForgeConfigSpec.ConfigValue<Integer> steveHeadX;
    public static final ForgeConfigSpec.ConfigValue<Integer> steveHeadY;

    public static final ForgeConfigSpec.ConfigValue<Integer> tempGaugeX;
    public static final ForgeConfigSpec.ConfigValue<Integer> tempGaugeY;

    public static final ForgeConfigSpec.ConfigValue<Boolean> animalsTemperature;

    static
    {
        //BUILDER.comment("Configuration file for Cold Sweat");
        //BUILDER.comment("Changes you make in the in-game config menu will be updated in this file and vice-versa");

        /*
          Temperature display preferences
         */
        BUILDER.push("Temperature display preferences");
            celsius = BUILDER
                    .comment("Sets all temperatures to be displayed in Celsius")
                    .comment("(default: false)")
                    .define("Celsius", false);

            tempOffset = BUILDER
                    .comment("Offsets all temperatures by an amount if the default doesn't make sense")
                    .comment("(default: 0, so a Plains biome is 75 \u00b0F or 21 \u00b0C)")
                    .define("Temperature Offset", 0);
        BUILDER.pop();

        /*
          Potion effects affecting the player's temperature
         */
        BUILDER.push("Potion effects affecting the player's temperature");
            fireResistanceEffect = BUILDER
                    .comment("Fire Resistance blocks all hot temperatures")
                    .comment("(default: true)")
                    .define("Fire Resistance Immunity", true);
            iceResistanceEffect = BUILDER
                .comment("Ice Resistance blocks all cold temperatures")
                .comment("(default: true)")
                .define("Fire Resistance Immunity", true);
        BUILDER.pop();

        /*
          Position of the "Steve Head" temperature gauge above the hotbar
         */
        BUILDER.push("Position of the 'Steve Head' temperature gauge above the hotbar");
            steveHeadX = BUILDER
                    .comment("The x position of the gauge relative to its normal position")
                    .comment("(default: 0)")
                    .define("Steve Head X Offset", 0);
            steveHeadY = BUILDER
                    .comment("The y position of the gauge relative to its normal position")
                    .comment("(default: 0)")
                    .define("Steve Head Y Offset", 0);
        BUILDER.pop();


        BUILDER.push("Position of the actual number temperature gauge above the hotbar");
            tempGaugeX = BUILDER
                    .comment("The x position of the temperature gauge relative to default")
                    .comment("(default: 0)")
                    .define("Temp Gauge X Offset", 0);
            tempGaugeY = BUILDER
                    .comment("The y position of the temperature gauge relative to default")
                    .comment("(default: 0)")
                    .define("Temp Gauge Y Offset", 0);
        BUILDER.pop();

        /*
          Misc. things that are affected by temperature
         */
        BUILDER.push("Misc things that are affected by temperature");
            animalsTemperature = BUILDER
                    .comment("Sets whether animals are affected by temperature")
                    .comment("(default: true)")
                    .define("Animals Have Temperature", true);
        BUILDER.pop();

        SPEC = BUILDER.build();
    }
}
