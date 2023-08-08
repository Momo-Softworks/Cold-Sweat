package dev.momostudios.coldsweat.mixin;

import com.mojang.brigadier.StringReader;
import dev.momostudios.coldsweat.ColdSweat;
import dev.momostudios.coldsweat.util.serialization.FloatAtMost;
import net.minecraft.advancements.criterion.MinMaxBounds;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

import javax.annotation.Nullable;

public abstract class MixinTriggerBounds
{
    @Mixin(MinMaxBounds.FloatBound.class)
    public static class MixinFloatBound implements FloatAtMost
    {

        MinMaxBounds.FloatBound self = (MinMaxBounds.FloatBound) (Object) this;

        @Shadow
        private static MinMaxBounds.FloatBound create(StringReader p_211352_0_, @Nullable Float p_211352_1_, @Nullable Float p_211352_2_)
        {   return null;
        }

        @Inject(method = "matches", at = @At("HEAD"), cancellable = true, remap = ColdSweat.REMAP_MIXINS)
        public void matches(float value, CallbackInfoReturnable<Boolean> cir)
        {
            Float min = self.getMin();
            Float max = self.getMax();
            cir.setReturnValue((min == null || value >= min) && (max == null || value <= max));
        }

        @Override
        public MinMaxBounds.FloatBound atMost(Float max)
        {   return create(null, null, max);
        }
    }
}
