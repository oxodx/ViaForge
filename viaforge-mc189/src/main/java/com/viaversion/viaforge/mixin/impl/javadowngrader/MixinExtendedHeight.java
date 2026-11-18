/*
 * This file is part of ViaForge - https://github.com/ViaVersion/ViaForge
 * Copyright (C) 2021-2026 Florian Reuth <git@florianreuth.de> and contributors
 *
 * This program is free software: you can redistribute it and/or modify
 * it under the terms of the GNU General Public License as published by
 * the Free Software Foundation, either version 3 of the License, or
 * (at your option) any later version.
 */
package com.viaversion.viaforge.mixin.impl.javadowngrader;

import com.viaversion.nbt.tag.CompoundTag;
import com.viaversion.nbt.tag.ListTag;
import com.viaversion.viabackwards.protocol.v1_17to1_16_4.Protocol1_17To1_16_4;
import com.viaversion.viaforge.common.ViaForgeCommon;
import com.viaversion.viaversion.api.Via;
import com.viaversion.viaversion.api.connection.StorableObject;
import com.viaversion.viaversion.api.data.entity.EntityTracker;
import com.viaversion.viaversion.api.protocol.remapper.PacketHandler;
import com.viaversion.viaversion.api.type.Types;
import com.viaversion.viaversion.protocols.v1_16_1to1_16_2.packet.ServerboundPackets1_16_2;
import com.viaversion.viaversion.protocols.v1_16_4to1_17.packet.ClientboundPackets1_17;
import com.viaversion.viaversion.util.TagUtil;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

/**
 * Presents the lowest 256 blocks of an extended-height world as Y=0..255.
 *
 * ViaBackwards normally drops sections below Y=0. Setting the tracked minimum
 * to zero makes its existing chunk and light rewriters select the first sixteen
 * sections instead. Player coordinates are offset at the same protocol boundary
 * so the client and server continue to agree about the player's location.
 */
@Mixin(value = Protocol1_17To1_16_4.class, remap = false)
public abstract class MixinExtendedHeight {

    private static final int LEGACY_HEIGHT = 256;
    private static final int RELATIVE_Y_FLAG = 0x02;

    @Inject(method = "registerPackets", at = @At("TAIL"))
    private void viaForge$registerExtendedHeightWorkaround(CallbackInfo ci) {
        Protocol1_17To1_16_4 protocol = (Protocol1_17To1_16_4) (Object) this;
        protocol.appendClientbound(ClientboundPackets1_17.LOGIN, this::viaForge$configureWorldWindow);
        protocol.appendClientbound(ClientboundPackets1_17.RESPAWN, this::viaForge$configureWorldWindow);
        protocol.appendClientbound(ClientboundPackets1_17.PLAYER_POSITION, wrapper -> {
            HeightOffsetStorage storage = wrapper.user().get(HeightOffsetStorage.class);
            if (storage == null || storage.offset == 0) {
                return;
            }

            byte flags = wrapper.get(Types.BYTE, 0);
            if ((flags & RELATIVE_Y_FLAG) == 0) {
                wrapper.set(Types.DOUBLE, 1, wrapper.get(Types.DOUBLE, 1) + storage.offset);
            }
        });

        PacketHandler movePlayer = wrapper -> {
            HeightOffsetStorage storage = wrapper.user().get(HeightOffsetStorage.class);
            wrapper.passthrough(Types.DOUBLE); // X
            double y = wrapper.read(Types.DOUBLE);
            wrapper.write(Types.DOUBLE, storage != null ? y - storage.offset : y);
            wrapper.passthrough(Types.DOUBLE); // Z
        };
        protocol.appendServerbound(ServerboundPackets1_16_2.MOVE_PLAYER_POS, movePlayer);
        protocol.appendServerbound(ServerboundPackets1_16_2.MOVE_PLAYER_POS_ROT, movePlayer);
    }

    private void viaForge$configureWorldWindow(com.viaversion.viaversion.api.protocol.packet.PacketWrapper wrapper) {
        // Protocols are registered before ViaForge's own config is constructed,
        // so the option must be checked when a connection actually logs in.
        if (ViaForgeCommon.getManager().getConfig() == null
            || !ViaForgeCommon.getManager().getConfig().isExtendedHeightWorkaround()) {
            return;
        }

        EntityTracker tracker = wrapper.user().getEntityTracker(Protocol1_17To1_16_4.class);
        int minY = tracker.currentMinY();
        int worldHeight = tracker.currentWorldSectionHeight() << 4;
        int offset = minY < 0 && worldHeight > LEGACY_HEIGHT ? -minY : 0;

        HeightOffsetStorage storage = wrapper.user().get(HeightOffsetStorage.class);
        if (storage == null) {
            storage = new HeightOffsetStorage();
            wrapper.user().put(storage);
        }
        storage.offset = offset;

        if (offset == 0) {
            return;
        }

        // ViaBackwards uses currentMinY to choose which sixteen chunk sections
        // survive conversion. Zero selects the bottom sixteen sections.
        tracker.setCurrentMinY(0);

        // LOGIN contains the registry and current dimension; RESPAWN only the
        // current dimension. Missing indices simply mean there is no second tag.
        for (int i = 0; i < 2; i++) {
            try {
                viaForge$rewriteDimensionTag(wrapper.get(Types.NAMED_COMPOUND_TAG, i));
            } catch (Exception ignored) {
                break;
            }
        }

        Via.getPlatform().getLogger().warning(
            "Using experimental extended-height window: server Y " + minY + ".." + (minY + 255)
                + " is exposed to the client as Y 0..255 (offset " + offset + ")"
        );
    }

    private static void viaForge$rewriteDimensionTag(CompoundTag tag) {
        if (tag.getNumberTag("min_y") != null) {
            tag.putInt("min_y", 0);
            tag.putInt("height", LEGACY_HEIGHT);
            tag.putInt("logical_height", LEGACY_HEIGHT);
        }

        ListTag<CompoundTag> dimensions = TagUtil.getRegistryEntries(tag, "dimension_type");
        if (dimensions == null) {
            return;
        }
        for (CompoundTag dimension : dimensions) {
            CompoundTag element = dimension.getCompoundTag("element");
            if (element != null && element.getNumberTag("min_y") != null) {
                element.putInt("min_y", 0);
                element.putInt("height", LEGACY_HEIGHT);
                element.putInt("logical_height", LEGACY_HEIGHT);
            }
        }
    }

    public static final class HeightOffsetStorage implements StorableObject {
        private int offset;

        public int offset() {
            return offset;
        }
    }
}
