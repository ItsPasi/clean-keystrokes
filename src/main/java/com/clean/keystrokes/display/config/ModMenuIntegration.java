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

        class ShadowOptionAvailability {
            Option<Boolean> customShadowColorsOption;
            Option<java.awt.Color> keyTextShadowColorOption;
            Option<java.awt.Color> keyPressedTextShadowColorOption;
            Option<Boolean> rainbowKeyTextShadowOption;
            Option<Boolean> rainbowKeyPressedTextShadowOption;

            void update() {
                boolean anyShadowEnabled = cfg.keyTextShadow || cfg.keyPressedTextShadow;

                if (!anyShadowEnabled) {
                    cfg.useCustomTextShadowColor = false;
                }

                if (!cfg.keyTextShadow) {
                    cfg.rainbowKeyTextShadow = false;
                    if (rainbowKeyTextShadowOption != null) {
                        rainbowKeyTextShadowOption.requestSet(false);
                    }
                }

                if (!cfg.keyPressedTextShadow) {
                    cfg.rainbowKeyPressedTextShadow = false;
                    if (rainbowKeyPressedTextShadowOption != null) {
                        rainbowKeyPressedTextShadowOption.requestSet(false);
                    }
                }

                if (customShadowColorsOption != null) {
                    customShadowColorsOption.setAvailable(anyShadowEnabled);
                }

                boolean shadowColorOptionsEnabled = anyShadowEnabled && cfg.useCustomTextShadowColor;
                if (keyTextShadowColorOption != null) {
                    keyTextShadowColorOption.setAvailable(shadowColorOptionsEnabled);
                }
                if (keyPressedTextShadowColorOption != null) {
                    keyPressedTextShadowColorOption.setAvailable(shadowColorOptionsEnabled);
                }

                if (rainbowKeyTextShadowOption != null) {
                    rainbowKeyTextShadowOption.setAvailable(cfg.keyTextShadow);
                }
                if (rainbowKeyPressedTextShadowOption != null) {
                    rainbowKeyPressedTextShadowOption.setAvailable(cfg.keyPressedTextShadow);
                }
            }
        }

        ShadowOptionAvailability shadowOptionAvailability = new ShadowOptionAvailability();
        boolean anyShadowEnabled = cfg.keyTextShadow || cfg.keyPressedTextShadow;
        boolean shadowColorOptionsEnabled = anyShadowEnabled && cfg.useCustomTextShadowColor;

        Option<java.awt.Color> keyTextShadowColorOption = Option.<java.awt.Color>createBuilder()
                .name(Component.literal("Key Text Shadow Color"))
                .binding(intToColor(0xFF000000), () -> intToColor(cfg.keyTextShadowColor), v -> {
                    cfg.keyTextShadowColor = colorToInt(v);
                    selectedPreset[0] = cfg.refreshPresetFromCurrentColors();
                })
                .available(shadowColorOptionsEnabled)
                .controller(opt -> ColorControllerBuilder.create(opt).allowAlpha(true))
                .build();
        shadowOptionAvailability.keyTextShadowColorOption = keyTextShadowColorOption;

        Option<java.awt.Color> keyPressedTextShadowColorOption = Option.<java.awt.Color>createBuilder()
                .name(Component.literal("Pressed Key Text Shadow Color"))
                .binding(intToColor(0xFF000000), () -> intToColor(cfg.keyPressedTextShadowColor), v -> {
                    cfg.keyPressedTextShadowColor = colorToInt(v);
                    selectedPreset[0] = cfg.refreshPresetFromCurrentColors();
                })
                .available(shadowColorOptionsEnabled)
                .controller(opt -> ColorControllerBuilder.create(opt).allowAlpha(true))
                .build();
        shadowOptionAvailability.keyPressedTextShadowColorOption = keyPressedTextShadowColorOption;

        Option<Boolean> customShadowColorsOption = Option.<Boolean>createBuilder()
                .name(Component.literal("Custom Shadow Colors"))
                .binding(false, () -> cfg.useCustomTextShadowColor, v -> {
                    cfg.useCustomTextShadowColor = v;
                    selectedPreset[0] = cfg.refreshPresetFromCurrentColors();
                    shadowOptionAvailability.update();
                })
                .available(anyShadowEnabled)
                .controller(opt -> BooleanControllerBuilder.create(opt).coloured(true))
                .build();
        shadowOptionAvailability.customShadowColorsOption = customShadowColorsOption;

        Option<Boolean> keyTextShadowOption = Option.<Boolean>createBuilder()
                .name(Component.literal("Key Text Shadow"))
                .binding(false, () -> cfg.keyTextShadow, v -> {
                    cfg.keyTextShadow = v;
                    selectedPreset[0] = cfg.refreshPresetFromCurrentColors();
                    shadowOptionAvailability.update();
                })
                .controller(opt -> BooleanControllerBuilder.create(opt).coloured(true))
                .build();

        Option<Boolean> keyPressedTextShadowOption = Option.<Boolean>createBuilder()
                .name(Component.literal("Pressed Key Text Shadow"))
                .binding(false, () -> cfg.keyPressedTextShadow, v -> {
                    cfg.keyPressedTextShadow = v;
                    selectedPreset[0] = cfg.refreshPresetFromCurrentColors();
                    shadowOptionAvailability.update();
                })
                .controller(opt -> BooleanControllerBuilder.create(opt).coloured(true))
                .build();

        Option<Boolean> rainbowKeyTextShadowOption = Option.<Boolean>createBuilder()
                .name(Component.literal("Rainbow Key Text Shadow"))
                .binding(false, () -> cfg.rainbowKeyTextShadow, v -> {
                    cfg.rainbowKeyTextShadow = v;
                    selectedPreset[0] = cfg.refreshPresetFromCurrentColors();
                })
                .available(cfg.keyTextShadow)
                .controller(TickBoxControllerBuilder::create)
                .build();
        shadowOptionAvailability.rainbowKeyTextShadowOption = rainbowKeyTextShadowOption;

        Option<Boolean> rainbowKeyPressedTextShadowOption = Option.<Boolean>createBuilder()
                .name(Component.literal("Rainbow Pressed Key Text Shadow"))
                .binding(false, () -> cfg.rainbowKeyPressedTextShadow, v -> {
                    cfg.rainbowKeyPressedTextShadow = v;
                    selectedPreset[0] = cfg.refreshPresetFromCurrentColors();
                })
                .available(cfg.keyPressedTextShadow)
                .controller(TickBoxControllerBuilder::create)
                .build();
        shadowOptionAvailability.rainbowKeyPressedTextShadowOption = rainbowKeyPressedTextShadowOption;

        boolean customPositionSelected = cfg.position == KeystrokeConfig.CornerPosition.CUSTOM;

        Option<Double> customPositionXOption = Option.<Double>createBuilder()
                .name(Component.literal("Horizontal Position (%)"))
                .binding(50.0, () -> cfg.customPositionXPercent, v -> cfg.customPositionXPercent = v)
                .available(customPositionSelected)
                .controller(opt -> DoubleSliderControllerBuilder.create(opt).range(0.0, 100.0).step(0.5).formatValue(v -> Component.literal(String.format("%.1f%%", v))))
                .build();

        Option<Double> customPositionYOption = Option.<Double>createBuilder()
                .name(Component.literal("Vertical Position (%)"))
                .binding(50.0, () -> cfg.customPositionYPercent, v -> cfg.customPositionYPercent = v)
                .available(customPositionSelected)
                .controller(opt -> DoubleSliderControllerBuilder.create(opt).range(0.0, 100.0).step(0.5).formatValue(v -> Component.literal(String.format("%.1f%%", v))))
                .build();

        Option<KeystrokeConfig.CornerPosition> positionOption = Option.<KeystrokeConfig.CornerPosition>createBuilder()
                .name(Component.literal("Position"))
                .binding(KeystrokeConfig.CornerPosition.TOP_RIGHT,
                        () -> cfg.position,
                        v -> {
                            cfg.position = v;
                            boolean custom = v == KeystrokeConfig.CornerPosition.CUSTOM;
                            customPositionXOption.setAvailable(custom);
                            customPositionYOption.setAvailable(custom);
                        })
                .controller(opt -> EnumControllerBuilder.create(opt)
                        .enumClass(KeystrokeConfig.CornerPosition.class))
                .build();

        Option<java.awt.Color> keyTextColorOption = Option.<java.awt.Color>createBuilder()
                .name(Component.literal("Key Text Color"))
                .binding(intToColor(0xFFFFFFFF), () -> intToColor(cfg.keyColor), v -> {
                    cfg.keyColor = colorToInt(v);
                    selectedPreset[0] = cfg.refreshPresetFromCurrentColors();
                })
                .controller(opt -> ColorControllerBuilder.create(opt).allowAlpha(true))
                .build();

        Option<java.awt.Color> keyPressedTextColorOption = Option.<java.awt.Color>createBuilder()
                .name(Component.literal("Pressed Key Text Color"))
                .binding(intToColor(0xFF000000), () -> intToColor(cfg.keyPressedColor), v -> {
                    cfg.keyPressedColor = colorToInt(v);
                    selectedPreset[0] = cfg.refreshPresetFromCurrentColors();
                })
                .controller(opt -> ColorControllerBuilder.create(opt).allowAlpha(true))
                .build();

        Option<java.awt.Color> keyBackgroundColorOption = Option.<java.awt.Color>createBuilder()
                .name(Component.literal("Key Background Color"))
                .binding(intToColor(0xAA000000), () -> intToColor(cfg.keyBackgroundColor), v -> {
                    cfg.keyBackgroundColor = colorToInt(v);
                    selectedPreset[0] = cfg.refreshPresetFromCurrentColors();
                })
                .controller(opt -> ColorControllerBuilder.create(opt).allowAlpha(true))
                .build();

        Option<java.awt.Color> keyPressedBackgroundColorOption = Option.<java.awt.Color>createBuilder()
                .name(Component.literal("Pressed Key Background Color"))
                .binding(intToColor(0xAAFFFFFF), () -> intToColor(cfg.keyPressedBackgroundColor), v -> {
                    cfg.keyPressedBackgroundColor = colorToInt(v);
                    selectedPreset[0] = cfg.refreshPresetFromCurrentColors();
                })
                .controller(opt -> ColorControllerBuilder.create(opt).allowAlpha(true))
                .build();

        Option<KeystrokeConfig.ColorPreset> presetOption = Option.<KeystrokeConfig.ColorPreset>createBuilder()
                .name(Component.literal("Presets"))
                .description(OptionDescription.createBuilder()
                        .image(PRESET_PREVIEW, 1422, 2142)
                        .build())
                .binding(KeystrokeConfig.ColorPreset.CLASSIC,
                        () -> selectedPreset[0],
                        v -> {
                            selectedPreset[0] = v;
                            if (!v.isCustom()) {
                                cfg.applyPreset(v);
                                keyTextColorOption.requestSet(intToColor(cfg.keyColor));
                                keyPressedTextColorOption.requestSet(intToColor(cfg.keyPressedColor));
                                keyBackgroundColorOption.requestSet(intToColor(cfg.keyBackgroundColor));
                                keyPressedBackgroundColorOption.requestSet(intToColor(cfg.keyPressedBackgroundColor));
                                keyTextShadowOption.requestSet(cfg.keyTextShadow);
                                keyPressedTextShadowOption.requestSet(cfg.keyPressedTextShadow);
                                customShadowColorsOption.requestSet(cfg.useCustomTextShadowColor);
                                rainbowKeyTextShadowOption.requestSet(cfg.rainbowKeyTextShadow);
                                rainbowKeyPressedTextShadowOption.requestSet(cfg.rainbowKeyPressedTextShadow);
                                keyTextShadowColorOption.requestSet(intToColor(cfg.keyTextShadowColor));
                                keyPressedTextShadowColorOption.requestSet(intToColor(cfg.keyPressedTextShadowColor));
                                shadowOptionAvailability.update();
                            }
                        })
                .controller(opt -> EnumControllerBuilder.create(opt)
                        .enumClass(KeystrokeConfig.ColorPreset.class))
                .build();

        return YetAnotherConfigLib.createBuilder()
                .title(Component.literal("Clean Keystroke Settings"))
                .category(ConfigCategory.createBuilder()
                        .name(Component.literal("General"))
                        .option(positionOption)
                        .option(customPositionXOption)
                        .option(customPositionYOption)
                        .option(Option.<Boolean>createBuilder()
                                .name(Component.literal("Show Sneak/Sprint Row"))
                                .binding(true, () -> cfg.showSneakSprintRow, v -> cfg.showSneakSprintRow = v)
                                .controller(TickBoxControllerBuilder::create)
                                .build())
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
                        .build())
                .category(ConfigCategory.createBuilder()
                        .name(Component.literal("Appearance"))
                        .option(presetOption)
                        .option(keyTextColorOption)
                        .option(keyPressedTextColorOption)
                        .option(keyBackgroundColorOption)
                        .option(keyPressedBackgroundColorOption)
                        .option(keyTextShadowOption)
                        .option(keyPressedTextShadowOption)
                        .option(customShadowColorsOption)
                        .option(keyTextShadowColorOption)
                        .option(keyPressedTextShadowColorOption)
                        .option(rainbowKeyTextShadowOption)
                        .option(rainbowKeyPressedTextShadowOption)
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