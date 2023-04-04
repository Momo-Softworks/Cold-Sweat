package dev.momostudios.coldsweat.common.event;

import dev.momostudios.coldsweat.common.capability.ModCapabilities;
import dev.momostudios.coldsweat.core.event.TaskScheduler;
import dev.momostudios.coldsweat.core.network.ColdSweatPacketHandler;
import dev.momostudios.coldsweat.core.network.message.SyncShearableDataMessage;
import dev.momostudios.coldsweat.util.config.ConfigSettings;
import dev.momostudios.coldsweat.util.registries.ModItems;
import dev.momostudios.coldsweat.util.world.WorldHelper;
import net.minecraft.core.particles.ParticleTypes;
import net.minecraft.network.protocol.game.ClientboundEntityEventPacket;
import net.minecraft.server.level.ServerChunkCache;
import net.minecraft.sounds.SoundEvents;
import net.minecraft.sounds.SoundSource;
import net.minecraft.world.Difficulty;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.ai.behavior.BehaviorUtils;
import net.minecraft.world.entity.ai.goal.WrappedGoal;
import net.minecraft.world.entity.ai.memory.MemoryModuleType;
import net.minecraft.world.entity.animal.goat.Goat;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.entity.schedule.Activity;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.Items;
import net.minecraftforge.event.entity.EntityJoinWorldEvent;
import net.minecraftforge.event.entity.living.LivingEvent;
import net.minecraftforge.event.entity.player.PlayerInteractEvent;
import net.minecraftforge.eventbus.api.SubscribeEvent;
import net.minecraftforge.fml.common.Mod;
import net.minecraftforge.network.PacketDistributor;
import oshi.util.tuples.Triplet;

@Mod.EventBusSubscriber
public class GoatFurHandler
{
    @SubscribeEvent
    public static void onShearGoat(PlayerInteractEvent.EntityInteract event)
    {
        Entity entity = event.getTarget();
        Player player = event.getPlayer();
        ItemStack stack = event.getItemStack();

        if (entity instanceof Goat goat && !goat.level.isClientSide && stack.getItem() == Items.SHEARS)
        {
            goat.getCapability(ModCapabilities.SHEARABLE_FUR).ifPresent(cap ->
            {
                if (cap.isSheared())
                {   event.setResult(PlayerInteractEvent.Result.DENY);
                    return;
                }

                // Use shears
                player.swing(event.getHand(), true);
                stack.hurtAndBreak(1, event.getPlayer(), (p) -> p.broadcastBreakEvent(event.getHand()));
                // Play sound
                goat.level.playSound(null, goat, SoundEvents.SHEEP_SHEAR, SoundSource.NEUTRAL, 1.0F, 1.0F);

                // Spawn item
                WorldHelper.entityDropItem(goat, new ItemStack(ModItems.GOAT_FUR));

                // Random chance to ram the player when sheared
                if (!player.isCreative() && goat.level.getDifficulty() != Difficulty.PEACEFUL
                && !goat.level.isClientSide && goat.getRandom().nextDouble() < 0.4)
                {
                    // Set ram cooldown ticks
                    goat.getBrain().setMemory(MemoryModuleType.RAM_COOLDOWN_TICKS, 30);
                    // Stop active goals
                    goat.goalSelector.getRunningGoals().forEach(WrappedGoal::stop);

                    // Start lowering head
                    TaskScheduler.scheduleServer(() ->
                    {
                        ClientboundEntityEventPacket packet = new ClientboundEntityEventPacket(goat, (byte) 58);
                        ((ServerChunkCache) goat.level.getChunkSource()).broadcastAndSend(goat, packet);
                    }, 5);

                    // Look at player
                    BehaviorUtils.lookAtEntity(goat, player);
                    // Stop walking
                    goat.getBrain().eraseMemory(MemoryModuleType.WALK_TARGET);

                    // Set ram target to player pos
                    goat.getBrain().setMemory(MemoryModuleType.RAM_TARGET, player.position());
                    TaskScheduler.scheduleServer(() ->
                    {
                        if (player.distanceTo(goat) <= 10)
                        {
                            goat.playSound(goat.isScreamingGoat() ? SoundEvents.GOAT_SCREAMING_PREPARE_RAM : SoundEvents.GOAT_PREPARE_RAM, 1.0F, 1.0F);
                            goat.getBrain().setMemory(MemoryModuleType.RAM_TARGET, player.position());
                        }
                    }, 30);

                    // Trigger ram
                    goat.getBrain().setActiveActivityIfPossible(Activity.RAM);
                }

                // Set sheared
                cap.setSheared(true);
                cap.setLastSheared(goat.tickCount);
                syncData(goat);
                event.setResult(PlayerInteractEvent.Result.ALLOW);
            });
        }
    }

    // Regrow goat fur
    @SubscribeEvent
    public static void onGoatTick(LivingEvent.LivingUpdateEvent event)
    {
        Entity entity = event.getEntity();
        Triplet<Integer, Integer, Double> furConfig = ConfigSettings.GOAT_FUR_TIMINGS.get();
        // Entity is goat, current tick is a multiple of the regrow time, and random chance succeeds
        if (entity instanceof Goat goat && !goat.level.isClientSide && goat.tickCount % furConfig.getA() == 0 && Math.random() < furConfig.getC())
        {
            goat.getCapability(ModCapabilities.SHEARABLE_FUR).ifPresent(cap ->
            {
                // Growth cooldown has passed and goat is sheared
                if (goat.tickCount - cap.lastSheared() >= furConfig.getB() && cap.isSheared())
                {
                    WorldHelper.playEntitySound(SoundEvents.WOOL_HIT, goat, goat.getSoundSource(), 0.5f, 0.6f);
                    WorldHelper.playEntitySound(SoundEvents.LLAMA_SWAG, goat, goat.getSoundSource(), 0.5f, 0.8f);

                    // Spawn particles
                    WorldHelper.spawnParticleBatch(goat.level, ParticleTypes.SPIT, goat.getX(), goat.getY() + goat.getBbHeight() / 2, goat.getZ(), 0.5f, 0.5f, 0.5f, 10, 0.05f);
                    // Set not sheared
                    cap.setSheared(false);
                    syncData(goat);
                }
            });
        }
    }

    @SubscribeEvent
    public static void onEntityLoaded(EntityJoinWorldEvent event)
    {
        Entity entity = event.getEntity();
        if (entity instanceof Goat goat && !goat.level.isClientSide)
        {   TaskScheduler.scheduleServer(() -> syncData(goat), 20);
        }
    }

    public static void syncData(Goat goat)
    {
        goat.getCapability(ModCapabilities.SHEARABLE_FUR).ifPresent(cap ->
        {
            if (!goat.level.isClientSide)
                ColdSweatPacketHandler.INSTANCE.send(PacketDistributor.TRACKING_ENTITY.with(() -> goat), new SyncShearableDataMessage(cap.isSheared(), cap.lastSheared(), goat.getId(), goat.level.dimension().location().toString()));
        });
    }
}
