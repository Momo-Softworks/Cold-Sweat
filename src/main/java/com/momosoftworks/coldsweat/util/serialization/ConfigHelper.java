package com.momosoftworks.coldsweat.util.serialization;

import com.mojang.datafixers.util.Pair;
import com.momosoftworks.coldsweat.ColdSweat;
import com.momosoftworks.coldsweat.api.insulation.AdaptiveInsulation;
import com.momosoftworks.coldsweat.api.insulation.Insulation;
import com.momosoftworks.coldsweat.api.insulation.StaticInsulation;
import com.momosoftworks.coldsweat.api.util.Temperature;
import com.momosoftworks.coldsweat.config.type.Insulator;
import com.momosoftworks.coldsweat.data.codec.requirement.EntityRequirement;
import com.momosoftworks.coldsweat.data.codec.requirement.ItemComponentsRequirement;
import com.momosoftworks.coldsweat.data.codec.requirement.ItemRequirement;
import com.momosoftworks.coldsweat.data.codec.requirement.NbtRequirement;
import com.momosoftworks.coldsweat.data.codec.util.AttributeModifierMap;
import com.momosoftworks.coldsweat.util.exceptions.ArgumentCountException;
import com.momosoftworks.coldsweat.util.math.CSMath;
import net.minecraft.core.RegistryAccess;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.core.registries.Registries;
import net.minecraft.core.Holder;
import net.minecraft.core.HolderSet;
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
import net.minecraft.world.level.levelgen.structure.Structure;
import net.minecraft.world.level.levelgen.structure.StructureType;
import oshi.util.tuples.Triplet;

import java.util.*;
import java.util.function.BiFunction;
import java.util.function.Consumer;
import java.util.function.Function;
import java.util.function.Predicate;

public class ConfigHelper
{
    private ConfigHelper() {}

    public static <T> List<T> parseRegistryItems(ResourceKey<Registry<T>> registry, RegistryAccess registryAccess, String objects)
    {
        List<T> biomeList = new ArrayList<>();
        Optional<Registry<T>> optReg = registryAccess.registry(registry);
        if (!optReg.isPresent()) return biomeList;

        Registry<T> reg = optReg.get();

        for (String objString : objects.split(","))
        {
            if (objString.startsWith("#"))
            {
                final String tagID = objString.replace("#", "");
                Optional<HolderSet.Named<T>> tag = reg.getTag(TagKey.create(registry, ResourceLocation.parse(tagID)));
                tag.ifPresent(tg -> biomeList.addAll(tg.stream().map(Holder::value).toList()));
            }
            else
            {
                ResourceLocation id = ResourceLocation.parse(objString);
                Optional<T> obj = Optional.ofNullable(reg.get(id));
                if (obj.isEmpty())
                {
                    ColdSweat.LOGGER.error("Error parsing config: \"{}\" does not exist", objString);
                    continue;
                }
                biomeList.add(obj.get());
            }
        }
        return biomeList;
    }

    public static List<Block> getBlocks(String... ids)
    {
        List<Block> blocks = new ArrayList<>();
        for (String id : ids)
        {
            if (id.startsWith("#"))
            {
                final String tagID = id.replace("#", "");
                CSMath.doIfNotNull(BuiltInRegistries.BLOCK.getTags(), tags ->
                {   Optional<Pair<TagKey<Block>, HolderSet.Named<Block>>> optionalTag = tags.filter(tag -> tag != null && tag.getFirst().location().toString().equals(tagID)).findFirst();
                    optionalTag.ifPresent(blockITag -> blocks.addAll(optionalTag.get().getSecond().stream().map(Holder::value).toList()));
                });
            }
            else
            {
                ResourceLocation blockId = ResourceLocation.parse(id);
                if (BuiltInRegistries.BLOCK.containsKey(blockId))
                {   blocks.add(BuiltInRegistries.BLOCK.get(blockId));
                }
                else
                {   ColdSweat.LOGGER.error("Error parsing block config: block \"{}\" does not exist", id);
                }
            }
        }
        return blocks;
    }

