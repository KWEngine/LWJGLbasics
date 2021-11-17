package de.openglapp.helper;

import java.nio.FloatBuffer;
import org.lwjgl.BufferUtils;
import org.lwjgl.util.vector.Matrix4f;
import org.lwjgl.util.vector.Quaternion;
import org.lwjgl.util.vector.Vector3f;
import org.lwjgl.util.vector.Vector4f;

public final class HelperMatrix {
	
	private static Matrix4f mBiasMatrix = null;
    private final static Matrix4f mTempMatrix = new Matrix4f();
    private final static Matrix4f mRot = new Matrix4f();
    private final static Matrix4f mRotX = new Matrix4f();
    private final static Matrix4f mRotY = new Matrix4f();
    private final static Matrix4f mRotZ = new Matrix4f();
    private final static Matrix4f mTrans = new Matrix4f();
    private final static Vector3f mWorldUp = new Vector3f(0, 1, 0);
    
	public static FloatBuffer genFBuffer(Matrix4f m) {
        FloatBuffer result = BufferUtils.createFloatBuffer(16);
        m.store(result);
        result.flip();
        return result;
    }

    public static Matrix4f multiplyWithScalar(Matrix4f source, float factor){
        Matrix4f result = new Matrix4f();
        result.m00 = source.m00 * factor;
        result.m01 = source.m01 * factor;
        result.m02 = source.m02 * factor;
        result.m03 = source.m03 * factor;
        
        result.m10 = source.m10 * factor;
        result.m11 = source.m11 * factor;
        result.m12 = source.m12 * factor;
        result.m13 = source.m13 * factor;
        
        result.m20 = source.m20 * factor;
        result.m21 = source.m21 * factor;
        result.m22 = source.m22 * factor;
        result.m23 = source.m23 * factor;
        
        result.m30 = source.m30 * factor;
        result.m31 = source.m31 * factor;
        result.m32 = source.m32 * factor;
        result.m33 = source.m33 * factor;
        
        return result;
    }
    
    public static void rotationFromQuaternion(Quaternion q, Matrix4f dest) {
        Vector4f axisAngle = toAxisAngle(q);
        float angle = axisAngle.w;
        // normalize and create a local copy of the vector.
        Vector3f axis = new Vector3f(axisAngle.x, axisAngle.y, axisAngle.z);
        axis.normalise();
        float axisX = axis.x, axisY = axis.y, axisZ = axis.z;

        // calculate angles
        float cos = (float) Math.cos(-angle);
        float sin = (float) Math.sin(-angle);
        float t = 1.0f - cos;

        // do the conversion math once
        float tXX = t * axisX * axisX,
                tXY = t * axisX * axisY,
                tXZ = t * axisX * axisZ,
                tYY = t * axisY * axisY,
                tYZ = t * axisY * axisZ,
                tZZ = t * axisZ * axisZ;

        float sinX = sin * axisX,
                sinY = sin * axisY,
                sinZ = sin * axisZ;

        dest.m00 = tXX + cos;
        dest.m10 = tXY - sinZ;
        dest.m20 = tXZ + sinY;
        dest.m30 = 0;

        dest.m01 = tXY + sinZ;
        dest.m11 = tYY + cos;
        dest.m21 = tYZ - sinX;
        dest.m31 = 0;

        dest.m02 = tXZ - sinY;
        dest.m12 = tYZ + sinX;
        dest.m22 = tZZ + cos;
        dest.m32 = 0;

        dest.m03 = 0;
        dest.m13 = 0;
        dest.m23 = 0;
        dest.m33 = 1;
    }
    
