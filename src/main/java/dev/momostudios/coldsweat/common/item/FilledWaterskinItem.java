package dev.momostudios.coldsweat.common.item;

import dev.momostudios.coldsweat.common.temperature.Temperature;
import dev.momostudios.coldsweat.core.init.ItemInit;
import dev.momostudios.coldsweat.core.itemgroup.ColdSweatGroup;
import dev.momostudios.coldsweat.util.registries.ModItems;
import net.minecraft.core.particles.ParticleTypes;
import net.minecraft.sounds.SoundEvents;
import net.minecraft.sounds.SoundSource;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.InteractionResultHolder;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.Level;
import dev.momostudios.coldsweat.common.temperature.modifier.WaterskinTempModifier;
import dev.momostudios.coldsweat.config.ConfigCache;
import dev.momostudios.coldsweat.util.entity.PlayerHelper;

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
                double temp = 0;
                if (itemTemp > 0)
                {
                    itemstack.getOrCreateTag().putDouble("temperature", itemTemp - Math.min(itemTemp, 0.03));
                    temp = 0.03;
                }
                else if (itemTemp < 0)
                {
                    itemstack.getOrCreateTag().putDouble("temperature", itemTemp + Math.min(-itemTemp, 0.03));
                    temp = -0.03;
                }

                PlayerHelper.addModifier((Player) entity, new WaterskinTempModifier(temp * ConfigCache.getInstance().rate).expires(1), Temperature.Types.BODY, true);
            }
        }
    }

    @Override
    public InteractionResultHolder<ItemStack> use(Level world, Player entity, InteractionHand hand)
    {
        InteractionResultHolder<ItemStack> ar = super.use(world, entity, hand);
        ItemStack itemstack = ar.getObject();

        PlayerHelper.addModifier(entity, new WaterskinTempModifier(itemstack.getOrCreateTag().getDouble("temperature")).expires(1), Temperature.Types.BODY, true);

        world.playLocalSound(entity.getX(), entity.getY(), entity.getZ(), SoundEvents.AMBIENT_UNDERWATER_EXIT, SoundSource.PLAYERS, 1, (float) ((Math.random() / 5) + 0.9), false);

        if (!entity.getInventory().contains(ModItems.WATERSKIN.getDefaultInstance()))
        {
            entity.setItemInHand(hand, ModItems.WATERSKIN.getDefaultInstance());
        }
        else
        {
            entity.addItem(ModItems.WATERSKIN.getDefaultInstance());
            itemstack.shrink(1);
        }
        entity.swing(hand);

        for (int p = 0; p < 50; p++)
        {
            world.addParticle(ParticleTypes.FALLING_WATER, entity.getX(), entity.getY() + entity.getBbHeight(), entity.getZ(), 0.3, 0.3, 0.3);
        }

        return ar;
    }

    @Override
    public boolean shouldCauseReequipAnimation(ItemStack oldStack, ItemStack newStack, boolean slotChanged)
    {
        return slotChanged;
    }

    public String getTranslationKey(ItemStack stack)
    {
        return "item.cold_sweat.waterskin";
    }
}
