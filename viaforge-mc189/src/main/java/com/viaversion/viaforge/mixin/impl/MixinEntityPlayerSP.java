/*
 * This file is part of ViaForge - https://github.com/ViaVersion/ViaForge
 * Copyright (C) 2021-2026 Florian Reuth <git@florianreuth.de> and contributors
 *
 * This program is free software: you can redistribute it and/or modify
 * it under the terms of the GNU General Public License as published by
 * the Free Software Foundation, either version 3 of the License, or
 * (at your option) any later version.
 */
package com.viaversion.viaforge.mixin.impl;

import com.viaversion.viaforge.common.ViaForgeCommon;
import com.viaversion.viaversion.api.protocol.version.ProtocolVersion;
import net.minecraft.client.entity.EntityPlayerSP;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.Constant;
import org.spongepowered.asm.mixin.injection.ModifyConstant;

/** Aligns the legacy position reminder cadence with post-1.8 protocols. */
@Mixin(EntityPlayerSP.class)
public abstract class MixinEntityPlayerSP {

    @ModifyConstant(method = "onUpdateWalkingPlayer", constant = @Constant(intValue = 20))
    private int viaForge$modernPositionReminder(int vanillaInterval) {
        ProtocolVersion targetVersion = ViaForgeCommon.getManager().getTargetVersion();
        return targetVersion != null && targetVersion.newerThan(ProtocolVersion.v1_8)
            ? 19
            : vanillaInterval;
    }
}
