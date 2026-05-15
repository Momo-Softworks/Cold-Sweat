package com.momosoftworks.coldsweat.common.item;

import com.momosoftworks.coldsweat.api.temperature.modifier.SoulLampTempModifier;
import com.momosoftworks.coldsweat.api.util.placement.Matcher;
import com.momosoftworks.coldsweat.api.util.Temperature;
import com.momosoftworks.coldsweat.client.event.RegisterModels;
import com.momosoftworks.coldsweat.common.capability.soul_lamp.SoulspringLampData;
import com.momosoftworks.coldsweat.compat.CompatManager;
import com.momosoftworks.coldsweat.common.capability.handler.EntityTempManager;
import com.momosoftworks.coldsweat.config.ConfigSettings;
import com.momosoftworks.coldsweat.core.event.TaskScheduler;
import com.momosoftworks.coldsweat.core.init.ModAdvancementTriggers;
import com.momosoftworks.coldsweat.core.init.ModItemComponents;
import com.momosoftworks.coldsweat.core.init.ModItems;
import com.momosoftworks.coldsweat.core.init.ModSounds;
import com.momosoftworks.coldsweat.core.network.message.ParticleBatchMessage;
import com.momosoftworks.coldsweat.data.codec.configuration.FuelData;
import com.momosoftworks.coldsweat.util.item.ItemStackHelper;
import com.momosoftworks.coldsweat.util.serialization.ConfigHelper;
import com.momosoftworks.coldsweat.util.math.CSMath;
import com.momosoftworks.coldsweat.util.world.WorldHelper;
import net.minecraft.client.Minecraft;
import net.minecraft.client.renderer.BlockEntityWithoutLevelRenderer;
import net.minecraft.core.BlockPos;
import net.minecraft.core.particles.ParticleTypes;
import net.minecraft.network.chat.Component;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.tags.EntityTypeTags;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.EquipmentSlot;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.SlotAccess;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.inventory.ClickAction;
import net.minecraft.world.inventory.Slot;
import net.minecraft.world.item.*;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.phys.AABB;
import net.neoforged.bus.api.SubscribeEvent;
import net.neoforged.fml.common.EventBusSubscriber;
import net.neoforged.neoforge.client.extensions.common.IClientItemExtensions;
import net.neoforged.neoforge.event.entity.living.LivingEquipmentChangeEvent;
import net.neoforged.neoforge.event.entity.living.LivingIncomingDamageEvent;

import java.util.List;
import java.util.Optional;
import java.util.function.Consumer;

@EventBusSubscriber
public class SoulspringLampItem extends Item
{
    public SoulspringLampItem()
    {
        super(new Properties().stacksTo(1).fireResistant().rarity(Rarity.RARE)
                              .component(ModItemComponents.SOULSPRING_LAMP_DATA, new SoulspringLampData()));
    }

    @Override
    public ItemStack getDefaultInstance()
    {
        ItemStack stack = super.getDefaultInstance();
        setFuel(stack, 64);
        return stack;
    }

    @Override
    public void initializeClient(Consumer<IClientItemExtensions> consumer)
    {
        consumer.accept(new IClientItemExtensions()
        {
            @Override
            public BlockEntityWithoutLevelRenderer getCustomRenderer()
            {
                RegisterModels.checkForInitModels();
                return RegisterModels.SOULSPRING_LAMP_RENDERER;
            }
        });
    }

    private static void updateComponents(ItemStack stack)
    {
        Double oldFuel = stack.get(ModItemComponents.SOULSPRING_LAMP_FUEL);
        if (oldFuel != null)
        {
            stack.remove(ModItemComponents.SOULSPRING_LAMP_FUEL);
            setFuel(stack, oldFuel);
        }
    }

