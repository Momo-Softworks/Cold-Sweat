package dev.momostudios.coldsweat.client.gui.config;

import com.mojang.blaze3d.systems.RenderSystem;
import com.mojang.blaze3d.vertex.PoseStack;
import dev.momostudios.coldsweat.util.math.CSMath;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.components.*;
import net.minecraft.client.gui.components.events.GuiEventListener;
import net.minecraft.client.gui.narration.NarratableEntry;
import net.minecraft.client.gui.screens.Screen;
import net.minecraft.network.chat.*;
import net.minecraft.resources.ResourceLocation;
import dev.momostudios.coldsweat.config.ConfigSettings;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.event.TickEvent;
import net.minecraftforge.eventbus.api.SubscribeEvent;
import net.minecraftforge.fml.common.Mod;

import javax.annotation.Nonnull;
import javax.annotation.Nullable;
import java.util.*;
import java.util.function.Consumer;
import java.util.function.Supplier;
import java.util.stream.Collectors;

@Mod.EventBusSubscriber(Dist.CLIENT)
public abstract class AbstractConfigPage extends Screen
{
    // Count how many ticks the mouse has been still for
    static int MOUSE_STILL_TIMER = 0;
    static int TOOLTIP_DELAY = 5;
    @SubscribeEvent
    public static void onClientTick(TickEvent.ClientTickEvent event)
    {
        MOUSE_STILL_TIMER++;
    }

    @Override
    public void mouseMoved(double mouseX, double mouseY)
    {
        MOUSE_STILL_TIMER = 0;
        super.mouseMoved(mouseX, mouseY);
    }

    private final Screen parentScreen;

    public Map<String, List<GuiEventListener>> widgetBatches = new HashMap<>();
    public Map<String, List<FormattedText>> tooltips = new HashMap<>();

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
    {
        super(Component.translatable("cold_sweat.config.title"));
        this.parentScreen = parentScreen;
    }

    public int index()
    {
        return 0;
    }

    /**
     * Adds an empty block to the list on the given side. One unit is the height of a button.
     */
    protected void addEmptySpace(Side side, double units)
    {
        if (side == Side.LEFT)
            this.leftSideLength += ConfigScreen.OPTION_SIZE * units;
        else
            this.rightSideLength += ConfigScreen.OPTION_SIZE * units;
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
            this.leftSideLength += font.lineHeight + 4;
        else
            this.rightSideLength += font.lineHeight + 4;
    }

