package com.momosoftworks.coldsweat.util.serialization;

import com.mojang.datafixers.util.Either;
import com.mojang.datafixers.util.Pair;
import com.mojang.serialization.Codec;
import com.momosoftworks.coldsweat.ColdSweat;
import com.momosoftworks.coldsweat.api.insulation.AdaptiveInsulation;
import com.momosoftworks.coldsweat.api.insulation.Insulation;
import com.momosoftworks.coldsweat.api.insulation.StaticInsulation;
import com.momosoftworks.coldsweat.api.util.Temperature;
import com.momosoftworks.coldsweat.config.type.Insulator;
import com.momosoftworks.coldsweat.data.codec.requirement.EntityRequirement;
import com.momosoftworks.coldsweat.data.codec.requirement.ItemRequirement;
import com.momosoftworks.coldsweat.data.codec.requirement.NbtRequirement;
import com.momosoftworks.coldsweat.data.codec.util.AttributeModifierMap;
import com.momosoftworks.coldsweat.util.exceptions.ArgumentCountException;
import com.momosoftworks.coldsweat.util.math.CSMath;
import com.momosoftworks.coldsweat.util.world.WorldHelper;
import net.minecraft.core.Registry;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.resources.ResourceKey;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.tags.TagKey;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.item.Item;
import net.minecraft.world.level.biome.Biome;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.block.state.properties.Property;
import net.minecraft.world.level.dimension.DimensionType;
import net.minecraft.world.level.levelgen.feature.ConfiguredStructureFeature;
import net.minecraft.world.level.levelgen.feature.StructureFeature;
import net.minecraftforge.registries.ForgeRegistries;
import net.minecraftforge.registries.IForgeRegistry;
import net.minecraftforge.registries.IForgeRegistryEntry;
import net.minecraftforge.registries.tags.ITag;
import oshi.util.tuples.Triplet;

import javax.annotation.Nullable;
import java.util.*;
import java.util.function.BiFunction;
import java.util.function.Consumer;
import java.util.function.Function;
import java.util.function.Predicate;

public class ConfigHelper
{
    private ConfigHelper() {}

    public static List<Block> getBlocks(String... ids)
    {
        List<Block> blocks = new ArrayList<>();
        for (String id : ids)
        {
            if (id.startsWith("#"))
            {
                final String tagID = id.replace("#", "");
                CSMath.doIfNotNull(ForgeRegistries.BLOCKS.tags(), tags ->
                {   Optional<ITag<Block>> optionalTag = tags.stream().filter(tag -> tag.getKey() != null && tag.getKey().location().toString().equals(tagID)).findFirst();
                    optionalTag.ifPresent(blockITag -> blocks.addAll(blockITag.stream().toList()));
                });
            }
            else
            {
                ResourceLocation blockId = new ResourceLocation(id);
                if (ForgeRegistries.BLOCKS.containsKey(blockId))
                {   blocks.add(ForgeRegistries.BLOCKS.getValue(blockId));
                }
                else
                {   ColdSweat.LOGGER.error("Error parsing block config: block \"{}\" does not exist", id);
                }
            }
        }
        return blocks;
    }

    public static Map<Block, Number> getBlocksWithValues(List<? extends List<?>> source)
    {
        Map<Block, Number> map = new HashMap<>();
        for (List<?> entry : source)
        {
            String id = (String) entry.get(0);

            if (id.startsWith("#"))
            {
                final String tagID = id.replace("#", "");
                CSMath.doIfNotNull(ForgeRegistries.BLOCKS.tags(), tags ->
                {
                    Optional<ITag<Block>> optionalTag = tags.stream().filter(tag -> tag.getKey() != null && tag.getKey().location().toString().equals(tagID)).findFirst();

                    optionalTag.ifPresent(itemITag ->
                    {   for (Block block : optionalTag.get().stream().toList())
                        {   map.put(block, (Number) entry.get(1));
                        }
                    });
                });
            }
            else
            {   Block newBlock = ForgeRegistries.BLOCKS.getValue(new ResourceLocation(id));
                if (newBlock != null) map.put(newBlock, (Number) entry.get(1));
            }
        }
        return map;
    }

