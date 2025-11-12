package com.momosoftworks.coldsweat.common.item;

import com.momosoftworks.coldsweat.api.temperature.modifier.WaterTempModifier;
import com.momosoftworks.coldsweat.api.temperature.modifier.WaterskinTempModifier;
import com.momosoftworks.coldsweat.api.util.Placement;
import com.momosoftworks.coldsweat.api.util.Temperature;
import com.momosoftworks.coldsweat.client.event.TooltipHandler;
import com.momosoftworks.coldsweat.compat.CompatManager;
import com.momosoftworks.coldsweat.common.entity.data.Preference;
import com.momosoftworks.coldsweat.config.ConfigSettings;
import com.momosoftworks.coldsweat.core.event.TaskScheduler;
import com.momosoftworks.coldsweat.core.init.ModItemComponents;
import com.momosoftworks.coldsweat.core.init.ModItems;
import com.momosoftworks.coldsweat.core.init.ModSounds;
import com.momosoftworks.coldsweat.core.network.message.ParticleBatchMessage;
import com.momosoftworks.coldsweat.util.math.CSMath;
import com.momosoftworks.coldsweat.util.world.WorldHelper;
import dev.ghen.thirst.content.registry.ThirstComponent;
import net.minecraft.ChatFormatting;
import net.minecraft.client.Minecraft;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.core.component.DataComponents;
import net.minecraft.core.dispenser.DispenseItemBehavior;
import net.minecraft.core.particles.ParticleTypes;
import net.minecraft.network.chat.Component;
import net.minecraft.network.chat.MutableComponent;
import net.minecraft.sounds.SoundSource;
import net.minecraft.util.Mth;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.InteractionResult;
import net.minecraft.world.InteractionResultHolder;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.*;
import net.minecraft.world.item.component.CustomData;
import net.minecraft.world.item.context.UseOnContext;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.Blocks;
import net.minecraft.world.level.block.DispenserBlock;
import net.minecraft.world.level.block.LayeredCauldronBlock;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.chunk.ChunkAccess;
import net.minecraft.world.phys.AABB;
import net.minecraft.world.phys.Vec3;
import net.neoforged.api.distmarker.Dist;
import net.neoforged.api.distmarker.OnlyIn;
import net.neoforged.bus.api.SubscribeEvent;
import net.neoforged.neoforge.common.NeoForge;
import net.neoforged.neoforge.event.tick.LevelTickEvent;

import java.util.*;

public class FilledWaterskinItem extends Item
{
    public static final double EFFECT_RATE = 0.5;

    public FilledWaterskinItem()
    {
        super(new Properties().stacksTo(1).craftRemainder(ModItems.WATERSKIN.get())
                              .component(ModItemComponents.WATER_TEMPERATURE, 0d));

        DispenserBlock.registerBehavior(this, DISPENSE_BEHAVIOR);
    }

    public static ItemStack getDisplayStack()
    {   return new ItemStack(ModItems.FILLED_WATERSKIN.asItem());
    }

    @Override
    public int getMaxDamage(ItemStack stack)
    {   return ConfigSettings.WATERSKIN_USES.get();
    }

    @Override
    public int getBarColor(ItemStack stack)
    {   return 0x2D5EDE;
    }

    @Override
    public boolean isBarVisible(ItemStack stack)
    {   return stack.getDamageValue() > 0;
    }

    private static int getDurability(ItemStack stack)
    {   return Math.max(0, stack.getMaxDamage() - (stack.getDamageValue()));
    }