    protected void addLabel(String id, Side side, String text)
    {
        this.addLabel(id, side, text, 16777215);
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
    protected void addButton(String id, Side side, Supplier<String> dynamicLabel, Consumer<Button> onClick,
                             boolean requireOP, boolean setsCustomDifficulty, boolean clientside, String... tooltip)
    {
        String label = dynamicLabel.get();

        boolean shouldBeActive = !requireOP || mc.player == null || mc.player.hasPermissions(2);
        int buttonX = this.width / 2;
        int xOffset = side == Side.LEFT ? -179 : 56;
        int buttonY = this.height / 4 - 8 + (side == Side.LEFT ? leftSideLength : rightSideLength);
        // Extend the button if the text is too long
        int buttonWidth = 152 + Math.max(0, font.width(label) - 140);

        // Make the button
        Button button = new ConfigButton(buttonX + xOffset, buttonY, buttonWidth, 20, Component.literal(label), button1 ->
        {
            onClick.accept(button1);
            button1.setMessage(Component.literal(dynamicLabel.get()));
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
        if (clientside) this.addRenderableOnly(new ConfigImage(TEXTURE, this.width / 2 + xOffset - 18, buttonY + 3, 16, 15, 0, 144));

        // Add the client disclaimer if the setting is marked clientside
        if (clientside)
        {   List<String> tooltipList = new ArrayList<>(Arrays.asList(tooltip));
            tooltipList.add("§8"+Component.translatable("cold_sweat.config.temp_offset.desc_2").getString()+"§r");
            tooltip = tooltipList.toArray(new String[0]);
        }
        // Assign the tooltip
        this.setTooltip(id, tooltip);

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
     * @param onValueWrite The action to perform when the input is changed.
     * @param onInit The action to perform when the input is initialized.
     * @param requireOP Whether the input should be disabled if the player is not OP.
     * @param setsCustomDifficulty Sets Cold Sweat's difficulty to custom when edited, if true.
     * @param clientside Whether the input is clientside only (renders the clientside icon).
     * @param tooltip The tooltip of the input when hovered.
     */
    protected void addDecimalInput(String id, Side side, Component label, Consumer<Double> onValueWrite, Consumer<EditBox> onInit,
                                   boolean requireOP, boolean setsCustomDifficulty, boolean clientside, String... tooltip)
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
                    onValueWrite.accept(Double.parseDouble(this.getValue()));
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
                    onValueWrite.accept(Double.parseDouble(this.getValue()));
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
                    onValueWrite.accept(Double.parseDouble(this.getValue()));
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
        if (clientside) this.addRenderableOnly(new ConfigImage(TEXTURE, this.width / 2 + xOffset - 115, this.height / 4 - 4 + yOffset, 16, 15, 0, 144));

        // Add the client disclaimer if the setting is marked clientside
        if (clientside)
        {   List<String> tooltipList = new ArrayList<>(Arrays.asList(tooltip));
            tooltipList.add("§8"+Component.translatable("cold_sweat.config.temp_offset.desc_2").getString()+"§r");
            tooltip = tooltipList.toArray(new String[0]);
        }
        // Assign the tooltip
        this.setTooltip(id, tooltip);

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
    protected void addDirectionPanel(String id, Side side, Component label, Consumer<Integer> leftRightPressed, Consumer<Integer> upDownPressed, Runnable reset,
                                     boolean requireOP, boolean setsCustomDifficulty, boolean clientside, String... tooltip)
    {
        int xOffset = side == Side.LEFT ? -97 : 136;
        int yOffset = side == Side.LEFT ? this.leftSideLength : this.rightSideLength;

        boolean shouldBeActive = !requireOP || mc.player == null || mc.player.hasPermissions(2);

        int labelWidth = font.width(label.getString());
        int labelOffset = labelWidth > 84
                        ? labelWidth - 84
                        : 0;

        // Left button
        ImageButton leftButton = new ImageButton(this.width / 2 + xOffset + labelOffset, this.height / 4 - 8 + yOffset, 14, 20, 0, 0, 20, TEXTURE, button ->
        {
            leftRightPressed.accept(-1);

            if (setsCustomDifficulty)
                ConfigSettings.DIFFICULTY.set(4);
        });
        leftButton.active = shouldBeActive;

        // Up button
        ImageButton upButton = new ImageButton(this.width / 2 + xOffset + 14 + labelOffset, this.height / 4 - 8 + yOffset, 20, 10, 14, 0, 20, TEXTURE, button ->
        {
            upDownPressed.accept(-1);

            if (setsCustomDifficulty)
                ConfigSettings.DIFFICULTY.set(4);
        });
        upButton.active = shouldBeActive;

        // Down button
        ImageButton downButton = new ImageButton(this.width / 2 + xOffset + 14 + labelOffset, this.height / 4 + 2 + yOffset, 20, 10, 14, 10, 20, TEXTURE, button ->
        {
            upDownPressed.accept(1);

            if (setsCustomDifficulty)
                ConfigSettings.DIFFICULTY.set(4);
        });
        downButton.active = shouldBeActive;

        // Right button
        ImageButton rightButton = new ImageButton(this.width / 2 + xOffset + 34 + labelOffset, this.height / 4 - 8 + yOffset, 14, 20, 34, 0, 20, TEXTURE, button ->
        {
            leftRightPressed.accept(1);

            if (setsCustomDifficulty)
                ConfigSettings.DIFFICULTY.set(4);
        });
        rightButton.active = shouldBeActive;

        // Reset button
        ImageButton resetButton = new ImageButton(this.width / 2 + xOffset + 52 + labelOffset, this.height / 4 - 8 + yOffset, 20, 20, 48, 0, 20, TEXTURE, button ->
        {
            reset.run();

            if (setsCustomDifficulty)
                ConfigSettings.DIFFICULTY.set(4);
        });
        resetButton.active = shouldBeActive;

        // Add the option text
        ConfigLabel configLabel = new ConfigLabel(id, label.getString(), this.width / 2 + xOffset - 79, this.height / 4 + yOffset, shouldBeActive ? 16777215 : 8421504);
        // Add the clientside indicator
        if (clientside)
            this.addRenderableOnly(new ConfigImage(TEXTURE, this.width / 2 + xOffset - 98, this.height / 4 - 8 + yOffset + 5, 16, 15, 0, 144));

        // Add the client disclaimer if the setting is marked clientside
        if (clientside)
        {   List<String> tooltipList = new ArrayList<>(Arrays.asList(tooltip));
            tooltipList.add("§8"+Component.translatable("cold_sweat.config.temp_offset.desc_2").getString()+"§r");
            tooltip = tooltipList.toArray(new String[0]);
        }
        // Assign the tooltip
        this.setTooltip(id, tooltip);

        this.addWidgetBatch(id, List.of(upButton, downButton, leftButton, rightButton, resetButton, configLabel));

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

        this.addRenderableWidget(new Button(
            this.width / 2 - BOTTOM_BUTTON_WIDTH / 2,
            this.height - BOTTOM_BUTTON_HEIGHT_OFFSET,
            BOTTOM_BUTTON_WIDTH, 20,
            Component.translatable("gui.done"),
            button -> this.close())
        );

        // Navigation
        nextNavButton = new ImageButton(this.width - 32, 12, 20, 20, 0, 88, 20, TEXTURE,
                button -> mc.setScreen(ConfigScreen.getPage(this.index() + 1, parentScreen)));
        if (this.index() < ConfigScreen.LAST_PAGE)
            this.addRenderableWidget(nextNavButton);

        prevNavButton = new ImageButton(this.width - 76, 12, 20, 20, 20, 88, 20, TEXTURE,
                button -> mc.setScreen(ConfigScreen.getPage(this.index() - 1, parentScreen)));
        if (this.index() > ConfigScreen.FIRST_PAGE)
            this.addRenderableWidget(prevNavButton);
    }

    @Override
    public void render(@Nonnull PoseStack poseStack, int mouseX, int mouseY, float partialTicks)
    {
        this.renderBackground(poseStack);

        // Page Title
        drawCenteredString(poseStack, this.font, this.title.getString(), this.width / 2, TITLE_HEIGHT, 0xFFFFFF);

        // Page Number
        drawString(poseStack, this.font, Component.literal(this.index() + 1 + "/" + (ConfigScreen.LAST_PAGE + 1)), this.width - 53, 18, 16777215);

        // Section 1 Title
        drawString(poseStack, this.font, this.sectionOneTitle(), this.width / 2 - 204, this.height / 4 - 28, 16777215);

        // Section 1 Divider
        RenderSystem.setShaderTexture(0, TEXTURE);
        this.blit(poseStack, this.width / 2 - 202, this.height / 4 - 16, 255, 0, 1, 154);

        if (this.sectionTwoTitle() != null)
        {
            // Section 2 Title
            drawString(poseStack, this.font, this.sectionTwoTitle(), this.width / 2 + 32, this.height / 4 - 28, 16777215);

            // Section 2 Divider
            RenderSystem.setShaderTexture(0, TEXTURE);
            this.blit(poseStack, this.width / 2 + 34, this.height / 4 - 16, 255, 0, 1, 154);
        }

        super.render(poseStack, mouseX, mouseY, partialTicks);

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
                    if (minX == 0 || widget.x < minX)
                        minX = widget.x;
                    if (minY == 0 || widget.y < minY)
                        minY = widget.y;
                    if (maxX == 0 || widget.x + widget.getWidth() > maxX)
                        maxX = widget.x + widget.getWidth();
                    if (maxY == 0 || widget.y + widget.getHeight() > maxY)
                        maxY = widget.y + widget.getHeight();
                }
            }

            // if the mouse is hovering over any of the widgets in the batch, show the corresponding tooltip
            if (CSMath.withinRange(mouseX, minX, maxX) && CSMath.withinRange(mouseY, minY, maxY))
            {
                List<FormattedText> tooltipList = this.tooltips.get(id);
                if (tooltipList != null && !tooltipList.isEmpty())
                {
                    List<Component> tooltip = this.tooltips.get(id).stream().map(text -> Component.literal(text.getString())).collect(Collectors.toList());
                    this.renderComponentTooltip(poseStack, tooltip, mouseX, mouseY);
                }
                break;
            }
        }
    }

    @Override
    public void tick()
    {
        super.tick();
    }

    @Override
    public boolean isPauseScreen()
    {
        return true;
    }

    public void close()
    {
        this.onClose();
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
            if (element instanceof Widget && element instanceof NarratableEntry widget)
                this.addRenderableWidget((GuiEventListener & Widget & NarratableEntry) widget);
        }
        this.widgetBatches.put(id, elements);
    }

    public List<GuiEventListener> getWidgetBatch(String id)
    {
        return this.widgetBatches.get(id);
    }

    protected void setTooltip(String id, String[] tooltip)
    {
        List<FormattedText> tooltipList = new ArrayList<>();
        for (String string : tooltip)
        {
            tooltipList.addAll(font.getSplitter().splitLines(string, 300, Style.EMPTY));
        }
        this.tooltips.put(id, tooltipList);
    }
}
