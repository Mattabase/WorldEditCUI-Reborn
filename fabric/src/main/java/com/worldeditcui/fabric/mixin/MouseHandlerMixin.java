package com.worldeditcui.fabric.mixin;

import com.worldeditcui.WorldEditCUIReborn;
import net.minecraft.client.MouseHandler;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

@Mixin(MouseHandler.class)
public class MouseHandlerMixin {

    @Inject(method = "onScroll", at = @At("HEAD"), cancellable = true)
    private void worldeditcui$onScroll(long windowPointer, double xoffset, double yoffset, CallbackInfo ci) {
        if (WorldEditCUIReborn.getGizmoManager().onScroll(yoffset)) {
            ci.cancel();
        }
    }
}
