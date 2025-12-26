package com.exodus.health.client;

import com.exodus.core.ExodusCoreAPI;
import com.exodus.core.api.player.PlayerHealthData;
import com.exodus.core.api.player.StatusEffect;
import com.exodus.health.client.effects.BloodOverlay;
import com.exodus.health.client.effects.LowHealthEffects;
import net.fabricmc.fabric.api.client.rendering.v1.HudRenderCallback;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.client.player.LocalPlayer;

/**
 * Простой HUD здоровья (нарисованный, без спрайтов)
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
     * Отрисовка HUD + визуальные эффекты
     */
    private static void render(GuiGraphics graphics, float tickDelta) {
        Minecraft mc = Minecraft.getInstance();
        LocalPlayer player = mc.player;

        if (player == null || mc.options.hideGui) {
            return;
        }

        int screenWidth = mc.getWindow().getGuiScaledWidth();
        int screenHeight = mc.getWindow().getGuiScaledHeight();

        // 1. ВИНЬЕТКА ПРИ НИЗКОМ HP
        LowHealthEffects.renderVignette(graphics, screenWidth, screenHeight, player);

        // 2. КРОВАВЫЕ СЛЕДЫ
        BloodOverlay.render(graphics, screenWidth, screenHeight);

        // 3. HUD ЗДОРОВЬЯ
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
        graphics.fill(HUD_X - 1, HUD_Y - 1, HUD_X + HP_BAR_WIDTH + 1, HUD_Y, 0xFFFFFFFF); // Верх
        graphics.fill(HUD_X - 1, HUD_Y + HP_BAR_HEIGHT, HUD_X + HP_BAR_WIDTH + 1, HUD_Y + HP_BAR_HEIGHT + 1, 0xFFFFFFFF); // Низ
        graphics.fill(HUD_X - 1, HUD_Y, HUD_X, HUD_Y + HP_BAR_HEIGHT, 0xFFFFFFFF); // Лево
        graphics.fill(HUD_X + HP_BAR_WIDTH, HUD_Y, HUD_X + HP_BAR_WIDTH + 1, HUD_Y + HP_BAR_HEIGHT, 0xFFFFFFFF); // Право

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
            // Зелёный (100% - 60%)
            return 0xFF00FF00;
        } else if (percentage > 0.3f) {
            // Жёлтый (60% - 30%)
            return 0xFFFFFF00;
        } else {
            // Красный (30% - 0%)
            return 0xFFFF0000;
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
        int color = effect.getColor() | 0xAA000000; // Добавляем прозрачность
        graphics.fill(x, y, x + EFFECT_ICON_SIZE, y + EFFECT_ICON_SIZE, color);

        // Рамка (белая)
        graphics.fill(x - 1, y - 1, x + EFFECT_ICON_SIZE + 1, y, 0xFFFFFFFF); // Верх
        graphics.fill(x - 1, y + EFFECT_ICON_SIZE, x + EFFECT_ICON_SIZE + 1, y + EFFECT_ICON_SIZE + 1, 0xFFFFFFFF); // Низ
        graphics.fill(x - 1, y, x, y + EFFECT_ICON_SIZE, 0xFFFFFFFF); // Лево
        graphics.fill(x + EFFECT_ICON_SIZE, y, x + EFFECT_ICON_SIZE + 1, y + EFFECT_ICON_SIZE, 0xFFFFFFFF); // Право

        // Первая буква эффекта
        String letter = effect.getDisplayName().substring(0, 1);
        int textX = x + EFFECT_ICON_SIZE / 2 - 3;
        int textY = y + EFFECT_ICON_SIZE / 2 - 4;

        graphics.drawString(mc.font, letter, textX, textY, 0xFFFFFFFF, true);
    }
}