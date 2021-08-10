package net.momostudios.coldsweat.util.init;

import net.minecraft.inventory.container.ContainerType;
import net.minecraftforge.common.extensions.IForgeContainerType;
import net.minecraftforge.fml.RegistryObject;
import net.minecraftforge.registries.DeferredRegister;
import net.minecraftforge.registries.ForgeRegistries;
import net.momostudios.coldsweat.ColdSweat;
import net.momostudios.coldsweat.block.container.BoilerContainer;

public class ContainerInit
{
    public static final DeferredRegister<ContainerType<?>> CONTAINER_TYPES = DeferredRegister.create(ForgeRegistries.CONTAINERS, ColdSweat.MOD_ID);
    public static final RegistryObject<ContainerType<BoilerContainer>> BOILER_CONTAINER_TYPE =
            CONTAINER_TYPES.register("boiler", () -> IForgeContainerType.create(BoilerContainer::new));
}
