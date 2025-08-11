package com.deflanko.MCCFishingMessages;

import com.deflanko.MCCFishingMessages.config.Config;
import com.deflanko.MCCFishingMessages.config.ConfigManager;
import net.minecraft.client.MinecraftClient;
import net.minecraft.client.gui.DrawContext;
import net.minecraft.client.gui.hud.ChatHudLine;
import net.minecraft.client.gui.hud.MessageIndicator;
import net.minecraft.client.render.RenderTickCounter;
import net.minecraft.network.message.MessageSignatureData;
import net.minecraft.text.OrderedText;
import net.minecraft.text.Style;
import net.minecraft.text.Text;
import net.minecraft.util.math.MathHelper;
import org.jetbrains.annotations.Nullable;
//import net.minecraft.client.main.Main;
//import net.minecraft.text.Style;
//import net.minecraft.client.font.TextRenderer;
//import net.fabricmc.fabric.api.client.event.lifecycle.v1.ClientTickEvents;


import java.util.*;

public class FishingChatBox {
    private static final int MAX_MESSAGES = 100;
    //private static final int MESSAGE_FADE_TIME = 200;
    //private static final int MESSAGE_STAY_TIME = 10000; // 10 seconds
    // NOTE: color - the text color in the 0xAARRGGBB format; Alpha channel value ranges from 00 (fully transparent) to FF (fully opaque).
    private static final int BACKGROUND_COLOR = 0x80000000; // Semi-transparent black
    private static final int MESSAGE_HEIGHT = 9;

    private final MinecraftClient client;
    private final Deque<ChatMessage> messages = new LinkedList<>();
    private int scrollOffset = 0;
    private boolean focused = false;
    private boolean visible = true;
    private boolean editMode = false;
    private EditState state = EditState.NONE;
    private int xDisplacement;
    private int yDisplacement;
    private int backupX;
    private int backupY;
    private int backupHeight;
    private int backupWidth;
    private final int minBoxWidth = 140;
    private final int minBoxHeight = 45;
    private int boxX;  // Default position
    private int boxY; // Top of screen, below hot bar
    private int boxWidth;
    private int boxHeight;
    private float fontSize;
    private int guiScaleFactor = 1;
    private int maxVisibleMessages = 10;
    private int linesPerScroll;
    private boolean debug = false;
    private boolean macDisplay;

    // Add at the top of the class
    private static final Text COPY_ICON = Text.literal("📋");
    private static final int COPY_ICON_COLOR = 0xFF00DCFF;

    private final FishingLocation location = new FishingLocation();
    private static FishingSpot getPerks() {
        return FishingSpot.getCurrentFishingSpot();
    }

    public FishingChatBox(MinecraftClient client, Config config) {
        this.client = client;
        this.boxX = config.boxX;
        this.boxY = config.boxY;
        this.boxHeight = config.boxHeight;
        this.boxWidth = config.boxWidth;
        this.fontSize = config.fontSize;
        this.linesPerScroll = config.scrollAmount;
        if (this.boxWidth < minBoxWidth) {
            this.boxWidth = minBoxWidth;
        }
        if (this.boxHeight < minBoxHeight) {
            this.boxHeight = minBoxHeight;
        }
        this.macDisplay = config.sillyMacDisplay;
        backupX = boxX;
        backupY = boxY;
        backupHeight = boxHeight;
        backupWidth = boxWidth;
    }

