package de.openglapp.helper;

public class HelperMath {
	public static float getRadiansFromDegrees(float degrees) {
		return (float) Math.PI * degrees / 180f;
	}

	public static float getDegreesFromRadians(float radiant) {
		return (180f * radiant) / (float) Math.PI;
	}

	public static float clamp(float v, float lower, float upper) {
		if (v >= lower && v <= upper) {
			return v;
		} else {
			if (v < lower)
				return lower;
			else
				return upper;
		}
	}
}
