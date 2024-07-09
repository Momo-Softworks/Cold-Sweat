package com.momosoftworks.coldsweat.core.init;

import net.minecraft.core.registries.Registries;
import net.minecraft.world.inventory.MenuType;
import com.momosoftworks.coldsweat.ColdSweat;
import com.momosoftworks.coldsweat.common.container.BoilerContainer;
import com.momosoftworks.coldsweat.common.container.HearthContainer;
import com.momosoftworks.coldsweat.common.container.IceboxContainer;
import com.momosoftworks.coldsweat.common.container.SewingContainer;
import net.neoforged.neoforge.common.extensions.IMenuTypeExtension;
import net.neoforged.neoforge.registries.DeferredHolder;
import net.neoforged.neoforge.registries.DeferredRegister;

public class ModMenus
{
    public static final DeferredRegister<MenuType<?>> MENU_TYPES = DeferredRegister.create(Registries.MENU, ColdSweat.MOD_ID);

    public static final DeferredHolder<MenuType<?>, MenuType<BoilerContainer>> BOILER_CONTAINER_TYPE =
            MENU_TYPES.register("boiler", () -> IMenuTypeExtension.create(BoilerContainer::new));

    public static final DeferredHolder<MenuType<?>, MenuType<IceboxContainer>> ICEBOX_CONTAINER_TYPE =
        MENU_TYPES.register("ice_box", () -> IMenuTypeExtension.create(IceboxContainer::new));

    public static final DeferredHolder<MenuType<?>, MenuType<SewingContainer>> SEWING_CONTAINER_TYPE =
        MENU_TYPES.register("sewing_table", () -> IMenuTypeExtension.create(SewingContainer::new));

    public static final DeferredHolder<MenuType<?>, MenuType<HearthContainer>> HEARTH_CONTAINER_TYPE =
        MENU_TYPES.register("hearth", () -> IMenuTypeExtension.create(HearthContainer::new));
}
