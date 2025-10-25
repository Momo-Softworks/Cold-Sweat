package com.momosoftworks.coldsweat.util.serialization;

import com.google.common.collect.Multimap;
import com.google.gson.JsonObject;
import com.mojang.datafixers.util.Either;
import com.mojang.datafixers.util.Pair;
import com.mojang.serialization.Codec;
import com.mojang.serialization.DataResult;
import com.mojang.serialization.DynamicOps;
import com.mojang.serialization.JsonOps;
import com.momosoftworks.coldsweat.ColdSweat;
import com.momosoftworks.coldsweat.config.ConfigLoadingHandler;
import com.momosoftworks.coldsweat.config.spec.CSConfigSpec;
import com.momosoftworks.coldsweat.data.RegistryHolder;
import com.momosoftworks.coldsweat.data.codec.configuration.FuelData;
import com.momosoftworks.coldsweat.data.codec.impl.ConfigData;
import com.momosoftworks.coldsweat.data.codec.util.NegatableList;
import com.momosoftworks.coldsweat.util.math.FastMap;
import com.momosoftworks.coldsweat.util.math.FastMultiMap;
import com.momosoftworks.coldsweat.util.math.RegistryMultiMap;
import net.minecraft.core.*;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.core.registries.Registries;
import net.minecraft.resources.ResourceKey;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.server.packs.resources.Resource;
import net.minecraft.server.packs.resources.ResourceManager;
import net.minecraft.tags.TagKey;
import net.minecraft.util.GsonHelper;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.block.Block;
import net.neoforged.neoforge.common.ModConfigSpec;
import org.apache.logging.log4j.util.TriConsumer;

import java.io.IOException;
import java.io.InputStreamReader;
import java.io.Reader;
import java.nio.charset.StandardCharsets;
import java.util.*;
import java.util.function.Function;
import java.util.function.Predicate;
import java.util.function.Supplier;

public class ConfigHelper
{
    private ConfigHelper() {}

    public static <T> NegatableList<Either<TagKey<T>, OptionalHolder<T>>> parseRegistryItems(ResourceKey<Registry<T>> registry, RegistryAccess registryAccess, String objects)
    {   return parseRegistryItems(registry, registryAccess, objects.split(","));
    }

    public static <T> NegatableList<Either<TagKey<T>, OptionalHolder<T>>> parseRegistryItems(ResourceKey<Registry<T>> registry, RegistryAccess registryAccess, String[] objects)
    {
        NegatableList<Either<TagKey<T>, OptionalHolder<T>>> registryList = new NegatableList<>();
        Registry<T> reg = registryAccess.registryOrThrow(registry);

        for (String objString : objects)
        {
            boolean negate = objString.startsWith("!");
            if (negate) objString = objString.substring(1);
            if (objString.startsWith("#"))
            {
                final String tagString = objString.replace("#", "");
                ResourceLocation tagID = ResourceLocation.tryParse(tagString);
                if (tagID == null)
                {   ColdSweat.LOGGER.error("Error parsing config: {} \"{}\" is not a valid tag ID", registry.location().getPath(), tagString);
                    continue;
                }
                registryList.add(Either.left(TagKey.create(registry, tagID)), negate);
            }
            else
            {
                ResourceLocation id = ResourceLocation.tryParse(objString);
                if (id == null)
                {   ColdSweat.LOGGER.error("Error parsing config: {} \"{}\" is not a valid ID", registry.location().getPath(), objString);
                    continue;
                }
                Optional<Holder.Reference<T>> obj = reg.getHolder(ResourceKey.create(registry, id));
                if (obj.isEmpty())
                {
                    ColdSweat.LOGGER.error("Error parsing config: {} \"{}\" does not exist", registry.location().getPath(), objString);
                    continue;
                }
                registryList.add(Either.right(OptionalHolder.ofHolder(obj.get())), negate);
            }
        }
        return registryList;
    }

