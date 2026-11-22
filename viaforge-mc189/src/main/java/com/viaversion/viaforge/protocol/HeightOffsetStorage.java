/*
 * This file is part of ViaForge - https://github.com/ViaVersion/ViaForge
 * Copyright (C) 2021-2026 Florian Reuth <git@florianreuth.de> and contributors
 *
 * This program is free software: you can redistribute it and/or modify
 * it under the terms of the GNU General Public License as published by
 * the Free Software Foundation, either version 3 of the License, or
 * (at your option) any later version.
 */
package com.viaversion.viaforge.protocol;

import com.viaversion.viaversion.api.connection.StorableObject;

/** Per-connection vertical offset used by the legacy extended-height window. */
public final class HeightOffsetStorage implements StorableObject {

    private int offset;

    public int offset() {
        return offset;
    }

    public void setOffset(int offset) {
        this.offset = offset;
    }
}
