package net.momostudios.coldsweat.core.init;

import net.minecraft.item.BlockItem;
import net.minecraft.item.Item;
import net.minecraftforge.fml.RegistryObject;
import net.minecraftforge.registries.DeferredRegister;
import net.minecraftforge.registries.ForgeRegistries;
import net.momostudios.coldsweat.ColdSweat;
import net.momostudios.coldsweat.common.block.BoilerBlock;
import net.momostudios.coldsweat.common.block.IceboxBlock;
import net.momostudios.coldsweat.common.block.SewingTableBlock;
import net.momostudios.coldsweat.common.item.FilledWaterskinItem;
import net.momostudios.coldsweat.common.item.WaterskinItem;

public class ModItems
{
    public static final DeferredRegister<Item> ITEMS = DeferredRegister.create(ForgeRegistries.ITEMS, ColdSweat.MOD_ID);

    //Items
    public static final RegistryObject<Item> WATERSKIN = ITEMS.register("waterskin", WaterskinItem::new);
    public static final RegistryObject<Item> FILLED_WATERSKIN = ITEMS.register("filled_waterskin", FilledWaterskinItem::new);

    //BlockItems
    public static final RegistryObject<BlockItem> BOILER = ITEMS.register("boiler", () -> new BlockItem(ModBlocks.BOILER.get(), BoilerBlock.getItemProperties()));
    public static final RegistryObject<BlockItem> ICEBOX = ITEMS.register("icebox", () -> new BlockItem(ModBlocks.ICEBOX.get(), IceboxBlock.getItemProperties()));
    public static final RegistryObject<BlockItem> SEWING_TABLE = ITEMS.register("sewing_table", () -> new BlockItem(ModBlocks.SEWING_TABLE.get(), SewingTableBlock.getItemProperties()));
}