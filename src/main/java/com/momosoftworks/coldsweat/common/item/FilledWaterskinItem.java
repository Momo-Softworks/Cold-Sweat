package com.momosoftworks.coldsweat.common.item;

import com.momosoftworks.coldsweat.api.temperature.modifier.WaterTempModifier;
import com.momosoftworks.coldsweat.api.temperature.modifier.WaterskinTempModifier;
import com.momosoftworks.coldsweat.api.util.placement.Matcher;
import com.momosoftworks.coldsweat.api.util.Temperature;
import com.momosoftworks.coldsweat.api.util.placement.Mode;
import com.momosoftworks.coldsweat.api.util.placement.Order;
import com.momosoftworks.coldsweat.api.util.placement.Placement;
import com.momosoftworks.coldsweat.client.event.TooltipHandler;
import com.momosoftworks.coldsweat.common.capability.handler.EntityTempManager;
import com.momosoftworks.coldsweat.common.entity.data.Preference;
import com.momosoftworks.coldsweat.config.ConfigSettings;
import com.momosoftworks.coldsweat.core.event.TaskScheduler;
import com.momosoftworks.coldsweat.core.init.ItemInit;
import com.momosoftworks.coldsweat.core.itemgroup.ColdSweatGroup;
import com.momosoftworks.coldsweat.core.network.message.ParticleBatchMessage;
import com.momosoftworks.coldsweat.util.math.CSMath;
import com.momosoftworks.coldsweat.util.registries.ModItems;
import com.momosoftworks.coldsweat.util.registries.ModSounds;
import com.momosoftworks.coldsweat.util.world.WorldHelper;
import net.minecraft.block.BlockState;
import net.minecraft.block.Blocks;
import net.minecraft.block.CauldronBlock;
import net.minecraft.block.DispenserBlock;
import net.minecraft.client.Minecraft;
import net.minecraft.client.util.ITooltipFlag;
import net.minecraft.dispenser.IDispenseItemBehavior;
import net.minecraft.entity.Entity;
import net.minecraft.entity.LivingEntity;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.item.*;
import net.minecraft.nbt.CompoundNBT;
import net.minecraft.particles.ParticleTypes;
import net.minecraft.util.*;
import net.minecraft.util.math.AxisAlignedBB;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.MathHelper;
import net.minecraft.util.math.vector.Vector3d;
import net.minecraft.util.text.*;
import net.minecraft.world.World;
import net.minecraft.world.chunk.IChunk;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.api.distmarker.OnlyIn;
import net.minecraftforge.common.MinecraftForge;
import net.minecraftforge.event.TickEvent;
import net.minecraftforge.event.entity.living.LivingEntityUseItemEvent;
import net.minecraftforge.eventbus.api.SubscribeEvent;

import java.util.*;

public class FilledWaterskinItem extends Item
{
    public static final double DRAIN_RATE = 0.5;
    public static final String NBT_TEMPERATURE = "Temperature";

    public FilledWaterskinItem()
    {
        super(new Properties().tab(ColdSweatGroup.COLD_SWEAT).stacksTo(1).craftRemainder(ItemInit.WATERSKIN.get()));

        DispenserBlock.registerBehavior(this, DISPENSE_BEHAVIOR);
    }

    public static ItemStack getDisplayStack()
    {   return new ItemStack(ModItems.FILLED_WATERSKIN);
    }

    @Override
    public int getMaxDamage(ItemStack stack)
    {   return ConfigSettings.WATERSKIN_USES.get();
    }

    @Override
    public int getRGBDurabilityForDisplay(ItemStack stack)
    {   return 0x2D5EDE;
    }

    @Override
    public boolean showDurabilityBar(ItemStack stack)
    {   return stack.getDamageValue() > 0;
    }

    private static int getDurability(ItemStack stack)
    {   return Math.max(0, stack.getMaxDamage() - (stack.getDamageValue()));
    }

    @Override
    public void inventoryTick(ItemStack stack, World world, Entity entity, int slot, boolean isSelected)
    {
        super.inventoryTick(stack, world, entity, slot, isSelected);
        if (!world.isClientSide() && EntityTempManager.isTemperatureEnabled(entity) && entity.tickCount % 5 == 0)
        {
            double itemTemp = stack.getOrCreateTag().getDouble(FilledWaterskinItem.NBT_TEMPERATURE);
            boolean shouldDrain = ConfigSettings.ITEM_TEMPERATURES.get().get(stack.getItem()).stream().anyMatch(c -> c.test(entity, stack, slot, null));
            if (shouldDrain)
            {
                double drainAmt = (DRAIN_RATE / 20) * ConfigSettings.WATERSKIN_NEUTRALIZE_SPEED.get() * 4;
                double newTemp = CSMath.shrink(itemTemp, drainAmt);
                stack.getOrCreateTag().putDouble(FilledWaterskinItem.NBT_TEMPERATURE, newTemp);
            }
        }
    }

