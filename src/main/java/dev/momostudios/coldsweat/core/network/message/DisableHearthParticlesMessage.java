package dev.momostudios.coldsweat.core.network.message;

import net.minecraft.core.Registry;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.network.FriendlyByteBuf;
import net.minecraft.resources.ResourceKey;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.server.MinecraftServer;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.level.Level;
import net.minecraftforge.common.util.LogicalSidedProvider;
import net.minecraftforge.fml.LogicalSide;
import net.minecraftforge.fml.util.ObfuscationReflectionHelper;
import net.minecraftforge.network.NetworkEvent;

import java.lang.reflect.Field;
import java.lang.reflect.Method;
import java.util.function.Supplier;

public class DisableHearthParticlesMessage
{
    CompoundTag nbt;
    int entityID;
    String worldKey;

    public DisableHearthParticlesMessage(Player player, CompoundTag nbt)
    {
        this.nbt = nbt;
        this.entityID = player.getId();
        this.worldKey = player.level.dimension().location().toString();
    }

    DisableHearthParticlesMessage(int entityID, String worldKey, CompoundTag nbt)
    {
        this.nbt = nbt;
        this.entityID = entityID;
        this.worldKey = worldKey;
    }

    public static void encode(DisableHearthParticlesMessage message, FriendlyByteBuf buffer)
    {
        buffer.writeInt(message.entityID);
        buffer.writeUtf(message.worldKey);
        buffer.writeNbt(message.nbt);
    }

    public static DisableHearthParticlesMessage decode(FriendlyByteBuf buffer)
    {
        return new DisableHearthParticlesMessage(buffer.readInt(), buffer.readUtf(), buffer.readNbt());
    }

    static Class MINECRAFT = null;
    static Method GET_INSTANCE = null;
    static Field CLIENT_LEVEL = null;
    static
    {
        try
        {
            MINECRAFT = Class.forName("net.minecraft.client.Minecraft");
            GET_INSTANCE = ObfuscationReflectionHelper.findMethod(MINECRAFT, "m_91087_");
            CLIENT_LEVEL = ObfuscationReflectionHelper.findField(MINECRAFT, " f_91073_");
        } catch (Exception ignored) {}
    }

    public static void handle(DisableHearthParticlesMessage message, Supplier<NetworkEvent.Context> contextSupplier)
    {
        NetworkEvent.Context context = contextSupplier.get();
        context.enqueueWork(() ->
        {
            try
            {
                Level level = (context.getDirection().getReceptionSide().isClient() && ((Level) CLIENT_LEVEL.get(GET_INSTANCE.invoke(null))).dimension().location().toString().equals(message.worldKey))
                        ? (Level) CLIENT_LEVEL.get(GET_INSTANCE.invoke(null))
                        : ((MinecraftServer) LogicalSidedProvider.WORKQUEUE.get(LogicalSide.SERVER)).getLevel(ResourceKey.create(Registry.DIMENSION_REGISTRY, new ResourceLocation(message.worldKey)));
                if (level != null)
                {
                    Entity entity = level.getEntity(message.entityID);
                    if (entity instanceof Player)
                    {
                        entity.getPersistentData().put("disabledHearths", message.nbt);
                    }
                }
            } catch (Exception ignored) {}
        });
        context.setPacketHandled(true);
    }
}