    public static void updateModelMatrix(Vector3f scale, Quaternion rotation, Vector3f position, Matrix4f m)
    {
    	HelperMatrix.createModelMatrixFromQuaternion(rotation, m);

    	// first row:
    	m.m00 = m.m00 * scale.x;
        m.m10 = m.m10 * scale.x;
        m.m20 = m.m20 * scale.x;
        m.m30 = m.m30 * scale.x;

        // second row:
        m.m01 = m.m01 * scale.y;
        m.m11 = m.m11 * scale.y;
        m.m21 = m.m21 * scale.y;
        m.m31 = m.m31 * scale.x;

        // third row:
        m.m02 = m.m02 * scale.z;
        m.m12 = m.m12 * scale.z;
        m.m22 = m.m22 * scale.z;
        m.m32 = m.m32 * scale.z;

        // fourth row:
        m.m03 = position.x;
        m.m13 = position.y;
        m.m23 = position.z;
        m.m33 = 1;
    }

    public static Matrix4f rotationFromQuaternion(Quaternion q) {
        if (Math.abs(q.w) > 1.0f) {
            q.normalise();
        }

        Vector3f axis = new Vector3f();

        float angle = 2.0f * (float) Math.acos(q.w); // angle
        float den = (float) Math.sqrt(1.0 - q.w * q.w);
        if (den > 0.0001f) {
            float mult = 1.0f / den;
            axis.x = q.x * mult;
            axis.y = q.y * mult;
            axis.z = q.z * mult;
        } else {
            // This occurs when the angle is zero. 
            // Not a problem: just set an arbitrary normalized axis.
            axis.x = 1;
            axis.y = 0;
            axis.z = 0;
        }

        axis.normalise();

        // =====================================
        float cos = (float) Math.cos(-angle);
        float sin = (float) Math.sin(-angle);
        float t = 1.0f - cos;

        Matrix4f result = new Matrix4f();
        result.m00 = t * axis.x * axis.x + cos;
        result.m10 = t * axis.x * axis.y - sin * axis.z;
        result.m20 = t * axis.x * axis.z + sin * axis.y;
        result.m30 = 0;

        result.m01 = t * axis.x * axis.y + sin * axis.z;
        result.m11 = t * axis.y * axis.y + cos;
        result.m21 = t * axis.y * axis.z - sin * axis.x;
        result.m31 = 0;

        result.m02 = t * axis.x * axis.z - sin * axis.y;
        result.m12 = t * axis.y * axis.z + sin * axis.x;
        result.m22 = t * axis.z * axis.z + cos;
        result.m32 = 0;

        result.m03 = 0;
        result.m13 = 0;
        result.m23 = 0;
        result.m33 = 1;

        return result;
    }
    
    public static void createModelMatrixFromQuaternion(Quaternion q, Matrix4f result) {
        if (Math.abs(q.w) > 1.0f) {
            q.normalise();
        }

        Vector3f axis = new Vector3f();

        float angle = 2.0f * (float) Math.acos(q.w); // angle
        float den = (float) Math.sqrt(1.0 - q.w * q.w);
        if (den > 0.0001f) {
            float mult = 1.0f / den;
            axis.x = q.x * mult;
            axis.y = q.y * mult;
            axis.z = q.z * mult;
        } else {
            // This occurs when the angle is zero. 
            // Not a problem: just set an arbitrary normalized axis.
            axis.x = 1;
            axis.y = 0;
            axis.z = 0;
        }

        axis.normalise();

        // =====================================
        float cos = (float) Math.cos(-angle);
        float sin = (float) Math.sin(-angle);
        float t = 1.0f - cos;

        result.m00 = t * axis.x * axis.x + cos;
        result.m10 = t * axis.x * axis.y - sin * axis.z;
        result.m20 = t * axis.x * axis.z + sin * axis.y;
        result.m30 = 0;

        result.m01 = t * axis.x * axis.y + sin * axis.z;
        result.m11 = t * axis.y * axis.y + cos;
        result.m21 = t * axis.y * axis.z - sin * axis.x;
        result.m31 = 0;

        result.m02 = t * axis.x * axis.z - sin * axis.y;
        result.m12 = t * axis.y * axis.z + sin * axis.x;
        result.m22 = t * axis.z * axis.z + cos;
        result.m32 = 0;

        result.m03 = 0;
        result.m13 = 0;
        result.m23 = 0;
        result.m33 = 1;
    }