    public static <T> NegatableList<Either<TagKey<T>, T>> parseBuiltinItems(ResourceKey<Registry<T>> registryKey, Registry<T> registry, String objects)
    {   return parseBuiltinItems(registryKey, registry, objects.split(","));
    }

    public static <T> NegatableList<Either<TagKey<T>, T>> parseBuiltinItems(ResourceKey<Registry<T>> registryKey, Registry<T> registry, String[] objects)
    {
        NegatableList<Either<TagKey<T>, T>> registryList = new NegatableList<>();

        for (String objString : objects)
        {
            boolean negate = objString.startsWith("!");
            if (negate) objString = objString.substring(1);
            if (objString.startsWith("#"))
            {
                final String tagString = objString.replace("#", "");
                ResourceLocation tagID = ResourceLocation.tryParse(tagString);
                if (tagID == null)
                {   ColdSweat.LOGGER.error("Error parsing config: {} \"{}\" is not a valid tag ID", registryKey.location().getPath(), tagString);
                    continue;
                }
                registryList.add(Either.left(TagKey.create(registryKey, tagID)), negate);
            }
            else
            {
                ResourceLocation id = ResourceLocation.tryParse(objString);
                if (id == null)
                {   ColdSweat.LOGGER.error("Error parsing config: {} \"{}\" is not a valid ID", registryKey.location().getPath(), objString);
                    continue;
                }
                Optional<T> obj = registry.getOptional(id);
                if (obj.isEmpty())
                {   ColdSweat.LOGGER.error("Error parsing config: {} \"{}\" does not exist", registryKey.location().getPath(), objString);
                    continue;
                }
                registryList.add(Either.right(obj.get()), negate);
            }
        }
        return registryList;
    }

    public static NegatableList<Either<TagKey<Block>, Block>> getBlocks(String blocks)
    {   return getBlocks(blocks.split(","));
    }
    public static NegatableList<Either<TagKey<Block>, Block>> getBlocks(String[] blocks)
    {   return parseBuiltinItems(Registries.BLOCK, BuiltInRegistries.BLOCK, blocks);
    }

    public static NegatableList<Either<TagKey<Item>, Item>> getItems(String items)
    {   return getItems(items.split(","));
    }
    public static NegatableList<Either<TagKey<Item>, Item>> getItems(String[] items)
    {   return parseBuiltinItems(Registries.ITEM, BuiltInRegistries.ITEM, items);
    }

    public static NegatableList<Either<TagKey<EntityType<?>>, EntityType<?>>> getEntityTypes(String entities)
    {   return getEntityTypes(entities.split(","));
    }
    public static NegatableList<Either<TagKey<EntityType<?>>, EntityType<?>>> getEntityTypes(String[] entities)
    {   return parseBuiltinItems(Registries.ENTITY_TYPE, BuiltInRegistries.ENTITY_TYPE, entities);
    }

    public static <K, V extends ConfigData> Multimap<K, V> parseTomlRegistry(CSConfigSpec.ConfigValue<List<? extends List<?>>> config, Function<List<?>, V> tomlParser, Function<V, NegatableList<Either<TagKey<K>, K>>> keyListGetter,
                                                                             Registry<K> keyRegistry, RegistryHolder<V> valueRegistry)
    {
        Multimap<K, V> dataMap = new RegistryMultiMap<>();
        for (List<?> entry : config.get())
        {
            V data = tomlParser.apply(entry);
            if (data == null) continue;

            data.setConfigType(ConfigData.Type.TOML);

            RegistryHelper.mapBuiltinRegistryTagList(keyRegistry, keyListGetter.apply(data)).forEach(ent -> dataMap.put(ent, data));
        }
        // Handle registry modifiers
        ConfigLoadingHandler.modifyEntries(dataMap, valueRegistry);
        return dataMap;
    }

