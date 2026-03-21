package com.silversword3214.axiomrenderapi.test;

import com.silversword3214.axiomrenderapi.api.Renderer3D;
import com.silversword3214.axiomrenderapi.utils.ShapeModeEnum;
import com.silversword3214.axiomrenderapi.utils.color.Color;
import net.minecraft.client.Minecraft;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.phys.AABB;

import java.util.List;

public class ESP {
    private static final ESP INSTANCE = new ESP();
    private boolean enabled = true;

    private ESP() {}

    public static ESP getInstance() {
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

        List<LivingEntity> entities = mc.level.getEntitiesOfClass(LivingEntity.class,
                mc.player.getBoundingBox().inflate(50.0));

        for (LivingEntity entity : entities) {
            if (entity == mc.player) continue;

            // Interpolate position
            double x = entity.xOld + (entity.getX() - entity.xOld) * tickDelta;
            double y = entity.yOld + (entity.getY() - entity.yOld) * tickDelta;
            double z = entity.zOld + (entity.getZ() - entity.zOld) * tickDelta;

            double width = entity.getBbWidth();
            double height = entity.getBbHeight();
            double halfWidth = width / 2.0;

            // inside render method
            Color outlineColor = new Color(0, 255, 0, 204);
            Color fillColor = new Color(255, 0, 0, 48);
            renderer.drawBox(x - halfWidth, y, z - halfWidth,
                    x + halfWidth, y + height, z + halfWidth,
                    fillColor, outlineColor, ShapeModeEnum.BOTH, 0);
        }
    }
}