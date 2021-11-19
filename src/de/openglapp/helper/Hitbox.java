package de.openglapp.helper;

import org.lwjgl.util.vector.*;

public class Hitbox {

	private static final Matrix4f _tempMatrix = new Matrix4f();
	private static final Vector3f _tempVector = new Vector3f();
	private static final Matrix4f _normalMatrix = new Matrix4f();

	private final Vector3f[] BASEVERTICES = new Vector3f[] { 
			new Vector3f(-0.5f, -0.5f, +0.5f),
			new Vector3f(+0.5f, -0.5f, +0.5f), 
			new Vector3f(+0.5f, -0.5f, -0.5f), 
			new Vector3f(-0.5f, -0.5f, -0.5f),
			new Vector3f(+0.5f, +0.5f, +0.5f), 
			new Vector3f(-0.5f, +0.5f, +0.5f), 
			new Vector3f(-0.5f, +0.5f, -0.5f),
			new Vector3f(+0.5f, +0.5f, -0.5f) 
			};

	private final Vector3f[] BASENORMALS = new Vector3f[] { 
			new Vector3f(1, 0, 0), 
			new Vector3f(0, 1, 0),
			new Vector3f(0, 0, 1) 
			};

	private final Vector3f BASECENTER = new Vector3f(0, 0, 0);

	private Vector3f[] _vertices = new Vector3f[8];
	private Vector3f[] _normals = new Vector3f[3];
	private Vector3f _center = new Vector3f(0, 0, 0);

	public Hitbox()
	{
		// Fülle alle Eckpunkte der Hitbox einmalig mit (0|0|0):
		for(int i = 0; i < _vertices.length; i++)
		{
			if(i < _normals.length)
			{
				_normals[i] = new Vector3f(0,0,0);
			}
			_vertices[i] = new Vector3f(0,0,0);
		}
	}
	
	public void update(Matrix4f modelMatrix) {

		// Um die Normal-Matrix zu berechnen, transponiert man
		// zuerst die model-Matrix und invertiert anschließend
		// das Ergebnis daraus:
		Matrix4f.transpose(modelMatrix, _tempMatrix);
		Matrix4f.invert(_tempMatrix, _normalMatrix);

		for (int i = 0; i < 8; i++) {
			if (i < 3) {
				// transform the normals by multiplying them by the normal matrix:
				// (Hint: you may use the modelMatrix here as well)
				transformNormal(BASENORMALS[i], _normalMatrix, _normals[i]);
			}
			// transform the corner points by multiplying them by the model matrix:
			transformPosition(BASEVERTICES[i], modelMatrix, _vertices[i]);
		}
		// transform the center point by multiplying it by the model matrix:
		transformPosition(BASECENTER, modelMatrix, _center);
	}

	public static boolean doCollisionTest(Hitbox a, Hitbox b, Vector3f mtv) {
		float mtvDistance = Float.MAX_VALUE;
		float mtvDirection = 1;
		mtv.x = 0;
		mtv.y = 0;
		mtv.z = 0;

		Vector2f shape1MinMax;// = new Vector2f();
		Vector2f shape2MinMax;// = new Vector2f();

		for (int i = 0; i < 3; i++) {
			shape1MinMax = satTest(a._normals[i], a._vertices);
			shape2MinMax = satTest(a._normals[i], b._vertices);
			if (!overlaps(shape1MinMax.x, shape1MinMax.y, shape2MinMax.x, shape2MinMax.y)) {
				return false;
			} else {
				boolean error = calculateOverlap(a._normals[i], shape1MinMax.x, shape1MinMax.y, shape2MinMax.x,
						shape2MinMax.y, mtvDistance, mtv, mtvDirection, a._center, b._center);
				if (error)
					return false;
			}

			shape1MinMax = satTest(b._normals[i], a._vertices);
			shape2MinMax = satTest(b._normals[i], b._vertices);
			if (!overlaps(shape1MinMax.x, shape1MinMax.y, shape2MinMax.x, shape2MinMax.y)) {
				return false;
			} else {
				boolean error = calculateOverlap(b._normals[i], shape1MinMax.x, shape1MinMax.y, shape2MinMax.x,
						shape2MinMax.y, mtvDistance, mtv, mtvDirection, a._center, b._center);
				if (error)
					return false;
			}

		}

		return true;
	}

