package com.momosoftworks.coldsweat;

import com.momosoftworks.coldsweat.client.event.ClientJoinSetup;
import com.momosoftworks.coldsweat.client.gui.Overlays;
import com.momosoftworks.coldsweat.common.event.EntityTempManager;
import com.momosoftworks.coldsweat.common.event.RegisterDispenserBehaviors;
import com.momosoftworks.coldsweat.config.ClientSettingsConfig;
import com.momosoftworks.coldsweat.config.ColdSweatConfig;
import com.momosoftworks.coldsweat.config.ItemSettingsConfig;
import com.momosoftworks.coldsweat.config.WorldSettingsConfig;
import com.momosoftworks.coldsweat.core.network.ColdSweatPacketHandler;
import com.momosoftworks.coldsweat.util.compat.CompatManager;
import com.momosoftworks.coldsweat.util.registries.ModItems;
import com.momosoftworks.coldsweat.util.registries.crafting.ModRecipes;
import cpw.mods.fml.common.FMLCommonHandler;
import cpw.mods.fml.common.event.FMLPostInitializationEvent;
import cpw.mods.fml.common.event.FMLPreInitializationEvent;
import cpw.mods.fml.common.event.FMLServerStartedEvent;
import com.momosoftworks.coldsweat.core.init.TempModifierInit;
import com.momosoftworks.coldsweat.util.world.TaskScheduler;
import cpw.mods.fml.common.Mod;
import cpw.mods.fml.common.Mod.EventHandler;
import cpw.mods.fml.common.event.FMLInitializationEvent;
import cpw.mods.fml.common.network.NetworkRegistry;
import net.minecraft.creativetab.CreativeTabs;
import net.minecraft.item.Item;
import net.minecraftforge.common.MinecraftForge;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

@Mod(modid = ColdSweat.MOD_ID, version = ColdSweat.VERSION)
public class ColdSweat
{
    public static final String MOD_ID = "cold_sweat";
    public static final String VERSION = "2.2.1";
    public static final Logger LOGGER = LogManager.getLogger();

    public static CreativeTabs TAB_COLD_SWEAT = new CreativeTabs("tab_cold_sweat")
    {   @Override
        public Item getTabIconItem()
        {   return ModItems.FILLED_WATERSKIN;
        }
    };
    
    @EventHandler
    public void init(FMLInitializationEvent event)
    {
        // Event handler classes
        regEventHandler(this);
        regEventHandler(new EntityTempManager());
        regEventHandler(new TaskScheduler());
        regEventHandler(new TempModifierInit());
        regEventHandler(new CompatManager());
        regEventHandler(new TaskScheduler());
        regEventHandler(new Overlays());
        regEventHandler(new ColdSweatConfig());
        regEventHandler(new WorldSettingsConfig());
        regEventHandler(new ItemSettingsConfig());
        regEventHandler(new ClientSettingsConfig());
        regEventHandler(new ClientJoinSetup());

        // Registration
        ModItems.init();
        ModRecipes.registerRecipes();

        ColdSweatPacketHandler.CHANNEL = NetworkRegistry.INSTANCE.newSimpleChannel(ColdSweatPacketHandler.NETWORK_ID);
        ColdSweatPacketHandler.registerMessages();
    }

    void regEventHandler(Object eventHandler)
    {   MinecraftForge.EVENT_BUS.register(eventHandler);
        FMLCommonHandler.instance().bus().register(eventHandler);
    }

    @EventHandler
    public static void preInit(FMLPreInitializationEvent event)
    {   String configDir = event.getModConfigurationDirectory().toString();
        ColdSweatConfig.init(configDir);
        WorldSettingsConfig.init(configDir);
        ItemSettingsConfig.init(configDir);
        ClientSettingsConfig.init(configDir);
    }

    @EventHandler
    public static void postInit(FMLPostInitializationEvent event)
    {   RegisterDispenserBehaviors.register();
    }

    @EventHandler
    public void serverStarted(FMLServerStartedEvent event)
    {   TempModifierInit.buildRegistries();
    }
}