    private static Vector4f toAxisAngle(Quaternion q) {
        if (Math.abs(q.w) > 1.0f) {
            q.normalise();
        }
        Vector4f result = new Vector4f();
        result.w = 2.0f * (float) Math.acos(q.w); // angle
        float den = (float) Math.sqrt(1.0 - q.w * q.w);
        if (den > 0.0001f) {
            Vector3f v = new Vector3f(q.x, q.y, q.z);
            HelperVector.mul(1 / den, v);
            result.x = q.x;
            result.y = q.y;
            result.z = q.z;
        } else {
            result.x = 1;
            result.y = 0;
            result.z = 0;
        }
        return result;
    }

    public static void rotateAndTranslate(Quaternion rotation, Vector3f translation, Matrix4f source, Matrix4f result) {
        HelperMatrix.rotationFromQuaternion(rotation, mRot);
        HelperMatrix.createTranslation(translation, mTrans);
        HelperMatrix.multiply(source, mRot, mTempMatrix);
        HelperMatrix.multiply(mTempMatrix, mTrans, result);
    }

    public static void rotateAndTranslate(Vector3f rotation, Vector3f translation, Matrix4f source, Matrix4f result) {

    	HelperMatrix.createRotationZ(HelperMath.getRadiansFromDegrees(rotation.z), mRotZ);
    	HelperMatrix.createRotationY(HelperMath.getRadiansFromDegrees(rotation.y), mRotY);
    	HelperMatrix.createRotationX(HelperMath.getRadiansFromDegrees(rotation.x), mRotX);
    	HelperMatrix.createTranslation(translation, mTrans);

    	HelperMatrix.multiply(source, mRotZ, mTempMatrix);
    	HelperMatrix.multiply(mTempMatrix, mRotY, mTempMatrix);
    	HelperMatrix.multiply(mTempMatrix, mRotX, mTempMatrix);
    	HelperMatrix.multiply(mTempMatrix, mTrans, result);
    }

    public static void createRotationXYZ(Vector3f rotation, Matrix4f result) {
        Matrix4f source = new Matrix4f();
        HelperMatrix.createRotationZ(HelperMath.getRadiansFromDegrees(rotation.z), mRotZ);
        HelperMatrix.createRotationY(HelperMath.getRadiansFromDegrees(rotation.y), mRotY);
        HelperMatrix.createRotationX(HelperMath.getRadiansFromDegrees(rotation.x), mRotX);

        HelperMatrix.multiply(source, mRotZ, mTempMatrix);
        HelperMatrix.multiply(mTempMatrix, mRotY, mTempMatrix);
        HelperMatrix.multiply(mTempMatrix, mRotX, result);
    }

