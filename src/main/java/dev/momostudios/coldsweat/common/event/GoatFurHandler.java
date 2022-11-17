package dev.momostudios.coldsweat.common.event;

import dev.momostudios.coldsweat.ColdSweat;
import dev.momostudios.coldsweat.util.config.ValueLoader;
import dev.momostudios.coldsweat.util.config.ConfigSettings;
import dev.momostudios.coldsweat.util.registries.ModItems;
import dev.momostudios.coldsweat.util.world.WorldHelper;
import it.unimi.dsi.fastutil.objects.Object2IntMap;
import net.minecraft.network.syncher.EntityDataAccessor;
import net.minecraft.network.syncher.EntityDataSerializers;
import net.minecraft.network.syncher.SynchedEntityData;
import net.minecraft.sounds.SoundEvents;
import net.minecraft.sounds.SoundSource;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.animal.goat.Goat;
import net.minecraft.world.entity.item.ItemEntity;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.Items;
import net.minecraftforge.event.entity.EntityJoinWorldEvent;
import net.minecraftforge.event.entity.EntityLeaveWorldEvent;
import net.minecraftforge.event.entity.living.LivingEvent;
import net.minecraftforge.event.entity.player.PlayerInteractEvent;
import net.minecraftforge.eventbus.api.SubscribeEvent;
import net.minecraftforge.fml.common.Mod;
import net.minecraftforge.fml.util.ObfuscationReflectionHelper;

import java.lang.reflect.Field;
import java.util.Random;

@Mod.EventBusSubscriber
public class GoatFurHandler
{
    // locate the entity data registry
    static Field EID_POOL_FIELD = ObfuscationReflectionHelper.findField(SynchedEntityData.class, "f_135343_");
    static Object2IntMap<Class<? extends Entity>> ENTITY_ID_POOL;
    static
    {
        EID_POOL_FIELD.setAccessible(true);
        try
        {
            ENTITY_ID_POOL = (Object2IntMap<Class<? extends Entity>>) EID_POOL_FIELD.get(null);
        }
        catch (IllegalAccessException e)
        {
            ColdSweat.LOGGER.fatal("Error when registering \"GOAT_SHEARED\" data tracker");
            throw new RuntimeException(e);
        }
    }
    // register the GOAT_SHEARED entity data after the last ID in the registry via ENTITY_ID_POOL.getInt(Goat.class) + 1
    public static final ValueLoader<EntityDataAccessor<Boolean>> GOAT_SHEARED = new ValueLoader<>(() -> new EntityDataAccessor<>(ENTITY_ID_POOL.containsKey(Goat.class)
            // try to get the last data ID and add 1
            ? ENTITY_ID_POOL.getInt(Goat.class) + 1
            // else the default value is 18
            : 18, EntityDataSerializers.BOOLEAN));

    @SubscribeEvent
    public static void onShearGoat(PlayerInteractEvent.EntityInteract event)
    {
        Entity entity = event.getTarget();
        Player player = event.getPlayer();
        ItemStack stack = event.getItemStack();

        if (entity instanceof Goat && stack.getItem() == Items.SHEARS && !entity.getEntityData().get(GOAT_SHEARED.get()))
        {
            // use shears
            stack.hurtAndBreak(1, event.getPlayer(), (p) -> p.broadcastBreakEvent(event.getHand()));
            player.swing(event.getHand());
            // play sound
            entity.level.playSound(null, entity, SoundEvents.SHEEP_SHEAR, SoundSource.NEUTRAL, 1.0F, 1.0F);

            // spawn item
            Random rand = new Random();
            ItemEntity item = entity.spawnAtLocation(new ItemStack(ModItems.GOAT_FUR), 1f);
            if (item != null)
                item.setDeltaMovement(item.getDeltaMovement().add(((rand.nextFloat() - rand.nextFloat()) * 0.1F), (rand.nextFloat() * 0.05F), ((rand.nextFloat() - rand.nextFloat()) * 0.1F)));

            // set sheared
            entity.getEntityData().set(GOAT_SHEARED.get(), true);
            event.setResult(PlayerInteractEvent.Result.ALLOW);
        }
    }

    // Regrow goat fur
    @SubscribeEvent
    public static void onGoatTick(LivingEvent.LivingUpdateEvent event)
    {
        Entity entity = event.getEntity();
        if (entity instanceof Goat && !entity.level.isClientSide && entity.tickCount % ConfigSettings.GOAT_FUR_TIMINGS.get().getFirst() == 0
        && Math.random() < ConfigSettings.GOAT_FUR_TIMINGS.get().getSecond() && entity.getEntityData().get(GOAT_SHEARED.get()))
        {
            WorldHelper.playEntitySound(SoundEvents.WOOL_HIT, entity, SoundSource.NEUTRAL, 0.5f, 0.6f);
            WorldHelper.playEntitySound(SoundEvents.LLAMA_SWAG, entity, SoundSource.NEUTRAL, 0.5f, 0.8f);
            entity.getEntityData().set(GOAT_SHEARED.get(), false);
        }
    }

    @SubscribeEvent
    public static void onGoatSpawn(EntityJoinWorldEvent event)
    {
        Entity entity = event.getEntity();
        if (entity instanceof Goat)
        {
            entity.getEntityData().define(GOAT_SHEARED.get(), entity.getPersistentData().getBoolean("Sheared"));
        }
    }

    @SubscribeEvent
    public static void onGoatDespawn(EntityLeaveWorldEvent event)
    {
        Entity entity = event.getEntity();
        if (entity instanceof Goat)
        {
            entity.getPersistentData().putBoolean("Sheared", entity.getEntityData().get(GOAT_SHEARED.get()));
        }
    }
}
