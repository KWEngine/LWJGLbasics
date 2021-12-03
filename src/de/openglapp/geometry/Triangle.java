package de.openglapp.geometry;

import java.nio.FloatBuffer;

import org.lwjgl.BufferUtils;
import org.lwjgl.opengl.GL45;

public final class Triangle {

	private static int _vao = -1;
	private static int _vboVertices = -1;
	private static int _vboNormals = -1;
	private static int _vboTextureUVs = -1;

	public Triangle() throws Exception {
		throw new Exception("Cube may not be an instance!");
	}

	// Hier werden alle Eckpunkte des Würfels erstellt.
	// Alle Objekte bestehen aus Dreiecken - so dass eine 
	// Würfelseite aus zwei Dreiecken bestehen muss,
	// damit sie ein Viereck wird:
	public final static float[] VERTICES = { 
			// front face
	        0, 0.5f, 0f,
	        -0.5f, -0.5f, 0f,
	        0.5f, -0.5f, 0f
	};

	// Die Normals sind die Richtungen, in die eine Würfelseite zeigt.
	// Normals sind für die Lichtberechnung ungemein wichtig!
	public final static float[] NORMALS = { 
			0, 0, 1, 
			0, 0, 1, 
			0, 0, 1
	};

	// UVs sind Texturkoordinaten und geben an, 
	// wie eine Textur entlang einer Seite aufgespannt
	// werden soll:
	public final static float[] UVS = {
			0.5f, 0,
	        0, 1,
	        1, 0
	};

	public static void init() {
		if (_vao <= 0) {
			
			// Erstelle ein Vertex-Array-Object (VAO) für die Würfeldaten.
			// Ein VAO ist ein Container für einzelne Datenpakete (Eckpunkte, Normals, UVs):
			_vao = GL45.glCreateVertexArrays();
			GL45.glBindVertexArray(_vao);

			// 1.1 Packe die Eckpunkte in einen FloatBuffer:
			FloatBuffer vertexBuffer = BufferUtils.createFloatBuffer(VERTICES.length);
			vertexBuffer.put(VERTICES);
			vertexBuffer.flip();

			// 1.2 Erstelle ein Vertex-Buffer-Object für die Eckpunkte:
			_vboVertices = GL45.glCreateBuffers();
			GL45.glBindBuffer(GL45.GL_ARRAY_BUFFER, _vboVertices);
			GL45.glBufferData(GL45.GL_ARRAY_BUFFER, vertexBuffer, GL45.GL_STATIC_DRAW);
			GL45.glVertexAttribPointer(0, 3, GL45.GL_FLOAT, false, 0, 0);
			GL45.glEnableVertexAttribArray(0); // Packe die Daten in das VAO-Objekt an Position 0:
			GL45.glBindBuffer(GL45.GL_ARRAY_BUFFER, 0);

			// 2.1 Normals:
			FloatBuffer normalBuffer = BufferUtils.createFloatBuffer(NORMALS.length);
			normalBuffer.put(NORMALS);
			normalBuffer.flip();

			// 2.2 
			_vboNormals = GL45.glCreateBuffers();
			GL45.glBindBuffer(GL45.GL_ARRAY_BUFFER, _vboNormals);
			GL45.glBufferData(GL45.GL_ARRAY_BUFFER, normalBuffer, GL45.GL_STATIC_DRAW);
			GL45.glVertexAttribPointer(1, 3, GL45.GL_FLOAT, false, 0, 0);
			GL45.glEnableVertexAttribArray(1);
			GL45.glBindBuffer(GL45.GL_ARRAY_BUFFER, 0);

			// 3.1 UVs
			FloatBuffer uvBuffer = BufferUtils.createFloatBuffer(UVS.length);
			uvBuffer.put(UVS);
			uvBuffer.flip();
			
			// 3.2 
			_vboTextureUVs = GL45.glCreateBuffers();
			GL45.glBindBuffer(GL45.GL_ARRAY_BUFFER, _vboTextureUVs);
			GL45.glBufferData(GL45.GL_ARRAY_BUFFER, uvBuffer, GL45.GL_STATIC_DRAW);
			GL45.glVertexAttribPointer(2, 2, GL45.GL_FLOAT, false, 0, 0);
			GL45.glEnableVertexAttribArray(2);
			GL45.glBindBuffer(GL45.GL_ARRAY_BUFFER, 0);

			GL45.glBindVertexArray(0);
		}
	}

	public static void dispose() {

		// Lösche die Geometrie-Daten (z.B. sollte das bei Programmende aufgerufen werden
		// oder wenn die Geometrie in der aktuellen Welt nicht mehr benötigt wird).
		GL45.glBindBuffer(GL45.GL_ARRAY_BUFFER, 0);
		GL45.glBindVertexArray(0);

		GL45.glDeleteBuffers(_vboVertices);
		GL45.glDeleteBuffers(_vboNormals);
		GL45.glDeleteBuffers(_vboTextureUVs);
		GL45.glDeleteVertexArrays(_vao);
	}

	// Erfragt die OpenGL-ID für das VAO des Würfels:
	public static int getVAO() {
		return _vao;
	}
	
	// Gibt die Anzahl der Eckpunkte des Würfels zurück.
	// (Wichtig für den draw call!)
	public static int getVertexCount()
	{
		return 3;
	}
}
