package com.silversword3214.axiomrenderapi.core;

import org.joml.Matrix3x2f;
import org.joml.Matrix4f;

public class MatrixUtil {
    public static Matrix4f toMatrix4f(Matrix3x2f mat2, float z) {
        Matrix4f m = new Matrix4f();
        m.m00(mat2.m00);
        m.m01(mat2.m01);
        m.m03(mat2.m20);
        m.m10(mat2.m10);
        m.m11(mat2.m11);
        m.m13(mat2.m21);
        m.m22(1);
        m.m33(1);
        m.m23(z);
        return m;
    }
}