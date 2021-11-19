package de.openglapp.window;

import org.lwjgl.*;
import org.lwjgl.glfw.*;
import org.lwjgl.opengl.*;
import org.lwjgl.system.*;
import org.lwjgl.util.vector.Matrix4f;

import de.openglapp.geometry.Cube;
import de.openglapp.helper.HelperTexture;
import de.openglapp.scene.GameObject;
import de.openglapp.scene.Obstacle;
import de.openglapp.scene.Player;
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
	
	public static GameWindow CURRENTWINDOW;
	public static Scene CURRENTSCENE;
	
	public static void main(String[] args) {
		CURRENTWINDOW = new GameWindow();
		CURRENTWINDOW.run();
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

		// Interne Speicherallokation für das Win32-Fenster:
		try ( MemoryStack stack = stackPush() ) {
			IntBuffer pWidth = stack.mallocInt(1); // int*
			IntBuffer pHeight = stack.mallocInt(1); // int*

			// Auflösung des Fensters erfragen:
			glfwGetWindowSize(window, pWidth, pHeight);
			windowWidth = pWidth.get(0);
			windowHeight = pHeight.get(0);

			// Monitorauflösung erfragen:
			GLFWVidMode vidmode = glfwGetVideoMode(glfwGetPrimaryMonitor());

			// Fenster auf dem Bildschirm zentrieren:
			glfwSetWindowPos(
				window,
				(vidmode.width() - pWidth.get(0)) / 2,
				(vidmode.height() - pHeight.get(0)) / 2
			);
		} // the stack frame is popped automatically

		// In der Regel gibt es in OpenGL nur einen 'Kontext',
		// in dem Textur-IDs usw. gespeichert werden. Dieser
		// wird hier aktiviert:
		glfwMakeContextCurrent(window);
		
		// V-Sync aktivieren/deaktivieren:
		glfwSwapInterval(1);

		// Fenster sichtbar machen:
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
		CURRENTSCENE = s;
		
		// Endlosschleife, bis ESCAPE gedrückt wird:
		while ( !glfwWindowShouldClose(window) ) {
			
			// Lösche die Inhalte des Bildschirms, damit das neue
			// Bild gezeichnet werden kann:
			glClear(GL_COLOR_BUFFER_BIT | GL_DEPTH_BUFFER_BIT); 

			for(GameObject g : s.getObjects())
			{
				g.update();
			}
			
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
		// Setze Kameraposition (und Ziel jeweils als x,y,z-Koordinaten):
		s.updateViewMatrix(3, 3, 3, 0, 0, 0);
		
		// Aktualisiere die Projektionsmatrix der Szene:
		s.updateProjectionMatrix(windowWidth, windowHeight);
		
		// Erstelle aus der View-Matrix (Kameraposition) und der 
		// Projektionsmatrix (Bildschirmseitenverhältnis)
		// die kombinierte View-Projection-Matrix:
		s.updateViewProjectionMatrix(_viewProjectionMatrix);
		
		Player playerObject = new Player();
		playerObject.setPosition(-2, 0,  0);
		playerObject.setScale(1, 1, 1);
		playerObject.updateModelMatrix();
		playerObject.SetTexture("/textures/stone.jpg");
		s.addObject(playerObject);
		
		Obstacle obstacleObject = new Obstacle();
		obstacleObject.setPosition(3, 0,  0);
		obstacleObject.setScale(1, 1, 1);
		obstacleObject.updateModelMatrix();
		obstacleObject.SetTexture("/textures/wall.png");
		s.addObject(obstacleObject);
		
		return s;
	}
	
	public boolean isKeyDown(String keyName) {
        if (keyName == null || keyName.trim().length() < 1) {
            return false;
        }
        keyName = keyName.toUpperCase().trim();
        switch (keyName) {
            case "A":
                return glfwGetKey(window, GLFW_KEY_A) == GLFW_PRESS;
            case "B":
                return glfwGetKey(window, GLFW_KEY_B) == GLFW_PRESS;
            case "C":
                return glfwGetKey(window, GLFW_KEY_C) == GLFW_PRESS;
            case "D":
                return glfwGetKey(window, GLFW_KEY_D) == GLFW_PRESS;
            case "E":
                return glfwGetKey(window, GLFW_KEY_E) == GLFW_PRESS;
            case "F":
                return glfwGetKey(window, GLFW_KEY_F) == GLFW_PRESS;
            case "G":
                return glfwGetKey(window, GLFW_KEY_G) == GLFW_PRESS;
            case "H":
                return glfwGetKey(window, GLFW_KEY_H) == GLFW_PRESS;
            case "I":
                return glfwGetKey(window, GLFW_KEY_I) == GLFW_PRESS;
            case "J":
                return glfwGetKey(window, GLFW_KEY_J) == GLFW_PRESS;
            case "K":
                return glfwGetKey(window, GLFW_KEY_K) == GLFW_PRESS;
            case "L":
                return glfwGetKey(window, GLFW_KEY_L) == GLFW_PRESS;
            case "M":
                return glfwGetKey(window, GLFW_KEY_M) == GLFW_PRESS;
            case "N":
                return glfwGetKey(window, GLFW_KEY_N) == GLFW_PRESS;
            case "O":
                return glfwGetKey(window, GLFW_KEY_O) == GLFW_PRESS;
            case "P":
                return glfwGetKey(window, GLFW_KEY_P) == GLFW_PRESS;
            case "Q":
                return glfwGetKey(window, GLFW_KEY_Q) == GLFW_PRESS;
            case "R":
                return glfwGetKey(window, GLFW_KEY_R) == GLFW_PRESS;
            case "S":
                return glfwGetKey(window, GLFW_KEY_S) == GLFW_PRESS;
            case "T":
                return glfwGetKey(window, GLFW_KEY_T) == GLFW_PRESS;
            case "U":
                return glfwGetKey(window, GLFW_KEY_U) == GLFW_PRESS;
            case "V":
                return glfwGetKey(window, GLFW_KEY_V) == GLFW_PRESS;
            case "W":
                return glfwGetKey(window, GLFW_KEY_W) == GLFW_PRESS;
            case "X":
                return glfwGetKey(window, GLFW_KEY_X) == GLFW_PRESS;
            case "Y":
                return glfwGetKey(window, GLFW_KEY_Z) == GLFW_PRESS;
            case "Z":
                return glfwGetKey(window, GLFW_KEY_Y) == GLFW_PRESS;
            case "1":
                return glfwGetKey(window, GLFW_KEY_1) == GLFW_PRESS;
            case "2":
                return glfwGetKey(window, GLFW_KEY_2) == GLFW_PRESS;
            case "3":
                return glfwGetKey(window, GLFW_KEY_3) == GLFW_PRESS;
            case "4":
                return glfwGetKey(window, GLFW_KEY_4) == GLFW_PRESS;
            case "5":
                return glfwGetKey(window, GLFW_KEY_5) == GLFW_PRESS;
            case "6":
                return glfwGetKey(window, GLFW_KEY_6) == GLFW_PRESS;
            case "7":
                return glfwGetKey(window, GLFW_KEY_7) == GLFW_PRESS;
            case "8":
                return glfwGetKey(window, GLFW_KEY_8) == GLFW_PRESS;
            case "9":
                return glfwGetKey(window, GLFW_KEY_9) == GLFW_PRESS;
            case "0":
                return glfwGetKey(window, GLFW_KEY_0) == GLFW_PRESS;
            case "KP1":
                return glfwGetKey(window, GLFW_KEY_KP_1) == GLFW_PRESS;
            case "KP2":
                return glfwGetKey(window, GLFW_KEY_KP_2) == GLFW_PRESS;
            case "KP3":
                return glfwGetKey(window, GLFW_KEY_KP_3) == GLFW_PRESS;
            case "KP4":
                return glfwGetKey(window, GLFW_KEY_KP_4) == GLFW_PRESS;
            case "KP5":
                return glfwGetKey(window, GLFW_KEY_KP_5) == GLFW_PRESS;
            case "KP6":
                return glfwGetKey(window, GLFW_KEY_KP_6) == GLFW_PRESS;
            case "KP7":
                return glfwGetKey(window, GLFW_KEY_KP_7) == GLFW_PRESS;
            case "KP8":
                return glfwGetKey(window, GLFW_KEY_KP_8) == GLFW_PRESS;
            case "KP9":
                return glfwGetKey(window, GLFW_KEY_KP_9) == GLFW_PRESS;
            case "KP0":
                return glfwGetKey(window, GLFW_KEY_KP_0) == GLFW_PRESS;
            case "ALTLEFT":
                return glfwGetKey(window, GLFW_KEY_LEFT_ALT) == GLFW_PRESS;
            case "ALTRIGHT":
                return glfwGetKey(window, GLFW_KEY_RIGHT_ALT) == GLFW_PRESS;
            case "CTRLLEFT":
                return glfwGetKey(window, GLFW_KEY_LEFT_CONTROL) == GLFW_PRESS;
            case "CTRLRIGHT":
                return glfwGetKey(window, GLFW_KEY_RIGHT_CONTROL) == GLFW_PRESS;
            case "SHIFTLEFT":
                return glfwGetKey(window, GLFW_KEY_LEFT_SHIFT) == GLFW_PRESS;
            case "SHIFTRIGHT":
                return glfwGetKey(window, GLFW_KEY_RIGHT_SHIFT) == GLFW_PRESS;
            case "SPACE":
                return glfwGetKey(window, GLFW_KEY_SPACE) == GLFW_PRESS;
            case "ENTER":
                return glfwGetKey(window, GLFW_KEY_ENTER) == GLFW_PRESS;
            case "KPENTER":
                return glfwGetKey(window, GLFW_KEY_KP_ENTER) == GLFW_PRESS;
            case "KPPLUS":
                return glfwGetKey(window, GLFW_KEY_KP_ADD) == GLFW_PRESS;
            case "KPMINUS":
                return glfwGetKey(window, GLFW_KEY_KP_SUBTRACT) == GLFW_PRESS;
            case "KPMULTIPLY":
                return glfwGetKey(window, GLFW_KEY_KP_MULTIPLY) == GLFW_PRESS;
            case "KPDIVIDE":
                return glfwGetKey(window, GLFW_KEY_KP_DIVIDE) == GLFW_PRESS;
            case "KPPERIOD":
                return glfwGetKey(window, GLFW_KEY_KP_DECIMAL) == GLFW_PRESS;
            case "F1":
                return glfwGetKey(window, GLFW_KEY_F1) == GLFW_PRESS;
            case "F2":
                return glfwGetKey(window, GLFW_KEY_F2) == GLFW_PRESS;
            case "F3":
                return glfwGetKey(window, GLFW_KEY_F3) == GLFW_PRESS;
            case "F4":
                return glfwGetKey(window, GLFW_KEY_F4) == GLFW_PRESS;
            case "F5":
                return glfwGetKey(window, GLFW_KEY_F5) == GLFW_PRESS;
            case "F6":
                return glfwGetKey(window, GLFW_KEY_F6) == GLFW_PRESS;
            case "F7":
                return glfwGetKey(window, GLFW_KEY_F7) == GLFW_PRESS;
            case "F8":
                return glfwGetKey(window, GLFW_KEY_F8) == GLFW_PRESS;
            case "F9":
                return glfwGetKey(window, GLFW_KEY_F9) == GLFW_PRESS;
            case "F10":
                return glfwGetKey(window, GLFW_KEY_F10) == GLFW_PRESS;
            case "F11":
                return glfwGetKey(window, GLFW_KEY_F11) == GLFW_PRESS;
            case "F12":
                return glfwGetKey(window, GLFW_KEY_F12) == GLFW_PRESS;
            case "INSERT":
                return glfwGetKey(window, GLFW_KEY_INSERT) == GLFW_PRESS;
            case "DELETE":
                return glfwGetKey(window, GLFW_KEY_DELETE) == GLFW_PRESS;
            case "BACKSPACE":
                return glfwGetKey(window, GLFW_KEY_BACKSPACE) == GLFW_PRESS;
            case "TAB":
                return glfwGetKey(window, GLFW_KEY_TAB) == GLFW_PRESS;
            case "NUMLOCK":
                return glfwGetKey(window, GLFW_KEY_NUM_LOCK) == GLFW_PRESS;
            case "POS1":
            case "HOME":
                return glfwGetKey(window, GLFW_KEY_HOME) == GLFW_PRESS;
            case "PGUP":
                return glfwGetKey(window, GLFW_KEY_PAGE_UP) == GLFW_PRESS;
            case "PGDOWN":
                return glfwGetKey(window, GLFW_KEY_PAGE_DOWN) == GLFW_PRESS;
            case "END":
                return glfwGetKey(window, GLFW_KEY_END) == GLFW_PRESS;
            case "LEFT":
                return glfwGetKey(window, GLFW_KEY_LEFT) == GLFW_PRESS;
            case "RIGHT":
                return glfwGetKey(window, GLFW_KEY_RIGHT) == GLFW_PRESS;
            case "UP":
                return glfwGetKey(window, GLFW_KEY_UP) == GLFW_PRESS;
            case "DOWN":
                return glfwGetKey(window, GLFW_KEY_DOWN) == GLFW_PRESS;
            default:
                break;
        }

        return false;
    }

    public boolean isMouseKeyDown(String mouseKey) {
        switch (mouseKey.toUpperCase().trim()) {
            case "LEFT":
                return glfwGetMouseButton(window, GLFW_MOUSE_BUTTON_LEFT) == GLFW_PRESS;
            case "RIGHT":
                return glfwGetMouseButton(window, GLFW_MOUSE_BUTTON_RIGHT) == GLFW_PRESS;
            case "MIDDLE":
                return glfwGetMouseButton(window, GLFW_MOUSE_BUTTON_MIDDLE) == GLFW_PRESS;
            default:
                break;
        }
        return false;
    }
}
