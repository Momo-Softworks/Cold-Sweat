package com.momosoftworks.coldsweat.core.network.message;

import com.momosoftworks.coldsweat.ColdSweat;
import com.momosoftworks.coldsweat.util.ClientOnlyHelper;
import com.momosoftworks.coldsweat.util.serialization.RegistryHelper;
import net.minecraft.client.Minecraft;
import net.minecraft.core.registries.Registries;
import net.minecraft.network.FriendlyByteBuf;
import net.minecraft.network.codec.StreamCodec;
import net.minecraft.network.protocol.common.custom.CustomPacketPayload;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.sounds.SoundEvent;
import net.minecraft.sounds.SoundSource;
import net.minecraft.world.entity.Entity;
import net.neoforged.neoforge.network.handling.IPayloadContext;

import java.nio.charset.StandardCharsets;

public class PlayEntityAttachedSoundMessage implements CustomPacketPayload
{
    public static final CustomPacketPayload.Type<PlayEntityAttachedSoundMessage> TYPE = new CustomPacketPayload.Type<>(ResourceLocation.fromNamespaceAndPath(ColdSweat.MOD_ID, "play_sound"));
    public static final StreamCodec<FriendlyByteBuf, PlayEntityAttachedSoundMessage> CODEC = CustomPacketPayload.codec(PlayEntityAttachedSoundMessage::encode, PlayEntityAttachedSoundMessage::decode);

    String sound;
    SoundSource source;
    float volume;
    float pitch;
    int entityID;

    public PlayEntityAttachedSoundMessage(SoundEvent sound, SoundSource source, float volume, float pitch, int entityID)
    {   this(RegistryHelper.getRegistry(Registries.SOUND_EVENT).getKey(sound).toString(), source, volume, pitch, entityID);
    }

    PlayEntityAttachedSoundMessage(String sound, SoundSource source, float volume, float pitch, int entityID)
    {
        this.sound = sound;
        this.source = source;
        this.volume = volume;
        this.pitch = pitch;
        this.entityID = entityID;
    }

    public static void encode(PlayEntityAttachedSoundMessage message, FriendlyByteBuf buffer)
    {
        buffer.writeInt(message.sound.length());
        buffer.writeCharSequence(message.sound, StandardCharsets.UTF_8);
        buffer.writeEnum(message.source);
        buffer.writeFloat(message.volume);
        buffer.writeFloat(message.pitch);
        buffer.writeInt(message.entityID);
    }

    public static PlayEntityAttachedSoundMessage decode(FriendlyByteBuf buffer)
    {
        int soundChars = buffer.readInt();
        return new PlayEntityAttachedSoundMessage(buffer.readCharSequence(soundChars, StandardCharsets.UTF_8).toString(), buffer.readEnum(SoundSource.class), buffer.readFloat(), buffer.readFloat(), buffer.readInt());
    }

    public static void handle(PlayEntityAttachedSoundMessage message, IPayloadContext context)
    {
        context.enqueueWork(() ->
        {
            SoundEvent sound = RegistryHelper.getRegistry(Registries.SOUND_EVENT).get(ResourceLocation.parse(message.sound));
            Entity entity = Minecraft.getInstance().level.getEntity(message.entityID);

            if (entity != null && sound != null)
            {   ClientOnlyHelper.playEntitySound(sound, message.source, message.volume, message.pitch, entity);
            }
        });
    }

    @Override
    public Type<? extends CustomPacketPayload> type()
    {   return TYPE;
    }
}