package de.openglapp.window;

import org.lwjgl.*;
import org.lwjgl.glfw.*;
import org.lwjgl.opengl.*;
import org.lwjgl.system.*;
import org.lwjgl.util.vector.Matrix4f;

import de.openglapp.geometry.Cube;
import de.openglapp.helper.HelperTexture;
import de.openglapp.scene.GameObject;
import de.openglapp.scene.Scene;
import de.openglapp.shaderprogram.ShaderProgramMain;

import java.nio.*;

import static org.lwjgl.glfw.Callbacks.*;
import static org.lwjgl.glfw.GLFW.*;
import static org.lwjgl.opengl.GL11.*;
import static org.lwjgl.system.MemoryStack.*;
import static org.lwjgl.system.MemoryUtil.*;

public class GameWindow {

	private long window;
	private int windowWidth;
	private int windowHeight;
	
	// Die View-Projection-Matrix beinhaltet Kameraposition
	// und Bildschirmseitenverhältnis in einer 4x4-Tabelle:
	private Matrix4f _viewProjectionMatrix = new Matrix4f();
	
	public static void main(String[] args) {
		new GameWindow().run();
	}
	
	public void run() {
		System.out.println("Starting LWJGL " + Version.getVersion() + "...");

		// Initialisiere GLFW und OpenGL:
		init();
		
		// Start den Game-Loop:
		loop();

		// Aufräumen:
		glfwFreeCallbacks(window);
		glfwDestroyWindow(window);
		glfwTerminate();
		glfwSetErrorCallback(null).free();
	}
	
	private void init() {
		// Setup an error callback. The default implementation
		// will print the error message in System.err.
		GLFWErrorCallback.createPrint(System.err).set();

		// Initialize GLFW. Most GLFW functions will not work before doing this.
		if ( !glfwInit() )
			throw new IllegalStateException("Unable to initialize GLFW");

		// Configure GL for Windows (GLFW)
		glfwDefaultWindowHints(); // optional, the current window hints are already the default
		glfwWindowHint(GLFW_VISIBLE, GLFW_FALSE); // the window will stay hidden after creation
		glfwWindowHint(GLFW_RESIZABLE, GLFW_FALSE); // the window will not be resizable (much easier to handle!)
		glfwWindowHint(GLFW_CONTEXT_VERSION_MAJOR, 4);
        glfwWindowHint(GLFW_CONTEXT_VERSION_MINOR, 5);

		// Create the window
		window = glfwCreateWindow(1280, 720, "My first 3D game with LWJGL!", NULL, NULL);
		if ( window == NULL )
			throw new RuntimeException("Failed to create the GLFW window");

		// Setup a key callback. It will be called every time a key is pressed, repeated or released.
		glfwSetKeyCallback(window, (window, key, scancode, action, mods) -> {
			if ( key == GLFW_KEY_ESCAPE && action == GLFW_RELEASE )
				glfwSetWindowShouldClose(window, true); // We will detect this in the rendering loop
		});

		// Get the thread stack and push a new frame
		try ( MemoryStack stack = stackPush() ) {
			IntBuffer pWidth = stack.mallocInt(1); // int*
			IntBuffer pHeight = stack.mallocInt(1); // int*

			// Get the window size passed to glfwCreateWindow
			glfwGetWindowSize(window, pWidth, pHeight);
			windowWidth = pWidth.get(0);
			windowHeight = pHeight.get(0);

			// Get the resolution of the primary monitor
			GLFWVidMode vidmode = glfwGetVideoMode(glfwGetPrimaryMonitor());

			// Center the window
			glfwSetWindowPos(
				window,
				(vidmode.width() - pWidth.get(0)) / 2,
				(vidmode.height() - pHeight.get(0)) / 2
			);
		} // the stack frame is popped automatically

		// Make the OpenGL context current
		glfwMakeContextCurrent(window);
		// Enable v-sync
		glfwSwapInterval(1);

		// Make the window visible
		glfwShowWindow(window);
	}

