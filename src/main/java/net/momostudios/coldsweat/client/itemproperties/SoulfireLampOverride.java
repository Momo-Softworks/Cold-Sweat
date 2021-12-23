package net.momostudios.coldsweat.client.itemproperties;

import net.minecraft.client.Minecraft;
import net.minecraft.client.audio.SimpleSound;
import net.minecraft.client.world.ClientWorld;
import net.minecraft.entity.LivingEntity;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.item.IItemPropertyGetter;
import net.minecraft.item.ItemStack;
import net.minecraft.util.SoundEvent;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.api.distmarker.OnlyIn;
import net.momostudios.coldsweat.client.event.AmbientGaugeDisplay;
import net.momostudios.coldsweat.config.ClientSettingsConfig;
import net.momostudios.coldsweat.config.ConfigCache;
import net.momostudios.coldsweat.core.util.MathHelperCS;
import net.momostudios.coldsweat.core.util.PlayerTemp;
import net.momostudios.coldsweat.core.util.Units;
import net.momostudios.coldsweat.core.util.registrylists.ModSounds;

import javax.annotation.Nullable;

@OnlyIn(Dist.CLIENT)
public class SoulfireLampOverride implements IItemPropertyGetter
{
    @Override
    public float call(ItemStack stack, @Nullable ClientWorld world, @Nullable LivingEntity entity)
    {
        if (world == null && Minecraft.getInstance().player != null)
            world = Minecraft.getInstance().player.worldClient;

        double max = MathHelperCS.convertUnits(ConfigCache.getInstance().maxTemp, Units.MC, ClientSettingsConfig.getInstance().celsius() ? Units.C : Units.F, true);
        double min = MathHelperCS.convertUnits(ConfigCache.getInstance().minTemp, Units.MC, ClientSettingsConfig.getInstance().celsius() ? Units.C : Units.F, true);

        if (stack.getOrCreateTag().getInt("fuel") > 0 && world != null && world.getDimensionKey().getLocation().getPath().equals("the_nether") &&
        (!(entity instanceof PlayerEntity) || -2.5 * (-AmbientGaugeDisplay.clientTemp - 0.5 * (-min - max) -0.4 * max) > max))
        {
            return stack.getOrCreateTag().getInt("fuel") > 43 ? 1 :
                   stack.getOrCreateTag().getInt("fuel") > 22 ? 2 : 3;
        }
        else
        {
            return 0;
        }
    }
}
