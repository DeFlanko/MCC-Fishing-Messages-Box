package com.deflanko.MCCFishingMessages;

import com.deflanko.MCCFishingMessages.config.ConfigManager;
import net.fabricmc.api.ClientModInitializer;
import net.fabricmc.api.EnvType;
import net.fabricmc.api.Environment;
import net.fabricmc.fabric.api.client.message.v1.ClientReceiveMessageEvents;
import net.fabricmc.fabric.api.client.rendering.v1.HudLayerRegistrationCallback;
import net.fabricmc.fabric.api.client.rendering.v1.IdentifiedLayer;
import net.minecraft.client.MinecraftClient;
import net.minecraft.client.gui.hud.MessageIndicator;
import net.minecraft.text.Text;
import net.minecraft.util.Identifier;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.ArrayList;
import java.util.List;

@Environment(EnvType.CLIENT)
public class MCCFishingMessagesMod implements ClientModInitializer {
    public static final Logger LOGGER = LoggerFactory.getLogger("mcc-fishing-messages");
    public static final MinecraftClient CLIENT = MinecraftClient.getInstance();
    public static FishingChatBox fishingChatBox;
    public static final String MODID = "mccfishingmessages";
    private static final Identifier FISHING_NOTIFICATION_HUD_LAYER = Identifier.of("mcc-fishing-messages", "fishing-noti-layer");
    private static List<String> pulledPhrases = new ArrayList<>();
    private static List<String> blockedPhrases = new ArrayList<>();

    @Override
    public void onInitializeClient() {
        LOGGER.info("MCC Island Fishing Chat Filter initialized");

        // Create our custom chat box


        // Register mouse handlers

        ConfigManager.init();
        ConfigManager.loadWithFailureBackup();

        setWordLists();

        fishingChatBox = new FishingChatBox(CLIENT, ConfigManager.instance());

        InputHandler.init();
        // Register the HUD renderer
        HudLayerRegistrationCallback.EVENT.register((layeredDrawerWrapper -> {
            layeredDrawerWrapper.attachLayerBefore(IdentifiedLayer.CHAT, FISHING_NOTIFICATION_HUD_LAYER, (drawContext, tickCounter) -> {
                if (CLIENT.player != null && isOnMCCIsland()) {
                    fishingChatBox.render(drawContext, CLIENT.mouse.getX(), CLIENT.mouse.getY(), tickCounter);
                }
            });
        }));

        ClientReceiveMessageEvents.ALLOW_GAME.register(
                (message, a) -> {
                    if (!isOnMCCIsland()) {
                        return true;
                    }
                    if (isBlockedPhrase(message)) {
                        return false;
                    }

                    if (MCCFishingMessagesMod.isPulledPhrase(message)) {
                        // Add to our custom fishing chat box
                        MCCFishingMessagesMod.fishingChatBox.addMessage(message, null, MessageIndicator.system());

                        // If the window is visible then steal messages, else cancel.
                        return !MCCFishingMessagesMod.fishingChatBox.isVisible();
                    }
                    return true;
                }
        );

    }

    public static boolean isOnMCCIsland() {
        return CLIENT.getCurrentServerEntry() != null &&
               CLIENT.getCurrentServerEntry().address.contains("mccisland.net");
    }


    public static boolean isPulledPhrase(Text message) {
        String text = message.getString().toLowerCase();
        boolean caught = false;
        for(String line : pulledPhrases){
            if(text.contains(line)){
                caught = true;
                break;
            }
        }
        return caught;

        //previous logged messages can be found in Config.java -ty

            //Not Implementing
            //||text.contains("info:")
            //||text.contains("important: the instance you are currently on is restarting. You will shortly be teleported to another instance.")

    }

    public static boolean isBlockedPhrase(Text message){
        if(blockedPhrases.isEmpty()){
            return false;
        }
        String text = message.getString().toLowerCase();
        boolean caught = false;
        for(String line : blockedPhrases){
            if(text.contains(line)){
                caught = true;
                break;
            }
        }
        return caught;
    }

    private void setWordLists() {
        pulledPhrases = ConfigManager.instance().pulledPhrases;
        blockedPhrases = ConfigManager.instance().blockedPhrases;
    }
}