    public static <K, V extends ConfigData> Map<K, V> parseTomlRegistryUnique(ModConfigSpec.ConfigValue<List<? extends List<?>>> config, Function<List<?>, V> tomlParser, Function<V, List<Either<TagKey<K>, K>>> keyListGetter,
                                                                              Registry<K> keyRegistry, RegistryHolder<V> valueRegistry)
    {
        Map<K, V> dataMap = new HashMap<>();
        for (List<?> entry : config.get())
        {
            V data = tomlParser.apply(entry);
            if (data == null) continue;

            data.setConfigType(ConfigData.Type.TOML);

            RegistryHelper.mapBuiltinRegistryTagList(keyRegistry, keyListGetter.apply(data)).forEach(ent -> dataMap.put(ent, data));
        }
        // Handle registry removals
        ConfigLoadingHandler.modifyEntries(dataMap, valueRegistry);
        return dataMap;
    }

    private static <K, V> void putRegistryEntries(Multimap<K, V> map, Registry<K> registry, List<Either<TagKey<K>, K>> list, V data)
    {
        RegistryHelper.mapBuiltinRegistryTagList(registry, list).forEach(entry -> map.put(entry, data));
    }

    public static <K, V extends ConfigData> Map<Holder<K>, V> getRegistryMap(List<? extends List<?>> source, RegistryAccess registryAccess, ResourceKey<Registry<K>> keyRegistry,
                                                                             Function<List<?>, V> valueCreator, Function<V, NegatableList<Either<TagKey<K>, OptionalHolder<K>>>> taggedListGetter)
    {
        return getRegistryMapLike(source, registryAccess, keyRegistry, valueCreator, taggedListGetter, FastMap::new, FastMap::put);
    }

    public static <K, V extends ConfigData> Multimap<Holder<K>, V> getRegistryMultimap(List<? extends List<?>> source, RegistryAccess registryAccess, ResourceKey<Registry<K>> keyRegistry,
                                                                                       Function<List<?>, V> valueCreator, Function<V, NegatableList<Either<TagKey<K>, OptionalHolder<K>>>> taggedListGetter)
    {
        return getRegistryMapLike(source, registryAccess, keyRegistry, valueCreator, taggedListGetter, FastMultiMap::new, FastMultiMap::put);
    }

    private static <K, V extends ConfigData, M> M getRegistryMapLike(List< ? extends List<?>> source, RegistryAccess registryAccess, ResourceKey<Registry<K>> keyRegistry,
                                                                     Function<List<?>, V> valueCreator, Function<V, NegatableList<Either<TagKey<K>, OptionalHolder<K>>>> taggedListGetter,
                                                                     Supplier<M> mapSupplier, TriConsumer<M, Holder<K>, V> mapAdder)
    {
        M map = mapSupplier.get();
        for (List<?> entry : source)
        {
            V data = valueCreator.apply(entry);
            if (data != null)
            {
                data.setConfigType(ConfigData.Type.TOML);
                for (OptionalHolder<K> key : RegistryHelper.mapRegistryTagList(keyRegistry, taggedListGetter.apply(data), registryAccess))
                {   mapAdder.accept(map, key.get(), data);
                }
            }
            else ColdSweat.LOGGER.error("Error parsing {} config \"{}\"", keyRegistry.location(), entry.toString());
        }
        return map;
    }

