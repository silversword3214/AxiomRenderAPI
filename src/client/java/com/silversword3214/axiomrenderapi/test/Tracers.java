package com.silversword3214.axiomrenderapi.test;

import com.silversword3214.axiomrenderapi.api.Renderer3D;
import com.silversword3214.axiomrenderapi.utils.RenderUtils;
import net.minecraft.client.Minecraft;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.phys.Vec3;

import java.util.List;

public class Tracers {
    private static final Tracers INSTANCE = new Tracers();
    private boolean enabled = true;

    private Tracers() {}

    public static Tracers getInstance() {
        return INSTANCE;
    }

    public void setEnabled(boolean enabled) {
        this.enabled = enabled;
    }

    public boolean isEnabled() {
        return enabled;
    }

    public void render(Renderer3D renderer, float tickDelta) {
        if (!enabled) return;

        Minecraft mc = Minecraft.getInstance();
        if (mc.level == null || mc.player == null) return;

        Vec3 start = RenderUtils.center; // ruudun keskellä oleva maailmapiste

        List<LivingEntity> entities = mc.level.getEntitiesOfClass(LivingEntity.class,
                mc.player.getBoundingBox().inflate(50.0));

        for (LivingEntity entity : entities) {
            if (entity == mc.player) continue;

            // Entityn interpoloidut koordinaatit (silmäkorkeus)
            double ex = entity.xOld + (entity.getX() - entity.xOld) * tickDelta;
            double ey = entity.yOld + (entity.getY() - entity.yOld) * tickDelta + entity.getEyeHeight();
            double ez = entity.zOld + (entity.getZ() - entity.zOld) * tickDelta;

            // Käytetään koordinaattipohjaista drawLine-metodia
            renderer.drawLine(start.x, start.y, start.z, ex, ey, ez, 0xFF00FF00);
        }
    }
}