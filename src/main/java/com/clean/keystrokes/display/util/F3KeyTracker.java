package com.clean.keystrokes.display.util;

import org.lwjgl.glfw.GLFW;

public final class F3KeyTracker {

    private static boolean initialized;
    private static boolean f3WasDown;
    private static boolean f3CombinationUsed;
    private static boolean mainF3Open;

    public static boolean isMainF3Open(long window) {
        boolean f3Down = GLFW.glfwGetKey(window, GLFW.GLFW_KEY_F3) == GLFW.GLFW_PRESS;

        if (!initialized) {
            initialized = true;
            f3WasDown = f3Down;
            return mainF3Open;
        }

        if (!f3Down && f3WasDown) {
            if (!f3CombinationUsed) {
                mainF3Open = !mainF3Open;
            }
            f3CombinationUsed = false;
        }

        f3WasDown = f3Down;
        return mainF3Open;
    }

    public static void onDebugCombination() {
        f3CombinationUsed = true;
    }
}
