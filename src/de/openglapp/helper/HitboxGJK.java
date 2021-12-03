package de.openglapp.helper;

import java.util.ArrayList;
import java.util.List;

import org.lwjgl.util.vector.*;

public class HitboxGJK {

	private static final Matrix4f _tempMatrix = new Matrix4f();
	private static final Vector3f _tempVector = new Vector3f();
	private static final Matrix4f _normalMatrix = new Matrix4f();
	private static final Vector3f ORIGIN = new Vector3f(0,0,0);
	
	private final Vector3f[] BASEVERTICES;
	private final Vector3f[] BASENORMALS;
	private final Vector3f BASECENTER;

	private Vector3f[] _vertices;
	private Vector3f[] _normals;
	private Vector3f _center;

	public HitboxGJK(float[] vertices, float[] normals, float[] center)
	{
		BASEVERTICES = new Vector3f[vertices.length / 3];
		for(int i = 0, j = 0; i < vertices.length; i+=3, j++)
		{
			BASEVERTICES[j] = new Vector3f(vertices[i], vertices[i+1], vertices[i+2]); 
		}
		
		BASENORMALS = new Vector3f[normals.length / 3];
		for(int i = 0, j = 0; i < normals.length; i+=3, j++)
		{
			BASENORMALS[j] = new Vector3f(normals[i], normals[i+1], normals[i+2]); 
		}
		
		BASECENTER = new Vector3f(center[0], center[1], center[2]);
		
		_vertices = new Vector3f[BASEVERTICES.length];
		for(int i = 0; i < BASEVERTICES.length; i++)
		{
			_vertices[i] = new Vector3f(BASEVERTICES[i].x,BASEVERTICES[i].y,BASEVERTICES[i].z);
		}
		
		_normals = new Vector3f[BASENORMALS.length];
		for(int i = 0; i < BASENORMALS.length; i++)
		{
			_normals[i] = new Vector3f(BASENORMALS[i].x,BASENORMALS[i].y,BASENORMALS[i].z);
		}
		
		_center = new Vector3f();
	}
	
	public void update(Matrix4f modelMatrix) {

		// Um die Normal-Matrix zu berechnen, transponiert man
		// zuerst die model-Matrix und invertiert anschließend
		// das Ergebnis daraus:
		Matrix4f.transpose(modelMatrix, _tempMatrix);
		Matrix4f.invert(_tempMatrix, _normalMatrix);

		for (int i = 0; i < BASEVERTICES.length; i++) {
			if (i < BASENORMALS.length) {
				// transform the normals by multiplying them by the normal matrix:
				// (Hint: you may use the modelMatrix here as well)
				HelperVector.transformNormal(BASENORMALS[i], _normalMatrix, _normals[i]);
			}
			// transform the corner points by multiplying them by the model matrix:
			HelperVector.transformPosition(BASEVERTICES[i], modelMatrix, _vertices[i]);
		}
		// transform the center point by multiplying it by the model matrix:
		HelperVector.transformPosition(BASECENTER, modelMatrix, _center);
	}

	public Vector3f getCenter()
	{
		return _center;
	}
	
	public Vector3f[] getVertices()
	{
		return _vertices;
	}
	
	public Vector3f[] getNormals()
	{
		return _normals;
	}
	
	public static boolean doCollisionTest(HitboxGJK s1, HitboxGJK s2) {
		Vector3f d = HelperVector.sub(s1.getCenter(), s2.getCenter());
		d.normalise();
		
		List<Vector3f> simplex = new ArrayList<Vector3f>();
		simplex.add(support(s1, s2, d));
		
		d = HelperVector.sub(ORIGIN, simplex.get(0));
		while(true)
		{
			Vector3f a = support(s1, s2, d);
			float dot = Vector3f.dot(a, d);
			if(dot < 0)
			{
				return false;
			}
			simplex.add(a);
			if(handleSimplex(simplex, d))
			{
				return true;
			}
		}
	}
	
	private static boolean handleSimplex(List<Vector3f> simplex, Vector3f d)
	{
		if(simplex.size() == 2)
			return lineCase(simplex, d);
		else
			return triangleCase(simplex, d);
	}
	
	private static boolean triangleCase(List<Vector3f> simplex, Vector3f d)
	{
		// A = 0 ?
		// B = 1 ?
		// C = 2 ?
		Vector3f ab = HelperVector.sub(simplex.get(1), simplex.get(0));
		Vector3f ac = HelperVector.sub(simplex.get(2), simplex.get(0));
		Vector3f ao = HelperVector.sub(ORIGIN, simplex.get(0));
		
		Vector3f abPerp = HelperVector.tripleProduct(ab, ab, ab);
		Vector3f acPerp = HelperVector.tripleProduct(ab, ac, ac);
		
		float dotABPerpAo = Vector3f.dot(abPerp, ao);
		float dotACPerpAo = Vector3f.dot(acPerp, ao);
		if(dotABPerpAo > 0)
		{
			simplex.remove(2);
			d.x = abPerp.x;
			d.y = abPerp.y;
			d.z = abPerp.z;
			return false;
		}
		else if(dotACPerpAo > 0)
		{
			simplex.remove(1);
			d.x = acPerp.x;
			d.y = acPerp.y;
			d.z = acPerp.z;
			return false;
		}
		else {
			return true;
		}
		
	}
	
	private static boolean lineCase(List<Vector3f> simplex, Vector3f d)
	{
		Vector3f ab = HelperVector.sub(simplex.get(0), simplex.get(1)); // ? 0 - 1 oder 1 - 0?
		Vector3f ao = HelperVector.sub(ORIGIN, simplex.get(1));
		Vector3f abPerp = HelperVector.tripleProduct(ab, ao, ab);
		
		// d.set(abPerp);
		d.x = abPerp.x;
		d.y = abPerp.y;
		d.z = abPerp.z;
		
		return false;
	}
	
	
	
	private static Vector3f support(HitboxGJK a, HitboxGJK b, Vector3f d)
	{
		Vector3f invertedVector = HelperVector.mul(d, -1);
		Vector3f supportPoint = HelperVector.sub(a.furthestPoint(d), b.furthestPoint(invertedVector));
		
		return supportPoint;
	}

	private Vector3f furthestPoint(Vector3f d)
	{
		int furthestIndex = 0;
		float currentMax = Float.NEGATIVE_INFINITY;
		
		for(int i = 0; i < _vertices.length; i++)
		{
			float dot = Vector3f.dot(d, _vertices[i]);
			if(dot > currentMax)
			{
				currentMax = dot;
				furthestIndex = i;
			}
		}
		return _vertices[furthestIndex];
	}
	
}
