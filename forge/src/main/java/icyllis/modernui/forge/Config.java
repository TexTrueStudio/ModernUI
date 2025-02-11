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
import icyllis.modernui.core.Core;
import icyllis.modernui.core.Handler;
import icyllis.modernui.graphics.Color;
import icyllis.modernui.graphics.font.GLFontAtlas;
import icyllis.modernui.graphics.font.GlyphManager;
import icyllis.modernui.textmc.TextLayoutEngine;
import icyllis.modernui.view.ViewConfiguration;
import net.minecraft.Util;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.screens.ChatScreen;
import net.minecraft.client.resources.language.I18n;
import net.minecraftforge.common.ForgeConfigSpec;
import net.minecraftforge.fml.ModContainer;
import net.minecraftforge.fml.ModLoadingContext;
import net.minecraftforge.fml.config.IConfigSpec;
import net.minecraftforge.fml.config.ModConfig;
import net.minecraftforge.fml.event.config.ModConfigEvent;
import net.minecraftforge.fml.javafmlmod.FMLJavaModLoadingContext;
import net.minecraftforge.fml.loading.FMLEnvironment;
import net.minecraftforge.fml.loading.FMLPaths;
import org.jetbrains.annotations.ApiStatus;
import org.lwjgl.glfw.GLFW;

import javax.annotation.Nonnull;
import java.util.*;

import static icyllis.modernui.ModernUI.*;

@ApiStatus.Internal
final class Config {

    static Client CLIENT;
    private static ForgeConfigSpec CLIENT_SPEC;

    static final Common COMMON;
    private static final ForgeConfigSpec COMMON_SPEC;

    /*static final Server SERVER;
    private static final ForgeConfigSpec SERVER_SPEC;*/

    static {
        ForgeConfigSpec.Builder builder;

        if (FMLEnvironment.dist.isClient()) {
            builder = new ForgeConfigSpec.Builder();
            CLIENT = new Client(builder);
            CLIENT_SPEC = builder.build();
        }

        builder = new ForgeConfigSpec.Builder();
        COMMON = new Common(builder);
        COMMON_SPEC = builder.build();

        /*builder = new ForgeConfigSpec.Builder();
        SERVER = new Server(builder);
        SERVER_SPEC = builder.build();*/
    }

    static void init() {
        FMLPaths.getOrCreateGameRelativePath(FMLPaths.CONFIGDIR.get().resolve(ModernUI.NAME_CPT), ModernUI.NAME_CPT);
        ModContainer container = ModLoadingContext.get().getActiveContainer();
        if (FMLEnvironment.dist.isClient()) {
            container.addConfig(new ModConfig(ModConfig.Type.CLIENT, CLIENT_SPEC, container,
                    ModernUI.NAME_CPT + "/client.toml")); // client only
            container.addConfig(new ModConfig(ModConfig.Type.COMMON, COMMON_SPEC, container,
                    ModernUI.NAME_CPT + "/common.toml")); // client only, but server logic
            /*container.addConfig(new ModConfig(ModConfig.Type.SERVER, SERVER_SPEC, container,
                    ModernUI.NAME_CPT + "/server.toml")); // sync to client (local)*/
        } else {
            container.addConfig(new ModConfig(ModConfig.Type.COMMON, COMMON_SPEC, container,
                    ModernUI.NAME_CPT + "/common.toml")); // dedicated server only
            /*container.addConfig(new ModConfig(ModConfig.Type.SERVER, SERVER_SPEC, container,
                    ModernUI.NAME_CPT + "/server.toml")); // sync to client (network)*/
        }
        FMLJavaModLoadingContext.get().getModEventBus().addListener(Config::reload);
    }

