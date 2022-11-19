package dev.momostudios.coldsweat.common.item;

import dev.momostudios.coldsweat.api.temperature.modifier.SoulLampTempModifier;
import dev.momostudios.coldsweat.api.temperature.modifier.TempModifier;
import dev.momostudios.coldsweat.api.util.Temperature;
import dev.momostudios.coldsweat.client.gui.tooltip.SoulspringTooltip;
import dev.momostudios.coldsweat.core.itemgroup.ColdSweatGroup;
import dev.momostudios.coldsweat.core.network.ColdSweatPacketHandler;
import dev.momostudios.coldsweat.core.network.message.ParticleBatchMessage;
import dev.momostudios.coldsweat.util.config.ConfigSettings;
import dev.momostudios.coldsweat.util.entity.NBTHelper;
import dev.momostudios.coldsweat.util.math.CSMath;
import dev.momostudios.coldsweat.util.registries.ModSounds;
import dev.momostudios.coldsweat.util.world.WorldHelper;
import net.minecraft.core.BlockPos;
import net.minecraft.core.NonNullList;
import net.minecraft.core.particles.ParticleTypes;
import net.minecraft.sounds.SoundSource;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.MobType;
import net.minecraft.world.entity.SlotAccess;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.inventory.ClickAction;
import net.minecraft.world.inventory.Slot;
import net.minecraft.world.inventory.tooltip.TooltipComponent;
import net.minecraft.world.item.CreativeModeTab;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.phys.AABB;
import net.minecraftforge.fml.common.Mod;
import net.minecraftforge.network.PacketDistributor;

import java.util.Optional;
import java.util.Random;

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
            double max = ConfigSettings.getInstance().maxTemp;
            double temp;

            // Is selected
            if ((isSelected || player.getOffhandItem() == stack))
            {
                TempModifier lampMod = Temperature.getModifier(player, Temperature.Type.WORLD, SoulLampTempModifier.class);
                // Is world temp more than max
                if (ConfigSettings.LAMP_DIMENSIONS.get().contains(worldIn.dimension().location().toString())
                // Is in valid dimension
                && (temp = lampMod != null ? lampMod.getLastInput() : Temperature.get(player, Temperature.Type.WORLD)) > max && getFuel(stack) > 0)
                {
                    if (player.tickCount % 5 == 0)
                    {
                        // Drain fuel
                        if (!(player.isCreative() || player.isSpectator()))
                            addFuel(stack, -0.01d * CSMath.clamp(temp - max, 1d, 3d));

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
                                Temperature.replaceModifier(entity, new SoulLampTempModifier().expires(5).tickRate(5), Temperature.Type.WORLD);
                        }
                    }

                    // If the conditions are met, turn on the lamp
                    if (!stack.getOrCreateTag().getBoolean("isOn") && stack.getOrCreateTag().getInt("stateChangeTimer") <= 0)
                    {
                        stack.getOrCreateTag().putInt("stateChangeTimer", 10);
                        stack.getOrCreateTag().putBoolean("isOn", true);

                        WorldHelper.playEntitySound(ModSounds.NETHER_LAMP_ON, player, SoundSource.PLAYERS, 1.5f, (float) Math.random() / 5f + 0.9f);
                    }
                }
            }
            // If the conditions are not met, turn off the lamp
            else
            {
                if (stack.getOrCreateTag().getBoolean("isOn") && stack.getOrCreateTag().getInt("stateChangeTimer") <= 0)
                {
                    stack.getOrCreateTag().putInt("stateChangeTimer", 10);
                    stack.getOrCreateTag().putBoolean("isOn", false);

                    if (getFuel(stack) < 0.5)
                        setFuel(stack, 0);

                    WorldHelper.playEntitySound(ModSounds.NETHER_LAMP_OFF, player, SoundSource.PLAYERS, 1.5f, (float) Math.random() / 5f + 0.9f);
                }
            }

            // Decrement the state change timer
            NBTHelper.incrementTag(stack, "stateChangeTimer", -1, tag -> tag > 0);
        }
    }

    @Override
    public boolean shouldCauseReequipAnimation(ItemStack oldStack, ItemStack newStack, boolean slotChanged)
    {
        return slotChanged;
    }

    private void setFuel(ItemStack stack, double fuel)
    {
        stack.getOrCreateTag().putDouble("fuel", fuel);
    }
    private void addFuel(ItemStack stack, double fuel)
    {
        setFuel(stack, Math.min(64, getFuel(stack) + fuel));
    }
    private double getFuel(ItemStack stack)
    {
        return stack.getOrCreateTag().getDouble("fuel");
    }

    // Restore fuel if player hits an enemy
    @Override
    public boolean hurtEnemy(ItemStack stack, LivingEntity target, LivingEntity attacker)
    {
        // If fuel < 64 and target NOT player
        if (attacker instanceof Player && getFuel(stack) < 64 && !(target instanceof Player)
        && target.getHealth() > target.getMaxHealth() - 1 && target.getMobType() != MobType.UNDEAD)
        {
            // Add fuel
            addFuel(stack, Math.min(4, target.getMaxHealth()));

            // Spawn particles
            ParticleBatchMessage packet = new ParticleBatchMessage();
            Random rand = new Random();
            // Spawn random particles proportionally to the entity's size
            for (int i = 0; i < CSMath.clamp(target.getBbWidth() * target.getBbWidth() * target.getBbHeight() * 3, 5, 50); i++)
            {
                packet.addParticle(ParticleTypes.SOUL, new ParticleBatchMessage.ParticlePlacement(
                        target.getX() + target.getBbWidth() * rand.nextFloat(),
                        target.getY() + target.getBbHeight() * rand.nextFloat(),
                        target.getZ() + target.getBbWidth() * rand.nextFloat(),
                        rand.nextFloat() * 0.2f - 0.1f, rand.nextFloat() * 0.2f - 0.1f, rand.nextFloat() * 0.2f - 0.1f));
            }
            ColdSweatPacketHandler.INSTANCE.send(PacketDistributor.TRACKING_ENTITY.with(() -> target), packet);
            // Play soul stealing sound
            WorldHelper.playEntitySound(ModSounds.NETHER_LAMP_ON, attacker, SoundSource.PLAYERS, 1f, (float) Math.random() / 5f + 1.3f);
            return true;
        }
        return super.hurtEnemy(stack, attacker, target);
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
    public boolean overrideOtherStackedOnMe(ItemStack thisStack, ItemStack newStack, Slot slot, ClickAction action, Player player, SlotAccess slotAccess)
    {
        if (ConfigSettings.LAMP_FUEL_ITEMS.get().contains(newStack.getItem()) && getFuel(thisStack) < 64)
        {
            int stackCount = newStack.getCount();
            newStack.shrink(64 - (int) getFuel(thisStack));
            addFuel(thisStack, stackCount);
            return true;
        }
        return false;
    }

    @Override
    public Optional<TooltipComponent> getTooltipImage(ItemStack stack)
    {
        return Optional.of(new SoulspringTooltip(getFuel(stack)));
    }
}
