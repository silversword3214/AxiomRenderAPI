package com.silversword3214.axiomrenderapi.mixin;

import com.mojang.blaze3d.pipeline.RenderPipeline;
import com.silversword3214.axiomrenderapi.mixininterface.ILineSmoothing;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Unique;

@Mixin(RenderPipeline.class)
public class LineSmoothingMixin implements ILineSmoothing {
    @Unique
    private boolean lineSmooth;

    @Override
    public void axiom_setLineSmooth(boolean smooth) {
        this.lineSmooth = smooth;
    }

    @Override
    public boolean axiom_getLineSmooth() {
        return lineSmooth;
    }
}