    public static boolean performPourAction(ItemStack stack, LivingEntity entity, Hand hand)
    {
        if (!(entity instanceof PlayerEntity && stack.getItem() == ModItems.FILLED_WATERSKIN)) return false;
        PlayerEntity player = ((PlayerEntity) entity);

        // Play empty sound
        if (!player.level.isClientSide)
        {
            double temperature = stack.getOrCreateTag().getDouble(FilledWaterskinItem.NBT_TEMPERATURE);
            double wetnessTemp = 0.05 * CSMath.sign(temperature == 0 ? 1 : temperature);
            // Replace or add water temp modifier
            Placement modPlacement = Placement.of(Mode.REPLACE, Order.FIRST, mod -> mod instanceof WaterTempModifier).orElse(Placement.LAST);
            Temperature.addModifier(player, new WaterTempModifier(wetnessTemp).tickRate(5), Temperature.Trait.WORLD, modPlacement);

            WorldHelper.playEntitySound(ModSounds.WATERSKIN_POUR, player, player.getSoundSource(), 2f, (float) ((Math.random() / 5) + 0.9));
        }

        consumeWaterskin(stack, player, hand);
        MinecraftForge.EVENT_BUS.post(new LivingEntityUseItemEvent.Finish(player, stack, 1, stack.getContainerItem()));
        player.swing(hand, true);

        // spawn falling water particles
        Random rand = new Random();
        for (int i = 0; i < 6; i++)
        {
            TaskScheduler.scheduleServer(() ->
            {
                ParticleBatchMessage particleBatch = new ParticleBatchMessage(2);
                for (int p = 0; p < 10; p++)
                {
                    AxisAlignedBB playerBB = player.getDimensions(player.getPose()).makeBoundingBox(player.position()).inflate(0.2);
                    particleBatch.addParticle(ParticleTypes.FALLING_WATER,
                                              MathHelper.lerp(rand.nextFloat(), playerBB.minX, playerBB.maxX),
                                              playerBB.maxY,
                                              MathHelper.lerp(rand.nextFloat(), playerBB.minZ, playerBB.maxZ),
                                              0.3, 0.3, 0.3);
                }
                particleBatch.sendEntity(player);
            }, i);
        }
        player.getCooldowns().addCooldown(ModItems.FILLED_WATERSKIN, 10);
        player.getCooldowns().addCooldown(ModItems.WATERSKIN, 10);

        return true;
    }

    public static ItemStack consumeWaterskin(ItemStack stack, LivingEntity entity, Hand usedHand)
    {
        // Create empty waterskin item
        ItemStack emptyStack = stack.getContainerItem();

        // Add the item to the player's inventory
        if (entity instanceof PlayerEntity && ((PlayerEntity) entity).inventory.contains(emptyStack))
        {   PlayerEntity player = (PlayerEntity) entity;
            player.addItem(emptyStack);
            player.setItemInHand(usedHand, ItemStack.EMPTY);
            return ItemStack.EMPTY;
        }
        else
        {   entity.setItemInHand(usedHand, emptyStack);
            return emptyStack;
        }
    }

    @Override
    public ActionResult<ItemStack> use(World level, PlayerEntity player, Hand hand)
    {
        if (player.isCrouching())
        {   return performAction(Preference.getOrDefault(player, Preference.WATERSKIN_SECONDARY, Preference.WaterskinAction.DRINK), level, player, hand);
        }
        else
        {   return performAction(Preference.getOrDefault(player, Preference.WATERSKIN_PRIMARY, Preference.WaterskinAction.POUR), level, player, hand);
        }
    }

    @Override
    public void onUseTick(World level, LivingEntity entity, ItemStack stack, int remainingTicks)
    {
        super.onUseTick(level, entity, stack, remainingTicks);
        if (remainingTicks % 5 == 0 && remainingTicks < this.getUseDuration(stack) - 5)
        {
            Vector3d playerPos = entity.position().add(0, entity.getBbHeight() / 2, 0);
            Vector3d lookVec = new Vector3d(entity.getLookAngle().x, 0, entity.getLookAngle().z).normalize();
            Vector3d particlePos = playerPos.add(lookVec.scale(0.3));
            WorldHelper.spawnParticleBatch(level, ParticleTypes.SPLASH, particlePos.x, particlePos.y, particlePos.z, 0.2, 0.2, 0.2, 4, 0);
        }
    }

