package com.momosoftworks.coldsweat.client.renderer;

import com.momosoftworks.coldsweat.ColdSweat;
import com.momosoftworks.coldsweat.client.renderer.animation.AnimationBatch;
import net.minecraft.util.ResourceLocation;

public class ChameleonAnimations
{
    public static AnimationBatch WALK = AnimationBatch.loadFromFile(new ResourceLocation(ColdSweat.MOD_ID, "animation/chameleon.animation.walk.json"));
    public static AnimationBatch IDLE = AnimationBatch.loadFromFile(new ResourceLocation(ColdSweat.MOD_ID, "animation/chameleon.animation.idle.json"));
    public static AnimationBatch EAT  = AnimationBatch.loadFromFile(new ResourceLocation(ColdSweat.MOD_ID,  "animation/chameleon.animation.eat.json"));
    public static AnimationBatch RIDE = AnimationBatch.loadFromFile(new ResourceLocation(ColdSweat.MOD_ID,  "animation/chameleon.animation.ride.json"));
}
