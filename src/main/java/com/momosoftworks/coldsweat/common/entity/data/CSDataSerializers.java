package com.momosoftworks.coldsweat.common.entity.data;

import net.minecraft.network.FriendlyByteBuf;
import net.minecraft.network.syncher.EntityDataSerializer;
import net.minecraft.network.syncher.EntityDataSerializers;
import net.minecraft.world.item.Item;
import net.minecraftforge.registries.ForgeRegistries;
import org.jetbrains.annotations.NotNull;

import java.util.*;

public class CSDataSerializers
{
    public static final EntityDataSerializer<Long> LONG = new EntityDataSerializer<>()
    {
        public void write(FriendlyByteBuf buffer, Long value)
        {   buffer.writeLong(value);
        }

        public Long read(FriendlyByteBuf buffer)
        {   return buffer.readLong();
        }

        public Long copy(Long value)
        {   return value;
        }
    };

    public static final EntityDataSerializer<Set<UUID>> PLAYER_LIST = new EntityDataSerializer<>()
    {
        public void write(FriendlyByteBuf buffer, Set<UUID> value)
        {
            buffer.writeVarInt(value.size());
            for (UUID uuid : value)
            {
                buffer.writeUUID(uuid);
            }
        }

        public Set<UUID> read(FriendlyByteBuf buffer)
        {
            int size = buffer.readVarInt();
            Set<UUID> uuids = new HashSet<>();
            for (int i = 0; i < size; i++)
            {
                uuids.add(buffer.readUUID());
            }
            return uuids;
        }

        public Set<UUID> copy(Set<UUID> value)
        {   return new HashSet<>(value);
        }
    };

    public static final EntityDataSerializer<Set<Item>> ITEM_LIST = new EntityDataSerializer<>()
    {
        public void write(FriendlyByteBuf buffer, Set<Item> value)
        {
            buffer.writeVarInt(value.size());
            for (Item item : value)
            {
                buffer.writeRegistryId(ForgeRegistries.ITEMS, item);
            }
        }

        @NotNull
        public Set<Item> read(FriendlyByteBuf buffer)
        {
            int size = buffer.readVarInt();
            Set<Item> items = new HashSet<>();
            for (int i = 0; i < size; i++)
            {
                items.add(buffer.readRegistryIdUnsafe(ForgeRegistries.ITEMS));
            }
            return items;
        }

        @NotNull
        public Set<Item> copy(@NotNull Set<Item> value)
        {   return new HashSet<>(value);
        }
    };

    public static final EntityDataSerializer<Map<Item, Integer>> ITEM_INT_MAP = new EntityDataSerializer<>()
    {
        public void write(FriendlyByteBuf buffer, Map<Item, Integer> value)
        {
            buffer.writeVarInt(value.size());
            for (Map.Entry<Item, Integer> entry : value.entrySet())
            {
                buffer.writeRegistryId(ForgeRegistries.ITEMS, entry.getKey());
                buffer.writeVarInt(entry.getValue());
            }
        }

        public Map<Item, Integer> read(FriendlyByteBuf buffer)
        {
            int size = buffer.readVarInt();
            Map<Item, Integer> map = new HashMap<>();
            for (int i = 0; i < size; i++)
            {
                map.put(buffer.readRegistryIdUnsafe(ForgeRegistries.ITEMS), buffer.readVarInt());
            }
            return map;
        }

        public Map<Item, Integer> copy(Map<Item, Integer> value)
        {   return new HashMap<>(value);
        }
    };

    static
    {
        EntityDataSerializers.registerSerializer(LONG);
        EntityDataSerializers.registerSerializer(PLAYER_LIST);
        EntityDataSerializers.registerSerializer(ITEM_LIST);
        EntityDataSerializers.registerSerializer(ITEM_INT_MAP);
    }
}
