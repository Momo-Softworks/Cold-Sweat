package dev.momostudios.coldsweat.common.item;

import dev.momostudios.coldsweat.api.event.common.ItemSwappedInInventoryEvent;
import dev.momostudios.coldsweat.api.temperature.modifier.SoulLampTempModifier;
import dev.momostudios.coldsweat.api.temperature.modifier.TempModifier;
import dev.momostudios.coldsweat.api.util.Temperature;
import dev.momostudios.coldsweat.core.advancement.trigger.ModAdvancementTriggers;
import dev.momostudios.coldsweat.core.itemgroup.ColdSweatGroup;
import dev.momostudios.coldsweat.config.ConfigSettings;
import dev.momostudios.coldsweat.util.serialization.NBTHelper;
import dev.momostudios.coldsweat.util.math.CSMath;
import dev.momostudios.coldsweat.util.registries.ModSounds;
import dev.momostudios.coldsweat.util.world.WorldHelper;
import net.minecraft.block.BlockState;
import net.minecraft.client.util.ITooltipFlag;
import net.minecraft.entity.CreatureAttribute;
import net.minecraft.entity.Entity;
import net.minecraft.entity.LivingEntity;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.entity.player.ServerPlayerEntity;
import net.minecraft.item.Item;
import net.minecraft.item.ItemGroup;
import net.minecraft.item.ItemStack;
import net.minecraft.item.Rarity;
import net.minecraft.nbt.CompoundNBT;
import net.minecraft.particles.ParticleTypes;
import net.minecraft.util.DamageSource;
import net.minecraft.util.EntityDamageSource;
import net.minecraft.util.NonNullList;
import net.minecraft.util.math.AxisAlignedBB;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.text.ITextComponent;
import net.minecraft.util.text.StringTextComponent;
import net.minecraft.world.World;
import net.minecraftforge.event.entity.living.LivingAttackEvent;
import net.minecraftforge.eventbus.api.SubscribeEvent;
import net.minecraftforge.fml.common.Mod;

import java.util.List;
import java.util.Optional;

@Mod.EventBusSubscriber
public class SoulspringLampItem extends Item
{
    public SoulspringLampItem()
    {   super(new Properties().tab(ColdSweatGroup.COLD_SWEAT).stacksTo(1).fireResistant().rarity(Rarity.UNCOMMON));
    }