    public static void multiply(Matrix4f left, Matrix4f right, Matrix4f result) {
        float lM11 = left.m00, lM12 = left.m10, lM13 = left.m20, lM14 = left.m30,
                lM21 = left.m01, lM22 = left.m11, lM23 = left.m21, lM24 = left.m31,
                lM31 = left.m02, lM32 = left.m12, lM33 = left.m22, lM34 = left.m32,
                lM41 = left.m03, lM42 = left.m13, lM43 = left.m23, lM44 = left.m33,
                rM11 = right.m00, rM12 = right.m10, rM13 = right.m20, rM14 = right.m30,
                rM21 = right.m01, rM22 = right.m11, rM23 = right.m21, rM24 = right.m31,
                rM31 = right.m02, rM32 = right.m12, rM33 = right.m22, rM34 = right.m32,
                rM41 = right.m03, rM42 = right.m13, rM43 = right.m23, rM44 = right.m33;

        result.m00 = (((lM11 * rM11) + (lM12 * rM21)) + (lM13 * rM31)) + (lM14 * rM41);
        result.m10 = (((lM11 * rM12) + (lM12 * rM22)) + (lM13 * rM32)) + (lM14 * rM42);
        result.m20 = (((lM11 * rM13) + (lM12 * rM23)) + (lM13 * rM33)) + (lM14 * rM43);
        result.m30 = (((lM11 * rM14) + (lM12 * rM24)) + (lM13 * rM34)) + (lM14 * rM44);
        result.m01 = (((lM21 * rM11) + (lM22 * rM21)) + (lM23 * rM31)) + (lM24 * rM41);
        result.m11 = (((lM21 * rM12) + (lM22 * rM22)) + (lM23 * rM32)) + (lM24 * rM42);
        result.m21 = (((lM21 * rM13) + (lM22 * rM23)) + (lM23 * rM33)) + (lM24 * rM43);
        result.m31 = (((lM21 * rM14) + (lM22 * rM24)) + (lM23 * rM34)) + (lM24 * rM44);
        result.m02 = (((lM31 * rM11) + (lM32 * rM21)) + (lM33 * rM31)) + (lM34 * rM41);
        result.m12 = (((lM31 * rM12) + (lM32 * rM22)) + (lM33 * rM32)) + (lM34 * rM42);
        result.m22 = (((lM31 * rM13) + (lM32 * rM23)) + (lM33 * rM33)) + (lM34 * rM43);
        result.m32 = (((lM31 * rM14) + (lM32 * rM24)) + (lM33 * rM34)) + (lM34 * rM44);
        result.m03 = (((lM41 * rM11) + (lM42 * rM21)) + (lM43 * rM31)) + (lM44 * rM41);
        result.m13 = (((lM41 * rM12) + (lM42 * rM22)) + (lM43 * rM32)) + (lM44 * rM42);
        result.m23 = (((lM41 * rM13) + (lM42 * rM23)) + (lM43 * rM33)) + (lM44 * rM43);
        result.m33 = (((lM41 * rM14) + (lM42 * rM24)) + (lM43 * rM34)) + (lM44 * rM44);
    }

    public static Matrix4f orthographic(float fovX, float fovY, float near, float far) {
        Matrix4f result = new Matrix4f();
        createOrthographicOffCenter(-fovX / 2, fovX / 2, -fovY / 2, fovY / 2, near, far, result);
        return result;
    }

    private static void createOrthographicOffCenter(float left, float right, float bottom, float top, float near, float far, Matrix4f result) {

        float invRL = 1 / (right - left);
        float invTB = 1 / (top - bottom);
        float invFN = 1 / (far - near);

        result.m00 = 2 * invRL;
        result.m11 = 2 * invTB;
        result.m22 = -2 * invFN;

        result.m03 = -(right + left) * invRL;
        result.m13 = -(top + bottom) * invTB;
        result.m23 = -(far + near) * invFN;
        result.m33 = 1;
    }

    private static Matrix4f createPerspectiveOffCenter(float left, float right, float bottom, float top, float zNear, float zFar) {
        Matrix4f result = new Matrix4f();

        float x = (2.0f * zNear) / (right - left);
        float y = (2.0f * zNear) / (top - bottom);
        float a = (right + left) / (right - left);
        float b = (top + bottom) / (top - bottom);
        float c = -(zFar + zNear) / (zFar - zNear);
        float d = -(2.0f * zFar * zNear) / (zFar - zNear);
        result.m00 = x;
        result.m10 = 0;
        result.m20 = 0;
        result.m30 = 0;

        result.m01 = 0;
        result.m11 = y;
        result.m21 = 0;
        result.m31 = 0;

        result.m02 = a;
        result.m12 = b;
        result.m22 = c;
        result.m32 = -1;

        result.m03 = 0;
        result.m13 = 0;
        result.m23 = d;
        result.m33 = 0;

        return result;
    }

