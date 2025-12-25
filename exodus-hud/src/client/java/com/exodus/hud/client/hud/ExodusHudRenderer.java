package com.exodus.hud.client.hud;

import com.exodus.core.ExodusCoreAPI;
import com.exodus.core.api.stats.IPlayerStat;
import com.exodus.core.api.stats.IStatsProvider;
import com.mojang.blaze3d.systems.RenderSystem;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.entity.player.Inventory;
import net.minecraft.world.item.ItemStack;

import java.util.List;

public class ExodusHudRenderer {

    private final Minecraft minecraft;
    private static final ResourceLocation CROSSHAIR_TEXTURE = new ResourceLocation("exodus-hud", "textures/gui/crosshair.png");
    private final BodyPartRenderer bodyPartRenderer; // ← ДОБАВЬ ЭТО

    public ExodusHudRenderer(Minecraft minecraft) {
        this.minecraft = minecraft;
        this.bodyPartRenderer = new BodyPartRenderer(minecraft); // ← ДОБАВЬ ЭТО
    }

    public void render(GuiGraphics guiGraphics, float tickDelta) {
        if (minecraft.options.hideGui) {
            return;
        }

        int screenWidth = guiGraphics.guiWidth();
        int screenHeight = guiGraphics.guiHeight();

        renderCrosshair(guiGraphics, screenWidth, screenHeight);
        renderHotbar(guiGraphics, screenWidth, screenHeight, tickDelta);

        // ПОКАЗЫВАЕМ ЧАСТИ ТЕЛА (левый верхний угол)
        renderBodyParts(guiGraphics, screenWidth, screenHeight);

        // ПОКАЗЫВАЕМ КИСЛОРОД/ГОЛОД/ЖАЖДУ (правый верхний угол)
        renderPlayerStats(guiGraphics, screenWidth, screenHeight);
    }

    /**
     * Отрисовка частей тела персонажа
     */
    private void renderBodyParts(GuiGraphics guiGraphics, int screenWidth, int screenHeight) {
        if (minecraft.player == null) {
            return;
        }

        bodyPartRenderer.render(guiGraphics, minecraft.player, screenWidth, screenHeight);
    }

    private void renderCrosshair(GuiGraphics guiGraphics, int screenWidth, int screenHeight) {
        if (!minecraft.options.getCameraType().isFirstPerson()) {
            return;
        }

        RenderSystem.enableBlend();

        int centerX = screenWidth / 2;
        int centerY = screenHeight / 2;
        int crosshairSize = 8;
        int thickness = 2;
        int gap = 2;
        int color = 0xFFFFFFFF;

        guiGraphics.fill(centerX - crosshairSize - gap, centerY - thickness/2, centerX - gap, centerY + thickness/2, color);
        guiGraphics.fill(centerX + gap, centerY - thickness/2, centerX + crosshairSize + gap, centerY + thickness/2, color);
        guiGraphics.fill(centerX - thickness/2, centerY - crosshairSize - gap, centerX + thickness/2, centerY - gap, color);
        guiGraphics.fill(centerX - thickness/2, centerY + gap, centerX + thickness/2, centerY + crosshairSize + gap, color);

        RenderSystem.disableBlend();
    }