    private static ActionResult<ItemStack> performAction(Preference.WaterskinAction action, World level, PlayerEntity player, Hand hand)
    {
        ItemStack stack = player.getItemInHand(hand);
        switch (action)
        {
            case DRINK :
            {   return DrinkHelper.useDrink(level, player, hand);
            }
            case POUR :
            {   if (performPourAction(stack, player, hand))
                {   return ActionResult.consume(stack);
                }
                break;
            }
            case NONE : break;
        }
        return ActionResult.pass(stack);
    }

    @Override
    public ActionResultType useOn(ItemUseContext context)
    {
        World level = context.getLevel();
        BlockPos pos = context.getClickedPos();
        BlockState state = level.getBlockState(pos);
        PlayerEntity player = context.getPlayer();

        if (state.is(Blocks.CAULDRON))
        {
            // Fill cauldron
            int waterLevel = state.getValue(CauldronBlock.LEVEL);
            if (waterLevel >= 3) return ActionResultType.PASS;
            state = state.setValue(CauldronBlock.LEVEL, waterLevel + 1);
            level.setBlock(pos, state, 3);
            // Pouring sound / visuals
            if (!level.isClientSide)
            {   level.playSound(null, pos, ModSounds.WATERSKIN_FILL, SoundCategory.BLOCKS, 2f, (float) Math.random() / 5 + 0.9f);
                WorldHelper.spawnParticleBatch(level, ParticleTypes.SPLASH, pos.getX() + 0.5, pos.getY() + 0.65, pos.getZ() + 0.5, 0.5, 0.5, 0.5, 10, 0);
            }
            // Consume waterskin
            if (player != null)
            {   consumeWaterskin(context.getItemInHand(), player, context.getHand());
                player.getCooldowns().addCooldown(ModItems.WATERSKIN, 10);
            }
            return ActionResultType.SUCCESS;
        }
        return ActionResultType.PASS;
    }

    @Override
    public UseAction getUseAnimation(ItemStack stack)
    {   return UseAction.DRINK;
    }

    @Override
    public int getUseDuration(ItemStack stack)
    {   return 32;
    }

    @Override
    public ItemStack finishUsingItem(ItemStack stack, World level, LivingEntity entity)
    {   double amount = stack.getOrCreateTag().getDouble(FilledWaterskinItem.NBT_TEMPERATURE) * (ConfigSettings.WATERSKIN_CONSUME_STRENGTH.get() / 50d);
        Temperature.addModifier(entity, new WaterskinTempModifier(amount / (100 * stack.getMaxDamage())).expires(100), Temperature.Trait.CORE, Placement.LAST);
        if (entity instanceof PlayerEntity && ((PlayerEntity) entity).isCreative())
        {   return stack;
        }
        else
        {
            if (getDurability(stack) <= 1)
            {   return consumeWaterskin(stack, entity, entity.getUsedItemHand());
            }
            else
            {   stack.setDamageValue(stack.getDamageValue() + 1);
                return stack;
            }
        }
    }

