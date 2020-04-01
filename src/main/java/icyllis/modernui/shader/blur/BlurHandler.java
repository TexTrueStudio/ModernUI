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

package icyllis.modernui.shader.blur;

import icyllis.modernui.gui.master.GlobalModuleManager;
import icyllis.modernui.system.ModernUI;
import net.minecraft.client.Minecraft;
import net.minecraft.client.renderer.GameRenderer;
import net.minecraft.client.resources.ClientResourcePackInfo;
import net.minecraft.client.shader.Shader;
import net.minecraft.client.shader.ShaderDefault;
import net.minecraft.client.shader.ShaderGroup;
import net.minecraft.resources.IPackFinder;
import net.minecraft.resources.PackCompatibility;
import net.minecraft.resources.ResourcePackInfo;
import net.minecraft.resources.ResourcePackList;
import net.minecraft.util.ResourceLocation;
import net.minecraft.util.text.StringTextComponent;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.api.distmarker.OnlyIn;
import net.minecraftforge.fml.common.ObfuscationReflectionHelper;

import javax.annotation.Nonnull;
import java.lang.reflect.Field;
import java.util.List;
import java.util.Map;

@OnlyIn(Dist.CLIENT)
public enum BlurHandler {
    INSTANCE;

    private final ResourceLocation BLUR = new ResourceLocation("shaders/post/fade_in_blur.json");

    private DummyResourcePack sp = new DummyResourcePack();

    private boolean changingProgress;

    BlurHandler() {
        this.loadResourcePack();
    }

    private void loadResourcePack() {
        ResourcePackList<ClientResourcePackInfo> rps = ObfuscationReflectionHelper.getPrivateValue(Minecraft.class, Minecraft.getInstance(), "field_110448_aq");
        if(rps != null)
            rps.addPackFinder(new IPackFinder() {
                @SuppressWarnings("unchecked")
                @Override
                public <T extends ResourcePackInfo> void addPackInfosToMap(@Nonnull Map<String, T> nameToPackMap, @Nonnull ResourcePackInfo.IFactory<T> packInfoFactory) {
                    ResourcePackInfo pack = new ClientResourcePackInfo(ModernUI.MODID + "_blur", true, () -> sp, new StringTextComponent(sp.getName()), new StringTextComponent(""),
                            PackCompatibility.COMPATIBLE, ResourcePackInfo.Priority.BOTTOM, true, null, true);
                    nameToPackMap.put(ModernUI.MODID + "_blur", (T) pack);
                }
            });
    }

    public void blur(boolean hasGui) {
        if (Minecraft.getInstance().world != null) {
            GameRenderer gr = Minecraft.getInstance().gameRenderer;
            if (hasGui) {
                if (gr.getShaderGroup() == null) {
                    gr.loadShader(BLUR);
                    changingProgress = true;
                    ModernUI.LOGGER.debug("Blur shader loaded");
                } else {
                    ModernUI.LOGGER.debug("Can't load blur shader");
                }
            } else {
                gr.stopUseShader();
                changingProgress = false;
            }
        }
    }

    public void tick() {
        if (changingProgress) {
            float p = Math.min(GlobalModuleManager.INSTANCE.getAnimationTime(), 4.0f);
            this.updateProgress(p);
            if (p == 4.0f) {
                changingProgress = false;
            }
        }
    }

    private void updateProgress(float value) {
        ShaderGroup sg = Minecraft.getInstance().gameRenderer.getShaderGroup();
        if(sg == null)
            return;
        List<Shader> shaders = sg.listShaders;
        for (Shader s : shaders) {
            ShaderDefault u = s.getShaderManager().getShaderUniform("Progress");
            u.set(value);
        }
    }

}
