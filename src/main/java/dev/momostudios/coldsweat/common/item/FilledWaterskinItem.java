package dev.momostudios.coldsweat.common.item;

import dev.momostudios.coldsweat.api.temperature.Temperature;
import dev.momostudios.coldsweat.config.ItemSettingsConfig;
import dev.momostudios.coldsweat.core.init.ItemInit;
import dev.momostudios.coldsweat.core.itemgroup.ColdSweatGroup;
import dev.momostudios.coldsweat.util.config.ConfigCache;
import dev.momostudios.coldsweat.util.config.DynamicValue;
import dev.momostudios.coldsweat.util.math.CSMath;
import dev.momostudios.coldsweat.util.registries.ModItems;
import dev.momostudios.coldsweat.util.world.WorldHelper;
import net.minecraft.core.particles.ParticleTypes;
import net.minecraft.network.chat.TranslatableComponent;
import net.minecraft.sounds.SoundEvents;
import net.minecraft.sounds.SoundSource;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.InteractionResultHolder;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.Level;
import dev.momostudios.coldsweat.api.temperature.modifier.WaterskinTempModifier;
import dev.momostudios.coldsweat.util.config.ConfigCache;
import dev.momostudios.coldsweat.api.util.TempHelper;

public class FilledWaterskinItem extends Item
{
    static DynamicValue<Integer> WATERSKIN_STRENGTH = DynamicValue.of(() -> ItemSettingsConfig.getInstance().waterskinStrength());

    public FilledWaterskinItem()
    {
        super(new Properties().tab(ColdSweatGroup.COLD_SWEAT).stacksTo(1).craftRemainder(ItemInit.WATERSKIN.get()));
    }

    @Override
    public void inventoryTick(ItemStack itemstack, Level world, Entity entity, int slot, boolean isSelected)
    {
        super.inventoryTick(itemstack, world, entity, slot, isSelected);
        if (entity instanceof Player player)
        {
            double itemTemp = itemstack.getOrCreateTag().getDouble("temperature");
            if (itemTemp != 0)
            {
                if (CSMath.isInRange(itemTemp, -1, 1))
                {
                    itemstack.getOrCreateTag().putDouble("temperature", 0);
                    return;
                }

                if (slot <= 8 || player.getOffhandItem().equals(itemstack))
                {
                    double temp = 0.03 * ConfigCache.getInstance().rate * CSMath.getSign(itemTemp);
                    double newTemp = itemTemp - temp;

                    itemstack.getOrCreateTag().putDouble("temperature", newTemp);

                    TempHelper.addModifier(player, new WaterskinTempModifier(temp * 1.5).expires(1), Temperature.Type.CORE, true);
                }
            }
        }
    }

    @Override
    public InteractionResultHolder<ItemStack> use(Level level, Player player, InteractionHand hand)
    {
        InteractionResultHolder<ItemStack> ar = super.use(level, player, hand);
        ItemStack itemstack = ar.getObject();

        double amount = itemstack.getOrCreateTag().getDouble("temperature") * (WATERSKIN_STRENGTH.get() / 50d);
        if (player.tickCount % 5 == 0)
        TempHelper.addModifier(player, new WaterskinTempModifier(amount).expires(5), Temperature.Type.CORE, true);

        // Play empty sound
        level.playLocalSound(player.getX(), player.getY(), player.getZ(), SoundEvents.AMBIENT_UNDERWATER_EXIT,
                SoundSource.PLAYERS, 1, (float) ((Math.random() / 5) + 0.9), false);

        // Create empty waterskin item
        ItemStack emptyWaterskin = new ItemStack(ModItems.WATERSKIN);

        // Preserve NBT (except temperature)
        emptyWaterskin.setTag(itemstack.getTag());
        emptyWaterskin.removeTagKey("temperature");

        // Add the item to the player's inventory
        if (player.getInventory().contains(emptyWaterskin))
        {
            player.addItem(emptyWaterskin);
            player.setItemInHand(hand, ItemStack.EMPTY);
        }
        else
        {
            player.setItemInHand(hand, emptyWaterskin);
        }

        player.swing(hand);

        for (int i = 0; i < 10; i++)
        {
            TaskScheduler.scheduleClient(() ->
            {
                for (int p = 0; p < 5; p++)
                {
                    level.addParticle(ParticleTypes.FALLING_WATER,
                            player.getX() + Math.random() * player.getBbWidth() - (player.getBbWidth() / 2),
                            player.getY() + player.getBbHeight() + Math.random() * 0.5,
                            player.getZ() + Math.random() * player.getBbWidth() - (player.getBbWidth() / 2), 0.3, 0.3, 0.3);
                }
            }, i);
        }

        return ar;
    }

    @Override
    public boolean shouldCauseReequipAnimation(ItemStack oldStack, ItemStack newStack, boolean slotChanged)
    {
        return slotChanged;
    }

    public String getDescriptionId()
    {
        return new TranslatableComponent("item.cold_sweat.waterskin").getString();
    }
}
