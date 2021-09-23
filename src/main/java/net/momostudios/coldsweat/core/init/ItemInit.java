package net.momostudios.coldsweat.core.init;

import net.minecraft.item.*;
import net.minecraftforge.fml.RegistryObject;
import net.minecraftforge.fml.common.Mod;
import net.minecraftforge.registries.DeferredRegister;
import net.minecraftforge.registries.ForgeRegistries;
import net.momostudios.coldsweat.ColdSweat;
import net.momostudios.coldsweat.common.block.BoilerBlock;
import net.momostudios.coldsweat.common.block.HearthBlock;
import net.momostudios.coldsweat.common.block.IceboxBlock;
import net.momostudios.coldsweat.common.block.SewingTableBlock;
import net.momostudios.coldsweat.common.item.FilledWaterskinItem;
import net.momostudios.coldsweat.common.item.MinecartInsulationItem;
import net.momostudios.coldsweat.common.item.WaterskinItem;
import net.momostudios.coldsweat.core.itemgroup.ColdSweatGroup;

@Mod.EventBusSubscriber
public class ItemInit
{
    public static final DeferredRegister<Item> ITEMS = DeferredRegister.create(ForgeRegistries.ITEMS, ColdSweat.MOD_ID);

    //Items
    public static final RegistryObject<Item> WATERSKIN_REGISTRY = ITEMS.register("waterskin", WaterskinItem::new);
    public static final RegistryObject<Item> FILLED_WATERSKIN_REGISTRY = ITEMS.register("filled_waterskin", FilledWaterskinItem::new);
    public static final RegistryObject<Item> THERMOMETER_REGISTRY = ITEMS.register("thermometer", () -> new Item((new Item.Properties()).group(ColdSweatGroup.COLD_SWEAT).rarity(Rarity.UNCOMMON)));
    public static final RegistryObject<Item> MINECART_INSULATION_REGISTRY = ITEMS.register("minecart_insulation", MinecartInsulationItem::new);
    public static final RegistryObject<Item> OBSIDIAN_SHARD_REGISTRY = ITEMS.register("obsidian_shard", () -> new Item((new Item.Properties()).group(ColdSweatGroup.COLD_SWEAT)));

    //BlockItems
    public static final RegistryObject<BlockItem> BOILER = ITEMS.register("boiler", () -> new BlockItem(ModBlocks.BOILER.get(), BoilerBlock.getItemProperties()));
    public static final RegistryObject<BlockItem> ICEBOX = ITEMS.register("icebox", () -> new BlockItem(ModBlocks.ICEBOX.get(), IceboxBlock.getItemProperties()));
    public static final RegistryObject<BlockItem> SEWING_TABLE = ITEMS.register("sewing_table", () -> new BlockItem(ModBlocks.SEWING_TABLE.get(), SewingTableBlock.getItemProperties()));
    public static final RegistryObject<BlockItem> HEARTH = ITEMS.register("hearth", () -> new BlockItem(ModBlocks.HEARTH.get(), HearthBlock.getItemProperties()));
}