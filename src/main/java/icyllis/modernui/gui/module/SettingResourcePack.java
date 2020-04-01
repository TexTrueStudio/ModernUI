/*
 * Modern UI.
 * Copyright (C) 2019 BloCamLimb. All rights reserved.
 *
 * Modern UI is free software: you can redistribute it and/or modify
 * it under the terms of the GNU General Public License as published by
 * the Free Software Foundation, either version 3 of the License, or
 * (at your option) any later version.
 *
 * Modern UI is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE. See the
 * GNU General Public License for more details.
 *
 * You should have received a copy of the GNU General Public License
 * along with Modern UI. If not, see <https://www.gnu.org/licenses/>.
 */

package icyllis.modernui.gui.module;

import icyllis.modernui.gui.element.IElement;
import icyllis.modernui.gui.scroll.ResourceScrollWindow;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.IGuiEventListener;
import net.minecraft.client.renderer.texture.TextureManager;
import net.minecraft.util.ResourceLocation;

import java.util.ArrayList;
import java.util.List;
import java.util.function.Consumer;
import java.util.function.Function;

public class SettingResourcePack implements IGuiModule {

    private Minecraft minecraft;

    private List<IElement> elements = new ArrayList<>();

    private List<IGuiEventListener> listeners = new ArrayList<>();

    private ResourceScrollWindow leftWindow;

    private ResourceScrollWindow rightWindow;

    public SettingResourcePack() {
        Function<Integer, Float> widthFunc = w -> Math.min((float) Math.round((w - 80) / 2f), 200);
        Function<Integer, Float> leftXFunc = w -> w / 2f - widthFunc.apply(w);

        leftWindow = new ResourceScrollWindow(leftXFunc, widthFunc);
        rightWindow = new ResourceScrollWindow(w -> w / 2f, widthFunc);

        Consumer<ResourceScrollWindow> consumer = s -> {
            elements.add(s);
            listeners.add(s);
        };
        consumer.accept(leftWindow);
        consumer.accept(rightWindow);
    }

    @Override
    public List<? extends IElement> getElements() {
        return elements;
    }

    @Override
    public List<? extends IGuiEventListener> getEventListeners() {
        return listeners;
    }
}
