package com.momosoftworks.coldsweat.common.event;

import com.mojang.datafixers.util.Either;
import com.mojang.datafixers.util.Pair;
import com.mojang.serialization.Codec;
import com.mojang.serialization.JsonOps;
import com.momosoftworks.coldsweat.ColdSweat;
import com.momosoftworks.coldsweat.api.insulation.Insulation;
import com.momosoftworks.coldsweat.api.registry.BlockTempRegistry;
import com.momosoftworks.coldsweat.api.temperature.block_temp.BlockTemp;
import com.momosoftworks.coldsweat.api.util.Temperature;
import com.momosoftworks.coldsweat.config.ConfigSettings;
import com.momosoftworks.coldsweat.data.ModRegistries;
import com.momosoftworks.coldsweat.data.codec.requirement.EntityRequirement;
import com.momosoftworks.coldsweat.data.codec.requirement.NbtRequirement;
import com.momosoftworks.coldsweat.data.codec.util.AttributeModifierMap;
import com.momosoftworks.coldsweat.data.configuration.data.*;
import com.momosoftworks.coldsweat.data.configuration.value.InsulatingMount;
import com.momosoftworks.coldsweat.data.configuration.value.Insulator;
import com.momosoftworks.coldsweat.data.configuration.value.PredicateItem;
import com.momosoftworks.coldsweat.data.tag.ModBlockTags;
import com.momosoftworks.coldsweat.data.tag.ModDimensionTags;
import com.momosoftworks.coldsweat.data.tag.ModEffectTags;
import com.momosoftworks.coldsweat.data.tag.ModItemTags;
import com.momosoftworks.coldsweat.util.compat.CompatManager;
import com.momosoftworks.coldsweat.util.math.CSMath;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Holder;
import net.minecraft.core.Registry;
import net.minecraft.core.RegistryAccess;
import net.minecraft.core.registries.Registries;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.resources.ResourceKey;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.tags.TagKey;
import net.minecraft.util.GsonHelper;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.item.Item;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.biome.Biome;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.dimension.DimensionType;
import net.minecraft.world.level.levelgen.blockpredicates.BlockPredicate;
import net.minecraft.world.level.levelgen.structure.Structure;
import net.minecraftforge.event.server.ServerStartedEvent;
import net.minecraftforge.eventbus.api.SubscribeEvent;
import net.minecraftforge.fml.common.Mod;
import net.minecraftforge.fml.loading.FMLPaths;
import net.minecraftforge.registries.ForgeRegistries;
import oshi.util.tuples.Triplet;

import java.io.File;
import java.io.FileReader;
import java.nio.file.Path;
import java.util.*;
import java.util.stream.Collectors;
import java.util.stream.Stream;

