package de.openglapp.helper;

import org.lwjgl.util.vector.Matrix4f;
import org.lwjgl.util.vector.Quaternion;
import org.lwjgl.util.vector.Vector3f;
import org.lwjgl.util.vector.Vector4f;


public class HelperVector {
    public static enum RotationAxis { X, Y, Z};
	
    private final static Vector3f xAxis = new Vector3f(1, 0, 0);
    private final static Vector3f yAxis = new Vector3f(0, 1, 0);
    private final static Vector3f zAxis = new Vector3f(0, 0, 1);
    private final static Matrix4f tmpMatrix = new Matrix4f();
    private static final Matrix4f _tempMatrix = new Matrix4f();
	private static final Vector3f _tempVector = new Vector3f();
    private final static Vector4f temp4 = new Vector4f();
    private final static Quaternion qtemp = new Quaternion(0, 0, 0, 1);
    
    public static final Vector3f UNITX = new Vector3f(1, 0, 0);
    public static final Vector3f UNITY = new Vector3f(0, 1, 0);
    public static final Vector3f UNITZ = new Vector3f(0, 0, 1);
    
    private static Vector3f temp = new Vector3f();
	
	public static void mul(Vector3f vec, float scalar, Vector3f dest){
        dest.x = vec.x * scalar;
        dest.y = vec.y * scalar;
        dest.z = vec.z * scalar;
    }
    
    public static Vector3f mul(Vector3f vec, float scalar) {
        return new Vector3f(vec.x * scalar, vec.y * scalar, vec.z * scalar);
    }

    public static void mul(float scalar, Vector3f dest) {
        dest.x *= scalar;
        dest.y *= scalar;
        dest.z *= scalar;
    }

    public static Vector3f tripleProduct(Vector3f a, Vector3f b, Vector3f c)
    {
    	Vector3f result = new Vector3f(0, 0, 0);
    	Vector3f.cross(a,  b,  result);
    	Vector3f.cross(result, c, result);
    	return result;
    }
    
    public static void toAxisAngle(Quaternion src, Vector4f result) {
        Quaternion q = new Quaternion(src);
        //System.out.println("src: " + q.x + "; " + q.y + "; " + q.z + "; " + q.w);
        if (Math.abs(q.w) > 1.0f) {
            q.normalise(q);
        }

        result.w = 2.0f * (float) Math.acos(q.w); // angle
        float den = (float) Math.sqrt(1.0 - q.w * q.w);
        if (den > 0.0001f) {
            result.x = q.x / den;
            result.y = q.y / den;
            result.z = q.z / den;
        } else {
            // This occurs when the angle is zero.
            // Not a problem: just set an arbitrary normalized axis.
            result.x = 1;
            result.y = 0;
            result.z = 0;
        }
    }

    public static float calculateDotProduct(Vector3f a, Vector3f b){
        return Vector3f.dot(a, b);
    }
    
    public static Vector3f calculateCrossProduct(Vector3f a, Vector3f b){
        Vector3f result = new Vector3f();
        Vector3f.cross(a, b, result);
        return result;
    }
    
    private static float getTrace(Matrix4f matrix, boolean x3) {
        if (x3) {
            return matrix.m00 + matrix.m11 + matrix.m22;
        } else {
            return matrix.m00 + matrix.m11 + matrix.m22 + matrix.m33;
        }
    }

