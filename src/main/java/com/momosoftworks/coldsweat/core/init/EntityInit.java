package com.momosoftworks.coldsweat.core.init;

import com.momosoftworks.coldsweat.ColdSweat;
import com.momosoftworks.coldsweat.common.entity.ChameleonEntity;
import net.minecraft.entity.EntityClassification;
import net.minecraft.entity.EntityType;
import net.minecraft.util.ResourceLocation;
import net.minecraftforge.fml.RegistryObject;
import net.minecraftforge.registries.DeferredRegister;
import net.minecraftforge.registries.ForgeRegistries;

public class EntityInit
{
    public static final DeferredRegister<EntityType<?>> ENTITY_TYPES = DeferredRegister.create(ForgeRegistries.ENTITIES, ColdSweat.MOD_ID);

    //chameleon
    public static final RegistryObject<EntityType<ChameleonEntity>> CHAMELEON = ENTITY_TYPES.register("chameleon",
                                                                                                      () -> EntityType.Builder.of(ChameleonEntity::new, EntityClassification.CREATURE)
                                                                             .sized(0.75f, 0.65f)
                                                                             .build(new ResourceLocation(ColdSweat.MOD_ID, "chameleon").toString()));
}
