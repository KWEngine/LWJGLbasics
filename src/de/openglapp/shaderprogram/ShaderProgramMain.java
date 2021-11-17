package de.openglapp.shaderprogram;

import java.io.BufferedReader;
import java.io.InputStream;
import java.io.InputStreamReader;

import org.lwjgl.opengl.GL45;

import de.openglapp.helper.HelperShader;
import de.openglapp.scene.GameObject;
import de.openglapp.scene.Scene;

public final class ShaderProgramMain {
	private static int _programId;
	private static int _vertexShader;
	private static int _fragmentShader;
	
	private static int _uniformModelViewProjectionMatrix = -1;
	private static int _uniformColor = -1;
	
	public static void init()
	{
		_programId = GL45.glCreateProgram();
		_vertexShader = HelperShader.loadAndCompileShader("/shaders/main.vert", GL45.GL_VERTEX_SHADER);
		_fragmentShader = HelperShader.loadAndCompileShader("/shaders/main.frag", GL45.GL_FRAGMENT_SHADER);
		
		GL45.glAttachShader(_programId,  _vertexShader);
		GL45.glAttachShader(_programId,  _fragmentShader);
		
		GL45.glLinkProgram(_programId);
		
		_uniformModelViewProjectionMatrix = GL45.glGetUniformLocation(_programId, "uModelViewProjectionMatrix");
		_uniformColor = GL45.glGetUniformLocation(_programId, "uModelViewProjectionMatrix");
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
	
	public static void render(Scene s)
	{
		//GL45.
	
	}
}
