package dev.momostudios.coldsweat.data;

import com.google.gson.JsonElement;
import com.mojang.serialization.JsonOps;
import dev.momostudios.coldsweat.ColdSweat;
import dev.momostudios.coldsweat.common.world.feature.ModFeatures;
import net.minecraft.core.HolderSet;
import net.minecraft.core.RegistryAccess;
import net.minecraft.data.DataGenerator;
import net.minecraft.resources.RegistryOps;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.tags.BiomeTags;
import net.minecraft.world.level.levelgen.GenerationStep;
import net.minecraftforge.common.data.ExistingFileHelper;
import net.minecraftforge.common.data.JsonCodecProvider;
import net.minecraftforge.common.world.BiomeModifier;
import net.minecraftforge.common.world.ForgeBiomeModifiers;
import net.minecraftforge.data.event.GatherDataEvent;
import net.minecraftforge.eventbus.api.SubscribeEvent;
import net.minecraftforge.fml.common.Mod;
import net.minecraftforge.registries.ForgeRegistries;

import java.util.Map;


@Mod.EventBusSubscriber(modid = ColdSweat.MOD_ID, bus = Mod.EventBusSubscriber.Bus.MOD)
public class ColdSweatData {
    @SubscribeEvent
    public static void gatherData(GatherDataEvent event) {
        DataGenerator generator = event.getGenerator();
        ExistingFileHelper helper = event.getExistingFileHelper();

        RegistryOps<JsonElement> registryOps = RegistryOps.create(JsonOps.INSTANCE, RegistryAccess.builtinCopy());

        JsonCodecProvider<BiomeModifier> jsonCodecProvider = JsonCodecProvider.forDatapackRegistry(generator, helper, ColdSweat.MOD_ID, registryOps, ForgeRegistries.Keys.BIOME_MODIFIERS,
                Map.of(
                        new ResourceLocation(ColdSweat.MOD_ID, "add_soul_stalk"), new ForgeBiomeModifiers.AddFeaturesBiomeModifier(registryOps.registry(ForgeRegistries.Keys.BIOMES).get().getOrCreateTag(BiomeTags.IS_NETHER), HolderSet.direct(ModFeatures.SOUL_STALK_PLACEMENT), GenerationStep.Decoration.VEGETAL_DECORATION)
//                        new ResourceLocation(ColdSweat.MOD_ID, "add_spawms", new ForgeBiomeModifiers.AddSpawnsBiomeModifier())
                )
        );
        generator.addProvider(event.includeServer(), jsonCodecProvider);
    }
}