    public void render(DrawContext context, double mouseX, double mouseY, RenderTickCounter tickCounter) {
        if (macDisplay) {
            mouseX *= 2;
            mouseY *= 2;
        }
        int scaledX = (int) (mouseX / guiScaleFactor);
        int scaledY = (int) (mouseY / guiScaleFactor);

        if (!visible || messages.isEmpty() || client.getDebugHud().shouldShowDebugHud()) return;

        // Draw background
        context.fill(boxX, boxY, boxX + boxWidth, boxY + boxHeight, BACKGROUND_COLOR);

        //unfocus the box with chat unfocused
        if (focused && !client.inGameHud.getChatHud().isChatFocused()) {
            focused = false;
            scrollOffset = 0; //reset scroll offset to 0 to warp box back to the bottom
            if (state != EditState.NONE) {
                state = EditState.NONE;
                boxX = backupX;
                boxY = backupY;
                boxHeight = backupHeight;
                boxWidth = backupWidth;
            }
        }
        if (focused) {
            context.drawBorder(boxX, boxY, boxWidth + 2, boxHeight + 2, 0xFFFFFFFF);
        }
        /*if (debug) {  //debug stuff, prolly leave disabled
            boolean hovered = (visible && client.inGameHud.getChatHud().isChatFocused() && MouseWithinBox(mouseX, mouseY));
            int i = hovered ? 0xFF00FF00 : 0xFFFF0000;
            context.fill(boxX + boxWidth - 10, boxY + boxHeight - 10, boxX + boxWidth, boxY + boxHeight, i);
            String width = "BoxX: " + boxX + "  Box Width: " + boxWidth;
            String height = "BoxY: " + boxY + "  Box Height: " + boxHeight;
            String mouse = "MouseX: " + scaledX + "  MouseY: " + scaledY;
            context.drawText(client.textRenderer, width, boxX + boxWidth + 5, boxY + 10, 0xFFFFFFFF, true);
            context.drawText(client.textRenderer, height, boxX + boxWidth + 5, boxY + 20, 0xFFFFFFFF, true);
            context.drawText(client.textRenderer, mouse, boxX + boxWidth + 5, boxY + 30, 0xFFFFFFFF, true);
            if (client.inGameHud.getChatHud().isChatFocused()) {
                //int translatedMouseX = (int) Math.floor((((mouseX / guiScaleFactor)-boxX)/fontSize));
                context.fill(scaledX, scaledY, scaledX + 10, scaledY + 10, 0xFFFFFFFF);
            }
        }*/

        // Draw title
        String title = "Fishing Messages";
        assert client.player != null;

        //Draw Cords
        var pos = client.player.getPos();
        //String cords = "X: " + (int) client.player.getX() + " Y: " + (int) client.player.getY() + " Z: " + (int) client.player.getZ();
        String cords = "X: " + (int) pos.x + " Y: " + (int) pos.y + " Z: " + (int) pos.z;
        //MCCFishingMessagesMod.LOGGER.info("Cords value: " + cords); //Works

        //sets title to none if box width is smaller than everything.
        if (boxWidth < client.textRenderer.getWidth(cords) + 20 + client.textRenderer.getWidth(title)) {
            title = "";
        }
        // Render Title
        context.drawText(client.textRenderer, title, boxX + 5, boxY + 5, 0xFFFFFFFF, true);

        // Render Cords
        int textX = boxX + boxWidth - client.textRenderer.getWidth(cords) - 20; // 20px padding from right edge
        context.drawText(client.textRenderer, cords, textX, boxY + 5, 0xFFFFAA00, true); // Gold Color

        // Add clipboard icon
        int iconX = boxX + (boxWidth - 10);
        context.drawText(client.textRenderer, COPY_ICON, iconX, boxY + 5, COPY_ICON_COLOR, true);

        //draw a line to underline Title area
        context.drawBorder(boxX, boxY + 16, boxWidth, 1, 0xFFFFFFFF);

        // Check if mouse is hovering over icon
        if (client.inGameHud.getChatHud().isChatFocused() && mouseX >= iconX * guiScaleFactor && mouseX <= iconX * guiScaleFactor + client.textRenderer.getWidth(COPY_ICON) &&
                mouseY >= (boxY + 5) * guiScaleFactor && mouseY <= (boxY + 5 + 9) * guiScaleFactor) {
            context.fill(iconX, boxY + 5, iconX + client.textRenderer.getWidth(COPY_ICON), boxY + 14, 0xAAFFFFFF);
        }

        //apply font size
        //context.getMatrices().push();
        //context.getMatrices().scale(fontSize, fontSize, fontSize);

        var matrices = context.getMatrices();
        matrices.scale(fontSize, fontSize);

        //context.drawText(client.textRenderer, String.valueOf(maxVisibleMessages), boxWidth - 10, boxY + 5, 0xFFFFFFFF, true );
        int fontMarginWidth = (int) (boxWidth / fontSize) - 5;

        //Draw messages
        int yOffset = (int) ((boxY + boxHeight) / fontSize) - MESSAGE_HEIGHT; // Start at bottom of box
        int xOffset = (int) (boxX / fontSize);
        int visibleCount = 0;
        maxVisibleMessages = (int) ((boxHeight - 18) / fontSize) / MESSAGE_HEIGHT;
        List<ChatMessage> visibleMessages = new ArrayList<>(messages);
        int startIndex = Math.max(0, Math.min(scrollOffset, messages.size() - maxVisibleMessages));
        List<OrderedText> onScreenMessages = new ArrayList<>();
        //actually display the messages
        for (int i = startIndex; i < visibleMessages.size() && visibleCount < maxVisibleMessages; i++) {
            ChatMessage message = visibleMessages.get(i);
            List<OrderedText> wrappedText = new ArrayList<>(client.textRenderer.wrapLines(message.chathudline.content(), fontMarginWidth));
            reverseList(wrappedText);
            int localSize = 0;
            for (OrderedText line : wrappedText) {
                if (visibleCount >= maxVisibleMessages) {
                    continue;
                }
                localSize++;
                context.drawText(client.textRenderer, line, xOffset + 5, yOffset, 0xFFFFFFFF, true);
                if (message.size < localSize) {
                    message.size = localSize;
                }
                yOffset -= MESSAGE_HEIGHT;
                visibleCount++;
                onScreenMessages.add(line);
            }
        }

        //context.getMatrices().pop();
        matrices.scale(1/fontSize, 1/fontSize); // Reset scale
        //matrices.translate(originalX, originalY); // Reset position
        //check for hover text

        if (visible && client.inGameHud.getChatHud().isChatFocused() && MouseWithinBox(mouseX, mouseY)) {
            int i = (int) Math.floor((mouseY / guiScaleFactor)) - boxY - 17 + (int) Math.floor((MESSAGE_HEIGHT * fontSize) / 2);
            int lineIndex = (int) (i / fontSize) / MESSAGE_HEIGHT;
            lineIndex -= (maxVisibleMessages - Math.min(onScreenMessages.size(), maxVisibleMessages));
            lineIndex -= 1;
            lineIndex = (onScreenMessages.size() - 1) - lineIndex;
            if (lineIndex >= 0 && lineIndex < onScreenMessages.size()) {
                int translatedMouseX = (int) Math.floor(((mouseX / guiScaleFactor)-boxX) / fontSize);
                if (translatedMouseX > 0) {
                    Style style = this.client.textRenderer.getTextHandler().getStyleAt(onScreenMessages.get(lineIndex), translatedMouseX);
                    if (style != null && style.getHoverEvent() != null) {
                        context.drawHoverEvent(this.client.textRenderer, style, scaledX, scaledY);
                    }
                }
            }
        }
        // Draw scroll bar if needed
        if (messages.size() > maxVisibleMessages) {
            int scrollBarHeight = boxHeight - 25;
            int thumbSize = Math.max(10, scrollBarHeight * maxVisibleMessages / messages.size());
            int thumbPosition = scrollOffset * (scrollBarHeight - thumbSize) / (messages.size() - maxVisibleMessages);

            // Scroll bar background
            context.fill(boxX + boxWidth - 5, boxY + 20, boxX + boxWidth - 2, boxY + boxHeight - 5, 0x40FFFFFF);
            // Scroll thumb
            context.fill(boxX + boxWidth - 5, boxY + boxHeight - 5 - thumbPosition, boxX + boxWidth - 2,
                    boxY + boxHeight - 5 - thumbPosition - thumbSize, 0xFFAAAAAA);
        }
        //display edit mode on
        if (editMode) {
            context.fill(boxX, boxY, boxX + 10, boxY + 10, 0xFF00FF00);
        }
        //edit mode stuff
        if (editMode && focused) {

            //move box
            context.fill(boxX + 5, boxY + 2, boxX + boxWidth - 20, boxY + 17, 0xFFFFFF20);
            if (state == EditState.BOX) {
                boxX = Math.max((int) (mouseX / guiScaleFactor) - xDisplacement, 0);
                boxY = Math.max((int) (mouseY / guiScaleFactor) - yDisplacement, 0);
            }
            //right box
            context.fill(boxX + boxWidth - 5, boxY + 10, boxX + boxWidth, boxY + boxHeight - 8, 0xFFFF20FF);
            if (state == EditState.WIDTH) {
                boxWidth = Math.max((int) (mouseX / guiScaleFactor), minBoxWidth + boxX) - boxX + 2;
            }
            //bottom box
            context.fill(boxX + 3, boxY + boxHeight - 5, boxX + boxWidth - 3, boxY + boxHeight, 0xFF20FFFF);
            if (state == EditState.HEIGHT) {
                boxHeight = Math.max((int) (mouseY / guiScaleFactor), minBoxHeight + boxY) - boxY + 2;
            }

        }

    }