    static void reload(@Nonnull ModConfigEvent event) {
        final IConfigSpec<?> spec = event.getConfig().getSpec();
        if (spec == CLIENT_SPEC) {
            CLIENT.reload();
            LOGGER.debug(MARKER, "Client config reloaded with {}", event.getClass().getSimpleName());
        } else if (spec == COMMON_SPEC) {
            COMMON.reload();
            LOGGER.debug(MARKER, "Common config reloaded with {}", event.getClass().getSimpleName());
        }/* else if (spec == SERVER_SPEC) {
            SERVER.reload();
            LOGGER.debug(MARKER, "Server config reloaded with {}", event.getClass().getSimpleName());
        }*/
    }

    /*private static class C extends ModConfig {

        private static final Toml _TOML = new Toml();

        public C(Type type, ForgeConfigSpec spec, ModContainer container, String name) {
            super(type, spec, container, ModernUI.NAME_CPT + "/" + name + ".toml");
        }

        @Override
        public ConfigFileTypeHandler getHandler() {
            return _TOML;
        }
    }

    private static class Toml extends ConfigFileTypeHandler {

        private Toml() {
        }

        // reroute it to the global config directory
        // see ServerLifecycleHooks, ModConfig.Type.SERVER
        private static Path reroute(@Nonnull Path configBasePath) {
            //noinspection SpellCheckingInspection
            if (configBasePath.endsWith("serverconfig")) {
                return FMLPaths.CONFIGDIR.get();
            }
            return configBasePath;
        }

        @Override
        public Function<ModConfig, CommentedFileConfig> reader(Path configBasePath) {
            return super.reader(reroute(configBasePath));
        }

        @Override
        public void unload(Path configBasePath, ModConfig config) {
            super.unload(reroute(configBasePath), config);
        }
    }*/

    public static class Client {

        public static final int ANIM_DURATION_MIN = 0;
        public static final int ANIM_DURATION_MAX = 800;
        public static final int BLUR_RADIUS_MIN = 2;
        public static final int BLUR_RADIUS_MAX = 18;
        public static final float FONT_SCALE_MIN = 0.5f;
        public static final float FONT_SCALE_MAX = 2.0f;

        final ForgeConfigSpec.BooleanValue mBlurEffect;
        final ForgeConfigSpec.IntValue mBackgroundDuration;
        final ForgeConfigSpec.IntValue mBlurRadius;
        final ForgeConfigSpec.ConfigValue<List<? extends String>> mBackgroundColor;
        final ForgeConfigSpec.BooleanValue mInventoryPause;
        final ForgeConfigSpec.BooleanValue mTooltip;
        final ForgeConfigSpec.ConfigValue<List<? extends String>> mTooltipFill;
        final ForgeConfigSpec.ConfigValue<List<? extends String>> mTooltipStroke;
        final ForgeConfigSpec.IntValue mTooltipDuration;
        final ForgeConfigSpec.BooleanValue mDing;
        //private final ForgeConfigSpec.BooleanValue hudBars;
        final ForgeConfigSpec.BooleanValue mForceRtl;
        final ForgeConfigSpec.DoubleValue mFontScale;
        final ForgeConfigSpec.EnumValue<WindowMode> mWindowMode;
        final ForgeConfigSpec.BooleanValue mUseNewGuiScale;
        final ForgeConfigSpec.BooleanValue mRemoveSignature;
        final ForgeConfigSpec.BooleanValue mRemoveTelemetry;
        final ForgeConfigSpec.BooleanValue mSecurePublicKey;

        final ForgeConfigSpec.IntValue mScrollbarSize;
        final ForgeConfigSpec.IntValue mTouchSlop;
        final ForgeConfigSpec.IntValue mMinScrollbarTouchTarget;
        final ForgeConfigSpec.IntValue mMinimumFlingVelocity;
        final ForgeConfigSpec.IntValue mMaximumFlingVelocity;
        final ForgeConfigSpec.IntValue mOverscrollDistance;
        final ForgeConfigSpec.IntValue mOverflingDistance;
        final ForgeConfigSpec.DoubleValue mVerticalScrollFactor;
        final ForgeConfigSpec.DoubleValue mHorizontalScrollFactor;