    public static <T> Codec<Either<TagKey<T>, T>> tagOrBuiltinCodec(ResourceKey<Registry<T>> vanillaRegistry, Registry<T> forgeRegistry)
    {
        Codec<TagKey<T>> tagCodec = Codec.STRING.comapFlatMap(
            str ->
            {
                if (!str.startsWith("#"))
                {   return DataResult.error(() -> String.format("Not a tag key for builtin registry %s: %s", vanillaRegistry.location(), str));
                }
                ResourceLocation tagID = ResourceLocation.tryParse(str.replace("#", ""));
                if (tagID == null) return DataResult.error(() -> String.format("Invalid tag ID \"%s\"", str));
                return DataResult.success(TagKey.create(vanillaRegistry, tagID));
            },
            key -> "#" + key.location());

        Codec<T> objectCodec = Codec.STRING.comapFlatMap(
            str ->
            {
                ResourceLocation objectID = ResourceLocation.tryParse(str);
                if (objectID == null) return DataResult.error(() -> String.format("Invalid ID \"%s\"", str));
                Optional<T> obj = forgeRegistry.getOptional(objectID);
                if (obj.isEmpty())
                {
                    ColdSweat.LOGGER.error("Error deserializing config: object \"{}\" does not exist", str);
                    return DataResult.error(() -> "Object does not exist");
                }
                return DataResult.success(obj.get());
            },
            obj ->
            {
                ResourceLocation itemLocation = forgeRegistry.getKey(obj);
                return itemLocation.toString();
            });

        return Codec.either(tagCodec, objectCodec);
    }

    public static <T> Codec<Either<TagKey<T>, Holder<T>>> tagOrBuiltinHolderCodec(ResourceKey<Registry<T>> vanillaRegistry, Registry<T> registry)
    {
        Codec<TagKey<T>> tagCodec = Codec.STRING.comapFlatMap(
            str ->
            {   // Tag keys must start with "#"
                if (!str.startsWith("#"))
                {   return DataResult.error(() -> String.format("Not a tag key for builtin holder registry %s: %s", vanillaRegistry.location(), str));
                }
                ResourceLocation tagID = ResourceLocation.parse(str.replace("#", ""));
                return DataResult.success(TagKey.create(vanillaRegistry, tagID));
            },
            key -> "#" + key.location());

        Codec<Holder<T>> holderCodec = Codec.STRING.comapFlatMap(
            str ->
            {   // Parse object ID
                ResourceLocation objectID = ResourceLocation.tryParse(str);
                if (objectID == null) return DataResult.error(() -> String.format("Invalid ID \"%s\"", str));
                // Get holder
                Optional<Holder.Reference<T>> holder = registry.getHolder(objectID);
                if (holder.isEmpty())
                {
                    ColdSweat.LOGGER.error("Error deserializing config: object \"{}\" does not exist", str);
                    return DataResult.error(() -> "Object does not exist");
                }
                return DataResult.success(holder.get());
            },
            holder ->
            {
                ResourceLocation itemLocation = registry.getKey(holder.value());
                return itemLocation.toString();
            });

        return Codec.either(tagCodec, holderCodec);
    }

    public static <T> Codec<Either<TagKey<T>, OptionalHolder<T>>> tagOrHolderCodec(ResourceKey<Registry<T>> vanillaRegistry)
    {
        return new Codec<>()
        {
            @Override
            public <S> DataResult<Pair<Either<TagKey<T>, OptionalHolder<T>>, S>> decode(DynamicOps<S> ops, S input)
            {
                DataResult<String> result = Codec.STRING.parse(ops, input);
                if (result.error().isPresent())
                {   return DataResult.error(() -> result.error().get().message());
                }
                String str = result.result().orElse("");
                // Decode tag key
                if (str.startsWith("#"))
                {
                    ResourceLocation tagID = ResourceLocation.tryParse(str.replace("#", ""));
                    if (tagID == null) return DataResult.error(() -> String.format("Invalid tag ID \"%s\"", str));
                    return DataResult.success(Pair.of(Either.left(TagKey.create(vanillaRegistry, tagID)), input));
                }
                // Decode holder
                else
                {
                    ResourceLocation objectID = ResourceLocation.tryParse(str);
                    if (objectID == null) return DataResult.error(() -> String.format("Invalid ID \"%s\"", str));
                    ResourceKey<T> key = ResourceKey.create(vanillaRegistry, objectID);
                    return DataResult.success(Pair.of(Either.right(new OptionalHolder<>(key)), input));
                }
            }

            @Override
            public <S> DataResult<S> encode(Either<TagKey<T>, OptionalHolder<T>> either, DynamicOps<S> ops, S prefix)
            {
                if (either.left().isPresent())
                {   return Codec.STRING.encode("#" + either.left().get().location(), ops, prefix);
                }
                else if (either.right().isPresent())
                {   OptionalHolder<T> holder = either.right().get();
                    ResourceKey<T> key = holder.key();
                    return Codec.STRING.encode(key.location().toString(), ops, prefix);
                }
                return DataResult.error(() -> "Either is empty");
            }
        };
    }

