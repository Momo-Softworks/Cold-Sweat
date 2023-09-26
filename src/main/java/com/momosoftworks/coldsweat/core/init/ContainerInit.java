package com.momosoftworks.coldsweat.core.init;

import net.minecraft.inventory.container.ContainerType;
import net.minecraftforge.common.extensions.IForgeContainerType;
import net.minecraftforge.fml.RegistryObject;
import net.minecraftforge.registries.DeferredRegister;
import net.minecraftforge.registries.ForgeRegistries;
import com.momosoftworks.coldsweat.ColdSweat;
import com.momosoftworks.coldsweat.common.container.BoilerContainer;
import com.momosoftworks.coldsweat.common.container.HearthContainer;
import com.momosoftworks.coldsweat.common.container.IceboxContainer;
import com.momosoftworks.coldsweat.common.container.SewingContainer;

public class ContainerInit
{
    public static final DeferredRegister<ContainerType<?>> MENU_TYPES = DeferredRegister.create(ForgeRegistries.CONTAINERS, ColdSweat.MOD_ID);

    public static final RegistryObject<ContainerType<BoilerContainer>> BOILER_CONTAINER_TYPE =
            MENU_TYPES.register("boiler", () -> IForgeContainerType.create(BoilerContainer::new));

    public static final RegistryObject<ContainerType<IceboxContainer>> ICEBOX_CONTAINER_TYPE =
        MENU_TYPES.register("ice_box", () -> IForgeContainerType.create(IceboxContainer::new));

    public static final RegistryObject<ContainerType<SewingContainer>> SEWING_CONTAINER_TYPE =
        MENU_TYPES.register("sewing_table", () -> IForgeContainerType.create(SewingContainer::new));

    public static final RegistryObject<ContainerType<HearthContainer>> HEARTH_CONTAINER_TYPE =
        MENU_TYPES.register("hearth", () -> IForgeContainerType.create(HearthContainer::new));
}
