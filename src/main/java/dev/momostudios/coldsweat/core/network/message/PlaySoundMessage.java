package dev.momostudios.coldsweat.core.network.message;

import dev.momostudios.coldsweat.util.ClientOnlyHelper;
import net.minecraft.client.Minecraft;
import net.minecraft.entity.Entity;
import net.minecraft.network.PacketBuffer;
import net.minecraft.util.ResourceLocation;
import net.minecraft.util.SoundCategory;
import net.minecraft.util.SoundEvent;
import net.minecraftforge.fml.network.NetworkEvent;
import net.minecraftforge.registries.ForgeRegistries;

import java.nio.charset.StandardCharsets;
import java.util.function.Supplier;

public class PlaySoundMessage
{
    String sound;
    int soundChars;
    SoundCategory category;
    float volume;
    float pitch;
    int entityID;

    public PlaySoundMessage(String sound, SoundCategory category, float volume, float pitch, int entityID)
    {
        this.sound = sound;
        this.category = category;
        soundChars = sound.length();
        this.volume = volume;
        this.pitch = pitch;
        this.entityID = entityID;
    }

    public static void encode(PlaySoundMessage message, PacketBuffer buffer) {
        buffer.writeInt(message.soundChars);
        buffer.writeCharSequence(message.sound, StandardCharsets.UTF_8);
        buffer.writeEnum(message.category);
        buffer.writeFloat(message.volume);
        buffer.writeFloat(message.pitch);
        buffer.writeInt(message.entityID);
    }

    public static PlaySoundMessage decode(PacketBuffer buffer)
    {
        int soundChars = buffer.readInt();
        return new PlaySoundMessage(buffer.readCharSequence(soundChars, StandardCharsets.UTF_8).toString(), buffer.readEnum(SoundCategory.class), buffer.readFloat(), buffer.readFloat(), buffer.readInt());
    }

    public static void handle(PlaySoundMessage message, Supplier<NetworkEvent.Context> contextSupplier)
    {
        NetworkEvent.Context context = contextSupplier.get();
        context.enqueueWork(() ->
        {
            if (context.getDirection().getReceptionSide().isClient())
            {
                SoundEvent sound = ForgeRegistries.SOUND_EVENTS.getValue(new ResourceLocation(message.sound));
                Entity entity = Minecraft.getInstance().level.getEntity(message.entityID);

                if (entity != null && sound != null)
                {
                    ClientOnlyHelper.playEntitySound(sound, message.category, message.volume, message.pitch, entity);
                }
            }
        });
        context.setPacketHandled(true);
    }
}
