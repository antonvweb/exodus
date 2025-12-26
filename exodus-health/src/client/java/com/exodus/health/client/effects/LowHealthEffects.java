package com.exodus.health.client.effects;

import com.exodus.core.ExodusCoreAPI;
import com.mojang.blaze3d.systems.RenderSystem;
import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.client.player.LocalPlayer;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.util.Mth;

/**
 * Виньетка при низком HP - ТОЛЬКО текстура!
 */
public class LowHealthEffects {

    private static final ResourceLocation VIGNETTE_TEXTURE =
            new ResourceLocation("exodus-health", "textures/gui/vignette.png");

    /**
     * Отрисовать виньетку
     */
    public static void renderVignette(GuiGraphics graphics, int screenWidth, int screenHeight, LocalPlayer player) {
        if (player == null) {
            return;
        }

        float currentHP = ExodusCoreAPI.getCurrentHP(player);
        float maxHP = ExodusCoreAPI.getMaxHP(player);
        float healthPercentage = currentHP / maxHP;

        // Виньетка появляется когда HP < 50%
        if (healthPercentage >= 0.5f) {
            return;
        }

        // Интенсивность (0.0 - 1.0)
        float intensity = 1.0f - (healthPercentage / 0.5f);
        intensity = Mth.clamp(intensity, 0.0f, 1.0f);

        // Пульсация
        float pulse = (float) Math.sin(System.currentTimeMillis() / 500.0) * 0.15f + 0.85f;
        intensity *= pulse;

        RenderSystem.enableBlend();
        RenderSystem.defaultBlendFunc();

        // Рисуем твою текстуру
        RenderSystem.setShaderColor(1.0f, 1.0f, 1.0f, intensity);
        graphics.blit(VIGNETTE_TEXTURE, 0, 0, 0, 0, screenWidth, screenHeight, screenWidth, screenHeight);
        RenderSystem.setShaderColor(1.0f, 1.0f, 1.0f, 1.0f);

        RenderSystem.disableBlend();
    }
}