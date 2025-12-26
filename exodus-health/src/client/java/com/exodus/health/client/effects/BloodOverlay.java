package com.exodus.health.client.effects;

import com.mojang.blaze3d.systems.RenderSystem;
import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.util.Mth;

import java.util.ArrayList;
import java.util.List;
import java.util.Random;

/**
 * Программные кровавые следы (БЕЗ текстур)
 */
public class BloodOverlay {

    private static final List<BloodSplatter> splatters = new ArrayList<>();
    private static final Random RANDOM = new Random();

    /**
     * Добавить кровавый след
     */
    public static void addBloodSplatter(float damage) {
        System.out.println("=== ADDING BLOOD SPLATTER! Damage: " + damage + " ===");

        int count = Mth.clamp((int) (damage / 3.0f), 1, 3);

        for (int i = 0; i < count; i++) {
            splatters.add(new BloodSplatter(damage));
        }

        while (splatters.size() > 10) {
            splatters.remove(0);
        }
    }

    /**
     * Отрисовать все следы
     */
    public static void render(GuiGraphics graphics, int screenWidth, int screenHeight) {
        if (splatters.isEmpty()) {
            return;
        }

        RenderSystem.enableBlend();
        RenderSystem.defaultBlendFunc();

        splatters.removeIf(splatter -> splatter.render(graphics, screenWidth, screenHeight));

        RenderSystem.disableBlend();
    }

    /**
     * Один след
     */
    private static class BloodSplatter {
        private final float x;
        private final float y;
        private final float size;
        private final long startTime;
        private final long duration;
        private final int numDrops;

        public BloodSplatter(float damage) {
            if (RANDOM.nextFloat() < 0.5f) {
                this.x = 0.3f + RANDOM.nextFloat() * 0.4f;
                this.y = 0.3f + RANDOM.nextFloat() * 0.4f;
            } else {
                this.x = RANDOM.nextFloat();
                this.y = RANDOM.nextFloat();
            }

            this.size = 50.0f + (damage * 5.0f);
            this.numDrops = 3 + RANDOM.nextInt(6);
            this.startTime = System.currentTimeMillis();
            this.duration = 2000 + RANDOM.nextInt(2000);
        }

        public boolean render(GuiGraphics graphics, int screenWidth, int screenHeight) {
            long elapsed = System.currentTimeMillis() - startTime;

            if (elapsed > duration) {
                return true;
            }

            float progress = (float) elapsed / duration;
            float alpha;
            if (progress < 0.1f) {
                alpha = progress / 0.1f;
            } else {
                alpha = 1.0f - ((progress - 0.1f) / 0.9f);
            }
            alpha = Mth.clamp(alpha, 0.0f, 0.7f);

            int centerX = (int) (screenWidth * x);
            int centerY = (int) (screenHeight * y);

            for (int i = 0; i < numDrops; i++) {
                RANDOM.setSeed(startTime + i);

                int offsetX = (int) (RANDOM.nextFloat() * size - size / 2);
                int offsetY = (int) (RANDOM.nextFloat() * size - size / 2);
                int dropSize = (int) (size * (0.4f + RANDOM.nextFloat() * 0.6f));

                drawBloodDrop(graphics, centerX + offsetX, centerY + offsetY, dropSize, alpha);
            }

            return false;
        }

        private void drawBloodDrop(GuiGraphics graphics, int x, int y, int size, float alpha) {
            int r = 139;
            int g = 0;
            int b = 0;

            int alphaInt = (int) (alpha * 255);
            int color = (alphaInt << 24) | (r << 16) | (g << 8) | b;

            int radius = size / 2;

            for (int dy = -radius; dy <= radius; dy++) {
                int width = (int) Math.sqrt(radius * radius - dy * dy);
                graphics.fill(x - width, y + dy, x + width, y + dy + 1, color);
            }
        }
    }

    public static void clear() {
        splatters.clear();
    }
}