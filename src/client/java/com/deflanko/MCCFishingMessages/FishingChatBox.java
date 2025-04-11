package com.deflanko.MCCFishingMessages;

import com.deflanko.MCCFishingMessages.config.Config;
import net.minecraft.client.MinecraftClient;
import net.minecraft.client.gui.DrawContext;
import net.minecraft.client.gui.hud.MessageIndicator;
import net.minecraft.client.render.RenderTickCounter;
import net.minecraft.text.OrderedText;
import net.minecraft.text.Text;
import net.minecraft.util.math.MathHelper;
//import net.minecraft.client.main.Main;
//import net.minecraft.text.Style;
//import net.minecraft.client.font.TextRenderer;
//import net.fabricmc.fabric.api.client.event.lifecycle.v1.ClientTickEvents;

import java.util.*;

public class FishingChatBox {
    private static final int MAX_MESSAGES = 100;
    //private static final int MESSAGE_FADE_TIME = 200;
    //private static final int MESSAGE_STAY_TIME = 10000; // 10 seconds
    private static final int BACKGROUND_COLOR = 0x80000000; // Semi-transparent black
    private static final int MESSAGE_HEIGHT = 9;

    private final MinecraftClient client;
    private final Deque<ChatMessage> messages = new LinkedList<>();
    private int scrollOffset = 0;
    private boolean focused = false;
    private boolean visible = true;


    private int boxX = 0;  // Default position
    private int boxY = 30; // Top of screen, below hot bar
    private int boxWidth = 330;
    private int boxHeight = 110;
    private float fontSize = 1.0f;
    private int guiScaleFactor = 1;
    private int maxVisibleMessages = 10;

    // Add at the top of the class
    private static final Text COPY_ICON = Text.literal("📋");
    private static final int COPY_ICON_COLOR = 0xFF00DCFF;

    //For islandText
    private String islandText = "";
    private final FishingLocation location = new FishingLocation();

    public FishingChatBox(MinecraftClient client, Config config) {
        this.client = client;
        this.boxX = config.boxX;
        this.boxY = config.boxY;
        this.boxHeight = config.boxHeight;
        this.boxWidth = config.boxWidth;
        this.fontSize = config.fontSize;
    }

