package de.openglapp.scene;

import org.lwjgl.util.vector.Vector3f;

import de.openglapp.helper.HelperMath;
import de.openglapp.helper.HelperMatrix;
import de.openglapp.helper.HelperTexture;
import de.openglapp.helper.HelperVector;
import de.openglapp.helper.Hitbox;

import org.lwjgl.util.vector.Quaternion;
import org.lwjgl.util.vector.Matrix4f;

public class GameObject {
	private Vector3f _position = new Vector3f(0,0,0);
	private Quaternion _rotation = new Quaternion(0,0,0,1);
	private Vector3f _scale = new Vector3f(1,1,1);
	private int _textureId = -1;
	private Matrix4f _modelMatrix = new Matrix4f();
	
	private Hitbox _hitbox = new Hitbox();
	
	public Hitbox getHitbox()
	{
		return _hitbox;
	}
	
	// Da am Ende nicht die einzelnen Daten (Position, etc.) des Objekts
	// an die GPU ?bergeben werden, sondern alle Daten in eine 4x4-Matrix
	// gepackt werden, muss man diese Matrix nach jeder ?nderung am Objekt
	// aktualisieren:
	public void updateModelMatrix()
	{
		HelperMatrix.updateModelMatrix(_scale, _rotation, _position, _modelMatrix);
		
		// Wenn sich die Model-Matrix aktualisiert, aktualisiere auch die Hitbox:
		_hitbox.update(_modelMatrix);
	}
	
	public Matrix4f getModelMatrix()
	{
		return _modelMatrix;
	}
	
	public void setPosition(float x, float y, float z)
	{
		_position.x = x;
		_position.y = y;
		_position.z = z;
	}
	
	public void setScale(float x, float y, float z)
	{
		// Hier m?sste man noch Skalierungswerte <= 0 abfangen!
		// Alle Werte m?ssen > 0 sein! ZWINGEND!
		_scale.x = x;
		_scale.y = y;
		_scale.z = z;
	}
	
	public void addRotationX(float amount, boolean absolute)
    {
		Quaternion tmpRotate = HelperVector.fromAxisAngle(HelperMath.getRadiansFromDegrees(amount), HelperVector.UNITX);
        if (absolute)
        {
            Quaternion.mul(tmpRotate, _rotation, _rotation);
        }
        else
        {
        	Quaternion.mul(_rotation, tmpRotate, _rotation);
        }
    }
	
	public void addRotationY(float amount, boolean absolute)
    {
		Quaternion tmpRotate = HelperVector.fromAxisAngle(HelperMath.getRadiansFromDegrees(amount), HelperVector.UNITY);
        if (absolute)
        {
            Quaternion.mul(tmpRotate, _rotation, _rotation);
        }
        else
        {
        	Quaternion.mul(_rotation, tmpRotate, _rotation);
        }
    }
	
	public void addRotationZ(float amount, boolean absolute)
    {
		Quaternion tmpRotate = HelperVector.fromAxisAngle(HelperMath.getRadiansFromDegrees(amount), HelperVector.UNITZ);
        if (absolute)
        {
            Quaternion.mul(tmpRotate, _rotation, _rotation);
        }
        else
        {
        	Quaternion.mul(_rotation, tmpRotate, _rotation);
        }
    }
	
	public void moveOffset(float x, float y, float z)
	{
		_position.x += x;
		_position.y += y;
		_position.z += z;
	}
	
	public void moveOffset(Vector3f amount)
	{
		_position.x += amount.x;
		_position.y += amount.y;
		_position.z += amount.z;
	}
	
	public void SetTexture(String filename)
	{
		_textureId = HelperTexture.importTexture(filename);
	}
	
	public int GetTexture()
	{
		return _textureId;
	}
	
	public void update()
	{
		
	}
}
