package com.momosoftworks.coldsweat.common.potions;

import com.momosoftworks.coldsweat.ColdSweat;
import net.minecraft.client.Minecraft;
import net.minecraft.potion.Potion;
import net.minecraft.util.ResourceLocation;

public class GracePotion extends Potion
{
    public GracePotion(int p_i1573_1_, boolean p_i1573_2_, int p_i1573_3_)
    {   super(p_i1573_1_, p_i1573_2_, p_i1573_3_);
    }

    @Override
    public boolean hasStatusIcon()
    {   Minecraft.getMinecraft().renderEngine.bindTexture(new ResourceLocation(ColdSweat.MOD_ID, "textures/gui/icons.png"));
        return super.hasStatusIcon();
    }

    public Potion setIconIndex(int par1, int par2)
    {   super.setIconIndex(par1, par2);
        return this;
    }
}
