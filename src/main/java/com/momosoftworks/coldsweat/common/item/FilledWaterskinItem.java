package com.momosoftworks.coldsweat.common.item;

import com.momosoftworks.coldsweat.api.temperature.modifier.WaterskinTempModifier;
import com.momosoftworks.coldsweat.api.util.Temperature;
import com.momosoftworks.coldsweat.client.event.TooltipHandler;
import com.momosoftworks.coldsweat.config.ConfigSettings;
import com.momosoftworks.coldsweat.core.event.TaskScheduler;
import com.momosoftworks.coldsweat.core.init.ItemInit;
import com.momosoftworks.coldsweat.core.itemgroup.ColdSweatGroup;
import com.momosoftworks.coldsweat.core.network.ColdSweatPacketHandler;
import com.momosoftworks.coldsweat.core.network.message.ParticleBatchMessage;
import com.momosoftworks.coldsweat.core.network.message.UseFilledWaterskinMessage;
import com.momosoftworks.coldsweat.util.compat.CompatManager;
import com.momosoftworks.coldsweat.util.math.CSMath;
import com.momosoftworks.coldsweat.util.registries.ModItems;
import com.momosoftworks.coldsweat.util.world.WorldHelper;
import net.minecraft.ChatFormatting;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.core.NonNullList;
import net.minecraft.core.particles.ParticleTypes;
import net.minecraft.network.chat.Component;
import net.minecraft.network.chat.TextComponent;
import net.minecraft.network.chat.TranslatableComponent;
import net.minecraft.sounds.SoundEvents;
import net.minecraft.sounds.SoundSource;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.InteractionResultHolder;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.*;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.DispenserBlock;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.chunk.ChunkAccess;
import net.minecraft.world.level.chunk.LevelChunk;
import net.minecraft.world.phys.AABB;
import net.minecraftforge.common.MinecraftForge;
import net.minecraftforge.event.TickEvent;
import net.minecraftforge.event.entity.player.PlayerInteractEvent;
import net.minecraftforge.eventbus.api.SubscribeEvent;
import net.minecraftforge.fml.common.Mod;
import net.minecraftforge.network.PacketDistributor;
import org.jetbrains.annotations.Nullable;

import java.util.ArrayList;
import java.util.List;
import java.util.Random;

@Mod.EventBusSubscriber
public class FilledWaterskinItem extends Item
{
    public static final double EFFECT_RATE = 0.4;
    public static final String NBT_TEMPERATURE = "Temperature";

