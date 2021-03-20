/*
 * Modern UI.
 * Copyright (C) 2019-2021 BloCamLimb. All rights reserved.
 *
 * Modern UI is free software; you can redistribute it and/or
 * modify it under the terms of the GNU Lesser General Public
 * License as published by the Free Software Foundation; either
 * version 3 of the License, or (at your option) any later version.
 *
 * Modern UI is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE. See the
 * GNU Lesser General Public License for more details.
 *
 * You should have received a copy of the GNU Lesser General Public
 * License along with Modern UI. If not, see <https://www.gnu.org/licenses/>.
 */

package icyllis.modernui.graphics.shader.program;

import icyllis.modernui.ModernUI;
import icyllis.modernui.graphics.shader.ShaderProgram;
import org.lwjgl.opengl.GL20;

@Deprecated
public class FeatheredRectProgram extends ShaderProgram {

    public static FeatheredRectProgram INSTANCE = new FeatheredRectProgram("rect", "rect_fill_v");

    private FeatheredRectProgram(String vert, String frag) {
        super(ModernUI.ID, vert, frag);
    }

    public void setThickness(float thickness) {
        GL20.glUniform1f(0, thickness);
    }

    public void setInnerRect(float left, float top, float right, float bottom) {
        GL20.glUniform4f(1, left, top, right, bottom);
    }
}
