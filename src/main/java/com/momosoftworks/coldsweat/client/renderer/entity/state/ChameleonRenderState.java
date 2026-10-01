package com.momosoftworks.coldsweat.client.renderer.entity.state;

import com.momosoftworks.coldsweat.common.entity.Chameleon;
import net.minecraft.client.model.geom.PartPose;
import net.minecraft.client.renderer.entity.state.LivingEntityRenderState;

import javax.annotation.Nullable;
import java.util.HashMap;
import java.util.Map;

public class ChameleonRenderState extends LivingEntityRenderState
{
    /**
     * The chameleon's animations accumulate state on the entity itself, so the renderer keeps a reference to it
     */
    @Nullable
    public Chameleon chameleon;
    /** Model part poses, computed once per frame in the renderer */
    public final Map<String, PartPose> partPoses = new HashMap<>();
    public boolean tongueVisible;
    public float opacity = 1f;

    // Riding a player's head
    public boolean ridingPlayer;
    public float ridingYaw;
    public float ridingPitch;
    public float ridingOffset;
}
