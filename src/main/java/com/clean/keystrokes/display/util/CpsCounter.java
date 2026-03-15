package com.clean.keystrokes.display.util;

import java.util.ArrayDeque;
import java.util.Deque;

public class CpsCounter {
    private final Deque<Long> clicks = new ArrayDeque<>();

    public void registerClick() {
        clicks.addLast(System.currentTimeMillis());
    }

    public int getCps() {
        long now = System.currentTimeMillis();
        while (!clicks.isEmpty() && now - clicks.peekFirst() > 1000) {
            clicks.pollFirst();
        }
        return clicks.size();
    }
}