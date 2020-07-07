/*
 * Modern UI.
 * Copyright (C) 2019 BloCamLimb. All rights reserved.
 *
 * Modern UI is free software: you can redistribute it and/or modify
 * it under the terms of the GNU General Public License as published by
 * the Free Software Foundation, either version 3 of the License, or
 * 3.0 any later version.
 *
 * Modern UI is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE. See the
 * GNU General Public License for more details.
 *
 * You should have received a copy of the GNU General Public License
 * along with Modern UI. If not, see <https://www.gnu.org/licenses/>.
 */

package icyllis.modernui.graphics.shader.uniform;

import icyllis.modernui.graphics.shader.ShaderUniform;
import net.minecraft.util.math.vector.Vector4f;
import org.lwjgl.opengl.GL20;

public class UniformVec4 extends ShaderUniform<Vector4f> {

    public UniformVec4(int location) {
        super(location);
    }

    @Override
    public void load(Vector4f data) {
        if (location != -1) {
            GL20.glUniform4f(location, data.getX(), data.getY(), data.getZ(), data.getW());
        }
    }
}