    public static List<Item> getItems(String... ids)
    {
        List<Item> items = new ArrayList<>();
        for (String itemId : ids)
        {
            if (itemId.startsWith("#"))
            {
                final String tagID = itemId.replace("#", "");
                CSMath.doIfNotNull(BuiltInRegistries.ITEM.getTags(), tags ->
                {   Optional<Pair<TagKey<Item>, HolderSet.Named<Item>>> optionalTag = tags.filter(tag -> tag != null && tag.getFirst().location().toString().equals(tagID)).findFirst();
                    optionalTag.ifPresent(itemITag -> items.addAll(optionalTag.get().getSecond().stream().map(Holder::value).toList()));
                });
            }
            else
            {
                ResourceLocation itemID = ResourceLocation.parse(itemId);
                if (BuiltInRegistries.ITEM.containsKey(itemID))
                {   items.add(BuiltInRegistries.ITEM.get(itemID));
                }
                else
                {   ColdSweat.LOGGER.error("Error parsing item config: item \"{}\" does not exist", itemId);
                }
            }
        }
        return items;
    }

    public static Map<Biome, Triplet<Double, Double, Temperature.Units>> getBiomesWithValues(List<? extends List<?>> source, boolean absolute, RegistryAccess registryAccess)
    {
        Map<Biome, Triplet<Double, Double, Temperature.Units>> map = new HashMap<>();
        for (List<?> entry : source)
        {
            try
            {
                String biomeIdString = (String) entry.get(0);
                for (Biome biome : parseRegistryItems(Registries.BIOME, registryAccess, biomeIdString))
                {
                    if (biome == null)
                    {   ColdSweat.LOGGER.error("Error parsing biome config: string \"{}\" contains a biome that does not exist or is not loaded yet", biomeIdString);
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
                        double variance = 1 / Math.max(1, 2 + biome.getModifiedClimateSettings().downfall() * 2);
                        min = mid - variance;
                        max = mid + variance;
                        units = Temperature.Units.MC;
                    }

                    // Maps the biome ID to the temperature (and variance if present)
                    map.put(biome, new Triplet<>(min, max, units));
                }
            }
            catch (Exception e)
            {
                ColdSweat.LOGGER.error("Error parsing biome config \"{}\"", entry.toString());
                e.printStackTrace();
            }
        }
        return map;
    }

    public static Map<DimensionType, Pair<Double, Temperature.Units>> getDimensionsWithValues(List<? extends List<?>> source, boolean absolute, RegistryAccess registryAccess)
    {
        Map<DimensionType, Pair<Double, Temperature.Units>> map = new HashMap<>();
        for (List<?> entry : source)
        {
            try
            {
                String dimensionIdString = (String) entry.get(0);
                for (DimensionType dimension : parseRegistryItems(Registries.DIMENSION_TYPE, registryAccess, dimensionIdString))
                {
                    if (dimension == null)
                    {   ColdSweat.LOGGER.error("Error parsing dimension config: string \"{}\" contains a dimension that does not exist or is not loaded yet", dimensionIdString);
                        continue;
                    }
                    double temp = ((Number) entry.get(1)).doubleValue();
                    Temperature.Units units = entry.size() == 3 ? Temperature.Units.valueOf(((String) entry.get(2)).toUpperCase()) : Temperature.Units.MC;
                    map.put(dimension, Pair.of(Temperature.convert(temp, units, Temperature.Units.MC, absolute), units));

                }
            }
            catch (Exception e)
            {
                ColdSweat.LOGGER.error("Error parsing dimension config \"{}\"", entry.toString());
                e.printStackTrace();
            }
        }
        return map;
    }

