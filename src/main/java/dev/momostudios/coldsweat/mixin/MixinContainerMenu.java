package dev.momostudios.coldsweat.mixin;

import dev.momostudios.coldsweat.ColdSweat;
import dev.momostudios.coldsweat.config.ItemSettingsConfig;
import dev.momostudios.coldsweat.util.config.ConfigEntry;
import dev.momostudios.coldsweat.util.registries.ModItems;
import net.minecraft.client.Minecraft;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.inventory.AbstractContainerMenu;
import net.minecraft.world.inventory.ClickType;
import net.minecraft.world.inventory.Slot;
import net.minecraft.world.item.ItemStack;
import net.minecraftforge.registries.ForgeRegistries;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

@Mixin(AbstractContainerMenu.class)
public class MixinContainerMenu
{
    AbstractContainerMenu menu = (AbstractContainerMenu) (Object) this;

    @Inject(method = "clicked(IILnet/minecraft/world/inventory/ClickType;Lnet/minecraft/world/entity/player/Player;)V", at = @At("HEAD"),
    remap = ColdSweat.REMAP_MIXINS, cancellable = true)
    public void slotClicked(int slotId, int mouseButton, ClickType clickType, Player player, CallbackInfo ci)
    {
        try
        {
            Slot slot = menu.getSlot(slotId);
            ItemStack stack = slot.getItem();
            if (stack.getItem() == ModItems.HELLSPRING_LAMP && Minecraft.getInstance().player != null)
            {
                double fuel = stack.getOrCreateTag().getDouble("fuel");
                ItemStack holdingStack = menu.getCarried();
                if (fuel <= 63 && clickType == ClickType.PICKUP && getItemEntry(holdingStack).value > 0)
                {
                    stack.getOrCreateTag().putDouble("fuel", Math.min(64, fuel + (mouseButton == 1 ? 1 : holdingStack.getCount())));
                    holdingStack.shrink(mouseButton == 1 ? 1 : 64 - (int) fuel);
                    ci.cancel();
                }
            }
        } catch (Exception e) {}
    }

    private static ConfigEntry getItemEntry(ItemStack stack)
    {
        for (String entry : ItemSettingsConfig.getInstance().soulLampItems())
        {
            if (entry.equals(ForgeRegistries.ITEMS.getKey(stack.getItem()).toString()))
            {
                return new ConfigEntry(entry, 1);
            }
        }
        return new ConfigEntry("minecraft:air", 0);
    }
}