    public static void quaternionFromMatrix(Matrix4f matrix, Quaternion result) {
        float trace = getTrace(matrix, true);

        if (trace > 0) {
            float s = (float) Math.sqrt(trace + 1) * 2;
            float invS = 1f / s;

            result.w = s * 0.25f;
            result.x = (matrix.m12 - matrix.m21) * invS;
            result.y = (matrix.m20 - matrix.m02) * invS;
            result.z = (matrix.m01 - matrix.m10) * invS;
        } else {
            float m00 = matrix.m00, m11 = matrix.m11, m22 = matrix.m22;

            if (m00 > m11 && m00 > m22) {
                float s = (float) Math.sqrt(1 + m00 - m11 - m22) * 2;
                float invS = 1f / s;

                result.w = (matrix.m12 - matrix.m21) * invS;
                result.x = s * 0.25f;
                result.y = (matrix.m10 + matrix.m01) * invS;
                result.z = (matrix.m20 + matrix.m02) * invS;
            } else if (m11 > m22) {
                float s = (float) Math.sqrt(1 + m11 - m00 - m22) * 2;
                float invS = 1f / s;

                result.w = (matrix.m20 - matrix.m02) * invS;
                result.x = (matrix.m10 + matrix.m01) * invS;
                result.y = s * 0.25f;
                result.z = (matrix.m21 + matrix.m12) * invS;
            } else {
                float s = (float) Math.sqrt(1 + m22 - m00 - m11) * 2;
                float invS = 1f / s;

                result.w = (matrix.m01 - matrix.m10) * invS;
                result.x = (matrix.m20 + matrix.m02) * invS;
                result.y = (matrix.m21 + matrix.m12) * invS;
                result.z = s * 0.25f;
            }
        }
    }

    public static Quaternion fromAxisAngle(float angle, Vector3f axis) {
        qtemp.x = 0; qtemp.y = 0; qtemp.z = 0; qtemp.w = 1;
        angle *= 0.5f;
        //axis.normalise(axis);
        float tmpx = axis.x * (float) Math.sin(angle);
        float tmpy = axis.y * (float) Math.sin(angle);
        float tmpz = axis.z * (float) Math.sin(angle);
        qtemp.x = tmpx;
        qtemp.y = tmpy;
        qtemp.z = tmpz;
        qtemp.w = (float) Math.cos(angle);
        qtemp.normalise(qtemp);
        return qtemp;
    }

    public static void fromAxisAngle(Vector3f eulerAngles, Quaternion dest) {
        dest.x = 0;
        dest.y = 0;
        dest.z = 0;
        dest.w = 1; 
        Quaternion rotX = fromAxisAngle(HelperMath.getRadiansFromDegrees(eulerAngles.x), xAxis);
        Quaternion rotY = fromAxisAngle(HelperMath.getRadiansFromDegrees(eulerAngles.y), yAxis);
        Quaternion rotZ = fromAxisAngle(HelperMath.getRadiansFromDegrees(eulerAngles.z), zAxis);
        qtemp.x = 0;
        qtemp.y = 0;
        qtemp.z = 0;
        qtemp.w = 1;
        Quaternion.mul(rotZ, rotY, qtemp);
        Quaternion.mul(qtemp, rotX, dest);
    }

    public static Quaternion fromAxisAngle(float x, float y, float z) {
        temp.x = x;
        temp.y = y;
        temp.z = z;
        Quaternion result = new Quaternion();
        fromAxisAngle(temp, result);
        return result;
    }

    public static Vector3f convertQuaternionToEulerAngles(Quaternion q) {
        Vector3f result = new Vector3f(0, 0, 0);
        // roll (x-axis rotation)
        double sinr = +2.0 * (q.w * q.x + q.y * q.z);
        double cosr = +1.0 - 2.0 * (q.x * q.x + q.y * q.y);
        result.x = (float) Math.atan2(sinr, cosr);

        // pitch (y-axis rotation)
        double sinp = +2.0 * (q.w * q.y - q.z * q.x);
        if (Math.abs(sinp) >= 1) {
            result.y = sinp < 0 ? ((float) Math.PI / 2.0f) * -1.0f : (float) Math.PI / 2.0f;
        } else {
            result.y = (float) Math.asin(sinp);
        }

        // yaw (z-axis rotation)
        double siny = +2.0 * (q.w * q.z + q.x * q.y);
        double cosy = +1.0 - 2.0 * (q.y * q.y + q.z * q.z);
        result.z = (float) Math.atan2(siny, cosy);

        result.x = HelperMath.getDegreesFromRadians(result.x);
        result.y = HelperMath.getDegreesFromRadians(result.y);
        result.z = HelperMath.getDegreesFromRadians(result.z);

        return result;
    }