        private final ForgeConfigSpec.ConfigValue<List<? extends String>> mBlurBlacklist;

        final ForgeConfigSpec.BooleanValue mAntiAliasing;
        final ForgeConfigSpec.BooleanValue mFractionalMetrics;
        final ForgeConfigSpec.BooleanValue mLinearSampling;
        final ForgeConfigSpec.ConfigValue<List<? extends String>> mFontFamily;

        final ForgeConfigSpec.BooleanValue mSkipGLCapsError;
        final ForgeConfigSpec.BooleanValue mShowGLCapsError;

        private WindowMode mLastWindowMode;

        private Client(@Nonnull ForgeConfigSpec.Builder builder) {
            builder.comment("Screen Config")
                    .push("screen");

            mBackgroundDuration = builder.comment(
                            "The duration of GUI background color and blur radius animation in milliseconds. (0 = OFF)")
                    .defineInRange("animationDuration", 200, ANIM_DURATION_MIN, ANIM_DURATION_MAX);
            mBackgroundColor = builder.comment(
                            "The GUI background color in #RRGGBB or #AARRGGBB format. Default value: #66000000",
                            "Can be one to four values representing top left, top right, bottom right and bottom left" +
                                    " color.",
                            "Multiple values produce a gradient effect, whereas one value produce a solid color.",
                            "When values is less than 4, the rest of the corner color will be replaced by the last " +
                                    "value.")
                    .defineList("backgroundColor", () -> {
                        List<String> list = new ArrayList<>();
                        list.add("#99000000");
                        return list;
                    }, o -> true);

            mBlurEffect = builder.comment(
                            "Add blur effect to GUI background when opened, it is incompatible with OptiFine's FXAA " +
                                    "shader and some mods.")
                    .define("blurEffect", true);
            mBlurRadius = builder.comment(
                            "The strength for two-pass gaussian convolution blur effect, spp = (radius * 2) + 1.")
                    .defineInRange("blurRadius", 5, BLUR_RADIUS_MIN, BLUR_RADIUS_MAX);
            mBlurBlacklist = builder.comment(
                            "A list of GUI screen superclasses that won't activate blur effect when opened.")
                    .defineList("blurBlacklist", () -> {
                        List<String> list = new ArrayList<>();
                        list.add(ChatScreen.class.getName());
                        return list;
                    }, o -> true);
            mInventoryPause = builder.comment(
                            "(Beta) Pause the game when inventory (also includes creative mode) opened.")
                    .define("inventoryPause", false);

            builder.pop();

            builder.comment("Tooltip Config")
                    .push("tooltip");

            mTooltip = builder.comment(
                            "Whether to enable Modern UI tooltip style, or back to vanilla style.")
                    .define("enable", true);
            mTooltipFill = builder.comment(
                            "The tooltip FILL color in #RRGGBB or #AARRGGBB format. Default: #D4000000",
                            "Can be one to four values representing top left, top right, bottom right and bottom left" +
                                    " color.",
                            "Multiple values produce a gradient effect, whereas one value produce a solid color.",
                            "When values is less than 4, the rest of the corner color will be replaced by the last " +
                                    "value.")
                    .defineList("colorFill", () -> {
                        List<String> list = new ArrayList<>();
                        list.add("#D4000000");
                        return list;
                    }, $ -> true);
            mTooltipStroke = builder.comment(
                            "The tooltip STROKE color in #RRGGBB or #AARRGGBB format. Default: #F0AADCF0, #F0DAD0F4, " +
                                    "#F0FFC3F7 and #F0DAD0F4",
                            "Can be one to four values representing top left, top right, bottom right and bottom left" +
                                    " color.",
                            "Multiple values produce a gradient effect, whereas one value produce a solid color.",
                            "When values is less than 4, the rest of the corner color will be replaced by the last " +
                                    "value.")
                    .defineList("colorStroke", () -> {
                        List<String> list = new ArrayList<>();
                        list.add("#F0AADCF0");
                        list.add("#F0DAD0F4");
                        list.add("#F0FFC3F7");
                        list.add("#F0DAD0F4");
                        return list;
                    }, $ -> true);
            mTooltipDuration = builder.comment(
                            "The duration of tooltip alpha animation in milliseconds. (0 = OFF)")
                    .defineInRange("animationDuration", 100, ANIM_DURATION_MIN, ANIM_DURATION_MAX);

            builder.pop();

            builder.comment("General Config")
                    .push("general");

            mDing = builder.comment("Play a sound effect when the game is loaded.")
                    .define("ding", true);

            /*hudBars = builder.comment(
                    "Show additional HUD bars added by ModernUI on the bottom-left of the screen.")
                    .define("hudBars", false);*/

            mWindowMode = builder.comment("Control the window mode, normal mode does nothing.")
                    .defineEnum("windowMode", WindowMode.NORMAL);
            mUseNewGuiScale = builder.comment("Whether to replace vanilla GUI scale button to slider with tips.")
                    .define("useNewGuiScale", true);

            mSkipGLCapsError = builder.comment("UI renderer is disabled when the OpenGL capability test fails.",
                            "Sometimes the driver reports wrong values, you can enable this to ignore it.")
                    .define("skipGLCapsError", false);
            mShowGLCapsError = builder.comment("A dialog popup is displayed when the OpenGL capability test fails.",
                            "Set to false to not show it. This is ignored when skipGLCapsError=true")
                    .define("showGLCapsError", true);

            mRemoveSignature = builder.comment("Remove signature of chat messages and commands.")
                    .define("removeSignature", false);
            mRemoveTelemetry = builder.comment("Remove telemetry event of client behaviors.")
                    .define("removeTelemetry", false);
            mSecurePublicKey = builder.comment("Don't report profile's public key to server.")
                    .define("securePublicKey", false);

            builder.pop();

            builder.comment("View system config, only applied to Modern UI Core.")
                    .push("view");

            mForceRtl = builder.comment("Force layout direction to RTL, otherwise, the current Locale setting.")
                    .define("forceRtl", false);
            mFontScale = builder.comment("The global font scale used with sp units.")
                    .defineInRange("fontScale", 1.0f, FONT_SCALE_MIN, FONT_SCALE_MAX);
            mScrollbarSize = builder.comment("Default scrollbar size in dips.")
                    .defineInRange("scrollbarSize", ViewConfiguration.SCROLL_BAR_SIZE, 0, 1024);
            mTouchSlop = builder.comment("Distance a touch can wander before we think the user is scrolling in dips.")
                    .defineInRange("touchSlop", ViewConfiguration.TOUCH_SLOP, 0, 1024);
            mMinScrollbarTouchTarget = builder.comment("Minimum size of the touch target for a scrollbar in dips.")
                    .defineInRange("minScrollbarTouchTarget", ViewConfiguration.MIN_SCROLLBAR_TOUCH_TARGET, 0, 1024);
            mMinimumFlingVelocity = builder.comment("Minimum velocity to initiate a fling in dips per second.")
                    .defineInRange("minimumFlingVelocity", ViewConfiguration.MINIMUM_FLING_VELOCITY, 0, 32767);
            mMaximumFlingVelocity = builder.comment("Maximum velocity to initiate a fling in dips per second.")
                    .defineInRange("maximumFlingVelocity", ViewConfiguration.MAXIMUM_FLING_VELOCITY, 0, 32767);
            mOverscrollDistance = builder.comment("Max distance in dips to overscroll for edge effects.")
                    .defineInRange("overscrollDistance", ViewConfiguration.OVERSCROLL_DISTANCE, 0, 1024);
            mOverflingDistance = builder.comment("Max distance in dips to overfling for edge effects.")
                    .defineInRange("overflingDistance", ViewConfiguration.OVERFLING_DISTANCE, 0, 1024);
            mVerticalScrollFactor = builder.comment("Amount to scroll in response to a vertical scroll event, in dips" +
                            " " +
                            "per axis value.")
                    .defineInRange("verticalScrollFactor", ViewConfiguration.VERTICAL_SCROLL_FACTOR, 0, 1024);
            mHorizontalScrollFactor = builder.comment("Amount to scroll in response to a horizontal scroll event, in " +
                            "dips per axis value.")
                    .defineInRange("horizontalScrollFactor", ViewConfiguration.HORIZONTAL_SCROLL_FACTOR, 0, 1024);

            builder.pop();


            builder.comment("Font Config")
                    .push("font");

            mAntiAliasing = builder.comment(
                            "Control the anti-aliasing of raw glyph rendering.")
                    .define("antiAliasing", true);
            mFractionalMetrics = builder.comment(
                            "Control the fractional metrics of raw glyph rendering.",
                            "Disable for rougher fonts; Enable for smoother fonts.")
                    .define("fractionalMetrics", true);
            mLinearSampling = builder.comment(
                            "Enable linear sampling for font atlases with mipmaps, mag filter will be always NEAREST.",
                            "If your fonts are not bitmap fonts, then you should keep this setting true.")
                    .define("linearSampling", true);
            // Segoe UI, Source Han Sans CN Medium, Noto Sans, Open Sans, San Francisco, Calibri,
            // Microsoft YaHei UI, STHeiti, SimHei, SansSerif
            mFontFamily = builder.comment(
                            "A set of font families with fallbacks to determine the typeface to use.",
                            "TrueType & OpenTrue are supported. Each element can be one of the following three cases.",
                            "1) Font family root name for those installed on your PC, for instance: Segoe UI",
                            "2) File path for external fonts on your PC, for instance: /usr/shared/fonts/x.otf",
                            "3) Resource location for those loaded with resource packs, for instance: " +
                                    "modernui:font/biliw.otf",
                            "Using bitmap fonts should consider other text settings, default glyph size should be 16x.",
                            "This list is only read once when the game is loaded. A game restart is required to reload")
                    .defineList("fontFamily", () -> {
                        List<String> list = new ArrayList<>();
                        list.add("modernui:font/default.ttf");
                        list.add("Segoe UI");
                        list.add("modernui:font/biliw.otf");
                        list.add("Noto Sans");
                        list.add("San Francisco");
                        list.add("Calibri");
                        list.add("Microsoft YaHei UI");
                        list.add("STHeiti");
                        list.add("SimHei");
                        list.add("SansSerif");
                        list.add("modernui:font/muii18ncompat/muii18ncompat.ttf");
                        return list;
                    }, s -> true);

            builder.pop();
        }

