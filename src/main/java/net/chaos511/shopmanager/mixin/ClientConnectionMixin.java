package net.chaos511.shopmanager.mixin;

import io.netty.util.concurrent.Future;
import io.netty.util.concurrent.GenericFutureListener;
import net.minecraft.client.MinecraftClient;
import net.minecraft.network.ClientConnection;
import net.minecraft.network.Packet;
import net.minecraft.server.network.packet.ChatMessageC2SPacket;
import net.minecraft.text.LiteralText;
import net.chaos511.shopmanager.Command;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

@Mixin(ClientConnection.class)
public class ClientConnectionMixin {

    @Inject(method = "send(Lnet/minecraft/network/Packet;Lio/netty/util/concurrent/GenericFutureListener;)V", at = @At("HEAD"), cancellable = true)
    public void send(Packet<?> packet, GenericFutureListener<? extends Future<? super Void>> futureListener, CallbackInfo callback) {
        if(packet instanceof ChatMessageC2SPacket) {
            ChatMessageC2SPacket chatPacket = (ChatMessageC2SPacket) packet;
            if (chatPacket.getChatMessage().split(" ").length>0&&Command.isPrefix(chatPacket.getChatMessage().split(" ")[0])){
                String returntxt = "Command not Found /shopmanager help for a list of commands";
                if(chatPacket.getChatMessage().split(" ").length>1&&Command.isCommand(chatPacket.getChatMessage().split(" ")[1])) {
                    returntxt = Command.execCommand(chatPacket.getChatMessage().split(" ")[0],chatPacket.getChatMessage().split(" ")[1],chatPacket.getChatMessage().split(" "));
                 }
                callback.cancel();
                MinecraftClient.getInstance().player.addChatMessage(new LiteralText(returntxt),false);
                //MinecraftClient.getInstance().player.sendChatMessage(returntxt);
            }
        }
    }
}
