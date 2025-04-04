package com.example.mccfishingchat;

import net.fabricmc.fabric.api.client.event.lifecycle.v1.ClientTickEvents;
import net.fabricmc.fabric.api.client.keybinding.v1.KeyBindingHelper;
import net.minecraft.client.option.KeyBinding;
import net.minecraft.client.util.InputUtil;
import org.lwjgl.glfw.GLFW;

public class InputHandler {
    private static KeyBinding toggleVisibilityKey;
    private static KeyBinding increaseFontSize;
    private static KeyBinding decreaseFontSize;


    public static void init() {
        // Register keybinding to toggle chat box visibility
        toggleVisibilityKey = KeyBindingHelper.registerKeyBinding(new KeyBinding(
                "MCC Fish Chatbox Toggle",
                InputUtil.Type.KEYSYM,
                GLFW.GLFW_KEY_F9,
                "MCC Fishing Messages"
        ));


        increaseFontSize = KeyBindingHelper.registerKeyBinding(new KeyBinding(
                "Increase Font Size",
                InputUtil.Type.KEYSYM,
                GLFW.GLFW_KEY_RIGHT_BRACKET,
                "MCC Fishing Chat"
        ));
        decreaseFontSize = KeyBindingHelper.registerKeyBinding(new KeyBinding(
                "Decrease Font Size",
                InputUtil.Type.KEYSYM,
                GLFW.GLFW_KEY_LEFT_BRACKET,
                "MCC Fishing Messages"
        ));

        // Register mouse handlers through Fabric's event system
        ClientTickEvents.END_CLIENT_TICK.register(client -> {
            while (toggleVisibilityKey.wasPressed()) {
                // Toggle chat box visibility
                MCCFishingMessagesMod.fishingChatBox.toggleVisibility();
            }

            while (increaseFontSize.wasPressed()){
                MCCFishingMessagesMod.fishingChatBox.changeFontSize(0.05f);
            }
            while (decreaseFontSize.wasPressed()){
                MCCFishingMessagesMod.fishingChatBox.changeFontSize(-0.05f);
            }

        });
    }
}