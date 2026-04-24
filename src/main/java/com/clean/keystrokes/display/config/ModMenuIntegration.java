package com.clean.keystrokes.display.config;

import com.clean.keystrokes.CleanKeyStrokes;
import com.terraformersmc.modmenu.api.ConfigScreenFactory;
import com.terraformersmc.modmenu.api.ModMenuApi;
import dev.isxander.yacl3.api.*;
import dev.isxander.yacl3.api.controller.*;
import net.minecraft.client.gui.screens.Screen;
import net.minecraft.network.chat.Component;
import net.minecraft.resources.Identifier;

public class ModMenuIntegration implements ModMenuApi {

    private static final Identifier PRESET_PREVIEW = Identifier.fromNamespaceAndPath(
            CleanKeyStrokes.MOD_ID,
            "textures/gui/presets/preset_overview.png"
    );

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
        final KeystrokeConfig.ColorPreset[] selectedPreset = {cfg.colorPreset};

        Option<java.awt.Color> keyTextShadowColorOption = Option.<java.awt.Color>createBuilder()
                .name(Component.literal("Key Text Shadow Color"))
                .binding(intToColor(0xFF000000), () -> intToColor(cfg.keyTextShadowColor), v -> cfg.keyTextShadowColor = colorToInt(v))
                .available(cfg.useCustomTextShadowColor)
                .controller(opt -> ColorControllerBuilder.create(opt).allowAlpha(true))
                .build();

        Option<java.awt.Color> keyPressedTextShadowColorOption = Option.<java.awt.Color>createBuilder()
                .name(Component.literal("Pressed Key Text Shadow Color"))
                .binding(intToColor(0xFF000000), () -> intToColor(cfg.keyPressedTextShadowColor), v -> cfg.keyPressedTextShadowColor = colorToInt(v))
                .available(cfg.useCustomTextShadowColor)
                .controller(opt -> ColorControllerBuilder.create(opt).allowAlpha(true))
                .build();

        Option<Boolean> customShadowColorsOption = Option.<Boolean>createBuilder()
                .name(Component.literal("Custom Shadow Colors"))
                .binding(false, () -> cfg.useCustomTextShadowColor, v -> cfg.useCustomTextShadowColor = v)
                .listener((opt, value) -> {
                    keyTextShadowColorOption.setAvailable(value);
                    keyPressedTextShadowColorOption.setAvailable(value);
                })
                .controller(opt -> BooleanControllerBuilder.create(opt).coloured(true))
                .build();

        Option<Double> customPositionXOption = Option.<Double>createBuilder()
                .name(Component.literal("Horizontal Position (%)"))
                .binding(50.0, () -> cfg.customPositionXPercent, v -> cfg.customPositionXPercent = v)
                .available(cfg.useCustomPosition)
                .controller(opt -> DoubleSliderControllerBuilder.create(opt).range(0.0, 100.0).step(0.5).formatValue(v -> Component.literal(String.format("%.1f%%", v))))
                .build();

        Option<Double> customPositionYOption = Option.<Double>createBuilder()
                .name(Component.literal("Vertical Position (%)"))
                .binding(50.0, () -> cfg.customPositionYPercent, v -> cfg.customPositionYPercent = v)
                .available(cfg.useCustomPosition)
                .controller(opt -> DoubleSliderControllerBuilder.create(opt).range(0.0, 100.0).step(0.5).formatValue(v -> Component.literal(String.format("%.1f%%", v))))
                .build();

