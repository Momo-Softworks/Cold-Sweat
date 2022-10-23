package dev.momostudios.coldsweat.api.event.client;

import com.mojang.blaze3d.vertex.PoseStack;
import com.mojang.math.Matrix4f;
import net.minecraft.client.Camera;
import net.minecraft.client.Minecraft;
import net.minecraft.client.renderer.GameRenderer;
import net.minecraft.client.renderer.LevelRenderer;
import net.minecraft.client.renderer.LightTexture;
import net.minecraft.client.renderer.culling.Frustum;
import net.minecraft.world.level.Level;
import net.minecraftforge.eventbus.api.Cancelable;
import net.minecraftforge.eventbus.api.Event;

@Cancelable
public class RenderLevelEvent extends Event
{
    Level level;
    PoseStack poseStack;
    float partialTicks;
    long nanoTime;
    boolean renderBlockOutline;
    Camera camera;
    LevelRenderer levelRenderer;
    GameRenderer renderer;
    LightTexture lightTexture;
    Matrix4f lastMatrix;
    Frustum frustum;

    public RenderLevelEvent(PoseStack poseStack, float partialTicks, long nanoTime, boolean renderBlockOutline,
                            Camera camera, LevelRenderer levelRenderer, GameRenderer gameRenderer,
                            LightTexture lightTexture, Matrix4f matrix4f, Frustum frustum)
    {
        this.level = Minecraft.getInstance().level;
        this.poseStack = poseStack;
        this.partialTicks = partialTicks;
        this.nanoTime = nanoTime;
        this.renderBlockOutline = renderBlockOutline;
        this.camera = camera;
        this.levelRenderer = levelRenderer;
        this.renderer = gameRenderer;
        this.lightTexture = lightTexture;
        this.lastMatrix = matrix4f;
        this.frustum = frustum;
    }

    public Level getLevel()
    {
        return level;
    }

    public PoseStack getPoseStack()
    {
        return poseStack;
    }

    public float getPartialTicks()
    {
        return partialTicks;
    }

    public long getNanoTime()
    {
        return nanoTime;
    }

    public boolean renderBlockOutline()
    {
        return renderBlockOutline;
    }

    public Camera getCamera()
    {
        return camera;
    }

    public LevelRenderer getLevelRenderer()
    {
        return levelRenderer;
    }

    public GameRenderer getRenderer()
    {
        return renderer;
    }

    public LightTexture getLightTexture()
    {
        return lightTexture;
    }

    public Matrix4f getLastMatrix()
    {
        return lastMatrix;
    }

    public Frustum getFrustum()
    {
        return frustum;
    }
}
