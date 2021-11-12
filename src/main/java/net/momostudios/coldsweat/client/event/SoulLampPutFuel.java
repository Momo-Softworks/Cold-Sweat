package net.momostudios.coldsweat.client.event;

import com.mojang.blaze3d.matrix.MatrixStack;
import com.mojang.blaze3d.systems.RenderSystem;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.screen.inventory.ContainerScreen;
import net.minecraft.client.gui.screen.inventory.CreativeScreen;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.inventory.container.Container;
import net.minecraft.inventory.container.CraftingResultSlot;
import net.minecraft.inventory.container.Slot;
import net.minecraft.item.ItemGroup;
import net.minecraft.item.ItemStack;
import net.minecraft.item.Items;
import net.minecraft.util.ResourceLocation;
import net.minecraft.util.math.vector.Matrix4f;
import net.minecraft.util.math.vector.Vector3f;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.client.event.GuiScreenEvent;
import net.minecraftforge.event.TickEvent;
import net.minecraftforge.eventbus.api.Event;
import net.minecraftforge.eventbus.api.EventPriority;
import net.minecraftforge.eventbus.api.SubscribeEvent;
import net.minecraftforge.fml.client.gui.GuiUtils;
import net.minecraftforge.fml.common.Mod;
import net.minecraftforge.registries.ForgeRegistries;
import net.momostudios.coldsweat.config.ItemSettingsConfig;
import net.momostudios.coldsweat.core.network.ColdSweatPacketHandler;
import net.momostudios.coldsweat.core.network.message.SoulLampInputMessage;
import net.momostudios.coldsweat.core.util.ItemEntry;
import net.momostudios.coldsweat.core.util.registrylists.ModItems;

import java.util.List;

@Mod.EventBusSubscriber(Dist.CLIENT)
public class SoulLampPutFuel
{
    static int time = 0;

    @SubscribeEvent(priority = EventPriority.HIGHEST)
    public static void onSwapItem(final GuiScreenEvent.MouseReleasedEvent event)
    {
        if (event.getGui() instanceof ContainerScreen && event.getButton() == 0 || event.getButton() == 1 && event.getPhase() == EventPriority.HIGHEST)
        {
            ContainerScreen<?> inventoryScreen = (ContainerScreen<?>) event.getGui();
            Slot slot = inventoryScreen.getSlotUnderMouse();
            PlayerEntity player = Minecraft.getInstance().player;

            if (slot != null && !(slot instanceof CraftingResultSlot) && player != null)
            {
                ItemStack holdingStack = player.inventory.getItemStack();
                ItemStack slotStack = slot.getStack();

                if (slot.canTakeStack(player) && slot.isEnabled() && inventoryScreen.getSlotUnderMouse().getStack().getItem() == ModItems.SOULFIRE_LAMP &&
                !holdingStack.isEmpty() && getItemEntry(holdingStack).value > 0)
                {
                    if (slotStack.getOrCreateTag().getFloat("fuel") < 63)
                    {
                        int slotIndex = inventoryScreen instanceof CreativeScreen ? slot.getSlotIndex() : slot.slotNumber;

                        event.setCanceled(true);
                        event.setResult(Event.Result.DENY);
                        ColdSweatPacketHandler.INSTANCE.sendToServer(new SoulLampInputMessage(slotIndex, holdingStack, event.getButton() == 1));
                    }
                }
            }
        }
    }