    public static List<Item> getItems(String... ids)
    {
        List<Item> items = new ArrayList<>();
        for (String itemId : ids)
        {
            if (itemId.startsWith("#"))
            {
                final String tagID = itemId.replace("#", "");
                CSMath.doIfNotNull(ForgeRegistries.ITEMS.tags(), tags ->
                {   Optional<ITag<Item>> optionalTag = tags.stream().filter(tag -> tag.getKey() != null && tag.getKey().location().toString().equals(tagID)).findFirst();
                    optionalTag.ifPresent(itemITag -> items.addAll(itemITag.stream().toList()));
                });
            }
            else
            {
                ResourceLocation itemID = new ResourceLocation(itemId);
                if (ForgeRegistries.ITEMS.containsKey(itemID))
                {   items.add(ForgeRegistries.ITEMS.getValue(itemID));
                }
                else
                {   ColdSweat.LOGGER.error("Error parsing item config: item \"{}\" does not exist", itemId);
                }
            }
        }
        return items;
    }

    public static Map<Biome, Triplet<Double, Double, Temperature.Units>> getBiomesWithValues(List<? extends List<?>> source, boolean absolute)
    {
        Map<Biome, Triplet<Double, Double, Temperature.Units>> map = new HashMap<>();
        for (List<?> entry : source)
        {
            try
            {
                ResourceLocation biomeId = new ResourceLocation((String) entry.get(0));
                Biome biome = WorldHelper.getBiome(biomeId);
                if (biome == null)
                {   ColdSweat.LOGGER.error("Error parsing biome config: biome \"{}\" does not exist or is not loaded yet", biomeId);
                    continue;
                }

                double min;
                double max;
                Temperature.Units units;
                // The config defines a min and max value, with optional unit conversion
                if (entry.size() > 2)
                {   units = entry.size() == 4 ? Temperature.Units.valueOf(((String) entry.get(3)).toUpperCase()) : Temperature.Units.MC;
                    min = Temperature.convert(((Number) entry.get(1)).doubleValue(), units, Temperature.Units.MC, absolute);
                    max = Temperature.convert(((Number) entry.get(2)).doubleValue(), units, Temperature.Units.MC, absolute);
                }
                // The config only defines a mid-temperature
                else
                {   double mid = ((Number) entry.get(1)).doubleValue();
                    double variance = 1 / Math.max(1, 2 + biome.getDownfall() * 2);
                    min = mid - variance;
                    max = mid + variance;
                    units = Temperature.Units.MC;
                }

                // Maps the biome ID to the temperature (and variance if present)
                map.put(biome, new Triplet<>(min, max, units));
            }
            catch (Exception e)
            {
                ColdSweat.LOGGER.error("Error parsing biome config: biome \"{}\" does not exist or is not loaded yet.", entry.get(0));
            }
        }
        return map;
    }

    public static Map<DimensionType, Pair<Double, Temperature.Units>> getDimensionsWithValues(List<? extends List<?>> source, boolean absolute)
    {
        Map<DimensionType, Pair<Double, Temperature.Units>> map = new HashMap<>();
        for (List<?> entry : source)
        {
            try
            {
                ResourceLocation dimensionId = new ResourceLocation((String) entry.get(0));
                double temp = ((Number) entry.get(1)).doubleValue();
                Temperature.Units units = entry.size() == 3 ? Temperature.Units.valueOf(((String) entry.get(2)).toUpperCase()) : Temperature.Units.MC;
                DimensionType dimension = WorldHelper.getRegistry(Registry.DIMENSION_TYPE_REGISTRY).get(dimensionId);
                if (dimension != null)
                {   map.put(dimension, Pair.of(Temperature.convert(temp, units, Temperature.Units.MC, absolute), units));
                }
                else
                {   ColdSweat.LOGGER.error("Error parsing dimension config: dimension \"{}\" does not exist", dimensionId);
                }
            }
            catch (Exception e)
            {
                ColdSweat.LOGGER.error("Error parsing dimension config: dimension \"{}\" does not exist or is not loaded yet", entry.get(0));
            }
        }
        return map;
    }