    @Override
    public void inventoryTick(ItemStack stack, Level level, Entity entity, int itemSlot, boolean isSelected)
    {
        if (!entity.level().isClientSide && entity instanceof LivingEntity living && living.tickCount % 5 == 0)
        {
            updateComponents(stack);
            boolean shouldBeOn = false;
            try
            {
                if (!(isSelected || living.getOffhandItem() == stack || CompatManager.Curios.hasCurio(living, stack)))
                {   return;
                }
                double max = Temperature.get(living, Temperature.Trait.BURNING_POINT);

                double temp = Temperature.getModifier(living, Temperature.Trait.WORLD, SoulLampTempModifier.class)
                              .map(mod -> mod.getLastInput(Temperature.Trait.WORLD)).orElseGet(() -> Temperature.get(living, Temperature.Trait.WORLD));

                // Is in valid dimension
                if ((ConfigSettings.LAMP_DIMENSIONS.get(level.registryAccess()).contains(level.dimensionTypeRegistration()))
                // Is world temp more than max
                && temp > max && getFuel(stack) > 0)
                {
                    shouldBeOn = true;
                    // Drain fuel
                    if (!(living instanceof Player player && player.isCreative() || living.isSpectator()))
                    {   addFuel(stack, -0.005 * CSMath.clamp(temp - max, 1, 3));
                    }

                    // Affect nearby players
                    double radius = 5d;
                    AABB bb = new AABB(living.getX() - radius, living.getY() + (living.getBbHeight() / 2) - radius, living.getZ() - radius,
                                       living.getX() + radius, living.getY() + (living.getBbHeight() / 2) + radius, living.getZ() + radius);

                    if (Math.random() < 0.6)
                    {
                        AABB bb2 = bb.inflate(-3);
                        double x = bb2.minX + (bb2.maxX - bb2.minX) * Math.random();
                        double y = bb2.minY + (bb2.maxY - bb2.minY) * Math.random();
                        double z = bb2.minZ + (bb2.maxZ - bb2.minZ) * Math.random();
                        double xSpeed = (Math.random() - 0.5) * 0.02;
                        double zSpeed = (Math.random() - 0.5) * 0.02;
                        new ParticleBatchMessage(0).addParticle(ParticleTypes.SOUL_FIRE_FLAME, x, y, z, xSpeed, 0, zSpeed)
                                                   .sendEntity(living);
                    }

                    for (LivingEntity ent : WorldHelper.getEntitiesOfClass(LivingEntity.class, level, bb, e -> true))
                    {
                        if (!EntityTempManager.isTemperatureEnabled(ent))
                        {   continue;
                        }
                        // Extend modifier time if it is present
                        Optional<SoulLampTempModifier> mod = Temperature.getModifier(ent, Temperature.Trait.WORLD, SoulLampTempModifier.class);
                        if (mod.isPresent())
                        {   mod.get().setTicksExisted(0);
                        }
                        else
                        {   Temperature.replaceOrAddModifier(ent, new SoulLampTempModifier().expires(5).tickRate(5), Temperature.Trait.WORLD, Matcher.SAME_CLASS);
                        }
                    }
                }
            }
            finally
            {
                // If the conditions are not met, turn off the lamp
                if (isLit(stack) != shouldBeOn)
                {
                    setLit(stack, shouldBeOn);
                    if (getFuel(stack) < 0.5)
                    {   setFuel(stack, 0);
                    }
                    WorldHelper.playEntitySound(shouldBeOn ? ModSounds.SOUL_LAMP_ON.value() : ModSounds.SOUL_LAMP_OFF.value(), living, living.getSoundSource(), 1.5f, (float) Math.random() / 5f + 0.9f);
                }
            }
        }
    }

    @Override
    public boolean shouldCauseReequipAnimation(ItemStack oldStack, ItemStack newStack, boolean slotChanged)
    {   return slotChanged;
    }

    public static void setFuel(ItemStack stack, double fuel)
    {   ItemStackHelper.ifPresent(stack, ModItemComponents.SOULSPRING_LAMP_DATA, lampData -> lampData.setFuel(fuel));
    }
    public static void addFuel(ItemStack stack, double amount)
    {   setFuel(stack, Math.min(64, getFuel(stack) + amount));
    }
    public static void addFuel(ItemStack stack, ItemStack fuelStack)
    {   addFuel(stack, getFuelForStack(fuelStack) * fuelStack.getCount());
    }
    public static double getFuel(ItemStack stack)
    {   return ItemStackHelper.getOpt(stack, ModItemComponents.SOULSPRING_LAMP_DATA).map(SoulspringLampData::fuel).orElse(0d);
    }

