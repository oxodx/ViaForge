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
import com.viaversion.nbt.tag.NumberTag;
import com.viaversion.viabackwards.protocol.v1_17to1_16_4.rewriter.BlockItemPacketRewriter1_17;
import com.viaversion.viaforge.protocol.HeightOffsetStorage;
import com.viaversion.viaversion.api.minecraft.chunks.Chunk;
import com.viaversion.viaversion.api.protocol.packet.PacketWrapper;
import com.viaversion.viaversion.api.type.Type;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Redirect;

/** Keeps block-entity coordinates aligned with the shifted chunk sections. */
@Mixin(value = BlockItemPacketRewriter1_17.class, remap = false)
public abstract class MixinExtendedHeightChunk {

    @Redirect(
        method = "lambda$registerPackets$6",
        at = @At(
            value = "INVOKE",
            target = "Lcom/viaversion/viaversion/api/protocol/packet/PacketWrapper;read(Lcom/viaversion/viaversion/api/type/Type;)Ljava/lang/Object;"
        )
    )
    private Object viaForge$shiftBlockEntities(PacketWrapper wrapper, Type<?> type) throws Exception {
        Object value = wrapper.read(type);
        if (!(value instanceof Chunk)) {
            return value;
        }

        HeightOffsetStorage storage = wrapper.user().get(HeightOffsetStorage.class);
        if (storage == null || storage.offset() == 0) {
            return value;
        }

        Chunk chunk = (Chunk) value;
        for (CompoundTag blockEntity : chunk.getBlockEntities()) {
            NumberTag y = blockEntity.getNumberTag("y");
            if (y != null) {
                blockEntity.putInt("y", y.asInt() + storage.offset());
            }
        }
        return chunk;
    }
}