	private static boolean calculateOverlap(Vector3f axis, float shape1Min, float shape1Max, float shape2Min,
			float shape2Max, float mtvDistance, Vector3f mtv, float mtvDirection, Vector3f posA, Vector3f posB) {
		float intersectionDepthScaled;
		if (shape1Min < shape2Min) {
			if (shape1Max > shape2Max) {
				float diff1 = shape1Max - shape2Max;
				float diff2 = shape2Min - shape1Min;
				if (diff1 > diff2) {
					intersectionDepthScaled = shape2Max - shape1Min;
				} else {
					intersectionDepthScaled = shape2Min - shape1Max;
				}
			} else {
				intersectionDepthScaled = shape1Max - shape2Min; // default
			}
		} else {
			if (shape1Max < shape2Max) {
				float diff1 = shape2Max - shape1Max;
				float diff2 = shape1Min - shape2Min;
				if (diff1 > diff2) {
					intersectionDepthScaled = shape1Max - shape2Min;
				} else {
					intersectionDepthScaled = shape1Min - shape2Max;
				}
			} else {
				intersectionDepthScaled = shape1Min - shape2Max; // default
			}
		}

		float axisLengthSquared = Vector3f.dot(axis, axis);
		if (axisLengthSquared < 1.0e-8f) {
			return true; // math error!
		}
		float intersectionDepthSquared = (intersectionDepthScaled * intersectionDepthScaled) / axisLengthSquared;

		if (intersectionDepthSquared < mtvDistance || mtvDistance < 0) {
			mtvDistance = intersectionDepthSquared;
			HelperVector.mul(axis, intersectionDepthScaled / axisLengthSquared, mtv);
			float notSameDirection = Vector3f.dot(HelperVector.sub(posA, posB), mtv);
			mtvDirection = notSameDirection < 0 ? -1.0f : 1.0f;
			HelperVector.mul(mtvDirection, mtv);
		}

		return false; // no error
	}

	private static Vector2f satTest(Vector3f axisToTest, Vector3f[] points) {
		Vector2f result = new Vector2f();

		float minAlong = Float.MAX_VALUE;
		float maxAlong = Float.MIN_VALUE;
		for (int i = 0; i < points.length; i++) {
			float dotVal = Vector3f.dot(points[i], axisToTest);
			if (dotVal < minAlong)
				minAlong = dotVal;
			if (dotVal > maxAlong)
				maxAlong = dotVal;
		}

		result.x = minAlong;
		result.y = maxAlong;
		return result;
	}

	private static boolean overlaps(float min1, float max1, float min2, float max2) {
		return isBetweenOrdered(min2, min1, max1) || isBetweenOrdered(min1, min2, max2);
	}

	private static boolean isBetweenOrdered(float val, float lowerBound, float upperBound) {
		return lowerBound <= val && val <= upperBound;
	}

	private static void transformNormal(Vector3f src, Matrix4f matrix, Vector3f result) {
		Matrix4f.invert(matrix, _tempMatrix);

		_tempVector.x = _tempMatrix.m00;
		_tempVector.y = _tempMatrix.m10;
		_tempVector.z = _tempMatrix.m20;
		result.x = Vector3f.dot(src, _tempVector);

		_tempVector.x = _tempMatrix.m01;
		_tempVector.y = _tempMatrix.m11;
		_tempVector.z = _tempMatrix.m21;
		result.y = Vector3f.dot(src, _tempVector);

		_tempVector.x = _tempMatrix.m02;
		_tempVector.y = _tempMatrix.m12;
		_tempVector.z = _tempMatrix.m22;
		result.z = Vector3f.dot(src, _tempVector);
	}

	private static void transformPosition(Vector3f src, Matrix4f matrix, Vector3f result) {
		_tempVector.x = matrix.m00;
		_tempVector.y = matrix.m01;
		_tempVector.z = matrix.m02;
		result.x = Vector3f.dot(src, _tempVector) + matrix.m03;

		_tempVector.x = matrix.m10;
		_tempVector.y = matrix.m11;
		_tempVector.z = matrix.m22;
		result.y = Vector3f.dot(src, _tempVector) + matrix.m13;

		_tempVector.x = matrix.m20;
		_tempVector.y = matrix.m21;
		_tempVector.z = matrix.m22;
		result.z = Vector3f.dot(src, _tempVector) + matrix.m23;
	}
}
