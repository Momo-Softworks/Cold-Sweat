package net.momostudios.coldsweat.common.item;

import net.minecraft.entity.Entity;
import net.minecraft.entity.item.ItemEntity;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.item.Item;
import net.minecraft.item.ItemStack;
import net.minecraft.nbt.IntNBT;
import net.minecraft.particles.ParticleTypes;
import net.minecraft.util.ActionResult;
import net.minecraft.util.Hand;
import net.minecraft.util.ResourceLocation;
import net.minecraft.util.SoundCategory;
import net.minecraft.world.World;
import net.minecraft.world.server.ServerWorld;
import net.minecraftforge.registries.ForgeRegistries;
import net.momostudios.coldsweat.common.temperature.PlayerTemp;
import net.momostudios.coldsweat.common.temperature.modifier.WaterskinTempModifier;
import net.momostudios.coldsweat.core.itemgroup.ColdSweatGroup;
import net.momostudios.coldsweat.core.init.ModItems;

public class FilledWaterskinItem extends Item
{
    public FilledWaterskinItem()
    {
        super(new Item.Properties().group(ColdSweatGroup.COLD_SWEAT).maxStackSize(1).containerItem(ModItems.WATERSKIN.get()));
    }

    @Override
    public void inventoryTick(ItemStack itemstack, World world, Entity entity, int slot, boolean selected)
    {
        super.inventoryTick(itemstack, world, entity, slot, selected);
        if (entity instanceof PlayerEntity)
        {
            int itemTemp = itemstack.getOrCreateTag().getInt("temperature");
            if (entity.ticksExisted % 20 == 0 && itemTemp != 0 && slot <= 8)
            {
                int temp = 0;
                if (itemTemp > 0)
                {
                    if (entity.ticksExisted % 40 == 0) itemstack.getOrCreateTag().putInt("temperature", itemTemp - 1);
                    temp = 1;
                }
                else if (itemTemp < 0)
                {
                    if (entity.ticksExisted % 40 == 0) itemstack.getOrCreateTag().putInt("temperature", itemTemp + 1);
                    temp = -1;
                }

                PlayerTemp.applyModifier((PlayerEntity) entity, new WaterskinTempModifier(), PlayerTemp.Types.BODY, false, IntNBT.valueOf(temp));
            }
        }
    }

    @Override
    public ActionResult<ItemStack> onItemRightClick(World world, PlayerEntity entity, Hand hand)
    {
        ActionResult<ItemStack> ar = super.onItemRightClick(world, entity, hand);
        ItemStack itemstack = ar.getResult();

        PlayerTemp.applyModifier(entity, new WaterskinTempModifier(), PlayerTemp.Types.BODY, false, IntNBT.valueOf(itemstack.getOrCreateTag().getInt("temperature")));

        world.playSound(entity.getPosX(), entity.getPosY(), entity.getPosZ(),
        ForgeRegistries.SOUND_EVENTS.getValue(new ResourceLocation("ambient.underwater.exit")),
        SoundCategory.PLAYERS, 1, (float) ((Math.random() / 5) + 0.9), false);

        if (!entity.inventory.hasItemStack(ModItems.WATERSKIN.get().getDefaultInstance()))
        {
            entity.setHeldItem(hand, ModItems.WATERSKIN.get().getDefaultInstance());
        }
        else
        {
            entity.addItemStackToInventory(ModItems.WATERSKIN.get().getDefaultInstance());
            itemstack.shrink(1);
        }
        entity.swingArm(hand);

        if (world instanceof ServerWorld)
            ((ServerWorld) world).spawnParticle(ParticleTypes.FALLING_WATER, entity.getPosX(), (entity.getPosY() + (entity.getHeight())), entity.getPosZ(), (int) 50, 0.3, 0.3, 0.3, 0.05);
        return ar;
    }

}
