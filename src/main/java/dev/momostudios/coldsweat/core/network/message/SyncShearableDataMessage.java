package dev.momostudios.coldsweat.core.network.message;

import dev.momostudios.coldsweat.common.capability.ModCapabilities;
import net.minecraft.client.Minecraft;
import net.minecraft.core.Registry;
import net.minecraft.network.FriendlyByteBuf;
import net.minecraft.resources.ResourceKey;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.server.MinecraftServer;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.level.Level;
import net.minecraftforge.common.util.LogicalSidedProvider;
import net.minecraftforge.fml.LogicalSide;
import net.minecraftforge.fml.util.ObfuscationReflectionHelper;
import net.minecraftforge.network.NetworkEvent;

import java.lang.reflect.Field;
import java.lang.reflect.Method;
import java.util.function.Supplier;

public class SyncShearableDataMessage
{
    private final boolean isSheared;
    private final int lastSheared;
    private final int entityId;
    String worldKey;

    public SyncShearableDataMessage(boolean isSheared, int lastSheared, int entityId, String worldKey)
    {
        this.isSheared = isSheared;
        this.lastSheared = lastSheared;
        this.entityId = entityId;
    }

    public static void encode(SyncShearableDataMessage msg, FriendlyByteBuf buffer)
    {
        buffer.writeBoolean(msg.isSheared);
        buffer.writeInt(msg.lastSheared);
        buffer.writeInt(msg.entityId);
        buffer.writeUtf(msg.worldKey);
    }

    public static SyncShearableDataMessage decode(FriendlyByteBuf buffer)
    {
        return new SyncShearableDataMessage(buffer.readBoolean(), buffer.readInt(), buffer.readInt(), buffer.readUtf());
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
            CLIENT_LEVEL = ObfuscationReflectionHelper.findField(MINECRAFT, "f_91073_");
        } catch (Exception ignored) {}
    }

    public static void handle(SyncShearableDataMessage message, Supplier<NetworkEvent.Context> contextSupplier)
    {
        NetworkEvent.Context context = contextSupplier.get();
        if (context.getDirection().getReceptionSide().isClient())
        {
            context.enqueueWork(() ->
            {
                try
                {
                    Level level = (context.getDirection().getReceptionSide().isClient() && ((Level) CLIENT_LEVEL.get(GET_INSTANCE.invoke(null))).dimension().location().toString().equals(message.worldKey))
                            ? (Level) CLIENT_LEVEL.get(GET_INSTANCE.invoke(null))
                            : ((MinecraftServer) LogicalSidedProvider.WORKQUEUE.get(LogicalSide.SERVER)).getLevel(ResourceKey.create(Registry.DIMENSION_REGISTRY, new ResourceLocation(message.worldKey)));
                    if (level != null)
                    {
                        Entity entity = level.getEntity(message.entityId);
                        if (entity != null)
                        {
                            entity.getCapability(ModCapabilities.SHEARABLE_FUR).ifPresent(cap ->
                            {
                                cap.setSheared(message.isSheared);
                                cap.setLastSheared(message.lastSheared);
                            });
                        }
                    }
                } catch (Exception ignored) {}
            });
        }
        context.setPacketHandled(true);
    }
}