    public static <T> void reverseList(List<T> list) {
        // base condition when the list size is 0
        if (list.size() <= 1)
            return;

        T value = list.removeFirst();

        // call the recursive function to reverse
        // the list after removing the first element
        reverseList(list);

        // now after the rest of the list has been
        // reversed by the upper recursive call,
        // add the first value at the end
        list.add(value);
    }

    public void addMessage(Text message, @Nullable MessageSignatureData signatureData, @Nullable MessageIndicator indicator) {
        messages.addFirst(new ChatMessage(new ChatHudLine(client.inGameHud.getTicks(), message, signatureData, indicator)));
        while (messages.size() > MAX_MESSAGES) {
            messages.removeLast();
        }
    }

    public boolean WithinBounds(double min, double value, double max) {
        return (value > min && value < max);
    }

    public boolean MouseWithinBox(double mouseX, double mouseY) {
        return (WithinBounds(boxX, mouseX / guiScaleFactor, boxX + boxWidth)
                && WithinBounds(boxY, mouseY / guiScaleFactor, boxY + boxHeight));
    }

    public void scroll(int amount) {
        amount *= linesPerScroll;
        if (focused) {
            scrollOffset = MathHelper.clamp(scrollOffset + amount, 0, Math.max(0, messages.size() - maxVisibleMessages));
        }
    }

