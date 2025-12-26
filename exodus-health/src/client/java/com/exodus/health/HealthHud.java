package com.exodus.health;

import com.exodus.core.ExodusCoreAPI;
import com.exodus.core.api.player.PlayerHealthData;
import com.exodus.core.api.player.StatusEffect;
import net.fabricmc.fabric.api.client.rendering.v1.HudRenderCallback;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.client.player.LocalPlayer;

/**
 * Простой HUD здоровья (только полоска HP и эффекты)
 */
public class HealthHud {

    private static final int HUD_X = 10;
    private static final int HUD_Y = 10;

    private static final int HP_BAR_WIDTH = 150;
    private static final int HP_BAR_HEIGHT = 20;

    private static final int EFFECT_ICON_SIZE = 16;
    private static final int EFFECT_SPACING = 20;

    /**
     * Регистрировать HUD
     */
    public static void register() {
        System.out.println("=== REGISTERING HEALTH HUD ===");
        HudRenderCallback.EVENT.register(HealthHud::render);
    }

    /**
     * Отрисовка HUD (БЕЗ виньетки и крови)
     */
    private static void render(GuiGraphics graphics, float tickDelta) {
        Minecraft mc = Minecraft.getInstance();
        LocalPlayer player = mc.player;

        if (player == null || mc.options.hideGui) {
            return;
        }

        // Рисуем только HUD здоровья
        renderHealthBar(graphics, player, mc);
        renderStatusEffects(graphics, player, mc);
    }

    /**
     * Отрисовать полоску HP
     */
    private static void renderHealthBar(GuiGraphics graphics, LocalPlayer player, Minecraft mc) {
        PlayerHealthData data = ExodusCoreAPI.getHealthData(player);

        float currentHP = data.getCurrentHP();
        float maxHP = data.getMaxHP();
        float percentage = data.getHPPercentage();

        // Фон (чёрный с прозрачностью)
        graphics.fill(HUD_X, HUD_Y, HUD_X + HP_BAR_WIDTH, HUD_Y + HP_BAR_HEIGHT, 0x88000000);

        // Рамка (белая)
        graphics.fill(HUD_X - 1, HUD_Y - 1, HUD_X + HP_BAR_WIDTH + 1, HUD_Y, 0xFFFFFFFF);
        graphics.fill(HUD_X - 1, HUD_Y + HP_BAR_HEIGHT, HUD_X + HP_BAR_WIDTH + 1, HUD_Y + HP_BAR_HEIGHT + 1, 0xFFFFFFFF);
        graphics.fill(HUD_X - 1, HUD_Y, HUD_X, HUD_Y + HP_BAR_HEIGHT, 0xFFFFFFFF);
        graphics.fill(HUD_X + HP_BAR_WIDTH, HUD_Y, HUD_X + HP_BAR_WIDTH + 1, HUD_Y + HP_BAR_HEIGHT, 0xFFFFFFFF);

        // Полоска HP (цвет зависит от процента)
        int hpBarWidth = (int) (HP_BAR_WIDTH * percentage);
        int hpColor = getHealthColor(percentage);
        graphics.fill(HUD_X, HUD_Y, HUD_X + hpBarWidth, HUD_Y + HP_BAR_HEIGHT, hpColor);

        // Текст HP (центр полоски)
        String hpText = String.format("%.0f / %.0f", currentHP, maxHP);
        int textX = HUD_X + HP_BAR_WIDTH / 2 - mc.font.width(hpText) / 2;
        int textY = HUD_Y + HP_BAR_HEIGHT / 2 - 4;

        // Тень текста
        graphics.drawString(mc.font, hpText, textX + 1, textY + 1, 0xFF000000, false);
        // Текст
        graphics.drawString(mc.font, hpText, textX, textY, 0xFFFFFFFF, false);
    }

    /**
     * Получить цвет полоски HP в зависимости от процента
     */
    private static int getHealthColor(float percentage) {
        if (percentage > 0.6f) {
            return 0xFF00FF00; // Зелёный
        } else if (percentage > 0.3f) {
            return 0xFFFFFF00; // Жёлтый
        } else {
            return 0xFFFF0000; // Красный
        }
    }

    /**
     * Отрисовать иконки статусных эффектов
     */
    private static void renderStatusEffects(GuiGraphics graphics, LocalPlayer player, Minecraft mc) {
        PlayerHealthData data = ExodusCoreAPI.getHealthData(player);

        int x = HUD_X;
        int y = HUD_Y + HP_BAR_HEIGHT + 5;

        // Отрисовываем каждый активный эффект
        for (StatusEffect effect : StatusEffect.values()) {
            if (data.hasEffect(effect)) {
                renderEffectIcon(graphics, effect, x, y, mc);
                x += EFFECT_SPACING;
            }
        }
    }

    /**
     * Отрисовать иконку одного эффекта
     */
    private static void renderEffectIcon(GuiGraphics graphics, StatusEffect effect, int x, int y, Minecraft mc) {
        // Фон иконки (цвет эффекта)
        int color = effect.getColor() | 0xAA000000;
        graphics.fill(x, y, x + EFFECT_ICON_SIZE, y + EFFECT_ICON_SIZE, color);

        // Рамка (белая)
        graphics.fill(x - 1, y - 1, x + EFFECT_ICON_SIZE + 1, y, 0xFFFFFFFF);
        graphics.fill(x - 1, y + EFFECT_ICON_SIZE, x + EFFECT_ICON_SIZE + 1, y + EFFECT_ICON_SIZE + 1, 0xFFFFFFFF);
        graphics.fill(x - 1, y, x, y + EFFECT_ICON_SIZE, 0xFFFFFFFF);
        graphics.fill(x + EFFECT_ICON_SIZE, y, x + EFFECT_ICON_SIZE + 1, y + EFFECT_ICON_SIZE, 0xFFFFFFFF);

        // Первая буква эффекта
        String letter = effect.getDisplayName().substring(0, 1);
        int textX = x + EFFECT_ICON_SIZE / 2 - 3;
        int textY = y + EFFECT_ICON_SIZE / 2 - 4;

        graphics.drawString(mc.font, letter, textX, textY, 0xFFFFFFFF, true);
    }
}