package dev.momostudios.coldsweat.core.network.message;

import net.minecraft.client.Minecraft;
import net.minecraft.network.FriendlyByteBuf;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.sounds.SoundEvent;
import net.minecraft.sounds.SoundSource;
import net.minecraft.world.entity.Entity;
import net.minecraftforge.fml.util.ObfuscationReflectionHelper;
import net.minecraftforge.network.NetworkEvent;
import net.minecraftforge.registries.ForgeRegistries;

import java.lang.reflect.Constructor;
import java.lang.reflect.Method;
import java.nio.charset.StandardCharsets;
import java.util.function.Supplier;

public class PlaySoundMessage
{
    static Constructor<?> SOUND_MAKER;
    static Method PLAY_METHOD;

    static
    {
        try
        {
            SOUND_MAKER = ObfuscationReflectionHelper.findConstructor(Class.forName("net.minecraft.client.resources.sounds.EntityBoundSoundInstance"),
                    SoundEvent.class, SoundSource.class, float.class, float.class, Entity.class);
            PLAY_METHOD = ObfuscationReflectionHelper.findMethod(Class.forName("net.minecraft.client.sounds.SoundManager"), "m_120367_",
                    Class.forName("net.minecraft.client.resources.sounds.SoundInstance"));
        }
        catch (ClassNotFoundException e)
        {
            e.printStackTrace();
        }
    }

    String sound;
    int soundChars;
    SoundSource source;
    float volume;
    float pitch;
    int entityID;

    public PlaySoundMessage(String sound, SoundSource source, float volume, float pitch, int entityID)
    {
        this.sound = sound;
        this.source = source;
        soundChars = sound.length();
        this.volume = volume;
        this.pitch = pitch;
        this.entityID = entityID;
    }

    public static void encode(PlaySoundMessage message, FriendlyByteBuf buffer) {
        buffer.writeInt(message.soundChars);
        buffer.writeCharSequence(message.sound.subSequence(0, message.sound.length()), StandardCharsets.UTF_8);
        buffer.writeEnum(message.source);
        buffer.writeFloat(message.volume);
        buffer.writeFloat(message.pitch);
        buffer.writeInt(message.entityID);
    }

    public static PlaySoundMessage decode(FriendlyByteBuf buffer)
    {
        int soundChars = buffer.readInt();
        return new PlaySoundMessage(buffer.readCharSequence(soundChars, StandardCharsets.UTF_8).toString(), buffer.readEnum(SoundSource.class), buffer.readFloat(), buffer.readFloat(), buffer.readInt());
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
                    try
                    {
                        PLAY_METHOD.invoke(Minecraft.getInstance().getSoundManager(), SOUND_MAKER.newInstance(sound,
                                message.source, message.volume, message.pitch, entity));
                    }
                    catch (Exception e)
                    {
                        e.printStackTrace();
                    }
                }
            }
        });
        context.setPacketHandled(true);
    }
}