	private void loop() {
		// Erstelle eine interne Liste der von der aktuellen GPU unterstützten
		// OpenGL-Features:
		GL.createCapabilities();

		// Lege grundlegende Eigenschaften für OpenGL fest:
		initBasicGLCommands();
		
		// Erstelle eine Demoszene mit einem GameObject:
		Scene s = initDemoScene();
		
		// Endlosschleife, bis ESCAPE gedrückt wird:
		while ( !glfwWindowShouldClose(window) ) {
			
			// Lösche die Inhalte des Bildschirms, damit das neue
			// Bild gezeichnet werden kann:
			glClear(GL_COLOR_BUFFER_BIT | GL_DEPTH_BUFFER_BIT); 

			// Übergib das Szenen-Objekt an die render-Methode des Shader-Programms:
			ShaderProgramMain.render(s, _viewProjectionMatrix);
			
			// Nach dem Rendern wird die gerenderte Szene an den Monitor gesendet:
			glfwSwapBuffers(window);

			// Horche auf Tastenanschläge: 
			glfwPollEvents();
		}
	}
	
	private void initBasicGLCommands()
	{
		// Setze die "Löschfarbe", mit der das letzte gerenderte Bild überschrieben wird:
		glClearColor(0.0f, 0.0f, 0.0f, 0.0f);
		
		// Aktiviere Textureinheiten:
		// (sollte eigentlich bei modernem OpenGL nicht mehr nötig sein)
		glEnable(GL45.GL_TEXTURE);
		
		// Aktiviere den Tiefenpuffer, der sich merkt,
		// wie weit ein Objekt von der Kamera entfernt ist:
		glEnable(GL45.GL_DEPTH_TEST);
		
		// Erlaube 'back-face-culling':
		// Wenn aktiv, werden Rückseiten von Objekten nicht
		// mehr gerendert, weil sie von der Kamera eh nicht 
		// gesehen werden:
		glEnable(GL45.GL_CULL_FACE);
		glCullFace(GL45.GL_BACK);
		
		// Normalerweise sind Eckpunkt von 3D-Modellen immer 
		// gegen den Uhrzeigersinn (counter-clock-wise) arrangiert:
		// (ACHTUNG: für orthogonale Projektion muss dies auf CW gestellt werden!!)
		glFrontFace(GL45.GL_CCW);
		
		// Gleiche den OpenGL-internen Bildschirmausschnitt an die Fenstergröße an:
		GL45.glViewport(0,  0, windowWidth, windowHeight);
		
		// Lade den Shader-Code und kompiliere und linke ihn:
		ShaderProgramMain.init();
		
		// Teile OpenGL mit, dass dieses Programm ab jetzt benutzt werden soll:
		ShaderProgramMain.bindProgram();
		
		// Initialisiere einen simplen Würfel:
		Cube.init();
		
		// Initialisiere eine weiße Standardtextur (als Backup-Lösung):
		HelperTexture.initDefaultTexture();
		
	}
	
	private Scene initDemoScene()
	{
		Scene s = new Scene();
		// Setze Kameraposition (und Ziel jeweils als x,y,z-Koordinaten:
		s.updateViewMatrix(0, 0, 100, 0, 0, 0);
		
		// Aktualisiere die Projektionsmatrix der Szene:
		s.updateProjectionMatrix(windowWidth, windowHeight);
		
		// Erstelle aus der View-Matrix (Kameraposition) und der 
		// Projektionsmatrix (Bildschirmseitenverhältnis)
		// die kombinierte View-Projection-Matrix:
		s.updateViewProjectionMatrix(_viewProjectionMatrix);
		
		GameObject exampleObject = new GameObject();
		exampleObject.setPosition(0, 0,  0);
		exampleObject.setScale(5, 5,  1);
		exampleObject.updateModelMatrix();
		exampleObject.SetTexture("/textures/stone.jpg");
		
		s.addObject(exampleObject);
		
		return s;
	}
}
