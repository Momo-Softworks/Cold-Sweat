package com.momosoftworks.coldsweat.client.gui.config;

import com.momosoftworks.coldsweat.config.ConfigSettings;
import com.momosoftworks.coldsweat.util.math.CSMath;
import net.minecraft.ChatFormatting;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.Font;
import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.client.gui.components.*;
import net.minecraft.client.gui.components.events.GuiEventListener;
import net.minecraft.client.gui.narration.NarratableEntry;
import net.minecraft.client.gui.screens.Screen;
import net.minecraft.network.chat.*;
import net.minecraft.resources.ResourceLocation;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.client.event.ScreenEvent;
import net.minecraftforge.event.TickEvent;
import net.minecraftforge.eventbus.api.SubscribeEvent;
import net.minecraftforge.fml.common.Mod;
import net.minecraftforge.fml.util.ObfuscationReflectionHelper;

import javax.annotation.Nullable;
import java.lang.reflect.Field;
import java.util.*;
import java.util.function.Consumer;
import java.util.function.Supplier;

@Mod.EventBusSubscriber(Dist.CLIENT)
public abstract class AbstractConfigPage extends Screen
{
    // Count how many ticks the mouse has been still for
    static int MOUSE_STILL_TIMER = 0;
    static int TOOLTIP_DELAY = 5;

    public final String ON = CommonComponents.OPTION_ON.getString();
    public final String OFF = CommonComponents.OPTION_OFF.getString();

    @SubscribeEvent
    public static void onClientTick(TickEvent.ClientTickEvent event)
    {   MOUSE_STILL_TIMER++;
    }

    @Override
    public void mouseMoved(double mouseX, double mouseY)
    {   MOUSE_STILL_TIMER = 0;
        super.mouseMoved(mouseX, mouseY);
    }

    @SubscribeEvent
    public static void onMouseClicked(ScreenEvent.MouseButtonPressed event)
    {
        if (Minecraft.getInstance().screen instanceof AbstractConfigPage screen)
        {   screen.children().forEach(child ->
            {
                if (child instanceof AbstractWidget widget && !widget.isMouseOver(event.getMouseX(), event.getMouseY()))
                {   widget.setFocused(false);
                }
            });
        }
    }

    private final Screen parentScreen;

    public Map<String, List<GuiEventListener>> widgetBatches = new HashMap<>();
    public Map<String, List<Component>> tooltips = new HashMap<>();

    protected int rightSideLength = 0;
    protected int leftSideLength = 0;

    private static final int TITLE_HEIGHT = ConfigScreen.TITLE_HEIGHT;
    private static final int BOTTOM_BUTTON_HEIGHT_OFFSET = ConfigScreen.BOTTOM_BUTTON_HEIGHT_OFFSET;
    private static final int BOTTOM_BUTTON_WIDTH = ConfigScreen.BOTTOM_BUTTON_WIDTH;
    public static Minecraft mc = Minecraft.getInstance();

    static ResourceLocation TEXTURE = new ResourceLocation("cold_sweat:textures/gui/screen/config_gui.png");

    ImageButton nextNavButton;
    ImageButton prevNavButton;

    public abstract Component sectionOneTitle();

    @Nullable
    public abstract Component sectionTwoTitle();

    public AbstractConfigPage(Screen parentScreen)
    {   super(Component.translatable("cold_sweat.config.title"));
        this.parentScreen = parentScreen;
    }

    /**
     * Adds an empty block to the list on the given side. One unit is the height of a button.
     */
    protected void addEmptySpace(Side side, double height)
    {
        if (side == Side.LEFT)
        {   this.leftSideLength += (int) (ConfigScreen.OPTION_SIZE * height);
        }
        else
        {   this.rightSideLength += (int) (ConfigScreen.OPTION_SIZE * height);
        }
    }

    /**
     * Adds a label with plain text to the list on the given side.
     * @param id The internal id of the label. This widget can be accessed by this id.
     */
    protected void addLabel(String id, Side side, String text, int color)
    {
        int labelX = side == Side.LEFT ? this.width / 2 - 185 : this.width / 2 + 51;
        int labelY = this.height / 4 + (side == Side.LEFT ? leftSideLength : rightSideLength);
        ConfigLabel label = new ConfigLabel(id, text, labelX, labelY, color);

        this.addWidgetBatch(id, List.of(label));

        if (side == Side.LEFT)
        {   this.leftSideLength += font.lineHeight + 4;
        }
        else
        {   this.rightSideLength += font.lineHeight + 4;
        }
    }

