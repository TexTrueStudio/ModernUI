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

package icyllis.modernui.forge;

import com.mojang.blaze3d.platform.Window;
import icyllis.modernui.ModernUI;
import icyllis.modernui.annotation.MainThread;
import icyllis.modernui.annotation.RenderThread;
import icyllis.modernui.core.Core;
import icyllis.modernui.fragment.Fragment;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.screens.Screen;
import net.minecraft.core.BlockPos;
import net.minecraft.network.FriendlyByteBuf;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.util.Mth;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.inventory.AbstractContainerMenu;
import net.minecraft.world.inventory.MenuConstructor;
import net.minecraftforge.common.MinecraftForge;
import net.minecraftforge.event.entity.player.PlayerContainerEvent;
import org.jetbrains.annotations.ApiStatus;

import javax.annotation.Nonnull;
import javax.annotation.Nullable;
import java.io.PrintWriter;
import java.util.concurrent.CopyOnWriteArrayList;
import java.util.function.Consumer;

/**
 * Public APIs for Minecraft Forge mods to Modern UI.
 *
 * @since 3.3
 */
public final class MuiForgeApi {

    static final CopyOnWriteArrayList<OnScrollListener> sOnScrollListeners =
            new CopyOnWriteArrayList<>();
    static final CopyOnWriteArrayList<OnScreenChangeListener> sOnScreenChangeListeners =
            new CopyOnWriteArrayList<>();
    static final CopyOnWriteArrayList<OnWindowResizeListener> sOnWindowResizeListeners =
            new CopyOnWriteArrayList<>();
    static final CopyOnWriteArrayList<OnDebugDumpListener> sOnDebugDumpListeners =
            new CopyOnWriteArrayList<>();

    private MuiForgeApi() {
    }

    /**
     * Get the lifecycle of current server. At most one server instance exists
     * at the same time, which may be integrated or dedicated.
     *
     * @return {@code true} if server started
     */
    @ApiStatus.Internal
    public static boolean isServerStarted() {
        return ServerHandler.INSTANCE.mStarted;
    }

    /**
     * Open a container menu on server, generating a container id represents the next screen
     * (due to network latency). Then send a packet to the player to request the application
     * user interface on client. This method must be called from server thread, this client
     * will trigger a {@link OpenMenuEvent} later.
     * <p>
     * This is served as a client/server interaction model, there must be a running server.
     *
     * @param player   the server player to open the screen for
     * @param provider a provider to create a menu on server side
     * @see #openMenu(Player, MenuConstructor, Consumer)
     * @see net.minecraftforge.common.extensions.IForgeMenuType#create(net.minecraftforge.network.IContainerFactory)
     * @see OpenMenuEvent
     * @deprecated use {@link MenuScreenFactory} instead
     */
    @Deprecated
    public static void openMenu(@Nonnull Player player, @Nonnull MenuConstructor provider) {
        openMenu(player, provider, (Consumer<FriendlyByteBuf>) null);
    }

    /**
     * Open a container menu on server, generating a container id represents the next screen
     * (due to network latency). Then send a packet to the player to request the application
     * user interface on client. This method must be called from server thread, this client
     * will trigger a {@link OpenMenuEvent} later.
     * <p>
     * This is served as a client/server interaction model, there must be a running server.
     *
     * @param player   the server player to open the screen for
     * @param provider a provider to create a menu on server side
     * @param pos      a block pos to send to client, this will be passed to
     *                 the menu supplier that registered on client
     * @see #openMenu(Player, MenuConstructor, Consumer)
     * @see net.minecraftforge.common.extensions.IForgeMenuType#create(net.minecraftforge.network.IContainerFactory)
     * @see OpenMenuEvent
     * @deprecated use {@link MenuScreenFactory} instead
     */
    @Deprecated
    public static void openMenu(@Nonnull Player player, @Nonnull MenuConstructor provider, @Nonnull BlockPos pos) {
        openMenu(player, provider, buf -> buf.writeBlockPos(pos));
    }

