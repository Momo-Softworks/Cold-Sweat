package com.momosoftworks.coldsweat.core.itemgroup;

import com.momosoftworks.coldsweat.config.ConfigSettings;
import com.momosoftworks.coldsweat.util.registries.ModItems;
import net.minecraft.core.NonNullList;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.item.ArmorItem;
import net.minecraft.world.item.CreativeModeTab;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraftforge.registries.ForgeRegistries;
import org.jetbrains.annotations.NotNull;

import java.util.ArrayList;
import java.util.Comparator;
import java.util.List;
import java.util.Set;

public class InsulationItemsGroup extends CreativeModeTab
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
    public void fillItemList(@NotNull NonNullList<ItemStack> items)
    {
        // Spoof the item categories to allow items to be added to the tab
        List<List<ItemStack>> itemCategories = List.of(
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

    private static List<ItemStack> sort(Set<Item> items)
    {   List<ItemStack> list = new ArrayList<>(items.stream().map(Item::getDefaultInstance).toList());
        // Sort by name first
        list.sort(Comparator.comparing(item -> item.getDisplayName().getString()));
        // Sort by tags the items are in
        list.sort(Comparator.comparing(item -> ForgeRegistries.ITEMS.tags().getReverseTag(item.getItem()).orElse(null).getTagKeys().sequential().map(tag -> tag.location().toString()).reduce("", (a, b) -> a + b)));
        // Sort by armor material and slot
        list.sort(Comparator.comparing(item -> item.getItem() instanceof ArmorItem armor
                                               ? armor.getMaterial().getName() + (3 - LivingEntity.getEquipmentSlotForItem(item).getIndex())
                                               : ""));
        return list;
    }
}
