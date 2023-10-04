package com.momosoftworks.coldsweat.common.item;

import com.momosoftworks.coldsweat.api.temperature.modifier.WaterskinTempModifier;
import com.momosoftworks.coldsweat.api.util.Temperature;
import com.momosoftworks.coldsweat.client.event.TooltipHandler;
import com.momosoftworks.coldsweat.config.ClientSettingsConfig;
import com.momosoftworks.coldsweat.core.event.TaskScheduler;
import com.momosoftworks.coldsweat.core.init.ItemInit;
import com.momosoftworks.coldsweat.core.network.ColdSweatPacketHandler;
import com.momosoftworks.coldsweat.core.network.message.ParticleBatchMessage;
import com.momosoftworks.coldsweat.config.ConfigSettings;
import com.momosoftworks.coldsweat.util.math.CSMath;
import com.momosoftworks.coldsweat.util.registries.ModItems;
import com.momosoftworks.coldsweat.util.world.WorldHelper;
import net.minecraft.ChatFormatting;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.core.particles.ParticleTypes;
import net.minecraft.network.chat.Component;
import net.minecraft.sounds.SoundEvents;
import net.minecraft.sounds.SoundSource;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.InteractionResultHolder;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.TooltipFlag;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.DispenserBlock;
import net.minecraft.world.level.chunk.ChunkAccess;
import net.minecraft.world.level.chunk.LevelChunk;
import net.minecraft.world.phys.AABB;
import net.minecraftforge.common.MinecraftForge;
import net.minecraftforge.event.TickEvent;
import net.minecraftforge.eventbus.api.SubscribeEvent;
import net.minecraftforge.network.PacketDistributor;
import org.jetbrains.annotations.Nullable;

import java.util.ArrayList;
import java.util.List;
import java.util.Random;

public class FilledWaterskinItem extends Item
{
    public FilledWaterskinItem()
    {
        super(new Properties().stacksTo(1).craftRemainder(ItemInit.WATERSKIN.get()));

        DispenserBlock.registerBehavior(this, (source, stack) ->
        {
            BlockPos pos = source.getPos().relative(source.getBlockState().getValue(DispenserBlock.FACING));
            Level level = source.getLevel();
            ChunkAccess chunk = WorldHelper.getChunk(level, pos);
            double itemTemp = stack.getOrCreateTag().getDouble("temperature");

            if (chunk == null) return stack;

            // Play sound
            level.playLocalSound(pos.getX(), pos.getY(), pos.getZ(), SoundEvents.AMBIENT_UNDERWATER_EXIT,
                    SoundSource.PLAYERS, 1, (float) ((Math.random() / 5) + 0.9), false);

            // Spawn particles
            Random rand = new Random();
            for (int i = 0; i < 6; i++)
            {
                TaskScheduler.scheduleServer(() ->
                {
                    ParticleBatchMessage particles = new ParticleBatchMessage();
                    for (int p = 0; p < rand.nextInt(5) + 5; p++)
                    {
                        particles.addParticle(ParticleTypes.FALLING_WATER,
                                new ParticleBatchMessage.ParticlePlacement(pos.getX() + rand.nextDouble(),
                                                                           pos.getY() + rand.nextDouble(),
                                                                           pos.getZ() + rand.nextDouble(), 0, 0, 0));
                    }
                    ColdSweatPacketHandler.INSTANCE.send(PacketDistributor.TRACKING_CHUNK.with(() -> (LevelChunk) chunk), particles);
                }, i);
            }

            final AABB[] aabb = { new AABB(pos).inflate(0.5) };

            // Spawn a hitbox that falls at the same rate as the particles and gives players below the waterskin effect
            new Object()
            {
                double acceleration = 0;
                int tick = 0;
                // Track affected players to prevent duplicate effects
                List<Player> affectedPlayers = new ArrayList<>();

                void start()
                {   MinecraftForge.EVENT_BUS.register(this);
                }

                @SubscribeEvent
                public void onTick(TickEvent.LevelTickEvent event)
                {
                    if (event.level.isClientSide == level.isClientSide && event.phase == TickEvent.Phase.START)
                    {
                        // Temperature of waterskin weakens over time
                        double waterTemp = CSMath.blend(itemTemp, itemTemp / 5, tick, 20, 100);

                        // Move the box down at the speed of gravity
                        AABB movedBox;
                        aabb[0] = movedBox = aabb[0].move(0, -acceleration, 0);

                        // If there's ground, stop
                        BlockPos pos = BlockPos.containing(movedBox.minX, movedBox.minY, movedBox.minZ);
                        if (WorldHelper.isSpreadBlocked(level, chunk.getBlockState(pos), pos, Direction.DOWN, Direction.DOWN))
                        {   MinecraftForge.EVENT_BUS.unregister(this);
                            return;
                        }

                        // Apply the waterskin modifier to all entities in the box
                        level.getEntitiesOfClass(Player.class, movedBox).forEach(player ->
                        {
                            if (!affectedPlayers.contains(player))
                            {   // Apply the effect and store the player
                                Temperature.addModifier(player, new WaterskinTempModifier(waterTemp).expires(0), Temperature.Type.CORE, true);
                                affectedPlayers.add(player);
                            }
                        });

                        // Increase the speed of the box
                        acceleration += 0.0052;
                        tick++;

                        // Expire after 5 seconds
                        if (tick > 100)
                        {
                            MinecraftForge.EVENT_BUS.unregister(this);
                        }
                    }
                }
            }.start();

            return getEmpty(stack);
        });
    }

