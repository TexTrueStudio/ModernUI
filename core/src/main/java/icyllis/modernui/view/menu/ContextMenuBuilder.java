/*
 * Modern UI.
 * Copyright (C) 2019-2022 BloCamLimb. All rights reserved.
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

package icyllis.modernui.view.menu;

import icyllis.modernui.graphics.drawable.Drawable;
import icyllis.modernui.view.ContextMenu;
import icyllis.modernui.view.View;

/**
 * Implementation of the {@link ContextMenu} interface.
 * <p>
 * Most clients of the menu framework will never need to touch this
 * class.  However, if the client has a window that
 * is not a content view of a Dialog or Activity (for example, the
 * view was added directly to the window manager) and needs to show
 * context menus, it will use this class.
 * <p>
 * To use this class, instantiate it via {@link #ContextMenuBuilder()},
 * and optionally populate it with any of your custom items.  Finally,
 * call {@link #showDialog(View, IBinder)} which will populate the menu
 * with a view's context menu items and show the context menu.
 */
public class ContextMenuBuilder extends MenuBuilder implements ContextMenu {

    public ContextMenuBuilder() {
        super();
    }

    @Override
    public ContextMenu setHeaderIcon(Drawable icon) {
        super.setHeaderIconInt(icon);
        return this;
    }

    @Override
    public ContextMenu setHeaderTitle(CharSequence title) {
        super.setHeaderTitleInt(title);
        return this;
    }

    @Override
    public ContextMenu setHeaderView(View view) {
        super.setHeaderViewInt(view);
        return this;
    }

    //TODO show the menu
}
