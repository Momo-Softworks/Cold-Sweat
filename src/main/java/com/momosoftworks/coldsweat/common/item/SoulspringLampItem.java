package com.momosoftworks.coldsweat.common.item;

import com.momosoftworks.coldsweat.api.temperature.modifier.SoulLampTempModifier;
import com.momosoftworks.coldsweat.api.temperature.modifier.TempModifier;
import com.momosoftworks.coldsweat.api.util.Temperature;
import com.momosoftworks.coldsweat.core.advancement.trigger.ModAdvancementTriggers;
import com.momosoftworks.coldsweat.core.itemgroup.ColdSweatGroup;
import com.momosoftworks.coldsweat.config.ConfigSettings;
import com.momosoftworks.coldsweat.data.tags.ModDimensionTags;
import com.momosoftworks.coldsweat.util.serialization.NBTHelper;
import com.momosoftworks.coldsweat.util.math.CSMath;
import com.momosoftworks.coldsweat.util.registries.ModSounds;
import com.momosoftworks.coldsweat.util.world.WorldHelper;
import net.minecraft.core.BlockPos;
import net.minecraft.core.NonNullList;
import net.minecraft.core.particles.ParticleTypes;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.network.chat.Component;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.damagesource.DamageSource;
import net.minecraft.world.damagesource.EntityDamageSource;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.MobType;
import net.minecraft.world.entity.SlotAccess;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.inventory.ClickAction;
import net.minecraft.world.inventory.Slot;
import net.minecraft.world.item.*;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.phys.AABB;
import net.minecraftforge.event.entity.living.LivingAttackEvent;
import net.minecraftforge.eventbus.api.SubscribeEvent;
import net.minecraftforge.fml.common.Mod;
import org.jetbrains.annotations.Nullable;

import java.util.List;
import java.util.Optional;

@Mod.EventBusSubscriber
public class SoulspringLampItem extends Item
{
    public SoulspringLampItem()
    {   super(new Properties().tab(ColdSweatGroup.COLD_SWEAT).stacksTo(1).fireResistant().rarity(Rarity.UNCOMMON));
    }

    @Override
    public void inventoryTick(ItemStack stack, Level level, Entity entityIn, int itemSlot, boolean isSelected)
    {
        if (entityIn instanceof Player player && !level.isClientSide && player.tickCount % 5 == 0)
        {
            boolean shouldBeOn = false;
            try
            {
                if (!(isSelected || player.getOffhandItem() == stack)) return;
                double max = ConfigSettings.MAX_TEMP.get();

                double temp = Temperature.getModifier(player, Temperature.Type.WORLD, SoulLampTempModifier.class)
                              .map(TempModifier::getLastInput).orElseGet(() -> Temperature.get(player, Temperature.Type.WORLD));

                // Is selected
                if ((isSelected || player.getOffhandItem() == stack)
                // Is in valid dimension
                && (level.dimensionTypeRegistration().is(ModDimensionTags.SOUL_LAMP_VALID) || ConfigSettings.LAMP_DIMENSIONS.get().contains(level.dimension().location()))
                // Is world temp more than max
                && temp > max && getFuel(stack) > 0)
                {
                    // Drain fuel
                    if (!(player.isCreative() || player.isSpectator()))
                        addFuel(stack, -0.005 * CSMath.clamp(temp - max, 1, 3));

                    // Affect nearby players
                    double radius = 5d;
                    AABB bb = new AABB(player.getX() - radius, player.getY() + (player.getBbHeight() / 2) - radius, player.getZ() - radius,
                                       player.getX() + radius, player.getY() + (player.getBbHeight() / 2) + radius, player.getZ() + radius);

                    for (Player entity : level.getEntitiesOfClass(Player.class, bb))
                    {
                        // Extend modifier time if it is present
                        Optional<SoulLampTempModifier> mod = Temperature.getModifier(entity, Temperature.Type.WORLD, SoulLampTempModifier.class);
                        if (mod.isPresent())
                        {   mod.get().setTicksExisted(0);
                        }
                        else
                        {   Temperature.addOrReplaceModifier(entity, new SoulLampTempModifier().expires(5).tickRate(5), Temperature.Type.WORLD);
                        }
                    }
                    shouldBeOn = true;
                }
            }
            finally
            {
                CompoundTag itemTag = stack.getOrCreateTag();
                // If the conditions are not met, turn off the lamp
                if (itemTag.getInt("stateChangeTimer") <= 0
                && itemTag.getBoolean("Lit") != shouldBeOn)
                {
                    itemTag.putInt("stateChangeTimer", 2);
                    itemTag.putBoolean("Lit", shouldBeOn);

                    if (getFuel(stack) < 0.5)
                        setFuel(stack, 0);

                    WorldHelper.playEntitySound(shouldBeOn ? ModSounds.NETHER_LAMP_ON : ModSounds.NETHER_LAMP_OFF, player, entityIn.getSoundSource(), 1.5f, (float) Math.random() / 5f + 0.9f);
                }
                else
                {   // Decrement the state change timer
                    NBTHelper.incrementTag(stack, "stateChangeTimer", -1, tag -> tag > 0);
                }
            }
        }
    }

