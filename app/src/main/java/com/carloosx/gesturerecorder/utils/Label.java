package com.carloosx.gesturerecorder.utils;

public enum Label {
    LEFT(0xFF2980B9),
    RIGHT(0xFFE74C3C),
    TOP(0xFF27AE60),
    BOTTOM(0xFFF1C40F),
    CIRCLE(0xFF8E44AD);

    public final int color;

    Label(int color) {
        this.color = color;
    }
}
