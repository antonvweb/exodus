package com.exodus.survival.client;

import net.fabricmc.fabric.api.client.keybinding.v1.KeyBindingHelper;
import net.minecraft.client.KeyMapping;
import org.lwjgl.glfw.GLFW;

/**
 * Регистрация клавиш мода
 */
public class ExodusKeyBindings {
    public static KeyMapping OPEN_INVENTORY;
    public static KeyMapping PICK_ITEM;

    /**
     * Инициализация - вызывается ОДИН раз при загрузке клиента
     */
    public static void register() {
        // Создаем KeyMapping
        OPEN_INVENTORY = KeyBindingHelper.registerKeyBinding(
                new KeyMapping(
                        "key.exodus.open_inventory",        // ID перевода (для localization)
                        GLFW.GLFW_KEY_I,                    // Клавиша (I)
                        "category.exodus.inventory"          // Категория в настройках
                )
        );

        PICK_ITEM = KeyBindingHelper.registerKeyBinding(
                new KeyMapping(
                        "key.exodus.pick_item",        // ID перевода (для localization)
                        GLFW.GLFW_KEY_G,                    // Клавиша (I)
                        "category.exodus.inventory"          // Категория в настройках
                )
        );
    }
}