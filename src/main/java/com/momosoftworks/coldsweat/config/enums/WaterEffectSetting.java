package com.momosoftworks.coldsweat.config.enums;

import net.minecraft.util.StringRepresentable;

public enum WaterEffectSetting implements StringRepresentable
{
    OFF("options.off", false, false),
    PARTICLES("cold_sweat.config.show_water_effect.particles", true, false),
    SCREEN("cold_sweat.config.show_water_effect.screen", false, true),
    ALL("cold_sweat.config.show_water_effect.all", true, true);

    private final String translationKey;
    private final boolean showParticles;
    private final boolean showGui;

    WaterEffectSetting(String translationKey, boolean showParticles, boolean showGui)
    {
        this.translationKey = translationKey;
        this.showParticles = showParticles;
        this.showGui = showGui;
    }

    @Override
    public String getSerializedName()
    {
        return this.translationKey;
    }

    public boolean showParticles()
    {
        return this.showParticles;
    }

    public boolean showGui()
    {
        return this.showGui;
    }
}
