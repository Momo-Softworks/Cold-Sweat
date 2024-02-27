package com.momosoftworks.coldsweat.core.itemgroup;

import com.momosoftworks.coldsweat.config.ConfigSettings;
import com.momosoftworks.coldsweat.config.util.ItemData;
import com.momosoftworks.coldsweat.util.registries.ModItems;
import net.minecraft.entity.MobEntity;
import net.minecraft.item.ArmorItem;
import net.minecraft.item.Item;
import net.minecraft.item.ItemGroup;
import net.minecraft.item.ItemStack;
import net.minecraft.util.NonNullList;
import net.minecraft.util.ResourceLocation;

import java.util.*;
import java.util.stream.Collectors;

public class InsulationItemsGroup extends ItemGroup
{
    public static final InsulationItemsGroup INSULATION_ITEMS = new InsulationItemsGroup("cs_insulation_items");
    public InsulationItemsGroup(String label)
    {   super(label);
    }

    @Override
    public ItemStack makeIcon()
    {   return new ItemStack(ModItems.CHAMELEON_MOLT);
    }

    public void register()
    {   return;
    }

    @Override
    public void fillItemList(NonNullList<ItemStack> items)
    {
        // Spoof the item categories to allow items to be added to the tab
        List<List<ItemStack>> itemCategories = Arrays.asList(
                sort(ConfigSettings.INSULATION_ITEMS.get().keySet()),
                sort(ConfigSettings.INSULATING_ARMORS.get().keySet()),
                sort(ConfigSettings.INSULATING_CURIOS.get().keySet())
        );

        for (List<ItemStack> category : itemCategories)
        {
            for (ItemStack stack : category)
            {
                // Make a dummy item list to get the result of the item's fillItemCategory() method
                NonNullList<ItemStack> dummyList = NonNullList.create();
                stack.getItem().fillItemCategory(stack.getItem().getItemCategory(), dummyList);

                // Skip if this item is already in the tab
                if (!dummyList.isEmpty() && items.stream().noneMatch(item -> item.sameItem(dummyList.get(0))))
                {   items.add(dummyList.get(0));
                }
            }
        }
    }

    private static List<ItemStack> sort(Set<ItemData> items)
    {   List<ItemStack> list = new ArrayList<>(items.stream().map(data -> new ItemStack(data.getItem())).collect(Collectors.toList()));
        // Sort by name first
        list.sort(Comparator.comparing(item -> item.getDisplayName().getString()));
        // Sort by tags the items are in
        list.sort(Comparator.comparing(item -> item.getItem().getTags().stream().map(ResourceLocation::toString).reduce("", (a, b) -> a + b)));
        // Sort by armor material and slot
        list.sort(Comparator.comparing(item -> item.getItem() instanceof ArmorItem
                                               ? ((ArmorItem) item.getItem()).getMaterial().getName() + (3 - MobEntity.getEquipmentSlotForItem(item).getIndex())
                                               : ""));
        return list;
    }
}