    protected void addLabel(String id, Side side, String text)
    {   this.addLabel(id, side, text, 16777215);
    }

    /**
     * Adds a button to the list on the given side.
     * @param id The internal id of the button. This widget can be accessed by this id.
     * @param dynamicLabel A supplier that returns the label of the button. The label is updated when the button is pressed.
     * @param onClick The action to perform when the button is pressed.
     * @param requireOP Whether the button should be disabled if the player is not OP.
     * @param setsCustomDifficulty Sets Cold Sweat's difficulty to custom when pressed, if true.
     * @param clientside Whether the button is clientside only (renders the clientside icon).
     * @param tooltip The tooltip of the button when hovered.
     */
    protected void addButton(String id, Side side, Supplier<Component> dynamicLabel, Consumer<Button> onClick,
                             boolean requireOP, boolean setsCustomDifficulty, boolean clientside,
                             Component... tooltip)
    {
        Component label = dynamicLabel.get();

        boolean shouldBeActive = !requireOP || mc.player == null || mc.player.hasPermissions(2);
        int buttonX = this.width / 2;
        int xOffset = side == Side.LEFT ? -179 : 56;
        int buttonY = this.height / 4 - 8 + (side == Side.LEFT ? leftSideLength : rightSideLength);
        // Extend the button if the text is too long
        int buttonWidth = 152 + Math.max(0, font.width(label) - 140);

        // Make the button
        Button button = new ConfigButton(buttonX + xOffset, buttonY, buttonWidth, 20, label, button1 ->
        {
            onClick.accept(button1);
            button1.setMessage(dynamicLabel.get());
        })
        {
            @Override
            public boolean setsCustomDifficulty()
            {
                return setsCustomDifficulty;
            }
        };
        button.active = shouldBeActive;

        // Add the clientside indicator
        if (clientside)
        {   this.addRenderableOnly(new ConfigImage(TEXTURE, this.width / 2 + xOffset - 18, buttonY + 3, 16, 15, 0, 144));
        }

        List<Component> tooltipList = new ArrayList<>(Arrays.asList(tooltip));
        // Add the client disclaimer if the setting is marked clientside
        if (clientside)
        {   tooltipList.add(Component.translatable("cold_sweat.config.clientside_warning").withStyle(ChatFormatting.DARK_GRAY));
        }
        // Assign the tooltip
        this.setTooltip(id, tooltipList);

        this.addWidgetBatch(id, List.of(button));

        // Mark this space as used
        if (side == Side.LEFT)
            this.leftSideLength += ConfigScreen.OPTION_SIZE;
        else
            this.rightSideLength += ConfigScreen.OPTION_SIZE;
    }