@Mod.EventBusSubscriber
public class LoadConfigSettings
{
    @SubscribeEvent
    public static void onServerStarted(ServerStartedEvent event)
    {
        ConfigSettings.load();

        RegistryAccess registries = event.getServer().registryAccess();

        /*
         Add blocks from tags to configs
         */
        ConfigSettings.HEARTH_SPREAD_WHITELIST.get().addAll(registries.registryOrThrow(Registries.BLOCK)
                                                            .getTag(ModBlockTags.HEARTH_SPREAD_WHITELIST).orElseThrow()
                                                            .stream().map(Holder::get).toList());
        ConfigSettings.HEARTH_SPREAD_BLACKLIST.get().addAll(registries.registryOrThrow(Registries.BLOCK)
                                                            .getTag(ModBlockTags.HEARTH_SPREAD_BLACKLIST).orElseThrow()
                                                            .stream().map(Holder::get).toList());
        ConfigSettings.SLEEP_CHECK_IGNORE_BLOCKS.get().addAll(registries.registryOrThrow(Registries.BLOCK)
                                                              .getTag(ModBlockTags.IGNORE_SLEEP_CHECK).orElseThrow()
                                                              .stream().map(Holder::get).toList());
        ConfigSettings.LAMP_DIMENSIONS.get().addAll(registries.registryOrThrow(Registries.DIMENSION_TYPE)
                                                    .getTag(ModDimensionTags.SOUL_LAMP_VALID).orElseThrow()
                                                    .stream().map(holder -> holder.get()).toList());
        ConfigSettings.INSULATION_BLACKLIST.get().addAll(registries.registryOrThrow(Registries.ITEM)
                                                        .getTag(ModItemTags.NOT_INSULATABLE).orElseThrow()
                                                        .stream().map(Holder::get).toList());
        ConfigSettings.HEARTH_POTION_BLACKLIST.get().addAll(registries.registryOrThrow(Registries.MOB_EFFECT)
                                                           .getTag(ModEffectTags.HEARTH_BLACKLISTED).orElseThrow()
                                                           .stream().map(Holder::get).toList());

        /*
         Fetch JSON registries
        */
        Set<Holder<InsulatorData>> insulators = registries.registryOrThrow(ModRegistries.INSULATOR_DATA).holders().collect(Collectors.toSet());
        Set<Holder< FuelData>> fuels = registries.registryOrThrow(ModRegistries.FUEL_DATA).holders().collect(Collectors.toSet());
        Set<Holder<ItemData>> foods = registries.registryOrThrow(ModRegistries.FOOD_DATA).holders().collect(Collectors.toSet());

        Set<Holder<BlockTempData>> blockTemps = registries.registryOrThrow(ModRegistries.BLOCK_TEMP_DATA).holders().collect(Collectors.toSet());
        Set<Holder<BiomeTempData>> biomeTemps = registries.registryOrThrow(ModRegistries.BIOME_TEMP_DATA).holders().collect(Collectors.toSet());
        Set<Holder<DimensionTempData>> dimensionTemps = registries.registryOrThrow(ModRegistries.DIMENSION_TEMP_DATA).holders().collect(Collectors.toSet());
        Set<Holder<StructureTempData>> structureTemps = registries.registryOrThrow(ModRegistries.STRUCTURE_TEMP_DATA).holders().collect(Collectors.toSet());

        Set<Holder<MountData>> mounts = registries.registryOrThrow(ModRegistries.MOUNT_DATA).holders().collect(Collectors.toSet());

        /*
         Parse user-defined JSON data from the configs folder
        */
        insulators.addAll(parseConfigData(ModRegistries.INSULATOR_DATA, InsulatorData.CODEC));
        fuels.addAll(parseConfigData(ModRegistries.FUEL_DATA, FuelData.CODEC));
        foods.addAll(parseConfigData(ModRegistries.FOOD_DATA, ItemData.CODEC));

        blockTemps.addAll(parseConfigData(ModRegistries.BLOCK_TEMP_DATA, BlockTempData.CODEC));
        biomeTemps.addAll(parseConfigData(ModRegistries.BIOME_TEMP_DATA, BiomeTempData.CODEC));
        dimensionTemps.addAll(parseConfigData(ModRegistries.DIMENSION_TEMP_DATA, DimensionTempData.CODEC));
        structureTemps.addAll(parseConfigData(ModRegistries.STRUCTURE_TEMP_DATA, StructureTempData.CODEC));

        mounts.addAll(parseConfigData(ModRegistries.MOUNT_DATA, MountData.CODEC));

        /*
         Add JSON data to the config settings
         */
        // insulators
        addInsulatorConfigs(insulators, registries);
        // fuels
        addFuelConfigs(fuels, registries);
        // foods
        addFoodConfigs(foods, registries);

        // block temperatures
        addBlockTempConfigs(blockTemps, registries);
        // biome temperatures
        addBiomeTempConfigs(biomeTemps, registries);
        // dimension temperatures
        addDimensionTempConfigs(dimensionTemps, registries);
        // structure temperatures
        addStructureTempConfigs(structureTemps, registries);

        // mounts
        addMountConfigs(mounts, registries);
    }

    private static void addInsulatorConfigs(Set<Holder<InsulatorData>> insulators, RegistryAccess registries)
    {
        insulators.forEach(holder ->
        {
            InsulatorData insulatorData = holder.get();
            // Check if the required mods are loaded
            if (insulatorData.requiredMods().isPresent())
            {
                List<String> requiredMods = insulatorData.requiredMods().get();
                if (requiredMods.stream().anyMatch(mod -> !CompatManager.modLoaded(mod)))
                {   return;
                }
            }
            Insulation insulation = insulatorData.insulation();
            NbtRequirement nbt = insulatorData.nbt();
            EntityRequirement predicate = insulatorData.predicate();
            AttributeModifierMap attributeModifiers = insulatorData.attributes().orElse(new AttributeModifierMap());

            // Add listed items as insulators
            for (Either<TagKey<Item>, Item> either : insulatorData.items())
            {
                Insulator insulator = new Insulator(insulation, insulatorData.slot(), nbt, predicate, attributeModifiers);
                for (Item item : either.map(tagKey -> registries.registryOrThrow(Registries.ITEM)
                                                                .getTag(tagKey).orElseThrow()
                                                                .stream().map(Holder::get).toList(),
                                            item -> List.of(item)))
                {
                    switch (insulatorData.slot())
                    {
                        case ITEM -> ConfigSettings.INSULATION_ITEMS.get().put(item, insulator);
                        case ARMOR -> ConfigSettings.INSULATING_ARMORS.get().put(item, insulator);
                        case CURIO ->
                        {
                            if (CompatManager.isCuriosLoaded())
                            {   ConfigSettings.INSULATING_CURIOS.get().put(item, insulator);
                            }
                        }
                    }
                }
            }
        });
    }

