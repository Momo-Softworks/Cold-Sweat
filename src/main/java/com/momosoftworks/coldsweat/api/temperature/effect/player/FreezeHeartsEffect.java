package com.momosoftworks.coldsweat.api.temperature.effect.player;

import com.mojang.blaze3d.platform.GlStateManager;
import com.mojang.blaze3d.systems.RenderSystem;
import com.momosoftworks.coldsweat.ColdSweat;
import com.momosoftworks.coldsweat.api.event.vanilla.RenderHeartEvent;
import com.momosoftworks.coldsweat.api.temperature.effect.TempEffect;
import com.momosoftworks.coldsweat.api.temperature.effect.TempEffectType;
import com.momosoftworks.coldsweat.config.ConfigSettings;
import com.momosoftworks.coldsweat.data.codec.util.IntegerBounds;
import com.momosoftworks.coldsweat.util.math.CSMath;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.AbstractGui;
import net.minecraft.entity.LivingEntity;
import net.minecraft.util.ResourceLocation;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.api.distmarker.OnlyIn;
import net.minecraftforge.event.TickEvent;
import net.minecraftforge.eventbus.api.SubscribeEvent;

import static net.minecraft.client.gui.AbstractGui.GUI_ICONS_LOCATION;

public class FreezeHeartsEffect extends TempEffect
{
    public FreezeHeartsEffect(TempEffectType<?> type, LivingEntity entity, IntegerBounds bounds)
    {   super(type, entity, bounds);
    }

    private static final ResourceLocation HEART_TEXTURE = new ResourceLocation(ColdSweat.MOD_ID, "textures/gui/overlay/hearts_frozen.png");
    // Ticks up as hearts are rendered, representing the "index" of the current heart
    private int heartIndex = 0;

    @OnlyIn(Dist.CLIENT)
    @SubscribeEvent
    public void onRenderHeart(RenderHeartEvent event)
    {
        if (!this.test(Minecraft.getInstance().player)) return;
        if (!ConfigSettings.SHOW_FROZEN_HEALTH.get()) return;

        RenderHeartEvent.HeartType heartType = event.getHeartType();
        boolean halfHeart = event.isHalfHeart();
        int x = event.getX();
        int y = event.getY();
        // This check ensures that this only gets called once per heart
        if (heartType == RenderHeartEvent.HeartType.CONTAINER)
        {   heartIndex += 1;
        }

        double effect = this.getEffectFactor();
        double heartsFreezePercentage = ConfigSettings.HEARTS_FREEZING_PERCENTAGE.get();
        if (heartsFreezePercentage == 0) return;

        float maxHealth = this.entity().getMaxHealth();
        boolean isHardcore = this.entity().level.getLevelData().isHardcore();

        float maxFrozenHealth = (float) (maxHealth * heartsFreezePercentage);
        if (maxFrozenHealth == 0) return;

        int frozenHealth = (int) Math.round(CSMath.blend(0, maxHealth * heartsFreezePercentage, effect, 0, 1));
        int frozenHearts = Math.round(frozenHealth / 2f);
        boolean partialFrozen = frozenHealth % 2 == 1 && heartIndex == frozenHearts;
        int u = isHardcore ? 7 : 0;
        int v = partialFrozen ? halfHeart ? 21 : 14 : halfHeart ? 7 : 0;

        // Render frozen hearts
        if (heartIndex <= frozenHearts)
        {
            Minecraft.getInstance().textureManager.bind(HEART_TEXTURE);
            if (heartType == RenderHeartEvent.HeartType.CONTAINER)
            {   AbstractGui.blit(event.getPoseStack(), x + 1, y + 1, 14, v, 7, 7, 21, 28);
            }
            else
            {  AbstractGui.blit(event.getPoseStack(), x + 1, y + 1, u, v, 7, 7, 21, 28);
            }
            Minecraft.getInstance().textureManager.bind(GUI_ICONS_LOCATION);
        }
    }

    @SubscribeEvent
    public void resetHeartIndex(TickEvent.RenderTickEvent event)
    {
        if (event.phase == TickEvent.Phase.START)
        {   heartIndex = 0;
        }
    }

    @Override
    public Side getSide()
    {   return Side.CLIENT;
    }

}
