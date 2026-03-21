package com.silversword3214.axiomrenderapi.event;

public abstract class RenderEvent {
    protected final float tickDelta;

    public RenderEvent(float tickDelta) {
        this.tickDelta = tickDelta;
    }

    public float getTickDelta() {
        return tickDelta;
    }
}