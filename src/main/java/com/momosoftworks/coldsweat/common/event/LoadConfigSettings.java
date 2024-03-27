package com.momosoftworks.coldsweat.common.event;

import com.mojang.datafixers.util.Either;
import com.mojang.datafixers.util.Pair;
import com.momosoftworks.coldsweat.api.insulation.Insulation;
import com.momosoftworks.coldsweat.api.registry.BlockTempRegistry;
import com.momosoftworks.coldsweat.api.temperature.block_temp.BlockTemp;
import com.momosoftworks.coldsweat.api.util.InsulationSlot;
import com.momosoftworks.coldsweat.api.util.Temperature;
import com.momosoftworks.coldsweat.config.ConfigSettings;
import com.momosoftworks.coldsweat.data.ModRegistries;
import com.momosoftworks.coldsweat.data.codec.requirement.EntityRequirement;
import com.momosoftworks.coldsweat.data.codec.requirement.NbtRequirement;
import com.momosoftworks.coldsweat.data.codec.util.AttributeModifierMap;
import com.momosoftworks.coldsweat.data.configuration.data.BiomeTempData;
import com.momosoftworks.coldsweat.data.configuration.data.BlockTempData;
import com.momosoftworks.coldsweat.data.configuration.data.DimensionTempData;
import com.momosoftworks.coldsweat.data.configuration.data.InsulatorData;
import com.momosoftworks.coldsweat.data.configuration.value.Insulator;
import com.momosoftworks.coldsweat.util.compat.CompatManager;
import com.momosoftworks.coldsweat.util.math.CSMath;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Holder;
import net.minecraft.core.Registry;
import net.minecraft.core.RegistryAccess;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.tags.TagKey;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.item.Item;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.biome.Biome;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.dimension.DimensionType;
import net.minecraft.world.level.levelgen.blockpredicates.BlockPredicate;
import net.minecraftforge.event.server.ServerStartedEvent;
import net.minecraftforge.eventbus.api.SubscribeEvent;
import net.minecraftforge.fml.common.Mod;
import oshi.util.tuples.Triplet;

import java.util.List;
import java.util.Objects;
import java.util.stream.Stream;

@Mod.EventBusSubscriber
public class LoadConfigSettings
{
    @SubscribeEvent
    public static void onServerStarted(ServerStartedEvent event)
    {
        ConfigSettings.load();

        // Load JSON data-driven insulators
        RegistryAccess registries = event.getServer().registryAccess();
        registries.registryOrThrow(ModRegistries.INSULATOR_DATA)
        .holders()
        .forEach(holder ->
        {
            InsulatorData insulatorData = holder.value();
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
                // If the item is a tag, write the insulation value for each item in the tag
                either.ifLeft(tagKey ->
                {
                    registries.registryOrThrow(Registry.ITEM_REGISTRY).getTag(tagKey).orElseThrow().stream()
                    .forEach(item ->
                    {   addItemConfig(item.value(), insulation, insulatorData.slot(), nbt, predicate, attributeModifiers);
                    });
                });
                // If the item is a single item, write the insulation value for the item
                either.ifRight(item ->
                {   addItemConfig(item, insulation, insulatorData.slot(), nbt, predicate, attributeModifiers);
                });
            }
        });

        // Load JSON data-driven block temperatures
        registries.registryOrThrow(ModRegistries.BLOCK_TEMP_DATA)
        .holders()
        .forEach(holder ->
        {
            BlockTempData blockTempData = holder.value();
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
                                            ? registries.registryOrThrow(Registry.BLOCK_REGISTRY).getTag(either.left().get()).orElseThrow().stream().map(Holder::get).toArray(Block[]::new)
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

        // Load JSON data-driven dimension temperatures
        registries.registryOrThrow(ModRegistries.BIOME_TEMP_DATA)
        .holders()
        .forEach(holder ->
        {
            BiomeTempData biomeTempData = holder.value();
            // Check if the required mods are loaded
            if (biomeTempData.requiredMods().isPresent())
            {
                List<String> requiredMods = biomeTempData.requiredMods().get();
                if (requiredMods.stream().anyMatch(mod -> !CompatManager.modLoaded(mod)))
                {   return;
                }
            }
            Registry<Biome> biomesRegistry = registries.registryOrThrow(Registry.BIOME_REGISTRY);

            for (Either<TagKey<Biome>, ResourceLocation> either : biomeTempData.biomes())
            {
                // If the dimension is a tag, add the temperature to all dimensions in the tag
                either.ifLeft(tag ->
                {
                    biomesRegistry.getTag(tag).orElseThrow()
                    .stream().map(biome -> biomesRegistry.getKey(biome.value()))
                    .forEach(location ->
                    {   addBiomeTempConfig(location, biomeTempData);
                    });
                });
                // If the dimension is a single dimension, add the temperature to the dimension
                either.ifRight(location ->
                {   addBiomeTempConfig(location, biomeTempData);
                });
            }
        });

        // Load JSON data-driven dimension temperatures
        registries.registryOrThrow(ModRegistries.DIMENSION_TEMP_DATA)
        .holders()
        .forEach(holder ->
        {
            // If the dimension is a tag, add the temperature to all biomes in the tag
            DimensionTempData dimensionTempData = holder.value();
            // Check if the required mods are loaded
            if (dimensionTempData.requiredMods().isPresent())
            {
                List<String> requiredMods = dimensionTempData.requiredMods().get();
                if (requiredMods.stream().anyMatch(mod -> !CompatManager.modLoaded(mod)))
                {   return;
                }
            }
            Registry<DimensionType> dimensionRegistry = registries.registryOrThrow(Registry.DIMENSION_TYPE_REGISTRY);
            for (Either<TagKey<DimensionType>, ResourceLocation> either : dimensionTempData.dimensions())
            {
                either.ifLeft(tag ->
                {
                    dimensionRegistry.getTag(tag).orElseThrow()
                    .stream().map(dimension -> dimensionRegistry.getKey(dimension.value()))
                    .forEach(location ->
                    {   addDimensionTempConfig(location, dimensionTempData);
                    });
                });
                // If the dimension is a single dimension, add the temperature to the dimension
                either.ifRight(location ->
                {   addDimensionTempConfig(location, dimensionTempData);
                });
            }
        });
    }

    private static void addItemConfig(Item item, Insulation insulation, InsulationSlot slot, NbtRequirement nbt, EntityRequirement predicate, AttributeModifierMap attributeModifiers)
    {
        Insulator insulator = new Insulator(insulation, slot, nbt, predicate, attributeModifiers);
        switch (slot)
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

    private static void addBiomeTempConfig(ResourceLocation biome, BiomeTempData biomeTempData)
    {
        Temperature.Units units = biomeTempData.units();
        if (biomeTempData.isOffset())
        {   ConfigSettings.BIOME_OFFSETS.get().put(biome, new Triplet<>(Temperature.convertUnits(biomeTempData.min(), units, Temperature.Units.MC, true),
                                                                        Temperature.convertUnits(biomeTempData.max(), units, Temperature.Units.MC, true),
                                                                        biomeTempData.units()));
        }
        else
        {
            ConfigSettings.BIOME_TEMPS.get().put(biome, new Triplet<>(Temperature.convertUnits(biomeTempData.min(), units, Temperature.Units.MC, true),
                                                                      Temperature.convertUnits(biomeTempData.max(), units, Temperature.Units.MC, true),
                                                                      biomeTempData.units()));
        }
    }

    private static void addDimensionTempConfig(ResourceLocation dimension, DimensionTempData dimensionTempData)
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
