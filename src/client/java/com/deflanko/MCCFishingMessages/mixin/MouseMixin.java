package com.deflanko.MCCFishingMessages.mixin;

import com.deflanko.MCCFishingMessages.MCCFishingMessagesMod;
import net.minecraft.client.Mouse;
import net.minecraft.client.input.MouseInput;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

@Mixin(Mouse.class)
public class MouseMixin {
    @Inject(method = "onMouseScroll", at = @At("RETURN"))
    private void onMouseScroll(long window, double horizontal, double vertical, CallbackInfo ci) {
        if (MCCFishingMessagesMod.isOnMCCIsland() && MCCFishingMessagesMod.fishingChatBox != null && MCCFishingMessagesMod.fishingChatBox.isFocused()) {
            MCCFishingMessagesMod.fishingChatBox.scroll((int) vertical);
        }
    }

    @Inject(method = "onMouseButton", at = @At("HEAD"))
    private void onMouseButton(long window, MouseInput input, int action, CallbackInfo ci) {
        if (MCCFishingMessagesMod.isOnMCCIsland() && MCCFishingMessagesMod.fishingChatBox != null && action == 1) { // 1 = press
            double x = MCCFishingMessagesMod.CLIENT.mouse.getX();
            double y = MCCFishingMessagesMod.CLIENT.mouse.getY();
            MCCFishingMessagesMod.fishingChatBox.mouseClicked(x, y, input.button());
        }
    }
}