    public static void transform(Vector4f vec, Matrix4f mat, Vector4f result) {
        Vector4f column0 = new Vector4f(mat.m00, mat.m01, mat.m02, mat.m03);
        Vector4f column1 = new Vector4f(mat.m10, mat.m11, mat.m12, mat.m13);
        Vector4f column2 = new Vector4f(mat.m20, mat.m21, mat.m22, mat.m23);
        Vector4f column3 = new Vector4f(mat.m30, mat.m31, mat.m32, mat.m33);

        temp4.x = Vector4f.dot(vec, column0);
        temp4.y = Vector4f.dot(vec, column1);
        temp4.z = Vector4f.dot(vec, column2);
        temp4.w = Vector4f.dot(vec, column3);

        result.x = temp4.x;
        result.y = temp4.y;
        result.z = temp4.z;
        result.w = temp4.w;
    }

    public static void lerp(Vector3f start, Vector3f end, float percent, Vector3f result) {
        Vector3f endMinusStart = new Vector3f(0, 0, 0);
        Vector3f.sub(end, start, endMinusStart);

        Vector3f.add(start, mul(endMinusStart, percent), result);
    }

    public static void fromEulerAngles(float rotationX, float rotationY, float rotationZ, Quaternion q) {
        rotationX *= 0.5f;
        rotationY *= 0.5f;
        rotationZ *= 0.5f;

        float c1 = (float) Math.cos(rotationX);
        float c2 = (float) Math.cos(rotationY);
        float c3 = (float) Math.cos(rotationZ);
        float s1 = (float) Math.sin(rotationX);
        float s2 = (float) Math.sin(rotationY);
        float s3 = (float) Math.sin(rotationZ);

        q.w = c1 * c2 * c3 - s1 * s2 * s3;
        q.x = s1 * c2 * c3 + c1 * s2 * s3;
        q.y = c1 * s2 * c3 - s1 * c2 * s3;
        q.z = c1 * c2 * s3 + s1 * s2 * c3;
    }

    public static void mulQuaternion(Quaternion quat, Vector3f vec, Vector3f result) {
        Vector3f xyz = new Vector3f(quat.x, quat.y, quat.z);

        Vector3f.cross(xyz, vec, temp);
        Vector3f temp2 = mul(vec, quat.w);
        Vector3f.add(temp, temp2, temp);
        Vector3f.cross(xyz, temp, temp);
        temp = mul(temp, 2);
        Vector3f.add(vec, temp, result);
    }

    public static Vector3f blendVector3(Vector3f v1, Vector3f v2, float blendfactor){
        Vector3f v = new Vector3f();
        lerp(v1, v2, blendfactor, v);
        return v;
    }
    
    public static Quaternion blendRotation(Quaternion q1, Quaternion q2, double blendfactor){
        Quaternion q = new Quaternion();
        slerp2(q1, q2, blendfactor, q);
        return q;
    }
    