    public void mouseClicked(double mouseX, double mouseY, int button) {
        updateGuiScale();
        if (macDisplay) {
            mouseX *= 2;
            mouseY *= 2;
        }
        // Update location and get island number
        assert client.player != null;
        location.updateLocation(client.player.getX(), client.player.getY(), client.player.getZ());
        int island = location.getIslandNumber();
        //For islandText
        String islandText = island > 0 ? "i" + island : "";

        // Format coordinates with island
        String cords = "";
        if (!islandText.isEmpty()) {
            cords = islandText + " ";
        }
        //todo: add perks to cords
        //cords += " " + (int) client.player.getX() + " " + (int) client.player.getY() + " " + (int) client.player.getZ() + " ";
        FishingSpot currentSpot = getPerks();
        String perksText = currentSpot != null ? currentSpot.toString() : "";
        cords += " " + (int) client.player.getX() + " " + (int) client.player.getY() + " " + (int) client.player.getZ();
        if (!perksText.isEmpty()) {
            cords += " " + perksText;
        }

        // Check clipboard icon click
        int iconX = boxX + boxWidth - 10; //place icon position from right border instead of left
        if (button == 0
                && WithinBounds(iconX, mouseX / guiScaleFactor, iconX + client.textRenderer.getWidth(COPY_ICON))
                && WithinBounds(boxY + 5, mouseY / guiScaleFactor, boxY + 14)) {
            client.keyboard.setClipboard(cords);
            // Optional: Add visual feedback
            MCCFishingMessagesMod.LOGGER.info("Copied coordinates to clipboard"); // Debug log
            return;
        }
        // Edit mode stuff
        if (editMode && focused && button == 0) {
            //check top box area
            if (WithinBounds(boxX + 5, (mouseX / guiScaleFactor), boxX + boxWidth - 20)
                    && WithinBounds(boxY + 2, (mouseY / guiScaleFactor), boxY + 17)) {
                if (state == EditState.BOX) {
                    EndEdit();
                } else {
                    //store the mouse offset
                    xDisplacement = (int) (mouseX / guiScaleFactor) - boxX;
                    yDisplacement = (int) (mouseY / guiScaleFactor) - boxY;
                    //enable edit mode
                    backupX = boxX;
                    backupY = boxY;
                    state = EditState.BOX;
                }
            } else if (WithinBounds(boxX + boxWidth - 5, (mouseX / guiScaleFactor), boxX + boxWidth)
                    && WithinBounds(boxY + 10, (mouseY / guiScaleFactor), boxY + boxHeight - 8)) {
                if (state == EditState.WIDTH) {
                    EndEdit();
                } else {
                    backupWidth = boxWidth;
                    state = EditState.WIDTH;
                }

            } else if (WithinBounds(boxX + 3, (mouseX / guiScaleFactor), boxX + boxWidth - 3)
                    && WithinBounds(boxY + boxHeight - 5, (mouseY / guiScaleFactor), boxY + boxHeight)) {
                if (state == EditState.HEIGHT) {
                    EndEdit();
                } else {
                    backupHeight = boxHeight;
                    state = EditState.HEIGHT;
                }
            }
        }
        // Original focus check
        focused = visible &&
                MouseWithinBox(mouseX, mouseY) &&
                button == 0 && client.inGameHud.getChatHud().isChatFocused();
        if (!focused) {
            scrollOffset = 0;
            state = EditState.NONE;
        }
    }

    public void Save() {
        ConfigManager.instance().SetNewValues(boxX, boxWidth, boxY, boxHeight, fontSize);
    }

    public void EndEdit() {
        state = EditState.NONE;
        Save();
    }

    public boolean isFocused() {
        return focused;
    }

    public void toggleVisibility() {
        visible = !visible;
    }

    public boolean isVisible() {
        return visible;
    }

    public void changeFontSize(float changeAmt) {
        float i = this.fontSize;
        this.fontSize = Math.max(0.2f, Math.min(1.5f, i + changeAmt));
        Save();
    }

    public void ToggleEditMode() {
        editMode = !editMode;
        if (state != EditState.NONE) {
            state = EditState.NONE;
            boxX = backupX;
            boxY = backupY;
            boxHeight = backupHeight;
            boxWidth = backupWidth;
        }
    }

    public void ToggleDebug() {
        debug = !debug;
    }

    public void updateGuiScale() {
        this.guiScaleFactor = client.options.getGuiScale().getValue();
    }

    private enum EditState {
        NONE,
        BOX,
        WIDTH,
        HEIGHT
    }

    private static class ChatMessage {
        public ChatHudLine chathudline;
        public int size;

        public ChatMessage(ChatHudLine text) {
            this.chathudline = text;
            this.size = 0;
        }
    }
}