        void saveOnly() {
            Util.ioPool().execute(() -> CLIENT_SPEC.save());
        }

        void saveAndReload() {
            Util.ioPool().execute(() -> {
                CLIENT_SPEC.save();
                reload();
            });
        }

        private void reload() {
            BlurHandler.sBlurEffect = mBlurEffect.get();
            BlurHandler.sAnimationDuration = mBackgroundDuration.get();
            BlurHandler.sBlurRadius = mBlurRadius.get();

            List<? extends String> colors = mBackgroundColor.get();
            int color = 0x66000000;
            for (int i = 0; i < 4; i++) {
                if (colors != null && i < colors.size()) {
                    String s = colors.get(i);
                    try {
                        color = Color.parseColor(s);
                    } catch (Exception e) {
                        LOGGER.error(MARKER, "Wrong color format for screen background, index: {}", i, e);
                    }
                }
                BlurHandler.sBackgroundColor[i] = color;
            }

            BlurHandler.INSTANCE.loadBlacklist(mBlurBlacklist.get());

            ModernUIForge.sInventoryScreenPausesGame = mInventoryPause.get();
            ModernUIForge.sRemoveMessageSignature = mRemoveSignature.get();
            ModernUIForge.sRemoveTelemetrySession = mRemoveTelemetry.get();
            ModernUIForge.sSecureProfilePublicKey = mSecurePublicKey.get();

            TooltipRenderer.sTooltip = !ModernUIForge.hasGLCapsError() && mTooltip.get();

            colors = mTooltipFill.get();
            color = 0xD4000000;
            for (int i = 0; i < 4; i++) {
                if (colors != null && i < colors.size()) {
                    String s = colors.get(i);
                    try {
                        color = Color.parseColor(s);
                    } catch (Exception e) {
                        LOGGER.error(MARKER, "Wrong color format for tooltip fill, index: {}", i, e);
                    }
                }
                TooltipRenderer.sFillColor[i] = color;
            }
            colors = mTooltipStroke.get();
            color = 0xF0AADCF0;
            for (int i = 0; i < 4; i++) {
                if (colors != null && i < colors.size()) {
                    String s = colors.get(i);
                    try {
                        color = Color.parseColor(s);
                    } catch (Exception e) {
                        LOGGER.error(MARKER, "Wrong color format for tooltil stroke, index: {}", i, e);
                    }
                }
                TooltipRenderer.sStrokeColor[i] = color;
            }
            TooltipRenderer.sAnimationDuration = mTooltipDuration.get();

            UIManager.sPlaySoundOnLoaded = mDing.get();

            WindowMode windowMode = mWindowMode.get();
            if (mLastWindowMode != windowMode) {
                mLastWindowMode = windowMode;
                if (windowMode != WindowMode.NORMAL) {
                    Minecraft.getInstance().tell(windowMode::apply);
                }
            }

            //TestHUD.sBars = hudBars.get();
            Handler handler = Core.getUiHandlerAsync();
            if (handler != null) {
                handler.post(() -> {
                    UIManager.getInstance().updateLayoutDir(mForceRtl.get());
                    ViewConfiguration.get().setFontScale(mFontScale.get().floatValue());
                    ViewConfiguration.get().setScrollbarSize(mScrollbarSize.get());
                    ViewConfiguration.get().setTouchSlop(mTouchSlop.get());
                    ViewConfiguration.get().setMinScrollbarTouchTarget(mMinScrollbarTouchTarget.get());
                    ViewConfiguration.get().setMinimumFlingVelocity(mMinimumFlingVelocity.get());
                    ViewConfiguration.get().setMaximumFlingVelocity(mMaximumFlingVelocity.get());
                    ViewConfiguration.get().setOverscrollDistance(mOverscrollDistance.get());
                    ViewConfiguration.get().setOverflingDistance(mOverflingDistance.get());
                    ViewConfiguration.get().setVerticalScrollFactor(mVerticalScrollFactor.get().floatValue());
                    ViewConfiguration.get().setHorizontalScrollFactor(mHorizontalScrollFactor.get().floatValue());
                });
            }

            boolean reload = false;
            if (GlyphManager.sAntiAliasing != mAntiAliasing.get()) {
                GlyphManager.sAntiAliasing = mAntiAliasing.get();
                reload = true;
            }
            if (GlyphManager.sFractionalMetrics != mFractionalMetrics.get()) {
                GlyphManager.sFractionalMetrics = mFractionalMetrics.get();
                reload = true;
            }
            if (GLFontAtlas.sLinearSampling != mLinearSampling.get()) {
                GLFontAtlas.sLinearSampling = mLinearSampling.get();
                reload = true;
            }
            if (reload) {
                Minecraft.getInstance().submit(() -> TextLayoutEngine.getInstance().reloadAll());
            }

            ModernUI.getSelectedTypeface();
        }

