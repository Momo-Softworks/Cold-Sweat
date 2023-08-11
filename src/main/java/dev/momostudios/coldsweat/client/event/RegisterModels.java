package dev.momostudios.coldsweat.client.event;

import dev.momostudios.coldsweat.client.renderer.model.armor.*;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.fml.common.Mod;

@Mod.EventBusSubscriber(bus = Mod.EventBusSubscriber.Bus.MOD, value = Dist.CLIENT)
public class RegisterModels
{
    public static HoglinHeadpieceModel<?> HOGLIN_HEADPIECE_MODEL = new HoglinHeadpieceModel<>(1f);
    public static HoglinTunicModel<?> HOGLIN_TUNIC_MODEL = new HoglinTunicModel<>(1f);
    public static HoglinHoovesModel<?> HOGLIN_HOOVES_MODEL = new HoglinHoovesModel<>(1f);
    public static HoglinTrousersModel<?> HOGLIN_TROUSERS_MODEL = new HoglinTrousersModel<>(1f);

    //public static GoatCapModel<?> GOAT_CAP_MODEL = null;
    //public static GoatParkaModel<?> GOAT_PARKA_MODEL = null;
    //public static GoatPantsModel<?> GOAT_PANTS_MODEL = null;
    //public static GoatBootsModel<?> GOAT_BOOTS_MODEL = null;
}