    /**
     * Adds an input that accepts decimal numbers to the list on the given side.
     * @param id The internal id of the input. This widget can be accessed by this id.
     * @param label The label text of the input.
     * @param onEdited The action to perform when the input is changed.
     * @param onInit The action to perform when the input is initialized (when the screen is created).
     * @param requireOP Whether the input should be disabled if the player is not OP.
     * @param setsCustomDifficulty Sets Cold Sweat's difficulty to custom when edited, if true.
     * @param clientside Whether the input is clientside only.
     * @param tooltip The tooltip of the input when hovered.
     */
    protected void addDecimalInput(String id, Side side, Component label, Consumer<Double> onEdited, Consumer<EditBox> onInit,
                                   boolean requireOP, boolean setsCustomDifficulty, boolean clientside,
                                   Component... tooltip)
    {
        boolean shouldBeActive = !requireOP || mc.player == null || mc.player.hasPermissions(2);
        int xOffset = side == Side.LEFT ? -82 : 151;
        int yOffset = (side == Side.LEFT ? this.leftSideLength : this.rightSideLength) - 2;
        int labelOffset = font.width(label.getString()) > 90 ?
                          font.width(label.getString()) - 84 : 0;

        // Make the input
        EditBox textBox = new EditBox(this.font, this.width / 2 + xOffset + labelOffset, this.height / 4 - 6 + yOffset, 51, 22, Component.literal(""))
        {
            @Override
            public void insertText(String text)
            {
                super.insertText(text);
                CSMath.tryCatch(() ->
                {
                    if (setsCustomDifficulty)
                        ConfigSettings.DIFFICULTY.set(4);
                    onEdited.accept(Double.parseDouble(this.getValue()));
                });
            }
            @Override
            public void deleteWords(int i)
            {
                super.deleteWords(i);
                CSMath.tryCatch(() ->
                {
                    if (setsCustomDifficulty)
                        ConfigSettings.DIFFICULTY.set(4);
                    onEdited.accept(Double.parseDouble(this.getValue()));
                });
            }
            @Override
            public void deleteChars(int i)
            {
                super.deleteChars(i);
                CSMath.tryCatch(() ->
                {
                    if (setsCustomDifficulty)
                        ConfigSettings.DIFFICULTY.set(4);
                    onEdited.accept(Double.parseDouble(this.getValue()));
                });
            }
        };

        // Disable the input if the player is not OP
        textBox.setEditable(shouldBeActive);

        // Set the initial value
        onInit.accept(textBox);

        // Round the input to 2 decimal places
        textBox.setValue(ConfigScreen.TWO_PLACES.format(Double.parseDouble(textBox.getValue())));

        // Make the label
        ConfigLabel configLabel = new ConfigLabel(id, label.getString(), this.width / 2 + xOffset - 95, this.height / 4 + yOffset, shouldBeActive ? 16777215 : 8421504);
        // Add the clientside indicator
        if (clientside)
        {   this.addRenderableOnly(new ConfigImage(TEXTURE, this.width / 2 + xOffset - 115, this.height / 4 - 4 + yOffset, 16, 15, 0, 144));
        }

        List<Component> tooltipList = new ArrayList<>(Arrays.asList(tooltip));
        // Add the client disclaimer if the setting is marked clientside
        if (clientside)
        {   tooltipList.add(Component.translatable("cold_sweat.config.clientside_warning").withStyle(ChatFormatting.DARK_GRAY));
        }
        // Assign the tooltip
        this.setTooltip(id, tooltipList);

        // Add the widget
        this.addWidgetBatch(id, List.of(textBox, configLabel));

        // Mark this space as used
        if (side == Side.LEFT)
            this.leftSideLength += ConfigScreen.OPTION_SIZE * 1.2;
        else
            this.rightSideLength += ConfigScreen.OPTION_SIZE * 1.2;
    }

