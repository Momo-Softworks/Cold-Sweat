package dev.momosoftworks.coldsweat.core.init;

import net.minecraft.world.inventory.MenuType;
import net.minecraftforge.common.extensions.IForgeMenuType;
import net.minecraftforge.registries.DeferredRegister;
import net.minecraftforge.registries.ForgeRegistries;
import dev.momosoftworks.coldsweat.ColdSweat;
import dev.momosoftworks.coldsweat.common.container.BoilerContainer;
import dev.momosoftworks.coldsweat.common.container.HearthContainer;
import dev.momosoftworks.coldsweat.common.container.IceboxContainer;
import dev.momosoftworks.coldsweat.common.container.SewingContainer;
import net.minecraftforge.registries.RegistryObject;

public class MenuInit
{
    public static final DeferredRegister<MenuType<?>> MENU_TYPES = DeferredRegister.create(ForgeRegistries.MENU_TYPES, ColdSweat.MOD_ID);

    public static final RegistryObject<MenuType<BoilerContainer>> BOILER_CONTAINER_TYPE =
            MENU_TYPES.register("boiler", () -> IForgeMenuType.create(BoilerContainer::new));

    public static final RegistryObject<MenuType<IceboxContainer>> ICEBOX_CONTAINER_TYPE =
        MENU_TYPES.register("ice_box", () -> IForgeMenuType.create(IceboxContainer::new));

    public static final RegistryObject<MenuType<SewingContainer>> SEWING_CONTAINER_TYPE =
        MENU_TYPES.register("sewing_table", () -> IForgeMenuType.create(SewingContainer::new));

    public static final RegistryObject<MenuType<HearthContainer>> HEARTH_CONTAINER_TYPE =
        MENU_TYPES.register("hearth", () -> IForgeMenuType.create(HearthContainer::new));
}
