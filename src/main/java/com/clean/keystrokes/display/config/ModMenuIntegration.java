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
                                .name(Component.literal("Tick Synced Key Presses"))
                                .description(OptionDescription.of(Component.literal("Shows keyboard keys as pressed only when Minecraft has registered the key state for the current tick.")))
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
                        .name(Component.literal("Colors"))
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