    private void renderHotbar(GuiGraphics guiGraphics, int screenWidth, int screenHeight, float partialTick) {
        if (minecraft.player == null) {
            return;
        }

        RenderSystem.enableBlend();

        Inventory inventory = minecraft.player.getInventory();

        int baseSlotSize = 24;
        int mainHandSize = (int)(baseSlotSize * 1.7);
        int slotPadding = 4;
        int margin = 10;

        int startX = margin;
        int startY = screenHeight - margin;

        int mainHandX = startX;
        int mainHandY = startY - mainHandSize - baseSlotSize - slotPadding;

        guiGraphics.fill(mainHandX, mainHandY, mainHandX + mainHandSize, mainHandY + mainHandSize, 0x80000000);
        drawSlotBorder(guiGraphics, mainHandX, mainHandY, mainHandSize, 0xFF00FFFF, 2);

        ItemStack mainHandItem = inventory.getSelected();
        if (!mainHandItem.isEmpty()) {
            int itemX = mainHandX + (mainHandSize - 16) / 2;
            int itemY = mainHandY + (mainHandSize - 16) / 2;
            guiGraphics.renderItem(mainHandItem, itemX, itemY);
            guiGraphics.renderItemDecorations(minecraft.font, mainHandItem, itemX, itemY);
        }

        int toolSlotY = startY - baseSlotSize;
        int toolSlotX = startX;

        guiGraphics.fill(toolSlotX, toolSlotY, toolSlotX + baseSlotSize, toolSlotY + baseSlotSize, 0x80555555);
        drawSlotBorder(guiGraphics, toolSlotX, toolSlotY, baseSlotSize, 0xFFFFFFFF, 1);

        int selectedSlot = inventory.selected + 1;
        String slotNumber = String.valueOf(selectedSlot);
        guiGraphics.drawString(minecraft.font, slotNumber, toolSlotX + 2, toolSlotY + 2, 0xFFFFFFFF, true);

        ItemStack toolItem = inventory.getSelected();
        if (!toolItem.isEmpty()) {
            guiGraphics.renderItem(toolItem, toolSlotX + 4, toolSlotY + 4);
            guiGraphics.renderItemDecorations(minecraft.font, toolItem, toolSlotX + 4, toolSlotY + 4);
        }

        int offhandX = toolSlotX + baseSlotSize + slotPadding;
        int offhandY = toolSlotY;

        guiGraphics.fill(offhandX, offhandY, offhandX + baseSlotSize, offhandY + baseSlotSize, 0x80000000);
        drawSlotBorder(guiGraphics, offhandX, offhandY, baseSlotSize, 0xFF00FF00, 1);

        ItemStack offhandItem = inventory.offhand.get(0);
        if (!offhandItem.isEmpty()) {
            guiGraphics.renderItem(offhandItem, offhandX + 4, offhandY + 4);
            guiGraphics.renderItemDecorations(minecraft.font, offhandItem, offhandX + 4, offhandY + 4);
        }

        RenderSystem.disableBlend();
    }

    private void drawSlotBorder(GuiGraphics guiGraphics, int x, int y, int size, int color, int thickness) {
        guiGraphics.fill(x, y, x + size, y + thickness, color);
        guiGraphics.fill(x, y + size - thickness, x + size, y + size, color);
        guiGraphics.fill(x, y, x + thickness, y + size, color);
        guiGraphics.fill(x + size - thickness, y, x + size, y + size, color);
    }

    /**
     * Отрисовка статов игрока через Core API
     */
    private void renderPlayerStats(GuiGraphics guiGraphics, int screenWidth, int screenHeight) {
        if (minecraft.player == null) {
            return;
        }

        RenderSystem.enableBlend();

        IStatsProvider statsProvider = ExodusCoreAPI.getStatsProvider();
        List<IPlayerStat> stats = statsProvider.getStats(minecraft.player);

        if (stats.isEmpty()) {
            RenderSystem.disableBlend();
            return;
        }

        // НОВАЯ ПОЗИЦИЯ: Правый верхний угол (чтобы не перекрывать части тела)
        int statsX = screenWidth - 120; // 120 пикселей от правого края
        int statsY = 10;
        int barWidth = 100;
        int barHeight = 8;
        int barSpacing = 12;

        int currentY = statsY;

        for (IPlayerStat stat : stats) {
            drawStatBar(
                    guiGraphics,
                    statsX,
                    currentY,
                    barWidth,
                    barHeight,
                    stat.getCurrent(),
                    stat.getMax(),
                    stat.getColor(),
                    darkenColor(stat.getColor()),
                    stat.getDisplayName()
            );
            currentY += barSpacing;
        }

        RenderSystem.disableBlend();
    }

    private void drawStatBar(GuiGraphics guiGraphics, int x, int y, int width, int height,
                             float current, float max, int fillColor, int bgColor, String label) {
        // Фон
        guiGraphics.fill(x, y, x + width, y + height, bgColor);

        // Заполнение
        int fillWidth = (int)((current / max) * width);
        guiGraphics.fill(x, y, x + fillWidth, y + height, fillColor);

        // Рамка
        guiGraphics.fill(x, y, x + width, y + 1, 0xFFFFFFFF);
        guiGraphics.fill(x, y + height - 1, x + width, y + height, 0xFFFFFFFF);
        guiGraphics.fill(x, y, x + 1, y + height, 0xFFFFFFFF);
        guiGraphics.fill(x + width - 1, y, x + width, y + height, 0xFFFFFFFF);

        // Текст (название и значение)
        String text = label + ": " + (int)current + "/" + (int)max;
        guiGraphics.drawString(minecraft.font, text, x + 2, y - 8, 0xFFFFFFFF, true);
    }

    private int darkenColor(int color) {
        int alpha = (color >> 24) & 0xFF;
        int red = ((color >> 16) & 0xFF) / 3;
        int green = ((color >> 8) & 0xFF) / 3;
        int blue = (color & 0xFF) / 3;

        return (alpha << 24) | (red << 16) | (green << 8) | blue;
    }
}