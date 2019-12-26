package net.chaos511.shopmanager.mixin;

import net.minecraft.block.Block;
import net.minecraft.block.entity.SignBlockEntity;
import net.minecraft.client.render.block.entity.SignBlockEntityRenderer;
import net.minecraft.util.Identifier;
import net.chaos511.shopmanager.ShopManager;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

@Mixin(SignBlockEntityRenderer.class)
public class SignBlockEntityRendererMixin {
    @Inject(method = "method_3582", at = @At("HEAD"), cancellable = true)
    private void method_3582(SignBlockEntity signBlockEntity, double d, double e, double f, float g, int i, CallbackInfo callback) {
       if (ShopManager.recording) {
           ShopManager.addCheckAndShop(signBlockEntity);
       }
    }
    @Inject(method = "getModelTexture", at = @At("HEAD"), cancellable = true)
    private void getModelTexture(Block block, CallbackInfoReturnable<Identifier> callbackInfoReturnable) {
        if (ShopManager.recording) {
            if(ShopManager.isValid){
                callbackInfoReturnable.setReturnValue(new Identifier("textures/entity/signs/oak.png"));
            }else {
                callbackInfoReturnable.setReturnValue(new Identifier("textures/entity/signs/dark_oak.png"));
            }
        }
    }
}