    private static void addFuelConfigs(Set<Holder<FuelData>> fuels, RegistryAccess registries)
    {
        fuels.forEach(holder ->
        {
            FuelData fuelData = holder.get();
            // Check if the required mods are loaded
            if (fuelData.requiredMods().isPresent())
            {
                List<String> requiredMods = fuelData.requiredMods().get();
                if (requiredMods.stream().anyMatch(mod -> !CompatManager.modLoaded(mod)))
                {   return;
                }
            }
            FuelData.FuelType type = fuelData.type();
            NbtRequirement nbt = fuelData.nbt();
            double fuel = fuelData.fuel();
            PredicateItem predicateItem = new PredicateItem(fuel, nbt, EntityRequirement.NONE);
            for (Either<TagKey<Item>, Item> either : fuelData.items())
            {
                either.map(tagKey -> registries.registryOrThrow(Registries.ITEM).getTag(tagKey).orElseThrow().stream().map(Holder::get),
                           item -> List.of(item).stream())
                .forEach(item ->
                {
                    switch (type)
                    {
                        case BOILER -> ConfigSettings.BOILER_FUEL.get().put(item, predicateItem);
                        case ICEBOX -> ConfigSettings.ICEBOX_FUEL.get().put(item, predicateItem);
                        case HEARTH -> ConfigSettings.HEARTH_FUEL.get().put(item, predicateItem);
                        case SOUL_LAMP -> ConfigSettings.SOULSPRING_LAMP_FUEL.get().put(item, predicateItem);
                    }
                });
            }
        });
    }

    private static void addFoodConfigs(Set<Holder<ItemData>> foods, RegistryAccess registries)
    {
        foods.forEach(holder ->
        {
            ItemData foodData = holder.get();
            // Check if the required mods are loaded
            if (foodData.requiredMods().isPresent())
            {
                List<String> requiredMods = foodData.requiredMods().get();
                if (requiredMods.stream().anyMatch(mod -> !CompatManager.modLoaded(mod)))
                {   return;
                }
            }
            NbtRequirement nbt = foodData.nbt();
            EntityRequirement predicate = foodData.entityRequirement().orElse(null);
            double food = foodData.value();
            PredicateItem predicateItem = new PredicateItem(food, nbt, predicate);
            for (Either<TagKey<Item>, Item> either : foodData.items())
            {
                either.map(tagKey -> registries.registryOrThrow(Registries.ITEM).getTag(tagKey).orElseThrow().stream().map(Holder::get),
                           item -> List.of(item).stream())
                .forEach(item ->
                {
                    ConfigSettings.FOOD_TEMPERATURES.get().put(item, predicateItem);
                });
            }
        });
    }