    public static void slerp2(Quaternion q1, Quaternion q2, double blend, Quaternion bm) {

        if (q1.lengthSquared() == 0.0f) {
            if (q2.lengthSquared() == 0.0f) {
                bm.x = 0;
                bm.y = 0;
                bm.z = 0;
                bm.w = 1;
                return;
            }
            bm.x = q2.x;
            bm.y = q2.y;
            bm.z = q2.z;
            bm.w = q2.w;
            return;
        } else if (q2.lengthSquared() == 0.0f) {
            bm.x = q1.x;
            bm.y = q1.y;
            bm.z = q1.z;
            bm.w = q1.w;
            return;
        }

        float cosHalfAngle = q1.w * q2.w + Vector3f.dot(new Vector3f(q1.x, q1.y, q1.z), new Vector3f(q2.x, q2.y, q2.z));

        if (cosHalfAngle >= 1.0f || cosHalfAngle <= -1.0f) {
            // angle = 0.0f, so just return one input.
            bm.x = q1.x;
            bm.y = q1.y;
            bm.z = q1.z;
            bm.w = q1.w;
            return;
        } else if (cosHalfAngle < 0.0f) {
            q2.x = -q2.x;
            q2.y = -q2.y;
            q2.z = -q2.z;
            q2.w = -q2.w;
            cosHalfAngle = -cosHalfAngle;
        }

        float blendA;
        float blendB;
        if (cosHalfAngle < 0.99f) {
            // do proper slerp for big angles
            float halfAngle = (float) Math.acos(cosHalfAngle);
            float sinHalfAngle = (float) Math.sin(halfAngle);
            float oneOverSinHalfAngle = 1.0f / sinHalfAngle;
            blendA = (float) Math.sin(halfAngle * (1.0f - blend)) * oneOverSinHalfAngle;
            blendB = (float) Math.sin(halfAngle * blend) * oneOverSinHalfAngle;
        } else {
            // do lerp if angle is really small.
            blendA = 1.0f - (float) blend;
            blendB = (float) blend;
        }

        float v1x = blendA * q1.x;
        float v1y = blendA * q1.y;
        float v1z = blendA * q1.z;

        float v2x = blendB * q2.x;
        float v2y = blendB * q2.y;
        float v2z = blendB * q2.z;

        bm.x = v1x + v2x;
        bm.y = v1y + v2y;
        bm.z = v1z + v2z;
        bm.w = blendA * q1.w + blendB * q2.w;

        if (bm.lengthSquared() > 0.0f) {
            //bm.normalise(bm);
        } else {
            bm.x = 0;
            bm.y = 0;
            bm.z = 0;
            bm.w = 1;
        }
    }

    public static float dot(Quaternion left, Quaternion right) {
        return left.x * right.x + left.y * right.y + left.z * right.z + left.w * right.w;
    }
    
    public static Vector3f rotateVector(Vector3f source, float degrees, RotationAxis axis){
        degrees = HelperMath.getRadiansFromDegrees(degrees);
        
        if(axis == RotationAxis.X)
            HelperMatrix.createRotationX(degrees, tmpMatrix);
        else if(axis == RotationAxis.Y)
        	HelperMatrix.createRotationY(degrees, tmpMatrix);
        else
        	HelperMatrix.createRotationZ(degrees, tmpMatrix);
        
        tmpMatrix.invert();
        
        Vector3f result = new Vector3f();
        result.x = Vector3f.dot(source, new Vector3f(tmpMatrix.m00, tmpMatrix.m10, tmpMatrix.m20));
        result.y = Vector3f.dot(source, new Vector3f(tmpMatrix.m01, tmpMatrix.m11, tmpMatrix.m21));
        result.z = Vector3f.dot(source, new Vector3f(tmpMatrix.m02, tmpMatrix.m12, tmpMatrix.m22));
        return result;
    }
    
    public static Vector3f sub(Vector3f a, Vector3f b)
    {
    	Vector3f result = new Vector3f();
    	Vector3f.sub(a, b, result);
    	return result;
    }
    
    public static void transformNormal(Vector3f src, Matrix4f matrix, Vector3f result) {
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
		
		result.normalise(result);
	}

	public static void transformPosition(Vector3f src, Matrix4f matrix, Vector3f result) {
		_tempVector.x = matrix.m00;
		_tempVector.y = matrix.m01;
		_tempVector.z = matrix.m02;
		result.x = Vector3f.dot(src, _tempVector) + matrix.m03;

		_tempVector.x = matrix.m10;
		_tempVector.y = matrix.m11;
		_tempVector.z = matrix.m12;
		result.y = Vector3f.dot(src, _tempVector) + matrix.m13;

		_tempVector.x = matrix.m20;
		_tempVector.y = matrix.m21;
		_tempVector.z = matrix.m22;
		result.z = Vector3f.dot(src, _tempVector) + matrix.m23;
	}
}