    public static Matrix4f perspective(float fov, float widthHeightAspect, float near, float far) {
        if (fov > 0 && fov <= Math.PI) {
            float yMax = near * (float) Math.tan(0.5f * fov);
            float yMin = -yMax;
            float xMin = yMin * widthHeightAspect;
            float xMax = yMax * widthHeightAspect;
            return createPerspectiveOffCenter(xMin, xMax, yMin, yMax, near, far);
        }
        return new Matrix4f();
    }

    public static void lookAt(Vector3f eye, Vector3f target, Vector3f up, Matrix4f result) {
        Vector3f z = new Vector3f();
        Vector3f.sub(eye, target, z);
        z.normalise();

        Vector3f x = new Vector3f();
        Vector3f.cross(up, z, x);
        x.normalise();

        Vector3f y = new Vector3f();
        Vector3f.cross(z, x, y);
        y.normalise();

        mRot.m00 = x.x;
        mRot.m10 = y.x;
        mRot.m20 = z.x;
        mRot.m30 = 0;

        mRot.m01 = x.y;
        mRot.m11 = y.y;
        mRot.m21 = z.y;
        mRot.m31 = 0;

        mRot.m02 = x.z;
        mRot.m12 = y.z;
        mRot.m22 = z.z;
        mRot.m32 = 0;

        mRot.m03 = 0;
        mRot.m13 = 0;
        mRot.m23 = 0;
        mRot.m33 = 1;

        eye = HelperVector.mul(eye, -1);
        createTranslation(eye, mTrans);
        multiply(mTrans, mRot, result);
    }
    
    public static void lookAt(float eyeX, float eyeY, float eyeZ, float targetX, float targetY, float targetZ, Matrix4f result) {
    	Vector3f eye = new Vector3f(eyeX, eyeY, eyeZ);
    	
    	Vector3f z = new Vector3f(eyeX - targetX, eyeY - targetY, eyeZ - targetZ);
        z.normalise();

        Vector3f x = new Vector3f();
        Vector3f.cross(mWorldUp, z, x);
        x.normalise();

        Vector3f y = new Vector3f();
        Vector3f.cross(z, x, y);
        y.normalise();

        mRot.m00 = x.x;
        mRot.m10 = y.x;
        mRot.m20 = z.x;
        mRot.m30 = 0;

        mRot.m01 = x.y;
        mRot.m11 = y.y;
        mRot.m21 = z.y;
        mRot.m31 = 0;

        mRot.m02 = x.z;
        mRot.m12 = y.z;
        mRot.m22 = z.z;
        mRot.m32 = 0;

        mRot.m03 = 0;
        mRot.m13 = 0;
        mRot.m23 = 0;
        mRot.m33 = 1;

        eye = HelperVector.mul(eye, -1);
        createTranslation(eye, mTrans);
        multiply(mTrans, mRot, result);
    }

    public static void createScale(Vector3f scale, Matrix4f dest) {
        dest.m00 = scale.x;
        dest.m10 = 0;
        dest.m20 = 0;
        dest.m30 = 0;

        dest.m01 = 0;
        dest.m11 = scale.y;
        dest.m21 = 0;
        dest.m31 = 0;

        dest.m02 = 0;
        dest.m12 = 0;
        dest.m22 = scale.z;
        dest.m32 = 0;

        dest.m03 = 0;
        dest.m13 = 0;
        dest.m23 = 0;
        dest.m33 = 1;
    }

    public static void createRotationX(float angle, Matrix4f dest) {
        float cos = (float) Math.cos(angle);
        float sin = (float) Math.sin(angle);

        dest.m00 = 1;
        dest.m10 = 0;
        dest.m20 = 0;
        dest.m30 = 0;

        dest.m01 = 0;
        dest.m11 = cos;
        dest.m21 = sin;
        dest.m31 = 0;

        dest.m02 = 0;
        dest.m12 = -sin;
        dest.m22 = cos;
        dest.m32 = 0;

        dest.m03 = 0;
        dest.m13 = 0;
        dest.m23 = 0;
        dest.m33 = 1;
    }