    public static boolean isLit(ItemStack stack)
    {   return ItemStackHelper.getOpt(stack, ModItemComponents.SOULSPRING_LAMP_DATA).map(SoulspringLampData::lit).orElse(false);
    }
    public static void setLit(ItemStack stack, boolean lit)
    {   ItemStackHelper.ifPresent(stack, ModItemComponents.SOULSPRING_LAMP_DATA, lampData -> lampData.setLit(lit));
    }

    public static double getFuelForStack(ItemStack item)
    {
        FuelData fuelData = ConfigHelper.getFirstOrNull(ConfigSettings.SOULSPRING_LAMP_FUEL, item.getItem(), data -> data.test(item));
        return CSMath.getIfNotNull(fuelData, data -> data.fuel(item), 0);
    }

    // Restore fuel if player hits an enemy
    @SubscribeEvent
    public static void onEntityHit(LivingIncomingDamageEvent event)
    {
        if (event.getSource().getEntity() instanceof Player attacker && !(event.getEntity() instanceof Player))
        {
            Level level = attacker.level();
            ItemStack stack = attacker.getMainHandItem();
            if (!(stack.getItem() instanceof SoulspringLampItem)) return;

            LivingEntity target = event.getEntity();

            // If fuel < 64 and target NOT player
            if (getFuel(stack) < 64
            && !target.getType().is(EntityTypeTags.UNDEAD)
            && !target.getPersistentData().getBoolean("SoulSucked"))
            {
                target.getPersistentData().putBoolean("SoulSucked", true);

                // Add fuel
                addFuel(stack, (int) Math.min(8, target.getMaxHealth() / 2));
                float extraDamage = Math.max(0, 8 - event.getAmount());
                if (extraDamage > 0)
                    target.hurt(level.damageSources().playerAttack(attacker), extraDamage);

                // Spawn particles
                if (!target.level().isClientSide)
                {
                    int particleCount = (int) CSMath.clamp(target.getBbWidth() * target.getBbWidth() * target.getBbHeight() * 3, 5, 50);
                    WorldHelper.spawnParticleBatch(attacker.level(), ParticleTypes.SOUL, target.getX(), target.getY() + target.getBbHeight() / 2, target.getZ(),
                                                   target.getBbWidth() / 2, target.getBbHeight() / 2, target.getBbWidth() / 2, particleCount, 0.05);
                }
                // Play soul stealing sound
                if (attacker.level().isClientSide)
                {   WorldHelper.playEntitySound(ModSounds.SOUL_LAMP_ON.value(), attacker, attacker.getSoundSource(), 1f, (float) Math.random() / 5f + 1.3f);
                }
            }
        }
    }

    @Override
    public boolean canAttackBlock(BlockState state, Level level, BlockPos blockPos, Player player)
    {   return !player.isCreative();
    }

    @Override
    public void appendHoverText(ItemStack stack, TooltipContext context, List<Component> tooltip, TooltipFlag advanced)
    {
        if (advanced.isAdvanced())
        {   tooltip.add(Component.literal("Fuel: " + (int) getFuel(stack) + " / " + 64));
        }
        super.appendHoverText(stack, context, tooltip, advanced);
    }

    @Override
    public boolean overrideOtherStackedOnMe(ItemStack thisStack, ItemStack fuelStack, Slot slot, ClickAction action, Player player, SlotAccess slotAccess)
    {
        FuelData fuel = ConfigHelper.findFirstFuelMatching(ConfigSettings.SOULSPRING_LAMP_FUEL, fuelStack).orElse(null);
        if (fuel != null && fuel.test(fuelStack) && getFuel(thisStack) < 64)
        {
            double currentFuel = getFuel(thisStack);
            if (action == ClickAction.PRIMARY)
            {
                addFuel(thisStack, fuelStack);
                fuelStack.shrink((int) ((64 - currentFuel) / getFuelForStack(fuelStack)));
            }
            else if (action == ClickAction.SECONDARY)
            {
                ItemStack singleFuelItem = fuelStack.copy();
                singleFuelItem.setCount(1);
                addFuel(thisStack, singleFuelItem);
                fuelStack.shrink(1);
            }

            if (player instanceof ServerPlayer serverPlayer)
            {   ModAdvancementTriggers.SOUL_LAMP_FUELED.value().trigger(serverPlayer, fuelStack, thisStack);
            }

            return true;
        }
        return false;
    }
}
