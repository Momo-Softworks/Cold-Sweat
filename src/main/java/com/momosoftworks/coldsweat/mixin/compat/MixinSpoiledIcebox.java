package com.momosoftworks.coldsweat.mixin.compat;

import com.momosoftworks.coldsweat.ColdSweat;
import com.momosoftworks.coldsweat.common.blockentity.IceboxBlockEntity;
import com.mrbysco.spoiled.config.SpoiledConfigCache;
import com.mrbysco.spoiled.handler.SpoilHandler;
import net.minecraft.tileentity.TileEntity;
import net.minecraft.util.ResourceLocation;
import net.minecraft.util.math.BlockPos;
import net.minecraft.world.server.ServerWorld;
import net.minecraftforge.event.TickEvent;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.Redirect;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;
import org.spongepowered.asm.mixin.injection.callback.LocalCapture;

import java.util.Iterator;
import java.util.List;
import java.util.Map;

@Mixin(SpoilHandler.class)
public class MixinSpoiledIcebox
{
    private static TileEntity BE;
    @Inject(method = "onWorldTick", at = @At(value = "INVOKE", target = "Ljava/util/Map;get(Ljava/lang/Object;)Ljava/lang/Object;"),
            locals = LocalCapture.CAPTURE_FAILHARD, remap = false)
    private void storeBlockEntity(TickEvent.WorldTickEvent event, CallbackInfo ci, ServerWorld world, List<TileEntity> tileEntities, Iterator<TileEntity> iterator, TileEntity te, ResourceLocation location, double spoilRate)
    {   BE = te;
    }

    @Redirect(method = "onWorldTick", at = @At(value = "INVOKE", target = "Ljava/util/Map;get(Ljava/lang/Object;)Ljava/lang/Object;"),
              remap = false)
    private Object onFoodRot(Map<ResourceLocation, Double> instance, Object o)
    {
        if (BE instanceof IceboxBlockEntity && ((IceboxBlockEntity) BE).getFuel() > 0)
        {
            IceboxBlockEntity icebox = (IceboxBlockEntity) BE;
            icebox.setFuel(icebox.getFuel() - 1);
            return 0.0;
        }
        return 1.0;
    }

    @Mixin(SpoiledConfigCache.class)
    public static class ContainerModifier
    {
        @Inject(method = "generateContainerModifier", at = @At("TAIL"), remap = false)
        private static void addIceboxConfig(List<? extends String> configValues, CallbackInfo ci)
        {
            SpoiledConfigCache.containerModifier.put(new ResourceLocation(ColdSweat.MOD_ID, "icebox"), 0.5);
        }
    }
}
