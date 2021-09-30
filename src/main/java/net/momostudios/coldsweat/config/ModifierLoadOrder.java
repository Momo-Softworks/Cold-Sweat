package net.momostudios.coldsweat.config;

import com.electronwill.nightconfig.core.file.CommentedFileConfig;
import com.electronwill.nightconfig.core.io.WritingMode;
import net.minecraftforge.common.ForgeConfigSpec;
import org.apache.commons.lang3.tuple.Pair;

import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.Arrays;
import java.util.List;

/**
 * Might be used in the future
 */
public final class ModifierLoadOrder
{
    private static final ModifierLoadOrder INSTANCE;
    private static final ForgeConfigSpec SPEC;
    private static final Path CONFIG_PATH = Paths.get("config/cold-sweat_modifier_load_order.toml");

    public List<List> ambientModifierOrder = Arrays.asList
            (
                    Arrays.asList("cold_sweat:biome_temperature", 0),
                    Arrays.asList("cold_sweat:time", 1),
                    Arrays.asList("minecraft:weather", 2),
                    Arrays.asList("minecraft:nearby_blocks", 3),
                    Arrays.asList("minecraft:depth", 4),
                    Arrays.asList("minecraft:hearth_insulation", 5)
            );
    public List<List> bodyModifierOrder = Arrays.asList
            (
                    Arrays.asList("cold_sweat:waterskin", 0),
                    Arrays.asList("cold_sweat:insulated_armor", 1),
                    Arrays.asList("minecraft:minecart", 2)
            );

    static
    {
        Pair<ModifierLoadOrder, ForgeConfigSpec> specPair = new ForgeConfigSpec.Builder().configure(ModifierLoadOrder::new);
        INSTANCE = specPair.getLeft();
        SPEC = specPair.getRight();
        CommentedFileConfig config = CommentedFileConfig.builder(CONFIG_PATH)
                .sync()
                .autoreload()
                .writingMode(WritingMode.REPLACE)
                .build();
        config.load();
        config.save();
        SPEC.setConfig(config);
    }

    public final ForgeConfigSpec.ConfigValue<List<List>> ambient;
    public final ForgeConfigSpec.ConfigValue<List<List>> body;


    private ModifierLoadOrder(ForgeConfigSpec.Builder configSpecBuilder)
    {
        /*
          Boiler Items
         */
        configSpecBuilder.push("Defines the order in which temperature modifiers are applied");
        ambient = configSpecBuilder
                .comment("Format: [[modifier-id-1, order-1], [modifier-id-2, order-2], ...etc]",
                        "Only change this if you know what you are doing!")
                .define("Ambient", ambientModifierOrder);
        configSpecBuilder.pop();
        configSpecBuilder.push("Defines the order in which temperature modifiers are applied");
        body = configSpecBuilder
                .comment("Format: [[modifier-id-1, order-1], [modifier-id-2, order-2], ...etc]",
                        "Only change this if you know what you are doing!")
                .define("Body", bodyModifierOrder);
        configSpecBuilder.pop();
    }

    public static ModifierLoadOrder getInstance()
    {
        return INSTANCE;
    }

    public List<List> ambient()
    {
        return ambient.get();
    }
    public List<List> body()
    {
        return body.get();
    }
}
