package dev.momostudios.coldsweat.api.event.client;

import com.mojang.blaze3d.matrix.MatrixStack;
import net.minecraft.client.renderer.ActiveRenderInfo;
import net.minecraft.client.renderer.GameRenderer;
import net.minecraft.client.renderer.LightTexture;
import net.minecraft.client.world.ClientWorld;
import net.minecraft.util.math.vector.Matrix4f;
import net.minecraftforge.eventbus.api.Event;

public class RenderWorldEvent extends Event
{
    MatrixStack matrixStack;
    float partialTicks;
    long finishTimeNano;
    boolean drawBlockOutline;
    ActiveRenderInfo activeRenderInf;
    GameRenderer gameRenderer;
    LightTexture lightmap;
    Matrix4f projection;
    ClientWorld world;

    public RenderWorldEvent(MatrixStack matrixStack, float partialTicks, long finishTimeNano, boolean drawBlockOutline, ActiveRenderInfo activeRenderInf,
                            GameRenderer gameRenderer, LightTexture lightmap, Matrix4f projection, ClientWorld world)
    {
        this.matrixStack = matrixStack;
        this.partialTicks = partialTicks;
        this.finishTimeNano = finishTimeNano;
        this.drawBlockOutline = drawBlockOutline;
        this.activeRenderInf = activeRenderInf;
        this.gameRenderer = gameRenderer;
        this.lightmap = lightmap;
        this.projection = projection;
        this.world = world;
    }

    public MatrixStack getMatrixStack()
    {   return matrixStack;
    }

    public float getPartialTicks()
    {   return partialTicks;
    }

    public long getFinishTimeNano()
    {   return finishTimeNano;
    }

    public boolean getDrawBlockOutline()
    {   return drawBlockOutline;
    }

    public ActiveRenderInfo getActiveRenderInfo()
    {   return activeRenderInf;
    }

    public GameRenderer getGameRenderer()
    {   return gameRenderer;
    }

    public LightTexture getLightmap()
    {   return lightmap;
    }

    public Matrix4f getProjectionMatrix()
    {   return projection;
    }

    public ClientWorld getWorld()
    {   return world;
    }
}
