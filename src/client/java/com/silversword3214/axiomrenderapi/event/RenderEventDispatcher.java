package com.silversword3214.axiomrenderapi.event;

import java.util.List;
import java.util.concurrent.CopyOnWriteArrayList;
import java.util.function.Consumer;

public class RenderEventDispatcher {
    private static final List<Consumer<Render3DEvent>> render3DListeners = new CopyOnWriteArrayList<>();
    private static final List<Consumer<RenderHUDEvent>> renderHUDListeners = new CopyOnWriteArrayList<>();

    public static void addRender3DListener(Consumer<Render3DEvent> listener) {
        render3DListeners.add(listener);
    }

    public static void removeRender3DListener(Consumer<Render3DEvent> listener) {
        render3DListeners.remove(listener);
    }

    public static void addRenderHUDListener(Consumer<RenderHUDEvent> listener) {
        renderHUDListeners.add(listener);
    }

    public static void removeRenderHUDListener(Consumer<RenderHUDEvent> listener) {
        renderHUDListeners.remove(listener);
    }

    public static void dispatchRender3D(Render3DEvent event) {
        for (Consumer<Render3DEvent> listener : render3DListeners) {
            listener.accept(event);
        }
    }

    public static void dispatchRenderHUD(RenderHUDEvent event) {
        for (Consumer<RenderHUDEvent> listener : renderHUDListeners) {
            listener.accept(event);
        }
    }
}