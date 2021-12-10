package net.momostudios.coldsweat.core.network.message;

import net.minecraft.network.PacketBuffer;
import net.minecraft.util.ResourceLocation;
import net.minecraft.util.SoundEvent;
import net.minecraftforge.fml.common.ObfuscationReflectionHelper;
import net.minecraftforge.fml.network.NetworkEvent;
import net.momostudios.coldsweat.ColdSweat;

import java.lang.reflect.Method;
import java.util.function.Supplier;

public class PlayerDamageMessage
{

    int damageID;

    public PlayerDamageMessage(int damageID) {
        this.damageID = damageID;
    }

    public static void encode(PlayerDamageMessage message, PacketBuffer buffer) {
        buffer.writeInt(message.damageID);
    }

    public static PlayerDamageMessage decode(PacketBuffer buffer)
    {
        return new PlayerDamageMessage(buffer.readInt());
    }

    public static void handle(PlayerDamageMessage message, Supplier<NetworkEvent.Context> contextSupplier)
    {
        NetworkEvent.Context context = contextSupplier.get();
        context.enqueueWork(() ->
        {
            if (!context.getDirection().getReceptionSide().isServer())
            {
                SoundEvent sound;
                switch (message.damageID)
                {
                    case 0:
                        sound = new SoundEvent(new ResourceLocation(ColdSweat.MOD_ID, "entity.player.damage.freeze"));
                    default:
                        sound = new SoundEvent(new ResourceLocation(ColdSweat.MOD_ID, "entity.player.damage.freeze"));
                }

                Method getInstance;
                Method getSoundHandler;
                Method master;
                Method play;
                try
                {
                    getInstance = ObfuscationReflectionHelper.findMethod(Class.forName("net.minecraft.client.Minecraft"), "getInstance");
                    getSoundHandler = ObfuscationReflectionHelper.findMethod(Class.forName("net.minecraft.client.audio.SoundHandler"), "getSoundHandler");
                    master = ObfuscationReflectionHelper.findMethod(Class.forName("net.minecraft.client.audio.SimpleSound"), "playSound", SoundEvent.class, float.class, float.class);
                    play = ObfuscationReflectionHelper.findMethod(Class.forName("net.minecraft.client.audio.SoundHandler"), "play", Class.forName("net.minecraft.client.audio.SimpleSound"));
                    play.invoke(getSoundHandler.invoke(getInstance.invoke(null)), master.invoke(sound, (float) Math.random() * 0.3f + 0.7f, 1f));
                } catch (Exception e)
                {
                }
            }
        });
    }
}
