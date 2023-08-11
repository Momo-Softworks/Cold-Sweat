package dev.momostudios.coldsweat.common.event;

import dev.momostudios.coldsweat.ColdSweat;
import dev.momostudios.coldsweat.common.capability.GoatFurCap;
import dev.momostudios.coldsweat.common.capability.IShearableCap;
import dev.momostudios.coldsweat.common.capability.ModCapabilities;
import dev.momostudios.coldsweat.core.network.ColdSweatPacketHandler;
import dev.momostudios.coldsweat.core.network.message.SyncShearableDataMessage;
import dev.momostudios.coldsweat.config.ConfigSettings;
import dev.momostudios.coldsweat.util.serialization.Triplet;
import dev.momostudios.coldsweat.util.registries.ModItems;
import dev.momostudios.coldsweat.util.world.WorldHelper;
import net.minecraft.entity.Entity;
import net.minecraft.entity.passive.horse.LlamaEntity;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.entity.player.ServerPlayerEntity;
import net.minecraft.item.ItemStack;
import net.minecraft.item.Items;
import net.minecraft.nbt.CompoundNBT;
import net.minecraft.particles.ParticleTypes;
import net.minecraft.util.Direction;
import net.minecraft.util.ResourceLocation;
import net.minecraft.util.SoundCategory;
import net.minecraft.util.SoundEvents;
import net.minecraftforge.common.capabilities.Capability;
import net.minecraftforge.common.capabilities.ICapabilityProvider;
import net.minecraftforge.common.capabilities.ICapabilitySerializable;
import net.minecraftforge.common.util.LazyOptional;
import net.minecraftforge.event.AttachCapabilitiesEvent;
import net.minecraftforge.event.entity.living.LivingEvent;
import net.minecraftforge.event.entity.player.PlayerEvent;
import net.minecraftforge.event.entity.player.PlayerInteractEvent;
import net.minecraftforge.eventbus.api.SubscribeEvent;
import net.minecraftforge.fml.common.Mod;
import net.minecraftforge.fml.network.PacketDistributor;

import javax.annotation.Nonnull;
import javax.annotation.Nullable;

@Mod.EventBusSubscriber
public class LlamaFurHandler
{
    @SubscribeEvent
    public static void onShearLlama(PlayerInteractEvent.EntityInteract event)
    {
        Entity entity = event.getTarget();
        PlayerEntity player = event.getPlayer();
        ItemStack stack = event.getItemStack();

        if (entity instanceof LlamaEntity)
        {
            LlamaEntity llama = (LlamaEntity) entity;
            if (llama.isBaby() || stack.getItem() != Items.SHEARS) return;

            llama.getCapability(ModCapabilities.SHEARABLE_FUR).ifPresent(cap ->
            {
                if (cap.isSheared())
                {   event.setResult(PlayerInteractEvent.Result.DENY);
                    event.setCanceled(true);
                    return;
                }

                if (llama.level.isClientSide) return;

                // Use shears
                player.swing(event.getHand(), true);
                stack.hurtAndBreak(1, event.getPlayer(), (p) -> p.broadcastBreakEvent(event.getHand()));
                // Play sound
                llama.level.playSound(null, llama, SoundEvents.SHEEP_SHEAR, SoundCategory.NEUTRAL, 1.0F, 1.0F);

                // Spawn item
                WorldHelper.entityDropItem(llama, new ItemStack(ModItems.LLAMA_FUR));

                // Set sheared
                cap.setSheared(true);
                cap.setLastSheared(llama.tickCount);
                syncData(llama, null);
                event.setResult(PlayerInteractEvent.Result.ALLOW);
            });
        }
    }

    // Regrow goat fur
    @SubscribeEvent
    public static void onLlamaTick(LivingEvent.LivingUpdateEvent event)
    {
        Entity entity = event.getEntity();
        if (!(entity instanceof LlamaEntity)) return;

        LlamaEntity llama = (LlamaEntity) entity;

        Triplet<Integer, Integer, Double> furConfig = ConfigSettings.LLAMA_FUR_TIMINGS.get();
        // Entity is goat, current tick is a multiple of the regrow time, and random chance succeeds
        if (!llama.level.isClientSide && llama.tickCount % furConfig.getFirst() == 0 && Math.random() < furConfig.getThird())
        {
            llama.getCapability(ModCapabilities.SHEARABLE_FUR).ifPresent(cap ->
            {
                // Growth cooldown has passed and goat is sheared
                if (llama.tickCount - cap.lastSheared() >= furConfig.getSecond() && cap.isSheared())
                {
                    WorldHelper.playEntitySound(SoundEvents.WOOL_HIT, llama, llama.getSoundSource(), 0.5f, 0.6f);
                    WorldHelper.playEntitySound(SoundEvents.LLAMA_SWAG, llama, llama.getSoundSource(), 0.5f, 0.8f);

                    // Spawn particles
                    WorldHelper.spawnParticleBatch(llama.level, ParticleTypes.SPIT, llama.getX(), llama.getY() + llama.getBbHeight() / 2, llama.getZ(), 0.5f, 0.5f, 0.5f, 10, 0.05f);
                    // Set not sheared
                    cap.setSheared(false);
                    syncData(llama, null);
                }
            });
        }
    }

    @SubscribeEvent
    public static void onEntityLoaded(PlayerEvent.StartTracking event)
    {
        if (event.getEntity() instanceof ServerPlayerEntity && event.getTarget() instanceof LlamaEntity)
        {   syncData((LlamaEntity) event.getTarget(), (ServerPlayerEntity) event.getEntity());
        }
    }

    public static void syncData(LlamaEntity goat, ServerPlayerEntity player)
    {
        if (!goat.level.isClientSide)
        {   goat.getCapability(ModCapabilities.SHEARABLE_FUR).ifPresent(cap ->
            {   ColdSweatPacketHandler.INSTANCE.send(player != null ? PacketDistributor.PLAYER.with(() -> player)
                                                                    : PacketDistributor.TRACKING_ENTITY.with(() -> goat),
                                                     new SyncShearableDataMessage(cap.isSheared(), cap.lastSheared(), goat.getId()));
            });
        }
    }

    @SubscribeEvent
    public static void attachCapabilityToEntityHandler(AttachCapabilitiesEvent<Entity> event)
    {
        if (event.getObject() instanceof LlamaEntity)
        {
            // Make a new capability instance to attach to the entity
            IShearableCap cap = new GoatFurCap();
            // Optional that holds the capability instance
            LazyOptional<IShearableCap> capOptional = LazyOptional.of(() -> cap);
            Capability<IShearableCap> capability = ModCapabilities.SHEARABLE_FUR;

            ICapabilityProvider provider = new ICapabilitySerializable<CompoundNBT>()
            {
                @Nonnull
                @Override
                public <T> LazyOptional<T> getCapability(@Nonnull Capability<T> cap, @Nullable Direction direction)
                {
                    // If the requested cap is the temperature cap, return the temperature cap
                    if (cap == capability)
                    {
                        return capOptional.cast();
                    }
                    return LazyOptional.empty();
                }

                @Override
                public CompoundNBT serializeNBT()
                {
                    return cap.serializeNBT();
                }

                @Override
                public void deserializeNBT(CompoundNBT nbt)
                {
                    cap.deserializeNBT(nbt);
                }
            };

            event.addCapability(new ResourceLocation(ColdSweat.MOD_ID, "goat_fur"), provider);
        }
    }
}
