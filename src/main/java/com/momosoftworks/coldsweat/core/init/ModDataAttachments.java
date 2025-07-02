package com.momosoftworks.coldsweat.core.init;

import com.momosoftworks.coldsweat.ColdSweat;
import com.momosoftworks.coldsweat.common.capability.handler.EntityTempManager;
import com.momosoftworks.coldsweat.common.capability.shearing.IShearableCap;
import com.momosoftworks.coldsweat.common.capability.shearing.ShearableFurCap;
import com.momosoftworks.coldsweat.common.capability.temperature.EntityTempCap;
import com.momosoftworks.coldsweat.common.capability.temperature.ITemperatureCap;
import com.momosoftworks.coldsweat.common.capability.temperature.PlayerTempCap;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.animal.goat.Goat;
import net.minecraft.world.entity.player.Player;
import net.neoforged.neoforge.attachment.AttachmentType;
import net.neoforged.neoforge.registries.DeferredHolder;
import net.neoforged.neoforge.registries.DeferredRegister;
import net.neoforged.neoforge.registries.NeoForgeRegistries;

public class ModDataAttachments
{
    public static final DeferredRegister<AttachmentType<?>> DATA_ATTACHMENTS = DeferredRegister.create(NeoForgeRegistries.ATTACHMENT_TYPES, ColdSweat.MOD_ID);

    public static final DeferredHolder<AttachmentType<?>, AttachmentType<ITemperatureCap>> ENTITY_TEMPERATURE =
            (DeferredHolder) DATA_ATTACHMENTS.register("entity_temperature",
            () -> AttachmentType.serializable(entity ->
            {
                if (entity instanceof Player)
                {   return new PlayerTempCap();
                }
                else if (entity instanceof LivingEntity living && EntityTempManager.isTemperatureEnabled(living))
                {   return new EntityTempCap();
                }
                return null;
            }).build());

    public static final DeferredHolder<AttachmentType<?>, AttachmentType<IShearableCap>> SHEARABLE_FUR =
            (DeferredHolder)  DATA_ATTACHMENTS.register("shearable_fur",
            () -> AttachmentType.serializable(entity ->
            {
                if (entity instanceof Goat goat)
                {   return new ShearableFurCap();
                }
                return null;
            }).build());
}
