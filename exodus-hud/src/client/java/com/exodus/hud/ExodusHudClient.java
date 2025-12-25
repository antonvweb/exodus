package com.exodus.hud;

import com.exodus.hud.client.hud.ExodusHudRenderer;
import net.fabricmc.api.ClientModInitializer;
import net.fabricmc.fabric.api.client.rendering.v1.HudRenderCallback;
import net.minecraft.client.Minecraft;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class ExodusHudClient implements ClientModInitializer {
    public static final Logger LOGGER = LoggerFactory.getLogger(ExodusHUD.MOD_ID);

    private static ExodusHudRenderer hudRenderer;

    @Override
    public void onInitializeClient() {
        LOGGER.info("Exodus HUD - Client initialization");

        // Создаем рендерер HUD
        hudRenderer = new ExodusHudRenderer(Minecraft.getInstance());

        // Регистрируем callback для рендеринга HUD
        HudRenderCallback.EVENT.register((guiGraphics, deltaTracker) -> {
            hudRenderer.render(guiGraphics, deltaTracker);
        });

        LOGGER.info("Custom HUD registered successfully!");
    }
}