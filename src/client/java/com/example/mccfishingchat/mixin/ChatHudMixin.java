package com.example.mccfishingchat.mixin;

import com.example.mccfishingchat.MCCFishingMessagesMod;
import net.minecraft.client.gui.hud.ChatHud;
import net.minecraft.client.gui.hud.MessageIndicator;
import net.minecraft.network.message.MessageSignatureData;
import net.minecraft.text.Text;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

@Mixin(ChatHud.class)
public class ChatHudMixin {
    @Inject(method = "addMessage(Lnet/minecraft/text/Text;Lnet/minecraft/network/message/MessageSignatureData;Lnet/minecraft/client/gui/hud/MessageIndicator;)V",
            at = @At("HEAD"), cancellable = true)

    private void onAddMessage(Text message, MessageSignatureData signatureData, MessageIndicator indicator, CallbackInfo ci) {
        // Only run on MCC Island
        if (MCCFishingMessagesMod.isOnMCCIsland()) {
            if(MCCFishingMessagesMod.isBlockedPhrase(message)){
                ci.cancel();
            }
            else{
                if (MCCFishingMessagesMod.isPulledPhrase(message)) {
                    // Add to our custom fishing chat box
                    MCCFishingMessagesMod.fishingChatBox.addMessage(message);

                    // If the window is visible then steal messages, else cancel.
                    if (MCCFishingMessagesMod.fishingChatBox.isVisible()) {

                        // Cancel the original message to prevent it from showing in the main chat
                        ci.cancel();
                    }//ci.cancel();
                }
            }
        }
    }
}