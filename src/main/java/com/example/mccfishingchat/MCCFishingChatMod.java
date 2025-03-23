package com.example.mccfishingchat;

import net.fabricmc.api.ClientModInitializer;
import net.fabricmc.api.EnvType;
import net.fabricmc.api.Environment;
import net.fabricmc.fabric.api.client.rendering.v1.HudLayerRegistrationCallback;
import net.minecraft.client.MinecraftClient;
import net.minecraft.client.gui.DrawContext;
import net.minecraft.text.Text;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

@Environment(EnvType.CLIENT)
public class MCCFishingChatMod implements ClientModInitializer {
    public static final Logger LOGGER = LoggerFactory.getLogger("mcc-fishing-chat");
    public static final MinecraftClient CLIENT = MinecraftClient.getInstance();
    public static FishingChatBox fishingChatBox;
    
    @Override
    public void onInitializeClient() {
        LOGGER.info("MCC Island Fishing Chat Filter initialized");
        
        // Create our custom chat box
        fishingChatBox = new FishingChatBox(CLIENT);
        
        // Register the HUD renderer
        HudLayerRegistrationCallback.EVENT.register((drawContext, tickDelta) -> {
            if (CLIENT.player != null && isOnMCCIsland()) {
                fishingChatBox.render(drawContext, 0, 0, tickDelta);
            }
        });
        
        // Register mouse handlers
        InputHandler.init();
    }
    
    public static boolean isOnMCCIsland() {
        return CLIENT.getCurrentServerEntry() != null && 
               CLIENT.getCurrentServerEntry().address.contains("mccisland.net");
    }

    public static boolean isFishingMessage(Text message) {
        String text = message.getString().toLowerCase();
        
        // Add all the patterns that match fishing messages on MCC Island
        return text.contains("you caught:")
            || text.contains("triggered:")
            || text.contains("you earned:")
            || text.contains("this spot is depleted, so you can no longer fish here")
            || text.contains("you've run out of your equipped")
            || text.contains("that's not a fishing spot! locate one and cast there.");
    }
}