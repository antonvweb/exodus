package com.exodus.hud.client.hud;

import com.exodus.core.ExodusCoreAPI;
import com.exodus.core.api.player.BodyPart;
import com.exodus.core.api.player.ExodusPlayerData;
import com.mojang.blaze3d.systems.RenderSystem;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.entity.player.Player;

public class BodyPartRenderer {

    private final Minecraft minecraft;

    // ДОБАВИЛИ константу размера спрайта
    private static final int SPRITE_SIZE = 20;

    public BodyPartRenderer(Minecraft minecraft) {
        this.minecraft = minecraft;
    }

    public void render(GuiGraphics guiGraphics, Player player, int screenWidth, int screenHeight) {
        if (player == null) {
            return;
        }

        RenderSystem.enableBlend();
        RenderSystem.defaultBlendFunc();

        // Позиция панели (левый верхний угол)
        int panelX = 10;
        int panelY = 10;
        int panelWidth = 80;  // Увеличенная ширина для цифр
        int panelHeight = 100;

        // Полупрозрачный фон
        guiGraphics.fill(panelX, panelY, panelX + panelWidth, panelY + panelHeight, 0x80000000);

        // Рамка
        guiGraphics.fill(panelX, panelY, panelX + panelWidth, panelY + 1, 0xFFFFFFFF);
        guiGraphics.fill(panelX, panelY + panelHeight - 1, panelX + panelWidth, panelY + panelHeight, 0xFFFFFFFF);
        guiGraphics.fill(panelX, panelY, panelX + 1, panelY + panelHeight, 0xFFFFFFFF);
        guiGraphics.fill(panelX + panelWidth - 1, panelY, panelX + panelWidth, panelY + panelHeight, 0xFFFFFFFF);

        // Отступы внутри панели
        int offsetX = panelX + 5;
        int offsetY = panelY + 5;

        // === ГОЛОВА ===
        int headSize = 20;
        int headX = offsetX + (56 - headSize) / 2; // Центрируем
        int headY = offsetY;
        renderBodyPart(guiGraphics, player, BodyPart.HEAD, headX, headY, headSize);

        // Цифры для головы
        renderBodyPartHP(guiGraphics, player, BodyPart.HEAD, headX + headSize + 3, headY + 6);

        // === ТОРС ===
        int torsoWidth = 20;
        int torsoHeight = 28;
        int torsoX = offsetX + (56 - torsoWidth) / 2;
        int torsoY = headY + headSize + 2;
        renderBodyPart(guiGraphics, player, BodyPart.TORSO, torsoX, torsoY, torsoWidth);

        // Цифры для торса
        renderBodyPartHP(guiGraphics, player, BodyPart.TORSO, torsoX + torsoWidth + 3, torsoY + 10);

        // === РУКИ ===
        int armWidth = 10;

        // Левая рука
        int leftArmX = torsoX - armWidth - 2;
        renderBodyPart(guiGraphics, player, BodyPart.LEFT_ARM, leftArmX, torsoY, armWidth);

        // Правая рука
        int rightArmX = torsoX + torsoWidth + 2;
        renderBodyPart(guiGraphics, player, BodyPart.RIGHT_ARM, rightArmX, torsoY, armWidth);

        // === НОГИ ===
        int legWidth = 10;
        int legY = torsoY + torsoHeight + 4;

        // Левая нога
        renderBodyPart(guiGraphics, player, BodyPart.LEFT_LEG, torsoX, legY, legWidth);

        // Правая нога
        int rightLegX = torsoX + torsoWidth - legWidth;
        renderBodyPart(guiGraphics, player, BodyPart.RIGHT_LEG, rightLegX, legY, legWidth);

        // Цифры для конечностей (внизу панели)
        int textY = panelY + panelHeight - 12;
        renderLimbsHP(guiGraphics, player, panelX + 5, textY);

        RenderSystem.disableBlend();
    }

    /**
     * Отрисовать HP части тела (текст)
     */
    private void renderBodyPartHP(GuiGraphics guiGraphics, Player player, BodyPart part, int x, int y) {
        ExodusPlayerData.BodyPartData partData = ExodusCoreAPI.getBodyPart(player, part);

        int current = (int) partData.currentHP;
        int max = (int) partData.maxHP;

        // Формируем текст
        String text = current + "/" + max;

        // Цвет в зависимости от состояния
        int color = getColorForHP(partData.getPercentage());

        // Рисуем текст с тенью
        guiGraphics.drawString(minecraft.font, text, x, y, color, true);
    }

