package dev.momostudios.coldsweat.common.item;

import dev.momostudios.coldsweat.api.temperature.modifier.SoulLampTempModifier;
import dev.momostudios.coldsweat.api.temperature.modifier.TempModifier;
import dev.momostudios.coldsweat.api.util.Temperature;
import dev.momostudios.coldsweat.core.advancement.trigger.ModAdvancementTriggers;
import dev.momostudios.coldsweat.core.itemgroup.ColdSweatGroup;
import dev.momostudios.coldsweat.util.config.ConfigSettings;
import dev.momostudios.coldsweat.util.entity.NBTHelper;
import dev.momostudios.coldsweat.util.math.CSMath;
import dev.momostudios.coldsweat.util.registries.ModSounds;
import dev.momostudios.coldsweat.util.world.WorldHelper;
import net.minecraft.core.BlockPos;
import net.minecraft.core.NonNullList;
import net.minecraft.core.particles.ParticleTypes;
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
import net.minecraft.world.item.CreativeModeTab;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.phys.AABB;
import net.minecraftforge.event.entity.living.LivingAttackEvent;
import net.minecraftforge.eventbus.api.SubscribeEvent;
import net.minecraftforge.fml.common.Mod;

@Mod.EventBusSubscriber
public class SoulspringLampItem extends Item
{
    public SoulspringLampItem()
    {
        super(new Properties().tab(ColdSweatGroup.COLD_SWEAT).stacksTo(1).fireResistant());
    }

    @Override
    public void inventoryTick(ItemStack stack, Level worldIn, Entity entityIn, int itemSlot, boolean isSelected)
    {
        if (entityIn instanceof Player player && !worldIn.isClientSide)
        {
            double max = ConfigSettings.MAX_TEMP.get();
            double temp;

            // Is selected
            TempModifier lampMod;
            if ((isSelected || player.getOffhandItem() == stack)
            // Is in valid dimension
            && ConfigSettings.LAMP_DIMENSIONS.get().contains(worldIn.dimension().location().toString())
            // Is world temp more than max
            && (temp = (lampMod = Temperature.getModifier(player, Temperature.Type.WORLD, SoulLampTempModifier.class)) != null
                ? lampMod.getLastInput()
                : Temperature.get(player, Temperature.Type.WORLD)) > max && getFuel(stack) > 0)
            {
                if (player.tickCount % 5 == 0)
                {
                    // Drain fuel
                    if (!(player.isCreative() || player.isSpectator()))
                        addFuel(stack, (int) (-0.01 * CSMath.clamp(temp - max, 1, 3)));

                    // Affect nearby players
                    double rad = 3.5d;
                    AABB bb = new AABB(player.getX() - rad, player.getY() - rad, player.getZ() - rad,
                                       player.getX() + rad, player.getY() + rad, player.getZ() + rad);

                    for (Player entity : worldIn.getEntitiesOfClass(Player.class, bb))
                    {
                        // Extend modifier time if it is present
                        SoulLampTempModifier modifier = Temperature.getModifier(entity, Temperature.Type.WORLD, SoulLampTempModifier.class);
                        if (modifier != null)
                            modifier.expires(modifier.getTicksExisted() + 5);
                        else
                            Temperature.addOrReplaceModifier(entity, new SoulLampTempModifier().expires(5).tickRate(5), Temperature.Type.WORLD);
                    }
                }

                // If the conditions are met, turn on the lamp
                if (!stack.getOrCreateTag().getBoolean("isOn") && stack.getOrCreateTag().getInt("stateChangeTimer") <= 0)
                {
                    stack.getOrCreateTag().putInt("stateChangeTimer", 10);
                    stack.getOrCreateTag().putBoolean("isOn", true);

                    WorldHelper.playEntitySound(ModSounds.NETHER_LAMP_ON, player, entityIn.getSoundSource(), 1.5f, (float) Math.random() / 5f + 0.9f);
                }
            }
            // If the conditions are not met, turn off the lamp
            else if (stack.getOrCreateTag().getInt("stateChangeTimer") <= 0)
            {
                if (stack.getOrCreateTag().getBoolean("isOn"))
                {
                    stack.getOrCreateTag().putInt("stateChangeTimer", 10);
                    stack.getOrCreateTag().putBoolean("isOn", false);

                    if (getFuel(stack) < 0.5)
                        setFuel(stack, 0);

                    WorldHelper.playEntitySound(ModSounds.NETHER_LAMP_OFF, player, entityIn.getSoundSource(), 1.5f, (float) Math.random() / 5f + 0.9f);
                }
            }
            else
            {
                // Decrement the state change timer
                NBTHelper.incrementTag(stack, "stateChangeTimer", -1, tag -> tag > 0);
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

    private static void addFuel(ItemStack stack, int amount)
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
        if (event.getSource().getEntity() instanceof Player attacker && !(event.getEntityLiving() instanceof Player))
        {
            ItemStack stack = attacker.getMainHandItem();
            if (!(stack.getItem() instanceof SoulspringLampItem)) return;

            LivingEntity target = event.getEntityLiving();

            // If fuel < 64 and target NOT player
            if (getFuel(stack) < 64
            && target.getMobType() != MobType.UNDEAD
            && !target.getPersistentData().getBoolean("SoulSucked"))
            {
                target.getPersistentData().putBoolean("SoulSucked", true);

                // Add fuel
                addFuel(stack, (int) Math.min(8, target.getMaxHealth()));
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
                WorldHelper.playEntitySound(ModSounds.NETHER_LAMP_ON, attacker, attacker.getSoundSource(), 1f, (float) Math.random() / 5f + 1.3f);
            }
        }
    }

    @Override
    public boolean canAttackBlock(BlockState state, Level level, BlockPos blockPos, Player player) {
        return !player.isCreative();
    }

    @Override
    public void fillItemCategory(CreativeModeTab tab, NonNullList<ItemStack> itemList)
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