    private static void addBlockTempConfigs(Set<Holder<BlockTempData>> blockTemps, RegistryAccess registries)
    {
        blockTemps.forEach(holder ->
        {
            BlockTempData blockTempData = holder.get();
            // Check if the required mods are loaded
            if (blockTempData.requiredMods().isPresent())
            {
                List<String> requiredMods = blockTempData.requiredMods().get();
                if (requiredMods.stream().anyMatch(mod -> !CompatManager.modLoaded(mod)))
                {   return;
                }
            }
            Block[] blocks = blockTempData.blocks()
                             .stream()
                             .map(either -> either.left().isPresent()
                                            ? registries.registryOrThrow(Registries.BLOCK).getTag(either.left().get()).orElseThrow().stream().map(Holder::get).toArray(Block[]::new)
                                            : new Block[] {either.right().get()}).flatMap(Stream::of).toArray(Block[]::new);
            BlockTemp blockTemp = new BlockTemp(blocks)
            {
                final double temperature = blockTempData.temperature();
                final double maxEffect = blockTempData.maxEffect();
                final boolean fade = blockTempData.fade();
                final List<BlockPredicate> conditions = blockTempData.conditions();
                final CompoundTag tag = blockTempData.tag().orElse(null);

                @Override
                public double getTemperature(Level level, LivingEntity entity, BlockState state, BlockPos pos, double distance)
                {
                    if (level instanceof ServerLevel serverLevel)
                    {
                        for (BlockPredicate condition : conditions)
                        {
                            if (!condition.test(serverLevel, pos))
                            {   return 0;
                            }
                        }
                    }
                    if (tag != null)
                    {
                        BlockEntity blockEntity = level.getBlockEntity(pos);
                        if (blockEntity != null)
                        {
                            CompoundTag blockTag = blockEntity.saveWithFullMetadata();
                            for (String key : tag.getAllKeys())
                            {
                                if (!Objects.equals(tag.get(key), blockTag.get(key)))
                                {   return 0;
                                }
                            }
                        }
                    }
                    return fade
                         ? CSMath.blend(temperature, 0, distance, 0, ConfigSettings.BLOCK_RANGE.get())
                         : temperature;
                }

                @Override
                public double maxEffect()
                {   return temperature > 0 ? maxEffect : super.maxEffect();
                }

                @Override
                public double minEffect()
                {   return temperature < 0 ? -maxEffect : super.minEffect();
                }
            };

            BlockTempRegistry.register(blockTemp);
        });
    }

    private static void addBiomeTempConfigs(Set<Holder<BiomeTempData>> biomeTemps, RegistryAccess registries)
    {
        biomeTemps.forEach(holder ->
        {
            BiomeTempData biomeTempData = holder.get();
            // Check if the required mods are loaded
            if (biomeTempData.requiredMods().isPresent())
            {
                List<String> requiredMods = biomeTempData.requiredMods().get();
                if (requiredMods.stream().anyMatch(mod -> !CompatManager.modLoaded(mod)))
                {   return;
                }
            }
            for (Either<TagKey<Biome>, ResourceLocation> either : biomeTempData.biomes())
            {
                for (ResourceLocation biome : either.map(tag -> registries.registryOrThrow(Registries.BIOME).getTag(tag).orElseThrow().stream()
                                                                          .map(biomeHolder -> biomeHolder.unwrapKey().get().location())
                                                                          .toList(),
                                              location -> List.of(location)))
                {
                    Temperature.Units units = biomeTempData.units();
                    if (biomeTempData.isOffset())
                    {   ConfigSettings.BIOME_OFFSETS.get().put(biome, new Triplet<>(Temperature.convertUnits(biomeTempData.min(), units, Temperature.Units.MC, true),
                                                                                    Temperature.convertUnits(biomeTempData.max(), units, Temperature.Units.MC, true),
                                                                                    biomeTempData.units()));
                    }
                    else
                    {   ConfigSettings.BIOME_TEMPS.get().put(biome, new Triplet<>(Temperature.convertUnits(biomeTempData.min(), units, Temperature.Units.MC, true),
                                                                                  Temperature.convertUnits(biomeTempData.max(), units, Temperature.Units.MC, true),
                                                                                  biomeTempData.units()));
                    }
                }
            }
        });
    }

    private static void addDimensionTempConfigs(Set<Holder<DimensionTempData>> dimensionTemps, RegistryAccess registries)
    {
        dimensionTemps.forEach(holder ->
        {
            DimensionTempData dimensionTempData = holder.get();
            // Check if the required mods are loaded
            if (dimensionTempData.requiredMods().isPresent())
            {
                List<String> requiredMods = dimensionTempData.requiredMods().get();
                if (requiredMods.stream().anyMatch(mod -> !CompatManager.modLoaded(mod)))
                {   return;
                }
            }
            Registry<DimensionType> dimensionRegistry = registries.registryOrThrow(Registries.DIMENSION_TYPE);
            for (Either<TagKey<DimensionType>, ResourceLocation> either : dimensionTempData.dimensions())
            {
                for (ResourceLocation dimension : either.map(tag -> dimensionRegistry.getTag(tag).orElseThrow().stream()
                                                                    .map(dimensionHolder -> dimensionHolder.unwrapKey().get().location())
                                                                    .toList(),
                                                            location -> List.of(location)))
                {
                    Temperature.Units units = dimensionTempData.units();
                    if (dimensionTempData.isOffset())
                    {   ConfigSettings.DIMENSION_OFFSETS.get().put(dimension, Pair.of(Temperature.convertUnits(dimensionTempData.temperature(), units, Temperature.Units.MC, true),
                                                                                      dimensionTempData.units()));
                    }
                    else
                    {   ConfigSettings.DIMENSION_TEMPS.get().put(dimension, Pair.of(Temperature.convertUnits(dimensionTempData.temperature(), units, Temperature.Units.MC, true),
                                                                                    dimensionTempData.units()));
                    }
                }
            }
        });
    }