        public enum WindowMode {
            NORMAL,
            FULLSCREEN,
            FULLSCREEN_BORDERLESS,
            MAXIMIZED,
            MINIMIZED,
            WINDOWED,
            WINDOWED_BORDERLESS;

            public void apply() {
                Window window = Minecraft.getInstance().getWindow();
                switch (this) {
                    case FULLSCREEN -> {
                        if (!window.isFullscreen()) {
                            window.toggleFullScreen();
                        }
                    }
                    case FULLSCREEN_BORDERLESS -> {
                        if (window.isFullscreen()) {
                            window.toggleFullScreen();
                        }
                        GLFW.glfwRestoreWindow(window.getWindow());
                        GLFW.glfwSetWindowAttrib(window.getWindow(),
                                GLFW.GLFW_DECORATED, GLFW.GLFW_FALSE);
                        GLFW.glfwMaximizeWindow(window.getWindow());
                    }
                    case MAXIMIZED -> {
                        if (window.isFullscreen()) {
                            window.toggleFullScreen();
                        }
                        GLFW.glfwSetWindowAttrib(window.getWindow(),
                                GLFW.GLFW_DECORATED, GLFW.GLFW_TRUE);
                        GLFW.glfwMaximizeWindow(window.getWindow());
                    }
                    case MINIMIZED -> {
                        if (window.isFullscreen()) {
                            window.toggleFullScreen();
                        }
                        GLFW.glfwSetWindowAttrib(window.getWindow(),
                                GLFW.GLFW_DECORATED, GLFW.GLFW_TRUE);
                        GLFW.glfwIconifyWindow(window.getWindow());
                    }
                    case WINDOWED -> {
                        if (window.isFullscreen()) {
                            window.toggleFullScreen();
                        }
                        GLFW.glfwSetWindowAttrib(window.getWindow(),
                                GLFW.GLFW_DECORATED, GLFW.GLFW_TRUE);
                        GLFW.glfwRestoreWindow(window.getWindow());
                    }
                    case WINDOWED_BORDERLESS -> {
                        if (window.isFullscreen()) {
                            window.toggleFullScreen();
                        }
                        GLFW.glfwSetWindowAttrib(window.getWindow(),
                                GLFW.GLFW_DECORATED, GLFW.GLFW_FALSE);
                        GLFW.glfwRestoreWindow(window.getWindow());
                    }
                }
            }

