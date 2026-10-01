package com.momosoftworks.coldsweat.client.renderer;

import com.momosoftworks.coldsweat.ColdSweat;
import net.minecraft.client.Minecraft;
import net.minecraft.client.renderer.GameRenderer;
import net.minecraft.resources.Identifier;

import javax.annotation.Nullable;

/**
 * Drives Cold Sweat's screen post-effects through the vanilla post-effect slot.<br>
 * Post-effect uniforms are static since 1.21.5, so effects with variable strength are pre-baked
 * into several data-driven post effects (i.e. {@code cold_sweat:post_effect/heat_blur_1} to {@code heat_blur_12})
 */
public class PostProcessShaderManager
{
    private static final PostProcessShaderManager INSTANCE = new PostProcessShaderManager();

    public static final int MAX_HEAT_BLUR_LEVEL = 12;

    public static PostProcessShaderManager getInstance()
    {   return INSTANCE;
    }

    public static Identifier getHeatBlurEffect(int level)
    {   return ColdSweat.createKey("heat_blur_" + level);
    }

    /**
     * Activates the given effect, unless it is already active
     */
    public void setEffect(Identifier effect)
    {
        GameRenderer renderer = Minecraft.getInstance().gameRenderer;
        if (!effect.equals(renderer.currentPostEffect()))
        {   renderer.setPostEffect(effect);
        }
    }

    /**
     * Clears the active post-effect, if it belongs to Cold Sweat
     */
    public void clearEffect()
    {
        GameRenderer renderer = Minecraft.getInstance().gameRenderer;
        if (isColdSweatEffect(renderer.currentPostEffect()))
        {   renderer.clearPostEffect();
        }
    }

    public boolean hasEffect()
    {   return isColdSweatEffect(Minecraft.getInstance().gameRenderer.currentPostEffect());
    }

    private static boolean isColdSweatEffect(@Nullable Identifier effect)
    {   return effect != null && effect.getNamespace().equals(ColdSweat.MOD_ID);
    }
}
