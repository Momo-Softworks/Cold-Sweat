package dev.momostudios.coldsweat.data;

import com.electronwill.nightconfig.core.CommentedConfig;
import com.electronwill.nightconfig.core.file.CommentedFileConfig;
import com.electronwill.nightconfig.core.file.FileConfig;
import com.electronwill.nightconfig.toml.TomlParser;
import com.google.gson.JsonElement;
import com.mojang.serialization.JsonOps;
import dev.momostudios.coldsweat.ColdSweat;
import dev.momostudios.coldsweat.common.entity.ChameleonEntity;
import dev.momostudios.coldsweat.config.EntitySettingsConfig;
import dev.momostudios.coldsweat.core.init.FeatureInit;
import dev.momostudios.coldsweat.util.config.ConfigSettings;
import dev.momostudios.coldsweat.util.registries.ModEntities;
import net.minecraft.core.HolderSet;
import net.minecraft.core.Registry;
import net.minecraft.core.RegistryAccess;
import net.minecraft.data.DataGenerator;
import net.minecraft.resources.RegistryOps;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.tags.BiomeTags;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.entity.animal.goat.Goat;
import net.minecraft.world.level.biome.Biome;
import net.minecraft.world.level.biome.MobSpawnSettings;
import net.minecraft.world.level.levelgen.GenerationStep;
import net.minecraft.world.level.levelgen.placement.PlacedFeature;
import net.minecraftforge.common.data.ExistingFileHelper;
import net.minecraftforge.common.data.JsonCodecProvider;
import net.minecraftforge.common.world.BiomeModifier;
import net.minecraftforge.common.world.ForgeBiomeModifiers;
import net.minecraftforge.data.event.GatherDataEvent;
import net.minecraftforge.eventbus.api.SubscribeEvent;
import net.minecraftforge.fml.ModList;
import net.minecraftforge.fml.common.Mod;
import net.minecraftforge.fml.loading.FMLPaths;
import net.minecraftforge.registries.ForgeRegistries;

import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.HashMap;
import java.util.Map;


@Mod.EventBusSubscriber(modid = ColdSweat.MOD_ID, bus = Mod.EventBusSubscriber.Bus.MOD)
public class ColdSweatData
{
    @SubscribeEvent
    public static void gatherData(GatherDataEvent event)
    {
        DataGenerator generator = event.getGenerator();
        ExistingFileHelper helper = event.getExistingFileHelper();
        RegistryOps<JsonElement> registryOps = RegistryOps.create(JsonOps.INSTANCE, RegistryAccess.builtinCopy());

        Registry<PlacedFeature> placedFeatures = registryOps.registry(Registry.PLACED_FEATURE_REGISTRY).orElseThrow();
        Registry<Biome> biomes = registryOps.registry(Registry.BIOME_REGISTRY).orElseThrow();

        Map<ResourceLocation, BiomeModifier> modifiers = new HashMap<>();
        modifiers.put(new ResourceLocation(ColdSweat.MOD_ID, "soul_stalk"),
                      new ForgeBiomeModifiers.AddFeaturesBiomeModifier(registryOps.registry(ForgeRegistries.Keys.BIOMES).get().getOrCreateTag(BiomeTags.IS_NETHER),
                                                                       HolderSet.direct(placedFeatures.getHolderOrThrow(FeatureInit.PlacedFeatureInit.SOUL_STALK_PLACEMENT.getKey())),
                                                                       GenerationStep.Decoration.VEGETAL_DECORATION));

        // Read the "entity_settings.toml" config file
        Path configPath = Paths.get(FMLPaths.CONFIGDIR.get().toAbsolutePath().toString(), "coldsweat");
        FileConfig conf = FileConfig.of(configPath.resolve("entity_settings.toml").toFile());
        conf.load();

        // Set the config values in EntitySettingsConfig (memory) to the values of the config file
        EntitySettingsConfig.SPEC.setConfig(CommentedConfig.fake(conf));

        // Iterate through chameleon spawn biomes and create biome modifiers
        EntityType<ChameleonEntity> chameleon = ModEntities.CHAMELEON;
        ConfigSettings.CHAMELEON_BIOMES.get().forEach((biomeID, weight) ->
        {
            String modID = biomeID.getNamespace();
            // Don't generate config if corresponding mod not loaded
            if (!modID.equals("minecraft") && !ModList.get().isLoaded(modID)) return;

            Biome biome = biomes.get(biomeID);
            if (biome == null)
            {   ColdSweat.LOGGER.warn("Chameleon spawn biome {} does not exist, skipping", biomeID);
                return;
            }
            modifiers.put(new ResourceLocation(ColdSweat.MOD_ID, "chameleon_spawns_" + biomeID.getNamespace() + "_" + biomeID.getPath()),
                          ForgeBiomeModifiers.AddSpawnsBiomeModifier.singleSpawn(HolderSet.direct(biomes.getHolderOrThrow(biomes.getResourceKey(biome).get())),
                                                                                 new MobSpawnSettings.SpawnerData(chameleon, weight, 1, 1)));
        });

        JsonCodecProvider<BiomeModifier> jsonCodecProvider = JsonCodecProvider.forDatapackRegistry(generator, helper, ColdSweat.MOD_ID, registryOps, ForgeRegistries.Keys.BIOME_MODIFIERS, modifiers);
        generator.addProvider(event.includeServer(), jsonCodecProvider);
    }
}