    public static Map<StructureFeature<?>, Pair<Double, Temperature.Units>> getStructuresWithValues(List<? extends List<?>> source, boolean absolute)
    {
        Map<StructureFeature<?>, Pair<Double, Temperature.Units>> map = new HashMap<>();
        for (List<?> entry : source)
        {
            try
            {
                ResourceLocation structureId = new ResourceLocation((String) entry.get(0));
                double temp = ((Number) entry.get(1)).doubleValue();
                Temperature.Units units = entry.size() == 3 ? Temperature.Units.valueOf(((String) entry.get(2)).toUpperCase()) : Temperature.Units.MC;
                map.put(WorldHelper.getRegistry(Registry.STRUCTURE_FEATURE_REGISTRY).get(structureId), Pair.of(Temperature.convert(temp, units, Temperature.Units.MC, absolute), units));
            }
            catch (Exception e)
            {
                ColdSweat.LOGGER.error("Error parsing structure config: dimension \"{}\" does not exist or is not loaded yet", entry.toString());
            }
        }
        return map;
    }

    public static Map<String, Predicate<BlockState>> getBlockStatePredicates(Block block, String predicates)
    {
        Map<String, Predicate<BlockState>> blockPredicates = new HashMap<>();
        // Separate comma-delineated predicates
        String[] predicateList = predicates.split(",");

        // Iterate predicates
        for (String predicate : predicateList)
        {
            // Split predicate into key-value pairs separated by "="
            String[] pair = predicate.split("=");
            String key = pair[0];
            String value = pair[1];

            // Get the property with the given name
            Property<?> property = block.getStateDefinition().getProperty(key);
            if (property != null)
            {
                // Parse the desired value for this property
                property.getValue(value).ifPresent(propertyValue ->
                {
                    // Add a new predicate to the list
                    blockPredicates.put(predicate, state ->
                    {   // If the value matches, this predicate returns true
                        return state.getValue(property).equals(propertyValue);
                    });
                });
            }
        }
        return blockPredicates;
    }

    public static List<Biome> getBiomes(String... biomes)
    {
        List<Biome> biomeList = new ArrayList<>();
        for (String biome : biomes)
        {
            if (biome.startsWith("#"))
            {
                final String tagID = biome.replace("#", "");
                CSMath.doIfNotNull(ForgeRegistries.BIOMES.tags(), tags ->
                {
                    Optional<ITag<Biome>> optionalTag = tags.stream().filter(tag -> tag.getKey() != null && tag.getKey().location().toString().equals(tagID)).findFirst();
                    optionalTag.ifPresent(biomeITag -> biomeList.addAll(biomeITag.stream().toList()));
                });
            }
            else
            {
                ResourceLocation biomeId = new ResourceLocation(biome);
                Biome biomeObj = getBiome(biomeId);
                if (biomeObj == null)
                {
                    ColdSweat.LOGGER.error("Error parsing biome config: biome \"{}\" does not exist", biome);
                    continue;
                }
                biomeList.add(biomeObj);
            }
        }
        return biomeList;
    }

    public static List<EntityType<?>> getEntityTypes(String... entities)
    {
        List<EntityType<?>> entityList = new ArrayList<>();
        for (String entity : entities)
        {
            if (entity.startsWith("#"))
            {
                final String tagID = entity.replace("#", "");
                CSMath.doIfNotNull(ForgeRegistries.ENTITIES.tags(), tags ->
                {
                    Optional<ITag<EntityType<?>>> optionalTag = tags.stream().filter(tag -> tag.getKey() != null && tag.getKey().location().toString().equals(tagID)).findFirst();
                    optionalTag.ifPresent(entityITag -> entityList.addAll(entityITag.stream().toList()));
                });
            }
            else
            {
                ResourceLocation entityId = new ResourceLocation(entity);
                if (ForgeRegistries.ENTITIES.containsKey(entityId))
                {   entityList.add(ForgeRegistries.ENTITIES.getValue(entityId));
                }
                else
                {   ColdSweat.LOGGER.error("Error parsing entity config: entity \"{}\" does not exist", entity);
                }
            }
        }
        return entityList;
    }

    public static CompoundTag serializeNbtBool(boolean value, String key)
    {
        CompoundTag tag = new CompoundTag();
        tag.putBoolean(key, value);
        return tag;
    }

