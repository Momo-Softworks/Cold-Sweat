package com.momosoftworks.coldsweat.data;

import com.momosoftworks.coldsweat.ColdSweat;
import com.momosoftworks.coldsweat.data.loot.modifier.AddDropsModifier;
import com.momosoftworks.coldsweat.data.loot.modifier.AddPiglinBartersModifier;
import net.minecraft.resources.ResourceLocation;
import net.minecraftforge.common.loot.GlobalLootModifierSerializer;
import net.minecraftforge.event.RegistryEvent;
import net.minecraftforge.eventbus.api.SubscribeEvent;
import net.minecraftforge.fml.common.Mod;

@Mod.EventBusSubscriber(bus = Mod.EventBusSubscriber.Bus.MOD)
public class LootTableModifiers
{
    @SubscribeEvent
    public static void registerModifierSerializers(RegistryEvent.Register<GlobalLootModifierSerializer<?>> event)
    {
        event.getRegistry().register(new AddDropsModifier.Serializer().setRegistryName(new ResourceLocation(ColdSweat.MOD_ID, "mob_drops")));
        event.getRegistry().register(new AddPiglinBartersModifier.Serializer().setRegistryName(new ResourceLocation(ColdSweat.MOD_ID, "piglin_barters")));
    }
}
