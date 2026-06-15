package com.momosoftworks.coldsweat.api.temperature.modifier;

import com.momosoftworks.coldsweat.api.util.Temperature;
import com.momosoftworks.coldsweat.config.ConfigSettings;
import com.momosoftworks.coldsweat.util.math.CSMath;
import net.minecraft.entity.EntityLivingBase;
import net.minecraft.world.EnumSkyBlock;
import net.minecraft.world.World;

import java.util.function.Function;

/**
 * Applies a temperature offset when the entity is shaded from the sky (blocks overhead or overcast weather).<br>
 * Ported from 1.16's {@code ShadeTempModifier}.
 */
public class ShadeTempModifier extends TempModifier
{
    public ShadeTempModifier() {}

    @Override
    protected Function<Double, Double> calculate(EntityLivingBase entity, Temperature.Type type)
    {
        World world = entity.worldObj;
        // No sky (Nether/End) => no shade effect
        if (world.provider.hasNoSky) return temp -> temp;

        int x = (int) Math.floor(entity.posX);
        int y = (int) Math.floor(entity.posY + entity.getEyeHeight());
        int z = (int) Math.floor(entity.posZ);

        int skyLight = world.getSavedLightValue(EnumSkyBlock.Sky, x, y, z);
        double darkness = 1 - (skyLight / 15.0);
        double overcast = world.getRainStrength(1f);
        double shade = Math.max(darkness, overcast);

        double shadeAmount = CSMath.blend(0, ConfigSettings.SHADE_TEMP_OFFSET.get(), shade, 0, 1);
        return temp -> temp + shadeAmount;
    }

    public String getID()
    {   return "cold_sweat:shade";
    }
}
