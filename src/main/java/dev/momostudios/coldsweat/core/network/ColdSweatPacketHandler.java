package dev.momostudios.coldsweat.core.network;

import dev.momostudios.coldsweat.ColdSweat;
import dev.momostudios.coldsweat.core.network.message.*;
import dev.momostudios.coldsweat.util.world.WorldHelper;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraft.world.level.chunk.ChunkAccess;
import net.minecraft.world.level.chunk.LevelChunk;
import net.minecraftforge.network.NetworkRegistry;
import net.minecraftforge.network.PacketDistributor;
import net.minecraftforge.network.simple.SimpleChannel;

public class ColdSweatPacketHandler
{
    private static final String PROTOCOL_VERSION = "0.1.2";
    public static final SimpleChannel INSTANCE = NetworkRegistry.newSimpleChannel(
            new ResourceLocation(ColdSweat.MOD_ID, "main"),
            () -> PROTOCOL_VERSION,
            PROTOCOL_VERSION::equals,
            PROTOCOL_VERSION::equals
    );

    public static void init()
    {
        INSTANCE.registerMessage(0, TemperatureSyncMessage.class, TemperatureSyncMessage::encode, TemperatureSyncMessage::decode, TemperatureSyncMessage::handle);
        INSTANCE.registerMessage(1, TempModifiersSyncMessage.class, TempModifiersSyncMessage::encode, TempModifiersSyncMessage::decode, TempModifiersSyncMessage::handle);
        INSTANCE.registerMessage(2, SyncConfigSettingsMessage.class, SyncConfigSettingsMessage::encode, SyncConfigSettingsMessage::decode, SyncConfigSettingsMessage::handle);
        INSTANCE.registerMessage(3, ClientConfigAskMessage.class, ClientConfigAskMessage::encode, ClientConfigAskMessage::decode, ClientConfigAskMessage::handle);
        INSTANCE.registerMessage(4, PlaySoundMessage.class, PlaySoundMessage::encode, PlaySoundMessage::decode, PlaySoundMessage::handle);
        INSTANCE.registerMessage(5, BlockDataUpdateMessage.class, BlockDataUpdateMessage::encode, BlockDataUpdateMessage::decode, BlockDataUpdateMessage::handle);
        INSTANCE.registerMessage(6, HearthResetMessage.class, HearthResetMessage::encode, HearthResetMessage::decode, HearthResetMessage::handle);
        INSTANCE.registerMessage(7, DisableHearthParticlesMessage.class, DisableHearthParticlesMessage::encode, DisableHearthParticlesMessage::decode, DisableHearthParticlesMessage::handle);
        INSTANCE.registerMessage(8, ParticleBatchMessage.class, ParticleBatchMessage::encode, ParticleBatchMessage::decode, ParticleBatchMessage::handle);
        INSTANCE.registerMessage(9, SyncShearableDataMessage.class, SyncShearableDataMessage::encode, SyncShearableDataMessage::decode, SyncShearableDataMessage::handle);
        INSTANCE.registerMessage(10, ChameleonEatMessage.class, ChameleonEatMessage::encode, ChameleonEatMessage::decode, ChameleonEatMessage::handle);
        INSTANCE.registerMessage(11, SyncForgeDataMessage.class, SyncForgeDataMessage::encode, SyncForgeDataMessage::decode, SyncForgeDataMessage::handle);
    }

    public static void syncBlockEntityData(BlockEntity be)
    {
        if (be.getLevel() == null || be.getLevel().isClientSide) return;

        ChunkAccess ichunk = WorldHelper.getChunk(be.getLevel(), be.getBlockPos());
        if (ichunk instanceof LevelChunk chunk)
        {   INSTANCE.send(PacketDistributor.TRACKING_CHUNK.with(() -> chunk), new BlockDataUpdateMessage(be));
        }
    }
}