    public FilledWaterskinItem()
    {
        super(new Properties().tab(ColdSweatGroup.COLD_SWEAT).stacksTo(1).craftRemainder(ItemInit.WATERSKIN.get()));

        DispenserBlock.registerBehavior(this, (source, stack) ->
        {
            BlockPos pos = source.getPos().relative(source.getBlockState().getValue(DispenserBlock.FACING));
            Level level = source.getLevel();
            ChunkAccess chunk = WorldHelper.getChunk(level, pos);
            double itemTemp = stack.getOrCreateTag().getDouble(FilledWaterskinItem.NBT_TEMPERATURE);

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

            // Spawn a hitbox that falls at the same rate as the particles and gives players below the waterskin effect
            new Object()
            {
                double acceleration = 0;
                int tick = 0;
                AABB aabb = new AABB(pos).inflate(0.5);
                // Track affected players to prevent duplicate effects
                List<Player> affectedPlayers = new ArrayList<>();

                void start()
                {   MinecraftForge.EVENT_BUS.register(this);
                }

                @SubscribeEvent
                public void onTick(TickEvent.WorldTickEvent event)
                {
                    if (event.world.isClientSide == level.isClientSide && event.phase == TickEvent.Phase.START)
                    {
                        // Temperature of waterskin weakens over time
                        double waterTemp = CSMath.blend(itemTemp, itemTemp / 5, tick, 20, 100);

                        // Move the box down at the speed of gravity
                        aabb = aabb.move(0, -acceleration, 0);

                        // If there's ground, stop
                        BlockPos pos = new BlockPos(aabb.minX, aabb.minY, aabb.minZ);
                        if (WorldHelper.isSpreadBlocked(level, chunk.getBlockState(pos), pos, Direction.DOWN, Direction.DOWN))
                        {   MinecraftForge.EVENT_BUS.unregister(this);
                            return;
                        }

                        // Apply the waterskin modifier to all entities in the box
                        level.getEntitiesOfClass(Player.class, aabb).forEach(player ->
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
            double itemTemp = itemstack.getOrCreateTag().getDouble(FilledWaterskinItem.NBT_TEMPERATURE);
            if (itemTemp != 0 && slot <= 8 || player.getOffhandItem().equals(itemstack))
            {
                double temp = (EFFECT_RATE / 20) * ConfigSettings.TEMP_RATE.get();
                double newTemp = CSMath.shrink(itemTemp, temp * 5);

                itemstack.getOrCreateTag().putDouble(FilledWaterskinItem.NBT_TEMPERATURE, newTemp);
                Temperature.addModifier(player, new WaterskinTempModifier(temp * CSMath.sign(itemTemp)).expires(5), Temperature.Type.CORE, true);
            }
        }
    }

    @SubscribeEvent
    public static void onLeftClick(PlayerInteractEvent.LeftClickEmpty event)
    {
        if (event.getItemStack().getItem() instanceof FilledWaterskinItem)
        {   ColdSweatPacketHandler.INSTANCE.send(PacketDistributor.SERVER.noArg(), new UseFilledWaterskinMessage());
            performPourAction(event.getItemStack(), event.getEntityLiving());
        }
    }

    @SubscribeEvent
    public static void onLeftClickBlock(PlayerInteractEvent.LeftClickBlock event)
    {
        if (event.getItemStack().getItem() instanceof FilledWaterskinItem)
        {   performPourAction(event.getItemStack(), event.getEntityLiving());
            event.setCanceled(true);
        }
    }

    @Override
    public boolean canAttackBlock(BlockState pState, Level pLevel, BlockPos pPos, Player pPlayer)
    {   return false;
    }

    @Override
    public boolean onLeftClickEntity(ItemStack stack, Player player, Entity entity)
    {   return performPourAction(stack, player);
    }

    public static boolean performPourAction(ItemStack stack, LivingEntity entity)
    {
        if (!(entity instanceof Player player && stack.is(ModItems.FILLED_WATERSKIN))) return false;

        Level level = player.level;
        double amount = stack.getOrCreateTag().getDouble(FilledWaterskinItem.NBT_TEMPERATURE) * (ConfigSettings.WATERSKIN_STRENGTH.get() / 50d);
        Temperature.addModifier(player, new WaterskinTempModifier(amount).expires(0), Temperature.Type.CORE, true);

        // Play empty sound
        level.playLocalSound(player.getX(), player.getY(), player.getZ(), SoundEvents.AMBIENT_UNDERWATER_EXIT,
                             SoundSource.PLAYERS, 1, (float) ((Math.random() / 5) + 0.9), false);

        // Create empty waterskin item
        ItemStack emptyStack = getEmpty(stack);
        emptyStack.getOrCreateTag().remove("Purity");

        // Add the item to the player's inventory
        if (player.getInventory().contains(emptyStack))
        {   player.addItem(emptyStack);
            player.setItemInHand(InteractionHand.MAIN_HAND, ItemStack.EMPTY);
        }
        else
        {   player.setItemInHand(InteractionHand.MAIN_HAND, emptyStack);
        }

        player.swing(InteractionHand.MAIN_HAND);

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

        return true;
    }

    public InteractionResultHolder<ItemStack> use(Level level, Player player, InteractionHand hand)
    {   return ItemUtils.startUsingInstantly(level, player, hand);
    }

    @Override
    public UseAnim getUseAnimation(ItemStack stack)
    {   return UseAnim.DRINK;
    }

    @Override
    public int getUseDuration(ItemStack stack)
    {   return 32;
    }

    @Override
    public ItemStack finishUsingItem(ItemStack stack, Level level, LivingEntity entity)
    {   double amount = stack.getOrCreateTag().getDouble(FilledWaterskinItem.NBT_TEMPERATURE) * (ConfigSettings.WATERSKIN_STRENGTH.get() / 50d);
        Temperature.addModifier(entity, new WaterskinTempModifier(amount / 100).expires(100), Temperature.Type.CORE, true);
        return entity instanceof Player player && player.isCreative()
               ? stack
               : this.getContainerItem(stack);
    }

    public static ItemStack getEmpty(ItemStack stack)
    {
        if (stack.getItem() instanceof FilledWaterskinItem)
        {
            ItemStack emptyWaterskin = new ItemStack(ModItems.WATERSKIN);

            // Preserve NBT (except temperature)
            emptyWaterskin.setTag(stack.getTag());
            emptyWaterskin.removeTagKey(FilledWaterskinItem.NBT_TEMPERATURE);
            return emptyWaterskin;
        }
        return stack;
    }

    @Override
    public void appendHoverText(ItemStack stack, @Nullable Level level, List<Component> tooltip, TooltipFlag advanced)
    {
        double temp = stack.getOrCreateTag().getDouble(FilledWaterskinItem.NBT_TEMPERATURE);
        // Info tooltip for hotbar functionality
        tooltip.add(new TextComponent(""));
        tooltip.add(new TranslatableComponent("tooltip.cold_sweat.hotbar").withStyle(ChatFormatting.GRAY));
        tooltip.add(new TranslatableComponent("tooltip.cold_sweat.temperature_effect",
                                           (CSMath.sign(temp) >= 0 ? "+" : "-")
                                           + (temp != 0 ? CSMath.truncate(EFFECT_RATE * ConfigSettings.TEMP_RATE.get(), 2) : 0))
                            .withStyle(temp > 0 ? TooltipHandler.HOT : temp < 0 ? TooltipHandler.COLD : ChatFormatting.WHITE));

        // Tooltip to display temperature
        boolean celsius = ConfigSettings.CELSIUS.get();
        ChatFormatting color = temp == 0 ? ChatFormatting.GRAY : (temp < 0 ? ChatFormatting.BLUE : ChatFormatting.RED);
        String tempUnits = celsius ? "C" : "F";
        temp = temp / 2 + 95;
        if (celsius) temp = Temperature.convertUnits(temp, Temperature.Units.F, Temperature.Units.C, true);
        temp += ConfigSettings.TEMP_OFFSET.get() / 2.0;

        tooltip.add(1, new TranslatableComponent("item.cold_sweat.waterskin.filled").withStyle(ChatFormatting.GRAY)
                       .append(" (")
                       .append(new TextComponent((int) temp + " \u00B0" + tempUnits).withStyle(color))
                       .append(new TextComponent(")").withStyle(ChatFormatting.GRAY)));

        super.appendHoverText(stack, level, tooltip, advanced);
    }

    @Override
    public boolean shouldCauseReequipAnimation(ItemStack oldStack, ItemStack newStack, boolean slotChanged)
    {   return slotChanged;
    }

    @Override
    public boolean hasContainerItem(ItemStack stack)
    {   return true;
    }

    @Override
    public ItemStack getContainerItem(ItemStack itemStack)
    {   ItemStack empty = getEmpty(itemStack);
        empty.getOrCreateTag().remove("Purity");
        return empty;
    }

    public String getDescriptionId()
    {   return new TranslatableComponent("item.cold_sweat.waterskin").getString();
    }

    @Override
    public void fillItemCategory(CreativeModeTab tab, NonNullList<ItemStack> itemList)
    {
        if (this.allowdedIn(tab))
        {   ItemStack stack = new ItemStack(this);
            stack = CompatManager.setWaterPurity(stack, 3);
            itemList.add(stack);
        }
    }
}