    @OnlyIn(Dist.CLIENT)
    @Override
    public void appendHoverText(ItemStack stack, World level, List<ITextComponent> tooltip, ITooltipFlag advanced)
    {
        // Display filled state
        IFormattableTextComponent filledLabel = new TranslationTextComponent("item.cold_sweat.waterskin.filled").withStyle(TextFormatting.GRAY);
        if (ConfigSettings.ENABLE_HINTS.get() && !TooltipHandler.isShiftDown())
        {   filledLabel.append(new StringTextComponent(" ").append(TooltipHandler.EXPAND_TOOLTIP_HINT));
        }
        tooltip.add(filledLabel);

        if (TooltipHandler.isShiftDown())
        {
            if (ConfigSettings.ENABLE_HINTS.get())
            {
                String crouchKey = Minecraft.getInstance().options.keyShift.getKey().getDisplayName().getString();
                String crouchAction;
                switch (Preference.getOrDefault(Minecraft.getInstance().player, Preference.WATERSKIN_SECONDARY, Preference.WaterskinAction.POUR))
                {
                    case DRINK : crouchAction = "tooltip.cold_sweat.waterskin.drink"; break;
                    case POUR : crouchAction = "tooltip.cold_sweat.waterskin.pour"; break;
                    default : crouchAction = "";
                }
                if (!crouchAction.isEmpty())
                {   tooltip.add(2, new TranslationTextComponent(crouchAction, new StringTextComponent(crouchKey).withStyle(TextFormatting.GRAY)).withStyle(TextFormatting.DARK_GRAY));
                }
            }
        }

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
    public ItemStack getContainerItem(ItemStack stack)
    {
        if (stack.getItem() instanceof FilledWaterskinItem)
        {
            ItemStack emptyWaterskin = new ItemStack(ModItems.WATERSKIN);

            // Preserve NBT (except temperature)
            emptyWaterskin.setTag(CSMath.orElse(stack.getTag(), new CompoundNBT()).copy());
            emptyWaterskin.removeTagKey(FilledWaterskinItem.NBT_TEMPERATURE);
            emptyWaterskin.removeTagKey("Damage");
            emptyWaterskin.removeTagKey("Purity");
            return emptyWaterskin;
        }
        return stack;
    }

    public String getDescriptionId()
    {   return new TranslationTextComponent("item.cold_sweat.waterskin").getString();
    }

    private static final IDispenseItemBehavior DISPENSE_BEHAVIOR = (source, stack) ->
    {
        BlockPos pos = source.getPos().relative(source.getBlockState().getValue(DispenserBlock.FACING));
        World level = source.getLevel();
        IChunk chunk = WorldHelper.getChunk(level, pos);
        double itemTemp = stack.getOrCreateTag().getDouble(FilledWaterskinItem.NBT_TEMPERATURE);

        if (chunk == null) return stack;

        // Play sound
        level.playLocalSound(pos.getX(), pos.getY(), pos.getZ(), SoundEvents.AMBIENT_UNDERWATER_EXIT,
                             SoundCategory.PLAYERS, 1, (float) ((Math.random() / 5) + 0.9), false);

        // Spawn particles
        Random rand = new Random();
        for (int i = 0; i < 6; i++)
        {
            TaskScheduler.scheduleServer(() ->
                                         {
                                             ParticleBatchMessage particles = new ParticleBatchMessage(2);
                                             for (int p = 0; p < rand.nextInt(5) + 5; p++)
                                             {
                                                 particles.addParticle(ParticleTypes.FALLING_WATER,
                                                                       new ParticleBatchMessage.ParticlePlacement(pos.getX() + rand.nextDouble(),
                                                                                                                  pos.getY() + rand.nextDouble(),
                                                                                                                  pos.getZ() + rand.nextDouble(), 0, 0, 0));
                                             }
                                             particles.sendWorld(level);
                                         }, i);
        }

        // Spawn a hitbox that falls at the same rate as the particles and gives players below the waterskin effect
        new Object()
        {
            double acceleration = 0;
            int tick = 0;
            AxisAlignedBB aabb = new AxisAlignedBB(pos).inflate(0.5);
            // Track affected players to prevent duplicate effects
            List<PlayerEntity> affectedPlayers = new ArrayList<>();

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
                    double effectAmount = waterTemp * (ConfigSettings.WATERSKIN_CONSUME_STRENGTH.get() / 50d);
                    double wetnessTemp = 0.05 * CSMath.sign(waterTemp+0.01);

                    // Move the box down at the speed of gravity
                    aabb = aabb.move(0, -acceleration, 0);

                    // If there's ground, stop
                    BlockPos pos = new BlockPos(aabb.minX, aabb.minY, aabb.minZ);
                    if (WorldHelper.isSpreadBlocked(level, chunk.getBlockState(pos), pos, Direction.DOWN, Direction.DOWN))
                    {   MinecraftForge.EVENT_BUS.unregister(this);
                        return;
                    }

                    // Apply the waterskin modifier to all entities in the box
                    WorldHelper.getEntitiesOfClass(PlayerEntity.class, level, aabb, e -> true).forEach(player ->
                                                                               {
                                                                                   if (!affectedPlayers.contains(player))
                                                                                   {   // Apply the effect and store the player
                                                                                       Temperature.addModifier(player, new WaterskinTempModifier(effectAmount).expires(0), Temperature.Trait.CORE, Placement.LAST);
                            Temperature.replaceOrAddModifier(player, new WaterTempModifier(wetnessTemp).tickRate(5), Temperature.Trait.WORLD, Matcher.SAME_CLASS);
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

        return stack.getContainerItem();
    };
}
