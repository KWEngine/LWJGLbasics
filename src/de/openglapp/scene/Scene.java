package de.openglapp.scene;

import java.util.ArrayList;

import org.lwjgl.util.vector.Matrix4f;

import de.openglapp.helper.HelperMatrix;

public class Scene {
	private ArrayList<GameObject> _objectList = new ArrayList<>();
	
	private Matrix4f _viewMatrix = new Matrix4f();
	private Matrix4f _projectionMatrix = new Matrix4f();
	
	public void updateViewMatrix(float camPosX, float camPosY, float camPosZ, float camTargetX, float camTargetY, float camTargetZ)
	{
		HelperMatrix.lookAt(camPosX, camPosY, camPosZ, camTargetX, camTargetY, camTargetZ, _viewMatrix);
	}
	
	public void addObject(GameObject g)
	{
		_objectList.add(g);
	}
	
	public ArrayList<GameObject> getObjects()
	{
		return _objectList;
	}
}