    @Override
    public void inventoryTick(ItemStack itemstack, Level world, Entity entity, int slot, boolean isSelected)
    {
        super.inventoryTick(itemstack, world, entity, slot, isSelected);
        if (entity.tickCount % 5 == 0 && entity instanceof Player player)
        {
            double itemTemp = itemstack.getOrCreateTag().getDouble("temperature");
            if (itemTemp != 0 && slot <= 8 || player.getOffhandItem().equals(itemstack))
            {
                double temp = 0.04 * ConfigSettings.TEMP_RATE.get() * CSMath.getSign(itemTemp);
                double newTemp = itemTemp - temp * 2;
                if (CSMath.withinRange(newTemp, -1, 1)) newTemp = 0;

                itemstack.getOrCreateTag().putDouble("temperature", newTemp);

                Temperature.addModifier(player, new WaterskinTempModifier(temp).expires(5), Temperature.Type.CORE, true);
            }
        }
    }

    @Override
    public InteractionResultHolder<ItemStack> use(Level level, Player player, InteractionHand hand)
    {
        InteractionResultHolder<ItemStack> ar = super.use(level, player, hand);
        ItemStack itemstack = ar.getObject();

        double amount = itemstack.getOrCreateTag().getDouble("temperature") * (ConfigSettings.WATERSKIN_STRENGTH.get() / 50d);
        Temperature.addModifier(player, new WaterskinTempModifier(amount).expires(0), Temperature.Type.CORE, true);

        // Play empty sound
        level.playLocalSound(player.getX(), player.getY(), player.getZ(), SoundEvents.AMBIENT_UNDERWATER_EXIT,
                SoundSource.PLAYERS, 1, (float) ((Math.random() / 5) + 0.9), false);

        // Create empty waterskin item
        ItemStack emptyStack = getEmpty(itemstack);

        // Add the item to the player's inventory
        if (player.getInventory().contains(emptyStack))
        {   player.addItem(emptyStack);
            player.setItemInHand(hand, ItemStack.EMPTY);
        }
        else
        {
            player.setItemInHand(hand, emptyStack);
        }

        player.swing(hand);

        // spawn falling water particles
        Random rand = new Random();
        for (int i = 0; i < 6; i++)
        {
            TaskScheduler.scheduleClient(() ->
            {
                for (int p = 0; p < rand.nextInt(5) + 5; p++)
                {
                    level.addParticle(ParticleTypes.FALLING_WATER,
                            player.getX() + rand.nextFloat() * player.getBbWidth() - (player.getBbWidth() / 2),
                            player.getY() + player.getBbHeight() + rand.nextFloat() * 0.5,
                            player.getZ() + rand.nextFloat() * player.getBbWidth() - (player.getBbWidth() / 2), 0.3, 0.3, 0.3);
                }
            }, i);
        }
        player.clearFire();

        player.getCooldowns().addCooldown(ModItems.FILLED_WATERSKIN, 10);
        player.getCooldowns().addCooldown(ModItems.WATERSKIN, 10);

        return ar;
    }

    public static ItemStack getEmpty(ItemStack stack)
    {
        if (stack.getItem() instanceof FilledWaterskinItem)
        {
            ItemStack emptyWaterskin = new ItemStack(ModItems.WATERSKIN);

            // Preserve NBT (except temperature)
            emptyWaterskin.setTag(stack.getTag());
            emptyWaterskin.removeTagKey("temperature");
            return emptyWaterskin;
        }
        return stack;
    }

    @Override
    public void appendHoverText(ItemStack stack, @Nullable Level level, List<Component> tooltip, TooltipFlag advanced)
    {
        double temp = stack.getOrCreateTag().getDouble("temperature");
        // Info tooltip for hotbar functionality
        tooltip.add(Component.empty());
        tooltip.add(Component.translatable("tooltip.cold_sweat.hotbar").withStyle(ChatFormatting.GRAY));
        tooltip.add(Component.translatable("tooltip.cold_sweat.temperature_effect", (CSMath.getSign(temp) >= 0 ? "+" : "-") + (temp != 0 ? 0.8 * ConfigSettings.TEMP_RATE.get() : 0))
                            .withStyle(temp > 0 ? TooltipHandler.HOT : temp < 0 ? TooltipHandler.COLD : ChatFormatting.WHITE));

        // Tooltip to display temperature
        boolean celsius = ClientSettingsConfig.getInstance().isCelsius();
        String color = temp == 0 ? "7" : (temp < 0 ? "9" : "c");
        String tempUnits = celsius ? "C" : "F";
        temp = temp / 2 + 95;
        if (celsius) temp = Temperature.convertUnits(temp, Temperature.Units.F, Temperature.Units.C, true);
        temp += ClientSettingsConfig.getInstance().getTempOffset() / 2.0;

        tooltip.add(1, Component.literal("§7" + Component.translatable(
                "item.cold_sweat.waterskin.filled").getString() + " (§" + color + (int) temp + " °" + tempUnits + "§7)§r"));

        super.appendHoverText(stack, level, tooltip, advanced);
    }

    @Override
    public boolean shouldCauseReequipAnimation(ItemStack oldStack, ItemStack newStack, boolean slotChanged)
    {   return slotChanged;
    }

    public String getDescriptionId()
    {
        return Component.translatable("item.cold_sweat.waterskin").getString();
    }
}