    public void render(DrawContext context, double mouseX, double mouseY, RenderTickCounter tickCounter) {
        if (!visible || messages.isEmpty() || client.getDebugHud().shouldShowDebugHud()) return;
        
        // Draw background
        context.fill(boxX, boxY, boxX + boxWidth, boxY + boxHeight, BACKGROUND_COLOR);

        //unfocus the box with chat unfocused
        if(focused && !client.inGameHud.getChatHud().isChatFocused()){
            focused = false;
            scrollOffset = 0; //reset scroll offset to 0 to warp box back to the bottom
        }
        if(focused){
            context.drawBorder(boxX, boxY, boxWidth+2, boxHeight+2, 0xFFFFFFFF);
        }

        // Draw title
        String title = "Fishing Messages  ";
        assert client.player != null;
        //Draw Cords
        String cords = "X: " + (int)client.player.getX() + " Y: " + (int)client.player.getY() + " Z: " + (int)client.player.getZ();
        if(boxWidth < client.textRenderer.getWidth(cords) + 20 + client.textRenderer.getWidth(title)){
            title = "";
        } //sets title to none if box width is smaller than everything.
        context.drawText(client.textRenderer, title, boxX + 5, boxY + 5, 0xFFFFFFFF, true);
        context.drawText(client.textRenderer, cords, (boxX + boxWidth) - (client.textRenderer.getWidth(cords) + 20), boxY + 5, 0xFFA000, true);

        // Add clipboard icon
        int iconX = boxX + (boxWidth - 10);
        context.drawText(client.textRenderer, COPY_ICON, iconX, boxY + 5, COPY_ICON_COLOR, true);

        // Check if mouse is hovering over icon
        if (client.inGameHud.getChatHud().isChatFocused() && mouseX >= iconX*guiScaleFactor && mouseX <= iconX*guiScaleFactor+ client.textRenderer.getWidth(COPY_ICON) &&
                mouseY >= (boxY + 5)*guiScaleFactor && mouseY <= (boxY + 5 + 9)*guiScaleFactor) {
            context.fill(iconX, boxY + 5, iconX + client.textRenderer.getWidth(COPY_ICON), boxY + 14, 0xAAFFFFFF);
        }
        //apply font size
        context.getMatrices().push();
        context.getMatrices().scale(fontSize,fontSize,fontSize);

        //context.drawText(client.textRenderer, String.valueOf(maxVisibleMessages), boxWidth - 10, boxY + 5, 0xFFFFFFFF, true );
        int fontMarginWidth = (int)(boxWidth/fontSize) - 5;

        //Draw messages
        int yOffset = (int)((boxY + boxHeight)/fontSize) - MESSAGE_HEIGHT; // Start at bottom of box
        int xOffset = (int)(boxX/fontSize);
        int visibleCount = 0;
        maxVisibleMessages = (int)((boxHeight - 18)/fontSize)/MESSAGE_HEIGHT;
        List<ChatMessage> visibleMessages = new ArrayList<>(messages);
        int startIndex = Math.max(0, Math.min(scrollOffset, messages.size() - maxVisibleMessages));


        for (int i = startIndex; i < visibleMessages.size() && visibleCount < maxVisibleMessages; i++) {
            ChatMessage message = visibleMessages.get(i);
            List<OrderedText> wrappedText = new ArrayList<>(client.textRenderer.wrapLines(message.text, fontMarginWidth));
            reverseList(wrappedText);
            int localSize = 0;
            for (OrderedText line : wrappedText) {
                if (visibleCount >=maxVisibleMessages){
                    continue;
                }
                localSize++;
                context.drawText(client.textRenderer, line, xOffset + 5 , yOffset, 0xFFFFFFFF, true);
                if( message.size < localSize){
                    message.size = localSize;
                }
                yOffset -= MESSAGE_HEIGHT;
                visibleCount++;
            }
        }
        context.getMatrices().pop();


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
    }
    public static <T> void reverseList(List<T> list) {
        // base condition when the list size is 0
        if (list.size() <= 1 )
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
    public void addMessage(Text message, MessageIndicator messageIndicator ) {
        messages.addFirst(new ChatMessage(message, client.inGameHud.getTicks(), 0));
        while (messages.size() > MAX_MESSAGES) {
            messages.removeLast();
        }
    }

    
    public void scroll(int amount) {
        if (focused) {
            scrollOffset = MathHelper.clamp(scrollOffset + amount, 0, Math.max(0, messages.size() - maxVisibleMessages));
        }
    }

    public void mouseClicked(double mouseX, double mouseY, int button) {
        updateGuiScale();

        // Get scaled coordinates for accurate detection
        double scaledMouseX = mouseX / guiScaleFactor;
        double scaledMouseY = mouseY / guiScaleFactor;

        // Update location and get island number
        assert client.player != null;
        location.updateLocation(client.player.getX(), client.player.getY(), client.player.getZ());
        int island = location.getIslandNumber();
        islandText = island > 0 ? "i" + island : "";

        // Format coordinates with island
        String cords = "";
        if (!islandText.isEmpty()) {
            cords = islandText + " ";
        }
        cords += " " + (int)client.player.getX() + " " + (int)client.player.getY() + " " + (int)client.player.getZ() + " ";


        // Check clipboard icon click
        int iconX = boxX + boxWidth - 10; //place icon position from right border instead of left
        if (scaledMouseX >= iconX && scaledMouseX <= iconX + client.textRenderer.getWidth(COPY_ICON) &&
                scaledMouseY >= boxY + 5 && scaledMouseY <= boxY + 5 + 9 && button == 0) {
            client.keyboard.setClipboard(cords);
            // Optional: Add visual feedback
            MCCFishingMessagesMod.LOGGER.info("Copied coordinates to clipboard"); // Debug log
            return;
        }

        // Original focus check
        focused = visible && mouseX >= boxX && mouseX <= (boxX + boxWidth)*guiScaleFactor &&
                mouseY >= boxY*guiScaleFactor && mouseY <= (boxY + boxHeight)*guiScaleFactor &&
                button == 0 && client.inGameHud.getChatHud().isChatFocused();
        if(!focused){
            scrollOffset = 0;
        }
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

    public void changeFontSize(float changeAmt){
        float i = this.fontSize;
        this.fontSize = Math.max(0.2f, Math.min(1.5f, i+changeAmt));
    }

    public void updateGuiScale(){
        this.guiScaleFactor = client.options.getGuiScale().getValue();
    }

    private static class ChatMessage {
        public final Text text;
        public final int timestamp;
        public int size;

        public ChatMessage(Text text, int timestamp, int size) {
            this.text = text;
            this.timestamp = timestamp;
            this.size = size;
        }
    }
}