    public static Map<StructureType<?>, Pair<Double, Temperature.Units>> getStructuresWithValues(List<? extends List<?>> source, boolean absolute, RegistryAccess registryAccess)
    {
        Map<StructureType<?>, Pair<Double, Temperature.Units>> map = new HashMap<>();
        for (List<?> entry : source)
        {
            try
            {
                String structureIdString = (String) entry.get(0);
                for (Structure structure : parseRegistryItems(Registries.STRUCTURE, registryAccess, structureIdString))
                {
                    if (structure == null)
                    {   ColdSweat.LOGGER.error("Error parsing structure config: string \"{}\" contains a structure that does not exist or is not loaded yet", structureIdString);
                        continue;
                    }
                    double temp = ((Number) entry.get(1)).doubleValue();
                    Temperature.Units units = entry.size() == 3 ? Temperature.Units.valueOf(((String) entry.get(2)).toUpperCase()) : Temperature.Units.MC;
                    map.put(structure.type(), Pair.of(Temperature.convert(temp, units, Temperature.Units.MC, absolute), units));
                }
            }
            catch (Exception e)
            {   ColdSweat.LOGGER.error("Error parsing structure config \"{}\"", entry.toString());
                e.printStackTrace();
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

    public static List<EntityType<?>> getEntityTypes(String... entities)
    {
        List<EntityType<?>> entityList = new ArrayList<>();
        for (String entity : entities)
        {
            if (entity.startsWith("#"))
            {
                final String tagID = entity.replace("#", "");
                CSMath.doIfNotNull(BuiltInRegistries.ENTITY_TYPE.getTags(), tags ->
                {   Optional<Pair<TagKey<EntityType<?>>, HolderSet.Named<EntityType<?>>>> optionalTag = tags.filter(tag -> tag != null && tag.getFirst().location().toString().equals(tagID)).findFirst();
                    optionalTag.ifPresent(entityITag -> entityList.addAll(optionalTag.get().getSecond().stream().map(Holder::value).toList()));
                });
            }
            else
            {
                ResourceLocation entityId = ResourceLocation.parse(entity);
                if (BuiltInRegistries.ENTITY_TYPE.containsKey(entityId))
                {   entityList.add(BuiltInRegistries.ENTITY_TYPE.get(entityId));
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

    public static CompoundTag serializeBiomeTemps(Map<Biome, Triplet<Double, Double, Temperature.Units>> map, String key, RegistryAccess registryAccess)
    {
        CompoundTag tag = new CompoundTag();
        CompoundTag mapTag = new CompoundTag();
        for (Map.Entry<Biome, Triplet<Double, Double, Temperature.Units>> entry : map.entrySet())
        {
            CompoundTag biomeTag = new CompoundTag();
            ResourceLocation biomeId = RegistryHelper.getBiomeId(entry.getKey(), registryAccess);
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

    public static Map<Biome, Triplet<Double, Double, Temperature.Units>> deserializeBiomeTemps(CompoundTag tag, String key, RegistryAccess registryAccess)
    {
        Map<Biome, Triplet<Double, Double, Temperature.Units>> map = new HashMap<>();
        CompoundTag mapTag = tag.getCompound(key);
        for (String biomeID : mapTag.getAllKeys())
        {
            CompoundTag biomeTag = mapTag.getCompound(biomeID);
            Biome biome = RegistryHelper.getBiome(ResourceLocation.parse(biomeID), registryAccess);
            if (biome == null)
            {   ColdSweat.LOGGER.error("Error deserializing biome temperatures: biome \"{}\" does not exist", biomeID);
                continue;
            }
            map.put(biome, new Triplet<>(biomeTag.getDouble("Min"), biomeTag.getDouble("Max"), Temperature.Units.valueOf(biomeTag.getString("Units"))));
        }
        return map;
    }

    public static CompoundTag serializeDimensionTemps(Map<DimensionType, Pair<Double, Temperature.Units>> map, String key, RegistryAccess registryAccess)
    {
        CompoundTag tag = new CompoundTag();
        CompoundTag mapTag = new CompoundTag();
        for (Map.Entry<DimensionType, Pair<Double, Temperature.Units>> entry : map.entrySet())
        {
            CompoundTag dimensionTag = new CompoundTag();
            ResourceLocation dimensionId = RegistryHelper.getDimensionId(entry.getKey(), registryAccess);
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

    public static Map<DimensionType, Pair<Double, Temperature.Units>> deserializeDimensionTemps(CompoundTag tag, String key, RegistryAccess registryAccess)
    {
        Map<DimensionType, Pair<Double, Temperature.Units>> map = new HashMap<>();
        CompoundTag mapTag = tag.getCompound(key);
        for (String dimensionId : mapTag.getAllKeys())
        {
            CompoundTag biomeTag = mapTag.getCompound(dimensionId);
            DimensionType dimension = RegistryHelper.getDimension(ResourceLocation.parse(dimensionId), registryAccess);
            if (dimension == null)
            {   ColdSweat.LOGGER.error("Error deserializing dimension temperatures: dimension \"{}\" does not exist", dimensionId);
                continue;
            }
            map.put(dimension, Pair.of(biomeTag.getDouble("Temp"), Temperature.Units.valueOf(biomeTag.getString("Units"))));
        }
        return map;
    }

    public static CompoundTag serializeStructureTemps(Map<StructureType<?>, Pair<Double, Temperature.Units>> map, String key, RegistryAccess registryAccess)
    {
        CompoundTag tag = new CompoundTag();
        CompoundTag mapTag = new CompoundTag();
        for (Map.Entry<StructureType<?>, Pair<Double, Temperature.Units>> entry : map.entrySet())
        {
            CompoundTag structureTag = new CompoundTag();
            ResourceLocation structureId = RegistryHelper.getStructureId(entry.getKey(), registryAccess);
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

    public static Map<StructureType<?>, Pair<Double, Temperature.Units>> deserializeStructureTemps(CompoundTag tag, String key, RegistryAccess registryAccess)
    {
        Map<StructureType<?>, Pair<Double, Temperature.Units>> map = new HashMap<>();
        CompoundTag mapTag = tag.getCompound(key);
        for (String structureId : mapTag.getAllKeys())
        {
            CompoundTag biomeTag = mapTag.getCompound(structureId);
            StructureType<?> structure = RegistryHelper.getStructure(ResourceLocation.parse(structureId), registryAccess);
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
            if (!BuiltInRegistries.ITEM.containsValue(entry.getKey()))
            {
                ColdSweat.LOGGER.error("Error serializing item map: item \"{}\" does not exist", entry.getKey());
                continue;
            }
            ResourceLocation itemId = BuiltInRegistries.ITEM.getKey(entry.getKey());
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
            Item item = BuiltInRegistries.ITEM.get(ResourceLocation.parse(itemID));
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
            if (!BuiltInRegistries.ITEM.containsValue(item))
            {   ColdSweat.LOGGER.error("Error writing item map: item \"{}\" does not exist", item);
                continue;
            }
            ResourceLocation itemID = BuiltInRegistries.ITEM.getKey(item);
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
            if (!BuiltInRegistries.ITEM.containsValue(item))
            {   ColdSweat.LOGGER.error("Error serializing item insulations: item \"{}\" does not exist", item);
                continue;
            }
            ResourceLocation itemID = BuiltInRegistries.ITEM.getKey(item);
            if (insulator == null)
            {   ColdSweat.LOGGER.error("Error serializing item insulations: insulation value for item \"{}\" is null", item);
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
            ResourceLocation itemLocation = ResourceLocation.parse(itemID);
            if (!BuiltInRegistries.ITEM.containsKey(itemLocation))
            {   ColdSweat.LOGGER.error("Error deserializing item insulations: item \"{}\" does not exist", itemID);
                continue;
            }
            Item item = BuiltInRegistries.ITEM.get(itemLocation);
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
            {   ColdSweat.LOGGER.error(new ArgumentCountException(args.size(), 2, String.format("Error parsing insulation config for item %s", item)).getMessage());
                return null;
            }
            double value1 = ((Number) args.get(0)).doubleValue();
            double value2 = ((Number) args.get(1)).doubleValue();
            String type = args.size() > 2 ? (String) args.get(2) : "static";
            ItemComponentsRequirement components = args.size() > 3 ? ItemComponentsRequirement.parse(((String) args.get(3))) : new ItemComponentsRequirement();
            Insulation insulation = type.equals("static")
                                    ? new StaticInsulation(value1, value2)
                                    : new AdaptiveInsulation(value1, value2);
            ItemRequirement requirement = new ItemRequirement(List.of(), Optional.empty(),
                                                              Optional.empty(), Optional.empty(),
                                                              Optional.empty(), Optional.empty(), components);

            return new Insulator(insulation, slot, requirement, EntityRequirement.NONE, new AttributeModifierMap(), new HashMap<>());
        });
    }

    public static void writeItemInsulations(Map<Item, Insulator> items, Consumer<List<? extends List<?>>> saver)
    {
        writeItemMap(items, saver, insulator ->
        {
            if (insulator == null)
            {   ColdSweat.LOGGER.error("Error writing item insulations: insulator value is null");
                return List.of();
            }
            if (!insulator.predicate().equals(EntityRequirement.NONE) || !insulator.attributes().getMap().isEmpty())
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
            itemData.add(insulator.data().components().serialize().toString());

            return itemData;
        });
    }
}
