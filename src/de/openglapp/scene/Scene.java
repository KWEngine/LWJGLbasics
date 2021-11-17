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
	
	public void updateProjectionMatrix(int width, int height)
	{
		HelperMatrix.updateOrthographicProjectionMatrix(0, width, height, 0, 0.1f, 1000f, _projectionMatrix);
		//HelperMatrix.perspective((float)Math.PI / 4, width / (float)height, 0.1f, 1000f, _projectionMatrix);
	}
	
	public void addObject(GameObject g)
	{
		_objectList.add(g);
	}
	
	public ArrayList<GameObject> getObjects()
	{
		return _objectList;
	}
	
	public void updateViewProjectionMatrix(Matrix4f vpMatrix)
	{
		HelperMatrix.multiply(_viewMatrix, _projectionMatrix, vpMatrix);
	}
}
