package com.momosoftworks.coldsweat.util.registries;

import com.momosoftworks.coldsweat.common.entity.ChameleonEntity;
import com.momosoftworks.coldsweat.common.entity.GoatEntity;
import com.momosoftworks.coldsweat.core.init.EntityInit;
import net.minecraft.entity.EntityType;

public class ModEntities
{
    public static final EntityType<ChameleonEntity> CHAMELEON = EntityInit.CHAMELEON.get();
    public static final EntityType<GoatEntity> GOAT = EntityInit.GOAT.get();
}
