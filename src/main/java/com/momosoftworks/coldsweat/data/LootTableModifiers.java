package com.momosoftworks.coldsweat.data;

import com.momosoftworks.coldsweat.ColdSweat;
import com.momosoftworks.coldsweat.data.loot.modifier.AddDropsModifier;
import com.momosoftworks.coldsweat.data.loot.modifier.AddPiglinBartersModifier;
import net.minecraft.resources.Identifier;
import net.neoforged.bus.api.SubscribeEvent;
import net.neoforged.fml.common.EventBusSubscriber;
import net.neoforged.neoforge.registries.NeoForgeRegistries;
import net.neoforged.neoforge.registries.RegisterEvent;

@EventBusSubscriber
public class LootTableModifiers
{
    @SubscribeEvent
    public static void registerModifierSerializers(RegisterEvent event)
    {
        event.register(NeoForgeRegistries.Keys.GLOBAL_LOOT_MODIFIER_SERIALIZERS, helper -> {
            helper.register(Identifier.fromNamespaceAndPath(ColdSweat.MOD_ID, "mob_drops"), AddDropsModifier.CODEC);
            helper.register(Identifier.fromNamespaceAndPath(ColdSweat.MOD_ID, "piglin_barters"), AddPiglinBartersModifier.CODEC);
        });
    }
}