    @Override
    public void inventoryTick(ItemStack stack, World world, Entity entity, int itemSlot, boolean isSelected)
    {
        if (entity instanceof PlayerEntity && !world.isClientSide && entity.tickCount % 5 == 0)
        {
            PlayerEntity player = (PlayerEntity) entity;
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
                && (ConfigSettings.LAMP_DIMENSIONS.get().contains(world.dimension().location()))
                // Is world temp more than max
                && temp > max && getFuel(stack) > 0)
                {
                    // Drain fuel
                    if (!(player.isCreative() || player.isSpectator()))
                        addFuel(stack, -0.005 * CSMath.clamp(temp - max, 1, 3));

                    // Affect nearby players
                    double radius = 5d;
                    AxisAlignedBB bb = new AxisAlignedBB(player.getX() - radius, player.getY() + (player.getBbHeight() / 2) - radius, player.getZ() - radius,
                                                player.getX() + radius, player.getY() + (player.getBbHeight() / 2) + radius, player.getZ() + radius);

                    for (PlayerEntity playerEnt : world.getEntitiesOfClass(PlayerEntity.class, bb))
                    {
                        // Extend modifier time if it is present
                        Optional<SoulLampTempModifier> mod = Temperature.getModifier(playerEnt, Temperature.Type.WORLD, SoulLampTempModifier.class);
                        if (mod.isPresent())
                        {   mod.get().setTicksExisted(0);
                        }
                        else
                        {   Temperature.addOrReplaceModifier(playerEnt, new SoulLampTempModifier().expires(5).tickRate(5), Temperature.Type.WORLD);
                        }
                    }
                    shouldBeOn = true;
                }
            }
            finally
            {
                CompoundNBT itemTag = stack.getOrCreateTag();
                // If the conditions are not met, turn off the lamp
                if (itemTag.getInt("stateChangeTimer") <= 0
                && itemTag.getBoolean("isOn") != shouldBeOn)
                {
                    itemTag.putInt("stateChangeTimer", 2);
                    itemTag.putBoolean("isOn", shouldBeOn);

                    if (getFuel(stack) < 0.5)
                        setFuel(stack, 0);

                    WorldHelper.playEntitySound(shouldBeOn ? ModSounds.NETHER_LAMP_ON : ModSounds.NETHER_LAMP_OFF, player, entity.getSoundSource(), 1.5f, (float) Math.random() / 5f + 0.9f);
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
    {
        return slotChanged;
    }

    private static void setFuel(ItemStack stack, double fuel)
    {
        stack.getOrCreateTag().putDouble("fuel", fuel);
    }

    private static void addFuel(ItemStack stack, double amount)
    {
        setFuel(stack, Math.min(64, getFuel(stack) + amount));
    }

    private static void addFuel(ItemStack stack, ItemStack fuelStack)
    {
        addFuel(stack, getFuelForStack(fuelStack) * fuelStack.getCount());
    }

    private static double getFuel(ItemStack stack)
    {
        return stack.getOrCreateTag().getDouble("fuel");
    }

    public static int getFuelForStack(ItemStack fuelStack)
    {
        return ConfigSettings.LAMP_FUEL_ITEMS.get().getOrDefault(fuelStack.getItem(), 0);
    }

    // Restore fuel if player hits an enemy
    @SubscribeEvent
    public static void onEntityHit(LivingAttackEvent event)
    {
        if (event.getSource().getEntity() instanceof PlayerEntity && !(event.getEntityLiving() instanceof PlayerEntity))
        {
            PlayerEntity attacker = (PlayerEntity) event.getSource().getEntity();
            ItemStack stack = attacker.getMainHandItem();
            if (!(stack.getItem() instanceof SoulspringLampItem)) return;

            LivingEntity target = event.getEntityLiving();

            // If fuel < 64 and target NOT player
            if (getFuel(stack) < 64
            && target.getMobType() != CreatureAttribute.UNDEAD
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
    public boolean canAttackBlock(BlockState state, World world, BlockPos blockPos, PlayerEntity player) {
        return !player.isCreative();
    }

    @Override
    public void fillItemCategory(ItemGroup tab, NonNullList<ItemStack> itemList)
    {
        if (this.allowdedIn(tab))
        {
            ItemStack stack = new ItemStack(this);
            stack.getOrCreateTag().putBoolean("isOn", true);
            stack.getOrCreateTag().putDouble("fuel", 64);
            itemList.add(stack);
        }
    }

    @Override
    public void appendHoverText(ItemStack stack, World level, List<ITextComponent> tooltip, ITooltipFlag advanced)
    {
        if (advanced.isAdvanced())
        {   tooltip.add(new StringTextComponent("§fFuel: " + (int) stack.getOrCreateTag().getDouble("fuel") + " / " + 64));
        }
        super.appendHoverText(stack, level, tooltip, advanced);
    }

    @SubscribeEvent
    public static void onItemClickedInGUI(ItemSwappedInInventoryEvent event)
    {
        ItemStack thisStack = event.getSlotItem();
        ItemStack fuelStack = event.getHeldItem();
        PlayerEntity player = event.getPlayer();
        if (ConfigSettings.LAMP_FUEL_ITEMS.get().containsKey(fuelStack.getItem()) && getFuel(thisStack) < 64)
        {
            double currentFuel = getFuel(thisStack);
            addFuel(thisStack, fuelStack);
            fuelStack.shrink((64 - (int) currentFuel) / getFuelForStack(fuelStack));

            if (player instanceof ServerPlayerEntity)
            {   ModAdvancementTriggers.SOUL_LAMP_FUELLED.trigger(((ServerPlayerEntity) player), fuelStack, thisStack);
            }
            event.setCanceled(true);
        }
    }
}