    /**
     * Adds a 4-way direction button panel with a reset button to the list on the given side.
     * @param id The internal id of the panel. This widget can be accessed by this id.
     * @param label The label text of the panel.
     * @param leftRightPressed The action to perform when the left or right button is pressed. 1 for right, -1 for left.
     * @param upDownPressed The action to perform when the up or down button is pressed. -1 for up, 1 for down.
     * @param reset The action to perform when the reset button is pressed.
     * @param requireOP Whether the panel should be disabled if the player is not OP.
     * @param setsCustomDifficulty Sets Cold Sweat's difficulty to custom when edited, if true.
     * @param clientside Whether the panel is clientside only (renders the clientside icon).
     * @param tooltip The tooltip of the panel when hovered.
     */
    protected void addDirectionPanel(String id, Side side, Component label, Consumer<Integer> leftRightPressed, Consumer<Integer> upDownPressed, Runnable reset, Supplier<Boolean> hide,
                                     boolean requireOP, boolean setsCustomDifficulty, boolean clientside, boolean canHide, Component... tooltip)
    {
        int xOffset = side == Side.LEFT ? -97 : 136;
        int yOffset = side == Side.LEFT ? this.leftSideLength : this.rightSideLength;

        boolean shouldBeActive = !requireOP || mc.player == null || mc.player.hasPermissions(2);

        int labelWidth = font.width(label.getString());
        int labelOffset = labelWidth > 84
                        ? labelWidth - 84
                        : 0;

        List<GuiEventListener> widgetBatch = new ArrayList<>();

        // Left button
        ImageButton leftButton = new ImageButton(this.width / 2 + xOffset + labelOffset, this.height / 4 - 8 + yOffset, 14, 20, 0, 0, 20, TEXTURE, button ->
        {
            leftRightPressed.accept(-1);
            if (setsCustomDifficulty)
            {   ConfigSettings.DIFFICULTY.set(4);
            }
        });
        leftButton.active = shouldBeActive;
        widgetBatch.add(leftButton);

        // Up button
        ImageButton upButton = new ImageButton(this.width / 2 + xOffset + 14 + labelOffset, this.height / 4 - 8 + yOffset, 20, 10, 14, 0, 20, TEXTURE, button ->
        {
            upDownPressed.accept(-1);
            if (setsCustomDifficulty)
            {   ConfigSettings.DIFFICULTY.set(4);
            }
        });
        upButton.active = shouldBeActive;
        widgetBatch.add(upButton);

        // Down button
        ImageButton downButton = new ImageButton(this.width / 2 + xOffset + 14 + labelOffset, this.height / 4 + 2 + yOffset, 20, 10, 14, 10, 20, TEXTURE, button ->
        {
            upDownPressed.accept(1);
            if (setsCustomDifficulty)
            {   ConfigSettings.DIFFICULTY.set(4);
            }
        });
        downButton.active = shouldBeActive;
        widgetBatch.add(downButton);

        // Right button
        ImageButton rightButton = new ImageButton(this.width / 2 + xOffset + 34 + labelOffset, this.height / 4 - 8 + yOffset, 14, 20, 34, 0, 20, TEXTURE, button ->
        {
            leftRightPressed.accept(1);
            if (setsCustomDifficulty)
            {   ConfigSettings.DIFFICULTY.set(4);
            }
        });
        rightButton.active = shouldBeActive;
        widgetBatch.add(rightButton);

        // Reset button
        ImageButton resetButton = new ImageButton(this.width / 2 + xOffset + 52 + labelOffset, this.height / 4 - 8 + yOffset, 20, canHide ? 10 : 20, canHide ? 68 : 48, 0, 20, TEXTURE, button ->
        {
            reset.run();
            if (setsCustomDifficulty)
            {   ConfigSettings.DIFFICULTY.set(4);
            }
        });
        resetButton.active = shouldBeActive;
        widgetBatch.add(resetButton);

        // hide button, displayed directly under the reset button if canHide is true
        if (canHide)
        {
            ImageButton hideButton = new ImageButton(this.width / 2 + xOffset + 52 + labelOffset, this.height / 4 + 2 + yOffset, 20, 10, 68, 10, 20, TEXTURE, button ->
            {
                if (setsCustomDifficulty)
                {   ConfigSettings.DIFFICULTY.set(4);
                }
                setImageX((ImageButton) button, hide.get());
            });
            hide.get();
            setImageX(hideButton, hide.get());
            hideButton.active = shouldBeActive;
            widgetBatch.add(hideButton);
        }

        // Add the option text
        ConfigLabel configLabel = new ConfigLabel(id, label.getString(), this.width / 2 + xOffset - 79, this.height / 4 + yOffset, shouldBeActive ? 16777215 : 8421504);
        // Add the clientside indicator
        if (clientside)
        {   this.addRenderableOnly(new ConfigImage(TEXTURE, this.width / 2 + xOffset - 98, this.height / 4 - 8 + yOffset + 5, 16, 15, 0, 144));
        }
        widgetBatch.add(configLabel);

        List<Component> tooltipList = new ArrayList<>(Arrays.asList(tooltip));
        // Add the client disclaimer if the setting is marked clientside
        if (clientside)
        {   tooltipList.add(Component.translatable("cold_sweat.config.clientside_warning").withStyle(ChatFormatting.DARK_GRAY));
        }
        // Assign the tooltip
        this.setTooltip(id, tooltipList);

        this.addWidgetBatch(id, widgetBatch);

        // Add height to the list
        if (side == Side.LEFT)
            this.leftSideLength += ConfigScreen.OPTION_SIZE * 1.2;
        else
            this.rightSideLength += ConfigScreen.OPTION_SIZE * 1.2;
    }

    @Override
    protected void init()
    {
        this.leftSideLength = 0;
        this.rightSideLength = 0;

        this.addRenderableWidget(new Button.Builder(
                Component.translatable("gui.done"),
                button -> this.close())
            .pos(this.width / 2 - BOTTOM_BUTTON_WIDTH / 2, this.height - BOTTOM_BUTTON_HEIGHT_OFFSET)
            .size(BOTTOM_BUTTON_WIDTH, 20)
            .createNarration(button -> MutableComponent.create(button.get().getContents()))
            .build()
        );

        // Navigation
        nextNavButton = new ImageButton(this.width - 32, 12, 20, 20, 0, 88, 20, TEXTURE,
                button ->
                {   ConfigScreen.CURRENT_PAGE++;
                    mc.setScreen(ConfigScreen.getPage(ConfigScreen.CURRENT_PAGE, parentScreen));
                });
        if (ConfigScreen.CURRENT_PAGE < ConfigScreen.LAST_PAGE)
            this.addRenderableWidget(nextNavButton);

        prevNavButton = new ImageButton(this.width - 76, 12, 20, 20, 20, 88, 20, TEXTURE,
                button ->
                {   ConfigScreen.CURRENT_PAGE--;
                    mc.setScreen(ConfigScreen.getPage(ConfigScreen.CURRENT_PAGE, parentScreen));
                });
        if (ConfigScreen.CURRENT_PAGE > ConfigScreen.FIRST_PAGE)
            this.addRenderableWidget(prevNavButton);
    }

