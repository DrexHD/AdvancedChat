package me.wesley1808.advancedchat.mixins;

import com.mojang.authlib.GameProfile;
import me.wesley1808.advancedchat.common.config.Config;
import me.wesley1808.advancedchat.common.data.AdvancedChatData;
import me.wesley1808.advancedchat.common.data.DataManager;
import me.wesley1808.advancedchat.common.interfaces.IServerPlayer;
import me.wesley1808.advancedchat.common.utils.Util;
import net.minecraft.core.BlockPos;
import net.minecraft.network.protocol.game.ClientboundSetActionBarTextPacket;
import net.minecraft.server.MinecraftServer;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.server.network.ServerGamePacketListenerImpl;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.level.Level;
import org.jetbrains.annotations.Nullable;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.Unique;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

@Mixin(ServerPlayer.class)
public abstract class ServerPlayerMixin extends Player implements IServerPlayer {
    @Shadow
    public ServerGamePacketListenerImpl connection;
    @Unique
    @Nullable
    private ClientboundSetActionBarTextPacket actionBarPacket;

    public ServerPlayerMixin(Level level, BlockPos blockPos, float f, GameProfile gameProfile) {
        super(level, blockPos, f, gameProfile);
    }

    @Inject(method = "<init>", at = @At(value = "TAIL"))
    private void advancedchat$onInit(MinecraftServer minecraftServer, ServerLevel serverLevel, GameProfile gameProfile, CallbackInfo ci) {
        this.resetActionBarPacket();
    }

    @Inject(method = "setLevel", at = @At(value = "TAIL"))
    private void advancedchat$onSetLevel(ServerLevel serverLevel, CallbackInfo ci) {
        this.resetActionBarPacket();
    }

    @Inject(method = "tick", at = @At(value = "TAIL"))
    private void advancedchat$onTick(CallbackInfo ci) {
        if (Config.instance().actionbar) {
            if (this.tickCount % 200 == 0) {
                this.resetActionBarPacket();
            }

            if (this.actionBarPacket != null) {
                this.connection.send(this.actionBarPacket);
            }
        }
    }

    @Override
    public void resetActionBarPacket() {
        ServerPlayer player = (ServerPlayer) (Object) this;
        AdvancedChatData data = DataManager.get(player);
        if (Util.isVanished(player) || data.channel == null) {
            this.actionBarPacket = null;
            return;
        }

        this.actionBarPacket = new ClientboundSetActionBarTextPacket(data.channel.getActionBarText(player));
    }
}
