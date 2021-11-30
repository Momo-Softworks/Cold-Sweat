package net.momostudios.coldsweat.core.network.message;

import net.minecraft.network.PacketBuffer;
import net.minecraftforge.fml.network.NetworkEvent;
import net.momostudios.coldsweat.config.ColdSweatConfig;

import java.util.function.Supplier;

public class ConfigSyncMessage
{
    int difficulty;
    double minTemp;
    double maxTemp;
    double rate;
    boolean fireRes;
    boolean iceRes;
    boolean damageScaling;
    boolean showAmbient;

    public ConfigSyncMessage(ColdSweatConfig config)
    {
        this.difficulty = config.difficulty();
        this.minTemp = config.minHabitable();
        this.maxTemp = config.maxHabitable();
        this.rate = config.rateMultiplier();
        this.fireRes = config.fireResistanceEffect();
        this.iceRes = config.iceResistanceEffect();
        this.damageScaling = config.damageScaling();
        this.showAmbient = config.showAmbient();
    }

    public ConfigSyncMessage(int difficulty, double minTemp, double maxTemp, double rate, boolean fireRes, boolean iceRes, boolean damageScaling, boolean showAmbient)
    {
        this.difficulty = difficulty;
        this.minTemp = minTemp;
        this.maxTemp = maxTemp;
        this.rate = rate;
        this.fireRes = fireRes;
        this.iceRes = iceRes;
        this.damageScaling = damageScaling;
        this.showAmbient = showAmbient;
    }

    public static void encode(ConfigSyncMessage message, PacketBuffer buffer)
    {
        buffer.writeInt(message.difficulty);
        buffer.writeDouble(message.minTemp);
        buffer.writeDouble(message.maxTemp);
        buffer.writeDouble(message.rate);
        buffer.writeBoolean(message.fireRes);
        buffer.writeBoolean(message.iceRes);
        buffer.writeBoolean(message.damageScaling);
        buffer.writeBoolean(message.showAmbient);
    }

    public static ConfigSyncMessage decode(PacketBuffer buffer)
    {
        return new ConfigSyncMessage(buffer.readInt(), buffer.readDouble(), buffer.readDouble(), buffer.readDouble(), buffer.readBoolean(), buffer.readBoolean(), buffer.readBoolean(), buffer.readBoolean());
    }

    public static void handle(ConfigSyncMessage message, Supplier<NetworkEvent.Context> contextSupplier)
    {
        NetworkEvent.Context context = contextSupplier.get();
        context.enqueueWork(() ->
        {
            ColdSweatConfig newConfig = new ColdSweatConfig();
            newConfig.setDifficulty(message.difficulty);
            newConfig.setMinHabitable(message.minTemp);
            newConfig.setMaxHabitable(message.maxTemp);
            newConfig.setRateMultiplier(message.rate);
            newConfig.setFireResistanceEffect(message.fireRes);
            newConfig.setIceResistanceEffect(message.iceRes);
            newConfig.setDamageScaling(message.damageScaling);
            newConfig.setShowAmbient(message.showAmbient);
            ColdSweatConfig.setConfigReference(newConfig);
        });
    }
}
