package de.openglapp.scene;

import org.lwjgl.util.vector.Vector3f;

import de.openglapp.helper.HelperMatrix;

import org.lwjgl.util.vector.Quaternion;
import org.lwjgl.util.vector.Matrix4f;

public class GameObject {
	private Vector3f _position = new Vector3f(0,0,0);
	private Quaternion _rotation = new Quaternion(0,0,0,1);
	private Vector3f _scale = new Vector3f(1,1,1);
	
	private Matrix4f _modelMatrix = new Matrix4f();
	
	public void updateModelMatrix()
	{
		HelperMatrix.updateModelMatrix(_scale, _rotation, _position, _modelMatrix);
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
		_scale.x = x;
		_scale.y = y;
		_scale.z = z; // in 2D, z SHOULD be 1 every time!
	}
}