    public static <T> Codec<Either<TagKey<T>, ResourceKey<T>>> tagOrResourceKeyCodec(ResourceKey<Registry<T>> vanillaRegistry)
    {
        return Codec.either(Codec.STRING.comapFlatMap(str ->
                                                      {
                                                          if (!str.startsWith("#"))
                                                          {   return DataResult.error(() -> String.format("Not a tag key for dynamic resource registry %s: %s", vanillaRegistry.location(), str));
                                                          }
                                                          ResourceLocation itemLocation = ResourceLocation.tryParse(str.replace("#", ""));
                                                          if (itemLocation == null) return DataResult.error(() -> String.format("Invalid tag ID \"%s\"", str));
                                                          return DataResult.success(TagKey.create(vanillaRegistry, itemLocation));
                                                      },
                                                      key -> "#" + key.location()),
                            ResourceKey.codec(vanillaRegistry));
    }

    public static Optional<FuelData> findFirstFuelMatching(DynamicHolder<Multimap<Item, FuelData>> predicates, ItemStack stack)
    {
        for (FuelData predicate : predicates.get().get(stack.getItem()))
        {
            if (predicate.test(stack))
            {   return Optional.of(predicate);
            }
        }
        return Optional.empty();
    }

    public static <T> Optional<T> parseResource(ResourceManager resourceManager, ResourceLocation location, Codec<T> codec)
    {
        if (resourceManager == null)
        {   return Optional.empty();
        }
        try
        {
            Resource resource = resourceManager.getResource(location).orElseThrow();
            try (Reader reader = new InputStreamReader(resource.open(), StandardCharsets.UTF_8))
            {
                JsonObject json = GsonHelper.parse(reader);
                return codec.parse(JsonOps.INSTANCE, json).result();
            }
        }
        catch (IOException e)
        {   ColdSweat.LOGGER.error(new RuntimeException("Failed to load JSON file: " + location, e));
            return Optional.empty();
        }
    }

    public static String concatStringList(List<String> list)
    {
        StringBuilder builder = new StringBuilder();
        Iterator<String> iter = list.iterator();
        while (iter.hasNext())
        {
            builder.append(iter.next());
            if (iter.hasNext())
            {   builder.append(",");
            }
        }
        return builder.toString();
    }

    public static <K, T extends ConfigData> List<T> getTaggedConfigsFor(K object, TagKey<T> tag, Multimap<K, T> config, RegistryAccess registryAccess)
    {
        Registry<T> registry = registryAccess.registryOrThrow(tag.registry());

        List<T> results = new ArrayList<>();
        for (T configData : config.get(object))
        {
            Optional.ofNullable(configData.registryKey()).flatMap(k -> registry.getHolder((ResourceKey<T>) (ResourceKey) k))
            .ifPresent(holder ->
            {
                if (holder.is(tag))
                {   results.add(configData);
                }
            });
        }
        return results;
    }

    public static <K, V> V getFirstOrNull(DynamicHolder<Multimap<K, V>> map, K key, Predicate<V> filter)
    {
        Collection<V> values = map.get().get(key).stream().filter(filter).toList();
        if (values.isEmpty())
        {   return null;
        }
        return values.iterator().next();
    }
}
