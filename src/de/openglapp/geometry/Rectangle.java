package de.openglapp.geometry;

public final class Rectangle extends Geometry{

	public Rectangle() throws Exception {
		super();
		
	}
	
	private final static float[] VERTICES = {
        -0.5f, 0.5f, 0f,  // 1st triangle
        -0.5f, -0.5f, 0f, // 1st triangle
        0.5f, 0.5f, 0f,   // 1st triangle
        -0.5f, -0.5f, 0f, // 2nd triangle
        0.5f, -0.5f, 0f,  // 2nd triangle
        0.5f, 0.5f, 0f    // 2nd triangle
	};
	
	private final static float[] NORMALS = {
	        0, 0, 1,
	        0, 0, 1,
	        0, 0, 1,
	        0, 0, 1,
	        0, 0, 1,
	        0, 0, 1
	};
	
	private final static float[] UVS = {
	        0, 1,
	        0, 0,
	        1, 1,
	        0, 0,
	        1, 0,
	        1, 1
	};
	
	
	public static void initRectangle()
	{
		Geometry.init(VERTICES, NORMALS, UVS);
	}
}