    public static CompoundTag serializeNbtInt(int value, String key)
    {
        CompoundTag tag = new CompoundTag();
        tag.putInt(key, value);
        return tag;
    }

    public static CompoundTag serializeNbtDouble(double value, String key)
    {
        CompoundTag tag = new CompoundTag();
        tag.putDouble(key, value);
        return tag;
    }

    public static CompoundTag serializeNbtString(String value, String key)
    {
        CompoundTag tag = new CompoundTag();
        tag.putString(key, value);
        return tag;
    }

    public static CompoundTag serializeBiomeTemps(Map<Biome, Triplet<Double, Double, Temperature.Units>> map, String key)
    {
        CompoundTag tag = new CompoundTag();
        CompoundTag mapTag = new CompoundTag();
        for (Map.Entry<Biome, Triplet<Double, Double, Temperature.Units>> entry : map.entrySet())
        {
            CompoundTag biomeTag = new CompoundTag();
            ResourceLocation biomeId = getBiomeId(entry.getKey());
            if (biomeId == null)
            {   ColdSweat.LOGGER.error("Error serializing biome temperatures: biome \"{}\" does not exist", entry.getKey());
                continue;
            }
            biomeTag.putDouble("Min", entry.getValue().getA());
            biomeTag.putDouble("Max", entry.getValue().getB());
            biomeTag.putString("Units", entry.getValue().getC().toString());
            mapTag.put(biomeId.toString(), biomeTag);
        }
        tag.put(key, mapTag);
        return tag;
    }

    public static Map<Biome, Triplet<Double, Double, Temperature.Units>> deserializeBiomeTemps(CompoundTag tag, String key)
    {
        Map<Biome, Triplet<Double, Double, Temperature.Units>> map = new HashMap<>();
        CompoundTag mapTag = tag.getCompound(key);
        for (String biomeID : mapTag.getAllKeys())
        {
            CompoundTag biomeTag = mapTag.getCompound(biomeID);
            Biome biome = getBiome(new ResourceLocation(biomeID));
            if (biome == null)
            {   ColdSweat.LOGGER.error("Error deserializing biome temperatures: biome \"{}\" does not exist", biomeID);
                continue;
            }
            map.put(biome, new Triplet<>(biomeTag.getDouble("Min"), biomeTag.getDouble("Max"), Temperature.Units.valueOf(biomeTag.getString("Units"))));
        }
        return map;
    }

    public static CompoundTag serializeDimensionTemps(Map<DimensionType, Pair<Double, Temperature.Units>> map, String key)
    {
        CompoundTag tag = new CompoundTag();
        CompoundTag mapTag = new CompoundTag();
        for (Map.Entry<DimensionType, Pair<Double, Temperature.Units>> entry : map.entrySet())
        {
            CompoundTag dimensionTag = new CompoundTag();
            ResourceLocation dimensionId = getDimensionId(entry.getKey());
            if (dimensionId == null)
            {   ColdSweat.LOGGER.error("Error serializing dimension temperatures: dimension \"{}\" does not exist", entry.getKey());
                continue;
            }
            mapTag.put(dimensionId.toString(), dimensionTag);
            dimensionTag.putDouble("Temp", entry.getValue().getFirst());
            dimensionTag.putString("Units", entry.getValue().getSecond().toString());
            mapTag.put(dimensionId.toString(), dimensionTag);
        }
        tag.put(key, mapTag);
        return tag;
    }

    public static Map<DimensionType, Pair<Double, Temperature.Units>> deserializeDimensionTemps(CompoundTag tag, String key)
    {
        Map<DimensionType, Pair<Double, Temperature.Units>> map = new HashMap<>();
        CompoundTag mapTag = tag.getCompound(key);
        for (String dimensionId : mapTag.getAllKeys())
        {
            CompoundTag biomeTag = mapTag.getCompound(dimensionId);
            DimensionType dimension = getDimension(new ResourceLocation(dimensionId));
            if (dimension == null)
            {   ColdSweat.LOGGER.error("Error deserializing dimension temperatures: dimension \"{}\" does not exist", dimensionId);
                continue;
            }
            map.put(dimension, Pair.of(biomeTag.getDouble("Temp"), Temperature.Units.valueOf(biomeTag.getString("Units"))));
        }
        return map;
    }

