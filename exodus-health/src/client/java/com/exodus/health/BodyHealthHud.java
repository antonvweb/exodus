package com.exodus.health;

import com.exodus.core.ExodusCoreAPI;
import com.exodus.core.api.player.BodyPart;
import com.exodus.core.api.player.PlayerHealthData;
import com.mojang.blaze3d.systems.RenderSystem;
import net.fabricmc.fabric.api.client.rendering.v1.HudRenderCallback;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.client.player.LocalPlayer;
import net.minecraft.resources.ResourceLocation;

/**
 * HUD здоровья с человечком и эффектами на частях тела
 */
public class BodyHealthHud {

    // Позиция HUD (слева сверху)
    private static final int HUD_X = 25; // ✅ Сдвинули вправо (было 10)
    private static final int HUD_Y = 10;

    // Размеры частей тела (ОРИГИНАЛЬНЫЕ размеры спрайтов)
    private static final int HEAD_WIDTH = 32;
    private static final int HEAD_HEIGHT = 32;

    private static final int TORSO_WIDTH = 32;
    private static final int TORSO_HEIGHT = 48;

    private static final int LIMB_WIDTH = 16;
    private static final int LIMB_HEIGHT = 48;

    // Размеры иконок эффектов
    private static final int EFFECT_SIZE = 10; // ✅ Уменьшили (было 12)

    // Текстуры частей тела
    private static final String TEXTURE_PATH = "exodus-health:textures/gui/body/";

    /**
     * Регистрировать HUD
     */
    public static void register() {
        System.out.println("=== REGISTERING BODY HEALTH HUD ===");
        HudRenderCallback.EVENT.register(BodyHealthHud::render);
    }

    /**
     * Отрисовка HUD
     */
    private static void render(GuiGraphics graphics, float tickDelta) {
        Minecraft mc = Minecraft.getInstance();
        LocalPlayer player = mc.player;

        if (player == null || mc.options.hideGui) {
            return;
        }

        PlayerHealthData data = ExodusCoreAPI.getHealthData(player);

        // Рисуем человечка
        renderBody(graphics, data, mc);

        // Рисуем эффекты на частях тела
        renderBodyPartEffects(graphics, data, mc);

        // Рисуем боль (глобально)
        renderPain(graphics, data, mc);
    }

    /**
     * Отрисовать тело (6 частей)
     */
    private static void renderBody(GuiGraphics graphics, PlayerHealthData data, Minecraft mc) {

        // Рассчитываем позиции для симметричного расположения
        int centerX = HUD_X + TORSO_WIDTH / 2;

        // === ГОЛОВА ===
        int headX = centerX - HEAD_WIDTH / 2;
        int headY = HUD_Y;
        renderBodyPart(graphics, BodyPart.HEAD, headX, headY, data);

        // === ТОРС ===
        int torsoX = centerX - TORSO_WIDTH / 2;
        int torsoY = headY + HEAD_HEIGHT;
        renderBodyPart(graphics, BodyPart.TORSO, torsoX, torsoY, data);

        // === РУКИ ===
        // На уровне плеч

        // Левая рука
        int leftArmX = torsoX - LIMB_WIDTH;
        renderBodyPart(graphics, BodyPart.LEFT_ARM, leftArmX, torsoY, data);

        // Правая рука
        int rightArmX = torsoX + TORSO_WIDTH;
        renderBodyPart(graphics, BodyPart.RIGHT_ARM, rightArmX, torsoY, data);

        // === НОГИ ===
        int legsY = torsoY + TORSO_HEIGHT;

        // Левая нога
        int leftLegX = centerX - LIMB_WIDTH;
        renderBodyPart(graphics, BodyPart.LEFT_LEG, leftLegX, legsY, data);

        // Правая нога
        renderBodyPart(graphics, BodyPart.RIGHT_LEG, centerX, legsY, data);
    }

    /**
     * Отрисовать одну часть тела
     */
    private static void renderBodyPart(GuiGraphics graphics, BodyPart part, int x, int y, PlayerHealthData data) {
        // Получаем состояние части тела
        BodyPart.BodyPartState state = data.getBodyPartState(part);

        // Формируем путь к текстуре
        String textureName = part.getId() + "_" + state.getTextureSuffix() + ".png";
        ResourceLocation texture = new ResourceLocation(TEXTURE_PATH + textureName);

        // Определяем размеры
        int width, height;
        if (part == BodyPart.HEAD) {
            width = HEAD_WIDTH;
            height = HEAD_HEIGHT;
        } else if (part == BodyPart.TORSO) {
            width = TORSO_WIDTH;
            height = TORSO_HEIGHT;
        } else {
            width = LIMB_WIDTH;
            height = LIMB_HEIGHT;
        }

        // Рендерим текстуру
        RenderSystem.setShaderTexture(0, texture);
        RenderSystem.enableBlend();
        graphics.blit(texture, x, y, 0, 0, width, height, width, height);
        RenderSystem.disableBlend();
    }

