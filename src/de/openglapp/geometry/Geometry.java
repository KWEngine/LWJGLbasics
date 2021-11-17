package de.openglapp.geometry;

import java.nio.FloatBuffer;
import org.lwjgl.BufferUtils;
import org.lwjgl.opengl.GL45;

public class Geometry {
	private static int _vao = -1;
	private static int _vboVertices = -1;
	private static int _vboNormals = -1;
	private static int _vboTextureUVs = -1;
	
	public Geometry() throws Exception
	{
		throw new Exception("Geometry class may not be instanced.");
	}
	
	public static int getVAO()
	{
		return _vao;
	}
	
	protected static void init(float[] vertices, float[] normals, float[] uvs)
	{
		if(_vao <= 0)
		{
			// Create and bind VAO:
			_vao = GL45.glCreateVertexArrays();
			GL45.glBindVertexArray(_vao);
			
			// 1.1 Generate buffer for vertices:
			FloatBuffer vertexBuffer = BufferUtils.createFloatBuffer(vertices.length);
	        vertexBuffer.put(vertices);
	        vertexBuffer.flip();
	        
	        // 1.2 Create VBO for vertices:
	        _vboVertices = GL45.glCreateBuffers();
	        GL45.glBindBuffer(GL45.GL_ARRAY_BUFFER, _vboVertices);
	        GL45.glBufferData(GL45.GL_ARRAY_BUFFER, vertexBuffer, GL45.GL_STATIC_DRAW);
	        GL45.glVertexAttribPointer(0, 3, GL45.GL_FLOAT, false, 0, 0);
	        GL45.glEnableVertexAttribArray(0);
	        GL45.glBindBuffer(GL45.GL_ARRAY_BUFFER, 0);
	        
	        // 2.1 Generate buffer for normals:
 			FloatBuffer normalBuffer = BufferUtils.createFloatBuffer(normals.length);
 			normalBuffer.put(vertices);
 			normalBuffer.flip();
	        
	        // 2.2 Create VBO for normals:
	        _vboNormals = GL45.glCreateBuffers();
	        GL45.glBindBuffer(GL45.GL_ARRAY_BUFFER, _vboNormals);
	        GL45.glBufferData(GL45.GL_ARRAY_BUFFER, normalBuffer, GL45.GL_STATIC_DRAW);
	        GL45.glVertexAttribPointer(1, 3, GL45.GL_FLOAT, false, 0, 0);
	        GL45.glEnableVertexAttribArray(1);
	        GL45.glBindBuffer(GL45.GL_ARRAY_BUFFER, 0);
	        
	        // 3.1 Generate buffer for UVs:
 			FloatBuffer uvBuffer = BufferUtils.createFloatBuffer(uvs.length);
 			uvBuffer.put(vertices);
 			uvBuffer.flip();
	        
	        // 3.2 Create VBO for UVs (texture coordinates):
	        _vboTextureUVs = GL45.glCreateBuffers();
	        GL45.glBindBuffer(GL45.GL_ARRAY_BUFFER, _vboTextureUVs);
	        GL45.glBufferData(GL45.GL_ARRAY_BUFFER, uvBuffer, GL45.GL_STATIC_DRAW);
	        GL45.glVertexAttribPointer(2, 2, GL45.GL_FLOAT, false, 0, 0);
	        GL45.glEnableVertexAttribArray(2);
	        GL45.glBindBuffer(GL45.GL_ARRAY_BUFFER, 0);
	        
        
	        GL45.glBindVertexArray(0);
	        		}
	}
	
	public static void dispose()
	{
		
		GL45.glBindBuffer(GL45.GL_ARRAY_BUFFER, 0);
		GL45.glBindVertexArray(0);

		GL45.glDeleteBuffers(_vboVertices);
		GL45.glDeleteBuffers(_vboNormals);
		GL45.glDeleteBuffers(_vboTextureUVs);
		GL45.glDeleteVertexArrays(_vao);
	}
}