    public static CompoundTag serializeStructureTemps(Map<StructureFeature<?>, Pair<Double, Temperature.Units>> map, String key)
    {
        CompoundTag tag = new CompoundTag();
        CompoundTag mapTag = new CompoundTag();
    for (Map.Entry<StructureFeature<?>, Pair<Double, Temperature.Units>> entry : map.entrySet())
        {
            CompoundTag structureTag = new CompoundTag();
            ResourceLocation structureId = getStructureId(entry.getKey());
            if (structureId == null)
            {   ColdSweat.LOGGER.error("Error serializing structure temperatures: structure \"{}\" does not exist", entry.getKey());
                continue;
            }
            mapTag.put(structureId.toString(), structureTag);
            structureTag.putDouble("Temp", entry.getValue().getFirst());
            structureTag.putString("Units", entry.getValue().getSecond().toString());
            mapTag.put(structureId.toString(), structureTag);
        }
        tag.put(key, mapTag);

        return tag;
    }

    public static Map<StructureFeature<?>, Pair<Double, Temperature.Units>> deserializeStructureTemps(CompoundTag tag, String key)
    {
        Map<StructureFeature<?>, Pair<Double, Temperature.Units>> map = new HashMap<>();
        CompoundTag mapTag = tag.getCompound(key);
        for (String structureId : mapTag.getAllKeys())
        {
            CompoundTag biomeTag = mapTag.getCompound(structureId);
            StructureFeature<?> structure = getStructure(new ResourceLocation(structureId));
            if (structure == null)
            {   ColdSweat.LOGGER.error("Error deserializing structure temperatures: structure \"{}\" does not exist", structureId);
                continue;
            }
            map.put(structure, Pair.of(biomeTag.getDouble("Temp"), Temperature.Units.valueOf(biomeTag.getString("Units"))));
        }
        return map;
    }

    public static <T> CompoundTag serializeItemMap(Map<Item, T> map, String key, Function<T, CompoundTag> serializer)
    {
        CompoundTag tag = new CompoundTag();
        CompoundTag mapTag = new CompoundTag();
        for (Map.Entry<Item, T> entry : map.entrySet())
        {
            ResourceLocation itemId = ForgeRegistries.ITEMS.getKey(entry.getKey());
            if (itemId == null)
            {
                ColdSweat.LOGGER.error("Error serializing item map: item \"{}\" does not exist", entry.getKey());
                continue;
            }
            mapTag.put(itemId.toString(), serializer.apply(entry.getValue()));
        }
        tag.put(key, mapTag);

        return tag;
    }

    public static <T> Map<Item, T> deserializeItemMap(CompoundTag tag, String key, Function<CompoundTag, T> deserializer)
    {
        Map<Item, T> map = new HashMap<>();
        CompoundTag mapTag = tag.getCompound(key);
        for (String itemID : mapTag.getAllKeys())
        {
            Item item = ForgeRegistries.ITEMS.getValue(new ResourceLocation(itemID));
            map.put(item, deserializer.apply(mapTag.getCompound(itemID)));
        }
        return map;
    }

    public static <T> Map<Item, T> readItemMap(List<? extends List<?>> source, BiFunction<Item, List<?>, T> valueParser)
    {
        Map<Item, T> map = new HashMap<>();
        for (List<?> entry : source)
        {
            String itemId = (String) entry.get(0);
            for (Item item : getItems(itemId))
            {
                map.put(item, valueParser.apply(item, entry.subList(1, entry.size())));
            }
        }
        return map;
    }

    public static <T> void writeItemMap(Map<Item, T> map, Consumer<List<? extends List<?>>> saver, Function<T, List<?>> valueWriter)
    {
        List<List<?>> list = new ArrayList<>();
        for (Map.Entry<Item, T> entry : map.entrySet())
        {
            Item item = entry.getKey();
            T value = entry.getValue();
            List<Object> itemData = new ArrayList<>();
            ResourceLocation itemID = ForgeRegistries.ITEMS.getKey(item);
            if (itemID == null)
            {   ColdSweat.LOGGER.error("Error writing item map: item \"{}\" does not exist", item);
                continue;
            }
            itemData.add(itemID.toString());
            itemData.addAll(valueWriter.apply(value));
            list.add(itemData);
        }
        saver.accept(list);
    }

