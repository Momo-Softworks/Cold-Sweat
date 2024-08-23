package com.momosoftworks.coldsweat.core.init;

import com.momosoftworks.coldsweat.ColdSweat;
import com.momosoftworks.coldsweat.common.entity.ChameleonEntity;
import com.momosoftworks.coldsweat.common.entity.GoatEntity;
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

    // goat
    public static final RegistryObject<EntityType<GoatEntity>> GOAT = ENTITY_TYPES.register("goat",
                                                                                            () -> EntityType.Builder.of(GoatEntity::new, EntityClassification.CREATURE)
                                                                          .sized(0.9F, 1.3F)
                                                                          .clientTrackingRange(10)
                                                                          .build(new ResourceLocation(ColdSweat.MOD_ID, "goat").toString()));
}
