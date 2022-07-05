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

package icyllis.modernui.textmc;

import icyllis.modernui.ModernUI;
import icyllis.modernui.core.Core;
import icyllis.modernui.forge.*;
import icyllis.modernui.graphics.font.GlyphManager;
import net.minecraft.Util;
import net.minecraft.client.Minecraft;
import net.minecraft.server.packs.resources.ResourceManagerReloadListener;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.api.distmarker.OnlyIn;
import net.minecraftforge.client.event.RegisterClientReloadListenersEvent;
import net.minecraftforge.common.ForgeConfigSpec;
import net.minecraftforge.eventbus.api.SubscribeEvent;
import net.minecraftforge.fml.ModContainer;
import net.minecraftforge.fml.ModLoadingContext;
import net.minecraftforge.fml.config.IConfigSpec;
import net.minecraftforge.fml.config.ModConfig;
import net.minecraftforge.fml.event.config.ModConfigEvent;
import net.minecraftforge.fml.event.lifecycle.FMLClientSetupEvent;
import net.minecraftforge.fml.event.lifecycle.ParallelDispatchEvent;
import net.minecraftforge.fml.javafmlmod.FMLJavaModLoadingContext;
import net.minecraftforge.fml.loading.FMLPaths;

import javax.annotation.Nonnull;

import static icyllis.modernui.ModernUI.*;

/**
 * Modern UI Text MC can bootstrap independently.
 */
@OnlyIn(Dist.CLIENT)
public final class ModernUITextMC {

    public static Config CONFIG;
    private static ForgeConfigSpec CONFIG_SPEC;

    private ModernUITextMC() {
    }

    @OnlyIn(Dist.CLIENT)
    public static void init() {
        FMLJavaModLoadingContext.get().getModEventBus().register(ModernUITextMC.class);
    }

    @OnlyIn(Dist.CLIENT)
    public static void initConfig() {
        FMLPaths.getOrCreateGameRelativePath(FMLPaths.CONFIGDIR.get().resolve(ModernUI.NAME_CPT), ModernUI.NAME_CPT);
        ModContainer mod = ModLoadingContext.get().getActiveContainer();

        ForgeConfigSpec.Builder builder = new ForgeConfigSpec.Builder();
        CONFIG = new Config(builder);
        CONFIG_SPEC = builder.build();
        mod.addConfig(new ModConfig(ModConfig.Type.CLIENT, CONFIG_SPEC, mod, ModernUI.NAME_CPT + "/text.toml"));

        FMLJavaModLoadingContext.get().getModEventBus().addListener(CONFIG::onReload);
    }

    @OnlyIn(Dist.CLIENT)
    @SubscribeEvent
    static void registerResourceListener(@Nonnull RegisterClientReloadListenersEvent event) {
        // language may reload, cause TranslatableComponent changed, so clear layout cache
        event.registerReloadListener((ResourceManagerReloadListener) manager -> TextLayoutEngine.getInstance().reload());
        LOGGER.debug(MARKER, "Registered language reload listener");
    }

    @OnlyIn(Dist.CLIENT)
    @SubscribeEvent
    static void setupClient(@Nonnull FMLClientSetupEvent event) {
        // preload text engine, note that this event is fired after client config first load
        // so that the typeface config is valid
        Minecraft.getInstance().execute(() -> {
            ModernUI.getSelectedTypeface();
            TextLayoutEngine.getInstance().lookupVanillaNode(ModernUI.NAME_CPT);
            if (ModernUIForge.isOptiFineLoaded()) {
                OptiFineIntegration.setFastRender(false);
                LOGGER.info(MARKER, "Disabled OptiFine Fast Render");
            }
        });
        MuiForgeApi.addOnWindowResizeListener((width, height, newScale, oldScale) -> {
            if (Core.hasRenderThread() && newScale != oldScale) {
                TextLayoutEngine.getInstance().reload();
            }
        });
        MuiForgeApi.addOnDebugDumpListener(pw -> {
            pw.print("Text Layout Entries: ");
            pw.println(TextLayoutEngine.getInstance().getLayoutCacheSize());
            pw.println("Glyph Manager:");
            GlyphManager.getInstance().dumpInfo(pw);
        });
        LOGGER.info(MARKER, "Loaded modern text engine");
    }

    @OnlyIn(Dist.CLIENT)
    @SubscribeEvent
    static void onParallelDispatch(@Nonnull ParallelDispatchEvent event) {
        // since Forge EVENT_BUS is not started yet, we should manually maintain that
        // in case of some mods render texts before entering main menu
        event.enqueueWork(() -> TextLayoutEngine.getInstance().cleanup());
    }

    /*@OnlyIn(Dist.CLIENT)
    @SubscribeEvent
    static void registerShaders(@Nonnull RegisterShadersEvent event) {
        ResourceProvider provider = event.getResourceManager();
        try {
            event.registerShader(new ShaderInstance(provider, TextRenderType.SHADER_RL,
                    DefaultVertexFormat.POSITION_COLOR_TEX_LIGHTMAP), TextRenderType::setShader);
            event.registerShader(new ShaderInstance(provider, TextRenderType.SHADER_SEE_THROUGH_RL,
                    DefaultVertexFormat.POSITION_COLOR_TEX_LIGHTMAP), TextRenderType::setShaderSeeThrough);
        } catch (IOException e) {
            throw new RuntimeException("Bad shaders", e);
        }
    }*/

    @OnlyIn(Dist.CLIENT)
    public static class Config {

        //final ForgeConfigSpec.BooleanValue globalRenderer;
        public final ForgeConfigSpec.BooleanValue mAllowShadow;
        public final ForgeConfigSpec.BooleanValue mFixedResolution;
        public final ForgeConfigSpec.IntValue mBaseFontSize;
        public final ForgeConfigSpec.IntValue mBaseline;
        public final ForgeConfigSpec.BooleanValue mSuperSampling;
        public final ForgeConfigSpec.BooleanValue mPixelAligned;

