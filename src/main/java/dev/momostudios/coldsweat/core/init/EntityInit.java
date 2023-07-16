package dev.momostudios.coldsweat.core.init;

import dev.momostudios.coldsweat.ColdSweat;
import dev.momostudios.coldsweat.common.entity.Chameleon;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.entity.MobCategory;
import net.minecraftforge.registries.DeferredRegister;
import net.minecraftforge.registries.ForgeRegistries;
import net.minecraftforge.registries.RegistryObject;

public class EntityInit
{
    public static final DeferredRegister<EntityType<?>> ENTITY_TYPES = DeferredRegister.create(ForgeRegistries.ENTITY_TYPES, ColdSweat.MOD_ID);

    //chameleon
    public static final RegistryObject<EntityType<Chameleon>> CHAMELEON = ENTITY_TYPES.register("chameleon",
                                                                                                () -> EntityType.Builder.of(Chameleon::new, MobCategory.CREATURE).sized(0.75f, 0.65f).build(new ResourceLocation(ColdSweat.MOD_ID, "chameleon").toString()));
}
