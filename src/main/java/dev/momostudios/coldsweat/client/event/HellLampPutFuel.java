package dev.momostudios.coldsweat.client.event;

import com.mojang.blaze3d.systems.RenderSystem;
import com.mojang.blaze3d.vertex.PoseStack;
import com.mojang.math.Matrix4f;
import com.mojang.math.Vector3f;
import dev.momostudios.coldsweat.core.network.ColdSweatPacketHandler;
import dev.momostudios.coldsweat.core.network.message.SoulLampInputMessage;
import dev.momostudios.coldsweat.util.ItemEntry;
import dev.momostudios.coldsweat.util.registrylists.ModItems;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.screens.inventory.AbstractContainerScreen;
import net.minecraft.client.gui.screens.inventory.ContainerScreen;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.inventory.Slot;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.Items;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.client.event.ScreenEvent;
import net.minecraftforge.client.gui.GuiUtils;
import net.minecraftforge.event.TickEvent;
import net.minecraftforge.eventbus.api.Event;
import net.minecraftforge.eventbus.api.EventPriority;
import net.minecraftforge.eventbus.api.SubscribeEvent;
import net.minecraftforge.fml.common.Mod;
import net.minecraftforge.registries.ForgeRegistries;
import dev.momostudios.coldsweat.config.ItemSettingsConfig;

@Mod.EventBusSubscriber(Dist.CLIENT)
public class HellLampPutFuel
{
    static int time = 0;

    @SubscribeEvent(priority = EventPriority.HIGHEST)
    public static void onSwapItem(final ScreenEvent.MouseReleasedEvent event)
    {
        if (event.getScreen() instanceof ContainerScreen inventoryScreen && (event.getButton() == 0 || event.getButton() == 1))
        {
            Slot slot = inventoryScreen.getSlotUnderMouse();
            Player player = Minecraft.getInstance().player;

            if (slot != null && player != null)
            {
                ItemStack holdingStack = ((ContainerScreen) event.getScreen()).getMenu().getCarried();
                ItemStack slotStack = slot.getItem();

                if (slot.allowModification(player) && slot.isActive() && inventoryScreen.getSlotUnderMouse().getItem().getItem() == ModItems.HELLSPRING_LAMP &&
                !holdingStack.isEmpty() && getItemEntry(holdingStack).value > 0)
                {
                    if (slotStack.getOrCreateTag().getFloat("fuel") < 63)
                    {
                        int slotIndex = slot.getSlotIndex();

                        event.setCanceled(true);
                        event.setResult(Event.Result.DENY);
                        ColdSweatPacketHandler.INSTANCE.sendToServer(new SoulLampInputMessage(slotIndex, holdingStack, event.getButton() == 1));
                    }
                }
            }
        }
    }