    @Override
    public boolean shouldCauseReequipAnimation(ItemStack oldStack, ItemStack newStack, boolean slotChanged)
    {   return slotChanged;
    }

    private static void setFuel(ItemStack stack, double fuel)
    {   stack.getOrCreateTag().putDouble("Fuel", fuel);
    }

    private static void addFuel(ItemStack stack, double amount)
    {   setFuel(stack, Math.min(64, getFuel(stack) + amount));
    }

    private static void addFuel(ItemStack stack, ItemStack fuelStack)
    {   addFuel(stack, getFuelForStack(fuelStack) * fuelStack.getCount());
    }

    private static double getFuel(ItemStack stack)
    {   return stack.getOrCreateTag().getDouble("Fuel");
    }

    public static int getFuelForStack(ItemStack fuelStack)
    {   return ConfigSettings.LAMP_FUEL_ITEMS.get().getOrDefault(fuelStack.getItem(), 0);
    }

    // Restore fuel if player hits an enemy
    @SubscribeEvent
    public static void onEntityHit(LivingAttackEvent event)
    {
        if (event.getSource().getEntity() instanceof Player attacker && !(event.getEntity() instanceof Player))
        {
            ItemStack stack = attacker.getMainHandItem();
            if (!(stack.getItem() instanceof SoulspringLampItem)) return;

            LivingEntity target = event.getEntity();

            // If fuel < 64 and target NOT player
            if (getFuel(stack) < 64
            && target.getMobType() != MobType.UNDEAD
            && !target.getPersistentData().getBoolean("SoulSucked"))
            {
                target.getPersistentData().putBoolean("SoulSucked", true);

                // Add fuel
                addFuel(stack, (int) Math.min(8, target.getMaxHealth() / 2));
                float extraDamage = Math.max(0, 8 - event.getAmount());
                if (extraDamage > 0)
                    target.hurt(new EntityDamageSource(DamageSource.MAGIC.msgId, attacker), extraDamage);

                // Spawn particles
                if (!target.level.isClientSide)
                {
                    int particleCount = (int) CSMath.clamp(target.getBbWidth() * target.getBbWidth() * target.getBbHeight() * 3, 5, 50);
                    WorldHelper.spawnParticleBatch(attacker.level, ParticleTypes.SOUL, target.getX(), target.getY() + target.getBbHeight() / 2, target.getZ(),
                        target.getBbWidth() / 2, target.getBbHeight() / 2, target.getBbWidth() / 2, particleCount, 0.05);
                }
                // Play soul stealing sound
                if (attacker.level.isClientSide)
                    WorldHelper.playEntitySound(ModSounds.NETHER_LAMP_ON, attacker, attacker.getSoundSource(), 1f, (float) Math.random() / 5f + 1.3f);
            }
        }
    }

    @Override
    public boolean canAttackBlock(BlockState state, Level level, BlockPos blockPos, Player player)
    {   return !player.isCreative();
    }

    @Override
    public void fillItemCategory(CreativeModeTab tab, NonNullList<ItemStack> itemList)
    {
        if (this.allowedIn(tab))
        {
            ItemStack stack = new ItemStack(this);
            stack.getOrCreateTag().putBoolean("isOn", true);
            stack.getOrCreateTag().putDouble("fuel", 64);
            itemList.add(stack);
        }
    }

    @Override
    public void appendHoverText(ItemStack stack, @Nullable Level level, List<Component> tooltip, TooltipFlag advanced)
    {
        if (advanced.isAdvanced())
        {   tooltip.add(Component.literal("Fuel: " + (int) stack.getOrCreateTag().getDouble("Fuel") + " / " + 64));
        }
        super.appendHoverText(stack, level, tooltip, advanced);
    }

    @Override
    public boolean overrideOtherStackedOnMe(ItemStack thisStack, ItemStack fuelStack, Slot slot, ClickAction action, Player player, SlotAccess slotAccess)
    {
        if (ConfigSettings.LAMP_FUEL_ITEMS.get().containsKey(fuelStack.getItem()) && getFuel(thisStack) < 64)
        {
            double currentFuel = getFuel(thisStack);
            addFuel(thisStack, fuelStack);
            fuelStack.shrink((64 - (int) currentFuel) / getFuelForStack(fuelStack));

            if (player instanceof ServerPlayer serverPlayer)
                ModAdvancementTriggers.SOUL_LAMP_FUELLED.trigger(serverPlayer, fuelStack, thisStack);

            return true;
        }
        return false;
    }
}