    public static CompoundTag serializeItemInsulations(Map<Item, Insulator> map, String key)
    {
        CompoundTag tag = new CompoundTag();
        CompoundTag mapTag = new CompoundTag();
        for (Map.Entry<Item, Insulator> entry : map.entrySet())
        {
            Item item = entry.getKey();
            Insulator insulator = entry.getValue();
            ResourceLocation itemID = ForgeRegistries.ITEMS.getKey(item);
            if (itemID == null)
            {   ColdSweat.LOGGER.error("Error serializing item insulations: item \"{}\" does not exist", item);
                continue;
            }
            mapTag.put(itemID.toString(), insulator.serialize());
        }
        tag.put(key, mapTag);

        return tag;
    }

    public static Map<Item, Insulator> deserializeItemInsulations(CompoundTag tag, String key)
    {
        Map<Item, Insulator> map = new HashMap<>();
        CompoundTag mapTag = tag.getCompound(key);
        for (String itemID : mapTag.getAllKeys())
        {
            CompoundTag insulatorTag = mapTag.getCompound(itemID);
            Item item = ForgeRegistries.ITEMS.getValue(new ResourceLocation(itemID));
            if (item == null)
            {   ColdSweat.LOGGER.error("Error deserializing item insulations: item \"{}\" does not exist", itemID);
                continue;
            }
            Insulator insulator = Insulator.deserialize(insulatorTag);
            map.put(item, insulator);
        }
        return map;
    }

    public static Map<Item, Insulator> readItemInsulations(List<? extends List<?>> items, Insulation.Slot slot)
    {
        return readItemMap(items, (item, args) ->
        {
            if (args.size() < 2)
            {   throw ColdSweat.LOGGER.throwing(new ArgumentCountException(args.size(), 2, String.format("Error parsing insulation config for item %s", item)));
            }
            double value1 = ((Number) args.get(0)).doubleValue();
            double value2 = ((Number) args.get(1)).doubleValue();
            String type = args.size() > 2 ? (String) args.get(2) : "static";
            CompoundTag nbt = args.size() > 3 ? NBTHelper.parseCompoundNbt(((String) args.get(3))) : new CompoundTag();
            Insulation insulation = type.equals("static")
                                    ? new StaticInsulation(value1, value2)
                                    : new AdaptiveInsulation(value1, value2);
            ItemRequirement requirement = new ItemRequirement(List.of(), Optional.empty(),
                                                              Optional.empty(), Optional.empty(),
                                                              Optional.empty(), Optional.empty(), new NbtRequirement(nbt));

            return new Insulator(insulation, slot, requirement, EntityRequirement.NONE, new AttributeModifierMap());
        });
    }

    public static void writeItemInsulations(Map<Item, Insulator> items, Consumer<List<? extends List<?>>> saver)
    {
        writeItemMap(items, saver, insulator ->
        {
            if (insulator.predicate().equals(EntityRequirement.NONE))
            {   return List.of();
            }
            List<Object> itemData = new ArrayList<>();
            itemData.add(insulator.insulation() instanceof StaticInsulation
                         ? insulator.insulation().getCold()
                         : ((AdaptiveInsulation) insulator.insulation()).getInsulation());
            itemData.add(insulator.insulation() instanceof StaticInsulation
                         ? insulator.insulation().getHeat()
                         : ((AdaptiveInsulation) insulator.insulation()).getSpeed());
            itemData.add(insulator.insulation() instanceof StaticInsulation
                         ? "static"
                         : "adaptive");
            itemData.add(insulator.data().nbt().serialize().toString());

            return itemData;
        });
    }

    public static <T extends IForgeRegistryEntry<T>> List<T> mapForgeRegistryTagList(IForgeRegistry<T> registry, List<Either<TagKey<T>, T>> eitherList)
    {
        List<T> list = new ArrayList<>();
        for (Either<TagKey<T>, T> either : eitherList)
        {
            either.ifLeft(tagKey -> list.addAll(registry.tags().getTag(tagKey).stream().toList()));
            either.ifRight(object -> list.add(object));
        }
        return list;
    }

