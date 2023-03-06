package dev.momostudios.coldsweat.core.init;

import dev.momostudios.coldsweat.common.item.*;
import net.minecraft.world.item.BlockItem;
import net.minecraft.world.item.CreativeModeTab;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.Rarity;
import net.minecraftforge.common.ForgeSpawnEggItem;
import net.minecraftforge.registries.DeferredRegister;
import net.minecraftforge.registries.ForgeRegistries;
import dev.momostudios.coldsweat.ColdSweat;
import dev.momostudios.coldsweat.common.block.BoilerBlock;
import dev.momostudios.coldsweat.common.block.HearthBottomBlock;
import dev.momostudios.coldsweat.common.block.IceboxBlock;
import dev.momostudios.coldsweat.common.block.SewingTableBlock;
import dev.momostudios.coldsweat.core.itemgroup.ColdSweatGroup;
import net.minecraftforge.registries.RegistryObject;

public class ItemInit
{
    public static final DeferredRegister<Item> ITEMS = DeferredRegister.create(ForgeRegistries.ITEMS, ColdSweat.MOD_ID);

    //Items
    public static final RegistryObject<Item> WATERSKIN = ITEMS.register("waterskin", WaterskinItem::new);
    public static final RegistryObject<Item> FILLED_WATERSKIN = ITEMS.register("filled_waterskin", FilledWaterskinItem::new);
    public static final RegistryObject<Item> MINECART_INSULATION = ITEMS.register("minecart_insulation", MinecartInsulationItem::new);
    public static final RegistryObject<Item> THERMOMETER = ITEMS.register("thermometer", () ->
            new Item(new Item.Properties().tab(ColdSweatGroup.COLD_SWEAT).rarity(Rarity.UNCOMMON)));
    public static final RegistryObject<Item> SOULSPRING_LAMP = ITEMS.register("soulspring_lamp", SoulspringLampItem::new);
    public static final RegistryObject<Item> GOAT_FUR = ITEMS.register("goat_fur", () ->
            new Item(new Item.Properties().tab(ColdSweatGroup.COLD_SWEAT)));
    public static final RegistryObject<Item> HOGLIN_HIDE = ITEMS.register("hoglin_hide", () ->
            new Item(new Item.Properties().tab(ColdSweatGroup.COLD_SWEAT)));
    public static final RegistryObject<Item> INSULATED_MINECART = ITEMS.register("insulated_minecart", () ->
            new InsulatedMinecartItem(new Item.Properties().tab(ColdSweatGroup.COLD_SWEAT)));
    public static final RegistryObject<Item> CHAMELEON_MOLT = ITEMS.register("chameleon_molt", () ->
            new Item(new Item.Properties().tab(ColdSweatGroup.COLD_SWEAT)));

    //BlockItems
    public static final RegistryObject<BlockItem> BOILER = ITEMS.register("boiler", () -> new BlockItem(BlockInit.BOILER.get(), BoilerBlock.getItemProperties()));
    public static final RegistryObject<BlockItem> ICEBOX = ITEMS.register("icebox", () -> new BlockItem(BlockInit.ICEBOX.get(), IceboxBlock.getItemProperties()));
    public static final RegistryObject<BlockItem> SEWING_TABLE = ITEMS.register("sewing_table", () -> new BlockItem(BlockInit.SEWING_TABLE.get(), SewingTableBlock.getItemProperties()));
    public static final RegistryObject<BlockItem> HEARTH = ITEMS.register("hearth", () -> new BlockItem(BlockInit.HEARTH_BOTTOM.get(), HearthBottomBlock.getItemProperties()));
    public static final RegistryObject<BlockItem> THERMOLITH = ITEMS.register("thermolith", () -> new BlockItem(BlockInit.THERMOLITH.get(), ThermolithBlock.getItemProperties()));

    //Spawn Eggs
    public static final RegistryObject<ForgeSpawnEggItem> CHAMELEON_SPAWN_EGG = ITEMS.register("chameleon_spawn_egg", () ->
            new ForgeSpawnEggItem(EntityInit.CHAMELEON, 0x82C841, 0x1C9170, new Item.Properties().tab(CreativeModeTab.TAB_MISC)));
}
