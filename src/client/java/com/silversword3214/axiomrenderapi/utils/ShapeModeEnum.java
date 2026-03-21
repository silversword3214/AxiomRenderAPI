package com.silversword3214.axiomrenderapi.utils;

public enum ShapeModeEnum {
    LINES,
    SIDES,
    BOTH;

    public boolean lines() {
        return this == LINES || this == BOTH;
    }

    public boolean sides() {
        return this == SIDES || this == BOTH;
    }
}