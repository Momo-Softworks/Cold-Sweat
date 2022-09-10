package dev.momostudios.coldsweat.common.item;

import dev.momostudios.coldsweat.api.temperature.Temperature;
import dev.momostudios.coldsweat.api.temperature.modifier.BiomeTempModifier;
import dev.momostudios.coldsweat.api.temperature.modifier.BlockTempModifier;
import dev.momostudios.coldsweat.api.temperature.modifier.TimeTempModifier;
import dev.momostudios.coldsweat.core.itemgroup.ColdSweatGroup;
import dev.momostudios.coldsweat.util.config.ConfigCache;
import dev.momostudios.coldsweat.util.math.CSMath;
import dev.momostudios.coldsweat.util.registries.ModItems;
import net.minecraft.sounds.SoundEvents;
import net.minecraft.sounds.SoundSource;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.InteractionResultHolder;
import net.minecraft.world.entity.item.ItemEntity;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.ClipContext;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.material.Material;
import net.minecraft.world.phys.BlockHitResult;
import net.minecraft.world.phys.HitResult;

import java.util.List;

public class WaterskinItem extends Item
{
    public WaterskinItem()
    {
        super(new Properties().tab(ColdSweatGroup.COLD_SWEAT).stacksTo(16));
    }

    @Override
    public InteractionResultHolder<ItemStack> use(Level level, Player player, InteractionHand hand)
    {
        InteractionResultHolder<ItemStack> ar = super.use(level, player, hand);
        ItemStack itemstack = ar.getObject();

        BlockHitResult blockhitresult = getPlayerPOVHitResult(level, player, ClipContext.Fluid.SOURCE_ONLY);
        BlockState lookingAt = level.getBlockState(blockhitresult.getBlockPos());

        if (blockhitresult.getType() != HitResult.Type.BLOCK)
        {
            return InteractionResultHolder.pass(itemstack);
        }
        else
        {
            if (lookingAt.getFluidState().isSource() && lookingAt.getMaterial() == Material.WATER)
            {
                ItemStack filledWaterskin = ModItems.FILLED_WATERSKIN.getDefaultInstance();
                filledWaterskin.setTag(itemstack.getTag());
                filledWaterskin.getOrCreateTag().putDouble("temperature", (new Temperature().with(player, List.of(
                        new BiomeTempModifier(),
                        new BlockTempModifier(),
                        new TimeTempModifier()
                )).get() - (CSMath.average(ConfigCache.getInstance().maxTemp, ConfigCache.getInstance().minTemp))) * 15);

                //Replace 1 of the stack with a FilledWaterskinItem
                if (itemstack.getCount() > 1)
                {
                    if (!player.addItem(filledWaterskin))
                    {
                        ItemEntity itementity = player.drop(filledWaterskin, false);
                        if (itementity != null)
                        {
                            itementity.setNoPickUpDelay();
                            itementity.setOwner(player.getUUID());
                        }
                    }
                    itemstack.shrink(1);
                }
                else
                {
                    player.setItemInHand(hand, filledWaterskin);
                }
                //Play filling sound
                level.playSound(null, player, SoundEvents.AMBIENT_UNDERWATER_ENTER, SoundSource.PLAYERS, 1, (float) Math.random() / 5 + 0.9f);
                player.swing(hand);

                player.getCooldowns().addCooldown(ModItems.FILLED_WATERSKIN, 10);
                player.getCooldowns().addCooldown(ModItems.WATERSKIN, 10);
            }
            return ar;
        }
    }

    @Override
    public boolean shouldCauseReequipAnimation(ItemStack oldStack, ItemStack newStack, boolean slotChanged)
    {
        return slotChanged;
    }
}
