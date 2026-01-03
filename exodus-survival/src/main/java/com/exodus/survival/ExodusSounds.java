package com.exodus.survival;

import net.minecraft.core.Registry;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.sounds.SoundEvent;

/**
 * Регистрация кастомных звуков для Exodus
 */
public class ExodusSounds {

    // ❄️ Звуки холода (гипотермия)
    public static final SoundEvent SHIVER = registerSound("ambient.shiver");

    // 🔥 Звуки жары (гипертермия)
    public static final SoundEvent BREATH = registerSound("ambient.breath");

    // 💀 Звуки травм
    public static final SoundEvent FRACTURE_BONES = registerSound("ambient.fracture_bones");

    /**
     * Зарегистрировать звук
     */
    private static SoundEvent registerSound(String name) {
        ResourceLocation id = new ResourceLocation("exodus-survival", name);
        return Registry.register(BuiltInRegistries.SOUND_EVENT, id, SoundEvent.createVariableRangeEvent(id));
    }

    /**
     * Инициализация (вызывается в ExodusSurvival.onInitialize())
     */
    public static void register() {
        ExodusSurvival.LOGGER.info("Registering Exodus Sounds");
    }
}
