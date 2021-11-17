package de.openglapp.helper;

public class HelperMath {
    public static float getRadiansFromDegrees(float degrees) {
        return (float) Math.PI * degrees / 180f;
    }

    public static float getDegreesFromRadians(float radiant) {
        return (180f * radiant) / (float) Math.PI;
    }
}
