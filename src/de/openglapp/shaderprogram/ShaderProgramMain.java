package de.openglapp.shaderprogram;

import java.nio.FloatBuffer;

import org.lwjgl.opengl.GL45;
import org.lwjgl.util.vector.Matrix4f;

import de.openglapp.geometry.Cube;
import de.openglapp.helper.HelperMatrix;
import de.openglapp.helper.HelperShader;
import de.openglapp.scene.GameObject;
import de.openglapp.scene.Scene;

public final class ShaderProgramMain {
	private static int _programId;
	private static int _vertexShader;
	private static int _fragmentShader;
	
	private static int _uniformModelViewProjectionMatrix = -1;
	private static int _uniformColor = -1;
	private static int _uniformTexture = -1;
	
	private static Matrix4f _currentMVP = new Matrix4f();
	
	public static void init()
	{
		_programId = GL45.glCreateProgram();
		_vertexShader = HelperShader.loadAndCompileShader("/shaders/main.vert", GL45.GL_VERTEX_SHADER);
		_fragmentShader = HelperShader.loadAndCompileShader("/shaders/main.frag", GL45.GL_FRAGMENT_SHADER);
		
		GL45.glAttachShader(_programId,  _vertexShader);
		GL45.glAttachShader(_programId,  _fragmentShader);
		
		GL45.glLinkProgram(_programId);
		
		_uniformModelViewProjectionMatrix = GL45.glGetUniformLocation(_programId, "uModelViewProjectionMatrix");
		_uniformColor = GL45.glGetUniformLocation(_programId, "uColor");
		_uniformTexture = GL45.glGetUniformLocation(_programId,"uTexture");
	}
	
	public static int getProgramId()
	{
		return _programId;
	}
	
	public static void bindProgram()
	{
		GL45.glUseProgram(_programId);
	}
	
	public static void unbindProgram()
	{
		GL45.glUseProgram(0);
	}
	
	public static void render(Scene s, Matrix4f viewProjectionMatrix)
	{
		for(GameObject g : s.getObjects())
		{
			renderGameObject(g, viewProjectionMatrix);
		}
	
	}
	
	private static void renderGameObject(GameObject g, Matrix4f viewProjectionMatrix)
	{
		// Transmit white as tint color:
		GL45.glUniform3f(_uniformColor, 1f, 1f, 1f);
		
		// Get model matrix (containing position, rotation and scale for current instance):
		Matrix4f modelMatrix = g.getModelMatrix();
		
		// Build the model-view-projection matrix by multiplying the 
		// modelMatrix with the view-projection matrix:
		Matrix4f.mul(modelMatrix, viewProjectionMatrix, _currentMVP);
		
		// Upload model-view-projection matrix to GPU:
		FloatBuffer mvpAsFloatBuffer = HelperMatrix.genFBuffer(_currentMVP);
		GL45.glUniformMatrix4fv(_uniformModelViewProjectionMatrix, true, mvpAsFloatBuffer);
		
		// Upload texture to GPU:
		GL45.glActiveTexture(GL45.GL_TEXTURE0);
		GL45.glBindTexture(GL45.GL_TEXTURE_2D, g.GetTexture());		
		GL45.glUniform1i(_uniformTexture, 0);
		
		GL45.glBindVertexArray(Cube.getVAO());
		
		// Tell GPU to draw the rectangle:
		GL45.glDrawArrays(GL45.GL_TRIANGLES, 0, 6); // 2 triangles = 2 x 3 vertices = 6 vertices total
		
		GL45.glBindVertexArray(0);
		
		// unbind texture from GPU memory:
		GL45.glBindTexture(GL45.GL_TEXTURE_2D, 0);
	}
}