    private static void addStructureTempConfigs(Set<Holder<StructureTempData>> structureTemps, RegistryAccess registries)
    {
        structureTemps.forEach(holder ->
        {
            StructureTempData structureTempData = holder.get();
            // Check if the required mods are loaded
            if (structureTempData.requiredMods().isPresent())
            {
                List<String> requiredMods = structureTempData.requiredMods().get();
                if (requiredMods.stream().anyMatch(mod -> !CompatManager.modLoaded(mod)))
                {   return;
                }
            }
            Registry<Structure> structureRegistry = registries.registryOrThrow(Registries.STRUCTURE);
            for (Either<TagKey<Structure>, ResourceLocation> either : structureTempData.structures())
            {
                for (ResourceLocation structure : either.map(tag -> structureRegistry.getTag(tag).orElseThrow().stream()
                                                                    .map(structureHolder -> structureHolder.unwrapKey().get().location())
                                                                    .toList(),
                                                            location -> List.of(location)))
                {
                    Temperature.Units units = structureTempData.units();
                    ConfigSettings.STRUCTURE_TEMPS.get().put(structure, Pair.of(Temperature.convertUnits(structureTempData.temperature(), units, Temperature.Units.MC, true),
                                                                              structureTempData.units()));
                }
            }
        });
    }

    private static void addMountConfigs(Set<Holder<MountData>> mounts, RegistryAccess registries)
    {
        mounts.forEach(holder ->
        {
            MountData mountData = holder.get();
            // Check if the required mods are loaded
            if (mountData.requiredMods().isPresent())
            {
                List<String> requiredMods = mountData.requiredMods().get();
                if (requiredMods.stream().anyMatch(mod -> !CompatManager.modLoaded(mod)))
                {   return;
                }
            }
            for (Either<TagKey<EntityType<?>>, ResourceLocation> either : mountData.entities())
            {
                for (EntityType<?> entity : either.map(tag -> registries.registryOrThrow(Registries.ENTITY_TYPE).getTag(tag).orElseThrow().stream()
                                                                    .map(Holder::get)
                                                                    .toList(),
                                                       location -> List.of(ForgeRegistries.ENTITY_TYPES.getValue(location))))
                {
                    ConfigSettings.INSULATED_ENTITIES.get().put(ForgeRegistries.ENTITY_TYPES.getKey(entity),
                                                                new InsulatingMount(entity, mountData.coldInsulation(), mountData.heatInsulation(), mountData.requirement()));
                }
            }
        });
    }

    private static <T> Set<Holder<T>> parseConfigData(ResourceKey<Registry<T>> registry, Codec<T> codec)
    {
        Set<Holder<T>> output = new HashSet<>();

        Path coldSweatDataPath = FMLPaths.CONFIGDIR.get().resolve("coldsweat/data").resolve(registry.location().getPath());
        File jsonDirectory = coldSweatDataPath.toFile();

        if (!jsonDirectory.exists())
        {   return output;
        }
        else for (File file : findFilesRecursive(jsonDirectory))
        {
            if (file.getName().endsWith(".json"))
            {
                try (FileReader reader = new FileReader(file))
                {
                    codec.parse(JsonOps.INSTANCE, GsonHelper.parse(reader))
                            .resultOrPartial(ColdSweat.LOGGER::error)
                            .ifPresent(insulator -> output.add(Holder.direct(insulator)));
                }
                catch (Exception e)
                {   ColdSweat.LOGGER.error("Failed to parse origin settings: " + e);
                }
            }
        }
        return output;
    }

    private static List<File> findFilesRecursive(File directory)
    {
        List<File> files = new ArrayList<>();
        File[] filesInDirectory = directory.listFiles();
        if (filesInDirectory == null)
        {   return files;
        }
        for (File file : filesInDirectory)
        {
            if (file.isDirectory())
            {   files.addAll(findFilesRecursive(file));
            }
            else
            {   files.add(file);
            }
        }
        return files;
    }
}