    /**
     * Open a container menu on server, generating a container id represents the next screen
     * (due to network latency). Then send a packet to the player to request the application
     * user interface on client. This method must be called from server thread, this client
     * will trigger a {@link OpenMenuEvent} later.
     * <p>
     * This is served as a client/server interaction model, there must be a running server.
     *
     * @param player   the server player to open the screen for
     * @param provider a provider to create a menu on server side
     * @param writer   a data writer to send additional data to client, this will be passed
     *                 to the menu supplier (IContainerFactory) that registered on client
     * @see net.minecraftforge.common.extensions.IForgeMenuType#create(net.minecraftforge.network.IContainerFactory)
     * @see OpenMenuEvent
     */
    @ApiStatus.Internal
    public static void openMenu(@Nonnull Player player, @Nonnull MenuConstructor provider,
                                @Nullable Consumer<FriendlyByteBuf> writer) {
        if (!(player instanceof ServerPlayer p)) {
            ModernUI.LOGGER.warn(ModernUI.MARKER, "openMenu() is not called from logical server",
                    new Exception().fillInStackTrace());
            return;
        }
        // do the same thing as ServerPlayer.openMenu()
        if (p.containerMenu != p.inventoryMenu) {
            p.closeContainer();
        }
        p.nextContainerCounter();
        AbstractContainerMenu menu = provider.createMenu(p.containerCounter, p.getInventory(), p);
        if (menu == null) {
            return;
        }
        NetworkMessages.openMenu(menu, writer).sendToPlayer(p);
        p.initMenu(menu);
        p.containerMenu = menu;
        MinecraftForge.EVENT_BUS.post(new PlayerContainerEvent.Open(p, menu));
    }

    /**
     * Start the lifecycle of user interface with the fragment and create views.
     * This method must be called from client side main thread.
     * <p>
     * This is served as a local interaction model, the server will not intersect with this before.
     * Otherwise, initiate this with a network model via {@link OpenMenuEvent#set(Fragment)})}.
     * <p>
     * Optionally, the main {@link Fragment} can implement {@link ScreenCallback}
     * to describe the screen properties.
     *
     * @param fragment the fragment
     */
    @MainThread
    public static void openScreen(@Nonnull Fragment fragment) {
        UIManager.getInstance().open(fragment);
    }

    /**
     * Get the elapsed time since the current screen is set, updated every frame on Render thread.
     * Ignoring game paused.
     *
     * @return elapsed time in milliseconds
     */
    @RenderThread
    public static long getElapsedTime() {
        return UIManager.getElapsedTime();
    }

    /**
     * Get synced UI frame time, updated every frame on Render thread. Ignoring game paused.
     *
     * @return frame time in milliseconds
     */
    @RenderThread
    public static long getFrameTime() {
        return getFrameTimeNanos() / 1000000;
    }

    /**
     * Get synced UI frame time, updated every frame on Render thread. Ignoring game paused.
     *
     * @return frame time in nanoseconds
     */
    @RenderThread
    public static long getFrameTimeNanos() {
        return UIManager.getFrameTimeNanos();
    }

    /**
     * Post a runnable to be executed asynchronously (no barrier) on UI thread.
     * This method is equivalent to calling {@link Core#getUiHandlerAsync()},
     * but {@link Core} is not a public API.
     *
     * @param r the Runnable that will be executed
     */
    public static void postToUiThread(@Nonnull Runnable r) {
        Core.getUiHandlerAsync().post(r);
    }

    /**
     * Returns whether the graphics engine failed to start, which means that UI will render nothing.
     * Call this after {@link net.minecraftforge.fml.event.lifecycle.FMLCommonSetupEvent} on render thread.
     */
    @RenderThread
    public static boolean hasNoRender() {
        return ModernUIForge.hasGLCapsError();
    }

    public static int calcGuiScales() {
        return calcGuiScales(Minecraft.getInstance().getWindow());
    }

    public static int calcGuiScales(@Nonnull Window window) {
        return calcGuiScales(window.getWidth(), window.getHeight());
    }

    public static int calcGuiScales(int framebufferWidth, int framebufferHeight) {
        int w = framebufferWidth / 16;
        int h = framebufferHeight / 9;
        int base = Math.min(w, h);

        int min;
        int max = Mth.clamp(Math.min(framebufferWidth / 12, h) / 27, 1, 9);
        if (max > 1) {
            min = Mth.clamp(base / 64, 2, 9);
        } else {
            min = 1;
        }

        int best;
        if (min > 1) {
            double step = base > 150 ? 40. : base > 100 ? 36. : 32.;
            int i = (int) (base / step);
            int j = (int) (Math.max(w, h) / step);
            double v1 = base / (i * 32.);
            if (v1 > 40 / 32. || j > i) {
                best = Math.min(max, i + 1);
            } else {
                best = Math.min(max, i);
            }
        } else {
            best = 1;
        }

        return min << 8 | best << 4 | max;
    }

    /**
     * Registers a callback to be called when {@link org.lwjgl.glfw.GLFWScrollCallback} is called.
     *
     * @param listener the listener to register
     * @see OnScrollListener
     */
    public static void addOnScrollListener(@Nonnull OnScrollListener listener) {
        sOnScrollListeners.addIfAbsent(listener);
    }

