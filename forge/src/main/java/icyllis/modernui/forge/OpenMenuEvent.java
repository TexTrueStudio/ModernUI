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

package icyllis.modernui.forge;

import icyllis.modernui.fragment.Fragment;
import net.minecraft.network.FriendlyByteBuf;
import net.minecraft.world.entity.player.Inventory;
import net.minecraft.world.inventory.AbstractContainerMenu;
import net.minecraftforge.eventbus.api.Cancelable;
import net.minecraftforge.eventbus.api.Event;
import net.minecraftforge.fml.event.IModBusEvent;
import net.minecraftforge.fml.loading.FMLEnvironment;
import net.minecraftforge.network.IContainerFactory;
import org.jetbrains.annotations.ApiStatus;

import javax.annotation.Nonnull;
import javax.annotation.Nullable;

/**
 * This event is triggered when the server requires the client to open a user
 * interface to display the container menu in world, this event is cancelled
 * after setting the user interface. The menu is created on the client from
 * {@link IContainerFactory}, which contains custom network data from server,
 * you can set the user interface through the data and the menu type.
 * For example:
 * <pre>{@code
 * @SubscribeEvent
 * static void onMenuOpen(OpenMenuEvent event) {
 *     if (event.getMenu().getType() == MyRegistry.MY_MENU) {
 *         event.set(new MyFragment());
 *     }
 * }
 * }</pre>
 * <p>
 * This event will be only posted to your own mod event bus on client main thread.
 * It's an error if no fragment set along with this event.
 * <p>
 * All menu types are registered via {@link net.minecraftforge.registries.RegisterEvent} ,
 * use {@link net.minecraftforge.common.extensions.IForgeMenuType#create(IContainerFactory)}
 * to create your registry entries when the registry event is triggered.
 *
 * @see MenuScreenFactory
 */
@ApiStatus.Internal
@Cancelable
public final class OpenMenuEvent extends Event implements IModBusEvent {

    static {
        assert (FMLEnvironment.dist.isClient());
    }

    @Nonnull
    private final AbstractContainerMenu mMenu;

    private Fragment mFragment;

    OpenMenuEvent(@Nonnull AbstractContainerMenu menu) {
        mMenu = menu;
    }

    /**
     * Get the container menu to open, which is created from
     * {@link IContainerFactory#create(int, Inventory, FriendlyByteBuf)}.
     *
     * @return the container menu
     */
    @Nonnull
    public AbstractContainerMenu getMenu() {
        return mMenu;
    }

    /**
     * Set the fragment for the menu. The event will be auto canceled.
     *
     * @param fragment the fragment
     */
    public void set(@Nonnull Fragment fragment) {
        mFragment = fragment;
        if (!isCanceled()) {
            setCanceled(true);
        }
    }

    @Nullable
    Fragment getFragment() {
        return mFragment;
    }
}
