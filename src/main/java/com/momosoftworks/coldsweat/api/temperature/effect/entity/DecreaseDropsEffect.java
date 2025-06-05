package com.momosoftworks.coldsweat.api.temperature.effect.entity;

import com.momosoftworks.coldsweat.api.temperature.effect.TempEffect;
import com.momosoftworks.coldsweat.data.codec.util.IntegerBounds;
import com.momosoftworks.coldsweat.util.math.CSMath;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.item.ItemEntity;
import net.neoforged.bus.api.SubscribeEvent;
import net.neoforged.neoforge.event.entity.living.LivingDropsEvent;

import java.util.Collection;

public class DecreaseDropsEffect extends TempEffect
{
    public DecreaseDropsEffect(LivingEntity entity, IntegerBounds range)
    {   super(entity, range);
    }

    @SubscribeEvent
    public void onDropItems(LivingDropsEvent event)
    {
        if (!this.test(event.getEntity())) return;

        Collection<ItemEntity> drops = event.getDrops();
        int totalCount = drops.stream().map(item -> item.getItem().getCount()).reduce(0, Integer::sum);
        int removedDrops = CSMath.ceil(CSMath.blend(0, totalCount, this.getTemperature(), this.bounds().min(), this.bounds().max()));
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
    public boolean isClient()
    {   return false;
    }
}
