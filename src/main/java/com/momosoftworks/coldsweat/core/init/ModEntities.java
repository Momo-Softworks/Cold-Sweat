package com.momosoftworks.coldsweat.core.init;

import com.momosoftworks.coldsweat.ColdSweat;
import com.momosoftworks.coldsweat.common.entity.Chameleon;
import net.minecraft.core.registries.Registries;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.entity.MobCategory;
import net.neoforged.neoforge.registries.DeferredHolder;
import net.neoforged.neoforge.registries.DeferredRegister;

public class ModEntities
{
    public static final DeferredRegister<EntityType<?>> ENTITY_TYPES = DeferredRegister.create(Registries.ENTITY_TYPE, ColdSweat.MOD_ID);

    //chameleon
    public static final DeferredHolder<EntityType<?>, EntityType<Chameleon>> CHAMELEON = ENTITY_TYPES.register("chameleon",
                        () -> EntityType.Builder.of(Chameleon::new, MobCategory.CREATURE).sized(0.75f, 0.65f).build(ResourceLocation.fromNamespaceAndPath(ColdSweat.MOD_ID, "chameleon").toString()));
}