            @Nonnull
            @Override
            public String toString() {
                return I18n.get("modernui.windowMode." + name().toLowerCase(Locale.ROOT));
            }
        }
    }

    /**
     * Common config exists on physical client and physical server once game loaded.
     * They are independent and do not sync with each other.
     */
    public static class Common {

        private final ForgeConfigSpec.BooleanValue developerMode;
        final ForgeConfigSpec.IntValue oneTimeEvents;

        final ForgeConfigSpec.BooleanValue autoShutdown;

        final ForgeConfigSpec.ConfigValue<List<? extends String>> shutdownTimes;

        private Common(@Nonnull ForgeConfigSpec.Builder builder) {
            builder.comment("Developer Config")
                    .push("developer");

            developerMode = builder.comment("Whether to enable developer mode.")
                    .define("enableDeveloperMode", false);
            oneTimeEvents = builder
                    .defineInRange("oneTimeEvents", 0, Integer.MIN_VALUE, Integer.MAX_VALUE);

            builder.pop();

            builder.comment("Auto Shutdown Config")
                    .push("autoShutdown");

            autoShutdown = builder.comment(
                            "Enable auto-shutdown for server.")
                    .define("enable", false);
            shutdownTimes = builder.comment(
                            "The time points of when server will auto-shutdown. Format: HH:mm.")
                    .defineList("times", () -> {
                        List<String> list = new ArrayList<>();
                        list.add("04:00");
                        list.add("16:00");
                        return list;
                    }, s -> true);

            builder.pop();
        }

        private void reload() {
            ModernUIForge.sDeveloperMode = developerMode.get();
            ServerHandler.INSTANCE.determineShutdownTime();
        }
    }

    // server config is available when integrated server or dedicated server started
    // if on dedicated server, all config data will sync to remote client via network
    /*public static class Server {

        private Server(@Nonnull ForgeConfigSpec.Builder builder) {

        }

        private void reload() {

        }
    }*/
}