    @Override
    public void inventoryTick(ItemStack stack, Level world, Entity entity, int slot, boolean isSelected)
    {
        super.inventoryTick(stack, world, entity, slot, isSelected);
        if (entity.tickCount % 5 == 0 && entity instanceof Player player)
        {
            double itemTemp = stack.get(ModItemComponents.WATER_TEMPERATURE);
            if (itemTemp != 0 && slot <= 8 || player.getOffhandItem().equals(stack))
            {
                double drainAmt = (EFFECT_RATE / 20) * ConfigSettings.WATERSKIN_NEUTRALIZE_SPEED.get();
                double newTemp = CSMath.shrink(itemTemp, drainAmt * 5);

                double tempEffect = (EFFECT_RATE / 10) * ConfigSettings.WATERSKIN_HOTBAR_STRENGTH.get();
                stack.set(ModItemComponents.WATER_TEMPERATURE, newTemp);
                Temperature.addModifier(player, new WaterskinTempModifier(tempEffect * CSMath.sign(itemTemp)).expires(5), Temperature.Trait.CORE, Placement.Duplicates.ALLOW);
            }
        }
    }

    public static boolean performPourAction(ItemStack stack, LivingEntity entity, InteractionHand hand)
    {
        if (!(entity instanceof Player player && stack.is(ModItems.FILLED_WATERSKIN) && stack.has(ModItemComponents.WATER_TEMPERATURE))) return false;

        // Play empty sound
        if (!player.level().isClientSide)
        {
            double temperature = stack.getOrDefault(ModItemComponents.WATER_TEMPERATURE, 0d);
            double effectAmount = temperature * (ConfigSettings.WATERSKIN_CONSUME_STRENGTH.get() / 50d);
            Temperature.addModifier(player, new WaterskinTempModifier(effectAmount).expires(0), Temperature.Trait.CORE, Placement.Duplicates.ALLOW);
            double wetnessTemp = 0.05 * CSMath.sign(temperature == 0 ? 1 : temperature);
            Temperature.addOrReplaceModifier(player, new WaterTempModifier(wetnessTemp).tickRate(5), Temperature.Trait.WORLD, Placement.Duplicates.BY_CLASS);

            WorldHelper.playEntitySound(ModSounds.WATERSKIN_POUR.value(), player, player.getSoundSource(), 2f, (float) ((Math.random() / 5) + 0.9));
        }

        consumeWaterskin(stack, player, hand);
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
                    AABB playerBB = player.getDimensions(player.getPose()).makeBoundingBox(player.position()).inflate(0.2);
                    particleBatch.addParticle(ParticleTypes.FALLING_WATER,
                                              Mth.lerp(rand.nextFloat(), playerBB.minX, playerBB.maxX),
                                              playerBB.maxY,
                                              Mth.lerp(rand.nextFloat(), playerBB.minZ, playerBB.maxZ),
                                              0.3, 0.3, 0.3);
                }
                particleBatch.sendEntity(player);
            }, i);
        }
        player.getCooldowns().addCooldown(ModItems.FILLED_WATERSKIN.value(), 10);
        player.getCooldowns().addCooldown(ModItems.WATERSKIN.value(), 10);

        return true;
    }

    public static ItemStack consumeWaterskin(ItemStack stack, LivingEntity entity, InteractionHand usedHand)
    {
        // Create empty waterskin item
        ItemStack emptyStack = stack.getCraftingRemainingItem();

        // Add the item to the player's inventory
        if (entity instanceof Player player && player.getInventory().contains(emptyStack))
        {   player.addItem(emptyStack);
            player.setItemInHand(usedHand, ItemStack.EMPTY);
            return ItemStack.EMPTY;
        }
        else
        {   entity.setItemInHand(usedHand, emptyStack);
            return emptyStack;
        }
    }

    @Override
    public InteractionResultHolder<ItemStack> use(Level level, Player player, InteractionHand hand)
    {
        if (player.isCrouching())
        {   return performAction(Preference.getOrDefault(player, Preference.WATERSKIN_SECONDARY, Preference.WaterskinAction.DRINK), level, player, hand);
        }
        else
        {   return performAction(Preference.getOrDefault(player, Preference.WATERSKIN_PRIMARY, Preference.WaterskinAction.POUR), level, player, hand);
        }
    }

    @Override
    public void onUseTick(Level level, LivingEntity entity, ItemStack stack, int remainingTicks)
    {
        super.onUseTick(level, entity, stack, remainingTicks);
        if (remainingTicks % 5 == 0 && remainingTicks < this.getUseDuration(stack, entity) - 5)
        {
            Vec3 playerPos = entity.position().add(0, entity.getBbHeight() / 2, 0);
            Vec3 lookVec = new Vec3(entity.getLookAngle().x, 0, entity.getLookAngle().z).normalize();
            Vec3 particlePos = playerPos.add(lookVec.scale(0.3));
            WorldHelper.spawnParticleBatch(level, ParticleTypes.SPLASH, particlePos.x, particlePos.y, particlePos.z, 0.2, 0.2, 0.2, 4, 0);
        }
    }

    private static InteractionResultHolder<ItemStack> performAction(Preference.WaterskinAction action, Level level, Player player, InteractionHand hand)
    {
        switch (action)
        {
            case DRINK ->
            {   return ItemUtils.startUsingInstantly(level, player, hand);
            }
            case POUR ->
            {   if (performPourAction(player.getItemInHand(hand), player, hand))
                {   return InteractionResultHolder.consume(player.getItemInHand(hand));
                }
            }
            case NONE -> {}
        }
        return InteractionResultHolder.pass(player.getItemInHand(hand));
    }

    @Override
    public InteractionResult useOn(UseOnContext context)
    {
        Level level = context.getLevel();
        BlockPos pos = context.getClickedPos();
        BlockState state = level.getBlockState(pos);
        Player player = context.getPlayer();

        if (state.is(Blocks.CAULDRON) || state.is(Blocks.WATER_CAULDRON))
        {
            boolean hasWater = state.is(Blocks.WATER_CAULDRON);
            // Fill cauldron
            int waterLevel = hasWater ? state.getValue(LayeredCauldronBlock.LEVEL) : 0;
            if (waterLevel >= 3) return InteractionResult.PASS;
            state = hasWater
                    ? state.setValue(LayeredCauldronBlock.LEVEL, waterLevel + 1)
                    : Blocks.WATER_CAULDRON.defaultBlockState().setValue(LayeredCauldronBlock.LEVEL, 1);
            level.setBlock(pos, state, 3);
            // Pouring sound / visuals
            if (!level.isClientSide)
            {   level.playSound(null, pos, ModSounds.WATERSKIN_FILL.value(), SoundSource.BLOCKS, 2f, (float) Math.random() / 5 + 0.9f);
                WorldHelper.spawnParticleBatch(level, ParticleTypes.SPLASH, pos.getX() + 0.5, pos.getY() + 0.65, pos.getZ() + 0.5, 0.5, 0.5, 0.5, 10, 0);
            }
            // Consume waterskin
            if (player != null)
            {   consumeWaterskin(context.getItemInHand(), player, context.getHand());
                player.getCooldowns().addCooldown(ModItems.WATERSKIN.value(), 10);
            }
            return InteractionResult.SUCCESS;
        }
        return InteractionResult.PASS;
    }

    @Override
    public UseAnim getUseAnimation(ItemStack stack)
    {   return UseAnim.DRINK;
    }

    @Override
    public int getUseDuration(ItemStack stack, LivingEntity entity)
    {   return 32;
    }

    @Override
    public ItemStack finishUsingItem(ItemStack stack, Level level, LivingEntity entity)
    {
        double amount = stack.get(ModItemComponents.WATER_TEMPERATURE) * (ConfigSettings.WATERSKIN_CONSUME_STRENGTH.get() / 50d);
        Temperature.addModifier(entity, new WaterskinTempModifier(amount / (100 * stack.getMaxDamage())).expires(100), Temperature.Trait.CORE, Placement.Duplicates.ALLOW);
        if (entity instanceof Player player && player.isCreative())
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

    @Override
    @OnlyIn(Dist.CLIENT)
    public void appendHoverText(ItemStack stack, TooltipContext context, List<Component> tooltip, TooltipFlag advanced)
    {
        double temp = CSMath.round(stack.get(ModItemComponents.WATER_TEMPERATURE), 2);
        double multiplier = ConfigSettings.WATERSKIN_CONSUME_STRENGTH.get() / 50d;

        // Display filled state
        MutableComponent filledLabel = Component.translatable("item.cold_sweat.waterskin.filled").withStyle(ChatFormatting.GRAY);
        if (ConfigSettings.ENABLE_HINTS.get() && !TooltipHandler.isShiftDown())
            filledLabel.append(Component.literal(" ").append(TooltipHandler.EXPAND_TOOLTIP));
        tooltip.add(filledLabel);

        // Info tooltip for drinking/pouring functionality
        int useEffect = (int) Math.round(temp * multiplier);
        Component tempText = useEffect > 0  ? Component.translatable("tooltip.cold_sweat.temperature_effect", "+" + useEffect).withStyle(TooltipHandler.HOT) :
                             useEffect == 0 ? Component.translatable("tooltip.cold_sweat.temperature_effect", "+" + useEffect).withStyle(ChatFormatting.WHITE)
                                            : Component.translatable("tooltip.cold_sweat.temperature_effect", useEffect).withStyle(TooltipHandler.COLD);
        tooltip.add(Component.empty());
        tooltip.add(Component.translatable("tooltip.cold_sweat.used").withStyle(ChatFormatting.GRAY));
        tooltip.add(tempText);
        if (TooltipHandler.isShiftDown())
        {
            if (ConfigSettings.ENABLE_HINTS.get())
            {
                String crouchKey = Minecraft.getInstance().options.keyShift.getKey().getDisplayName().getString();
                String crouchAction;
                switch (Preference.getOrDefault(Minecraft.getInstance().player, Preference.WATERSKIN_SECONDARY, Preference.WaterskinAction.POUR))
                {
                    case DRINK -> crouchAction = "tooltip.cold_sweat.waterskin.drink";
                    case POUR -> crouchAction = "tooltip.cold_sweat.waterskin.pour";
                    default -> crouchAction = "";
                }
                if (!crouchAction.isEmpty())
                {   tooltip.add(2, Component.translatable(crouchAction, Component.literal(crouchKey).withStyle(ChatFormatting.GRAY)).withStyle(ChatFormatting.DARK_GRAY));
                }
            }

            // Info tooltip for hotbar functionality
            String perSecond = Component.translatable("tooltip.cold_sweat.per_second").getString();

            tooltip.add(Component.empty());
            tooltip.add(Component.translatable("tooltip.cold_sweat.hotbar").withStyle(ChatFormatting.GRAY));
            double effectRate = EFFECT_RATE * ConfigSettings.WATERSKIN_HOTBAR_STRENGTH.get();
            Component tempEffectText = (temp > 0  ? Component.translatable("tooltip.cold_sweat.temperature_effect", "+" + CSMath.round(effectRate, 2)).withStyle(TooltipHandler.HOT) :
                                        temp == 0 ? Component.translatable("tooltip.cold_sweat.temperature_effect", "+0").withStyle(ChatFormatting.WHITE)
                                                  : Component.translatable("tooltip.cold_sweat.temperature_effect", "-" + CSMath.round(effectRate, 2)).withStyle(TooltipHandler.COLD))
                        .append(perSecond);
            tooltip.add(tempEffectText);
        }

        super.appendHoverText(stack, context, tooltip, advanced);
    }

    @Override
    public boolean shouldCauseReequipAnimation(ItemStack oldStack, ItemStack newStack, boolean slotChanged)
    {   return slotChanged;
    }

    @Override
    public boolean hasCraftingRemainingItem(ItemStack stack)
    {   return true;
    }

    @Override
    public ItemStack getCraftingRemainingItem(ItemStack stack)
    {
        if (stack.getItem() instanceof FilledWaterskinItem)
        {
            ItemStack emptyWaterskin = super.getCraftingRemainingItem(stack);

            // Preserve NBT (except temperature)
            emptyWaterskin.applyComponents(stack.getComponents());
            emptyWaterskin.remove(ModItemComponents.WATER_TEMPERATURE);
            emptyWaterskin.remove(DataComponents.DAMAGE);
            emptyWaterskin.remove(DataComponents.MAX_DAMAGE);
            emptyWaterskin.set(DataComponents.MAX_STACK_SIZE, emptyWaterskin.getItem().getDefaultMaxStackSize());
            if (CompatManager.isThirstLoaded())
            {   emptyWaterskin.remove(ThirstComponent.PURITY);
            }
            return emptyWaterskin;
        }
        return stack;
    }

    public String getDescriptionId()
    {   return Component.translatable("item.cold_sweat.waterskin").getString();
    }

    private static final DispenseItemBehavior DISPENSE_BEHAVIOR = (source, stack) ->
    {
        BlockPos pos = source.pos().relative(source.state().getValue(DispenserBlock.FACING));
        Level level = source.level();
        ChunkAccess chunk = WorldHelper.getChunk(level, pos);
        double itemTemp = stack.getOrDefault(ModItemComponents.WATER_TEMPERATURE, 0d);

        if (chunk == null) return stack;

        // Play sound
        level.playSound(null, pos.getX(), pos.getY(), pos.getZ(), ModSounds.WATERSKIN_POUR,
                SoundSource.BLOCKS, 1, (float) ((Math.random() / 5) + 0.9));

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
            AABB aabb = new AABB(pos).inflate(0.5);
            // Track affected players to prevent duplicate effects
            Set<Player> affectedPlayers = new HashSet<>();

            void start()
            {   NeoForge.EVENT_BUS.register(this);
            }

            @SubscribeEvent
            public void onTick(LevelTickEvent.Pre event)
            {
                if (event.getLevel().isClientSide() == level.isClientSide())
                {
                    // Temperature of waterskin weakens over time
                    double waterTemp = CSMath.blend(itemTemp, itemTemp / 5, tick, 20, 100);
                    double effectAmount = waterTemp * (ConfigSettings.WATERSKIN_CONSUME_STRENGTH.get() / 50d);
                    double wetnessTemp = 0.05 * CSMath.sign(waterTemp+0.01);

                    // Move the box down at the speed of gravity
                    aabb = aabb.move(0, -acceleration, 0);

                    // If there's ground, stop
                    BlockPos pos = BlockPos.containing(aabb.minX, aabb.minY, aabb.minZ);
                    if (WorldHelper.isFullSide(chunk.getBlockState(pos).getShape(level, pos), Direction.UP))
                    {   NeoForge.EVENT_BUS.unregister(this);
                        return;
                    }

                    // Apply the waterskin modifier to all entities in the box
                    level.getEntitiesOfClass(Player.class, aabb).forEach(player ->
                    {
                        if (!affectedPlayers.contains(player))
                        {   // Apply the effect and store the player
                            Temperature.addModifier(player, new WaterskinTempModifier(effectAmount).expires(0), Temperature.Trait.CORE, Placement.Duplicates.ALLOW);
                            Temperature.addOrReplaceModifier(player, new WaterTempModifier(wetnessTemp).tickRate(5), Temperature.Trait.WORLD, Placement.Duplicates.BY_CLASS);
                            affectedPlayers.add(player);
                        }
                    });

                    // Increase the speed of the box
                    acceleration += 0.0052;
                    tick++;

                    // Expire after 5 seconds
                    if (tick > 100)
                    {   NeoForge.EVENT_BUS.unregister(this);
                    }
                }
            }
        }.start();

        return stack.getCraftingRemainingItem();
    };
}