    /**
     * Remove a registered listener.
     *
     * @param listener the listener to unregister
     */
    public static void removeOnScrollListener(@Nonnull OnScrollListener listener) {
        sOnScrollListeners.remove(listener);
    }

    /**
     * Registers a callback to be called when {@link Minecraft#setScreen(Screen)} is called.
     *
     * @param listener the listener to register
     * @see OnScreenChangeListener
     */
    public static void addOnScreenChangeListener(@Nonnull OnScreenChangeListener listener) {
        sOnScreenChangeListeners.addIfAbsent(listener);
    }

    /**
     * Remove a registered listener.
     *
     * @param listener the listener to unregister
     */
    public static void removeOnScreenChangeListener(@Nonnull OnScreenChangeListener listener) {
        sOnScreenChangeListeners.remove(listener);
    }

    /**
     * Registers a callback to be invoked at the beginning of {@link Minecraft#resizeDisplay()}.
     *
     * @param listener the listener to register
     * @see OnWindowResizeListener
     */
    public static void addOnWindowResizeListener(@Nonnull OnWindowResizeListener listener) {
        sOnWindowResizeListeners.addIfAbsent(listener);
    }

    /**
     * Remove a registered listener.
     *
     * @param listener the listener to unregister
     */
    public static void removeOnWindowResizeListener(@Nonnull OnWindowResizeListener listener) {
        sOnWindowResizeListeners.remove(listener);
    }

    /**
     * Registers a callback to be called when Modern UI dumps its debug info to chat or console.
     *
     * @param listener the listener to register
     * @see OnDebugDumpListener
     */
    public static void addOnDebugDumpListener(@Nonnull OnDebugDumpListener listener) {
        sOnDebugDumpListeners.addIfAbsent(listener);
    }

    /**
     * Remove a registered OnDebugDumpListener.
     *
     * @param listener the listener to unregister
     */
    public static void removeOnDebugDumpListener(@Nonnull OnDebugDumpListener listener) {
        sOnDebugDumpListeners.remove(listener);
    }

    @FunctionalInterface
    public interface OnScrollListener {

        /**
         * Called when a scroll event polling from the main handler and responding to the main window.
         *
         * @param scrollX raw relative movement of the horizontal scroll wheel or touchpad gesture
         * @param scrollY raw relative movement of the vertical scroll wheel or touchpad gesture
         */
        void onScroll(double scrollX, double scrollY);
    }

    @FunctionalInterface
    public interface OnScreenChangeListener {

        /**
         * Called when {@link Minecraft#setScreen(Screen)} is called, and after Forge's
         * event is fired.
         *
         * @param oldScreen the old screen
         * @param newScreen the new screen
         */
        void onScreenChange(@Nullable Screen oldScreen, @Nullable Screen newScreen);
    }

    @FunctionalInterface
    public interface OnWindowResizeListener {

        /**
         * Invoked at the beginning of {@link Minecraft#resizeDisplay()}.
         * Gui scale algorithm is replaced by Modern UI, see {@link #calcGuiScales(Window)}.
         *
         * @param width       framebuffer width of the window in pixels
         * @param height      framebuffer height of the window in pixels
         * @param guiScale    the new gui scale will be applied to (not apply yet)
         * @param oldGuiScale the old gui scale, may be equal to the new gui scale
         */
        void onWindowResize(int width, int height, int guiScale, int oldGuiScale);
    }

    @FunctionalInterface
    public interface OnDebugDumpListener {

        /**
         * Called when Modern UI dumps its debug info to chat or console.
         *
         * @param writer the writer to add new lines
         */
        void onDebugDump(@Nonnull PrintWriter writer);
    }

    /* Screen */
    /*public static int getScreenBackgroundColor() {
        return (int) (BlurHandler.INSTANCE.getBackgroundAlpha() * 255.0f) << 24;
    }*/

    /* Minecraft */
    /*public static void displayInGameMenu(boolean usePauseScreen) {
        Minecraft minecraft = Minecraft.getInstance();
        if (minecraft.currentScreen == null) {
            // If press F3 + Esc and is single player and not open LAN world
            if (usePauseScreen && minecraft.isIntegratedServerRunning() && minecraft.getIntegratedServer() != null &&
             !minecraft.getIntegratedServer().getPublic()) {
                minecraft.displayGuiScreen(new IngameMenuScreen(false));
                minecraft.getSoundHandler().pause();
            } else {
                //UIManager.INSTANCE.openGuiScreen(new TranslationTextComponent("menu.game"), IngameMenuHome::new);
                minecraft.displayGuiScreen(new IngameMenuScreen(true));
            }
        }
    }*/
}
