package com.deflanko.MCCFishingMessages;

import com.mojang.blaze3d.platform.InputConstants;
import net.fabricmc.fabric.api.client.event.lifecycle.v1.ClientTickEvents;
import net.fabricmc.fabric.api.client.keymapping.v1.KeyMappingHelper;
import net.minecraft.client.KeyMapping;
import net.minecraft.resources.Identifier;
import org.lwjgl.glfw.GLFW;

public class InputHandler {
    private static KeyMapping toggleVisibilityKey;
    private static KeyMapping increaseFontSize;
    private static KeyMapping decreaseFontSize;
    private static KeyMapping enterEditMode;

    // Define a reusable category for all key bindings
    private static final KeyMapping.Category MCC_CATEGORY =
            new KeyMapping.Category(Identifier.fromNamespaceAndPath("mccfishingmessages", "category"));

    public static void init() {
        // Register keybinding to toggle chat box visibility
        toggleVisibilityKey = KeyMappingHelper.registerKeyMapping(new KeyMapping(
                "MCC Fish Chatbox Toggle",
                InputConstants.Type.KEYSYM,
                GLFW.GLFW_KEY_F9,
                MCC_CATEGORY
        ));

        increaseFontSize = KeyMappingHelper.registerKeyMapping(new KeyMapping(
                "Font Size - Increase",
                InputConstants.Type.KEYSYM,
                GLFW.GLFW_KEY_RIGHT_BRACKET,
                MCC_CATEGORY
        ));

        decreaseFontSize = KeyMappingHelper.registerKeyMapping(new KeyMapping(
                "Font Size - Decrease",
                InputConstants.Type.KEYSYM,
                GLFW.GLFW_KEY_LEFT_BRACKET,
                MCC_CATEGORY
        ));

        enterEditMode = KeyMappingHelper.registerKeyMapping(new KeyMapping(
                "Enable Edit Mode",
                InputConstants.Type.KEYSYM,
                GLFW.GLFW_KEY_RIGHT_ALT,
                MCC_CATEGORY
        ));

        // Register mouse handlers through Fabric's event system
        ClientTickEvents.END_CLIENT_TICK.register(client -> {
            while (toggleVisibilityKey.consumeClick()) {
                // Toggle chat box visibility
                MCCFishingMessagesMod.fishingChatBox.toggleVisibility();
            }

            while (increaseFontSize.consumeClick()){
                MCCFishingMessagesMod.fishingChatBox.changeFontSize(0.05f);
            }
            while (decreaseFontSize.consumeClick()){
                MCCFishingMessagesMod.fishingChatBox.changeFontSize(-0.05f);
            }
            while (enterEditMode.consumeClick()){
                MCCFishingMessagesMod.fishingChatBox.ToggleEditMode();
            }

            /*while (enterDebugMode.consumeClick()){
                MCCFishingMessagesMod.fishingChatBox.ToggleDebug();
            }*/

        });
    }
}