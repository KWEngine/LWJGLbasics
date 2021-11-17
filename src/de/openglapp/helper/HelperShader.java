package de.openglapp.helper;

import java.io.BufferedReader;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.util.Scanner;

import org.lwjgl.opengl.GL45;
import de.openglapp.shaderprogram.ShaderProgramMain;

public class HelperShader {
	public static String convertStreamToString(InputStream is) {
        java.util.Scanner s = new java.util.Scanner(is).useDelimiter("\\A");
        return s.hasNext() ? s.next() : "";
    }

	public static int loadShader(String filename, int shaderType, int program) {
        int address = GL45.glCreateShader(shaderType);
        String shaderCode = "";
        try {
            shaderCode = readFileAsString(filename);
        } catch (Exception ex) {

        }

        GL45.glShaderSource(address, shaderCode);
        GL45.glCompileShader(address);

        int status = GL45.glGetShaderi(address, GL45.GL_COMPILE_STATUS);
        if (status == GL45.GL_FALSE) {
            String error = GL45.glGetShaderInfoLog(address);
            System.err.println("Compile status: " + status + " for " + filename + " - " + error);
        }
        GL45.glAttachShader(program, address);
        return address;
    }

	private static String readFileAsString(String filename) throws Exception {

        InputStream is = ShaderProgramMain.class.getResourceAsStream(filename);
        StringBuilder source = new StringBuilder();
        Exception exception = null;

        BufferedReader reader;
        try {
            reader = new BufferedReader(new InputStreamReader(is, "UTF-8"));

            Exception innerExc = null;
            try {
                String line;
                while ((line = reader.readLine()) != null) {
                    source.append(line).append('\n');
                }
            } catch (Exception exc) {
                exception = exc;
            } finally {
                try {
                    reader.close();
                } catch (Exception exc) {
                    if (innerExc == null) {
                        innerExc = exc;
                    } else {
                        exc.printStackTrace();
                    }
                }
            }

            if (innerExc != null) {
                throw innerExc;
            }
        } catch (Exception exc) {
            exception = exc;
        } finally {
            try {
                is.close();
            } catch (Exception exc) {
                if (exception == null) {
                    exception = exc;
                } else {
                    exc.printStackTrace();
                }
            }

            if (exception != null) {
                throw exception;
            }
        }

        return source.toString().replaceAll("[^\\p{ASCII}]", "");
    }

	private static String loadFile(String filename) {
        StringBuilder code = new StringBuilder();
        String line = null;
        InputStream is = null;
        try {
            is = HelperShader.class.getResourceAsStream(filename);
            Scanner scanner = new Scanner(is);
            while (scanner.hasNextLine()) {
                line = scanner.nextLine();
                code.append(line).append("\n");
            }
            code.append("\0");
            scanner.close();
        } catch (Exception ex) {
            return "";
        }
        return code.toString();
    }

	public static int loadAndCompileShader(String filename, int shaderType) {
        // shader handle will be non zero if successfully created
        int handle = GL45.glCreateShader(shaderType);

        if (handle == 0) {
            throw new RuntimeException("could not created shader.");
        }

        // load code from file into String
        String code = loadFile(filename);

        // upload code to OpenGL and associate code with shader
        GL45.glShaderSource(handle, code);

        // compile source code into binary
        GL45.glCompileShader(handle);

        // acquire compilation status
        int shaderStatus = GL45.glGetShaderi(handle, GL45.GL_COMPILE_STATUS);

        // check whether compilation was successful
        if (shaderStatus == 0) {
            throw new IllegalStateException("compilation error for shader [" + filename + "]. Reason: " + GL45.glGetShaderInfoLog(handle, 1000));
        }

        return handle;
    }
}