        //private final ForgeConfigSpec.BooleanValue antiAliasing;
        //private final ForgeConfigSpec.BooleanValue highPrecision;
        //private final ForgeConfigSpec.BooleanValue enableMipmap;
        //private final ForgeConfigSpec.IntValue mipmapLevel;
        //private final ForgeConfigSpec.IntValue resolutionLevel;
        //private final ForgeConfigSpec.IntValue defaultFontSize;

        private Config(@Nonnull ForgeConfigSpec.Builder builder) {
            builder.comment("Text Config")
                    .push("text");

            /*globalRenderer = builder.comment(
                    "Apply Modern UI font renderer (including text layouts) to the entire game rather than only " +
                            "Modern UI itself.")
                    .define("globalRenderer", true);*/
            mAllowShadow = builder.comment(
                            "Allow text renderer to drop shadow, setting to false can improve performance.")
                    .define("allowShadow", true);
            mFixedResolution = builder.comment(
                            "Fixed resolution level. When the GUI scale increases, the resolution level keeps.",
                            "Gui scale should be even numbers (2, 4, 6...), based on Minecraft GUI system.",
                            "If your fonts are not bitmap fonts, then you should keep this setting false.")
                    .define("fixedResolution", false);
            mBaseFontSize = builder.comment(
                            "Define base font size, vanilla is 8. For bitmap font, it's 8x or 16x if fixed resolution.")
                    .defineInRange("fontSize", 8, 6, 10);
            mBaseline = builder.comment(
                            "Control vertical baseline for vanilla text layout, in normalized pixels.",
                            "For smaller font, 6 is recommended. The default value is 7.")
                    .defineInRange("baseline", 7, 5, 9);
            mSuperSampling = builder.comment(
                            "Super sampling can make the text more sharper with large font size or in the world.",
                            "But in some cases, it will make the edge too blurry and difficult to read.")
                    .define("superSampling", false);
            mPixelAligned = builder.comment(
                            "Enable to make each glyph in the text layout pixel aligned in the screen space.",
                            "This may not deform the text with bitmap fonts or fixed resolutions or linear sampling.")
                    .define("pixelAligned", false);
            /*antiAliasing = builder.comment(
                    "Enable font anti-aliasing.")
                    .define("antiAliasing", true);
            highPrecision = builder.comment(
                    "Enable high precision rendering, this is very useful especially when the font is very small.")
                    .define("highPrecision", true);
            enableMipmap = builder.comment(
                    "Enable mipmap for font textures, this makes font will not be blurred when scaling down.")
                    .define("enableMipmap", true);
            mipmapLevel = builder.comment(
                    "The mipmap level for font textures.")
                    .defineInRange("mipmapLevel", 4, 0, 4);*/
            /*resolutionLevel = builder.comment(
                    "The resolution level of font, higher levels would better work with high resolution monitors.",
                    "Reference: 1 (Standard, 1.5K Fullscreen), 2 (High, 2K~3K Fullscreen), 3 (Ultra, 4K Fullscreen)",
                    "This should match your GUI scale. Scale -> Level: [1,2] -> 1; [3,4] -> 2; [5,) -> 3")
                    .defineInRange("resolutionLevel", 2, 1, 3);*/
            /*defaultFontSize = builder.comment(
                    "The default font size for texts with no size specified. (deprecated, to be removed)")
                    .defineInRange("defaultFontSize", 16, 12, 20);*/

            builder.pop();
        }

        public void saveAndReload() {
            Util.ioPool().execute(() -> {
                CONFIG_SPEC.save();
                reload();
            });
        }

        void onReload(@Nonnull ModConfigEvent event) {
            final IConfigSpec<?> spec = event.getConfig().getSpec();
            if (spec != CONFIG_SPEC) {
                return;
            }
            reload();
            LOGGER.debug(MARKER, "Text config reloaded with {}", event.getClass().getSimpleName());
        }

        void reload() {
            boolean reload = false;
            ModernFontRenderer.sAllowShadow = mAllowShadow.get();
            if (TextLayoutEngine.sFixedResolution != mFixedResolution.get()) {
                TextLayoutEngine.sFixedResolution = mFixedResolution.get();
                reload = true;
            }
            if (TextLayoutProcessor.sBaseFontSize != mBaseFontSize.get()) {
                TextLayoutProcessor.sBaseFontSize = mBaseFontSize.get();
                reload = true;
            }
            TextRenderNode.sVanillaBaselineOffset = mBaseline.get().floatValue();
            if (TextLayoutEngine.sSuperSampling != mSuperSampling.get()) {
                TextLayoutEngine.sSuperSampling = mSuperSampling.get();
                reload = true;
            }
            if (TextLayoutProcessor.sPixelAligned != mPixelAligned.get()) {
                TextLayoutProcessor.sPixelAligned = mPixelAligned.get();
                reload = true;
            }
            if (reload) {
                Minecraft.getInstance().submit(() -> TextLayoutEngine.getInstance().reload());
            }
            /*GlyphManagerForge.sPreferredFont = preferredFont.get();
            GlyphManagerForge.sAntiAliasing = antiAliasing.get();
            GlyphManagerForge.sHighPrecision = highPrecision.get();
            GlyphManagerForge.sEnableMipmap = enableMipmap.get();
            GlyphManagerForge.sMipmapLevel = mipmapLevel.get();*/
            //GlyphManager.sResolutionLevel = resolutionLevel.get();
            //TextLayoutEngine.sDefaultFontSize = defaultFontSize.get();
        }
    }
}
