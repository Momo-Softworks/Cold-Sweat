package com.momosoftworks.coldsweat.mixin;

import com.momosoftworks.coldsweat.common.capability.handler.EntityTempManager;
import com.momosoftworks.coldsweat.mixin_interface.IPassthrough;
import net.minecraft.world.entity.ai.attributes.Attribute;
import net.minecraft.world.entity.ai.attributes.AttributeInstance;
import net.minecraft.world.entity.ai.attributes.AttributeModifier;
import org.spongepowered.asm.mixin.Final;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.Unique;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

import java.util.Collection;

@Mixin(AttributeInstance.class)
public abstract class MixinAttributeValue implements IPassthrough
{
    @Shadow @Final private Attribute attribute;
    @Shadow private double baseValue;

    @Shadow protected abstract Collection<AttributeModifier> getModifiersOrEmpty(AttributeModifier.Operation pOperation);

    @Unique
    private double passthroughValue;
    @Unique
    private double realCachedValue;
    @Unique
    private boolean realDirty;

    @Inject(method = "getBaseValue", at = @At("HEAD"), cancellable = true)
    private void getBaseValue(CallbackInfoReturnable<Double> cir)
    {
        if (EntityTempManager.isTemperatureAttribute(this.attribute) && Double.isNaN(this.baseValue))
        {   cir.setReturnValue(this.passthroughValue);
        }
    }

    @Inject(method = "setDirty", at = @At("HEAD"))
    private void setDirty(CallbackInfo ci)
    {   this.realDirty = true;
    }

    @Override
    public double getPassthroughValue()
    {   return passthroughValue;
    }
    @Override
    public void setPassthroughValue(double value)
    {   this.passthroughValue = value;
    }

    @Override
    public double getRealBaseValue()
    {   return this.baseValue;
    }

    @Override
    public double getRealValue()
    {
        if (this.realDirty)
        {   this.realCachedValue = this.calculateRealValue();
            this.realDirty = false;
        }
        return this.realCachedValue;
    }

    private double calculateRealValue()
    {
        double base = this.getRealBaseValue();

        for (AttributeModifier attributemodifier : this.getModifiersOrEmpty(AttributeModifier.Operation.ADDITION))
        {   base += attributemodifier.getAmount();
        }
        double addedBase = base;

        for (AttributeModifier attributemodifier1 : this.getModifiersOrEmpty(AttributeModifier.Operation.MULTIPLY_BASE))
        {   addedBase += base * attributemodifier1.getAmount();
        }
        for (AttributeModifier attributemodifier2 : this.getModifiersOrEmpty(AttributeModifier.Operation.MULTIPLY_TOTAL))
        {   addedBase *= 1.0D + attributemodifier2.getAmount();
        }
        return this.attribute.sanitizeValue(addedBase);
    }
}