    public static <T> List<T> mapVanillaRegistryTagList(ResourceKey<Registry<T>> registry, List<Either<TagKey<T>, T>> eitherList)
    {
        List<T> list = new ArrayList<>();
        for (Either<TagKey<T>, T> either : eitherList)
        {
            either.ifLeft(tagKey -> list.addAll(WorldHelper.getRegistry(registry).getTag(tagKey).orElseThrow()
                                                .stream()
                                                .map(holder -> holder.value())
                                                .toList()));
            either.ifRight(list::add);
        }
        return list;
    }

    public static <T extends IForgeRegistryEntry<T>> Codec<Either<TagKey<T>, T>> createForgeTagCodec(IForgeRegistry<T> forgeRegistry, ResourceKey<Registry<T>> vanillaRegistry)
    {
        return Codec.STRING.xmap(
               objectPath ->
               {
                   if (objectPath.startsWith("#"))
                   {   return Either.left(new TagKey<>(vanillaRegistry, new ResourceLocation(objectPath.substring(1))));
                   }
                   else
                   {   return Either.right(forgeRegistry.getValue(new ResourceLocation(objectPath)));
                   }
               },
               objectEither -> objectEither.map(
                       tagKey -> "#" + tagKey.location(),
                       object -> forgeRegistry.getKey(object).toString()
               ));
    }

    public static <T> Codec<Either<TagKey<T>, T>> createVanillaTagCodec(ResourceKey<Registry<T>> vanillaRegistry)
    {
        return Codec.STRING.xmap(
               objectPath ->
               {
                   if (objectPath.startsWith("#"))
                   {   return Either.left(new TagKey<>(vanillaRegistry, new ResourceLocation(objectPath.substring(1))));
                   }
                   else
                   {   return Either.right(getVanillaRegistryValue(vanillaRegistry, new ResourceLocation(objectPath)).orElseThrow());
                   }
               },
               objectEither -> objectEither.map(
                       tagKey -> "#" + tagKey.location(),
                       object -> getVanillaRegistryKey(vanillaRegistry, object).orElseThrow().toString()
               ));
    }

    public static <T> Optional<T> getVanillaRegistryValue(ResourceKey< Registry<T>> registry, ResourceLocation id)
    {
        try
        {
            return Optional.ofNullable(WorldHelper.getRegistry(registry).get(id));
        }
        catch (Exception e)
        {   return Optional.empty();
        }
    }

    public static <T> Optional<ResourceLocation> getVanillaRegistryKey(ResourceKey<Registry<T>> registry, T value)
    {
        try
        {
            return Optional.ofNullable(WorldHelper.getRegistry(registry).getKey(value));
        }
        catch (Exception e)
        {   return Optional.empty();
        }
    }

    @Nullable
    public static Biome getBiome(ResourceLocation biomeId)
    {
        return CSMath.orElse(ForgeRegistries.BIOMES.getValue(biomeId),
                             getVanillaRegistryValue(Registry.BIOME_REGISTRY, biomeId).orElse(null));
    }
    @Nullable
    public static ResourceLocation getBiomeId(Biome biome)
    {
        return CSMath.orElse(ForgeRegistries.BIOMES.getKey(biome),
                             getVanillaRegistryKey(Registry.BIOME_REGISTRY, biome).orElse(null));
    }

    @Nullable
    public static DimensionType getDimension(ResourceLocation dimensionId)
    {
        return getVanillaRegistryValue(Registry.DIMENSION_TYPE_REGISTRY, dimensionId).orElse(null);
    }
    @Nullable
    public static ResourceLocation getDimensionId(DimensionType dimension)
    {
        return getVanillaRegistryKey(Registry.DIMENSION_TYPE_REGISTRY, dimension).orElse(null);
    }

    @Nullable
    public static StructureFeature<?> getStructure(ResourceLocation structureId)
    {
        return getVanillaRegistryValue(Registry.STRUCTURE_FEATURE_REGISTRY, structureId).orElse(null);
    }
    @Nullable
    public static ResourceLocation getStructureId(StructureFeature<?> structure)
    {
        return getVanillaRegistryKey(Registry.STRUCTURE_FEATURE_REGISTRY, structure).orElse(null);
    }
}
