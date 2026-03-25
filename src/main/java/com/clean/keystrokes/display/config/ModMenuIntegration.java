package com.clean.keystrokes.display.config;

import com.terraformersmc.modmenu.api.ConfigScreenFactory;
import com.terraformersmc.modmenu.api.ModMenuApi;
import dev.isxander.yacl3.api.*;
import dev.isxander.yacl3.api.controller.*;
import net.minecraft.client.gui.screens.Screen;
import net.minecraft.network.chat.Component;

public class ModMenuIntegration implements ModMenuApi {

    @Override
    public ConfigScreenFactory<?> getModConfigScreenFactory() {
        return this::buildScreen;
    }
    private static java.awt.Color intToColor(int argb) {
        return new java.awt.Color((argb >> 16) & 0xFF, (argb >> 8) & 0xFF, argb & 0xFF, (argb >> 24) & 0xFF);
    }

    private static int colorToInt(java.awt.Color c) {
        return (c.getAlpha() << 24) | (c.getRed() << 16) | (c.getGreen() << 8) | c.getBlue();
    }

    private Screen buildScreen(Screen parent) {
        KeystrokeConfig cfg = KeystrokeConfig.get();

        return YetAnotherConfigLib.createBuilder()
                .title(Component.literal("Clean Keystroke Settings"))
                .category(ConfigCategory.createBuilder()
                        .name(Component.literal("General"))
                        .option(Option.<KeystrokeConfig.CornerPosition>createBuilder()
                                .name(Component.literal("Position"))
                                .binding(KeystrokeConfig.CornerPosition.TOP_RIGHT,
                                        () -> cfg.position,
                                        v -> cfg.position = v)
                                .controller(opt -> EnumControllerBuilder.create(opt)
                                        .enumClass(KeystrokeConfig.CornerPosition.class))
                                .build())
                        .option(Option.<Boolean>createBuilder()
                                .name(Component.literal("Text Shadow"))
                                .binding(false, () -> cfg.textShadow, v -> cfg.textShadow = v)
                                .controller(TickBoxControllerBuilder::create)
                                .build())
                        .option(Option.<Boolean>createBuilder()
                                .name(Component.literal("Press Animation"))
                                .binding(true, () -> cfg.pressAnimation, v -> cfg.pressAnimation = v)
                                .controller(TickBoxControllerBuilder::create)
                                .build())
                        .option(Option.<Boolean>createBuilder()
                                .name(Component.literal("Rainbow Text"))
                                .binding(false, () -> cfg.rainbowText, v -> cfg.rainbowText = v)
                                .controller(TickBoxControllerBuilder::create)
                                .build())
                        .build())
                .category(ConfigCategory.createBuilder()
                        .name(Component.literal("Colors"))
                        .option(Option.<java.awt.Color>createBuilder()
                                .name(Component.literal("Key Text Color"))
                                .binding(intToColor(0xFFFFFFFF), () -> intToColor(cfg.keyColor), v -> cfg.keyColor = colorToInt(v))
                                .controller(opt -> ColorControllerBuilder.create(opt).allowAlpha(true))
                                .build())
                        .option(Option.<java.awt.Color>createBuilder()
                                .name(Component.literal("Key Background Color"))
                                .binding(intToColor(0xAA000000), () -> intToColor(cfg.keyBackgroundColor), v -> cfg.keyBackgroundColor = colorToInt(v))
                                .controller(opt -> ColorControllerBuilder.create(opt).allowAlpha(true))
                                .build())
                        .option(Option.<java.awt.Color>createBuilder()
                                .name(Component.literal("Pressed Key Text Color"))
                                .binding(intToColor(0xFF000000), () -> intToColor(cfg.keyPressedColor), v -> cfg.keyPressedColor = colorToInt(v))
                                .controller(opt -> ColorControllerBuilder.create(opt).allowAlpha(true))
                                .build())
                        .option(Option.<java.awt.Color>createBuilder()
                                .name(Component.literal("Pressed Key Background Color"))
                                .binding(intToColor(0xAAFFFFFF), () -> intToColor(cfg.keyPressedBackgroundColor), v -> cfg.keyPressedBackgroundColor = colorToInt(v))
                                .controller(opt -> ColorControllerBuilder.create(opt).allowAlpha(true))
                                .build())
                        .build())
                .save(cfg::save)
                .build()
                .generateScreen(parent);
    }
}