    @SubscribeEvent
    public static void renderLanternTooltip(ScreenEvent.DrawScreenEvent.Post event)
    {
        if (event.getScreen() instanceof AbstractContainerScreen<?> inventoryScreen)
        {
            if (inventoryScreen.getSlotUnderMouse() != null && inventoryScreen.getSlotUnderMouse().getItem().getItem() == ModItems.HELLSPRING_LAMP)
            {
                int fuelValue = getItemEntry(inventoryScreen.getMenu().getCarried()).value * inventoryScreen.getMenu().getCarried().getCount();
                if (!inventoryScreen.getMenu().getCarried().isEmpty())
                {
                    if (fuelValue > 0)
                    {
                        float fuel = inventoryScreen.getSlotUnderMouse().getItem().getOrCreateTag().getFloat("fuel");
                        int slotX = inventoryScreen.getSlotUnderMouse().x + ((AbstractContainerScreen<?>) event.getScreen()).getGuiLeft();
                        int slotY = inventoryScreen.getSlotUnderMouse().y + ((AbstractContainerScreen<?>) event.getScreen()).getGuiTop();
                        int bgColor = GuiUtils.DEFAULT_BACKGROUND_COLOR;
                        int borderColor = GuiUtils.DEFAULT_BORDER_COLOR_START;
                        int borderColor2 = GuiUtils.DEFAULT_BORDER_COLOR_END;

                        PoseStack ms = event.getPoseStack();
                        Matrix4f matrix = ms.last().pose();
                        if (event.getMouseY() < slotY + 8)
                            matrix.translate(new Vector3f(0, 32, 0));
                        GuiUtils.drawGradientRect(matrix, 400, slotX - 9, slotY - 14, slotX + 25, slotY - 1, bgColor, bgColor); // background

                        GuiUtils.drawGradientRect(matrix, 400, slotX - 9, slotY - 2, slotX + 25, slotY - 1, bgColor, bgColor); // bottom
                        GuiUtils.drawGradientRect(matrix, 400, slotX - 9, slotY - 15, slotX + 25, slotY - 14, bgColor, bgColor); // top
                        GuiUtils.drawGradientRect(matrix, 400, slotX - 10, slotY - 14, slotX - 9, slotY - 2, bgColor, bgColor); // left
                        GuiUtils.drawGradientRect(matrix, 400, slotX + 25, slotY - 14, slotX + 26, slotY - 2, bgColor, bgColor); // right

                        GuiUtils.drawGradientRect(matrix, 400, slotX - 9, slotY - 3, slotX + 25, slotY - 2, borderColor2, borderColor2); // bottom border
                        GuiUtils.drawGradientRect(matrix, 400, slotX - 9, slotY - 14, slotX + 25, slotY - 13, borderColor, borderColor); // top border
                        GuiUtils.drawGradientRect(matrix, 400, slotX - 9, slotY - 13, slotX - 8, slotY - 3, borderColor, borderColor2); // left border
                        GuiUtils.drawGradientRect(matrix, 400, slotX + 24, slotY - 13, slotX + 25, slotY - 3, borderColor, borderColor2); // right border

                        Minecraft.getInstance().textureManager.bindForSetup(new ResourceLocation("cold_sweat:textures/gui/screen/soulfire_lamp_fuel_empty.png"));
                        event.getScreen().blit(ms, slotX - 7, slotY - 12, 400, 0, 0, 30, 8, 8, 30);
                        RenderSystem.enableBlend();
                        RenderSystem.defaultBlendFunc();
                        RenderSystem.setShaderColor(1f, 1f, 1f, 0.15f + (float) ((Math.sin(time / 5f) + 1f) / 2f) * 0.4f);
                        Minecraft.getInstance().textureManager.bindForSetup(new ResourceLocation("cold_sweat:textures/gui/screen/soulfire_lamp_fuel_ghost.png"));
                        event.getScreen().blit(ms, slotX - 7, slotY - 12, 400, 0, 0, Math.min(30, (int) ((fuel + fuelValue) / 2.1333f)), 8, 8, 30);
                        RenderSystem.setShaderColor(1.0F, 1.0F, 1.0F, 1f);
                        Minecraft.getInstance().textureManager.bindForSetup(new ResourceLocation("cold_sweat:textures/gui/screen/soulfire_lamp_fuel.png"));
                        event.getScreen().blit(ms, slotX - 7, slotY - 12, 400, 0, 0, (int) (fuel / 2.1333f), 8, 8, 30);
                    }
                }
                else
                {
                    int mouseX = event.getMouseX();
                    int mouseY = event.getMouseY();
                    PoseStack ms = event.getPoseStack();
                    float fuel = inventoryScreen.getSlotUnderMouse().getItem().getOrCreateTag().getFloat("fuel");

                    Minecraft.getInstance().textureManager.bindForSetup(new ResourceLocation("cold_sweat:textures/gui/screen/soulfire_lamp_fuel_empty.png"));
                    event.getScreen().blit(ms, mouseX + 12, mouseY, 400, 0, 0, 30, 8, 8, 30);
                    Minecraft.getInstance().textureManager.bindForSetup(new ResourceLocation("cold_sweat:textures/gui/screen/soulfire_lamp_fuel.png"));
                    event.getScreen().blit(ms, mouseX + 12, mouseY, 400, 0, 0, (int) (fuel / 2.1333f), 8, 8, 30);
                }
            }
        }
    }

    @SubscribeEvent
    public static void onClientTick(TickEvent.ClientTickEvent event)
    {
        if (event.phase == TickEvent.Phase.END)
        {
            time = (time + 1) % (int) (5 * (Math.PI * 2));
        }
    }

    public static ItemEntry getItemEntry(ItemStack stack)
    {
        for (String entry : ItemSettingsConfig.getInstance().soulLampItems())
        {
            if (entry.equals(ForgeRegistries.ITEMS.getKey(stack.getItem()).toString()))
            {
                return new ItemEntry(entry, 1);
            }
        }
        return new ItemEntry("minecraft:air", 0);
    }
}