    /**
     * Отрисовать HP конечностей (компактно)
     */
    private void renderLimbsHP(GuiGraphics guiGraphics, Player player, int x, int y) {
        // Левая рука
        ExodusPlayerData.BodyPartData leftArm = ExodusCoreAPI.getBodyPart(player, BodyPart.LEFT_ARM);
        String leftArmText = "LA:" + (int)leftArm.currentHP;
        int leftArmColor = getColorForHP(leftArm.getPercentage());
        guiGraphics.drawString(minecraft.font, leftArmText, x, y, leftArmColor, true);

        // Правая рука
        ExodusPlayerData.BodyPartData rightArm = ExodusCoreAPI.getBodyPart(player, BodyPart.RIGHT_ARM);
        String rightArmText = "RA:" + (int)rightArm.currentHP;
        int rightArmColor = getColorForHP(rightArm.getPercentage());
        guiGraphics.drawString(minecraft.font, rightArmText, x + 50, y, rightArmColor, true);

        // Левая нога
        ExodusPlayerData.BodyPartData leftLeg = ExodusCoreAPI.getBodyPart(player, BodyPart.LEFT_LEG);
        String leftLegText = "LL:" + (int)leftLeg.currentHP;
        int leftLegColor = getColorForHP(leftLeg.getPercentage());
        guiGraphics.drawString(minecraft.font, leftLegText, x, y+ 25, leftLegColor, true);

        // Правая нога
        ExodusPlayerData.BodyPartData rightLeg = ExodusCoreAPI.getBodyPart(player, BodyPart.RIGHT_LEG);
        String rightLegText = "RL:" + (int)rightLeg.currentHP;
        int rightLegColor = getColorForHP(rightLeg.getPercentage());
        guiGraphics.drawString(minecraft.font, rightLegText, x + 50, y + 25, rightLegColor, true);
    }

    /**
     * Получить цвет текста в зависимости от HP
     */
    private int getColorForHP(float percentage) {
        if (percentage > 0.75f) {
            return 0x00FF00; // Зелёный (здоров)
        } else if (percentage > 0.5f) {
            return 0xFFFF00; // Жёлтый (лёгкое повреждение)
        } else if (percentage > 0.25f) {
            return 0xFFA500; // Оранжевый (среднее повреждение)
        } else if (percentage > 0) {
            return 0xFF0000; // Красный (критическое)
        } else {
            return 0x808080; // Серый (мёртво)
        }
    }

    /**
     * Отрисовать часть тела с текстурой
     */
    private void renderBodyPart(GuiGraphics guiGraphics, Player player, BodyPart part, int x, int y, int displayWidth) {
        ExodusPlayerData.BodyPartData partData = ExodusCoreAPI.getBodyPart(player, part);
        BodyPartState state = getBodyPartState(partData);
        ResourceLocation texture = getTextureForBodyPart(part, state);

        // Загрузка текстуры
        minecraft.getTextureManager().bindForSetup(texture);
        RenderSystem.setShaderTexture(0, texture);
        RenderSystem.enableBlend();
        RenderSystem.defaultBlendFunc();

        // Получаем размер текстуры
        TextureSize size = getTextureSizeForPart(part);
        int displayHeight = (int)(displayWidth * ((float)size.height / size.width));

        // Рисуем текстуру
        guiGraphics.blit(
                texture,
                x, y,
                0, 0,
                displayWidth, displayHeight,
                size.width, size.height
        );

        RenderSystem.disableBlend();

        // Эффект кровотечения
        if (partData.isBleeding) {
            renderBleedingEffect(guiGraphics, x, y, displayWidth, displayHeight);
        }
    }

    /**
     * Размер текстуры для части тела
     */
    private TextureSize getTextureSizeForPart(BodyPart part) {
        return switch (part) {
            case HEAD -> new TextureSize(32, 32);
            case TORSO -> new TextureSize(32, 48);
            case LEFT_ARM, RIGHT_ARM, LEFT_LEG, RIGHT_LEG -> new TextureSize(16, 48);
        };
    }

    private record TextureSize(int width, int height) {}

    /**
     * Определить состояние части тела
     */
    private BodyPartState getBodyPartState(ExodusPlayerData.BodyPartData partData) {
        if (partData.currentHP <= 0) {
            return BodyPartState.DESTROYED;
        }

        float percentage = partData.currentHP / partData.maxHP;

        if (percentage <= 0.25f) {
            return BodyPartState.CRITICAL;
        } else if (percentage <= 0.50f) {
            return BodyPartState.INJURED;
        } else {
            return BodyPartState.HEALTHY;
        }
    }

    /**
     * Получить текстуру для части тела
     */
    private ResourceLocation getTextureForBodyPart(BodyPart part, BodyPartState state) {
        String partName = getPartTextureName(part);
        String stateName = state.name().toLowerCase();
        return new ResourceLocation("exodus-hud", "textures/gui/body/" + partName + "_" + stateName + ".png");
    }

    /**
     * Получить имя текстуры части тела
     */
    private String getPartTextureName(BodyPart part) {
        return switch (part) {
            case HEAD -> "head";
            case TORSO -> "torso";
            case LEFT_ARM -> "left_arm";
            case RIGHT_ARM -> "right_arm";
            case LEFT_LEG -> "left_leg";
            case RIGHT_LEG -> "right_leg";
        };
    }

    /**
     * Эффект кровотечения (пульсирующая красная подсветка)
     */
    private void renderBleedingEffect(GuiGraphics guiGraphics, int x, int y, int width, int height) {
        long time = System.currentTimeMillis();
        float alpha = (float) (Math.sin(time / 200.0) * 0.3 + 0.5);
        int color = (int) (alpha * 255) << 24 | 0xFF0000;
        guiGraphics.fill(x, y, x + width, y + height, color);
    }

    /**
     * Состояния частей тела
     */
    private enum BodyPartState {
        HEALTHY, INJURED, CRITICAL, DESTROYED
    }
}