    public static void createRotationY(float angle, Matrix4f dest) {
        float cos = (float) Math.cos(angle);
        float sin = (float) Math.sin(angle);

        dest.m00 = cos;
        dest.m10 = 0;
        dest.m20 = -sin;
        dest.m30 = 0;

        dest.m01 = 0;
        dest.m11 = 1;
        dest.m21 = 0;
        dest.m31 = 0;

        dest.m02 = sin;
        dest.m12 = 0;
        dest.m22 = cos;
        dest.m32 = 0;

        dest.m03 = 0;
        dest.m13 = 0;
        dest.m23 = 0;
        dest.m33 = 1;
    }

    public static void createRotationZ(float angle, Matrix4f dest) {
        float cos = (float) Math.cos(angle);
        float sin = (float) Math.sin(angle);

        dest.m00 = cos;
        dest.m10 = sin;
        dest.m20 = 0;
        dest.m30 = 0;

        dest.m01 = -sin;
        dest.m11 = cos;
        dest.m21 = 0;
        dest.m31 = 0;

        dest.m02 = 0;
        dest.m12 = 0;
        dest.m22 = 1;
        dest.m32 = 0;

        dest.m03 = 0;
        dest.m13 = 0;
        dest.m23 = 0;
        dest.m33 = 1;
    }

    public static void createTranslation(Vector3f trans, Matrix4f dest) {

        dest.setIdentity();
        dest.m03 = trans.x;
        dest.m13 = trans.y;
        dest.m23 = trans.z;
        dest.m33 = 1;
    }

    public static Matrix4f getBiasedMatrixForShadowMapping() {
        if (mBiasMatrix == null) {
            mBiasMatrix = new Matrix4f();
            mBiasMatrix.m00 = 0.5f;
            mBiasMatrix.m01 = 0.0f;
            mBiasMatrix.m02 = 0.0f;
            mBiasMatrix.m03 = 0.5f;

            mBiasMatrix.m10 = 0.0f;
            mBiasMatrix.m11 = 0.5f;
            mBiasMatrix.m12 = 0.0f;
            mBiasMatrix.m13 = 0.5f;

            mBiasMatrix.m20 = 0.0f;
            mBiasMatrix.m21 = 0.0f;
            mBiasMatrix.m22 = 0.5f;
            mBiasMatrix.m23 = 0.5f;

            mBiasMatrix.m30 = 0.0f;
            mBiasMatrix.m31 = 0.0f;
            mBiasMatrix.m32 = 0.0f;
            mBiasMatrix.m33 = 1.0f;
        }

        return mBiasMatrix;
    }

    public static void transform(Vector4f vec, Matrix4f mat, Vector4f result) {
        result.x = vec.x * mat.m00 + vec.y * mat.m01 + vec.z * mat.m02 + vec.w * mat.m03;
        result.y = vec.x * mat.m10 + vec.y * mat.m11 + vec.z * mat.m12 + vec.w * mat.m13;
        result.z = vec.x * mat.m20 + vec.y * mat.m21 + vec.z * mat.m22 + vec.w * mat.m23;
        result.w = vec.x * mat.m23 + vec.y * mat.m23 + vec.z * mat.m32 + vec.w * mat.m33;
    }

    public static void transformPosition(Vector3f pos, Matrix4f mat, Vector3f result) {
        result.x = pos.x * mat.m00 + pos.y * mat.m01 + pos.z * mat.m02 + mat.m03;
        result.y = pos.x * mat.m10 + pos.y * mat.m11 + pos.z * mat.m12 + mat.m13;
        result.z = pos.x * mat.m20 + pos.y * mat.m21 + pos.z * mat.m22 + mat.m23;
    }
}
