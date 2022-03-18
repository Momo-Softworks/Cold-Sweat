package dev.momostudios.coldsweat.common.item;

import dev.momostudios.coldsweat.api.temperature.Temperature;
import dev.momostudios.coldsweat.core.init.ItemInit;
import dev.momostudios.coldsweat.core.itemgroup.ColdSweatGroup;
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
import dev.momostudios.coldsweat.config.ConfigCache;
import dev.momostudios.coldsweat.util.entity.TempHelper;

public class FilledWaterskinItem extends Item
{
    public FilledWaterskinItem()
    {
        super(new Properties().tab(ColdSweatGroup.COLD_SWEAT).stacksTo(1).craftRemainder(ItemInit.WATERSKIN_REGISTRY.get()));
    }

    @Override
    public void inventoryTick(ItemStack itemstack, Level world, Entity entity, int slot, boolean selected)
    {
        super.inventoryTick(itemstack, world, entity, slot, selected);
        if (entity instanceof Player)
        {
            double itemTemp = itemstack.getOrCreateTag().getDouble("temperature");
            if (itemTemp != 0 && slot <= 8)
            {
                double temp = 0.03 * ConfigCache.getInstance().rate * (itemTemp / Math.abs(itemTemp));
                if (itemTemp > 0)
                {
                    itemstack.getOrCreateTag().putDouble("temperature", itemTemp - Math.min(itemTemp, temp));
                }
                else if (itemTemp < 0)
                {
                    itemstack.getOrCreateTag().putDouble("temperature", itemTemp + Math.min(-itemTemp, temp));
                }

                TempHelper.addModifier((Player) entity, new WaterskinTempModifier(temp).expires(1), Temperature.Types.CORE, true);
            }
        }
    }

    @Override
    public InteractionResultHolder<ItemStack> use(Level level, Player player, InteractionHand hand)
    {
        InteractionResultHolder<ItemStack> ar = super.use(level, player, hand);
        ItemStack itemstack = ar.getObject();

        TempHelper.addModifier(player, new WaterskinTempModifier(itemstack.getOrCreateTag().getDouble("temperature")).expires(1), Temperature.Types.CORE, true);

        level.playLocalSound(player.getX(), player.getY(), player.getZ(), SoundEvents.AMBIENT_UNDERWATER_EXIT, SoundSource.PLAYERS, 1, (float) ((Math.random() / 5) + 0.9), false);

        ItemStack emptyWaterskin = new ItemStack(ModItems.WATERSKIN);
        emptyWaterskin.setTag(itemstack.getTag());
        player.setItemInHand(hand, emptyWaterskin);

        player.swing(hand);

        for (int i = 0; i < 10; i++)
        {
            WorldHelper.schedule(() ->
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