    /**
     * ✅ Отрисовать эффекты на частях тела
     * Позиции ТОЧНО совпадают с renderBody
     */
    private static void renderBodyPartEffects(GuiGraphics graphics, PlayerHealthData data, Minecraft mc) {
        int x = HUD_X;
        int y = HUD_Y;

        int centerX = x + TORSO_WIDTH / 2;

        // ✅ ТОЧНО ТЕ ЖЕ позиции что и в renderBody
        int headX = centerX - HEAD_WIDTH / 2;
        int headY = y;

        int torsoX = centerX - TORSO_WIDTH / 2;
        int torsoY = headY + HEAD_HEIGHT;

        int leftArmX = torsoX - LIMB_WIDTH;
        int rightArmX = torsoX + TORSO_WIDTH;
        int armsY = torsoY;

        int leftLegX = centerX - LIMB_WIDTH;
        int rightLegX = centerX;
        int legsY = torsoY + TORSO_HEIGHT;

        // Рендерим эффекты для каждой части
        renderEffectsOnBodyPart(graphics, BodyPart.HEAD, headX, headY, HEAD_WIDTH, HEAD_HEIGHT, data, mc);
        renderEffectsOnBodyPart(graphics, BodyPart.TORSO, torsoX, torsoY, TORSO_WIDTH, TORSO_HEIGHT, data, mc);
        renderEffectsOnBodyPart(graphics, BodyPart.LEFT_ARM, leftArmX, armsY, LIMB_WIDTH, LIMB_HEIGHT, data, mc);
        renderEffectsOnBodyPart(graphics, BodyPart.RIGHT_ARM, rightArmX, armsY, LIMB_WIDTH, LIMB_HEIGHT, data, mc);
        renderEffectsOnBodyPart(graphics, BodyPart.LEFT_LEG, leftLegX, legsY, LIMB_WIDTH, LIMB_HEIGHT, data, mc);
        renderEffectsOnBodyPart(graphics, BodyPart.RIGHT_LEG, rightLegX, legsY, LIMB_WIDTH, LIMB_HEIGHT, data, mc);
    }

    /**
     * Отрисовать эффекты на одной части тела
     */
    private static void renderEffectsOnBodyPart(GuiGraphics graphics, BodyPart part, int x, int y,
                                                int width, int height, PlayerHealthData data, Minecraft mc) {

        // ✅ Центрируем эффекты ТОЧНО по середине части тела
        int effectX = x + (width / 2) - (EFFECT_SIZE / 2);
        int effectY = y + (height / 2) - (EFFECT_SIZE / 2);

        int offset = 0;

        // Кровотечение
        if (data.hasBleeding(part)) {
            renderEffectIcon(graphics, 0xFF0000, effectX + offset, effectY, "К", mc);
            offset += EFFECT_SIZE + 2;
        }

        // Перелом
        if (data.hasFracture(part)) {
            renderEffectIcon(graphics, 0xFFFFFF, effectX + offset, effectY, "П", mc);
            offset += EFFECT_SIZE + 2;
        }
    }

    /**
     * Отрисовать иконку эффекта
     */
    private static void renderEffectIcon(GuiGraphics graphics, int color, int x, int y, String letter, Minecraft mc) {
        // Фон иконки
        int bgColor = (color & 0x00FFFFFF) | 0xAA000000;
        graphics.fill(x, y, x + EFFECT_SIZE, y + EFFECT_SIZE, bgColor);

        // Рамка
        graphics.fill(x - 1, y - 1, x + EFFECT_SIZE + 1, y, 0xFFFFFFFF);
        graphics.fill(x - 1, y + EFFECT_SIZE, x + EFFECT_SIZE + 1, y + EFFECT_SIZE + 1, 0xFFFFFFFF);
        graphics.fill(x - 1, y, x, y + EFFECT_SIZE, 0xFFFFFFFF);
        graphics.fill(x + EFFECT_SIZE, y, x + EFFECT_SIZE + 1, y + EFFECT_SIZE, 0xFFFFFFFF);

        // Буква
        int textX = x + EFFECT_SIZE / 2 - 3;
        int textY = y + EFFECT_SIZE / 2 - 4;
        graphics.drawString(mc.font, letter, textX, textY, 0xFFFFFFFF, true);
    }

    /**
     * Отрисовать боль (глобально, отдельно от тела)
     */
    private static void renderPain(GuiGraphics graphics, PlayerHealthData data, Minecraft mc) {
        if (!data.hasPain()) {
            return;
        }

        // Позиция справа от тела
        int painX = HUD_X + TORSO_WIDTH + LIMB_WIDTH + 20;
        int painY = HUD_Y + 40;

        // Иконка боли
        renderEffectIcon(graphics, 0xFF6600, painX, painY, "Б", mc);

        // Текст интенсивности
        int intensity = (int)(data.getPainIntensity() * 100);
        String text = "Боль: " + intensity + "%";
        graphics.drawString(mc.font, text, painX + EFFECT_SIZE + 5, painY + 2, 0xFFFFFFFF, true);
    }
}