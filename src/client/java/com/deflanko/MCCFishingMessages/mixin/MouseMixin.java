package com.deflanko.MCCFishingMessages.mixin;

import com.deflanko.MCCFishingMessages.MCCFishingMessagesMod;
import net.minecraft.client.MouseHandler;
import net.minecraft.client.input.MouseButtonInfo;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

@Mixin(MouseHandler.class)
public class MouseMixin {
    @Inject(method = "onScroll", at = @At("RETURN"))
    private void onMouseScroll(long handle, double xoffset, double yoffset, CallbackInfo ci) {
        if (MCCFishingMessagesMod.isOnMCCIsland() && MCCFishingMessagesMod.fishingChatBox != null && MCCFishingMessagesMod.fishingChatBox.isFocused()) {
            MCCFishingMessagesMod.fishingChatBox.scroll((int) yoffset);
        }
    }

    @Inject(method = "onButton", at = @At("HEAD"))
    private void onMouseButton(long handle, MouseButtonInfo rawButtonInfo, int action, CallbackInfo ci) {
        if (MCCFishingMessagesMod.isOnMCCIsland() && MCCFishingMessagesMod.fishingChatBox != null && action == 1) { // 1 = press
            double x = MCCFishingMessagesMod.CLIENT.mouseHandler.xpos();
            double y = MCCFishingMessagesMod.CLIENT.mouseHandler.ypos();
            MCCFishingMessagesMod.fishingChatBox.mouseClicked(x, y, rawButtonInfo.button());
        }
    }
}