        Option<Boolean> customPositionOption = Option.<Boolean>createBuilder()
                .name(Component.literal("Custom Position"))
                .binding(false, () -> cfg.useCustomPosition, v -> cfg.useCustomPosition = v)
                .listener((opt, value) -> {
                    customPositionXOption.setAvailable(value);
                    customPositionYOption.setAvailable(value);
                })
                .controller(opt -> BooleanControllerBuilder.create(opt).coloured(true))
                .build();

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
                        .option(customPositionOption)
                        .option(customPositionXOption)
                        .option(customPositionYOption)
                        .option(Option.<Boolean>createBuilder()
                                .name(Component.literal("Press Animation"))
                                .binding(true, () -> cfg.pressAnimation, v -> cfg.pressAnimation = v)
                                .controller(TickBoxControllerBuilder::create)
                                .build())
                        .option(Option.<Boolean>createBuilder()
                                .name(Component.literal("Tick Synced Key Presses"))
                                .binding(false, () -> cfg.tickSyncedKeyPresses, v -> cfg.tickSyncedKeyPresses = v)
                                .controller(TickBoxControllerBuilder::create)
                                .build())
                        .option(Option.<Boolean>createBuilder()
                                .name(Component.literal("Show Sneak / Sprint Row"))
                                .binding(true, () -> cfg.showSneakSprintRow, v -> cfg.showSneakSprintRow = v)
                                .controller(TickBoxControllerBuilder::create)
                                .build())
                        .build())
                .category(ConfigCategory.createBuilder()
                        .name(Component.literal("Appearance"))
                        .option(Option.<KeystrokeConfig.ColorPreset>createBuilder()
                                .name(Component.literal("Presets"))
                                .description(OptionDescription.createBuilder()
                                        .image(PRESET_PREVIEW, 1422, 2142)
                                        .build())
                                .binding(KeystrokeConfig.ColorPreset.CLASSIC,
                                        () -> selectedPreset[0],
                                        v -> selectedPreset[0] = v)
                                .controller(opt -> EnumControllerBuilder.create(opt)
                                        .enumClass(KeystrokeConfig.ColorPreset.class))
                                .build())
                        .option(Option.<java.awt.Color>createBuilder()
                                .name(Component.literal("Key Text Color"))
                                .binding(intToColor(0xFFFFFFFF), () -> intToColor(cfg.keyColor), v -> {
                                    cfg.keyColor = colorToInt(v);
                                    selectedPreset[0] = cfg.refreshPresetFromCurrentColors();
                                })
                                .controller(opt -> ColorControllerBuilder.create(opt).allowAlpha(true))
                                .build())
                        .option(Option.<java.awt.Color>createBuilder()
                                .name(Component.literal("Pressed Key Text Color"))
                                .binding(intToColor(0xFF000000), () -> intToColor(cfg.keyPressedColor), v -> {
                                    cfg.keyPressedColor = colorToInt(v);
                                    selectedPreset[0] = cfg.refreshPresetFromCurrentColors();
                                })
                                .controller(opt -> ColorControllerBuilder.create(opt).allowAlpha(true))
                                .build())
                        .option(Option.<java.awt.Color>createBuilder()
                                .name(Component.literal("Key Background Color"))
                                .binding(intToColor(0xAA000000), () -> intToColor(cfg.keyBackgroundColor), v -> {
                                    cfg.keyBackgroundColor = colorToInt(v);
                                    selectedPreset[0] = cfg.refreshPresetFromCurrentColors();
                                })
                                .controller(opt -> ColorControllerBuilder.create(opt).allowAlpha(true))
                                .build())
                        .option(Option.<java.awt.Color>createBuilder()
                                .name(Component.literal("Pressed Key Background Color"))
                                .binding(intToColor(0xAAFFFFFF), () -> intToColor(cfg.keyPressedBackgroundColor), v -> {
                                    cfg.keyPressedBackgroundColor = colorToInt(v);
                                    selectedPreset[0] = cfg.refreshPresetFromCurrentColors();
                                })
                                .controller(opt -> ColorControllerBuilder.create(opt).allowAlpha(true))
                                .build())
                        .option(Option.<Boolean>createBuilder()
                                .name(Component.literal("Rainbow Key Text"))
                                .binding(false, () -> cfg.rainbowKeyNormal, v -> cfg.rainbowKeyNormal = v)
                                .controller(TickBoxControllerBuilder::create)
                                .build())
                        .option(Option.<Boolean>createBuilder()
                                .name(Component.literal("Rainbow Pressed Key Text"))
                                .binding(false, () -> cfg.rainbowKeyPressed, v -> cfg.rainbowKeyPressed = v)
                                .controller(TickBoxControllerBuilder::create)
                                .build())
                        .option(Option.<Boolean>createBuilder()
                                .name(Component.literal("Rainbow Background"))
                                .binding(false, () -> cfg.rainbowBackgroundNormal, v -> cfg.rainbowBackgroundNormal = v)
                                .controller(TickBoxControllerBuilder::create)
                                .build())
                        .option(Option.<Boolean>createBuilder()
                                .name(Component.literal("Rainbow Pressed Background"))
                                .binding(false, () -> cfg.rainbowBackgroundPressed, v -> cfg.rainbowBackgroundPressed = v)
                                .controller(TickBoxControllerBuilder::create)
                                .build())
                        .option(Option.<Boolean>createBuilder()
                                .name(Component.literal("Key Text Shadow"))
                                .binding(false, () -> cfg.keyTextShadow, v -> cfg.keyTextShadow = v)
                                .controller(opt -> BooleanControllerBuilder.create(opt).coloured(true))
                                .build())
                        .option(Option.<Boolean>createBuilder()
                                .name(Component.literal("Pressed Key Text Shadow"))
                                .binding(false, () -> cfg.keyPressedTextShadow, v -> cfg.keyPressedTextShadow = v)
                                .controller(opt -> BooleanControllerBuilder.create(opt).coloured(true))
                                .build())
                        .option(customShadowColorsOption)
                        .option(keyTextShadowColorOption)
                        .option(keyPressedTextShadowColorOption)
                        .build())
                .save(() -> {
                    if (selectedPreset[0] == null) {
                        selectedPreset[0] = cfg.refreshPresetFromCurrentColors();
                    }

                    if (!selectedPreset[0].isCustom()) {
                        cfg.applyPreset(selectedPreset[0]);
                    } else {
                        cfg.refreshPresetFromCurrentColors();
                    }

                    cfg.save();
                })
                .build()
                .generateScreen(parent);
    }
}