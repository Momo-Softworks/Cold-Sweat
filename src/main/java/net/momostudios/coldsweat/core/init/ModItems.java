package net.momostudios.coldsweat.core.init;

import net.minecraft.item.BlockItem;
import net.minecraft.item.Item;
import net.minecraftforge.fml.RegistryObject;
import net.minecraftforge.registries.DeferredRegister;
import net.minecraftforge.registries.ForgeRegistries;
import net.momostudios.coldsweat.ColdSweat;
import net.momostudios.coldsweat.common.block.BoilerBlock;
import net.momostudios.coldsweat.common.item.FilledWaterskinItem;
import net.momostudios.coldsweat.common.item.WaterskinItem;

public class ModItems
{
    public static final DeferredRegister<Item> ITEMS = DeferredRegister.create(ForgeRegistries.ITEMS, ColdSweat.MOD_ID);

    //Items
    public static final RegistryObject<Item> WATERSKIN = ITEMS.register("waterskin", () -> new WaterskinItem());
    public static final RegistryObject<Item> FILLED_WATERSKIN = ITEMS.register("filled_waterskin", () -> new Item(FilledWaterskinItem.getProperties()));

    //BlockItems
    public static final RegistryObject<BlockItem> BOILER = ITEMS.register("boiler", () -> new BlockItem(ModBlocks.BOILER.get(), BoilerBlock.getItemProperties()));
}