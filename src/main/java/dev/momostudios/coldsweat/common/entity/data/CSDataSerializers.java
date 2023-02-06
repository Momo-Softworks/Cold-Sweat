package dev.momostudios.coldsweat.common.entity.data;

import net.minecraft.network.FriendlyByteBuf;
import net.minecraft.network.syncher.EntityDataSerializer;
import net.minecraft.network.syncher.EntityDataSerializers;

import java.util.HashSet;
import java.util.Set;
import java.util.UUID;

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
        {   return value;
        }
    };

    static
    {
        EntityDataSerializers.registerSerializer(LONG);
        EntityDataSerializers.registerSerializer(PLAYER_LIST);
    }
}
