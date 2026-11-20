/*
 * This file is part of ViaForge - https://github.com/ViaVersion/ViaForge
 * Copyright (C) 2021-2026 Florian Reuth <git@florianreuth.de> and contributors
 *
 * This program is free software: you can redistribute it and/or modify
 * it under the terms of the GNU General Public License as published by
 * the Free Software Foundation, either version 3 of the License, or
 * (at your option) any later version.
 *
 * This program is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU General Public License for more details.
 *
 * You should have received a copy of the GNU General Public License
 * along with this program.  If not, see <http://www.gnu.org/licenses/>.
 */

package com.viaversion.viaforge.mixin.impl;

import com.viaversion.viaversion.api.connection.UserConnection;
import com.viaversion.viaversion.api.protocol.version.ProtocolVersion;
import com.viaversion.viaversion.connection.ConnectionDetails;
import com.viaversion.viaforge.common.ViaForgeCommon;
import io.netty.channel.Channel;
import net.minecraft.client.Minecraft;
import net.minecraft.client.network.NetHandlerPlayClient;
import net.minecraft.network.NetworkManager;
import net.minecraft.network.Packet;
import net.minecraft.network.play.client.C03PacketPlayer;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Unique;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.Redirect;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

@Mixin(NetHandlerPlayClient.class)
public class MixinNetHandlerPlayClient {

    @Unique
    private int viaForge$positionReminderTicks;

    @Redirect(
        method = "addToSendQueue",
        at = @At(
            value = "INVOKE",
            target = "Lnet/minecraft/network/NetworkManager;sendPacket(Lnet/minecraft/network/Packet;)V"
        )
    )
    private void viaForge$modernPositionReminder(NetworkManager networkManager, Packet<?> packet) {
        if (packet instanceof C03PacketPlayer) {
            C03PacketPlayer movement = (C03PacketPlayer) packet;
            if (movement.isMoving()) {
                this.viaForge$positionReminderTicks = 0;
            } else {
                ProtocolVersion targetVersion = ViaForgeCommon.getManager().getTargetVersion();
                if (targetVersion != null && targetVersion.newerThan(ProtocolVersion.v1_8)
                    && ++this.viaForge$positionReminderTicks >= 20) {
                    Minecraft minecraft = Minecraft.getMinecraft();
                    if (minecraft.thePlayer != null) {
                        packet = new C03PacketPlayer.C04PacketPlayerPosition(
                            minecraft.thePlayer.posX,
                            minecraft.thePlayer.getEntityBoundingBox().minY,
                            minecraft.thePlayer.posZ,
                            movement.isOnGround()
                        );
                        this.viaForge$positionReminderTicks = 0;
                    }
                }
            }
        }

        networkManager.sendPacket(packet);
    }

    @Inject(method = "handleJoinGame", at = @At("RETURN"))
    public void sendConnectionDetails(CallbackInfo ci) {
        final Channel channel = Minecraft.getMinecraft().thePlayer.sendQueue.getNetworkManager().channel();
        final UserConnection connection = channel.attr(ViaForgeCommon.VF_VIA_USER).get();
        if (connection == null) {
            return;
        }

        ConnectionDetails.sendConnectionDetails(connection, ConnectionDetails.MOD_CHANNEL);
    }

}