    @SubscribeEvent
    public static void renderLanternTooltip(GuiScreenEvent.DrawScreenEvent.Post event)
    {
        if (event.getGui() instanceof ContainerScreen)
        {
            ContainerScreen<?> inventoryScreen = (ContainerScreen<?>) event.getGui();
            if (inventoryScreen.getSlotUnderMouse() != null && inventoryScreen.getSlotUnderMouse().getStack().getItem() == ModItems.SOULFIRE_LAMP)
            {
                int fuelValue = getItemEntry(Minecraft.getInstance().player.inventory.getItemStack()).value * Minecraft.getInstance().player.inventory.getItemStack().getCount();
                if (!Minecraft.getInstance().player.inventory.getItemStack().isEmpty())
                {
                    if (fuelValue > 0)
                    {
                        float fuel = inventoryScreen.getSlotUnderMouse().getStack().getOrCreateTag().getFloat("fuel");
                        int slotX = inventoryScreen.getSlotUnderMouse().xPos + ((ContainerScreen<?>) event.getGui()).getGuiLeft();
                        int slotY = inventoryScreen.getSlotUnderMouse().yPos + ((ContainerScreen<?>) event.getGui()).getGuiTop();
                        int bgColor = GuiUtils.DEFAULT_BACKGROUND_COLOR;
                        int borderColor = GuiUtils.DEFAULT_BORDER_COLOR_START;
                        int borderColor2 = GuiUtils.DEFAULT_BORDER_COLOR_END;

                        MatrixStack ms = event.getMatrixStack();
                        Matrix4f matrix = ms.getLast().getMatrix();
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

                        Minecraft.getInstance().textureManager.bindTexture(new ResourceLocation("cold_sweat:textures/gui/screen/soulfire_lamp_fuel_empty.png"));
                        event.getGui().blit(ms, slotX - 7, slotY - 12, 400, 0, 0, 30, 8, 8, 30);
                        RenderSystem.enableBlend();
                        RenderSystem.defaultBlendFunc();
                        RenderSystem.color4f(1f, 1f, 1f, 0.15f + (float) ((Math.sin(time / 5f) + 1f) / 2f) * 0.4f);
                        Minecraft.getInstance().textureManager.bindTexture(new ResourceLocation("cold_sweat:textures/gui/screen/soulfire_lamp_fuel_ghost.png"));
                        event.getGui().blit(ms, slotX - 7, slotY - 12, 400, 0, 0, Math.min(30, (int) ((fuel + fuelValue) / 2.1333f)), 8, 8, 30);
                        RenderSystem.color4f(1.0F, 1.0F, 1.0F, 1f);
                        Minecraft.getInstance().textureManager.bindTexture(new ResourceLocation("cold_sweat:textures/gui/screen/soulfire_lamp_fuel.png"));
                        event.getGui().blit(ms, slotX - 7, slotY - 12, 400, 0, 0, (int) (fuel / 2.1333f), 8, 8, 30);
                    }
                }
                else
                {
                    int mouseX = event.getMouseX();
                    int mouseY = event.getMouseY();
                    MatrixStack ms = event.getMatrixStack();
                    float fuel = inventoryScreen.getSlotUnderMouse().getStack().getOrCreateTag().getFloat("fuel");

                    if (event.getGui() instanceof CreativeScreen && ((CreativeScreen) event.getGui()).getSelectedTabIndex() == ItemGroup.SEARCH.getIndex())
                    {
                        event.getMatrixStack().translate(0, 10, 0);
                    }

                    Minecraft.getInstance().textureManager.bindTexture(new ResourceLocation("cold_sweat:textures/gui/screen/soulfire_lamp_fuel_empty.png"));
                    event.getGui().blit(ms, mouseX + 12, mouseY, 400, 0, 0, 30, 8, 8, 30);
                    Minecraft.getInstance().textureManager.bindTexture(new ResourceLocation("cold_sweat:textures/gui/screen/soulfire_lamp_fuel.png"));
                    event.getGui().blit(ms, mouseX + 12, mouseY, 400, 0, 0, (int) (fuel / 2.1333f), 8, 8, 30);
                }
            }
        }
    }

    @SubscribeEvent
    public static void onClientTick(TickEvent.ClientTickEvent event)
    {
        if (event.phase == TickEvent.Phase.END)
        {
            time++;
        }
    }

    public static ItemEntry getItemEntry(ItemStack stack)
    {
        for (List<String> entry : ItemSettingsConfig.getInstance().soulLampItems())
        {
            if (entry.get(0).equals(ForgeRegistries.ITEMS.getKey(stack.getItem()).toString()))
            {
                return new ItemEntry(entry.get(0), Integer.parseInt(entry.get(1)));
            }
        }
        return new ItemEntry(ForgeRegistries.ITEMS.getKey(Items.AIR).toString(), 0);
    }
}
