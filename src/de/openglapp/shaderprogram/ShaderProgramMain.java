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
	// Speicheradressen für das Shader-Programm auf GPU.
	// Das Shader-Programm hat zwei Phasen:
	// - Vertex-Shader (positioniert alle Objekte)
	// - Fragment-Shader (füllt jedes Objekt mit Farbe)
	private static int _programId;
	private static int _vertexShader;
	private static int _fragmentShader;
	
	// Speicheradressen für Parameter, die man an die GPU sendet:
	private static int _uniformModelViewProjectionMatrix = -1;
	private static int _uniformColor = -1;
	private static int _uniformTexture = -1;
	
	private static Matrix4f _currentMVP = new Matrix4f();
	
	
	// Hier wird der in /resource/shaders/ liegende Quellcode für
	// die beiden Shader-Teile (vertex und fragment) eingelesen,
	// an das Hauptprogramm angeheftet und dann kompiliert und verlinkt:
	public static void init()
	{
		_programId = GL45.glCreateProgram();
		_vertexShader = HelperShader.loadAndCompileShader("/shaders/main.vert", GL45.GL_VERTEX_SHADER);
		_fragmentShader = HelperShader.loadAndCompileShader("/shaders/main.frag", GL45.GL_FRAGMENT_SHADER);
		
		GL45.glAttachShader(_programId,  _vertexShader);
		GL45.glAttachShader(_programId,  _fragmentShader);
		
		GL45.glLinkProgram(_programId);
	
		// Hier wird das kompilierte Shader-Programm kontaktiert und 
		// erfragt, wo die Speicherbereiche für die Parameter liegen,
		// die man für jedes Bild neu an die GPU senden muss:
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
		GL45.glUseProgram(_programId); // aktiviert das GPU-Renderprogramm
	}
	
	public static void unbindProgram()
	{
		GL45.glUseProgram(0); // deaktiviert das GPU-Renderprogramm
	}
	
	public static void render(Scene s, Matrix4f viewProjectionMatrix)
	{
		// Zeichnet in einer Schleife alle Objekte der Szene:
		for(GameObject g : s.getObjects())
		{
			renderGameObject(g, viewProjectionMatrix);
		}
	}
	
	private static void renderGameObject(GameObject g, Matrix4f viewProjectionMatrix)
	{
		// Übertrage an die Speicheradresse für die Färbung
		// die Farbe 100% rot, 100% grün und 100% blau:
		GL45.glUniform3f(_uniformColor, 1f, 1f, 1f);
		
		// Erfrage die Model-Matrix des aktuellen Objekts.
		// Die Model-Matrix beinhaltet immer die aktuelle
		// Größe, Rotation und Position des Objekts:
		Matrix4f modelMatrix = g.getModelMatrix();
		
		// Aus der Model-Matrix und der bereits vorher erstellten 
		// View-Projection-Matrix wird durch Multiplikation der beiden
		// Matrizen die kombinierte Model-View-Projection-Matrix generiert:
		Matrix4f.mul(modelMatrix, viewProjectionMatrix, _currentMVP);
		
		// Die Model-View-Projection-Matrix (kurz: MVP) wird dann an die GPU
		// gesendet:
		FloatBuffer mvpAsFloatBuffer = HelperMatrix.genFBuffer(_currentMVP);
		GL45.glUniformMatrix4fv(_uniformModelViewProjectionMatrix, true, mvpAsFloatBuffer);
		
		// Schicke die Textur an die GPU:
		GL45.glActiveTexture(GL45.GL_TEXTURE0);	// Aktiviere Textureinheit 0 (max. 32 gleichzeitig pro render-Call)
		GL45.glBindTexture(GL45.GL_TEXTURE_2D, g.GetTexture());	//Verwende Textur-ID des GameObjects	
		GL45.glUniform1i(_uniformTexture, 0); // Sag der GPU, dass sie die Textur in Einheit 0 ablegen soll
		
		// Sende die Geometrie-Daten (z.B. des Würfels) an die GPU: 
		GL45.glBindVertexArray(Cube.getVAO());
		
		// Sage der GPU, dass sie diese jetzt zeichnen soll:
		GL45.glDrawArrays(GL45.GL_TRIANGLES, 0, Cube.getVertexCount());
		
		// Verwirf die Geometrie-Daten:
		GL45.glBindVertexArray(0);
		
		// Verwirf die Textur für diesen render call (auch draw call genannt):
		GL45.glBindTexture(GL45.GL_TEXTURE_2D, 0);
	}
}
