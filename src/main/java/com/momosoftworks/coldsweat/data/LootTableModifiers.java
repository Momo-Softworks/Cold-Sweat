package com.momosoftworks.coldsweat.data;

import com.momosoftworks.coldsweat.ColdSweat;
import com.momosoftworks.coldsweat.data.loot.modifier.AddDropsModifier;
import com.momosoftworks.coldsweat.data.loot.modifier.AddPiglinBartersModifier;
import net.minecraft.resources.ResourceLocation;
import net.minecraftforge.eventbus.api.SubscribeEvent;
import net.minecraftforge.fml.common.Mod;
import net.minecraftforge.registries.ForgeRegistries;
import net.minecraftforge.registries.RegisterEvent;

@Mod.EventBusSubscriber(bus = Mod.EventBusSubscriber.Bus.MOD)
public class LootTableModifiers
{
    @SubscribeEvent
    public static void registerModifierSerializers(RegisterEvent event)
    {
        event.register(ForgeRegistries.Keys.GLOBAL_LOOT_MODIFIER_SERIALIZERS, helper -> {
            helper.register(new ResourceLocation(ColdSweat.MOD_ID, "mob_drops"), AddDropsModifier.CODEC);
            helper.register(new ResourceLocation(ColdSweat.MOD_ID, "piglin_barters"), AddPiglinBartersModifier.CODEC);
        });
    }
}
