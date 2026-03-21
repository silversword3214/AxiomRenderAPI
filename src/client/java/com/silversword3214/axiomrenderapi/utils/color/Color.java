package com.silversword3214.axiomrenderapi.utils.color;

/**
 * Represents a color with red, green, blue, and alpha components (0-255).
 */
public class Color {
    public int r, g, b, a;

    public Color(int r, int g, int b, int a) {
        this.r = clamp(r);
        this.g = clamp(g);
        this.b = clamp(b);
        this.a = clamp(a);
    }

    public Color(int r, int g, int b) {
        this(r, g, b, 255);
    }

    public Color(int argb) {
        this.a = (argb >> 24) & 0xFF;
        this.r = (argb >> 16) & 0xFF;
        this.g = (argb >> 8) & 0xFF;
        this.b = argb & 0xFF;
    }

    public Color(float r, float g, float b, float a) {
        this((int)(r * 255), (int)(g * 255), (int)(b * 255), (int)(a * 255));
    }

    public Color(float r, float g, float b) {
        this(r, g, b, 1.0f);
    }

    public Color(Color other) {
        this(other.r, other.g, other.b, other.a);
    }

    private static int clamp(int value) {
        return Math.max(0, Math.min(255, value));
    }

    public int getARGB() {
        return (a << 24) | (r << 16) | (g << 8) | b;
    }

    public int getRGB() {
        return (r << 16) | (g << 8) | b;
    }

    public Color setARGB(int argb) {
        this.a = (argb >> 24) & 0xFF;
        this.r = (argb >> 16) & 0xFF;
        this.g = (argb >> 8) & 0xFF;
        this.b = argb & 0xFF;
        return this;
    }

    public Color setRGB(int rgb) {
        this.r = (rgb >> 16) & 0xFF;
        this.g = (rgb >> 8) & 0xFF;
        this.b = rgb & 0xFF;
        return this;
    }

    public Color withAlpha(int alpha) {
        return new Color(r, g, b, alpha);
    }

    public Color withRed(int red) {
        return new Color(red, g, b, a);
    }

    public Color withGreen(int green) {
        return new Color(r, green, b, a);
    }

    public Color withBlue(int blue) {
        return new Color(r, g, blue, a);
    }

    public Color multiply(float factor) {
        return new Color((int)(r * factor), (int)(g * factor), (int)(b * factor), a);
    }

    public Color multiply(float factor, boolean multiplyAlpha) {
        int newAlpha = multiplyAlpha ? (int)(a * factor) : a;
        return new Color((int)(r * factor), (int)(g * factor), (int)(b * factor), newAlpha);
    }

    public Color lerp(Color other, float t) {
        return new Color(
                (int)(r + (other.r - r) * t),
                (int)(g + (other.g - g) * t),
                (int)(b + (other.b - b) * t),
                (int)(a + (other.a - a) * t)
        );
    }

    @Override
    public String toString() {
        return String.format("Color(r=%d, g=%d, b=%d, a=%d)", r, g, b, a);
    }

    @Override
    public boolean equals(Object obj) {
        if (this == obj) return true;
        if (!(obj instanceof Color)) return false;
        Color other = (Color) obj;
        return r == other.r && g == other.g && b == other.b && a == other.a;
    }

    @Override
    public int hashCode() {
        return getARGB();
    }
}