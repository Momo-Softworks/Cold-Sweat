package dev.momostudios.coldsweat.common.event;

import dev.momostudios.coldsweat.ColdSweat;
import dev.momostudios.coldsweat.common.capability.FurCap;
import dev.momostudios.coldsweat.common.capability.IShearableCap;
import dev.momostudios.coldsweat.common.capability.ModCapabilities;
import dev.momostudios.coldsweat.core.network.ColdSweatPacketHandler;
import dev.momostudios.coldsweat.core.network.message.SyncShearableDataMessage;
import dev.momostudios.coldsweat.config.ConfigSettings;
import dev.momostudios.coldsweat.util.compat.CompatManager;
import dev.momostudios.coldsweat.util.registries.ModItems;
import dev.momostudios.coldsweat.util.serialization.Triplet;
import dev.momostudios.coldsweat.util.world.WorldHelper;
import net.minecraft.entity.AgeableEntity;
import net.minecraft.entity.Entity;
import net.minecraft.entity.LivingEntity;
import net.minecraft.entity.passive.horse.AbstractChestedHorseEntity;
import net.minecraft.entity.passive.horse.LlamaEntity;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.entity.player.ServerPlayerEntity;
import net.minecraft.item.ItemStack;
import net.minecraft.nbt.CompoundNBT;
import net.minecraft.particles.ParticleTypes;
import net.minecraft.util.*;
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
public class FurHandler
{
    // Regrow fur
    @SubscribeEvent
    public static void onShearableEntityTick(LivingEvent.LivingUpdateEvent event)
    {
        if (!isShearable(event.getEntityLiving())) return;
        if (event.getEntityLiving() instanceof AgeableEntity)
        {
            AgeableEntity entity = (AgeableEntity) event.getEntityLiving();
            Triplet<Integer, Integer, Double> furConfig = ConfigSettings.LLAMA_FUR_TIMINGS.get();
            // Entity is shearable, current tick is a multiple of the regrow time, and random chance succeeds
            if (!entity.level.isClientSide && entity.tickCount % furConfig.getFirst() == 0 && Math.random() < furConfig.getThird())
            {
                entity.getCapability(ModCapabilities.SHEARABLE_FUR).ifPresent(cap ->
                {
                    // Growth cooldown has passed and llama is sheared
                    if (entity.getAge() - cap.lastSheared() >= furConfig.getSecond() && cap.isSheared())
                    {
                        WorldHelper.playEntitySound(SoundEvents.WOOL_HIT, entity, entity.getSoundSource(), 0.5f, 0.6f);
                        WorldHelper.playEntitySound(SoundEvents.LLAMA_SWAG, entity, entity.getSoundSource(), 0.5f, 0.8f);

                        // Spawn particles
                        WorldHelper.spawnParticleBatch(entity.level, ParticleTypes.SPIT, entity.getX(), entity.getY() + entity.getBbHeight() / 2, entity.getZ(), 0.5f, 0.5f, 0.5f, 10, 0.05f);
                        // Set not sheared
                        cap.setSheared(false);
                        syncData(entity, null);
                    }
                });
            }
        }
    }

    @SubscribeEvent
    public static void onEntitySheared(PlayerInteractEvent.EntityInteractSpecific event)
    {
        Entity entity = event.getTarget();
        PlayerEntity player = event.getPlayer();
        Hand hand = event.getHand();
        ItemStack stack = player.getItemInHand(hand);
        if (!entity.level.isClientSide && isShearable(entity) && !(entity instanceof AbstractChestedHorseEntity))
        {
            entity.getCapability(ModCapabilities.SHEARABLE_FUR).ifPresent(cap ->
            {
                if (!cap.isSheared())
                {   // Use shears
                    player.swing(hand, true);
                    stack.hurtAndBreak(1, player, (p) -> p.broadcastBreakEvent(hand));
                    // Play sound
                    entity.level.playSound(null, entity, SoundEvents.SHEEP_SHEAR, SoundCategory.NEUTRAL, 1.0F, 1.0F);
                    // Spawn item
                    WorldHelper.entityDropItem(entity, new ItemStack(ModItems.FUR));
                    // Set sheared
                    cap.setSheared(true);
                    cap.setLastSheared(entity.tickCount);
                    FurHandler.syncData(entity, null);
                }
            });
        }
    }

    @SubscribeEvent
    public static void onEntityLoaded(PlayerEvent.StartTracking event)
    {
        if (event.getEntity() instanceof ServerPlayerEntity && isShearable(event.getTarget()))
        {   syncData(event.getTarget(), (ServerPlayerEntity) event.getEntity());
        }
    }

    public static void syncData(Entity llama, ServerPlayerEntity player)
    {
        if (!llama.level.isClientSide)
        {   llama.getCapability(ModCapabilities.SHEARABLE_FUR).ifPresent(cap ->
            {   ColdSweatPacketHandler.INSTANCE.send(player != null ? PacketDistributor.PLAYER.with(() -> player)
                                                                    : PacketDistributor.TRACKING_ENTITY.with(() -> llama),
                                                     new SyncShearableDataMessage(cap.isSheared(), cap.lastSheared(), llama.getId()));
            });
        }
    }

    @SubscribeEvent
    public static void attachCapabilityToEntityHandler(AttachCapabilitiesEvent<Entity> event)
    {
        if (isShearable(event.getObject()))
        {
            // Make a new capability instance to attach to the entity
            IShearableCap cap = new FurCap();
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
                    {   return capOptional.cast();
                    }
                    return LazyOptional.empty();
                }

                @Override
                public CompoundNBT serializeNBT()
                {   return cap.serializeNBT();
                }

                @Override
                public void deserializeNBT(CompoundNBT nbt)
                {   cap.deserializeNBT(nbt);
                }
            };
            event.addCapability(new ResourceLocation(ColdSweat.MOD_ID, "fur"), provider);
        }
    }

    public static boolean isShearable(Entity entity)
    {   return entity instanceof LlamaEntity || CompatManager.isGoat(entity);
    }
}
