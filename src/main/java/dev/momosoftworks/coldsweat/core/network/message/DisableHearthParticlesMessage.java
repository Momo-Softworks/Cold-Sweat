package dev.momosoftworks.coldsweat.core.network.message;

import dev.momosoftworks.coldsweat.util.ClientOnlyHelper;
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
import net.minecraftforge.network.NetworkEvent;

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

    public static void handle(DisableHearthParticlesMessage message, Supplier<NetworkEvent.Context> contextSupplier)
    {
        NetworkEvent.Context context = contextSupplier.get();
        context.enqueueWork(() ->
        {
            try
            {
                Level level = (context.getDirection().getReceptionSide().isClient() && ClientOnlyHelper.getClientLevel().dimension().location().toString().equals(message.worldKey))
                        ? ClientOnlyHelper.getClientLevel()
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
