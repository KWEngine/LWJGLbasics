package de.openglapp.scene;

import java.util.ArrayList;

import org.lwjgl.util.vector.Matrix4f;
import org.lwjgl.util.vector.Vector3f;

import de.openglapp.helper.HelperMatrix;
import de.openglapp.helper.Hitbox;

public class Scene {
	private ArrayList<GameObject> _objectList = new ArrayList<>();

	private Matrix4f _viewMatrix = new Matrix4f();
	private Matrix4f _projectionMatrix = new Matrix4f();

	public void updateViewMatrix(float camPosX, float camPosY, float camPosZ, float camTargetX, float camTargetY,
			float camTargetZ) {
		HelperMatrix.lookAt(camPosX, camPosY, camPosZ, camTargetX, camTargetY, camTargetZ, _viewMatrix);
	}

	public void updateProjectionMatrix(int width, int height) {
		// Diese Zeile einkommentieren, wenn eine strikte 2D-Ansicht gewünscht ist.
		// Dort ist dann jede Weltkoordinate = Pixelkoordinate.
		// HelperMatrix.updateOrthographicProjectionMatrix(0, width, height, 0, 0.1f,
		// 1000f, _projectionMatrix);

		// Diese Zeile setzt die Projection-Matrix so, dass ein FOV von 90° gesetzt
		// wird:
		// (Math.PI / 4 = 90°)
		HelperMatrix.perspective((float) Math.PI / 4, width / (float) height, 0.1f, 1000f, _projectionMatrix);
	}

	public void addObject(GameObject g) {
		_objectList.add(g);
	}

	public ArrayList<GameObject> getObjects() {
		return _objectList;
	}

	public void updateViewProjectionMatrix(Matrix4f vpMatrix) {
		// Die View-Projection-Matrix kombiniert die Infos aus View-Matrix (Kamera)
		// und Projection-Matrix (Bildschirmauflösung, usw.) in einer einzigen
		// Matrix:
		HelperMatrix.multiply(_viewMatrix, _projectionMatrix, vpMatrix);
	}

	public ArrayList<Vector3f> getCollisionMTVsFor(GameObject caller) {
		ArrayList<Vector3f> vectorList = new ArrayList<>();

		for (GameObject g : getObjects()) {
			if (g.equals(caller)) {
				continue;
			} else {
				Vector3f currentMTV = new Vector3f();
				boolean hasCollision = Hitbox.doCollisionTest(caller.getHitbox(), g.getHitbox(), currentMTV);
				if(hasCollision)
				{
					vectorList.add(currentMTV);
				}
			}
		}

		return vectorList;
	}
}
