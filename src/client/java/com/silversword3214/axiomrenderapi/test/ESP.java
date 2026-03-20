package com.silversword3214.axiomrenderapi.test;

import com.silversword3214.axiomrenderapi.RenderAPI;
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

    public void render() {
        if (!enabled) return;

        Minecraft mc = Minecraft.getInstance();
        if (mc.level == null || mc.player == null) return;

        List<LivingEntity> entities = mc.level.getEntitiesOfClass(LivingEntity.class,
                mc.player.getBoundingBox().inflate(50.0));

        RenderAPI api = RenderAPI.getInstance();

        for (LivingEntity entity : entities) {
            if (entity == mc.player) continue;

            AABB box = entity.getBoundingBox();
            api.world().box((float) box.minX, (float) box.minY, (float) box.minZ,
                    (float) box.maxX, (float) box.maxY, (float) box.maxZ,
                    0xCCFFFFFF);
        }
    }
}