    @Override
    public void render(GuiGraphics graphics, int mouseX, int mouseY, float partialTicks)
    {
        this.renderBackground(graphics);
        Font font = this.font;

        // Page Title
        graphics.drawCenteredString(this.font, this.title.getString(), this.width / 2, TITLE_HEIGHT, 0xFFFFFF);

        // Page Number
        graphics.drawString(this.font, Component.literal((ConfigScreen.CURRENT_PAGE + 1) + "/" + (ConfigScreen.LAST_PAGE + 1)), this.width - 53, 18, 16777215, true);

        // Section 1 Title
        graphics.drawString(this.font, this.sectionOneTitle(), this.width / 2 - 204, this.height / 4 - 28, 16777215, true);

        // Section 1 Divider
        graphics.blit(TEXTURE, this.width / 2 - 202, this.height / 4 - 16, 255, 0, 1, 154);

        if (this.sectionTwoTitle() != null)
        {   // Section 2 Title
            graphics.drawString(this.font, this.sectionTwoTitle(), this.width / 2 + 32, this.height / 4 - 28, 16777215, true);

            // Section 2 Divider
            graphics.blit(TEXTURE, this.width / 2 + 34, this.height / 4 - 16, 255, 0, 1, 154);
        }

        super.render(graphics, mouseX, mouseY, partialTicks);

        // Render tooltip
        if (MOUSE_STILL_TIMER >= TOOLTIP_DELAY)
        for (Map.Entry<String, List<GuiEventListener>> entry : widgetBatches.entrySet())
        {
            String id = entry.getKey();
            List<GuiEventListener> widgets = entry.getValue();
            int minX = 0, minY = 0, maxX = 0, maxY = 0;
            for (GuiEventListener listener : widgets)
            {
                if (listener instanceof AbstractWidget widget)
                {
                    if (minX == 0 || widget.getX() < minX)
                        minX = widget.getX();
                    if (minY == 0 || widget.getY() < minY)
                        minY = widget.getY();
                    if (maxX == 0 || widget.getX() + widget.getWidth() > maxX)
                        maxX = widget.getX() + widget.getWidth();
                    if (maxY == 0 || widget.getY() + widget.getHeight() > maxY)
                        maxY = widget.getY() + widget.getHeight();
                }
            }

            // if the mouse is hovering over any of the widgets in the batch, show the corresponding tooltip
            if (CSMath.isWithin(mouseX, minX, maxX) && CSMath.isWithin(mouseY, minY, maxY))
            {
                List<Component> tooltipList = this.tooltips.get(id);
                if (tooltipList != null && !tooltipList.isEmpty())
                {
                    graphics.renderTooltip(font, tooltipList, Optional.empty(), mouseX, mouseY);
                }
                break;
            }
        }
    }

    @Override
    public void tick()
    {   super.tick();
    }

    @Override
    public boolean isPauseScreen()
    {   return true;
    }

    public void close()
    {   this.onClose();
        Minecraft.getInstance().setScreen(this.parentScreen);
    }

    public enum Side
    {
        LEFT,
        RIGHT
    }

    protected void addWidgetBatch(String id, List<GuiEventListener> elements)
    {
        for (GuiEventListener element : elements)
        {
            if (element instanceof Renderable widget)
                this.addRenderableWidget((GuiEventListener & Renderable & NarratableEntry) widget);
        }
        this.widgetBatches.put(id, elements);
    }

    public List<GuiEventListener> getWidgetBatch(String id)
    {
        return this.widgetBatches.get(id);
    }

    protected void setTooltip(String id, List<Component> tooltip)
    {
        List<Component> wrappedTooltip = new ArrayList<>();
        for (Component component : tooltip)
        {  // wrap lines at 300 px
           List<FormattedText> wrappedText = font.getSplitter().splitLines(component, 300, component.getStyle());
           // convert FormattedText back to styled Components
           wrappedTooltip.addAll(wrappedText.stream().map(text -> Component.literal(text.getString()).withStyle(component.getStyle())).toList());
        }
        this.tooltips.put(id, wrappedTooltip);
    }

    public static void setImageX(ImageButton button, boolean enabled)
    {
        Field imageX = ObfuscationReflectionHelper.findField(ImageButton.class, "f_94224_");
        imageX.setAccessible(true);
        try
        {   imageX.set(button, enabled ? 68 : 88);
        }
        catch (Exception ignored) {}
    }
}
