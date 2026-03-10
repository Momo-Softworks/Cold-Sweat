package com.momosoftworks.coldsweat.api.temperature.effect.entity;

import com.momosoftworks.coldsweat.api.temperature.effect.TempEffect;
import com.momosoftworks.coldsweat.api.temperature.effect.TempEffectType;
import com.momosoftworks.coldsweat.data.codec.util.IntegerBounds;
import com.momosoftworks.coldsweat.util.math.CSMath;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.item.ItemEntity;
import net.minecraftforge.event.entity.living.LivingDropsEvent;
import net.minecraftforge.eventbus.api.SubscribeEvent;

import java.util.Collection;

public class DecreaseDropsEffect extends TempEffect
{
    public DecreaseDropsEffect(TempEffectType<?> type, IntegerBounds bounds)
    {   super(type, bounds);
    }

    @SubscribeEvent
    public void onDropItems(LivingDropsEvent event)
    {
        LivingEntity entity = event.getEntityLiving();
        if (!this.test(entity)) return;

        Collection<ItemEntity> drops = event.getDrops();
        int totalCount = drops.stream().map(item -> item.getItem().getCount()).reduce(0, Integer::sum);
        int removedDrops = CSMath.ceil(CSMath.blend(0, totalCount, this.getTemperature(entity), this.bounds().min(), this.bounds().max()));
        // Remove random drops
        while (removedDrops > 0 && !drops.isEmpty())
        {
            int index = (int) (Math.random() * drops.size());
            ItemEntity drop = drops.stream().skip(index).findFirst().orElse(null);
            if (drop != null)
            {
                int count = drop.getItem().getCount();
                if (count <= removedDrops)
                {   removedDrops -= count;
                    drops.remove(drop);
                }
                else
                {   drop.getItem().setCount(count - removedDrops);
                    removedDrops = 0;
                }
            }
        }
    }

    @Override
    public Side getSide()
    {   return Side.SERVER;
    }
}
