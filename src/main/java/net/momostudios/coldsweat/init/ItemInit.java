package net.momostudios.coldsweat.init;

import net.minecraft.item.BlockItem;
import net.minecraft.item.Item;
import net.minecraft.item.ItemGroup;
import net.minecraftforge.fml.RegistryObject;
import net.minecraftforge.registries.DeferredRegister;
import net.minecraftforge.registries.ForgeRegistries;
import net.momostudios.coldsweat.ColdSweat;
import net.momostudios.coldsweat.block.BoilerBlock;

public class ItemInit
{
    public static final DeferredRegister<Item> ITEMS = DeferredRegister.create(ForgeRegistries.ITEMS, ColdSweat.MOD_ID);

    //Items

    //BlockItems
    public static final RegistryObject<BlockItem> BOILER = ITEMS.register("boiler",
            () -> new BlockItem(BlockInit.BOILER.get(), new Item.Properties().group(ItemGroup.DECORATIONS)));
}